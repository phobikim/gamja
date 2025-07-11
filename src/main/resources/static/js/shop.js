let currentUserGold = 0;

function openShopModal() {
    document.getElementById('shopModal').classList.remove('hidden');
    loadShopItems('buy'); // 초기 탭: 구매
}

document.querySelectorAll('.shop-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.shop-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        const tabType = tab.dataset.tab;
        loadShopItems(tabType);
    });
});



async function loadShopItems(type) {
    const sliderArea = document.getElementById('shopSliderArea');
    sliderArea.classList.add('hidden');

    // 선택된 카드 초기화
    document.querySelectorAll('.shop-item-card').forEach(c => c.classList.remove('selected'));

    const listEl = document.getElementById('shopItemList');
    listEl.innerHTML = '';

    const url = type === 'buy' ? '/api/shop/sell-list' : '/api/shop/inventory';

    try {
        const res = await apiRequest(url, 'GET');
        if (res.code === 'SUCCESS') {
            const { gold, items } = res.data;
            currentUserGold = gold;
            updateGoldDisplay(gold);

            items.forEach(item => {
                const card = renderShopItemCard(item, type);
                listEl.appendChild(card);
            });
        }
    } catch (err) {
        console.error(`${type} 아이템 불러오기 실패`, err);
    }
}

function renderShopItemCard(item, type) {
    const confirmBtn = document.getElementById('shopConfirmButton');
    const warningEl = document.getElementById('shopGoldWarning');

    const card = document.createElement('div');
    card.className = 'shop-item-card';
    card.dataset.itemId = item.itemId;

    card.innerHTML = `
        <img class="shop-item-thumb" src="${basePath}${item.iconPath}" alt="${item.name}" />
        <div class="shop-item-info">
            <div class="shop-item-name">
                ${item.name}
                ${
            type === 'sell' && item.quantity !== undefined
                ? `<span class="shop-item-qty">(x${item.quantity})</span>`
                : type === 'buy' && item.stock !== undefined
                    ? `<span class="shop-item-qty">( 남은 ${item.availableQuantity}개 / 하루 ${item.stock}개 )</span>`
                    : ''
        }
            </div>
            <div class="shop-item-desc">${item.description}</div>
        </div>
        <div class="shop-item-price">
            ${type === 'buy' ? item.price : item.sellPrice} G
        </div>
    `;

    card.addEventListener('click', () => {
        document.querySelectorAll('.shop-item-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');

        const currentTab = document.querySelector('.shop-tab.active')?.dataset.tab ?? 'buy';
        const slider = document.getElementById('shopQuantitySlider');
        const sliderArea = document.getElementById('shopSliderArea');

        slider.value = 1;
        slider.max = type === 'buy'
            ? item.stock ?? 99
            : item.quantity ?? 99;

        document.getElementById('shopSelectedItemName').textContent = item.name;
        document.getElementById('shopSelectedQuantity').textContent = `x1`;
        document.getElementById('shopConfirmButton').textContent = currentTab === 'buy' ? '구매' : '판매';

        // 초기 예상 금액 표시
        const unitPrice = type === 'buy' ? item.price : item.sellPrice;
        let total = unitPrice * 1;
        document.getElementById('shopTotalPrice').textContent = `${total} G`;
        warningEl.classList.add('hidden');
        confirmBtn.disabled = false;
        if (currentTab === 'buy' && currentUserGold < total) {
            confirmBtn.disabled = true;
            warningEl.textContent = '골드가 부족합니다!';
            warningEl.classList.remove('hidden');
        } else if (currentTab === 'sell' && 1 > (item.quantity ?? 0)) {
            confirmBtn.disabled = true;
            warningEl.textContent = '보유 수량이 부족합니다!';
            warningEl.classList.remove('hidden');
        } else {
            confirmBtn.disabled = false;
            warningEl.classList.add('hidden');
        }
        // 실시간 변경
        slider.oninput = () => {
            const count = parseInt(slider.value);
            total = unitPrice * count;
            document.getElementById('shopSelectedQuantity').textContent = `x${count}`;
            document.getElementById('shopTotalPrice').textContent = `${total} G`;

            warningEl.classList.add('hidden');
            confirmBtn.disabled = false;

            if (currentTab === 'buy' && currentUserGold < total) {
                confirmBtn.disabled = true;
                warningEl.textContent = '골드가 부족합니다!';
                warningEl.classList.remove('hidden');
            } else if (currentTab === 'sell' && count > (item.quantity ?? 0)) {
                confirmBtn.disabled = true;
                warningEl.textContent = '보유 수량이 부족합니다!';
                warningEl.classList.remove('hidden');
            } else {
                confirmBtn.disabled = false;
                warningEl.classList.add('hidden');
            }
        };

        sliderArea.classList.remove('hidden');
    });

    return card;
}

document.getElementById('shopConfirmButton').addEventListener('click', async () => {
    const itemName = document.getElementById('shopSelectedItemName').textContent;
    const quantity = parseInt(document.getElementById('shopQuantitySlider').value);
    const selectedCard = document.querySelector('.shop-item-card.selected');
    if (!selectedCard || isNaN(quantity)) return;

    const itemId = selectedCard.dataset.itemId;
    const currentTab = document.querySelector('.shop-tab.active')?.dataset.tab ?? 'buy';
    const endpoint = currentTab === 'buy' ? '/api/shop/buy' : '/api/shop/sell';

    try {
        const res = await apiRequestJson(endpoint, 'POST', {
            itemId: itemId,
            quantity: quantity
        });

        if (res.code === 'SUCCESS') {
            showMessageModal(`${itemName} ${currentTab === 'buy' ? '구매' : '판매'} 완료!`);
            // 데이터 새로고침
            loadShopItems(currentTab);
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
    if (el) el.textContent = `${amount} G`;
}

document.getElementById('closeShopModal').addEventListener('click', () => {
    document.getElementById('shopModal').classList.add('hidden');
    document.getElementById('workshopSelectModal').classList.remove('hidden');
});
