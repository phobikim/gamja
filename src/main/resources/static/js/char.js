
document.addEventListener('DOMContentLoaded', async () => {
    // BGM
    const toggleBtn = document.getElementById("bgmToggleBtn");
    // BGM Toggle
    toggleBtn.addEventListener("click", () => {
        toggleBGM("bgm_char");
    });
    const mainBtn = document.getElementById("mainBtn");
    mainBtn.addEventListener("click", () => {
        location.href = './index.html';
    });

    const mainCharacter = document.getElementById('mainCharacter');
    const hpBarFill = document.getElementById('hpBarFill');
    const hpBarText = document.getElementById('hpBarText');

    // 1. 캐릭터 로딩
    const userId = localStorage.getItem('userId');

    if (!userId) {
        showMessageModal('잘못된 접근입니다.');
        location.href = './index.html';
        return;
    }

    try {
        const res  = await apiRequest(`/api/char/${userId}`, 'GET');

        // 캐릭터 정보는 data 아래에 넘겨준다.
        if (res.code !== 'SUCCESS' || !res.data) {
            showMessageModal('캐릭터 정보를 불러오지 못했습니다.');
            location.href = '/login.html'; // 또는 사용자 정의 메시지
        }
        //캐릭터 기본 정보 설정
        setUserInfo(res.data);

    } catch (err) {
        showMessageModal('캐릭터 정보를 불러오지 못했습니다.');
        location.href = '/login.html'; // 또는 사용자 정의 메시지
        console.error(err);
    }

    function setUserInfo(data) {
        const {
            level,
            nickname,
            title,
            username,
            xp,
            characterImage
        } = data;

        const charNameEl = document.getElementById('charName');
        const charLevelEl = document.getElementById('charLevel');
        const userTitleEl = document.getElementById('userTitle');

        // 이미지 교체: null이 아닌 경우만
        if (characterImage) {
            const imagePath = '/character/';
            mainCharacter.src = basePath_image + imagePath + characterImage;
            mainCharacter.alt = nickname || username || '캐릭터';
        }

        // 닉네임
        if (nickname || username) {
            charNameEl.textContent = nickname || username;
        }

        // 레벨
        if (level !== null && level !== undefined) {
            charLevelEl.textContent = level;
        }

        // 칭호
        if (title !== null && title !== undefined) {
            userTitleEl.textContent = title;
        }

        const maxExp = 100 + (level - 1) * 20;
        const expPercent = ((xp / maxExp) * 100).toFixed(1); // 퍼센트로 계산
        // 경험치
        if (xp !== null && xp !== undefined) {
            hpBarText.textContent = `${xp} / ${maxExp}`;
            hpBarFill.style.width = `${expPercent}%`;
        }
    }

    window.setUserInfo = setUserInfo;

    // 2. 캐릭터 클릭 → 장비창
    mainCharacter.addEventListener('click', async () => {
        playEffect("se_click2");
        // 장비창
    });

});
