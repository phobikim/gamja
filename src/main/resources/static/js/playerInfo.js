const characterModal = document.getElementById('characterModal');
const itemtooltip = document.getElementById('itemTooltip');

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
    const total = detail.fromUser + detail.fromBase + detail.fromEquip;
    valueSpan.textContent = total;

    // 색상 순서: base → dex → equip
    const basePercent = (detail.fromBase / max) * 100;
    const dexPercent = (detail.fromUser / max) * 100;
    const equipPercent = (detail.fromEquip / max) * 100;

    barBg.innerHTML = `
    <div class="stat-bar-fill base" style="width:${basePercent}%"></div>
    <div class="stat-bar-fill user" style="width:${dexPercent}%"></div>
    <div class="stat-bar-fill equip" style="width:${equipPercent}%"></div>
  `;

}

function openInfoModal() {
    characterModal.classList.remove('hidden');
    // 캐릭터 기본 정보 호출
    loadCharacterBasicInfo();
    loadCharacterBattleInfo();
}


function loadCharacterBasicInfo() {
    const url = '/api/char';

    apiRequest(url, 'GET')
        .then(res => {
            if (res.code === 'SUCCESS') {
                const charRes = res.data;
                setCharacterBasicInfo(charRes);
                setUserInfo(charRes);
            } else {
                console.error('캐릭터 정보 불러오기 실패:', res.message);
            }
        })
        .catch(err => {
            console.error('API 요청 에러:', err);
        });
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
        WEAPON: 'combatSlotWeapon',
        HELMET: 'combatSlotHead',
        ARMOR: 'combatSlotTop',
        PANTS: 'combatSlotBottom',
        SHOES: 'combatSlotShoes',
        RING: 'combatSlotRing',
        NECK: 'combatSlotNeck',
        POTION: 'combatSlotPotion'
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
    bindItemTooltipEvents();
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
    updateStatValue('lifeMaking', data.making);

    // 슬롯 설정
    const slotMap = {
        FISHING_ROD: 'lifeSlotFishingRod',
        AXE: 'lifeSlotAxe',
        PICKAXE: 'lifeSlotPickaxe',
        GLOVE: 'lifeSlotGlove',
        RING: 'lifeSlotRing',
        NECKLACE: 'lifeSlotNecklace',
        BRACELET: 'lifeSlotBracelet',
        BELT: 'lifeSlotBelt'
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
    bindItemTooltipEvents();
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


characterModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.character-modal-content');
    if (!inside) characterModal.classList.add('hidden');
});


function bindItemTooltipEvents() {
    document.querySelectorAll('.inventoryCell img').forEach(img => {
        img.onclick = (e) => {
            const item = img.dataset.item ? JSON.parse(img.dataset.item) : null;
            if (item) showItemTooltip(item, e);
            e.stopPropagation();
        };
    });
}

document.addEventListener('click', (e) => {
    if (!itemtooltip.classList.contains('hidden') && !itemtooltip.contains(e.target)) {
        hideItemTooltip();
    }
});


document.getElementById('characterModalClose').addEventListener('click', () => {
    document.getElementById('characterModal').classList.add('hidden');
});

// 캐릭터 상세 정보로 이동
document.querySelector('.char-image-area').addEventListener('click', () => {
    // 여기에 보유 캐릭터 데이터를 넘겨야 함
    openCharacterSelectModal();
});
