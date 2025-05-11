import { apiRequest, apiRequestJson } from './util.js';
import { showMessageModal } from './modal.js';


document.addEventListener('DOMContentLoaded', async function () {
    const membersContainer = document.getElementById('membersContainer');
    const logoImg = document.querySelector('.logo-img');

    if (logoImg) {
        logoImg.addEventListener('click', () => {
            location.href = './index.html'; // index.html로 이동
        });
    }

    try {
        const response = await apiRequest('/api/main/list', 'GET');
        const members = response.data;

        members.forEach(member => {
            const card = document.createElement('div');
            card.className = 'member-card';

            const imgPath = './images/character/'
            // 캐릭터 이미지 (없으면 기본 default.png)
            const img = document.createElement('img');
            img.src = member.characterImage ? imgPath+member.characterImage : './images/character/default.png';
            img.alt = member.username || member.username;
            img.className = 'member-img';

            // 이름 (nickname 없으면 username 표시)
            const name = document.createElement('p');
            name.className = 'characterName';
            name.textContent = member.usernickname ? member.usernickname : member.username;

            // 레벨
            const level = document.createElement('p');
            level.className = 'characterLevelText';
            level.textContent = `레벨: ${member.level}`;

            // 카드에 전부 추가
            card.appendChild(img);
            card.appendChild(name);
            card.appendChild(level);
            card.setAttribute('data-user-id', member.id);

            // membersContainer에 추가
            membersContainer.appendChild(card);
        });

    } catch (error) {
        console.error('멤버 불러오기 실패:', error);
    }
});

// 캐릭터 클릭 시 해당 캐릭터로 이동 > pin 입력 검사 추가
document.getElementById('membersContainer').addEventListener('click', function (e) {
    const card = e.target.closest('.member-card');
    if (!card) return;
    const userId = card.dataset.userId;
    if (!userId) return;
    // pin 입력 modal
    const loginModal = document.getElementById('loginModal');
    const loginModalContent = document.querySelector('#loginModal .pin-modal-content');
    const loginErrorText = document.getElementById('loginErrorText');
    const LoginEnterBtn = document.getElementById('LoginEnterBtn');
    const pinInputs = document.querySelectorAll('.pin-input');

    loginModal.classList.remove('hidden');

    pinInputs.forEach(input => input.value = '');
    pinInputs[0].focus(); // 첫번째 칸에 자동 포커스
    loginErrorText.classList.add('hidden');

    loginModal.addEventListener('click', function (e) {
        if (!loginModalContent.contains(e.target)) {
            loginModal.classList.add('hidden');
        }
    });

    pinInputs.forEach((input, index) => {
        input.addEventListener('input', () => {
            if (input.value.length === 1 && index < pinInputs.length - 1) {
                pinInputs[index + 1].focus(); // 다음 칸 포커스 이동
            }

            // 🔥 4자리 모두 입력됐는지 검사
            const allFilled = Array.from(pinInputs).every(pinInput => pinInput.value.length === 1);
            if (allFilled) {
                setTimeout(() => {
                    LoginEnterBtn.click(); // 자동으로 입장하기 버튼 클릭
                }, 150); // 아주 살짝 딜레이(자연스럽게)
            }
        });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Backspace' && input.value === '' && index > 0) {
                pinInputs[index - 1].focus(); // 이전 칸 포커스 이동
            }
        });
    });


    // 관리자 PIN 입력
    LoginEnterBtn.addEventListener('click', async function () {
        const pin = Array.from(pinInputs).map(input => input.value).join('');
        const pinGroup = document.querySelector('.pin-input-group');

        try {
            const response = await apiRequestJson('/api/main/login', 'POST', {
                userId,
                pin
            });
            if (response.code === 'OK') {
                localStorage.setItem('userId', userId);
                loginModal.classList.remove('hidden');
                window.location.href = 'char.html';

            } else {
                loginErrorText.textContent = "잘못된 PIN입니다.";
                loginErrorText.classList.remove('hidden');
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
        } catch (error) {
            console.error('PIN 확인 실패:', error);
            showMessageModal('서버 오류 발생');
        }
    });
});

