// battle.js 파일에서만 유지할 로직들 — 전투 시작 및 처리 관련
const battleModal = document.getElementById('battleModal');
const lootModal = document.getElementById('lootModal');
const defeatModal = document.getElementById('defeatModal');
const monsterImage = document.getElementById('monsterCharacter');
const playerImage = document.getElementById('userCharacter');

let battleEnded = false;
let isPlayerTurn = true;
let isProcessingTurn = false;

let potionUsed = false;
let tempPowerBoost = 0;

const skillEffectImage = `${basePath_image}/effect/attack_slash5.png`;

window.battleState = {
    player: {},
    monster: {}
};

/**
 * 몬스터 드랍 아이템 생성 로직
 *
 * [1] COMMON 아이템 (확정 드랍)
 *  - rarity가 'COMMON'인 아이템 중 1개 무조건 드랍
 *  - 수량: 랜덤 2~3개
 *
 * [2] 보너스 아이템 (확률 드랍)
 *  - rarity가 'COMMON'이 아닌 아이템 중, item.dropRate 기반으로 드랍 시도
 *  - dropRate 통과한 아이템이 있을 경우, 가중치(rarity)에 따라 1개 선택
 *
 * [3] 보너스 아이템 수량 규칙
 *  - itemType이 'EQUIP_'로 시작하는 장비 아이템: 무조건 1개
 *  - UNCOMMON 등급: 1~2개 랜덤
 *  - RARE 이상: 1개 고정
 *
 * ※ rarity 가중치는 서버 기준과 통일 (COMMON: 75.0 ~ LEGENDARY: 0.01)
 * ※ pickWeightedByRarity 함수에서 rarity 기반 비율로 선택함
 */

const cardPool = [
    {
        name: "감자의 분노",
        desc: "공격력 x2! 실패 시 자해",
        mp: 3,
        effect: () => {
            // 추후 구현
        }
    },
    {
        name: "감자 던지기",
        desc: "랜덤 효과 발동",
        mp: 2,
        effect: () => {}
    },
    {
        name: "수분 회오리",
        desc: "체력 회복. 실패 시 MP -3",
        mp: 4,
        effect: () => {}
    },
    {
        name: "튀김 부메랑",
        desc: "두 번 타격. 50% 확률",
        mp: 3,
        effect: () => {}
    }
];

function drawBattleCards() {
    const container = document.getElementById('battleCardOptions');
    container.innerHTML = '';

    const shuffled = [...cardPool].sort(() => 0.5 - Math.random()).slice(0, 2);

    shuffled.forEach((card, index) => {
        const cardDiv = document.createElement('div');
        cardDiv.className = 'battle-card';
        cardDiv.onclick = () => selectBattleCard(index);

        cardDiv.innerHTML = `
            <div class="card-title">🔥 ${card.name}</div>
            <div class="card-desc">${card.desc}</div>
            <div class="card-cost">MP ${card.mp}</div>
        `;

        container.appendChild(cardDiv);
    });

    // 저장해놓기
    window.currentBattleCards = shuffled;
}

function selectBattleCard(index) {
    const card = window.currentBattleCards[index];
    if (!card) return;
    playEffect("se_select");
    console.log(`선택된 카드: ${card.name}`);
    card.effect(); // 추후 구체 효과
}


window.startBattleFromMap = async function(map) {
    battleModal.classList.remove('hidden');

    const userRes = await apiRequest('/api/battle/user-stat', 'GET');
    if (userRes.code !== 'SUCCESS') {
        showMessageModal(userRes.message || "유저 정보를 불러오지 못했습니다.");
        return;
    }
    const user = userRes.data;

    const monsterRes = await apiRequest(`/api/battle/monster_stat?mapId=${map.id}`, 'GET');
    if (monsterRes.code !== 'SUCCESS') {
        showMessageModal(monsterRes.message || "몬스터 정보를 불러오지 못했습니다.");
        return;
    }
    const monsters = monsterRes.data;
    if (!monsters || monsters.length === 0) {
        battleModal.classList.add('hidden');
        showMessageModal("해당 맵에 등장하는 몬스터가 없습니다.");
        return;
    }
    const selectedMonster = monsters[Math.floor(Math.random() * monsters.length)];

    initializeBattleScene(user, selectedMonster);
    startBattle(user, selectedMonster);
};

