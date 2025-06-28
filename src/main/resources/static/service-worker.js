self.addEventListener('install', event => {
    console.log('Service Worker installing.');
    self.skipWaiting(); // 즉시 활성화
});

self.addEventListener('activate', event => {
    console.log('Service Worker activating.');
});

self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);

    if (url.pathname.startsWith('/api/')) {
        // ✅ API는 항상 네트워크 요청
        event.respondWith(fetch(event.request));
        return;
    }

    // ✅ 정적 리소스 (이미지, CSS 등)은 캐시 우선
    event.respondWith(
        caches.match(event.request).then(cachedResponse => {
            return cachedResponse || fetch(event.request);
        })
    );
});

