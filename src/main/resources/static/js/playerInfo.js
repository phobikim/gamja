const characterModal = document.getElementById('characterModal');

// 탭 버튼
const battleTabBtn = document.getElementById('battleTabBtn');
const lifeTabBtn = document.getElementById('lifeTabBtn');

// 영역들
const combatStats = document.getElementById('combatStats');
const lifeStats = document.getElementById('lifeStats');
const combatEquipment = document.getElementById('combatEquipment');
const lifeEquipment = document.getElementById('lifeEquipment');


// 수치 조정
function updateStatValue(statId, detail, max = 100) {
    const block = document.getElementById(`stat-${statId}`);
    if (!block) return;

    const barBg = block.querySelector('.stat-bar-bg');
    const valueSpan = block.querySelector('.stat-bar-value');

    const {
        fromUser = 0,
        fromBase = 0,
        fromEquip = 0,
        fromTier = 0
    } = detail;

    const total = fromUser + fromBase + fromEquip + fromTier;
    valueSpan.textContent = total;

    let levelClass = 'level-1';
    let relativeValue = total;

    if (total > 300) {
        levelClass = 'level-4';
        relativeValue = total - 300;
    } else if (total > 200) {
        levelClass = 'level-3';
        relativeValue = total - 200;
    } else if (total > 100) {
        levelClass = 'level-2';
        relativeValue = total - 100;
    }


    const percent = Math.min(100, (relativeValue / max) * 100);

    // ✅ 클래스로만 처리
    barBg.innerHTML = `
        <div class="stat-bar-fill ${levelClass}" style="width:${percent}%"></div>
    `;
}

function openInfoModal() {
    characterModal.classList.remove('hidden');
    // 캐릭터 기본 정보 호출
    loadCharacterBasicInfo();
    loadCharacterBattleInfo();
}




// ✅ 기본 정보 DOM 세팅
function setCharacterBasicInfo(data) {
    if (data.characterImage) {
        document.getElementById('charImage').src =
            `https://phobi.me/gamja.img/images/character/${data.characterImage}`;
    }
    document.getElementById('combatLevelValue').textContent = data.level;
    document.getElementById('characterName').textContent = data.name;

}

function loadCharacterBattleInfo() {
    apiRequest('/api/char/battle', 'GET')
        .then(res => {
            if (res.code === 'SUCCESS') setCharacterBattleInfo(res.data);
            else console.error('전투 정보 불러오기 실패:', res.message);
        })
        .catch(console.error);
}

// ✅ 전투 정보 DOM 세팅
function setCharacterBattleInfo(data) {
    updateStatValue('combatAtk', data.power);
    updateStatValue('combatHp', data.hp);
    updateStatValue('combatSpeed', data.speed);

    const slotMap = {
        WEAPON: 'combatSlotWEAPON',
        HELMET: 'combatSlotHELMET',
        ARMOR: 'combatSlotARMOR',
        PANTS: 'combatSlotPANTS',
        SHOES: 'combatSlotSHOES',
        RING: 'combatSlotRING',
        NECK: 'combatSlotNECK',
        POTION: 'combatSlotPOTION'
    };

    data.equippedItems.forEach(item => {
        const slotId = slotMap[item.equipSlot];
        if (slotId) {
            const slot = document.getElementById(slotId);
            slot.innerHTML = '';
            const img = document.createElement('img');
            img.src = `${window.basePath}${item.iconPath}`;
            img.alt = item.name;
            img.title = item.name;
            img.dataset.item = JSON.stringify(item);
            slot.appendChild(img);
        }
    });
    applyRarityToEachSlot(slotMap, data.equippedItems);
}
function loadCharacterLifeInfo() {
    apiRequest('/api/char/life', 'GET')
        .then(res => {
            if (res.code === 'SUCCESS') setCharacterLifeInfo(res.data);
            else console.error('생활 정보 불러오기 실패:', res.message);
        })
        .catch(console.error);
}

// ✅ 생활 정보 DOM 세팅
function setCharacterLifeInfo(data) {
    // 스탯 설정
    updateStatValue('lifeFishing', data.fishing);
    updateStatValue('lifeWoodcutting', data.woodcutting);
    updateStatValue('lifeGathering', data.gathering);
    updateStatValue('lifeMining', data.mining);
    // updateStatValue('lifeMaking', data.making);

    // 슬롯 설정
    const slotMap = {
        FISHING_ROD: 'lifeSlotFISHING_ROD',
        AXE: 'lifeSlotAXE',
        PICKAXE: 'lifeSlotPICKAXE',
        KNIFE: 'lifeSlotKNIFE'
    }
    data.equippedItems.forEach(item => {
        const slotId = slotMap[item.equipSlot];
        if (slotId) {
            const slot = document.getElementById(slotId);
            slot.innerHTML = '';
            const img = document.createElement('img');
            img.src = `${window.basePath}${item.iconPath}`;
            img.alt = item.name;
            img.title = item.name;
            img.dataset.item = JSON.stringify(item);
            slot.appendChild(img);
        }
    });
}

// 탭 전환 핸들러
battleTabBtn.addEventListener('click', () => {
    loadCharacterBattleInfo();
    battleTabBtn.classList.add('active');
    lifeTabBtn.classList.remove('active');

    combatStats.classList.remove('hidden');
    combatEquipment.classList.remove('hidden');

    lifeStats.classList.add('hidden');
    lifeEquipment.classList.add('hidden');
});

lifeTabBtn.addEventListener('click', () => {
    loadCharacterLifeInfo();
    lifeTabBtn.classList.add('active');
    battleTabBtn.classList.remove('active');

    lifeStats.classList.remove('hidden');
    lifeEquipment.classList.remove('hidden');

    combatStats.classList.add('hidden');
    combatEquipment.classList.add('hidden');
});


document.getElementById('characterModalClose').addEventListener('click', () => {
    document.getElementById('characterModal').classList.add('hidden');
});

// 캐릭터 상세 정보로 이동
document.querySelector('.char-image-area').addEventListener('click', () => {
    // 여기에 보유 캐릭터 데이터를 넘겨야 함
    openCharacterSelectModal();
});
function applyRarityToEachSlot(slotMap, equippedItems) {
    const rarityColors = {
        'COMMON':'var(--baige-color)',
        'UNCOMMON': '#6ee367',
        'RARE': '#5492d7',
        'EPIC': '#c5589e',
        'LEGENDARY': '#fff2b8'
    };

    // 모든 슬롯 초기화 (기본 배경색으로)
    Object.values(slotMap).forEach(slotId => {
        const slotEl = document.getElementById(slotId);
        if (slotEl) {
            slotEl.style.backgroundColor = 'var(--baige-color)'; // 기본값
        }
    });

    // 장착된 아이템에 rarity 색상 적용
    equippedItems.forEach(item => {
        const slotId = slotMap[item.equipSlot];
        if (slotId && item.rarity && rarityColors[item.rarity]) {
            const slotEl = document.getElementById(slotId);
            if (slotEl) {
                slotEl.style.backgroundColor = rarityColors[item.rarity];
            }
        }
    });
}