function initializeBattleScene(user, monster) {
    const background = document.getElementById('battleBackground');
    if (background && window.selectedMap?.imagePath) {
        background.src = basePath + window.selectedMap.imagePath;
    }

    const mapNameBanner = document.getElementById('battleMapName');
    if (mapNameBanner) {
        mapNameBanner.textContent = window.selectedMap?.name || '전투 지역';
    }


    const userImage = document.getElementById('userCharacter');
    userImage.src = basePath_image + "/character/" + user.charImage;
    userImage.alt = user.dexName;
    playerImage.classList.remove('jump-in', 'fade-out', 'hit-effect');
    void userImage.offsetWidth;
    userImage.classList.add('jump-in');

    const monsterImg = document.getElementById('monsterCharacter');
    monsterImg.src = basePath + monster.imagePath;
    monsterImg.alt = monster.name;
    monsterImg.classList.remove('jump-in', 'fade-out', 'hit-effect');
    void monsterImg.offsetWidth;
    monsterImg.classList.add('jump-in');

    const effectElement = document.getElementById('monsterEffect');
    if (effectElement) {
        effectElement.className = 'monster-effect';
        const rankMap = {
            '야생': 'effect-common',
            '일반': 'effect-uncommon',
            '희귀': 'effect-rare',
            '정예': 'effect-epic',
            '보스': 'effect-legendary'
        };
        const rankClass = rankMap[monster.rank];
        if (rankClass) effectElement.classList.add(rankClass);
        const container = document.getElementById('monsterEffectContainer');
        if (container) {
            container.classList.remove('hidden');
            effectElement.classList.add('pop');
            setTimeout(() => {
                container.classList.add('hidden');
                effectElement.classList.remove('pop');
            }, 800);
        }
    }

    document.getElementById('playerNameLabel').textContent = user.dexName;
    document.getElementById('playerAttrLabel').textContent = user.attribute;

    document.getElementById('monsterNameLabel').textContent = monster.name;
    document.getElementById('monsterAttrLabel').textContent = monster.rank; // or 속성 이름이 있다면 그걸로!

}

function updateHpBar(current, max, barId, textId) {
    const bar = document.getElementById(barId);
    const text = document.getElementById(textId);

    const percent = Math.max(0, Math.min(100, (current / max) * 100));
    bar.style.width = `${percent}%`;

    const displayCurrent = Math.max(0, current);
    text.textContent = `${displayCurrent} / ${max}`;

    // ✅ 초록색 회복 이펙트 (물약 사용 시만)
    if (barId === 'playerHpBar' && potionUsed) {
        bar.classList.remove('hp-heal-effect');
        void bar.offsetWidth;
        bar.classList.add('hp-heal-effect');

        // 한 번만 적용되게 potionUsed 초기화 여기서!
        potionUsed = false;

        bar.addEventListener('animationend', function handleAnimEnd() {
            bar.classList.remove('hp-heal-effect');
            bar.removeEventListener('animationend', handleAnimEnd);
        });
    }

}

function resetBattleState(user, monster) {
    battleState.player = {
        dexName: user.dexName,
        attribute: user.attribute,
        maxHp: user.hp?.total || 10,
        currentHp: user.hp?.total || 10,
        power: user.power?.total || 5,
        speed: user.speed?.total || 5,
        currentXp: user.xp,
        lv: user.lv,
        charImg: user.charImage,

        //포션 정보 저장
        potion: user.potion || {
            itemPath: '',
            quantity: 0,
            bonusHp: 0,
            bonusPower: 0
        }
    };
    battleState.monster = {
        id: monster.id,
        name: monster.name,
        maxHp: monster.monsterHp,
        currentHp: monster.monsterHp,
        power: monster.monsterPower,
        drops: monster.dropItems || [],
        exp: monster.monsterXp,
        rank: monster.rank,
        imagePath: monster.imagePath
    };
}

