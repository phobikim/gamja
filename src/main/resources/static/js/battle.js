// battle.js 파일에서만 유지할 로직들 — 전투 시작 및 처리 관련
const battleModal = document.getElementById('battleModal');
const lootModal = document.getElementById('lootModal');
const defeatModal = document.getElementById('defeatModal');
const monsterImage = document.getElementById('monsterCharacter');
const playerImage = document.getElementById('userCharacter');
const logBox = document.getElementById('battleLog');

let battleEnded = false;
let isPlayerTurn = true;
let isProcessingTurn = false;

window.battleState = {
    player: {},
    monster: {}
};

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

    const log = document.getElementById('battleLog');
    if (log) {
        const colorMap = {
            '야생 들판': '#00653f',
            '감자도둑쥐의 소굴': '#0d072b',
            '기본': '#f5f5f5'
        };
        const color = colorMap[window.selectedMap?.name] || colorMap['기본'];
        log.style.backgroundColor = color;
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

    document.querySelector('.user-name').textContent = user.dexName;
    document.querySelector('.user-attribute').textContent = user.attribute;
    document.querySelector('.user-power').textContent = user.power.total;
    document.querySelector('.user-hp').textContent = user.hp.total;
    document.querySelector('.user-speed').textContent = user.speed.total;

    document.querySelector('.monster-name').textContent = monster.name;
    document.querySelector('.monster-rank').textContent = monster.rank;
    document.querySelector('.monster-hp').textContent = monster.monsterHp;
    document.querySelector('.monster-power').textContent = monster.monsterPower;
    document.querySelector('.monster-xp').textContent = monster.monsterXp;
}


function logBattle(message, type = 'player') {
    const line = document.createElement('div');
    line.textContent = message;
    line.style.margin = '0.2rem 0';
    line.style.wordBreak = 'break-word';

    if (type === 'player') {
        line.style.color = '#39ff14';
        line.style.textAlign = 'left';
    } else {
        line.style.color = '#ff4d4d';
        line.style.textAlign = 'right';
    }

    logBox.appendChild(line);
    logBox.scrollTop = logBox.scrollHeight;
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
        charImg: user.charImage
    };
    battleState.monster = {
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
    logBox.innerHTML = '';
    battleEnded = false;
    isPlayerTurn = true;
    isProcessingTurn = false;
    resetBattleState(user, monster);
    updateBattleUI();
    updateButtonStates();
    updateCardTurnStyles();
    logBattle('🔥 Fight !!', 'player');
}

function updateBattleUI() {
    document.querySelector('.user-hp').textContent = battleState.player.currentHp;
    document.querySelector('.monster-hp').textContent = battleState.monster.currentHp;
}

function updateCardTurnStyles() {
    const playerCard = document.querySelector('.user-stats');
    const monsterCard = document.querySelector('.monster-stats');

    playerCard.classList.remove('active-turn', 'inactive-turn');
    monsterCard.classList.remove('active-turn', 'inactive-turn');

    if (battleEnded) return;
    if (isPlayerTurn) {
        playerCard.classList.add('active-turn');
        monsterCard.classList.add('inactive-turn');
    } else {
        monsterCard.classList.add('active-turn');
        playerCard.classList.add('inactive-turn');
    }
}

function updateButtonStates() {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');
    const canAct = isPlayerTurn && !isProcessingTurn && !battleEnded;
    if (attackBtn) attackBtn.disabled = !canAct;
    if (healBtn) healBtn.disabled = !canAct;
    updateCardTurnStyles();
}

function doAttack() {
    if (!isPlayerTurn || isProcessingTurn || battleEnded) return;
    playEffect("se_attack");
    isProcessingTurn = true;
    updateButtonStates();

    const damage = battleState.player.power;
    battleState.monster.currentHp -= damage;
    applyHitEffect('.monster-character');
    showDamageText('.monster-container', damage);
    logBattle(`플레이어의 공격! ${damage}의 피해`, 'player');
    updateBattleUI();

    if (battleState.monster.currentHp <= 0) return winBattle();

    isPlayerTurn = false;
    setTimeout(() => monsterTurn(), 500);
}

