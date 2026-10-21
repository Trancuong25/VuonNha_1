import test from 'node:test';
import assert from 'node:assert/strict';
import { createAccountStore } from './accounts.mjs';
import { createScanServer } from './server.mjs';
import { mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve, dirname, basename } from 'node:path';

const registration = { name: 'Người trồng cây', username: 'garden.test', password: 'plant-test-password-42' };

test('account registration normalizes username, rejects duplicates and wrong passwords', async () => {
  const store = createAccountStore();
  try {
    const first = await store.register({ ...registration, username: 'Garden.Test' });
    assert.equal(first.user.username, 'garden.test');
    assert.equal(first.user.name, registration.name);
    assert.equal(first.user.password, undefined);
    assert.equal(store.resolve(first.token).user.id, first.user.id);
    await assert.rejects(store.register(registration), e => e.status === 409);
    await assert.rejects(store.login({ ...registration, password: 'incorrect-password' }), e => e.status === 401);
    const next = await store.login(registration);
    assert.notEqual(next.token, first.token);
    assert.equal(next.user.id, first.user.id);
    store.logout(next.token);
    assert.equal(store.resolve(next.token), null);
    assert.ok(store.resolve(first.token));
  } finally { store.close(); }
});

test('accounts validate input and session expiry without accepting forged tokens', async () => {
  let now = 1000000; const store = createAccountStore(':memory:', () => now);
  try {
    await assert.rejects(store.register({ ...registration, username: 'a' }), e => e.status === 400);
    await assert.rejects(store.register({ ...registration, password: 'short' }), e => e.status === 400);
    const session = await store.register(registration);
    assert.equal(store.resolve('f'.repeat(64)), null);
    assert.equal(store.resolve([session.token]), null);
    now = session.expiresAt;
    assert.equal(store.resolve(session.token), null);
  } finally { store.close(); }
});

test('account and session persist after reopening SQLite', async () => {
  const folder = mkdtempSync(join(tmpdir(), 'vuonnha-account-')); const file = join(folder, 'accounts.sqlite');
  let store = createAccountStore(file);
  try {
    const session = await store.register(registration);
    store.close(); store = createAccountStore(file);
    assert.equal(store.resolve(session.token).user.id, session.user.id);
    assert.equal((await store.login(registration)).user.id, session.user.id);
  } finally {
    store.close();
    assert.equal(dirname(resolve(folder)), resolve(tmpdir()));
    assert.ok(basename(folder).startsWith('vuonnha-account-'));
    rmSync(folder, { recursive: true, force: true });
  }
});

test('HTTP accounts authenticate sessions and isolate assistant from guest and other accounts', async t => {
  const server = createScanServer({ token: 'dev', askImpl: async () => 'Thông tin thử' });
  server.listen(0, '127.0.0.1'); await new Promise(resolve => server.once('listening', resolve));
  t.after(() => new Promise(resolve => { server.close(resolve); server.closeAllConnections(); }));
  const url = `http://127.0.0.1:${server.address().port}`;
  const send = (path, body, session) => fetch(url + path, { method: 'POST', headers: { 'content-type': 'application/json', ...(session ? { 'x-account-session': session } : {}) }, body: JSON.stringify(body) });
  const first = await (await send('/auth/register', registration)).json();
  const second = await (await send('/auth/register', { ...registration, username: 'garden.other' })).json();
  assert.equal((await fetch(url + '/auth/me')).status, 401);
  assert.equal((await (await fetch(url + '/auth/me', { headers: { 'x-account-session': first.token } })).json()).user.username, registration.username);
  const headers = { authorization: 'Bearer dev', 'content-type': 'application/json', 'x-garden-key': 'g'.repeat(64), 'x-account-session': first.token };
  const response = await fetch(url + '/assistant', { method: 'POST', headers, body: JSON.stringify({ question: 'Hôm nay tôi đã chăm sóc cây nào?', garden: { plants: [], logs: [] } }) });
  assert.equal(response.status, 200);
  const own = await (await fetch(url + '/assistant', { headers })).json(); assert.equal(own.messages.length, 2);
  for (const session of [undefined, second.token]) {
    const others = { ...headers }; if (session) others['x-account-session'] = session; else delete others['x-account-session'];
    assert.equal((await (await fetch(url + '/assistant', { headers: others })).json()).messages.length, 0);
  }
  await send('/auth/logout', {}, first.token);
  assert.equal((await fetch(url + '/assistant', { headers })).status, 401);
});