function startBattle(user, monster) {
    tempPowerBoost = 0;
    updateBattleUI();
    battleEnded = false;
    isPlayerTurn = true;
    isProcessingTurn = false;
    resetBattleState(user, monster);
    updateBattleUI();
    updateButtonStates();

    // 🔥 HP 바 초기화
    updateHpBar(
        battleState.player.currentHp,
        battleState.player.maxHp,
        "playerHpBar",
        "playerHpText"
    );
    updateHpBar(
        battleState.monster.currentHp,
        battleState.monster.maxHp,
        "monsterHpBar",
        "monsterHpText"
    );
    const defendBtn = document.getElementById('defendBtn');
    const potion = battleState.player.potion;

    if (defendBtn && potion?.itemPath) {
        defendBtn.innerHTML = `
        <div class="potion-btn-wrapper">
            <img src="${basePath}${potion.itemPath}" alt="물약" class="potion-btn-image">
            <div class="potion-count-label">x${potion.quantity}</div>
        </div>
    `;
        defendBtn.disabled = potion.quantity <= 0;
    }
}

function updateBattleUI() {
    const p = battleState.player;
    const m = battleState.monster;
    // 🔥 HP 바도 갱신
    updateHpBar(p.currentHp, p.maxHp, "playerHpBar", "playerHpText");
    updateHpBar(m.currentHp, m.maxHp, "monsterHpBar", "monsterHpText");

    const atkBase = p.power - tempPowerBoost;
    const atkDisplay = document.getElementById('attackPowerDisplay');
    if (atkDisplay) {
        if (tempPowerBoost > 0) {
            atkDisplay.textContent = `ATK: ${atkBase} +${tempPowerBoost}`;
        } else {
            atkDisplay.textContent = `ATK: ${p.power}`;
        }
    }
}


function updateButtonStates() {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');
    const canAct = isPlayerTurn && !isProcessingTurn && !battleEnded;
    disableBattleButtons(!canAct);
}

function doAttack() {
    if (!isPlayerTurn || isProcessingTurn || battleEnded) return;
    playEffect("se_attack");
    const attackBtn = document.getElementById('attackBtn');
    animateButton(attackBtn);
    isProcessingTurn = true;
    updateButtonStates();

    const damage = battleState.player.power;
    battleState.monster.currentHp -= damage;
    applyHitEffect('.monster-character');
    showSkillEffect();
    showDamageText('.monster-container', damage);
    updateBattleUI();

    if (battleState.monster.currentHp <= 0) {
        // ✅ 버튼 전부 비활성화
        disableBattleButtons(true);

        setTimeout(() => {
            winBattle();
        }, 1000);
        return;
    }

    isPlayerTurn = false;
    setTimeout(() => monsterTurn(), 500);
}

function monsterTurn() {
    if (isPlayerTurn || battleEnded) return; // 먼저 체크

    // ✅ 물약 효과 원상복구
    if (tempPowerBoost > 0) {
        battleState.player.power -= tempPowerBoost;
        tempPowerBoost = 0;
    }

    isProcessingTurn = true;

    const damage = Math.floor(Math.random() * battleState.monster.power) + 1;
    battleState.player.currentHp -= damage;
    applyHitEffect('.player-character');
    showDamageText('.player-container', damage);

    updateBattleUI();

    // 플레이어 턴 시작 전 버튼 상태 갱신 → 공격/힐 비활성화 유지됨
    isPlayerTurn = true;
    isProcessingTurn = false;
    // ✅ 먼저 체력 확인
    if (battleState.player.currentHp <= 0) {
        // 죽었으면 버튼 비활성화
        disableBattleButtons(true);

        setTimeout(() => {
            showDefeatModal("여기에 다시 묻히다...");
        }, 800);
    } else {
        // 살아있으면 1초 후 버튼 활성화
        setTimeout(() => {
            updateButtonStates();
        }, 1000);
    }
}

function showDefeatModal(text) {
    defeatModal.classList.remove('hidden');
    document.getElementById('defeatSignText').innerText = text || "여기에 다시 묻히다...";
}

function winBattle() {
    battleEnded = true;
    isProcessingTurn = false;
    updateBattleUI();
    updateButtonStates();
    monsterImage.classList.remove('jump-in', 'hit-effect', 'fade-out');
    monsterImage.style.animation = 'none';
    void monsterImage.offsetWidth;
    monsterImage.style.animation = '';
    monsterImage.classList.add('fade-out');
    updateWinBox();
}

