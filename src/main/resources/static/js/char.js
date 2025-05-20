
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
    const template = document.getElementById('dexCardTemplate');
    const dexOverlay = document.getElementById('cardOverlay');
    const dexPagination = document.getElementById('dexPagination');

    // 인벤토리
    const inventoryElements = {
        fish: document.getElementById('itemFish'),
        wood: document.getElementById('itemWood'),
        stone: document.getElementById('itemStone'),
        food: document.getElementById('itemFood'),
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
    mainCharacter.addEventListener('click', async () => {        // 사운드 재생
        playEffect("se_click2");
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

    // 3. 도감 카드 렌더링
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
        dexList.innerHTML = ''; // 기존 카드 제거
        const CharImagePath = './images/character/'
        data.forEach(item => {
            const clone = template.content.cloneNode(true);
            const card = clone.querySelector('.dex-card');
            const frontImg = clone.querySelector('.dex-img');
            const name = clone.querySelector('.dex-name');
            const rank = clone.querySelector('.dex-rank');
            const condition = clone.querySelector('.dex-condition');
            const btn = clone.querySelector('.apply-btn');

            if (!item.owned) card.classList.add('unowned');

            frontImg.src = CharImagePath + `${item.image}`;
            frontImg.alt = item.name;
            if (!item.owned) frontImg.classList.add('unowned');

            name.textContent = item.name;
            rank.textContent = `등급: ${item.rank}`;
            condition.textContent = item.description;
            btn.textContent = item.owned ? '적용하기' : '미획득';
            btn.dataset.dexid = item.id;
            btn.disabled = !item.owned;
            btn.classList.toggle('disabled', !item.owned);

            // 카드 클릭 → 확대
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

            dexList.appendChild(clone);

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
        playEffect("se_click2");
        // 1. apply-btn 누른 경우 → 적용
        if (e.target.classList.contains('apply-btn') && !e.target.classList.contains('disabled')) {
            const dexId = e.target.dataset.dexid;
            applyDexImage(dexId);
            return;
        }

        // 2. 클릭한 위치가 overlay 자체인 경우만 닫기 (배경 눌렀을 때만)
        if (e.target === dexOverlay) {
            dexOverlay.style.display = 'none';
            const preview = document.querySelector('.enlarged-preview');
            if (preview) preview.remove();
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
        if (e.target.classList.contains('dex-page-btn')) return;
        const inside = e.target.closest('.dex-modal-content');
        if (!inside) dexModal.classList.add('hidden');
    });


    // 액션 모달
    const catchCounts = {
        fish: 0,
        wood: 0,
        stone: 0,
        cook: 0
    };
    let currentAction = null;
    let currentIconSrc = "";

    // 모달 열기
    function openActionModal(slotEl) {
        playEffect("se_click");
        const { type, icon, img, alt } = slotEl.dataset;

        currentAction = type;
        currentIconSrc = `images/items/${icon}`;

        const modal = document.getElementById("actionModal");
        const modalContent = document.getElementById("actionModalContent");
        const imgEl = document.getElementById("actionImage");

        imgEl.src = `images/content/${img}`;
        imgEl.alt = alt;

        modalContent.className = `action-modal ${type}`;
        modal.classList.remove("hidden");

        catchCounts[type] = 0;
    }
    // 모달 닫기
    async function closeActionModal() {
        playEffect("se_coin");
        document.getElementById("actionModal").classList.add("hidden");

        if (!currentAction) return;
        const count = catchCounts[currentAction];
        if (count > 0) {
            try {
                const response = await apiRequestJson('/api/char/add-item', 'POST', {
                    count,
                    action: currentAction
                });

                if (response?.data) {
                    renderInventory(response.data.inventory);
                    setUserInfo(response.data);
                }
            } catch (err) {
                console.error("아이템 추가 실패:", err);
                showMessageModal("아이템 추가 중 오류가 발생했습니다.");
            }
        }
        // 초기화
        catchCounts[currentAction] = 0;
        currentAction = null;
    }

    // 완료 버튼
    document.getElementById("actionFinishBtn").addEventListener("click", closeActionModal);

    // 액션 이미지 클릭 → +1 연출
    document.getElementById("actionImage").addEventListener("click", () => {
        if (!currentAction) return;
        catchCounts[currentAction]++;
        createActionTextWithImage(currentIconSrc, "actionModal");
    });
    // 슬롯 클릭 바인딩
    ["fish", "wood", "stone", "cook"].forEach(type => {
        const slot = document.getElementById(`slot-${type}`);
        if (slot) {
            slot.addEventListener("click", () => openActionModal(slot));
        }
    });

    function createActionTextWithImage(imgSrc, modalId) {
        const actionWrapper = document.createElement('div');
        actionWrapper.className = 'get-item-image-text';

        const img = document.createElement('img');
        img.src = imgSrc;
        img.alt = '+1 item';
        img.className = 'get-item-image';

        const plusOne = document.createElement('span');
        plusOne.textContent = '+1';
        plusOne.className = 'get-item-plusone';

        actionWrapper.appendChild(img);
        actionWrapper.appendChild(plusOne);

        document.querySelector(`#${modalId}Content`).appendChild(actionWrapper);


        setTimeout(() => {
            actionWrapper.remove();
        }, 1000);
    }

    function renderInventory(inventory) {
        if (!inventory) return;

        const inventoryElements = {
            fish: document.getElementById('itemFish'),
            wood: document.getElementById('itemWood'),
            stone: document.getElementById('itemStone'),
            food: document.getElementById('itemFood')
        };

        Object.entries(inventoryElements).forEach(([key, el]) => {
            if (el && key in inventory) {
                el.textContent = inventory[key];
            }
        });
    }


});
