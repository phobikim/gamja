const characterModal = document.getElementById('characterModal');

// 탭 버튼
const battleTabBtn = document.getElementById('battleTabBtn');
const lifeTabBtn = document.getElementById('lifeTabBtn');

// 영역들
const combatStats = document.getElementById('combatStats');
const lifeStats = document.getElementById('lifeStats');
const combatEquipment = document.getElementById('combatEquipment');
const lifeEquipment = document.getElementById('lifeEquipment');

// 강화 영역
let currentCharacter = null;
const openGrowthModalBtn = document.getElementById('openGrowthModalBtn');
const openEnhanceModalBtn = document.getElementById('openEnhanceModalBtn');
const openAlchemyModalBtn = document.getElementById('openAlchemyModalBtn');
window.readOnlyMode = false;

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
    if (statId === 'combatSpeed') {
        const percent = Math.max(10, total / 10);
        valueSpan.textContent = `${percent.toFixed(1)}%`;
        valueSpan.classList.add('stat-critical');
    } else {
        valueSpan.textContent = total;
    }

    // 순환 구조 적용 (100 단위 순환)
    const levelIndex = Math.floor(total / 100) % 4; // 0 ~ 3
    const levelClass = `level-${levelIndex + 1}`;
    const relativeValue = total % 100;

    const percent = Math.min(100, (relativeValue / max) * 100);

    // ✅ 클래스로만 처리
    barBg.innerHTML = `
        <div class="stat-bar-fill ${levelClass}" style="width:${percent}%"></div>
    `;
}

function updateLifeStatValue(statId, detail, max = 10) {
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

    const total = fromBase;
    valueSpan.textContent = total;

    // 생활스탯은 10 단위로 순환 구조 적용
    const levelIndex = Math.floor(total / 10) % 4;
    const levelClass = `level-${levelIndex + 1}`;
    const relativeValue = total % 10;

    const percent = Math.min(100, (relativeValue / max) * 100);

    barBg.innerHTML = `
        <div class="stat-bar-fill ${levelClass}" style="width:${percent}%; border-radius: 10px;"></div>
    `;
}

async function openInfoModal() {
    window.readOnlyMode = false;
    // 탭 초기화
    battleTabBtn.classList.add('active');
    lifeTabBtn.classList.remove('active');
    battleTabBtn.style.pointerEvents = 'auto';
    lifeTabBtn.style.pointerEvents = 'auto';

    combatStats.classList.remove('hidden');
    combatEquipment.classList.remove('hidden');
    lifeStats.classList.add('hidden');
    lifeEquipment.classList.add('hidden');

    const valid = await checkSessionValid();
    if (!valid) return;
    characterModal.classList.remove('hidden');

    // ✅ 탭 보이기
    document.getElementById('charTab').classList.remove('hidden');
    document.getElementById('characterActions').classList.remove('hidden');
    // ✅ 유저네임 헤더 숨기기
    document.getElementById('charUsernameHeader').classList.add('hidden');

    loadCharacterBasicInfo();
    loadCharacterBattleInfo();
    loadCharacterLifeInfo()
}



// ✅ 기본 정보 DOM 세팅
function setCharacterBasicInfo(data) {
    const imgEl = document.getElementById('charImage');
    if (data.characterImage) {
        imgEl.src = `https://phobi.my/gamja.img/images/character/${data.characterImage}`;
    }

    const skinEl = document.getElementById('charBorderSkin');
    if (skinEl) {
        const raw = data.borderSkinImageUrl;
        if (raw) {
            const url = raw.startsWith('http') ? raw : `${basePath}${raw}`;
            skinEl.style.backgroundImage = `url("${url}")`;

            // ✅ skin 있을 때
            imgEl.classList.remove('no-skin');
            imgEl.classList.add('with-skin');
        } else {
            skinEl.style.removeProperty('background-image');

            // ✅ skin 없을 때
            imgEl.classList.remove('with-skin');
            imgEl.classList.add('no-skin');
        }
    }



    document.getElementById('combatLevelValue').textContent = data.level;
    document.getElementById('characterName').textContent = data.dexName;
    document.getElementById('characterDescription').textContent = data.description || '';
    currentCharacter = data;
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

            if (item.enhancementLevel && item.enhancementLevel > 0) {
                const label = createEnhanceLabel(item.enhancementLevel);
                slot.appendChild(label);
            }
        }
    });
    renderSpecialOptions({ equippedItems: data.equippedItems });
    applyRarityToEachSlot(slotMap, data.equippedItems);
}