function getCurrentBattleUser() {
    const p = battleState.player;
    return {
        dexName: p.dexName,
        attribute: p.attribute,
        beforeXp: p.currentXp,
        beforeLevel: p.lv,
        charImage: p.charImg,
        power: p.power,
        speed: p.speed,
        xp: p.currentXp,
        lv: p.lv,
        maxHp: p.maxHp
    };
}

function updateWinBox() {
    lootModal.classList.remove('hidden');
    const rewardStage = document.getElementById('rewardStage');
    if (rewardStage) rewardStage.style.display = 'block';
    showRewardResults();
}

function showRewardResults() {
    const currentUser = getCurrentBattleUser();
    const expReward = battleState.monster.exp || 0;
    const dropItems = generateLootItems();
    updateCharacterReward(currentUser, expReward, dropItems);
    updateLootItemsDisplay(dropItems);
}

function updateCharacterReward(user, expReward, items) {
    const payload = {
        exp: expReward,
        monsterId: battleState.monster.id,
        items: items.map(item => ({ itemId: item.id, count: item.count }))
    };

    apiRequestJson('/api/action/end-battle', 'POST', payload)
        .then(res => {
            if (res.code === 'SUCCESS' && res.data) {
                const { xp, maxExp, level } = res.data;
                user.afterXp = xp ?? user.beforeXp;
                user.maxExp = maxExp ?? 100;
                user.level = level ?? user.beforeLevel;
                updateRewardUI(user, expReward);
                setUserInfo(res.data);
            } else {
                showMessageModal("아이템 획득 처리 실패");
            }
        });
}

function generateLootItems() {
    const dropList = battleState.monster.drops || [];

    const commonList = dropList.filter(item => item.rarity?.toUpperCase() === 'COMMON');
    const bonusList = dropList.filter(item => item.rarity?.toUpperCase() !== 'COMMON');

    const loot = [];

    // 1. COMMON 확정 1개
    if (commonList.length > 0) {
        const guaranteed = commonList[Math.floor(Math.random() * commonList.length)];
        loot.push({
            ...guaranteed,
            count: Math.floor(Math.random() * 2) + 2 // 2~3개
        });
    }

    // 2. 희귀템 드랍 시도 → dropRate 통과한 후보들 필터링
    const bonusCandidates = bonusList.filter(item => {
        const chance = item.dropRate ?? 0;
        return Math.random() * 100 < chance;
    });

    // 3. 가중치 기반으로 후보 중 1개 선택
    if (bonusCandidates.length > 0) {
        const picked = pickWeightedByRarity(bonusCandidates);
        const rarity = picked.rarity?.toUpperCase();
        const isEquip = picked.itemType?.startsWith('EQUIP');

        let count = 1;
        if (!isEquip) {
            if (rarity === 'UNCOMMON') {
                count = Math.floor(Math.random() * 2) + 1; // 1~2개
            }
        }

        loot.push({ ...picked, count });
    }

    return loot;
}


function pickWeightedByRarity(items) {
    const rarityWeights = {
        COMMON: 75.0,
        UNCOMMON: 23.0,
        RARE: 1.4,
        EPIC: 0.59,
        LEGENDARY: 0.01
    };

    const weightedItems = items.map(item => {
        const rarity = item.rarity?.toUpperCase() || 'COMMON';
        const weight = rarityWeights[rarity] || 1;
        return { item, weight };
    });

    const total = weightedItems.reduce((sum, wi) => sum + wi.weight, 0);
    let random = Math.random() * total;

    for (const wi of weightedItems) {
        random -= wi.weight;
        if (random <= 0) {
            return wi.item;
        }
    }

    return weightedItems[0].item; // fallback
}

function updateLootItemsDisplay(items) {
    const lootItemsList = document.getElementById('lootItemsList');
    lootItemsList.innerHTML = '';
    items.forEach((item, index) => {
        const itemCard = document.createElement('div');
        itemCard.className = `loot-item-card ${item.rarity ? 'rarity-' + item.rarity.toLowerCase() : 'rarity-common'}`;
        const imgSrc = (typeof basePath !== 'undefined' && item.iconPath)
            ? basePath + item.iconPath
            : `placeholder_${item.iconPath || 'item.png'}`;
        itemCard.innerHTML = `
            <img src="${imgSrc}" alt="${item.name}" class="loot-item-image">
            <div class="loot-item-name">${item.name}</div>
            <div class="loot-item-count">x${item.count}</div>
        `;
        itemCard.style.opacity = '0';
        itemCard.style.transform = 'translateY(20px)';
        itemCard.style.transition = 'all 0.5s ease';
        lootItemsList.appendChild(itemCard);
        setTimeout(() => {
            itemCard.style.opacity = '1';
            itemCard.style.transform = 'translateY(0)';
        }, 500 + (index * 200));
    });
}


