import test from 'node:test';
import assert from 'node:assert/strict';
import { createScanServer, recognize, validateResult } from './server.mjs';

const candidate = { name: 'Kim tiền', scientificName: 'Zamioculcas zamiifolia', confidence: 'medium',
  features: ['Lá kép, lá chét bóng'], careTips: 'Kiểm tra độ khô của đất trước khi tưới.', tasks: [{ name: 'Kiểm tra đất', everyDays: 7 }] };
const result = { candidates: [candidate] };
const photo = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 0, 0]).toString('base64');
async function running(t, config) {
  const server = createScanServer(config);
  server.listen(0, '127.0.0.1');
  await new Promise(resolve => server.once('listening', resolve));
  t.after(() => new Promise(resolve => { server.close(resolve); server.closeAllConnections(); }));
  return `http://127.0.0.1:${server.address().port}`;
}
const headers = { authorization: 'Bearer test-token', 'content-type': 'application/json' };

test('authenticated scan returns validated candidates', async t => {
  let called = 0;
  const url = await running(t, { apiKey: 'test-only', model: 'test-model', token: 'test-token', recognizeImpl: async image => {
    assert.equal(image, photo); called++; return result;
  } });
  const response = await fetch(`${url}/identify`, { method: 'POST', headers, body: JSON.stringify({ imageBase64: photo }) });
  assert.equal(response.status, 200); assert.deepEqual(await response.json(), result); assert.equal(called, 1);
});
test('missing configuration does not call provider', async t => {
  const url = await running(t, { token: 'test-token', recognizeImpl: () => assert.fail('must not call') });
  const response = await fetch(`${url}/identify`, { method: 'POST', headers, body: '{}' });
  assert.equal(response.status, 503);
});
test('rejects unauthenticated scans', async t => {
  const url = await running(t, { apiKey: 'test-only', model: 'test-model', token: 'test-token' });
  assert.equal((await fetch(`${url}/identify`, { method: 'POST', body: '{}' })).status, 401);
});
test('rejects non-JPEG images before provider call', async t => {
  const url = await running(t, { apiKey: 'test-only', model: 'test-model', token: 'test-token', recognizeImpl: () => assert.fail('must not call') });
  const response = await fetch(`${url}/identify`, { method: 'POST', headers, body: JSON.stringify({ imageBase64: 'dGVzdGltYWdl' }) });
  assert.equal(response.status, 400);
});
test('rejects invalid generated care cycles', () => {
  assert.throws(() => validateResult({ candidates: [{ ...candidate, tasks: [{ name: 'Tưới nước', everyDays: 0 }] }] }));
  assert.throws(() => validateResult({ candidates: [{ ...candidate, name: '' }] }));
});
test('sends JPEG and JSON schema to Gemini without exposing key in URL', async () => {
  const actual = await recognize(photo, { apiKey: 'test-only', model: 'gemini-2.5-flash', fetchImpl: async (url, options) => {
    assert.equal(url, 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent');
    assert.equal(options.headers['x-goog-api-key'], 'test-only');
    const request = JSON.parse(options.body);
    assert.equal(request.generationConfig.responseMimeType, 'application/json');
    assert.equal(request.generationConfig.responseJsonSchema.properties.candidates.maxItems, 3);
    assert.deepEqual(request.contents[0].parts[1].inlineData, { mimeType: 'image/jpeg', data: photo });
    return { ok: true, json: async () => ({ candidates: [{ finishReason: 'STOP', content: { parts: [{ text: JSON.stringify(result) }] } }] }) };
  } });
  assert.deepEqual(actual, result);
});

test('blocked and truncated Gemini responses never become guessed plants', async () => {
  for (const response of [{ promptFeedback: { blockReason: 'SAFETY' } }, { candidates: [{ finishReason: 'MAX_TOKENS' }] }]) {
    await assert.rejects(recognize(photo, { apiKey: 'test-only', model: 'test-model', fetchImpl: async () => ({ ok: true, json: async () => response }) }));
  }
});

test('Gemini quota failure is returned as retry-later without exposing provider details', async t => {
  const url = await running(t, { apiKey: 'test-only', model: 'test-model', token: 'test-token', recognizeImpl: image =>
    recognize(image, { apiKey: 'test-only', model: 'test-model', fetchImpl: async () => ({ ok: false, status: 429 }) }) });
  const response = await fetch(`${url}/identify`, { method: 'POST', headers, body: JSON.stringify({ imageBase64: photo }) });
  assert.equal(response.status, 429);
  assert.deepEqual(await response.json(), { error: 'scan-failed' });
});

test('empty candidates are supported for unrecognizable photos', () => {
  assert.deepEqual(validateResult({ candidates: [] }), { candidates: [] });
});
test('oversized payload never reaches provider', async t => {
  const url = await running(t, { apiKey: 'test-only', model: 'test-model', token: 'test-token', recognizeImpl: () => assert.fail('must not call') });
  const response = await fetch(`${url}/identify`, { method: 'POST', headers, body: JSON.stringify({ imageBase64: 'A'.repeat(2_000_001) }) });
  assert.equal(response.status, 413);
});
