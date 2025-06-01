const characterModal = document.getElementById('characterModal');
const itemtooltip = document.getElementById('itemTooltip');

function openInfoModal() {
    characterModal.classList.remove('hidden');
    // 캐릭터 기본 정보 호출
    loadCharacterBasicInfo();
    loadCharacterBattleInfo();
}
// 탭 버튼
const battleTabBtn = document.getElementById('battleTabBtn');
const lifeTabBtn = document.getElementById('lifeTabBtn');

// 영역들
const combatStats = document.getElementById('combatStats');
const lifeStats = document.getElementById('lifeStats');
const combatEquipment = document.getElementById('combatEquipment');
const lifeEquipment = document.getElementById('lifeEquipment');



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
    const url = '/api/char/battle';

    apiRequest(url, 'GET')
        .then(res => {
            if (res.code === 'SUCCESS') {
                const data = res.data;
                setCharacterBattleInfo(res.data);
            } else {
                console.error('캐릭터 정보 불러오기 실패:', res.message);
            }
        })
        .catch(err => {
            console.error('API 요청 에러:', err);
        });
}

// ✅ 전투 정보 DOM 세팅
function setCharacterBattleInfo(data) {

    const stats = [
        {
            id: 'combatAtk',
            label: '공격력',
            icon: 'https://phobi.me/gamja.img/images/icons/icon_power.png',
            value: data.totalPower
        },
        {
            id: 'combatHp',
            label: '체력',
            icon: 'https://phobi.me/gamja.img/images/icons/icon_hp.png',
            value: data.totalHp
        },
        {
            id: 'combatSpeed',
            label: '스피드',
            icon: 'https://phobi.me/gamja.img/images/icons/icon_speed.png',
            value: data.totalSpeed
        }
    ];

    const container = document.getElementById('combatStats');
    container.innerHTML = ''; // 기존 내용 지우고
    stats.forEach(stat => generateStatBar({ ...stat, containerId: 'combatStats' }));

    const slotMap = {
        HEAD: 'combatSlotHead',
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
            slot.innerHTML = ''; // 기존 이미지 지우고

            const img = document.createElement('img');
            img.src = `${window.basePath}${item.iconPath}`;
            img.alt = item.name;
            img.title = item.name;
            img.dataset.item = JSON.stringify(item); // 안전하게 저장

            slot.appendChild(img);
        }
    });
    bindItemTooltipEvents();
}

function loadCharacterLifeInfo() {
    const url = '/api/char/life';

    apiRequest(url, 'GET')
        .then(res => {
            if (res.code === 'SUCCESS') {
                const data = res.data;
                setCharacterLifeInfo(res.data);

            } else {
                console.error('캐릭터 정보 불러오기 실패:', res.message);
            }
        })
        .catch(err => {
            console.error('API 요청 에러:', err);
        });
}

// ✅ 생활 정보 DOM 세팅
function setCharacterLifeInfo(data) {
    const stats = [
        { label: '낚시', icon: 'https://phobi.me/gamja.img/images/icon_fishing.png', value: data.fishing },
        { label: '벌목', icon: 'https://phobi.me/gamja.img/images/icon_woodcut.png', value: data.woodcutting },
        { label: '채집', icon: 'https://phobi.me/gamja.img/images/icon_gather.png', value: data.gathering },
        { label: '채광', icon: 'https://phobi.me/gamja.img/images/icon_mining.png', value: data.mining },
        { label: '제작', icon: 'https://phobi.me/gamja.img/images/icon_craft.png', value: data.making }
    ];

    const container = document.getElementById('lifeStats');
    container.innerHTML = ''; // 기존 내용 지우고
    stats.forEach(stat => generateStatBar({ ...stat, containerId: 'lifeStats' }));

    const slotMap = {
        HEAD: 'lifeSlotHead',
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
            slot.innerHTML = ''; // 기존 이미지 지우고

            const img = document.createElement('img');
            img.src = `${window.basePath}${item.iconPath}`;
            img.alt = item.name;
            img.title = item.name;
            img.dataset.item = JSON.stringify(item); // 안전하게 저장

            slot.appendChild(img);
        }
    });
    bindItemTooltipEvents();
}

function generateStatBar({ containerId, id, label, icon, value, max = 30 }) {
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