function applyHitEffect(targetSelector) {
    const el = document.querySelector(targetSelector);
    if (!el) return;
    el.classList.remove('hit-effect');
    void el.offsetWidth;
    el.classList.add('hit-effect');
}

function showDamageText(targetSelector, value, isHeal = false) {
    const container = document.querySelector(targetSelector);
    if (!container) return;
    const dmg = document.createElement('div');
    dmg.className = 'damage-float';
    dmg.textContent = `${isHeal ? '+' : '-'}${value}`;
    dmg.style.left = '50%';
    dmg.style.top = '0';
    dmg.style.color = isHeal ? '#2aff73' : 'red';
    container.appendChild(dmg);
    setTimeout(() => { dmg.remove(); }, 1000);
}

function updateRewardUI(user, expReward) {// 캐릭터 이미지 설정
    const charImg = document.getElementById('rewardCharacterImage');
    if (charImg) {
        if (typeof basePath_image !== 'undefined') {
            charImg.src = basePath_image + "/character/" + user.charImage;
        } else {
            charImg.src = `placeholder_${user.charImage}`;
        }
    }
    const levelEl = document.getElementById('rewardCharLevel');
    const currentXpEl = document.getElementById('currentXp');
    const maxXpEl = document.getElementById('maxXp');
    const xpGainEl = document.getElementById('xpGainText');
    const xpBarFill = document.getElementById('xpBarFill');

    if (levelEl) levelEl.textContent = user.level;
    if (currentXpEl) currentXpEl.textContent = user.afterXp;
    if (maxXpEl) maxXpEl.textContent = user.maxExp;
    if (xpGainEl) xpGainEl.textContent = `+${expReward} XP`;

    if (user.level > user.beforeLevel) {
        levelEl.textContent = user.level;
        levelEl.classList.add('level-up-highlight');
        setTimeout(() => {
            levelEl.classList.remove('level-up-highlight');
        }, 500);
    } else {
        levelEl.textContent = user.level;
    }

    if (xpBarFill) {
        let startXp = (user.level > user.beforeLevel) ? 0 : user.beforeXp;
        let startPercent = (startXp / user.maxExp) * 100;
        let endPercent = (user.afterXp / user.maxExp) * 100;

        xpBarFill.style.width = startPercent + '%';

        setTimeout(() => {
            xpBarFill.style.width = endPercent + '%';
        }, 300);
    }
}

function createRewardInfoElement() {
    const container = document.createElement('div');
    container.id = 'rewardInfo';
    container.className = 'reward-info-container';

    // lootModal 내부에 추가
    const lootModal = document.getElementById('lootModal');
    if (lootModal) {
        const lootItemsList = document.getElementById('lootItemsList');
        if (lootItemsList) {
            lootModal.insertBefore(container, lootItemsList);
        } else {
            lootModal.appendChild(container);
        }
    }

    return container;
}

function createExpBar() {
    const expBarContainer = document.createElement('div');
    expBarContainer.className = 'exp-bar-container';
    expBarContainer.innerHTML = `
        <div class="exp-bar">
            <div class="exp-fill"></div>
            <div class="exp-text">0 / 100</div>
        </div>
    `;
    return expBarContainer;
}

function updateExpBar(expBarContainer, currentExp, maxExp) {
    const expFill = expBarContainer.querySelector('.exp-fill');
    const expText = expBarContainer.querySelector('.exp-text');

    const percentage = Math.min((currentExp / maxExp) * 100, 100);

    if (expFill) {
        expFill.style.width = `${percentage}%`;
        expFill.style.transition = 'width 1s ease-in-out';
    }

    if (expText) {
        expText.textContent = `${currentExp} / ${maxExp}`;
    }
}

