
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







