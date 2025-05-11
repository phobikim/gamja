import { apiRequest, apiRequestJson } from './util.js';
import { showMessageModal } from './modal.js';

(async () => {
    // 감자단 도감 클릭 이벤트
    const characterImg = document.getElementById('characterImg');
    const dexModal = document.getElementById('dexModal');
    const dexList = document.getElementById('dexList');
    const overlay = document.getElementById('cardOverlay');

    // 콘텐츠 내부 클릭 시 모달 닫힘 방지
    document.querySelector('.dex-modal-content').addEventListener('click', (e) => {
        e.stopPropagation();
    });

    characterImg.addEventListener('click', async () => {
        try {
            overlay.style.display = 'none';
            document.querySelectorAll('.dex-card').forEach(card => {
                card.classList.remove('enlarged', 'flipped');
            });
            const response = await apiRequest('/api/dex/list', 'GET');
            const dexData = response.data;
            // renderDexCards(dexData);
            renderDexCardsWithPaging(dexData, 1);
            dexModal.classList.remove('hidden');
        } catch (err) {
            showMessageModal('도감 정보를 불러올 수 없습니다.');
            console.error(err);
        }
    });

    function renderDexCards(data) {
        dexList.innerHTML = '';
        data.forEach(item => {
            const card = document.createElement('div');
            card.className = 'dex-card';
            if (!item.owned) {
                card.classList.add('unowned'); // 🔥 보유하지 않은 감자
            }
            card.innerHTML = `
            <div class="card-inner">
                <div class="card-front">
                    <img src="./images/character/${item.image}" alt="${item.name}" class="dex-img ${item.owned ? '' : 'unowned'}"> 
                </div>
                <div class="card-back">
                    <p class="dex-name">${item.name}</p>
                    <div class="dex-body">
                        <p class="dex-rank">등급: ${item.rank}</p>
                        <p class="dex-condition">${item.description}</p>
<!--                        <p class="dex-condition">${item.acquireCondition}</p>-->
                    </div>
                    <button class="apply-btn ${item.owned ? '' : 'disabled'}" 
                        data-dexid="${item.id}" ${item.owned ? '' : 'disabled'}>
                        ${item.owned ? '적용하기' : '미획득'}
                    </button>
                </div>
            </div>
        `;
            const img = card.querySelector('.dex-img');
            img.style.opacity = 0;
            img.onload = () => {
                img.style.opacity = 1;
            };
            card.addEventListener('click', () => {
                overlay.innerHTML = ''; // 기존 프리뷰 초기화
                overlay.style.display = 'flex';

                const preview = card.cloneNode(true); // 카드 복제
                preview.classList.add('enlarged-preview'); // 새로운 스타일 클래스

                // flip 처리도 가능하게
                preview.addEventListener('click', () => {
                    preview.classList.toggle('flipped');
                });

                overlay.appendChild(preview);
            });
            dexList.appendChild(card);
        });
    }

    overlay.addEventListener('click', async (e) => {
        if (!e.target.closest('.enlarged-preview')) {
            overlay.style.display = 'none';
            const existingPreview = document.querySelector('.enlarged-preview');
            if (existingPreview) {
                existingPreview.remove();
            }
        }
        if (e.target.classList.contains('apply-btn') && !e.target.classList.contains('disabled')) {
            const dexId = e.target.dataset.dexid;
            try {
                const response = await apiRequestJson('/api/char/setImage', 'POST', { dexId });
                if (response.code === 'SUCCESS') {
                    showMessageModal('대표 감자가 변경되었습니다');
                    const newImage = response.data.characterImage;
                    if (newImage) {
                        characterImg.src = `./images/character/${newImage}`;
                    }
                    overlay.style.display = 'none';
                    dexModal.classList.add('hidden');
                } else {
                    showMessageModal(response.message);
                }
            } catch (err) {
                showMessageModal('오류가 났어요.');
                console.error(err);
            }
        }
    });

    // 적용 버튼 클릭 이벤트 등록
    dexList.addEventListener('click', async (e) => {
        if (e.target.classList.contains('apply-btn') && !e.target.classList.contains('disabled')) {
            const dexId = e.target.dataset.dexid;

            try {
                const response = await apiRequestJson('/api/char/setImage', 'POST', { dexId });
                if (response.code === 'SUCCESS') {
                    showMessageModal('대표 감자가 변경되었습니다');
                    const newImage = response.data.characterImage;
                    if (newImage) {
                        characterImg.src = `./images/character/${newImage}`;
                    }
                    overlay.style.display = 'none';
                    dexModal.classList.add('hidden');
                } else {
                    showMessageModal(response.message);
                }
            } catch (err) {
                showMessageModal('오류가 났어요.')
                console.error(err);
            }
        }
    });

    dexModal.addEventListener('click', (e) => {
        const isInsideModal = e.target.closest('.dex-modal-content');
        if (!isInsideModal) {
            dexModal.classList.add('hidden');
        }
    });


    const dexPagination = document.getElementById('dexPagination');
    let fullDexList = [];
    const itemsPerPage = 15;

    function renderDexCardsWithPaging(data, page = 1) {
        fullDexList = data;
        const start = (page - 1) * itemsPerPage;
        const pagedData = data.slice(start, start + itemsPerPage);
        renderDexCards(pagedData);
        renderPagination(data.length, page);
    }

    function renderPagination(totalItems, currentPage) {
        const totalPages = Math.ceil(totalItems / itemsPerPage);
        dexPagination.innerHTML = '';

        for (let i = 1; i <= totalPages; i++) {
            const btn = document.createElement('button');
            btn.className = 'dex-page-btn';
            if (i === currentPage) btn.classList.add('active');
            btn.textContent = i;
            btn.addEventListener('click', () => renderDexCardsWithPaging(fullDexList, i));
            dexPagination.appendChild(btn);
        }
    }


    let myMoney = 0;
    let skillItems = []; // 상점 아이템 목록 저장 (전역)

    const moneySlot = document.getElementById('slot-money');
    const shopModal = document.getElementById('shopModal');
    const closeShopBtn = document.getElementById('closeShopBtn');
    const shopItemList = document.getElementById('shopItemList');

    moneySlot.addEventListener('click', openShopModal);
    closeShopBtn.addEventListener('click', () => shopModal.classList.add('hidden'));

    async function openShopModal() {
        try {
            showMessageModal('상점 기능 Opening soon 많관부')
            // const response = await apiRequest('/api/shop/skills', 'GET');
            // skillItems = response.data;
            // renderShopItems();
            // updateShopGold();
            // shopModal.classList.remove('hidden');
        } catch (error) {
            showMessageModal('상점 모달 로드 실패')
        }
    }

    function renderShopItems() {
        shopItemList.innerHTML = '';

        skillItems.forEach(skill => {
            const button = document.createElement('button');
            button.className = 'shop-skill-btn';
            button.textContent = `${skill.skillName} ${skill.price}골드`;

            if (myMoney < skill.price) {
                button.disabled = true;
                button.classList.add('disabled');
            }

            button.addEventListener('click', async () => {
                await buySkill(skill.skillType, skill.price);
            });

            const li = document.createElement('li');
            li.appendChild(button);
            shopItemList.appendChild(li);
        });
    }

    async function buySkill(skillType, price) {
        if (myMoney < price) {
            showMessageModal('골드가 부족합니다!');
            return;
        }

        try {
            const requestBody = {
                userId: userId,
                skillType: skillType
            };

            const response = await apiRequestJson('/api/skill/buy', 'POST', requestBody);

            showMessageModal('구매 완료!');
            myMoney = response.data.remainingMoney;
            renderShopItems();
            updateShopGold();
        } catch (error) {
            console.error('구매 실패:', error);
            showMessageModal('구매에 실패했습니다.');
        }
    }

    function updateShopGold() {
        const shopGold = document.getElementById('shopGold');
        if (shopGold) {
            shopGold.textContent = `보유 재화: ${myMoney} 골드`;
        }
    }

    const characterName = document.getElementById('characterName');

    /*
    * Action 코드
    * */
    const catchCounts = {
        fish: 0,
        wood: 0,
        stone: 0,
        cook: 0
    };

    const actions = {
        fish: {
            inventorySlotId: 'slot-fish',
            modalId: 'fishingModal',
            imgId: 'fishImage',
            finishBtnId: 'finishFishingBtn',
            icon: 'images/items/i_fish.png'
        },
        wood: {
            inventorySlotId: 'slot-wood',
            modalId: 'woodModal',
            imgId: 'woodImage',
            finishBtnId: 'finishWoodBtn',
            icon: 'images/items/i_wood.png'
        },
        stone: {
            inventorySlotId: 'slot-stone',
            modalId: 'stoneModal',
            imgId: 'stoneImage',
            finishBtnId: 'finishStoneBtn',
            icon: 'images/items/i_stone.png'
        },
        cook: {
            inventorySlotId: 'slot-food',
            modalId: 'cookModal',
            imgId: 'cookImage',
            finishBtnId: 'finishCookBtn',
            icon: 'images/items/i_food.png'
        }
    };

    Object.entries(actions).forEach(([action, config]) => {
        const {
            inventorySlotId, modalId, imgId, finishBtnId, icon
        } = config;

        const slot = document.getElementById(inventorySlotId);
        const modal = document.getElementById(modalId);
        const actionImg = document.getElementById(imgId);
        const finishBtn = document.getElementById(finishBtnId);

        if (slot) {
            slot.addEventListener('click', () => {
                catchCounts[action] = 0;
                modal.classList.remove('hidden');
            });
        }

        if (actionImg) {
            actionImg.addEventListener('click', () => {
                catchCounts[action]++;
                createActionTextWithImage(icon, modalId);
            });
        }

        if (finishBtn) {
            finishBtn.addEventListener('click', async () => {
                modal.classList.add('hidden');
                const count = catchCounts[action];
                if (count > 0) {
                    const response = await apiRequestJson(`/api/char/add-item`, 'POST', {
                        count,
                        action
                    });
                    if (response?.data) {
                        renderInventory(response.data.inventory);
                        updateCharacterInfo(response.data);
                        highlightInventory(action);
                    }
                    catchCounts[action] = 0;
                }
            });
        }
    });

    const highlightInventory = (type) => {
        const slot = document.getElementById(`inventory${capitalize(type)}`);
        if (slot) {
            slot.parentElement.classList.add('highlight');
            setTimeout(() => {
                slot.parentElement.classList.remove('highlight');
            }, 500);
        }
    };

    function capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

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

        document.querySelector(`#${modalId} .modal-content`).appendChild(actionWrapper);

        setTimeout(() => {
            actionWrapper.remove();
        }, 1000);
    }


    const homeLogo = document.getElementById('homeLogo');
    homeLogo.addEventListener('click', () => {
        location.href = './main.html';
    });

    const userId = localStorage.getItem('userId');
    if (!userId) {
        showMessageModal('잘못된 접근');
        location.href = './main.html';
        return;
    }

    try {
        const response = await apiRequest(`/api/char/${userId}`, 'GET');
        const { data } = response;
        updateCharacterInfo(data);
        renderInventory(data.inventory);
        myMoney = data.inventory.money;
        updateShopGold();
    } catch (error) {
        console.error('캐릭터 정보 불러오기 실패:', error);
    }

    function updateCharacterInfo(charInfo) {
        const imgPath = './images/character/';
        const characterImg = document.getElementById('characterImg');
        characterImg.src = charInfo.characterImage ? imgPath + charInfo.characterImage : imgPath + 'default.png';
        characterImg.alt = charInfo.usernickname || charInfo.username;

        characterName.innerHTML = `
      ${charInfo.usernickname || charInfo.username}
      <span class="lv-text">(LV <span class="level-number">${charInfo.level}</span>)</span>
      <span class="title-text">${charInfo.title || '무명 감자'}</span>
    `;

        const xpFill = document.getElementById('xpFill');
        const currentXp = charInfo.xp;
        xpFill.style.width = `${currentXp}%`;
    }

    function renderInventory(invenInfo) {
        const inventoryMap = {
            fish: 'inventoryFish',
            wood: 'inventoryWood',
            stone: 'inventoryStone',
            food: 'inventoryFood',
            money: 'inventoryMoney'
        };

        for (const [key, id] of Object.entries(inventoryMap)) {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = invenInfo[key] ?? 0;
            }
        }
    }

})(); // end IIFE
