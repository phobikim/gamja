

const bagModal = document.getElementById('bagModal');
const allItems = []; // 전체 아이템을 여기에 저장

const tooltip = document.getElementById('bagTooltip');
const content = document.getElementById('tooltipContent');

function renderItemsByType(type) {
    const bagList = document.getElementById("bagList");
    bagList.innerHTML = "";

    const filtered = (type === "ALL") ? allItems : allItems.filter(item => item.itemType === type);

    const slots = [];
    for (let i = 0; i < 30; i++) {
        const li = document.createElement('li');
        li.className = 'bag-slot';
        slots.push(li);
        bagList.appendChild(li);
    }

    let insertIndex = 0;
    filtered.forEach(item => {
        if (item.quantity === 0 || insertIndex >= 30) return;

        const slot = slots[insertIndex];
        slot.innerHTML = '';

        const wrapper = document.createElement('div');
        wrapper.className = 'item-image-wrapper';

        const img = document.createElement('img');
        img.src = img.src = basePath + item.iconPath || `/images/items/default.png`;
        img.alt = item.name || `item-${item.itemId}`;
        img.className = 'item-icon';

        img.onerror = () => {
            img.src = '/images/character/default.png';
        };

        wrapper.addEventListener('click', (e) => {
            showItemTooltip(e, item);
        });
        wrapper.appendChild(img);

        const span = document.createElement('span');
        span.className = 'item-count';
        span.textContent = `${item.quantity}`;

        slot.appendChild(wrapper);
        slot.appendChild(span);

        insertIndex++;
    });
}

function setTabActive(type) {
    document.querySelectorAll(".bag-tabs button").forEach(btn => {
        btn.classList.toggle('active', btn.dataset.type === type);
    });
}

// 탭 클릭 이벤트 연결
document.querySelectorAll(".bag-tabs button").forEach(btn => {
    btn.addEventListener("click", () => {
        const type = btn.dataset.type;
        setTabActive(type);
        renderItemsByType(type);
    });
});

bagModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.bag-modal-content');
    const isTooltip = e.target.closest('#bagTooltip'); // 툴팁 클릭도 허용
    if (!inside && !isTooltip) {bagModal.classList.add('hidden')};
});

document.getElementById('closeTooltipBtn').addEventListener('click', () => {
    document.getElementById('bagTooltip').classList.add('hidden');
});

async function loadBagItems() {
    playEffect("se_click2");
    const bagContent = document.querySelector('.bag-modal-content');
    bagContent.scrollTop = 0; // ← 스크롤 맨 위로 초기화
    tooltip.classList.add('hidden');
    const bagList = document.getElementById('bagList');
    bagList.innerHTML = '';

    // 1. 빈칸 먼저 채우기
    const slots = [];
    for (let i = 0; i < 30; i++) {
        const li = document.createElement('li');
        li.className = 'bag-slot';
        slots.push(li);
        bagList.appendChild(li);
    }

    try {
        const res = await apiRequest(`/api/util/item/list/${userId}`, 'GET');
        if (res.code === 'SUCCESS') {
            const data = res.data;
            allItems.length = 0;
            allItems.push(...data);
            setTabActive("ALL");
            renderItemsByType("ALL");
        }
    } catch (err) {
        console.error('가방 아이템 로딩 실패:', err);
    }
}

async function handleBagClick() {
    playEffect("se_click2");
    bagModal.classList.remove('hidden');
    await loadBagItems();
}

function showItemTooltip(event, item) {
    // 기존 툴팁 강제 닫기 (안 보이게)
    tooltip.classList.add('hidden');
    tooltip.classList.remove('hidden');

    const rarity = item.rarity || 'COMMON';
    const rarityClass = `rarity-${rarity.toLowerCase()}`;

    content.innerHTML = `
      <div style="text-align: center;"><strong>[${item.name}]</strong></div><br>
      희귀도: <span class="rarity-text ${rarityClass}">${rarity}</span><br>
      ${item.description || '설명이 없습니다.'}
    `;

    // 모달 안에 고정 배치되도록 설정 (모달이 relative 여야 함)
    const modal = document.getElementById('bagModal');
    modal.appendChild(tooltip);

}