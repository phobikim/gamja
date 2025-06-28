window.addEventListener('DOMContentLoaded', async () => {
    try {
        const res = await fetch('/api/session-check', {
            credentials: 'include',
            cache: 'no-store'
        });

        const json = await res.json();

        if (json.code === 'MAINTENANCE') {
            location.href = './maintenance.html';
            return;
        }

        if (json.code === 'SUCCESS') {
            location.href = './char.html';
            return;
        }

        // 그 외 NO_VALID_USER면 stay

    } catch (e) {
        console.warn('세션 확인 실패:', e);
    }
});

// 로그인/회원가입 초기화 코드
(function initLoginPage() {
    // 요소 셀렉터
    const logo = document.getElementById('gamjadanLogo');
    const toggleBtn = document.getElementById("bgmToggleBtn");

    const adminModal = document.getElementById('adminModal');
    const adminLoginUserName = document.getElementById('LoginUsernameInput');
    const adminEnterBtn = document.getElementById('adminEnterBtn');
    const adminErrorText = document.getElementById('adminErrorText');

    const signupModal = document.getElementById('signupModal');
    const signupUsernameInput = document.getElementById('signupUsernameInput');
    const signupSubmitBtn = document.getElementById('signupSubmitBtn');
    const signupErrorText = document.getElementById('signupErrorText');
    const closeSignupBtn = document.getElementById('closeSignupBtn');
    const openSignupBtn = document.getElementById('openSignupBtn');

    const adminModalContent = document.querySelector('#adminModal .pin-modal-content');
    const signupModalContent = document.querySelector('#signupModal .pin-modal-content');

    const loginPinInputs = document.querySelectorAll('#adminModal .pin-input');
    const signupPinInputs = document.querySelectorAll('#signupModal .pin-input');

    // 유효성 검사
    function isValidUsername(str) {
        return /^[가-힣a-zA-Z0-9]+$/.test(str);
    }


    // 핀 이벤트 설정 (자동 로그인 버튼 연동 가능)
    function pinEvent(pinInputs, autoSubmitButton = null) {
        pinInputs.forEach((input, index) => {
            input.addEventListener('input', () => {
                tryResumeAudioContext();
                playEffect("se_input");
                if (input.value.length === 1 && index < pinInputs.length - 1) {
                    pinInputs[index + 1].focus();
                }
                if (autoSubmitButton && Array.from(pinInputs).every(p => p.value.length === 1)) {
                    setTimeout(() => autoSubmitButton.click(), 150);
                }
            });
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && input.value === '' && index > 0) {
                    pinInputs[index - 1].focus();
                }
            });
        });
    }

    pinEvent(loginPinInputs, adminEnterBtn);
    pinEvent(signupPinInputs);

    // BGM Toggle
    // toggleBtn.addEventListener("click", () => toggleBGM("bgm_main"));

    // 로그인 모달 열기
    logo.addEventListener('click', () => {
        tryResumeAudioContext();
        playEffect("se_click");
        adminModal.classList.remove('hidden');
        adminLoginUserName.value = '';
        loginPinInputs.forEach(input => input.value = '');
        adminLoginUserName.focus();
        adminErrorText.classList.add('hidden');
    });

    // 로그인 처리
    adminEnterBtn.addEventListener('click', async () => {
        tryResumeAudioContext();
        playEffect("se_input");
        const username = adminLoginUserName.value.trim();
        const pin = Array.from(loginPinInputs).map(i => i.value).join('');

        if (!username || !isValidUsername(username)) {
            adminErrorText.textContent = '아이디는 한글, 영어, 숫자만 입력할 수 있어요.';
            adminErrorText.classList.remove('hidden');
            adminLoginUserName.focus();
            return;
        }

        if (pin.length !== 4) {
            adminErrorText.textContent = 'PIN은 4자리를 모두 입력해주세요.';
            adminErrorText.classList.remove('hidden');
            loginPinInputs[0].focus();
            return;
        }

        try {
            const response = await apiRequest('/api/login', 'POST', { username, pin });
            if (response.code === 'SUCCESS') {
                location.href = './char.html';
            } else {
                showLoginError(response.message);
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            showLoginError('서버 오류가 발생했습니다.');
        }
    });

    function showLoginError(message) {
        adminErrorText.textContent = message;
        adminErrorText.classList.remove('hidden');
        loginPinInputs.forEach(i => i.value = '');
        loginPinInputs[0].focus();
        document.querySelector('.pin-input-group').classList.add('shake');
        setTimeout(() => document.querySelector('.pin-input-group').classList.remove('shake'), 500);
    }

    // 회원가입 모달 열기
    openSignupBtn.addEventListener('click', () => {
        adminModal.classList.add('hidden');
        signupModal.classList.remove('hidden');
        signupUsernameInput.value = '';
        signupPinInputs.forEach(input => input.value = '');
        setTimeout(() => signupUsernameInput.focus(), 150);
        signupErrorText.classList.add('hidden');
    });

    // 회원가입 처리
    signupSubmitBtn.addEventListener('click', async () => {
        const username = signupUsernameInput.value.trim();
        const pin = Array.from(signupPinInputs).map(i => i.value).join('');

        if (!username || !isValidUsername(username)) {
            signupErrorText.textContent = '아이디는 한글, 영어, 숫자만 입력할 수 있어요.';
            signupErrorText.classList.remove('hidden');
            return;
        }

        if( pin.length!== 4) {
            signupErrorText.textContent = 'PIN은 4자리를 모두 입력해주세요.';
            signupErrorText.classList.remove('hidden');
            return;
        }

        try {
            const response = await apiRequest('/api/signup', 'POST', { username, pin });
            if (response.code === 'SUCCESS') {
                location.href = './char.html';
            } else {
                signupErrorText.textContent = response.message || '회원 가입 실패';
                signupErrorText.classList.remove('hidden');
                signupPinInputs[0].focus();
            }
        } catch (error) {
            console.error('회원가입 실패:', error);
            signupErrorText.textContent = '서버 오류가 발생했습니다.';
            signupErrorText.classList.remove('hidden');
        }
    });

    // 모달 외부 클릭 시 닫기
    signupModal.addEventListener('click', (e) => {
        if (!signupModalContent.contains(e.target)) {
            signupModal.classList.add('hidden');
        }
    });

    adminModal.addEventListener('click', (e) => {
        if (!adminModalContent.contains(e.target)) {
            adminModal.classList.add('hidden');
        }
    });

    closeSignupBtn.addEventListener('click', () => signupModal.classList.add('hidden'));
})();
