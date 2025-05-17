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

    const dexModal = document.getElementById('dexModal');
    const dexList = document.getElementById('dexList');
    const dexOverlay = document.getElementById('cardOverlay');
    const dexPagination = document.getElementById('dexPagination');

    const inventoryElements = {
        fish: document.getElementById('itemFish'),
        wood: document.getElementById('itemWood'),
        stone: document.getElementById('itemStone'),
        food: document.getElementById('itemFood'),
        money: document.getElementById('itemMoney')
    };

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
            characterImage = 'default.png',
            inventory = {}
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

        Object.entries(inventoryElements).forEach(([key, el]) => {
            el.textContent = inventory[key] ?? 0;
        });

    }

    // 2. 캐릭터 클릭 → 도감 모달 실행
    mainCharacter.addEventListener('click', async () => {
        try {
            const res = await fetch('/api/dex/list');
            const { data } = await res.json();

            renderDexCardsWithPaging(data, 1);
            dexModal.classList.remove('hidden');
            dexOverlay.style.display = 'none';
        } catch (err) {
            showMessageModal('도감 정보를 불러올 수 없습니다.');
            console.error(err);
        }
    });

    // 도감 카드 렌더링
    let fullDexList = [];
    const itemsPerPage = 15;

    function renderDexCardsWithPaging(data, page = 1) {
        fullDexList = data;
        const start = (page - 1) * itemsPerPage;
        const paged = data.slice(start, start + itemsPerPage);
        renderDexCards(paged);
        renderPagination(data.length, page);
    }

    function renderDexCards(data) {
        dexList.innerHTML = '';
        data.forEach(item => {
            const card = document.createElement('div');
            card.className = 'dex-card';
            if (!item.owned) card.classList.add('unowned');

            card.innerHTML = `
        <div class="card-inner">
          <div class="card-front">
            <img src="./images/character/${item.image}" alt="${item.name}" class="dex-img ${item.owned ? '' : 'unowned'}" draggable="false"> 
          </div>
          <div class="card-back">
            <p class="dex-name">${item.name}</p>
            <div class="dex-body">
              <p class="dex-rank">등급: ${item.rank}</p>
              <p class="dex-condition">${item.description}</p>
            </div>
            <button class="apply-btn ${item.owned ? '' : 'disabled'}" data-dexid="${item.id}" ${item.owned ? '' : 'disabled'}>
              ${item.owned ? '적용하기' : '미획득'}
            </button>
          </div>
        </div>
      `;

            // 카드 클릭 → 확대 미리보기
            card.addEventListener('click', () => {
                dexOverlay.innerHTML = '';
                dexOverlay.style.display = 'flex';

                const preview = card.cloneNode(true);
                preview.classList.add('enlarged-preview');

                preview.addEventListener('click', () => {
                    preview.classList.toggle('flipped');
                });

                dexOverlay.appendChild(preview);
            });

            dexList.appendChild(card);
        });
    }

    function renderPagination(total, current) {
        dexPagination.innerHTML = '';
        const totalPages = Math.ceil(total / itemsPerPage);
        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement('button');
            btn.className = 'dex-page-btn';
            if (i === current) btn.classList.add('active');
            btn.textContent = i;
            btn.addEventListener('click', () => renderDexCardsWithPaging(fullDexList, i));
            dexPagination.appendChild(btn);
        }
    }

    // 도감 미리보기 → 닫기
    dexOverlay.addEventListener('click', (e) => {
        if (!e.target.closest('.enlarged-preview')) {
            dexOverlay.style.display = 'none';
            const preview = document.querySelector('.enlarged-preview');
            if (preview) preview.remove();
        }

        if (e.target.classList.contains('apply-btn') && !e.target.classList.contains('disabled')) {
            const dexId = e.target.dataset.dexid;
            applyDexImage(dexId);
        }
    });

    // 적용하기 버튼 처리
    async function applyDexImage(dexId) {
        try {
            const res = await fetch('/api/char/setImage', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ dexId })
            });
            const result = await res.json();

            if (result.code === 'SUCCESS') {
                showMessageModal('대표 감자가 변경되었습니다!');

                if (result.data?.characterImage) {
                    mainCharacter.src = `./images/character/${result.data.characterImage}`;
                }
                dexOverlay.style.display = 'none';
                dexModal.classList.add('hidden');
            } else {
                showMessageModal(result.message || '적용 실패');
            }
        } catch (err) {
            showMessageModal('적용 중 오류 발생');
            console.error(err);
        }
    }

    // 모달 바깥 클릭 → 닫기
    dexModal.addEventListener('click', (e) => {
        const inside = e.target.closest('.dex-modal-content');
        if (!inside) dexModal.classList.add('hidden');
    });
});
