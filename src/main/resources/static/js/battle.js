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
let tempPowerBoostUsed = false;

let currentDexImage = null;
const defaultSkillEffectImage = `${basePath_image}/skills/attack_slash.png`;

// 배틀 시작
window.startBattleFromMap = async function(map) {
    const valid = await checkSessionValid();
    if (!valid) return;

    battleModal.classList.remove('hidden');
    showBattleSkeleton();
    window.selectedMap = map;

    const res = await apiRequestJson('/api/battle/start-battle', 'POST', {
        mapId: map.id
    });

    if (res.code !== 'SUCCESS') {
        showMessageModal(res.message || "전투 시작에 실패했습니다.");
        battleModal.classList.add('hidden');
        document.body.style.overflow = 'hidden'; // 스크롤 잠금 유지
        battleMapSelectModal.classList.remove('hidden');
        return;
    }

    const { player, monster } = res.data;

    // 캐릭터 스킬
    if (player.skillImagePath) {
        window.skillEffectImage = basePath_image + player.skillImagePath;
    } else {
        window.skillEffectImage = defaultSkillEffectImage;
    }

    initializeBattleScene(player, monster);
    renderBattleState(player, monster);
    removeBattleSkeleton();

    isPlayerTurn = true;
    isProcessingTurn = false;
    battleEnded = false;
    updateButtonStates();
};

function initializeBattleScene(player, monster) {
    const background = document.getElementById('battleBackground');
    if (background && window.selectedMap?.imagePath) {
        background.src = basePath + window.selectedMap.imagePath;
    }

    const mapNameBanner = document.getElementById('battleMapName');
    if (mapNameBanner) {
        mapNameBanner.textContent = window.selectedMap?.name || '전투 지역';
    }

    const userImage = document.getElementById('userCharacter');
    const monsterImg = document.getElementById('monsterCharacter');
    const effectElement = document.getElementById('monsterEffect');
    const container = document.getElementById('monsterEffectContainer');

    const charImagePath = basePath_image + "/character/" + player.charImage;
    userImage.src = charImagePath;
    userImage.alt = player.dexName;

    monsterImg.src = basePath + monster.imagePath;
    monsterImg.alt = monster.name;

    // 애니메이션 초기화 + 적용
    userImage.classList.remove('jump-in', 'fade-out', 'hit-effect');
    monsterImg.classList.remove('jump-in', 'fade-out', 'hit-effect');
    void userImage.offsetWidth;
    void monsterImg.offsetWidth;
    userImage.classList.add('jump-in');
    monsterImg.classList.add('jump-in');

    // 이름/속성 설정
    document.getElementById('playerNameLabel').textContent = player.dexName;
    document.getElementById('playerAttrLabel').textContent = player.attribute;
    document.getElementById('monsterNameLabel').textContent = monster.name;
    document.getElementById('monsterAttrLabel').textContent = monster.rank;

    // 랭크 이펙트 적용
    if (effectElement && container) {
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
        container.classList.remove('hidden');
        effectElement.classList.add('pop');
        setTimeout(() => {
            container.classList.add('hidden');
            effectElement.classList.remove('pop');
        }, 800);
    }

    window.currentDexImage = charImagePath;
    waitForAllBattleImagesToLoad(() => {
        removeBattleSkeleton();
        isPlayerTurn = true;
        isProcessingTurn = false;
        battleEnded = false;
        requestAnimationFrame(() => {
            updateButtonStates();
        });
        setTimeout(() => {
            if (!document.getElementById('attackBtn')?.disabled && isPlayerTurn && !isProcessingTurn && !battleEnded) return;
            console.warn('[Fallback] 버튼 상태 재설정 실행됨');
            updateButtonStates();
        }, 2000);
    });
}

