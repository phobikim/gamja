

const bagModal = document.getElementById('bagModal');
const allItems = []; // 전체 아이템을 여기에 저장

const tooltip = document.getElementById('bagTooltip');
const content = document.getElementById('tooltipContent');

document.getElementById('closeBagModalBtn').addEventListener('click', () => {
    bagModal.classList.add('hidden');
    tooltip.classList.add('hidden');
});
function renderItemsByType(typeString) {
    const bagList = document.getElementById("bagList");
    bagList.innerHTML = "";

    const typeArray = typeString.split(',');
    const filtered = (typeString === "ALL")
        ? allItems
        : allItems.filter(item => typeArray.includes(item.itemType));

    // 🔹 보여줄 아이템 개수
    const itemCount = filtered.filter(item => item.quantity > 0).length;

    // 🔹 보유 수량에 맞춰 슬롯 개수 계산 (5칸씩 줄 맞춤)
    const slotCount = Math.ceil(itemCount / 5) * 5;

    // 슬롯 생성
    const slots = [];
    for (let i = 0; i < slotCount; i++) {
        const li = document.createElement('li');
        li.className = 'bag-slot';
        slots.push(li);
        bagList.appendChild(li);
    }

    let insertIndex = 0;
    filtered.forEach(item => {
        if (item.quantity === 0 || insertIndex >= slotCount) return;

        const slot = slots[insertIndex];
        slot.innerHTML = '';

        const wrapper = document.createElement('div');
        const rarityClass = item.rarity ? `rarity-background-${item.rarity.toLowerCase()}` : 'rarity-background-common';
        wrapper.classList.add('item-image-wrapper', rarityClass);

        const img = document.createElement('img');
        img.src = basePath + item.iconPath || `/images/items/default.png`;
        img.alt = item.name || `item-${item.itemId}`;
        img.className = 'item-icon';

        img.onerror = () => {
            img.src = '/images/character/default.png';
        };

        wrapper.addEventListener('click', (e) => {
            e.stopPropagation();
            showItemTooltipBag(e, item);
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
        // 🔥 스크롤 맨 위로 초기화
        const bagContent = document.querySelector('.bag-modal-content');
        if (bagContent) {
            bagContent.scrollTop = 0;
        }
        setTabActive(type);
        renderItemsByType(type);
    });
});

bagModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.bag-modal-content');
    const isTooltip = e.target.closest('#bagTooltip');

    // 툴팁이 열려있는 상태에서 툴팁 외부를 클릭했을 때만 툴팁 닫기
    if (!tooltip.classList.contains('hidden') && !isTooltip) {
        tooltip.classList.add('hidden');
        return;
    }

    // 모달 바깥을 클릭했을 때 모달 닫기
    if (!inside && !isTooltip) {
        bagModal.classList.add('hidden');
        tooltip.classList.add('hidden');
    }
});

function hideTooltip() {
    tooltip.classList.add('hidden');
}

async function loadBagItems() {
    const bagContent = document.querySelector('.bag-modal-content');
    const bagList = document.getElementById('bagList');

    bagContent.scrollTop = 0;
    tooltip.classList.add('hidden');
    bagList.innerHTML = '';

    // 초기 슬롯 30개만 넣기 (ALL 탭일 경우 나중에 render에서 다시 그려짐)
    for (let i = 0; i < 30; i++) {
        const li = document.createElement('li');
        li.className = 'bag-slot';
        bagList.appendChild(li);
    }

    const success = await getBagItems();
    if (success) {
        const defaultType = "ALL";
        setTabActive(defaultType);
        renderItemsByType(defaultType);
    }
}

// 아이템 List 호출
async function getBagItems() {
    try {
        const res = await apiRequest('/api/util/item/list', 'GET');
        if (res.code === 'SUCCESS') {
            allItems.length = 0;
            allItems.push(...res.data);
            return true;
        }
    } catch (err) {
        console.error('가방 아이템 API 호출 실패:', err);
    }
    return false;
}

async function handleBagClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    playEffect("se_click2");
    bagModal.classList.remove('hidden');
    await loadBagItems();
}

function showItemTooltipBag(event, item) {
    event.stopPropagation();
    tooltip.classList.remove('hidden');

    const isEquip = item.itemType?.startsWith("EQUIP");
    const rarity = item.rarity || 'COMMON';
    const equipped = item.equipped;

    const nameEl = document.getElementById('bagTooltipName');
    const rarityEl = document.getElementById('bagTooltipRarity');
    const descEl = document.getElementById('bagTooltipDescription');
    const closeBtn = document.getElementById('bagTooltipCloseBtn');
    const equipBtn = document.getElementById('bagTooltipEquipBtn');

    // 정보 설정
    nameEl.textContent = `[${item.name}]`;
    rarityEl.innerHTML = `희귀도: <span class="rarity-text rarity-${rarity.toLowerCase()}">${rarity}</span>`;
    descEl.textContent = item.description || '설명이 없습니다.';

    // 닫기 버튼 이벤트
    closeBtn.onclick = hideTooltip;

    // 장착 버튼 제어
    if (isEquip) {
        equipBtn.classList.remove('hidden');

        if (equipped) {
            equipBtn.textContent = '장착중';
            equipBtn.disabled = true;
            equipBtn.classList.add('equipped');
            equipBtn.style.backgroundColor = '#73685e';
            equipBtn.onclick = null;
        } else {
            equipBtn.textContent = '장착';
            equipBtn.disabled = false;
            equipBtn.classList.remove('equipped');
            equipBtn.style.backgroundColor = '';
            equipBtn.onclick = () => equipItem(item);
        }
    } else {
        equipBtn.classList.add('hidden');
        equipBtn.onclick = null;
    }

    // 툴팁 부착
    bagModal.appendChild(tooltip);
}


async function equipItem(item) {
    const url = '/api/util/item/equip';
    try {
        const res = await apiRequestJson(url, 'POST', {
            itemId: item.itemId
        });
        if (res.code === 'SUCCESS') {
            tooltip.classList.add('hidden');
            showMessageModal("장착 완료!");
            const success = await getBagItems();
            if (success) {
                renderItemsByType("EQUIP_GATHER,EQUIP_BATTLE"); // or 현재 탭 타입 유지하고 싶으면 따로 저장된 type 사용
            }
        }
    } catch (e) {
        showMessageModal("아이템 추가 중 오류가 발생했습니다.");
    }
}