let currentUserGold = 0;
let currentBuyCategory = 'ADVENTURE';
let cachedBuyItems = [];

function openShopModal() {
    document.getElementById('shopModal').classList.remove('hidden');
    document.querySelectorAll('.shop-tab').forEach(t => t.classList.remove('active'));
    const buyTab = document.querySelector('.shop-tab[data-tab="buy"]');
    if (buyTab) {
        buyTab.classList.add('active');
        // 소탭 초기화
        currentBuyCategory = 'ADVENTURE';
        setBuySubtabsVisibility(true);
        setActiveBuySubtab(currentBuyCategory);
        loadShopItems('buy');
    }
}

document.querySelectorAll('.shop-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.getElementById('shopSliderArea').classList.add('hidden');
        document.querySelectorAll('.shop-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const tabType = tab.dataset.tab;

        // 구매 탭일 때만 소탭 노출
        if (tabType === 'buy') {
            setBuySubtabsVisibility(true);
            setActiveBuySubtab(currentBuyCategory);
        } else {
            setBuySubtabsVisibility(false);
        }
        loadShopItems(tabType);
    });
});
function setBuySubtabsVisibility(show) {
    const subtabs = document.getElementById('shopBuySubtabs');
    if (!subtabs) return;
    subtabs.classList.toggle('hidden', !show);
}

function setActiveBuySubtab(category) {
    const subtabs = document.querySelectorAll('#shopBuySubtabs .shop-subtab');
    subtabs.forEach(btn => btn.classList.toggle('active', btn.dataset.category === category));
}
document.getElementById('shopBuySubtabs')?.addEventListener('click', (e) => {
    const btn = e.target.closest('.shop-subtab');
    if (!btn) return;
    document.getElementById('shopSliderArea').classList.add('hidden');

    currentBuyCategory = btn.dataset.category;
    setActiveBuySubtab(currentBuyCategory);

    // 캐시된 구매 목록에서 필터만 다시 렌더
    renderShopList(filterBuyItemsByCategory(cachedBuyItems, currentBuyCategory), 'buy');
});

async function loadShopItems(type) {
    const sliderArea = document.getElementById('shopSliderArea');
    sliderArea.classList.add('hidden');

    // 선택된 카드 초기화
    const listEl = document.getElementById('shopItemList');
    listEl.innerHTML = '';
    document.querySelectorAll('.shop-item-card').forEach(c => c.classList.remove('selected'));

    const url = type === 'buy' ? '/api/shop/sell-list' : '/api/shop/inventory';

    try {
        const res = await apiRequest(url, 'GET');
        if (res.code === 'SUCCESS') {
            const { gold, items } = res.data;
            currentUserGold = gold;
            updateGoldDisplay(gold);

            if (type === 'buy') {
                cachedBuyItems = Array.isArray(items) ? items : [];
                const filtered = filterBuyItemsByCategory(cachedBuyItems, currentBuyCategory);
                renderShopList(filtered, 'buy');
            } else {
                renderShopList(items || [], 'sell');
            }
        }
    } catch (err) {
        console.error(`${type} 아이템 불러오기 실패`, err);
    }
}
function filterBuyItemsByCategory(items, category) {
    const want = String(category || '').toUpperCase();
    return (items || []).filter(it => String(it.category || '').toUpperCase() === want);
}

