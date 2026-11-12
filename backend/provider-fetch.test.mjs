import test from 'node:test';
import assert from 'node:assert/strict';
import {providerFetch} from './provider-fetch.mjs';
test('retry only temporary 503 once, never quota 429',async()=>{
 const original=globalThis.fetch;let calls=0;
 try{
  globalThis.fetch=async()=>new Response('{}',{status:++calls===1?503:200});
  assert.equal((await providerFetch('https://example.test',{})).status,200);assert.equal(calls,2);
  calls=0;globalThis.fetch=async()=>{calls++;return new Response('{}',{status:429});};
  assert.equal((await providerFetch('https://example.test',{})).status,429);assert.equal(calls,1);
 }finally{globalThis.fetch=original;}
});
