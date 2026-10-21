import { DatabaseSync } from 'node:sqlite';
import { randomBytes, createHash, scrypt, timingSafeEqual } from 'node:crypto';
import { promisify } from 'node:util';
import { mkdirSync } from 'node:fs';
import { dirname } from 'node:path';

const derive = promisify(scrypt);
const hash = value => createHash('sha256').update(value).digest('hex');
const lifetime = 7 * 24 * 60 * 60 * 1000;
const failure = (status, message) => Object.assign(new Error(message), { status });
const usernameOf = value => {
  if (typeof value !== 'string' || !/^[a-zA-Z0-9_.-]{3,40}$/.test(value.trim())) throw failure(400, 'Tên đăng nhập cần 3–40 ký tự: chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang.');
  return value.trim().toLowerCase();
};
const passwordOf = value => {
  if (typeof value !== 'string' || value.length < 10 || value.length > 128) throw failure(400, 'Mật khẩu cần từ 10 đến 128 ký tự.');
  return value;
};

export function createAccountStore(path = ':memory:', now = Date.now) {
  if (path !== ':memory:') mkdirSync(dirname(path), { recursive: true });
  const db = new DatabaseSync(path);
  db.exec(`PRAGMA foreign_keys=ON; PRAGMA secure_delete=ON;
    CREATE TABLE IF NOT EXISTS accounts(id TEXT PRIMARY KEY, username TEXT UNIQUE NOT NULL, name TEXT NOT NULL, salt TEXT NOT NULL, digest TEXT NOT NULL, gardenKey TEXT NOT NULL);
    CREATE TABLE IF NOT EXISTS sessions(digest TEXT PRIMARY KEY, accountId TEXT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE, expires INTEGER NOT NULL);
    CREATE TABLE IF NOT EXISTS backups(accountId TEXT PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE, revision INTEGER NOT NULL, updatedAt INTEGER NOT NULL, checksum TEXT NOT NULL, payload BLOB NOT NULL);`);
  const publicUser = row => ({ id: row.id, username: row.username, name: row.name });
  const prune = () => db.prepare('DELETE FROM sessions WHERE expires<=?').run(now());
  function session(account) {
    prune();
    const token = randomBytes(32).toString('hex');
    const expiresAt = now() + lifetime;
    db.prepare('INSERT INTO sessions VALUES(?,?,?)').run(hash(token), account.id, expiresAt);
    db.prepare('DELETE FROM sessions WHERE accountId=? AND digest NOT IN (SELECT digest FROM sessions WHERE accountId=? ORDER BY expires DESC, rowid DESC LIMIT 5)').run(account.id, account.id);
    return { user: publicUser(account), token, expiresAt };
  }
  return {
    async register(body) {
      const username = usernameOf(body?.username); const password = passwordOf(body?.password);
      if (typeof body?.name !== 'string' || !body.name.trim() || body.name.trim().length > 80) throw failure(400, 'Tên hiển thị cần từ 1 đến 80 ký tự.');
      const salt = randomBytes(16).toString('hex');
      const digest = (await derive(password, salt, 64, { N: 32768, r: 8, p: 1, maxmem: 64 * 1024 * 1024 })).toString('hex');
      const account = { id: randomBytes(16).toString('hex'), username, name: body.name.trim(), salt, digest, gardenKey: randomBytes(32).toString('hex') };
      try { db.prepare('INSERT INTO accounts VALUES(?,?,?,?,?,?)').run(account.id, username, account.name, salt, digest, account.gardenKey); }
      catch (e) { if (e.code?.startsWith('ERR_SQLITE_CONSTRAINT') || db.prepare('SELECT id FROM accounts WHERE username=?').get(username)) throw failure(409, 'Tên đăng nhập này đã được sử dụng.'); throw e; }
      return session(account);
    },
    async login(body) {
      const username = usernameOf(body?.username); const password = passwordOf(body?.password);
      const account = db.prepare('SELECT * FROM accounts WHERE username=?').get(username);
      const digest = await derive(password, account?.salt || 'missing-account-salt', 64, { N: 32768, r: 8, p: 1, maxmem: 64 * 1024 * 1024 });
      if (!account || !timingSafeEqual(digest, Buffer.from(account.digest, 'hex'))) throw failure(401, 'Tên đăng nhập hoặc mật khẩu chưa đúng.');
      return session(account);
    },
    resolve(token) {
      if (typeof token !== 'string' || !/^[a-f0-9]{64}$/.test(token)) return null;
      prune();
      const row = db.prepare('SELECT a.* FROM accounts a JOIN sessions s ON a.id=s.accountId WHERE s.digest=? AND s.expires>?').get(hash(token), now());
      return row ? { user: publicUser(row), gardenKey: row.gardenKey } : null;
    },
    logout(token) { if (typeof token === 'string') db.prepare('DELETE FROM sessions WHERE digest=?').run(hash(token)); },
    backup(accountId, includeData = false) {
      const row = db.prepare(`SELECT revision,updatedAt,checksum,length(payload) AS bytes${includeData ? ',payload' : ''} FROM backups WHERE accountId=?`).get(accountId);
      return row ? { ...row, ...(includeData ? { payload: Buffer.from(row.payload) } : {}) } : null;
    },
    saveBackup(accountId, expectedRevision, payload) {
      const current = db.prepare('SELECT revision FROM backups WHERE accountId=?').get(accountId);
      if ((current?.revision || 0) !== expectedRevision) throw failure(409, 'Bản lưu đã thay đổi trên thiết bị khác. Hãy kiểm tra lại trước khi lưu.');
      if (!Buffer.isBuffer(payload) || payload.length < 4 || payload.length > 25_000_000 || payload[0] !== 0x50 || payload[1] !== 0x4b) throw failure(400, 'Bản sao lưu ZIP không hợp lệ.');
      db.prepare('INSERT INTO backups VALUES(?,?,?,?,?) ON CONFLICT(accountId) DO UPDATE SET revision=excluded.revision,updatedAt=excluded.updatedAt,checksum=excluded.checksum,payload=excluded.payload').run(accountId, expectedRevision + 1, now(), hash(payload), payload);
      return this.backup(accountId);
    },
    close() { db.close(); }
  };
}

