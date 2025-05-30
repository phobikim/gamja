let user = {
    beforeXp: 0,
    afterXp: 0,
    maxExp: 0,
    beforeLevel:0,
    level: 0,
};

function updateWinBox() {
    lootModal.classList.remove('hidden');

    // const chestStage = document.getElementById('chestStage');
    const rewardStage = document.getElementById('rewardStage');
    if (rewardStage) rewardStage.style.display = 'block';
    showRewardResults(); // 보상 표시

}

function showRewardResults() {
    const currentUser = getCurrentBattleUser();

    const expReward = battleState.monster.exp || 0;
    const dropItems = generateLootItems();
    updateCharacterReward(currentUser, expReward, dropItems);
    updateLootItemsDisplay(dropItems);

}

function getCurrentBattleUser() {
    return {
        beforeXp: battleState.player.currentXp, // 승리 전 경험치
        beforeLevel : battleState.player.lv, // 승리 전 레벨
        charImage: battleState.player.charImg

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

function updateCharacterReward(user, expReward, items) {
    const payload = {
        activityType: "ATTACK",
        exp:expReward,
        items: items.map(item => ({
            itemId: item.id,
            count: item.count
        }))
    };

    apiRequestJson('/api/action/addItems', 'POST', payload)
        .then(res => {
            if (res.code === 'SUCCESS') {
                user.afterXp = res.data.xp; //승리 후 경험치
                user.maxExp = res.data.maxExp;
                user.level = res.data.level;
                updateRewardUI(user, expReward)
                setUserInfo(res.data);
            } else {
                showMessageModal("아이템 획득 처리 실패");
            }
        });

}

function updateRewardUI(user, expReward) {
    console.log("레벨", user.beforeLevel, user.level);

    // 캐릭터 이미지 설정
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

function updateLootItemsDisplay(items) {
    const lootItemsList = document.getElementById('lootItemsList');
    lootItemsList.innerHTML = '';

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
        // 초기 애니메이션 상태 설정
        itemCard.style.opacity = '0';
        itemCard.style.transform = 'translateY(20px)';
        itemCard.style.transition = 'all 0.5s ease';

        lootItemsList.appendChild(itemCard);

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

