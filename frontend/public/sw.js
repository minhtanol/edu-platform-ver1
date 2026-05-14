const CACHE = 'edu-platform-v2';
self.addEventListener('install', event => event.waitUntil(caches.open(CACHE).then(cache => cache.addAll(['/', '/manifest.webmanifest']))));
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;
  event.respondWith(fetch(event.request).then(res => { const copy = res.clone(); caches.open(CACHE).then(c => c.put(event.request, copy)); return res; }).catch(() => caches.match(event.request)));
});
self.addEventListener('sync', event => { if (event.tag === 'edu-background-sync') event.waitUntil(Promise.resolve()); });