export function createAccountHandler(store) {
  const limits = new Map();
  const busy = new Set();
  return async (req, send, res) => {
    const token = req.headers['x-account-session'];
    if (req.url === '/auth/backup' || req.url === '/auth/backup/meta') {
      const account = store.resolve(token);
      if (!account) return send(401, { error: 'Phiên đăng nhập đã hết hạn. Hãy đăng nhập lại.' });
      if (req.method === 'GET' && req.url === '/auth/backup/meta') return send(200, { backup: store.backup(account.user.id) });
      if (req.method === 'GET' && req.url === '/auth/backup') {
        const backup = store.backup(account.user.id, true);
        if (!backup) return send(404, { error: 'Tài khoản chưa có bản lưu.' });
        res.writeHead(200, { 'Content-Type': 'application/zip', 'Content-Length': backup.bytes, 'Cache-Control': 'no-store', 'X-Backup-Revision': backup.revision, 'X-Backup-Checksum': backup.checksum });
        return res.end(backup.payload);
      }
      if (req.method !== 'PUT' || req.url !== '/auth/backup') return send(405, { error: 'method-not-allowed' });
      if (!req.headers['content-type']?.startsWith('application/zip')) return send(415, { error: 'zip-required' });
      const revision = Number(req.headers['x-backup-revision']);
      if (!/^\d+$/.test(req.headers['x-backup-revision'] || '') || !Number.isSafeInteger(revision)) return send(400, { error: 'invalid-revision' });
      const key = account.user.id;
      if (busy.has(key)) return send(429, { error: 'Đang lưu dữ liệu. Bạn thử lại sau nhé.' });
      busy.add(key);
      try {
        let size = 0; const chunks = [];
        for await (const chunk of req) { size += chunk.length; if (size > 25_000_000) return send(413, { error: 'Bản lưu vượt 25 MB. Hãy xuất ZIP trên thiết bị để giữ đủ ảnh.' }); chunks.push(chunk); }
        return send(200, { backup: store.saveBackup(key, revision, Buffer.concat(chunks)) });
      } catch (e) { return send(e.status || 500, { error: e.status ? e.message : 'Chưa lưu được dữ liệu. Bản lưu trước vẫn được giữ.' }); }
      finally { busy.delete(key); }
    }
    if (req.url === '/auth/me' && req.method === 'GET') {
      const account = store.resolve(token);
      return account ? send(200, { user: account.user }) : send(401, { error: 'Phiên đăng nhập đã hết hạn. Hãy đăng nhập lại.' });
    }
    if (req.url === '/auth/logout' && req.method === 'POST') { store.logout(token); return send(200, { ok: true }); }
    if (!['/auth/register', '/auth/login'].includes(req.url) || req.method !== 'POST') return send(404, { error: 'not-found' });
    if (!req.headers['content-type']?.startsWith('application/json')) return send(415, { error: 'json-required' });
    const now = Date.now(); const address = req.socket.remoteAddress || 'local';
    for (const [key, value] of limits) if (value.until <= now) limits.delete(key);
    const rate = limits.get(address) || { until: now + 60000, count: 0 };
    if (busy.has(address) || rate.count >= 10 || limits.size > 1000) return send(429, { error: 'Bạn thử quá nhiều lần. Vui lòng chờ một phút.' });
    rate.count++; limits.set(address, rate); busy.add(address);
    try {
      let length = 0; const chunks = [];
      for await (const chunk of req) { length += chunk.length; if (length > 4096) return send(413, { error: 'Dữ liệu đăng nhập quá dài.' }); chunks.push(chunk); }
      let body;
      try { body = JSON.parse(Buffer.concat(chunks).toString('utf8')); } catch { throw failure(400, 'Dữ liệu đăng nhập không hợp lệ.'); }
      return send(200, await (req.url === '/auth/register' ? store.register(body) : store.login(body)));
    } catch (e) { return send(e.status || 500, { error: e.status ? e.message : 'Chưa xử lý được tài khoản. Hãy thử lại.' }); }
    finally { busy.delete(address); }
  };
}
