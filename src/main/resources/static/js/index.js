window.addEventListener("click", async () => {
    await toggleBGM("bgm_main");
}, { once: true });

document.addEventListener('DOMContentLoaded', function () {
    // logo
    const logo = document.getElementById('gamjadanLogo');
    // BGM
    const toggleBtn = document.getElementById("bgmToggleBtn");

    // 로그인 모달
    const adminModal = document.getElementById('adminModal');
    const adminLoginUserName = document.getElementById('LoginUsernameInput');
    const adminEnterBtn = document.getElementById('adminEnterBtn');
    const openSignupBtn = document.getElementById('openSignupBtn');
    const adminErrorText = document.getElementById('adminErrorText');

    // 회원가입 모달
    const signupModal = document.getElementById('signupModal');
    const signupUsernameInput = document.getElementById('signupUsernameInput');
    const signupSubmitBtn = document.getElementById('signupSubmitBtn');
    const closeSignupBtn = document.getElementById('closeSignupBtn');
    const signupErrorText = document.getElementById('signupErrorText');

    // 	모달 내부의 콘텐츠 영역을 지정하기 위해 선언
    const adminModalContent = document.querySelector('#adminModal .pin-modal-content');
    const signupModalContent = document.querySelector('#signupModal .pin-modal-content');


    const loginPinInputs = document.querySelectorAll('#adminModal .pin-input');
    const signupPinInputs = document.querySelectorAll('#signupModal .pin-input');
    const loginEnterBtn = document.getElementById('adminEnterBtn');
    // 로그인용: 자동 로그인 버튼 클릭 포함
    pinEvent(loginPinInputs, loginEnterBtn);
    // 회원가입용: 자동 클릭 없음
    pinEvent(signupPinInputs);

    // BGM Toggle
    toggleBtn.addEventListener("click", () => {
        toggleBGM("bgm_main");
    });

    // 1. 로고 클릭 > 로그인 모달 열기
    logo.addEventListener('click', function () {
        // 사운드 재생
        playEffect("se_click");

        adminModal.classList.remove('hidden');
        adminLoginUserName.value = '';
        loginPinInputs.forEach(input => input.value = '');

        // 사용자 이름에 포커스
        adminLoginUserName.focus();
        adminErrorText.classList.add('hidden');
    });


    // 로그인 모달 이벤트
    adminEnterBtn.addEventListener('click', async function () {
        playEffect("se_input");
        const username = document.getElementById('LoginUsernameInput').value.trim();
        const pin = Array.from(loginPinInputs).map(input => input.value).join('');

        if (!username || pin.length !== 4) {
            adminErrorText.textContent = '이름과 PIN을 모두 입력해주세요.';
            adminErrorText.classList.remove('hidden');
            loginPinInputs[0].focus();
            return;
        }

        try {
            const response = await apiRequest('/api/login', 'POST', { username, pin });

            if (response.code === 'SUCCESS') {
                // 가입 완료 -> 본부로 이동
                const userId = response.data.userId;
                localStorage.setItem('userId', userId);
                location.href = './char.html';
            } else {
                adminErrorText.textContent = response.message || '로그인 실패. 정보를 확인해주세요.';
                adminErrorText.classList.remove('hidden');
                loginPinInputs.forEach(input => input.value = '');
                loginPinInputs[0].focus();
                // 🔥 흔들림 애니메이션 추가
                const pinGroup = document.querySelector('.pin-input-group');
                pinGroup.classList.add('shake');

                // 0.5초 후 흔들림 제거 (1회만 흔들리게)
                setTimeout(() => {
                    pinGroup.classList.remove('shake');
                }, 500);
            }
        } catch (error) {
            console.error('로그인 오류:', error);
            adminErrorText.textContent = response.message || '서버 오류가 발생했습니다.';
            adminErrorText.classList.remove('hidden');
        }

    });

    // 회원가입 모달 열기
    openSignupBtn.addEventListener('click', function () {
        adminModal.classList.add('hidden');
        signupModal.classList.remove('hidden');

        // 입력칸 초기화
        signupUsernameInput.value = '';
        signupPinInputs.forEach(input => input.value = '');

        signupUsernameInput.focus();
        signupErrorText.classList.add('hidden');
    });

    // 회원가입 요청
    signupSubmitBtn.addEventListener('click', async function () {
        const username = signupUsernameInput.value.trim();
        const pin = Array.from(signupPinInputs).map(input => input.value).join('');

        if (!username || pin.length !== 4) {
            adminErrorText.textContent = '이름과 PIN을 모두 입력해주세요.';
            adminErrorText.classList.remove('hidden');
            return;
        }

        try {
            const response = await apiRequest('/api/signup', 'POST', { username, pin });

            if (response.code === 'SUCCESS') {
                // 가입 완료 -> 본부로 이동
                const userId = response.data.userId;
                localStorage.setItem('userId', userId);
                location.href = './char.html';
            } else {
                signupErrorText.textContent = response.message || '회원 가입 실패';
                signupErrorText.classList.remove('hidden');
                signupPinInputs[0].focus();
            }
        } catch (error) {
            console.error('회원가입 실패:', error);
            signupErrorText.textContent = response.message || '서버 오류가 발생했습니다.';
            signupErrorText.classList.remove('hidden');
        }
    });

    // 모달 닫기

    closeSignupBtn.addEventListener('click', function (e){
        signupModal.classList.add('hidden');
    })
    signupModal.addEventListener('click', function (e) {
        if (!signupModalContent.contains(e.target)) {
            signupModal.classList.add('hidden');
        }
    });

    adminModal.addEventListener('click', function (e) {
        if (!adminModalContent.contains(e.target)) {
            adminModal.classList.add('hidden');
        }
    });

    function pinEvent(pinInputs, autoSubmitButton = null) {
        pinInputs.forEach((input, index) => {
            input.addEventListener('input', () => {
                playEffect("se_input");

                if (input.value.length === 1 && index < pinInputs.length - 1) {
                    pinInputs[index + 1].focus();
                }

                const allFilled = Array.from(pinInputs).every(pin => pin.value.length === 1);
                if (allFilled && autoSubmitButton) {
                    setTimeout(() => {
                        autoSubmitButton.click();
                    }, 150);
                }
            });

            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && input.value === '' && index > 0) {
                    pinInputs[index - 1].focus();
                }
            });
        });
    }

});
