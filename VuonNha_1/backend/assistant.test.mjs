import test from 'node:test';
import assert from 'node:assert/strict';
import {createGardenStore,sanitizeGarden,answerHistory,askGarden} from './assistant.mjs';
import {mobileGenerationConfig} from './gemini-config.mjs';
import {createScanServer} from './server.mjs';
const now=Date.parse('2026-10-11T04:00:00Z');
const garden={plants:[{id:1,name:'Bé Lá',species:'Kim tiền',location:'Ban công',profile:{light:'Gián tiếp',junk:'remove'},tasks:[{name:'Tưới',everyDays:7,lastDone:now}]}],logs:[{itemId:1,task:'Tưới nước',at:now-86400000}]};
test('mobile chat limits modern model effort and never displays thought parts',async()=>{
 const answer=await askGarden('Hỏi chăm cây',garden,[],{apiKey:'test-only',model:'gemini-3.8-flash',fetchImpl:async(_,options)=>{
  assert.equal(JSON.parse(options.body).generationConfig.thinkingConfig.thinkingLevel,'low');
  return new Response(JSON.stringify({candidates:[{finishReason:'STOP',content:{parts:[{thought:true,text:'hidden'},{text:'Gợi ý ngắn'}]}}]}));
 }});
 assert.equal(answer,'Gợi ý ngắn');
});
test('older and image models do not receive unsupported thinking-level settings',()=>{
 for(const model of ['gemini-2.5-flash','gemini-3.1-flash-lite-image'])assert.equal(mobileGenerationConfig(model,{maxOutputTokens:1500}).thinkingConfig,undefined);
});
test('garden whitelist removes photos, notes and unrelated fields',()=>{const g=sanitizeGarden({...garden,photo:'private',note:'private'});assert.equal(g.plants[0].profile.junk,undefined);assert.equal(g.photo,undefined);assert.equal(g.note,undefined);});
test('history uses actual log, never schedule baseline',()=>{assert.match(answerHistory('Hôm qua tôi tưới cây nào?',garden,now),/Bé Lá: Tưới nước/);assert.match(answerHistory('Hôm nay đã chăm sóc cây nào?',garden,now),/Chưa có ghi nhận/);});
test('planned care questions are not mistaken for completed care',()=>{
 assert.equal(answerHistory('Hôm nay tôi cần tưới cây nào?',garden,now),null);
 assert.equal(answerHistory('Hôm nay nên bón phân gì cho kim tiền?',garden,now),null);
});
test('multi-day and uncompleted-care questions are not answered as completed history',()=>{
 assert.equal(answerHistory('So sánh hôm qua và hôm nay tôi đã tưới cây nào?',garden,now),null);
 assert.equal(answerHistory('Hôm nay cây nào chưa được tưới?',garden,now),null);
});
test('history filters the requested plant and only actual watering or fertilizing tasks',()=>{
 const other={...garden.plants[0],id:2,name:'Chậu cửa sổ',species:'Trầu bà'};
 const g={plants:[...garden.plants,other],logs:[...garden.logs,{itemId:2,task:'Tưới nước',at:now-86400000},{itemId:1,task:'Kiểm tra phân bón',at:now-86400000},{itemId:1,task:'Bón phân',at:now-86400000},{itemId:1,task:'Tỉa lá',at:now-86400000}]};
 assert.doesNotMatch(answerHistory('Hôm qua tôi tưới kim tiền chưa?',g,now),/Chậu cửa sổ/);
 const answer=answerHistory('Hôm qua tôi tưới và bón cây nào?',g,now);
 assert.match(answer,/Bón phân/);assert.match(answer,/Tưới nước/);assert.doesNotMatch(answer,/Kiểm tra phân bón|Tỉa lá/);
});
test('garden rejects care cycles outside the supported year range',()=>{
 assert.throws(()=>sanitizeGarden({...garden,plants:[{...garden.plants[0],tasks:[{name:'Tưới',everyDays:366,lastDone:now}]}]}));
});
test('today follows device timezone at a day boundary',()=>{
 const clock=Date.parse('2026-10-11T20:00:00Z');
 const g={...garden,timeZone:'UTC',logs:[{itemId:1,task:'Tưới nước',at:Date.parse('2026-10-11T02:00:00Z')}]};
 assert.match(answerHistory('Hôm nay tôi tưới cây nào?',g,clock),/Bé Lá: Tưới nước/);
 assert.match(answerHistory('Hôm nay tôi tưới cây nào?',{...g,timeZone:'Asia/Ho_Chi_Minh'},clock),/Chưa có ghi nhận/);
 assert.equal(sanitizeGarden(g).timeZone,'UTC');
});
test('garden validates named and fixed-offset timezones',()=>{
 assert.throws(()=>sanitizeGarden({...garden,timeZone:'not-a-zone'}));
 assert.equal(sanitizeGarden({...garden,timeZone:'+07:00'}).timeZone,'+07:00');
});
test('yesterday is the previous local date across daylight-saving changes',()=>{
 const g={...garden,timeZone:'America/New_York',logs:[{itemId:1,task:'Tưới nước',at:Date.parse('2026-03-08T16:00:00Z')}]};
 assert.match(answerHistory('Hôm qua tôi tưới cây nào?',g,Date.parse('2026-03-09T04:30:00Z')),/2026-03-08/);
 assert.match(answerHistory('Hôm qua tôi tưới cây nào?',g,Date.parse('2026-03-09T04:30:00Z')),/Bé Lá/);
});
test('chat expires within 48 hours and gardens stay separate',()=>{let clock=now;const s=createGardenStore(':memory:',()=>clock);try{s.save('one',garden);s.append('one','question','answer');assert.equal(s.messages('two').length,0);assert.equal(s.garden('two').plants.length,0);clock+=48*60*60*1000;assert.equal(s.messages('one').length,0);assert.equal(s.garden('one').plants.length,1);s.forget('one');assert.equal(s.garden('one').plants.length,0);}finally{s.close();}});
test('backend chat authenticates garden and isolates history',async t=>{
 const s=createGardenStore();const server=createScanServer({token:'test',gardenStore:s,askImpl:async()=> 'Gợi ý AI'});
 server.listen(0,'127.0.0.1');await new Promise(r=>server.once('listening',r));t.after(()=>{server.closeAllConnections();server.close();s.close();});
 const url=`http://127.0.0.1:${server.address().port}/assistant`;
 const headers={authorization:'Bearer test','x-garden-key':'a'.repeat(72),'content-type':'application/json'};
 assert.equal((await fetch(url)).status,401);
 const response=await fetch(url,{method:'POST',headers,body:JSON.stringify({question:'Cây này chăm sao?',garden})});assert.equal(response.status,200);assert.equal((await response.json()).messages.length,2);
 assert.equal((await (await fetch(url,{headers:{...headers,'x-garden-key':'b'.repeat(72)}})).json()).messages.length,0);
 assert.equal((await fetch(url,{method:'POST',headers,body:JSON.stringify({question:'x',garden:{plants:[],logs:'bad'}})})).status,400);
 assert.equal((await fetch(url,{method:'POST',headers,body:'{broken'})).status,400);
 assert.equal((await fetch(url.replace('/assistant','/garden'),{method:'DELETE',headers})).status,200);
 assert.equal((await (await fetch(url,{headers})).json()).messages.length,0);
});

import { mkdtempSync, unlinkSync, rmdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
test('SQLite persists garden and unexpired chat across restart',()=>{
 const folder=mkdtempSync(join(tmpdir(),'vuonnha-store-test-'));const file=join(folder,'garden.sqlite');
 let store=createGardenStore(file,()=>now);
 try {store.save('one',sanitizeGarden(garden));store.append('one','Hỏi','Đáp');store.close();store=createGardenStore(file,()=>now);assert.equal(store.garden('one').plants[0].name,'Bé Lá');assert.equal(store.messages('one').length,2);}
 finally{store.close();unlinkSync(file);rmdirSync(folder);}
});
