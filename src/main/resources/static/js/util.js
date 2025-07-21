
/* 이미지 서버 경로  */
window.basePath_image = 'https://phobi.me/gamja.img/images';
window.basePath = 'https://phobi.me/gamja.img';

async function apiRequest(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        credentials: 'include',
        cache: 'no-store',
    };

    if (data) {
        options.body = new URLSearchParams(data).toString();
    }

    try {
        const response = await fetch(url, options);

        if (!response.ok) {
            const error = new Error('API 요청 실패');
            error.status = response.status;
            throw error;
        }

        return await response.json();
    } catch (err) {
        console.error('API 호출 에러:', err);
        throw err;
    }
}

async function apiRequestJson(url, method = 'POST', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        cache: 'no-store',
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        const resJson = await response.json(); // ⭐ 먼저 응답 JSON 파싱

        if (!response.ok) {
            // ⭐ 실패 응답도 그대로 throw (message 포함)
            throw resJson;
        }

        return resJson;
    } catch (err) {
        console.error('API 호출 에러:', err);

        // ✅ JSON 에러(message 포함)면 그대로 던지고
        if (err && err.message) {
            throw err;
        }

        // ✅ 그 외 네트워크/기타 에러는 fallback 처리
        throw new Error('알 수 없는 오류가 발생했습니다.');
    }
}
async function checkSessionValid() {
    try {
        const res = await fetch('/api/session-check', {
            credentials: 'include',
            cache: 'no-store'
        });

        if (!res.ok) throw new Error('세션 만료');
        return true;
    } catch (e) {
        showSessionExpiredModal();
        return false;
    }
}