function setOtherUserBattleInfo(data) {
    // 스탯 바 표시
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

    // 장비 유무 확인용 map
    const equippedMap = {};
    if (Array.isArray(data.equippedItems)) {
        data.equippedItems.forEach(item => {
            equippedMap[item.equipSlot] = item;
        });
    }

    // 슬롯 초기화 및 장비 반영
    Object.entries(slotMap).forEach(([slotKey, slotId]) => {
        const slot = document.getElementById(slotId);
        if (!slot) return;

        const equippedItem = equippedMap[slotKey];
        slot.innerHTML = ''; // 기존 내용 초기화

        if (equippedItem) {
            // ✅ 장비 있음 → 이미지만 표시
            const img = document.createElement('img');
            img.src = `${window.basePath}${equippedItem.iconPath}`;
            img.alt = equippedItem.name;
            img.title = equippedItem.name;
            img.dataset.item = JSON.stringify(equippedItem);
            slot.appendChild(img);
            if (equippedItem.enhancementLevel && equippedItem.enhancementLevel > 0) {
                const label = createEnhanceLabel(equippedItem.enhancementLevel);
                slot.appendChild(label);
            }
        } else {
            // ✅ 장비 없음 → 텍스트만 유지
            slot.textContent = getSlotLabel(slotKey);
        }
    });
    renderSpecialOptions({ equippedItems: data.equippedItems });
    applyRarityToEachSlot(slotMap, data.equippedItems || []);
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
    updateLifeStatValue('lifeFishing', data.fishing);
    updateLifeStatValue('lifeWoodcutting', data.woodcutting);
    updateLifeStatValue('lifeGathering', data.gathering);
    updateLifeStatValue('lifeMining', data.mining);
    // updateLifeStatValue('lifeMaking', data.making);

    const badgeSlots = [
        document.getElementById('lifeSlotBADGE_1'),
        document.getElementById('lifeSlotBADGE_2'),
        document.getElementById('lifeSlotBADGE_3'),
        document.getElementById('lifeSlotBADGE_4')
    ];
    badgeSlots.forEach(slot => (slot.innerHTML = '뱃지'));
    // 슬롯 설정
    const slotMap = {
        FISHING_ROD: 'lifeSlotFISHING_ROD',
        AXE: 'lifeSlotAXE',
        PICKAXE: 'lifeSlotPICKAXE',
        KNIFE: 'lifeSlotKNIFE',
        BADGE: ['lifeSlotBADGE_1', 'lifeSlotBADGE_2', 'lifeSlotBADGE_3', 'lifeSlotBADGE_4']
    };
    Object.values(slotMap).forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = el.textContent; // 기본 텍스트 유지
    });
    let badgeIndex = 0;

    data.equippedItems.forEach(item => {
        if (item.equipSlot === 'BADGE') {
            if (badgeIndex < badgeSlots.length) {
                const slot = badgeSlots[badgeIndex++];
                const img = document.createElement('img');
                img.src = `${window.basePath}${item.iconPath}`;
                img.alt = item.name;
                img.title = item.name;
                img.dataset.item = JSON.stringify(item);
                slot.innerHTML = '';
                slot.appendChild(img);
            }
        } else {
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
        }
    });
    applyRarityToEachSlot(slotMap, data.equippedItems);
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

// 성장 모달 클릭
openGrowthModalBtn.addEventListener('click', () => {
    characterModal.classList.add('hidden');
    openGrowthModal(currentCharacter);
});

// 강화 모달 클릭
openEnhanceModalBtn.addEventListener('click', () => {
    characterModal.classList.add('hidden');
    openEnhanceModal();
});

// 연금 모달 클릭
openAlchemyModalBtn.addEventListener('click', () => {
    // showMessageModal("Opening Soon!")
    characterModal.classList.add('hidden');
    openAlchemyModal();
});

// 업적 모달 클릭
openAlchemyModalBtn.addEventListener('click', () => {
    // showMessageModal("Opening Soon!")
    characterModal.classList.add('hidden');
    openAchieveModal();
});


document.getElementById('characterModalClose').addEventListener('click', () => {
    document.getElementById('characterModal').classList.add('hidden');
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
        const slotEls = Array.isArray(slotId) ? slotId : [slotId];
        slotEls.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                el.style.backgroundColor = 'var(--baige-color)';
                el.classList.remove('rarity-legendary', 'pulse'); // 기존 후광 제거
            }
        });
    });

    const badgeIndexMap = { index: 0 };

    // 장착된 아이템에 rarity 색상 적용
    equippedItems.forEach(item => {
        const slotDef = slotMap[item.equipSlot];
        const rarityColor = rarityColors[item.rarity];

        if (!slotDef || !rarityColor) return;

        if (Array.isArray(slotDef)) {
            // BADGE의 경우
            const i = badgeIndexMap.index++;
            const slotId = slotDef[i];
            const slotEl = document.getElementById(slotId);
            if (slotEl) {
                slotEl.style.backgroundColor = rarityColor;
            }
        } else {
            // 일반 슬롯
            const slotEl = document.getElementById(slotDef);
            if (!slotEl) return;

            slotEl.style.backgroundColor = rarityColor;
            if (item.rarity === 'LEGENDARY') {
                slotEl.classList.add('rarity-legendary', 'pulse');
            }
        }
    });
}

