function updateWinBox() {
    lootModal.classList.remove('hidden');

    // const chestStage = document.getElementById('chestStage');
    const rewardStage = document.getElementById('rewardStage');
    if (rewardStage) rewardStage.style.display = 'block';
    showRewardResults(); // 보상 표시

    // setTimeout(() => {
    //     // if (chestStage) chestStage.style.display = 'none';
    //     if (rewardStage) rewardStage.style.display = 'block';
    //     showRewardResults(); // 보상 표시
    // }, 500);
}

function showRewardResults() {
    const currentUser = getCurrentBattleUser();
    console.log("1. user 정보: ", currentUser);

    const expReward = battleState.monster.exp || 0;
    const dropItems = generateLootItems();

    updateCharacterReward(currentUser, expReward);
    updateLootItemsDisplay(dropItems);
}

function getCurrentBattleUser() {
    return {
        name: battleState.player.name || "테스트 유저",
        level: battleState.player.level || 10,
        maxXp: battleState.player.maxXp || 200,
        currentXp: battleState.player.xp,
        charImage: battleState.player.charImg || "test.png"
    };
}

function generateLootItems() {
    const dropList = battleState.monster.drops || [];
    const lootCount = Math.random() < 0.3 ? 2 : 1; // 30% 확률로 2개
    const selectedItems = [];

    for (let i = 0; i < lootCount && i < dropList.length; i++) {
        const randomItem = dropList[Math.floor(Math.random() * dropList.length)];
        const count = Math.floor(Math.random() * 3) + 1; // 1~3개

        selectedItems.push({
            ...randomItem,
            count: count
        });
    }

    return selectedItems;
}

function updateCharacterReward(user, expReward) {
    // 캐릭터 이미지 설정
    const charImg = document.getElementById('rewardCharacterImage');
    if (charImg) {
        if (typeof basePath_image !== 'undefined') {
            charImg.src = basePath_image + "/character/" + user.charImage;
        } else {
            charImg.src = `placeholder_${user.charImage}`;
        }
    }

    // 레벨 표시
    const levelEl = document.getElementById('rewardCharLevel');
    if (levelEl) levelEl.textContent = user.level;

    // XP 정보 설정
    const currentXpEl = document.getElementById('currentXp');
    const maxXpEl = document.getElementById('maxXp');
    const xpGainEl = document.getElementById('xpGainText');
    const xpBarFill = document.getElementById('xpBarFill');

    if (currentXpEl) currentXpEl.textContent = user.currentXp;
    if (maxXpEl) maxXpEl.textContent = user.maxXp;
    if (xpGainEl) xpGainEl.textContent = `+${expReward} XP`;

    // XP 바 애니메이션
    if (xpBarFill) {
        const currentPercent = (user.currentXp / user.maxXp) * 100;
        const newXp = Math.min(user.currentXp + expReward, user.maxXp);
        const newPercent = (newXp / user.maxXp) * 100;

        xpBarFill.style.width = currentPercent + '%';

        setTimeout(() => {
            xpBarFill.style.width = newPercent + '%';
        }, 300);
    }
}

function updateLootItemsDisplay(items) {
    const chestImage = document.getElementById('chestImage');
    const lootItemsList = document.getElementById('lootItemsList');
    lootItemsList.innerHTML = '';
    chestImage.classList.add('opening');

    if (!lootItemsList) return;



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

        lootList.appendChild(itemCard);

        // 순차 애니메이션
        setTimeout(() => {
            itemCard.style.transition = 'all 0.5s ease';
            itemCard.style.opacity = '1';
            itemCard.style.transform = 'translateY(0)';
        }, 500 + (index * 200));
    });
}

function nextBattle() {
    lootModal.classList.add('hidden');

    // 새로운 전투 시작
    setTimeout(() => {
        loadNewBattle();
    }, 300);
}


// ========== 새로운 전투 로딩 ==========
function loadNewBattle() {
    // 실제 서버 API 호출 (기존 코드 구조 유지)
    if (typeof apiRequestJson === 'function' && typeof userId !== 'undefined') {
        handleAttackClick(); // 기존 함수 재사용
    }
}

