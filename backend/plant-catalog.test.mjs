import test from 'node:test';
import assert from 'node:assert/strict';
import { lookupPlants } from './plant-catalog.mjs';
import { createScanServer } from './server.mjs';
test('lookup accepts Vietnamese accents, aliases and scientific names',()=>{
 assert.equal(lookupPlants('kim tiền')[0].id,'zz');
 assert.equal(lookupPlants('ho diep')[0].id,'orchid');
 assert.equal(lookupPlants('Monstera deliciosa')[0].id,'monstera');
 assert.deepEqual(lookupPlants('cay chua co'),[]);
});
test('catalog works without Gemini credentials and does not infer cultivar or actual height', async t=>{
 const server=createScanServer({recognizeImpl:()=>assert.fail('No AI call allowed')});
 server.listen(0,'127.0.0.1'); await new Promise(r=>server.once('listening',r));
 t.after(()=>new Promise(r=>{server.close(r);server.closeAllConnections();}));
 const response=await fetch(`http://127.0.0.1:${server.address().port}/plants?q=lan`);
 assert.equal(response.status,200);
 const result=await response.json(); assert.equal(result.plants[0].name,'Lan hồ điệp');
 assert.equal(result.plants[0].actualHeight,undefined);assert.equal(result.plants[0].cultivar,undefined);
 assert.equal((await fetch(`http://127.0.0.1:${server.address().port}/plants?q=a`)).status,400);
});