function nextBattle() {
    // 보상 모달만 닫기
    lootModal.classList.add('hidden');
    defeatModal.classList.add('hidden')

    // 같은 맵에서 새로운 전투 시작
    if (window.selectedMap) {
        window.startBattleFromMap(window.selectedMap);
    } else {
        // selectedMap이 없으면 맵 선택으로 돌아가기
        battleModal.classList.add('hidden');
        document.body.style.overflow = '';
        handleAttackClick();
    }
}

async function doDefend() {
    const player = battleState.player;

    // 조건: 턴 중 & 패배 아님 & 아직 안 쓴 경우 & 살아있는 경우만 가능
    if (!isPlayerTurn || isProcessingTurn || battleEnded || potionUsed || player.currentHp <= 0) return;
    const defendBtn = document.getElementById('defendBtn');
    animateButton(defendBtn);

    const {bonusHp, bonusPower, quantity} = player.potion;

    // 수량 없으면 차단 (정상 UI에선 실행 안 됨)
    if (quantity <= 0) {
        showMessageModal("물약이 없습니다!");
        return;
    }
    // ✅ 서버에 수량 감소 요청
    try {
        const res = await apiRequestJson('/api/battle/use-potion', 'POST', {});
        if (res.code !== 'SUCCESS' || typeof res.data?.quantity !== 'number') {
            showMessageModal("물약 사용 실패");
            return;
        }

        // 수량 반영
        const newQuantity = res.data.quantity;
        player.potion.quantity = newQuantity;
        potionUsed = true;

        // UI 업데이트
        const label = defendBtn.querySelector('.potion-count-label');
        if (label) label.textContent = `x${newQuantity}`;
        if (newQuantity <= 0) defendBtn.disabled = true;

        // ✅ 회복
        if (bonusHp > 0) {
            player.currentHp = Math.min(player.maxHp, player.currentHp + bonusHp);
            showDamageText('.player-container', bonusHp, true);
        }

        // ✅ 공격력 버프
        if (bonusPower > 0) {
            tempPowerBoost = bonusPower;
            player.power += bonusPower;
        }

        updateBattleUI();
    } catch (err) {
        console.error("물약 사용 오류:", err);
        showMessageModal("서버 오류로 물약을 사용할 수 없습니다.");
    }
}

function doHeal() {
    if (!isPlayerTurn || isProcessingTurn || battleEnded) return;
    showMessageModal("도망쳤습니다!");
    closeBattleModal();
}

function closeBattleModal() {
    document.getElementById('battleModal').classList.add('hidden');
    document.body.style.overflow = '';
    // ✅ 모달 닫을 때 상태 초기화
    battleEnded = false;
    isPlayerTurn = true;
    isProcessingTurn = false;
}

function showSkillEffect() {
    const monster = document.getElementById('monsterCharacter');
    const container = monster.parentElement;

    const effectImg = document.createElement('img');
    effectImg.src = skillEffectImage;
    effectImg.alt = 'Skill Effect';
    effectImg.className = 'skill-effect-image';

    container.appendChild(effectImg);

    setTimeout(() => {
        effectImg.remove();
    }, 1000);
}

function animateButton(buttonEl) {
    if (!buttonEl) return;
    buttonEl.classList.remove('battle-button-clicked'); // 중복 방지
    void buttonEl.offsetWidth; // 리플로우 강제
    buttonEl.classList.add('battle-button-clicked');
}

function disableBattleButtons(disabled) {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');
    const defendBtn = document.getElementById('defendBtn');

    if (attackBtn) attackBtn.disabled = disabled;
    if (healBtn) healBtn.disabled = disabled;
    if (defendBtn) {
        const quantity = battleState.player.potion?.quantity ?? 0;
        defendBtn.disabled = disabled || quantity <= 0 || potionUsed;
    }
}

function useFood(foodItem) {
    const powerBuff = foodItem.bonusPower || 0;

    if (powerBuff > 0) {
        // 기존 버프가 있다면 제거 (한 번에 하나만 허용)
        if (tempPowerBoost > 0) {
            battleState.player.power -= tempPowerBoost;
        }

        tempPowerBoost = powerBuff;
        battleState.player.power += powerBuff;

        updateBattleUI();
    }
}