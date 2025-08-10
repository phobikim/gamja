
/* 이미지 서버 경로  */
window.basePath_image = 'https://phobi.me/gamja.img/images';
window.basePath = 'https://phobi.me/gamja.img';

async function apiRequest(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        credentials: 'include',
        cache: 'no-store',
    };
    if (data) options.body = new URLSearchParams(data).toString();

    try {
        const response = await fetch(url, options);

        // ★ 여기서 401 공통 처리
        if (response.status === 401) {
            redirectToIndex();
            throw new Error('세션 만료');
        }

        if (!response.ok) {
            const error = new Error('API 요청 실패');
            error.status = response.status;
            throw error;
        }
        return await response.json();
    } catch (err) {
        // 네트워크 에러이지만 서버가 JSON 바디로 에러를 준 경우 대비
        if (err?.status === 401 || err?.message === '세션이 없습니다') {
            redirectToIndex();
        }
        console.error('API 호출 에러:', err);
        throw err;
    }
}

async function apiRequestJson(url, method = 'POST', data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        cache: 'no-store',
    };
    if (data) options.body = JSON.stringify(data);

    try {
        const response = await fetch(url, options);
        const resJson = await response.json();

        // ★ 여기서 401 공통 처리
        if (response.status === 401 || resJson?.status === 401) {
            redirectToIndex();
            throw resJson; // 상위에서 필요하면 메시지 사용
        }

        if (!response.ok) {
            throw resJson; // 실패지만 메시지 포함해 던짐
        }
        return resJson;
    } catch (err) {
        if (err?.status === 401 || err?.message === '세션이 없습니다') {
            redirectToIndex();
        }
        console.error('API 호출 에러:', err);
        // JSON 에러면 그대로, 그 외는 fallback
        if (err && err.message) throw err;
        throw new Error('알 수 없는 오류가 발생했습니다.');
    }
}

async function checkSessionValid() {
    try {
        const res = await fetch('/api/session-check', {
            credentials: 'include',
            cache: 'no-store'
        });

        // 응답이 실패(401 등)일 경우 처리
        if (!res.ok) {
            // 401 Unauthorized면 index.html로 이동
            if (res.status === 401) {
                window.location.href = '/index.html';
                return false;
            }
            throw new Error('세션 만료');
        }
        return true;
    } catch (e) {
        // 예외 발생 시에도 index.html로 이동
        window.location.href = '/index.html';
        return false;
    }
}

function redirectToIndex() {
    const target = '/index.html';
    if (window.location.pathname !== target) {
        window.location.replace(target);
    }
}