function renderBattleState(player, monster) {
    updateHpBar(player.hp, player.maxHp, "playerHpBar", "playerHpText");
    updateHpBar(monster.hp, monster.maxHp, "monsterHpBar", "monsterHpText");

    // 공격력 UI
    window.basePlayerPower = player.power;

    const atkDisplay = document.getElementById('attackPowerDisplay');
    if (atkDisplay) {
        atkDisplay.textContent = `ATK: ${player.power}${tempPowerBoost > 0 ? ` +${tempPowerBoost}` : ''}`;
    }

    // 포션 버튼
    const potionBtn = document.getElementById('potionBtn');
    const potion = player.potion;
    if (potionBtn && potion?.itemPath) {
        potionBtn.innerHTML = `
            <div class="potion-btn-wrapper">
                <img src="${basePath}${potion.itemPath}" alt="물약" class="potion-btn-image">
                <div class="potion-count-label">x${potion.quantity}</div>
            </div>`;
        window.currentPotionQuantity = potion.quantity;
        potionBtn.disabled = potion.quantity <= 0;
    } else {
        window.currentPotionQuantity = 0;
    }

    updateButtonStates();
}


async function doAttack() {
    const valid = await checkSessionValid();
    if (!valid) return;

    if (!isPlayerTurn || isProcessingTurn || battleEnded) return;

    isProcessingTurn = true;
    disableBattleButtons(true);

    playEffect("se_attack");
    animateButton(document.getElementById('attackBtn'));

    try {
        // 플레이어 공격
        const res = await apiRequestJson('/api/battle/player-attack', 'POST');
        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "플레이어 공격 실패");
            isProcessingTurn = false;
            return;
        }

        const {playerAttack, monster, victory} = res.data;

        // 플레이어 공격 animation
        showSkillEffect();
        applyHitEffect('.monster-character');
        showDamageText('.monster-container', playerAttack.damage);
        if (playerAttack.isCritical) showCriticalText();

        updateHpBar(monster.hp, monster.maxHp, "monsterHpBar", "monsterHpText");

        tempPowerBoost = 0;

        const atkDisplay = document.getElementById('attackPowerDisplay');
        if (atkDisplay) {
            atkDisplay.textContent = `ATK: ${window.basePlayerPower}`;
        }

        if (victory) {
            battleEnded = true;
            monsterImage.classList.remove('jump-in', 'hit-effect', 'fade-out');
            void monsterImage.offsetWidth;
            monsterImage.classList.add('fade-out');

            setTimeout(() => winBattle(), 600);
            return;
        }

        // 몬스터 반격 (딜레이 후 처리)
        setTimeout(async () => {
            const res2 = await apiRequestJson('/api/battle/monster-attack', 'POST');
            if (res2.code !== 'SUCCESS') {
                showMessageModal(res2.message || "몬스터 반격 실패");
                isProcessingTurn = false;
                return;
            }

            const {player, monsterAttack, defeat} = res2.data;

            // 몬스터 반격
            applyHitEffect('.player-character');
            showDamageText('.player-container', monsterAttack.damage);

            updateHpBar(player.hp, player.maxHp, "playerHpBar", "playerHpText");

            if (defeat) {
                battleEnded = true;
                setTimeout(() => showDefeatModal("여기에 다시 묻히다..."), 600);
            } else {
                // 다음 턴 활성화
                isPlayerTurn = true;
                isProcessingTurn = false;
                updateButtonStates();
            }
        }, 600);

        // 버프 1턴 유지 → 공격 후 초기화
        tempPowerBoost = 0;

    } catch (err) {
        console.error("공격 오류:", err);
        showMessageModal("공격 도중 오류 발생");
        isProcessingTurn = false;
    }
}

