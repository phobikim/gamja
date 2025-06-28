(async function () {
    const mainCharacter = document.getElementById('mainCharacter');

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

        loadCharacterBasicInfo();

    } catch (err) {
        // ✅ 서버 점검 응답 처리
        if (err.status === 503) {
            location.href = './maintenance.html';
            return;
        }
        if (err.status === 401) {
            showMessageModal('세션이 만료되었습니다. 다시 로그인해주세요.');
            setTimeout(() => location.href = './index.html', 1500);
            return;
        }
        showMessageModal(err.message || '로그인이 필요합니다.');
        setTimeout(() => location.href = './index.html', 1500);
    }
    function loadCharacterBasicInfo() {
        const url = '/api/char';

        apiRequest(url, 'GET')
            .then(res => {
                if (res.code === 'SUCCESS') {
                    const charRes = res.data;
                    setCharacterBasicInfo(charRes);
                    setUserInfo(charRes);
                } else {
                    console.error('캐릭터 정보 불러오기 실패:', res.message);
                }
            })
            .catch(err => {
                console.error('API 요청 에러:', err);
            });
    }
    function setUserInfo(data) {
        const {
            username,
            level,
            title,
            titleIconPath,
            xp,
            characterImage,
            dexName,
            backgroundImageUrl,
            backgroundImageName
        } = data;

        //배경 이미지 적용
        if (backgroundImageUrl || backgroundImageName) {
            const fullUrl = `${basePath}${backgroundImageUrl}`;
            const img = new Image();
            img.onload = () => {
                const gameContentEl = document.querySelector('.game-content');
                if (gameContentEl) {
                    gameContentEl.style.backgroundImage = `url('${fullUrl}')`;
                }

                // 색상 적용은 바로 해도 무방
                const charPage = document.querySelector('.char-page');
                const mainMenu = document.querySelector('.main-menu-grid');

                const topFooterColorMap = {
                    '숲':   { top: '#131e09', footer: '#131e09' },
                    '여름': { top: '#0f7c9f', footer: '#e1a92e' },
                    '가을': { top: '#411f16', footer: '#371d0d' },
                    '겨울': { top: '#2f5f82', footer: '#6e9bb6' },
                };

                const backgroundName = data.backgroundImageName || '';
                const colorSet = topFooterColorMap[backgroundName] || {
                    top: '#fff8dc',
                    footer: '#fff8dc'
                };

                if (charPage) charPage.style.backgroundColor = colorSet.top;
                if (mainMenu) mainMenu.style.backgroundColor = colorSet.footer;
            };

            img.src = fullUrl; // ✅ 여기서 비동기 로딩 시작
        }

        const imagePath = '/character/';
        /* 상단 카드에 들어가는 이미지 */
        const avatarImg = document.getElementById('playerAvatarImg');
        if (avatarImg) {
            avatarImg.src = basePath_image + imagePath + characterImage;
            avatarImg.alt = username || '캐릭터';
        }

        // 메인 캐릭터 이미지 설정
        const mainCharacterEl = document.getElementById('mainCharacter');
        if (characterImage) {
            mainCharacterEl.src = basePath_image + imagePath + characterImage;
            mainCharacterEl.alt = username || '캐릭터';
        }
        mainCharacterEl.addEventListener('load', adjustCharacterPosition);
        window.addEventListener('resize', adjustCharacterPosition);
        // 이름
        const playerNameEl = document.getElementById('playerName');
        playerNameEl.textContent = username;

        // 칭호
        const titleTextEl = document.getElementById('playerTitleText');
        const titleIconEl = document.getElementById('titleIcon');
        titleTextEl.textContent = title || '';
        if (titleIconPath) {
            titleIconEl.src = basePath + '/' + titleIconPath;
            titleIconEl.style.display = 'inline';
        } else {
            titleIconEl.style.display = 'none';
        }

        // 덱스 이름 및 레벨
        const dexNameEl = document.getElementById('dexName');
        const inlineLevelEl = document.getElementById('inlineLevel');
        if (dexNameEl && inlineLevelEl) {
            dexNameEl.childNodes[0].nodeValue = dexName + ' ';
            inlineLevelEl.textContent = `(Lv.${level})`;
        }

        // 경험치 퍼센트 계산 및 바 적용
        const maxExp = 100 + (level - 1) * 20;
        const xpPercent = ((xp / maxExp) * 100).toFixed(1);
        const xpFillEl = document.getElementById('playerAvatarXpFill');
        xpFillEl.style.setProperty('--xp-percent', `${xpPercent}%`);

        // 감자 원정대 정보
        const { corpsTierName, corpsTierIcon, corpsTierExp } = data;

        const tierMaxExp = 1000; // 예시, 티어별 최대 경험치 (서버에서 계산되면 대체 가능)

        document.getElementById('tierName').textContent = corpsTierName;
        document.getElementById('tierIcon').src = basePath + corpsTierIcon;
        // document.getElementById('tierExpText').textContent = `EXP ${corpsTierExp} / ${tierMaxExp}`;
        //
        // const percent = Math.min(100, Math.floor((corpsTierExp / tierMaxExp) * 100));
        // document.getElementById('tierExpBar').style.width = `${percent}%`;
    }


    // 필요 시 외부에서 호출 가능하도록 전역 등록
    window.loadCharacterBasicInfo = loadCharacterBasicInfo;

    function adjustCharacterPosition() {
        const mainCharacter = document.getElementById('mainCharacter');
        const gameContent = document.querySelector('.game-content');

        if (!mainCharacter || !gameContent) return;

        // 이미지가 로드된 후에 정확한 높이 측정
        const characterHeight = mainCharacter.offsetHeight;
        // 캐릭터 키의 24% 만큼 위로 올리기
        const offsetY = characterHeight * 0.15;
        mainCharacter.style.transform = `translateY(-${offsetY}px)`;
    }

})();
