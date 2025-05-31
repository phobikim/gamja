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
    const lootCount = Math.random() < 0.3 ? 2 : 1; // 드랍할 아이템 수
    const selectedItems = [];

    // 드랍 후보 중에서 무작위로 고르기
    const candidates = [...dropList];

    for (let i = 0; i < lootCount && candidates.length > 0; i++) {
        const randomIndex = Math.floor(Math.random() * candidates.length);
        const item = candidates.splice(randomIndex, 1)[0]; // 중복 방지 위해 제거

        let count = 1;
        switch (item.rarity?.toUpperCase()) {
            case 'COMMON':
                count = Math.floor(Math.random() * 2) + 2; // 2~3
                break;
            case 'UNCOMMON':
                count = Math.floor(Math.random() * 2) + 1; // 1~2
                break;
            case 'RARE':
                count = 1; // 고정
                break;
            default:
                count = 1; // 그 외는 기본 1
        }

        selectedItems.push({
            ...item,
            count: count
        });
    }

    return selectedItems;
}


function updateCharacterReward(user, expReward, items) {
    const payload = {
        exp:expReward,
        items: items.map(item => ({
            itemId: item.id,
            count: item.count
        }))
    };

    apiRequestJson('/api/action/endBattle', 'POST', payload)
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
    defeatModal.classList.add('hidden');
    lootModal.classList.add('hidden');
    loadNewBattle();
}


// ========== 새로운 전투 로딩 ==========
function loadNewBattle() {
    // 실제 서버 API 호출 (기존 코드 구조 유지)
    if (typeof apiRequestJson === 'function' && typeof userId !== 'undefined') {
        handleAttackClick();
    }
}


