// A temporary provider outage may recover on one bounded retry. Quota failures are never retried.
export async function providerFetch(url, options) {
 const response=await fetch(url,options);
 if(response.status!==503)return response;
 await response.body?.cancel();
 await new Promise(resolve=>setTimeout(resolve,1000));
 options.signal?.throwIfAborted();
 return fetch(url,options);
}