function openCharacterModal(data, readOnly = false) {
    window.readOnlyMode = readOnly;
    characterModal.classList.remove('hidden');

    // 탭 숨기기 / 간판 영역 표시
    document.getElementById('charTab').classList.add('hidden');
    document.getElementById('characterActions').classList.add('hidden');
    document.getElementById('charUsernameHeader').classList.remove('hidden');

    // 유저 이름
    document.getElementById('charUsernameLabel').textContent = data.username || '';

    // 타이틀 텍스트
    const titleNameEl = document.getElementById('charTitleName');
    if (data.title) {
        titleNameEl.textContent = `[${data.title}]`;
        titleNameEl.classList.remove('hidden');
    } else {
        titleNameEl.textContent = '';
        titleNameEl.classList.add('hidden');
    }

    // 타이틀 아이콘
    const titleIconEl = document.getElementById('charTitleIcon');
    if (data.titleIconPath) {
        titleIconEl.src = `${window.basePath}${data.titleIconPath}`;
        titleIconEl.style.display = 'inline-block';
    } else {
        titleIconEl.style.display = 'none';
    }

    // 기본 정보 및 스탯
    setCharacterBasicInfo(data);
    if (data.battleStat) {
        setOtherUserBattleInfo(data.battleStat);
    }
    if (data.lifeStat) setCharacterLifeInfo(data.lifeStat);

    // 탭 상태 기본값
    battleTabBtn.classList.add('active');
    lifeTabBtn.classList.remove('active');
    combatStats.classList.remove('hidden');
    combatEquipment.classList.remove('hidden');
    lifeStats.classList.add('hidden');
    lifeEquipment.classList.add('hidden');

    if (readOnly) {
        document.querySelectorAll('.equip-slot').forEach(slot => {
            slot.style.pointerEvents = 'none';
            slot.style.opacity = '0.6'; // 시각적으로 구분도 가능
        });
        battleTabBtn.style.pointerEvents = 'none';
        lifeTabBtn.style.pointerEvents = 'none';
    } else {
        document.querySelectorAll('.equip-slot').forEach(slot => {
            slot.style.pointerEvents = 'auto';
            slot.style.opacity = '1';
        });
        battleTabBtn.style.pointerEvents = 'auto';
        lifeTabBtn.style.pointerEvents = 'auto';
    }
}

function getSlotLabel(equipSlot) {
    const labels = {
        WEAPON: '무기',
        HELMET: '머리',
        ARMOR: '상의',
        PANTS: '하의',
        SHOES: '신발',
        RING: '반지',
        NECK: '목걸이',
        POTION: '물약'
    };
    return labels[equipSlot] || '';
}

function createEnhanceLabel(level) {
    const label = document.createElement('div');
    label.className = 'enhance-label';
    label.textContent = `+${level}`;

    if (level >= 20) label.classList.add('enhance-tier-20');
    else if (level >= 15) label.classList.add('enhance-tier-15');
    else if (level >= 10) label.classList.add('enhance-tier-10');
    else if (level >= 5) label.classList.add('enhance-tier-5');

    return label;
}

function renderSpecialOptions({ totals, equippedItems }) {
    // 1) 기본 구조
    const sum = {
        defense: 0,
        crit_rate: 0,
        crit_dmg: 0,
        exp_gain: 0,
        gold_gain: 0,
    };

    // 3) equippedItems 기반으로 합산 (특수옵션 배열로 오거나 key-value로 올 수 있음)
    if (equippedItems && equippedItems.length) {
        for (const item of equippedItems) {
            if (Array.isArray(item.specialOptions)) {
                for (const opt of item.specialOptions) {
                    addIfMatch(sum, opt.key, opt.value);
                }
            }
            if (Array.isArray(item.alchemyOptions)) {
                for (const opt of item.alchemyOptions) {
                    const key = (opt.optionType || '').toLowerCase();
                    const val = opt.optionValue;
                    addIfMatch(sum, key, val);
                }
            }
            addIfMatch(sum, 'defense', item.defense);
            addIfMatch(sum, 'crit_rate', item.crit_rate);
            addIfMatch(sum, 'crit_dmg',  item.crit_dmg);
            addIfMatch(sum, 'exp_gain',  item.exp_gain);
            addIfMatch(sum, 'gold_gain', item.gold_gain);
        }
    }

    // 4) 출력 (퍼센트 표기 규칙: 0~1이면 0~100%로, 1 이상이면 이미 %값으로 간주)
    setStatText('optDefense', sum.defense);
    setStatText('optCritRate',  sum.crit_rate);
    setStatText('optCritDmg',   sum.crit_dmg);
    setStatText('optExpGain',   sum.exp_gain);
    setStatText('optGoldGain',  sum.gold_gain);
}

