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

let currentDexImage = null;

const defaultSkillEffectImage = `${basePath_image}/skills/attack_slash.png`;

window.battleState = {
    player: {},
    monster: {}
};



window.startBattleFromMap = async function(map) {
    battleModal.classList.remove('hidden');
    showBattleSkeleton();
    // [1] 유저 정보
    const userRes = await apiRequest('/api/battle/user-stat', 'GET');
    if (userRes.code !== 'SUCCESS') {
        showMessageModal(userRes.message || "유저 정보를 불러오지 못했습니다.");
        return;
    }
    const user = userRes.data;

    // [2] 유저 스킬 정보 (속성 기준으로 BASIC 스킬 조회)
    const attribute = user.attribute; // 예: '구운', '튀김', '껍질' 등
    const skillRes = await apiRequest(`/api/battle/${attribute}?type=BASIC`, 'GET');
    if (skillRes.code !== 'SUCCESS') {
        showMessageModal(skillRes.message || "스킬 정보를 불러오지 못했습니다.");
        return;
    }
    const basicSkills = skillRes.data;
    if (basicSkills.length > 0 && basicSkills[0].images.length > 0) {
        window.skillEffectImage = basePath_image + basicSkills[0].images[0];
    } else {
        window.skillEffectImage = defaultSkillEffectImage;
    }

    // [3] 몬스터 정보
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

    let selectedMonster;
    selectedMonster = monsters[Math.floor(Math.random() * monsters.length)];

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
    currentDexImage = basePath_image + "/character/" + user.charImage;
    userImage.src = currentDexImage;
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
    waitForAllBattleImagesToLoad(removeBattleSkeleton);

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

function getPlayerPower() {
    const base = battleState.player.basePower;
    const buff = tempPowerBoost;
    const rawPower = base + buff;

    const isCritical = Math.random() < 0.3; //30% 확률
    if (isCritical) {
        showCriticalText(); // 크리 표시용 텍스트 (아래 따로 정의)
        return Math.floor(rawPower * 1.5);
    }

    return rawPower;
}

function showCriticalText() {
    const container = document.querySelector('.monster-container');
    if (!container) return;

    const crit = document.createElement('div');
    crit.className = 'critical-hit-text';
    crit.textContent = 'CRITICAL!';
    container.appendChild(crit);

    setTimeout(() => { crit.remove(); }, 1000);
}

function resetBattleState(user, monster) {
    battleState.player = {
        dexName: user.dexName,
        attribute: user.attribute,
        maxHp: user.hp || 10,
        currentHp: user.hp || 10,
        basePower: user.power || 5,
        speed: user.speed || 5,
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
    battleEnded = false;
    isProcessingTurn = false;
    isPlayerTurn = true;
    tempPowerBoost = 0;

    resetBattleState(user, monster);
    updateBattleUI();
    requestAnimationFrame(() => {
        updateButtonStates();  // 렌더 후 안전하게 버튼 상태 업데이트
    });

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

    const atkBase = p.basePower;
    const atkDisplay = document.getElementById('attackPowerDisplay');
    if (atkDisplay) {
        if (tempPowerBoost > 0) {
            atkDisplay.textContent = `ATK: ${atkBase} +${tempPowerBoost}`;
        } else {
            atkDisplay.textContent = `ATK: ${atkBase}`;
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

    const damage = getPlayerPower();
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
        tempPowerBoost = 0;
        battleState.player.power = getPlayerPower();
    }

    isProcessingTurn = true;

    // 최소 데미지를 **몬스터 공격력의 20% 로 설정
    const minRatio = 0.2;
    const ratio = minRatio + Math.random() * (1 - minRatio);
    const damage = Math.max(1, Math.floor(battleState.monster.power * ratio));
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
        items: items.map(item => ({ itemId: item.itemId, count: item.count }))
    };

    apiRequestJson('/api/action/end-battle', 'POST', payload)
        .then(res => {
            if (res.code === 'SUCCESS' && res.data) {
                const { xp, maxExp, level } = res.data;
                user.afterXp = xp;
                user.maxExp = maxExp;
                user.level = level;
                updateRewardUI(user, expReward);
                loadCharacterBasicInfo();
            } else {
                showMessageModal("아이템 획득 처리 실패");
            }
        });
}

function generateLootItems() {
    const dropList = battleState.monster.drops || [];
    const loot = [];

    dropList.forEach(drop => {
        const chance = drop.dropRate ?? 0;
        const roll = Math.random() * 100;

        if (roll <= chance) {
            const min = drop.minCount ?? 1;
            const max = drop.maxCount ?? min;
            const count = Math.floor(Math.random() * (max - min + 1)) + min;

            loot.push({
                ...drop,
                count
            });
        }
    });

    return loot;
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
        }, 300 + (index * 80)); // 약 0.08초 간격
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
    charImg.src = currentDexImage
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
    resetModalStyles(lootModal);
    resetModalStyles(defeatModal);
    resetModalStyles(battleModal);

    battleEnded = false;
    isProcessingTurn = false;
    isPlayerTurn = true;

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

function resetModalStyles(modalEl) {
    modalEl.classList.add('hidden');
    modalEl.style.display = '';
    modalEl.style.pointerEvents = '';
    modalEl.style.opacity = '';
    modalEl.style.zIndex = '';
}

async function doPotion() {
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
        }

        updateBattleUI();
    } catch (err) {
        console.error("물약 사용 오류:", err);
        showMessageModal("서버 오류로 물약을 사용할 수 없습니다.");
    }
}

