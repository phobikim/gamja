
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
            return;
        }
        //캐릭터 기본 정보 설정
        setUserInfo(res.data);

    } catch (err) {
        showMessageModal('캐릭터 정보를 불러오지 못했습니다.');
        console.error(err);
    }

    function setUserInfo(data) {
        const {
            level,
            nickname,
            title,
            username,
            xp = 0,
            characterImage = 'default.png'
        } = data;

        // 대표 캐릭터 이미지 세팅
        const imagePath = './images/character/';
        mainCharacter.src = imagePath + characterImage;
        mainCharacter.alt = nickname || username || '캐릭터';

        // 캐릭터 이름, 레벨 세팅
        document.getElementById('charName').textContent = nickname || username || '---';
        document.getElementById('charLevel').textContent = level ?? '-';
        document.getElementById('userTitle').textContent = title || '칭호 없음';

        hpBarFill.style.width = `${xp}%`;


    }
    window.setUserInfo = setUserInfo;

    // 2. 캐릭터 클릭 → 장비창
    mainCharacter.addEventListener('click', async () => {
        playEffect("se_click2");
        // 장비창
    });



});