test('HTTP rejects malformed account input and limits repeated attempts', async t => {
  const server = createScanServer({ token: 'dev' }); server.listen(0, '127.0.0.1'); await new Promise(resolve => server.once('listening', resolve));
  t.after(() => new Promise(resolve => { server.close(resolve); server.closeAllConnections(); }));
  const url = `http://127.0.0.1:${server.address().port}/auth/login`;
  const headers = { 'content-type': 'application/json' };
  assert.equal((await fetch(url, { method: 'POST', headers, body: '{broken' })).status, 400);
  for (let n = 0; n < 9; n++) assert.equal((await fetch(url, { method: 'POST', headers, body: '{}' })).status, 400);
  assert.equal((await fetch(url, { method: 'POST', headers, body: '{}' })).status, 429);
});

test('complete account backups persist and reject stale or malformed replacement', async () => {
  const dir = mkdtempSync(join(tmpdir(), 'vuonnha-backup-'));
  let store = createAccountStore(join(dir, 'accounts.sqlite'));
  try {
    const session = await store.register(registration);
    const bytes = Buffer.from('PK\x03\x04garden-photos-and-history');
    assert.equal(store.backup(session.user.id), null);
    assert.equal(store.saveBackup(session.user.id, 0, bytes).revision, 1);
    assert.throws(() => store.saveBackup(session.user.id, 0, bytes), e => e.status === 409);
    store.close(); store = createAccountStore(join(dir, 'accounts.sqlite'));
    assert.ok(store.resolve(session.token));
    assert.deepEqual(store.backup(session.user.id, true).payload, bytes);
    assert.throws(() => store.saveBackup(session.user.id, 1, Buffer.from('invalid')), e => e.status === 400);
    assert.equal(store.backup(session.user.id).revision, 1);
  } finally { store.close(); assert.equal(dirname(join(dir, 'accounts.sqlite')), dir); rmSync(dir, { recursive: true, force: true }); }
});

test('HTTP backups authenticate, isolate accounts, transfer bytes and guard revisions', async () => {
  const server = createScanServer({ token: 'test' });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const url = `http://127.0.0.1:${server.address().port}`;
  try {
    const register = async username => (await fetch(url + '/auth/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...registration, username }) })).json();
    const a = await register('backup.a'); const b = await register('backup.b');
    const bytes = Buffer.from('PK\x03\x04complete-archive');
    const headers = { 'X-Account-Session': a.token, 'Content-Type': 'application/zip', 'X-Backup-Revision': '0' };
    assert.equal((await fetch(url + '/auth/backup')).status, 401);
    const upload = await fetch(url + '/auth/backup', { method: 'PUT', headers, body: bytes });
    assert.equal(upload.status, 200); assert.equal((await upload.json()).backup.revision, 1);
    const download = await fetch(url + '/auth/backup', { headers: { 'X-Account-Session': a.token } });
    assert.equal(download.headers.get('content-type'), 'application/zip');
    assert.equal(download.headers.get('x-backup-checksum').length, 64);
    assert.deepEqual(Buffer.from(await download.arrayBuffer()), bytes);
    assert.equal((await fetch(url + '/auth/backup', { headers: { 'X-Account-Session': b.token } })).status, 404);
    assert.equal((await (await fetch(url + '/auth/backup/meta', { headers: { 'X-Account-Session': b.token } })).json()).backup, null);
    assert.equal((await fetch(url + '/auth/backup', { method: 'PUT', headers, body: bytes })).status, 409);
    assert.equal((await fetch(url + '/auth/backup', { method: 'PUT', headers: { ...headers, 'X-Backup-Revision': 'x' }, body: bytes })).status, 400);
    await fetch(url + '/auth/logout', { method: 'POST', headers: { 'X-Account-Session': a.token } });
    assert.equal((await fetch(url + '/auth/backup/meta', { headers: { 'X-Account-Session': a.token } })).status, 401);
  } finally { await new Promise(resolve => server.close(resolve)); }
});