function doRun() {
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
    effectImg.src = window.skillEffectImage;
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

function handleBattleClose() {
    // 전투 모달 닫기
    document.getElementById('battleModal')?.classList.add('hidden');
    document.getElementById('lootModal')?.classList.add('hidden');
    document.getElementById('defeatModal')?.classList.add('hidden');
    document.getElementById('battleMapSelectModal')?.classList.remove('hidden');
}

function showBattleSkeleton() {
    document.getElementById('userCharacterSkeleton')?.classList.add('skeleton');
    document.getElementById('monsterCharacterSkeleton')?.classList.add('skeleton');
    document.getElementById('battleBackgroundSkeleton')?.classList.add('skeleton');

    document.getElementById('userCharacter').style.display = 'none';
    document.getElementById('monsterCharacter').style.display = 'none';
    document.getElementById('battleBackground').style.display = 'none';
}

function waitForAllBattleImagesToLoad(callback) {
    let loaded = 0;
    const total = 3;

    function checkDone() {
        loaded++;
        if (loaded >= total && typeof callback === 'function') callback();
    }

    const userImage = document.getElementById('userCharacter');
    const monsterImage = document.getElementById('monsterCharacter');
    const background = document.getElementById('battleBackground');

    if (userImage?.complete) {
        checkDone();
    } else {
        userImage.onload = checkDone;
    }

    if (monsterImage?.complete) {
        checkDone();
    } else {
        monsterImage.onload = checkDone;
    }

    if (background?.complete) {
        checkDone();
    } else {
        background.onload = checkDone;
    }
}

function removeBattleSkeleton() {
    // 1. 스켈레톤 대체 div에서 .skeleton 클래스 제거
    const skeletonDivIds = [
        'userCharacterSkeleton',
        'monsterCharacterSkeleton',
        'battleBackgroundSkeleton'
    ];
    skeletonDivIds.forEach(id => {
        document.getElementById(id)?.classList.remove('skeleton');
    });

    // 2. 실제 이미지 요소 다시 표시 (img 태그)
    const imageIds = [
        'userCharacter',
        'monsterCharacter',
        'battleBackground'
    ];
    imageIds.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = '';
    });

    // 3. 내부 텍스트 요소에서 skeleton 클래스 제거
    const skeletonTextIds = [
        'userCharacter', 'monsterCharacter', 'battleBackground',
        'playerHpText', 'monsterHpText',
        'playerNameLabel', 'playerAttrLabel',
        'monsterNameLabel', 'monsterAttrLabel',
        'attackPowerDisplay'
    ];
    skeletonTextIds.forEach(id => {
        document.getElementById(id)?.classList.remove('skeleton');
    });

    // 4. char-label wrapper에서 skeleton 제거
    document.querySelectorAll('.char-label.skeleton').forEach(el => {
        el.classList.remove('skeleton');
    });
}