// 유틸
function addIfMatch(sum, key, val) {
    if (val == null) return;
    const k = (key || '').toString().toLowerCase();
    if (k === 'defense') sum.defense += toNumber(val);
    if (k === 'crit_rate') sum.crit_rate += toNumber(val);
    if (k === 'crit_dmg')  sum.crit_dmg  += toNumber(val);
    if (k === 'exp_gain')  sum.exp_gain  += toNumber(val);
    if (k === 'gold_gain') sum.gold_gain += toNumber(val);
}

function toNumber(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n : 0;
}

function setStatText(id, value) {
    const el = document.getElementById(id);
    if (!el) return;

    let numeric = toNumber(value);

    //치명타 피해량은 기본값 10%를 추가해서 표시
    if (id === 'optCritDmg') {
        numeric += 10;
    }

    // 퍼센트 출력 예외처리: 방어력은 flat 수치 그대로 출력
    let text;
    if (id === 'optDefense') {
        text = (numeric === 0 ? '0' : `${numeric}`);
    } else {
        text = (numeric === 0 ? '0%' : `${Number.isInteger(numeric) ? numeric : numeric.toFixed(1)}%`);
    }

    el.textContent = text;

    // 색상 클래스 처리 (공통)
    el.classList.remove('value-range-1', 'value-range-2', 'value-range-3', 'value-range-4', 'value-range-over');
    if (numeric <= 10) el.classList.add('value-range-1');
    else if (numeric <= 20) el.classList.add('value-range-2');
    else if (numeric <= 30) el.classList.add('value-range-3');
    else if (numeric <= 40) el.classList.add('value-range-4');
    else el.classList.add('value-range-over');
}

// 특수옵션 설명 텍스트 정의
const specialOptionDescription = `
    <strong class="special-option-name">방어력</strong> : 몬스터의 공격 피해를 먼저 막아주는 보호 수치입니다.<br>
    &nbsp;&nbsp;- 포션 효과로 회복되지 않으며, 전투 중 피해를 받으면 먼저 차감됩니다.<br><br>
    &nbsp;&nbsp;- 기본값은 <strong>0</strong>이며, <strong>특수 옵션</strong>에 의해 증가할 수 있습니다.<br><br>

    <strong class="special-option-name">치명타 확률</strong> : 공격 시 치명타가 발생할 확률입니다.<br>
    &nbsp;&nbsp;- 기본값은 <strong>10%</strong>이며, 감자의 <strong>스탯</strong>과 <strong>특수 옵션</strong>에 따라 증가할 수 있습니다.<br><br>

    <strong class="special-option-name">치명타 피해량</strong> : 치명타 발생 시 추가로 들어가는 피해량입니다.<br>
    &nbsp;&nbsp;- 기본값은 <strong>10%</strong>이며, <strong>특수 옵션</strong>에 의해 증가할 수 있습니다.<br><br>

    <strong class="special-option-name">경험치 획득량</strong> : 몬스터 처치 시 얻는 <strong>경험치</strong>가 증가합니다.<br><br>
    &nbsp;&nbsp;- 기본값은 <strong>0</strong>이며, <strong>특수 옵션</strong>에 의해 증가할 수 있습니다.<br><br>

    <strong class="special-option-name">골드 획득량</strong> : 몬스터 처치 시 얻는 <strong>감자코인</strong>이 증가합니다.<br>
    &nbsp;&nbsp;- 기본값은 <strong>0</strong>이며, <strong>특수 옵션</strong>에 의해 증가할 수 있습니다.<br><br>
`;

// 툴팁 표시 함수
function showSpecialTooltip(htmlContent) {
    const tooltip = document.getElementById('specialOptionTooltip');
    const text = document.getElementById('specialTooltipText');
    text.innerHTML = htmlContent;
    tooltip.classList.remove('hidden');

    tooltip.addEventListener('click', () => {
        tooltip.classList.add('hidden');
    }, { once: true });
}

// 이벤트 바인딩
document.getElementById('specialOptions')?.addEventListener('click', () => {
    showSpecialTooltip(specialOptionDescription);
});

