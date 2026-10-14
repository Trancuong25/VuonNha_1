import test from 'node:test';
import assert from 'node:assert/strict';
import { createScanServer } from './server.mjs';
const plant={name:'Táo',scientificName:'Malus domestica',matureHeight:'',humidity:'',soil:'',light:'',source:'AI',tasks:[]};
test('unknown catalog names use authenticated online lookup', async t=>{
 let calls=0;
 const server=createScanServer({apiKey:'test',model:'test',token:'test',lookupImpl:async q=>{assert.equal(q,'cây táo');calls++;return [plant];}});
 server.listen(0,'127.0.0.1');await new Promise(r=>server.once('listening',r));
 t.after(()=>{server.closeAllConnections();server.close();});
 const url=`http://127.0.0.1:${server.address().port}/plants?q=${encodeURIComponent('cây táo')}`;
 assert.equal((await fetch(url)).status,401);
 const response=await fetch(url,{headers:{authorization:'Bearer test'}});
 assert.equal(response.status,200);assert.deepEqual((await response.json()).plants,[plant]);assert.equal(calls,1);
});
test('missing AI configuration is reported instead of empty catalog',async t=>{
 const server=createScanServer({token:'test'});server.listen(0,'127.0.0.1');await new Promise(r=>server.once('listening',r));
 t.after(()=>{server.closeAllConnections();server.close();});
 assert.equal((await fetch(`http://127.0.0.1:${server.address().port}/plants?q=apple`,{headers:{authorization:'Bearer test'}})).status,503);
});
