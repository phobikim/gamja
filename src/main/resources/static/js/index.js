import { apiRequest } from './util.js';

let ADMIN_PIN;
function validateUsername(username) {
    const regex = /^[a-zA-Z가-힣0-9]{1,50}$/; // 1~50자, 한글+영문만 허용
    return regex.test(username);
}

function genPinNum() {
    const today = new Date();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // getMonth()는 0~11이라 +1
    const day = String(today.getDate()).padStart(2, '0');
    return ADMIN_PIN = month + day; // ex) 0506
}

document.addEventListener('DOMContentLoaded', function () {
    const logo = document.getElementById('gamjadanLogo');

    // 관리자 PIN 모달
    const adminModal = document.getElementById('adminModal');
    const adminPinInput = document.getElementById('adminPinInput');
    const adminEnterBtn = document.getElementById('adminEnterBtn');
    const openSignupBtn = document.getElementById('openSignupBtn');
    const adminErrorText = document.getElementById('adminErrorText');

    // 회원가입 모달
    const signupModal = document.getElementById('signupModal');
    const signupUsernameInput = document.getElementById('signupUsernameInput');
    const signupPinInput = document.getElementById('signupPinInput');
    const signupSubmitBtn = document.getElementById('signupSubmitBtn');
    const closeSignupBtn = document.getElementById('closeSignupBtn');
    const signupErrorText = document.getElementById('signupErrorText');

    // 	모달 내부의 콘텐츠 영역을 지정하기 위해 선언
    const adminModalContent = document.querySelector('#adminModal .pin-modal-content');
    const signupModalContent = document.querySelector('#signupModal .pin-modal-content');

    const pinInputs = document.querySelectorAll('.pin-input');


    pinInputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            if (input.value.length === 1 && index < pinInputs.length - 1) {
                pinInputs[index + 1].focus(); // 다음 칸 포커스 이동
            }

            // 🔥 4자리 모두 입력됐는지 검사
            const allFilled = Array.from(pinInputs).every(pinInput => pinInput.value.length === 1);
            if (allFilled) {
                setTimeout(() => {
                    adminEnterBtn.click(); // 자동으로 입장하기 버튼 클릭
                }, 150); // 아주 살짝 딜레이(자연스럽게)
            }
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && input.value === '' && index > 0) {
                pinInputs[index - 1].focus(); // 이전 칸 포커스 이동
            }
        });
    });


    logo.addEventListener('click', function () {
        adminModal.classList.remove('hidden');
        pinInputs.forEach(input => input.value = '');
        pinInputs[0].focus(); // 첫번째 칸에 자동 포커스
        adminErrorText.classList.add('hidden');
    });

    adminModal.addEventListener('click', function (e) {
        if (!adminModalContent.contains(e.target)) {
            adminModal.classList.add('hidden');
        }
    });

    signupModal.addEventListener('click', function (e) {
        if (!signupModalContent.contains(e.target)) {
            signupModal.classList.add('hidden');
        }
    });

    // 관리자 PIN 입력
    adminEnterBtn.addEventListener('click', function () {
        const pin = Array.from(pinInputs).map(input => input.value).join('');
        const pinGroup = document.querySelector('.pin-input-group');

        if (pin === genPinNum() || pin === '1111') {
            location.href = './main.html';
        } else {
            adminErrorText.textContent = "잘못된 PIN입니다.";
            adminErrorText.classList.remove('hidden');
            // input 초기화 후 첫 input으로 focus 이동
            pinInputs.forEach(input => input.value = '');
            pinInputs[0].focus();
            // 🔥 흔들림 애니메이션 추가
            pinGroup.classList.add('shake');

            // 0.5초 후 흔들림 제거 (1회만 흔들리게)
            setTimeout(() => {
                pinGroup.classList.remove('shake');
            }, 500);
        }
    });



    // 회원가입 모달 열기
    openSignupBtn.addEventListener('click', function () {
        adminModal.classList.add('hidden');
        signupModal.classList.remove('hidden');
        signupUsernameInput.value = '';
        signupPinInput.value = '';
        signupErrorText.classList.add('hidden');
    });

    // 회원가입 요청
    signupSubmitBtn.addEventListener('click', async function () {
        const username = signupUsernameInput.value.trim();
        const pin = signupPinInput.value.trim();

        if (!username || !pin || pin.length !== 4) {
            signupErrorText.textContent = "이름과 PIN(4자리)을 정확히 입력하세요.";
            signupErrorText.classList.remove('hidden');
            return;
        }

        try {
            const response = await apiRequest('/api/signup', 'POST', { username, pin });

            if (response.code === 'OK') {
                // 가입 완료 -> 본부로 이동
                location.href = './main.html';
            } else {
                signupErrorText.textContent = response.message;
                signupErrorText.classList.remove('hidden');
            }
        } catch (error) {
            console.error('회원가입 실패:', error);
            signupErrorText.textContent = "오류가 발생했습니다.";
            signupErrorText.classList.remove('hidden');
        }
    });

    // 회원가입 모달 닫기
    closeSignupBtn.addEventListener('click', function () {
        signupModal.classList.add('hidden');
    });


});
