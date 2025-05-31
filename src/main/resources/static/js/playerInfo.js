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
    document.getElementById('combatAtkValue').textContent = data.totalPower;
    document.getElementById('combatHpCurrent').textContent = data.totalHp;
    document.getElementById('combatSpeedValue').textContent = data.totalSpeed;

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
    document.getElementById('skillFishingValue').textContent = data.fishing;
    document.getElementById('skillMiningValue').textContent = data.mining;
    document.getElementById('skillWoodcuttingValue').textContent = data.woodcutting;
    document.getElementById('skillGatheringValue').textContent = data.gathering;
    document.getElementById('skillCraftingValue').textContent = data.making;

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

function showItemTooltip(item) {
    document.getElementById('tooltipName').textContent = item.name;
    document.getElementById('tooltipRarity').textContent = item.rarity;
    document.getElementById('tooltipDescription').textContent = item.description;
    itemtooltip.classList.remove('hidden');
}

function bindItemTooltipEvents() {
    document.querySelectorAll('.inventoryCell img').forEach(img => {
        img.onclick = (e) => {
            const item = img.dataset.item ? JSON.parse(img.dataset.item) : null;
            if (item) showItemTooltip(item);
            e.stopPropagation();
        };
    });
}

document.addEventListener('click', (e) => {
    if (!itemtooltip.classList.contains('hidden') && !itemtooltip.contains(e.target)) {
        itemtooltip.classList.add('hidden');
    }
});