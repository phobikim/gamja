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
function updateStatValue(statId, value, max = 50) {
    const block = document.getElementById(`stat-${statId}`);
    if (!block) return;
    const bar = block.querySelector('.stat-bar-fill');
    const valueSpan = block.querySelector('.stat-bar-value');
    bar.style.width = `${Math.min((value / max) * 100, 100)}%`;
    valueSpan.textContent = value;
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
                const data = res.data;
                setCharacterBasicInfo(res.data);
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

    updateStatValue('combatAtk', data.totalPower);
    updateStatValue('combatHp', data.totalHp);
    updateStatValue('combatSpeed', data.totalSpeed);

    const slotMap = {
        HELMET: 'combatSlotHead',
        ARMOR: 'combatSlotTop',
        PANTS: 'combatSlotBottom',
        WEAPON: 'combatSlotWeapon',
        SUB: 'combatSlotSub',
        SHOES: 'combatSlotShoes'
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
    updateStatValue('lifeFishing', data.fishing);
    updateStatValue('lifeWoodcutting', data.woodcutting);
    updateStatValue('lifeGathering', data.gathering);
    updateStatValue('lifeMining', data.mining);
    updateStatValue('lifeMaking', data.making);

    const slotMap = {
        HELMET: 'lifeSlotHead',
        ARMOR: 'lifeSlotTop',
        PANTS: 'lifeSlotBottom',
        TOOL: 'lifeSlotTool',
        SUB: 'lifeSlotSub',
        SHOES: 'lifeSlotShoes'
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

function generateStatBar({ containerId, id, label, icon, value, max = 50 }) {
    const container = document.getElementById(containerId);
    const template = document.getElementById('statBarTemplate');
    const clone = template.content.cloneNode(true);

    const iconEl = clone.querySelector('.stat-icon');
    const labelEl = clone.querySelector('.stat-label');
    const barEl = clone.querySelector('.stat-bar-fill');
    const valueEl = clone.querySelector('.stat-bar-value');

    iconEl.src = icon;
    labelEl.textContent = label;
    valueEl.textContent = value;
    barEl.style.width = `${Math.min((value / max) * 100, 100)}%`;

    container.appendChild(clone);
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