async function doPotion() {
    const valid = await checkSessionValid();
    if (!valid) return;

    if (!isPlayerTurn || isProcessingTurn || battleEnded || potionUsed) return;

    const potionBtn = document.getElementById('potionBtn');
    animateButton(potionBtn);

    try {
        const res = await apiRequestJson('/api/battle/use-potion', 'POST');
        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "물약 사용 실패");
            return;
        }

        const { playerHp, maxHp, bonusHp, bonusPower, quantity } = res.data;

        // ✅ 수량 업데이트
        window.currentPotionQuantity = quantity;
        potionUsed = true;

        // ✅ 포션 버튼 UI 반영
        const label = potionBtn.querySelector('.potion-count-label');
        if (label) label.textContent = `x${quantity}`;
        if (quantity <= 0) potionBtn.disabled = true;

        // ✅ 회복 이펙트
        if (bonusHp > 0) {
            updateHpBar(playerHp, maxHp, "playerHpBar", "playerHpText");
            showDamageText('.player-container', bonusHp, true);
        }

        // ✅ 공격력 버프 (단 1회만)
        if (bonusPower > 0 && !tempPowerBoostUsed) {
            tempPowerBoost = bonusPower;
            tempPowerBoostUsed = true;
            const atkDisplay = document.getElementById('attackPowerDisplay');
            if (atkDisplay) {
                atkDisplay.textContent += ` +${bonusPower}`;
            }
        }
        updateButtonStates();

    } catch (err) {
        console.error("물약 사용 오류:", err);
        showMessageModal("서버 오류로 물약을 사용할 수 없습니다.");
    }
}


async function winBattle() {
    try {
        const res = await apiRequestJson('/api/battle/end-battle', 'POST');
        if (res.code !== 'SUCCESS') {
            showMessageModal("전투 보상 처리 실패");
            return;
        }

        const {
            dexName, charImage,
            beforeLevel, afterLevel,
            beforeXp, afterXp,
            maxExp, gainedXp,
            items
        } = res.data;


        // UI 반영
        updateRewardUI(res.data);
        updateLootItemsDisplay(items);
        loadCharacterBasicInfo();

        lootModal.classList.remove('hidden');
        document.getElementById('rewardStage').style.display = 'block';

    } catch (err) {
        console.error("전투 보상 처리 실패:", err);
        showMessageModal("서버 오류로 보상을 처리하지 못했습니다.");
    }
}


function updateLootItemsDisplay(items) {
    const lootItemsList = document.getElementById('lootItemsList');
    lootItemsList.innerHTML = '';
    items.forEach((item, index) => {
        const rarityClass = item.rarity ? 'rarity-' + item.rarity.toLowerCase() : 'rarity-common';
        const chronicleClass = item.chronicle ? 'chronicle-item' : '';
        const itemCard = document.createElement('div');
        itemCard.className = `loot-item-card ${rarityClass} ${chronicleClass}`;

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
        }, 300 + (index * 80));
    });
}

async function doRun() {
    const valid = await checkSessionValid();
    if (!valid) return;

    if (!isPlayerTurn || isProcessingTurn || battleEnded) return;
    await apiRequestJson('/api/battle/end-battle', 'POST');

    closeBattleModal();
}


