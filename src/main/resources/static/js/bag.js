

const bagModal = document.getElementById('bagModal');
const allItems = []; // 전체 아이템을 여기에 저장

const tooltip = document.getElementById('bagTooltip');
const content = document.getElementById('tooltipContent');

function renderItemsByType(typeString) {
    const bagList = document.getElementById("bagList");
    bagList.innerHTML = "";
    const typeArray = typeString.split(',');
    const filtered = (typeString === "ALL")
        ? allItems
        : allItems.filter(item => typeArray.includes(item.itemType));

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
            e.stopPropagation(); // 버블링 방지 추가
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
        console.log("type:" , type);
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
        const res = await apiRequest('/api/util/item/list', 'GET');
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

function showItemTooltipBag(event, item) {
    event.stopPropagation();
    tooltip.classList.remove('hidden');

    const isEquip = item.itemType?.startsWith("EQUIP");
    const rarity = item.rarity || 'COMMON';

    // 아이템 정보 설정
    document.getElementById('bagTooltipName').textContent = `[${item.name}]`;
    document.getElementById('bagTooltipRarity').innerHTML = `희귀도: <span class="rarity-text rarity-${rarity.toLowerCase()}">${rarity}</span>`;
    document.getElementById('bagTooltipDescription').textContent = item.description || '설명이 없습니다.';

    // 버튼 영역 완전 재생성
    content.querySelector('.tooltip-btn-group')?.remove();

    const btnGroup = document.createElement('div');
    btnGroup.className = 'tooltip-btn-group';
    btnGroup.innerHTML = `
        <button class="tooltip-close-btn" onclick="hideTooltip()">닫기</button>
        ${isEquip ? `<button class="equip-btn" onclick="console.log('장착 이벤트')">장착</button>` : ''}
    `;

    content.appendChild(btnGroup);
    bagModal.appendChild(tooltip);
}

function equipItem(item) {
    // 예: 서버에 장착 요청 보내기
    apiRequest('/api/char/equip', 'POST', {
        itemId: item.itemId
    }).then(res => {
        if (res.code === 'SUCCESS') {
            alert('장착 완료!');
            tooltip.classList.add('hidden');
            loadCharacterBattleInfo(); // or loadBagItems() 등 갱신
        } else {
            alert('장착 실패: ' + res.message);
        }
    }).catch(err => {
        console.error('장착 오류:', err);
    });
}