
/* 이미지 서버 경로  */
window.basePath_image = 'https://phobi.me/gamja.img/images';
window.basePath = 'https://phobi.me/gamja.img';

async function apiRequest(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };

    if (data) {
        options.body = new URLSearchParams(data).toString();
    }

    try {
        const response = await fetch(url, options);
        return await response.json();
    } catch (error) {
        console.error('API 호출 에러:', error);
        throw error;
    }
}

async function apiRequestJson(url, method = 'POST', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        return await response.json();
    } catch (error) {
        console.error('API 호출 에러:', error);
        throw error;
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