function monsterTurn() {
    if (isPlayerTurn || battleEnded) return;
    const damage = Math.floor(Math.random() * battleState.monster.power) + 1;
    battleState.player.currentHp -= damage;
    applyHitEffect('.player-character');
    showDamageText('.player-container', damage);
    logBattle(`몬스터의 공격! ${damage}의 피해`, 'monster');

    setTimeout(() => {
        updateBattleUI();
        if (battleState.player.currentHp <= 0) return showDefeatModal("여기에 다시 묻히다...");
        isPlayerTurn = true;
        isProcessingTurn = false;
        updateButtonStates();
    }, 400);
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
        items: items.map(item => ({ itemId: item.id, count: item.count }))
    };

    apiRequestJson('/api/action/endBattle', 'POST', payload)
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
    const lootCount = Math.random() < 0.3 ? 2 : 1;
    const selected = pickWeightedRandomItems(dropList, lootCount);
    return selected.map(item => {
        let count = 1;
        if (item.itemType !== 'EQUIP_BATTLE' && item.itemType !== 'EQUIP_GATHER') {
            if (item.rarity?.toUpperCase() === 'COMMON') {
                count = Math.floor(Math.random() * 2) + 2;
            }
        }
        return { ...item, count };
    });
}

function pickWeightedRandomItems(items, count) {
    if (!items || items.length === 0) return [];

    const result = [];
    const pool = [...items];

    const rarityWeights = {
        'COMMON': 80, 'UNCOMMON': 15, 'RARE': 5, 'EPIC': 2, 'LEGENDARY': 1
    };

    for (let i = 0; i < count && pool.length > 0; i++) {
        // 가중치 계산
        const weightedItems = pool.map(item => ({
            item,
            weight: rarityWeights[item.rarity?.toUpperCase()] || 1
        }));

        const totalWeight = weightedItems.reduce((sum, wi) => sum + wi.weight, 0);
        let random = Math.random() * totalWeight;

        // 아이템 선택
        for (let j = 0; j < weightedItems.length; j++) {
            random -= weightedItems[j].weight;
            if (random <= 0) {
                const selectedItem = weightedItems[j].item;
                result.push(selectedItem);
                // 선택된 아이템을 풀에서 제거
                const poolIndex = pool.indexOf(selectedItem);
                if (poolIndex > -1) pool.splice(poolIndex, 1);
                break;
            }
        }
    }

    return result;
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

function showDamageText(targetSelector, damage) {
    const container = document.querySelector(targetSelector);
    if (!container) return;
    const dmg = document.createElement('div');
    dmg.className = 'damage-float';
    dmg.textContent = `-${damage}`;
    dmg.style.left = '50%';
    dmg.style.top = '0';
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

function doDefend() {
    const player = document.querySelector(".player-container");
    const effect = document.createElement("img");
    effect.className = "heal-frame-effect";
    effect.src = "https://phobi.me/gamja.img/images/effect/1.png"; // 초기 프레임
    player.appendChild(effect);
    const framePaths = [
        "https://phobi.me/gamja.img/images/effect/1.png",
        "https://phobi.me/gamja.img/images/effect/2.png",
        "https://phobi.me/gamja.img/images/effect/3.png",
        "https://phobi.me/gamja.img/images/effect/4.png"
    ];
    let index = 0;
    const interval = setInterval(() => {
        index++;
        if (index >= framePaths.length) {
            clearInterval(interval);
            effect.remove();
            return;
        }
        effect.src = framePaths[index];
    }, 200);
}

function doHeal() {
    // ✅ 턴 검증
    if (!isPlayerTurn || isProcessingTurn || battleEnded) {
        return;
    }

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
