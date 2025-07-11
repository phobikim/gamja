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
    const card = document.createElement('div');
    card.className = 'shop-item-card';

    card.innerHTML = `
        <img class="shop-item-thumb" src="${basePath}${item.iconPath}" alt="${item.name}" />
        <div class="shop-item-info">
            <div class="shop-item-name">
                ${item.name}
                ${
            type === 'sell' && item.quantity !== undefined
                ? `<span class="shop-item-qty">(x${item.quantity})</span>`
                : type === 'buy' && item.stock !== undefined
                    ? `<span class="shop-item-qty">(최대 ${item.stock}개 구매 가능)</span>`
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
        document.getElementById('shopTotalPrice').textContent =
            `${unitPrice} G ${currentTab === 'buy' ? '구매' : '판매'}`;

        // 실시간 변경
        slider.oninput = () => {
            const count = parseInt(slider.value);
            document.getElementById('shopSelectedQuantity').textContent = `x${count}`;
            const total = unitPrice * count;
            document.getElementById('shopTotalPrice').textContent =
                `${total} G ${currentTab === 'buy' ? '구매' : '판매'}`;
        };

        sliderArea.classList.remove('hidden');
    });

    return card;
}

function updateGoldDisplay(amount) {
    const el = document.getElementById('goldDisplay');
    if (el) el.textContent = `${amount} G`;
}

document.getElementById('closeShopModal').addEventListener('click', () => {
    document.getElementById('shopModal').classList.add('hidden');
    document.getElementById('workshopSelectModal').classList.remove('hidden');
});