// 버튼 상태 완전 초기화
function resetBattleButtons() {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');
    const potionBtn = document.getElementById('potionBtn');

    if (attackBtn) attackBtn.disabled = true;
    if (healBtn) healBtn.disabled = true;
    if (potionBtn) potionBtn.disabled = true;

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


function showCriticalText() {
    const container = document.querySelector('.monster-container');
    if (!container) return;

    const crit = document.createElement('div');
    crit.className = 'critical-hit-text';
    crit.textContent = 'CRITICAL!';
    container.appendChild(crit);

    setTimeout(() => { crit.remove(); }, 1000);
}


function updateButtonStates() {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');
    if (!attackBtn || !healBtn) {
        console.warn('[Retry] 버튼 DOM 아직 생성 안 됨. 재시도');
        setTimeout(updateButtonStates, 100); // 재귀 1회
        return;
    }
    const canAct = isPlayerTurn && !isProcessingTurn && !battleEnded;
    disableBattleButtons(!canAct);
}


async function showDefeatModal(message) {
    await apiRequestJson('/api/battle/end-battle', 'POST'); // 세션 정리
    defeatModal.classList.remove('hidden');
    document.getElementById('defeatSignText').innerText = message || "여기에 다시 묻히다...";
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

function updateRewardUI({
                            dexName, charImage,
                            beforeLevel, afterLevel,
                            beforeXp, afterXp,
                            maxExp, gainedXp
                        }) {

    const charImg = document.getElementById('rewardCharacterImage');
    const levelEl = document.getElementById('rewardCharLevel');
    const currentXpEl = document.getElementById('currentXp');
    const maxXpEl = document.getElementById('maxXp');
    const xpGainEl = document.getElementById('xpGainText');
    const xpBarFill = document.getElementById('xpBarFill');

    if (charImg && charImage) {
        charImg.src = basePath_image + "/character/" + charImage;
        charImg.alt = dexName;
    }

    if (levelEl) levelEl.textContent = afterLevel;
    if (currentXpEl) currentXpEl.textContent = afterXp;
    if (maxXpEl) maxXpEl.textContent = maxExp;
    if (xpGainEl) xpGainEl.textContent = `+${gainedXp} XP`;

    if (afterLevel > beforeLevel) {
        levelEl.classList.add('level-up-highlight');
        setTimeout(() => levelEl.classList.remove('level-up-highlight'), 500);
    }

    if (xpBarFill) {
        const startXp = afterLevel > beforeLevel ? 0 : beforeXp;
        const startPercent = (startXp / maxExp) * 100;
        const endPercent = (afterXp / maxExp) * 100;

        xpBarFill.style.width = startPercent + '%';
        setTimeout(() => {
            xpBarFill.style.width = endPercent + '%';
        }, 300);
    }
}


async function nextBattle() {
    const valid = await checkSessionValid();
    if (!valid) return;
    // 기존 전투 데이터 초기화
    resetModalStyles(lootModal);
    resetModalStyles(defeatModal);
    resetModalStyles(battleModal);
    // 드랍 영역 초기화
    clearLootItemsDisplay();

    battleEnded = false;
    isProcessingTurn = false;
    isPlayerTurn = true;
    tempPowerBoost = 0;
    tempPowerBoostUsed = false;
    potionUsed = false;
    resetBattleButtons();

    // 같은 맵에서 새로운 전투 시작
    if (window.selectedMap) {
        await window.startBattleFromMap(window.selectedMap);
        updateButtonStates();
    } else {
        // selectedMap이 없으면 맵 선택으로 돌아가기
        battleModal.classList.add('hidden');
        document.body.style.overflow = '';
        await handleAttackClick();
    }
}

function resetModalStyles(modalEl) {
    modalEl.classList.add('hidden');
    modalEl.style.display = '';
    modalEl.style.pointerEvents = '';
    modalEl.style.opacity = '';
    modalEl.style.zIndex = '';
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
    const potionBtn = document.getElementById('potionBtn');
    const btnList = [attackBtn, healBtn, potionBtn];

    btnList.forEach(btn => {
        if (!btn) return;
        btn.disabled = disabled || false;
        btn.classList.toggle('disabled', btn.disabled);
    });

    if (potionBtn) {
        const quantity = window.currentPotionQuantity ?? 0;
        potionBtn.disabled = disabled || quantity <= 0 || potionUsed;
        potionBtn.classList.toggle('disabled', potionBtn.disabled);
    }
}

function handleBattleClose() {
    // 전투 모달 닫기
    document.getElementById('battleModal')?.classList.add('hidden');
    document.getElementById('lootModal')?.classList.add('hidden');
    document.getElementById('defeatModal')?.classList.add('hidden');
    document.getElementById('battleMapSelectModal')?.classList.remove('hidden');
    // 드랍 영역 초기화
    clearLootItemsDisplay();
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
function clearLootItemsDisplay() {
    const lootItemsList = document.getElementById('lootItemsList');
    if (lootItemsList) lootItemsList.innerHTML = '';
}