function renderShopItemCard(item, type) {
    const confirmBtn = document.getElementById('shopConfirmButton');
    const warningEl = document.getElementById('shopGoldWarning');

    const card = document.createElement('div');
    card.className = 'shop-item-card';
    card.dataset.targetId = item.targetId;
    card.dataset.category = item.category;

    const isSellDisabled = (type === 'sell' && (!item.quantity || item.quantity === 0));
    const isBuyOutOfStock = (type === 'buy' && ((item.availableQuantity ?? 0) === 0));
    const isOwnedSkin = (type === 'buy' && item.category === 'SKIN' && item.owned === true);

    if (isSellDisabled || isBuyOutOfStock || isOwnedSkin) {
        card.classList.add('disabled');
    }

    const ownedBadge = (isOwnedSkin)
        ? `<span class="shop-owned-label">보유중</span>`
        : '';


    card.innerHTML = `
        ${ownedBadge}
        <img class="shop-item-thumb" src="${basePath}${item.iconPath}" alt="${item.name}" />
        <div class="shop-item-info">
          <div class="shop-item-name">
            ${item.name}
            ${
            type === 'sell' && item.quantity !== undefined
                ? `<span class="shop-item-qty">(x${item.quantity})</span>`
                : type === 'buy'
                    ? `<span class="shop-item-qty">${
                        item.category === 'SKIN'
                            ? `( 남은 ${item.availableQuantity ?? 0}개 / 1개만 구매 가능 )`
                            : `( 남은 ${item.availableQuantity ?? 0}개 / 하루 ${item.stock ?? 0}개 )`
                    }</span>`
                    : ''
        }
          </div>
          <div class="shop-item-desc">${item.description}</div>
        </div>
        <div class="shop-item-price">
          ${(type === 'buy' ? item.price : item.sellPrice).toLocaleString()} G
        </div>
      `;

    if (card.classList.contains('disabled')) {
        return card;
    }

    card.addEventListener('click', () => {
        document.querySelectorAll('.shop-item-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');

        const currentTab = document.querySelector('.shop-tab.active')?.dataset.tab ?? 'buy';
        const slider = document.getElementById('shopQuantitySlider');
        const sliderArea = document.getElementById('shopSliderArea');

        if (type === 'buy') {
            if (item.category === 'SKIN') {
                slider.max = Math.min(1, item.availableQuantity ?? 0);
            } else {
                slider.max = item.availableQuantity ?? 99;
            }
        } else {
            slider.max = item.quantity ?? 99;
        }
        slider.value = Math.min(1, parseInt(slider.max) || 0);

        document.getElementById('shopSelectedItemName').textContent = item.name;
        document.getElementById('shopSelectedQuantity').textContent = `x1`;
        document.getElementById('shopConfirmButton').textContent = currentTab === 'buy' ? '구매' : '판매';

        const bottomRow = document.getElementById('shopBottomRow');
        // 초기 예상 금액 표시
        const unitPrice = type === 'buy' ? item.price : item.sellPrice;
        let total = unitPrice * 1;
        document.getElementById('shopTotalPrice').textContent = `${total.toLocaleString()} G`;
        warningEl.classList.add('hidden');
        bottomRow.classList.remove('hidden');
        confirmBtn.disabled = false;

        validateShopState(currentTab, item, total, confirmBtn, warningEl, bottomRow);

        slider.oninput = () => {
            const count = parseInt(slider.value);
            total = unitPrice * count;
            document.getElementById('shopSelectedQuantity').textContent = `x${count}`;
            document.getElementById('shopTotalPrice').textContent = `${total.toLocaleString()} G`;
            warningEl.classList.add('hidden');
            bottomRow.classList.remove('hidden');
            confirmBtn.disabled = false;

            validateShopState(currentTab, item, total, confirmBtn, warningEl, bottomRow);
        };
        sliderArea.classList.remove('hidden');
    });

    return card;
}

function validateShopState(currentTab, item, total, confirmBtn, warningEl, bottomRow) {
    if (currentTab === 'buy') {
        if (currentUserGold < total) {
            confirmBtn.disabled = true;
            warningEl.textContent = '골드가 부족합니다!';
            warningEl.classList.remove('hidden');
            bottomRow.classList.add('hidden');
            return;
        }
        // 재고 0 체크
        if ((item.availableQuantity ?? 0) <= 0) {
            confirmBtn.disabled = true;
            warningEl.textContent = '재고가 없습니다!';
            warningEl.classList.remove('hidden');
            bottomRow.classList.add('hidden');
            return;
        }
    } else {
        if ((item.quantity ?? 0) <= 0) {
            confirmBtn.disabled = true;
            warningEl.textContent = '보유 수량이 부족합니다!';
            warningEl.classList.remove('hidden');
            bottomRow.classList.add('hidden');
            return;
        }
    }
    confirmBtn.disabled = false;
    warningEl.classList.add('hidden');
}

document.getElementById('shopConfirmButton').addEventListener('click', async () => {
    playEffect("se_craft");
    const itemName = document.getElementById('shopSelectedItemName').textContent;
    const quantity = parseInt(document.getElementById('shopQuantitySlider').value);
    const selectedCard = document.querySelector('.shop-item-card.selected');
    if (!selectedCard || isNaN(quantity)) return;

    const targetId = selectedCard.dataset.targetId;
    const category = (selectedCard.dataset.category || currentBuyCategory || 'ADVENTURE').toUpperCase();
    const currentTab = document.querySelector('.shop-tab.active')?.dataset.tab ?? 'buy';
    const endpoint = currentTab === 'buy' ? '/api/shop/buy' : '/api/shop/sell';

    try {
        const payload = currentTab === 'buy'
            ? { targetId, quantity, category }
            : { targetId, quantity };
        const res = await apiRequestJson(endpoint, 'POST', payload);

        if (res.code === 'SUCCESS') {
            showMessageModal(`${itemName} ${currentTab === 'buy' ? '구매' : '판매'} 완료!`);
            // 데이터 새로고침
            loadShopItems(currentTab);
            loadCharacterBasicInfo();
        } else {
            showMessageModal(`처리 실패: ${res.message}`);
        }
    } catch (err) {
        console.error("요청 실패:", err);
        showMessageModal("서버 오류가 발생했습니다.");
    }
});

function updateGoldDisplay(amount) {
    const el = document.getElementById('goldDisplay');
    if (el) el.textContent = `${amount.toLocaleString()} G`;
}

document.getElementById('closeShopModal').addEventListener('click', () => {
    document.getElementById('shopModal').classList.add('hidden');
    document.getElementById('workshopSelectModal').classList.remove('hidden');
});

function renderShopList(items = [], type) {
    const listEl = document.getElementById('shopItemList');
    listEl.innerHTML = '';
    items.forEach(item => {
        const card = renderShopItemCard(item, type);
        listEl.appendChild(card);
    });
}
