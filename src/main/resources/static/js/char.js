(async function () {
    const mainCharacter = document.getElementById('mainCharacter');
    const hpBarFill = document.getElementById('hpBarFill');
    const hpBarText = document.getElementById('hpBarText');

    try {
        // ✅ 1. 세션 확인 (/api/me)
        const meRes = await apiRequest('/api/me', 'GET');
        if (meRes.code !== 'SUCCESS') {
            throw new Error('세션이 만료되었습니다.');
        }

        // ✅ 2. 캐릭터 정보 호출
        const charRes = await apiRequest('/api/char', 'GET');
        if (charRes.code !== 'SUCCESS' || !charRes.data) {
            throw new Error('캐릭터 정보를 불러오지 못했습니다.');
        }

        setUserInfo(charRes.data);

    } catch (err) {
        showMessageModal(err.message || '로그인이 필요합니다.');
        location.href = './index.html';
        console.error(err);
    }

    function setUserInfo(data) {
        const {
            username,
            level,
            nickname,
            title,
            titleIconPath,
            xp,
            characterImage,
            dexName
        } = data;

        const charNameEl = document.getElementById('charName');
        const charLevelEl = document.getElementById('charLevel');
        const userTitleEl = document.getElementById('userTitleText');
        const userTitleIconEl = document.getElementById('userTitleIcon');
        const dexNameEl = document.getElementById('dexName');

        if (characterImage) {
            const imagePath = '/character/';
            mainCharacter.src = basePath_image + imagePath + characterImage;
            mainCharacter.alt = nickname || username || '캐릭터';
        }

        charNameEl.textContent = nickname || username;
        dexNameEl.textContent = dexName;
        // 칭호 텍스트 + 아이콘
        userTitleEl.textContent = title || '';
        if (titleIconPath) {
            userTitleIconEl.src = basePath + '/' + titleIconPath;
            userTitleIconEl.classList.remove('hidden');
        } else {
            userTitleIconEl.classList.add('hidden');
        }

        if (level != null) {
            charLevelEl.textContent = level;
        }

        const maxExp = 100 + (level - 1) * 20;
        const expPercent = ((xp / maxExp) * 100).toFixed(1);
        hpBarText.textContent = `${xp} / ${maxExp}`;
        hpBarFill.style.width = `${expPercent}%`;
    }

    // 캐릭터 클릭 → 장비창
    mainCharacter.addEventListener('click', () => {
        playEffect("se_click2");
        // 장비창 열기 로직 작성 예정
    });

    // 필요 시 외부에서 호출 가능하도록 전역 등록
    window.setUserInfo = setUserInfo;

})();
