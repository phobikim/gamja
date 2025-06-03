// 🌟 전역 상태
let hp = 3;
let stage = 1;
const maxStage = 10;
let cardEventPool = [];      // 서버에서 불러온 카드 이벤트
let gainedItems = [];        // 누적 보상 아이템
let totalExp = 0;            // 누적 경험치
const explorationModal = document.getElementById('explorationModal');

// 🌟 탐사 시작
async function openExploration(activityType, rank) {
    hp = 3;
    stage = 1;
    totalExp = 0;
    gainedItems = [];

    document.getElementById('hp').textContent = hp;
    document.getElementById('stage').textContent = stage;
    document.getElementById('log').innerHTML = '';
    explorationModal.classList.remove('hidden');
    explorationModal.classList.add('show');

    try {
        const url = `/api/action/card-event?activity=${activityType}&rank=${rank}`;
        const cardRes = await apiRequest(url, 'GET');

        if (cardRes.code !== 'SUCCESS') {
            closeExploration();
            showMessageModal(cardRes.message || '카드 목록을 불러오는 데 실패했습니다.');
            return;
        }

        cardEventPool = cardRes.data;
        renderCards();
    } catch (e) {
        closeExploration();
        showMessageModal('서버 오류가 발생했습니다.');
        console.error(e);
    }
}

function renderCards() {
    const leftCard = document.getElementById('leftCard');
    const rightCard = document.getElementById('rightCard');

    if (cardEventPool.length < 2) return;

    let leftIndex = Math.floor(Math.random() * cardEventPool.length);
    let rightIndex;
    do {
        rightIndex = Math.floor(Math.random() * cardEventPool.length);
    } while (rightIndex === leftIndex);

    const left = cardEventPool[leftIndex];
    const right = cardEventPool[rightIndex];

    leftCard.textContent = '⬅️ ' + left.cardText;
    rightCard.textContent = '➡️ ' + right.cardText;
    leftCard.dataset.event = JSON.stringify(left);
    rightCard.dataset.event = JSON.stringify(right);

    restoreCardListeners();
}

function restoreCardListeners() {
    document.getElementById('leftCard').onclick = () => choosePath('left');
    document.getElementById('rightCard').onclick = () => choosePath('right');
}

function choosePath(direction) {
    if (hp <= 0 || stage > maxStage) return;

    const log = document.getElementById('log');
    const selectedCard = document.getElementById(direction === 'left' ? 'leftCard' : 'rightCard');
    const event = JSON.parse(selectedCard.dataset.event);

    const logEntry = document.createElement('div');
    logEntry.textContent = (direction === 'left' ? '⬅️' : '➡️') + ' ' + event.eventMessage;
    log.prepend(logEntry);

    if (event.eventType === 'TRAP') {
        hp += event.hpChange || 0;
    }
    document.getElementById('hp').textContent = hp;

    if (Array.isArray(event.drops)) {
        event.drops.forEach(drop => {
            const chance = Math.random();
            if (chance <= drop.dropRate) {
                const qty = Math.floor(Math.random() * (drop.maxQuantity - drop.minQuantity + 1)) + drop.minQuantity;
                const existing = gainedItems.find(i => i.itemId === drop.itemId);
                if (existing) {
                    existing.count += qty;
                } else {
                    gainedItems.push({ itemId: drop.itemId, count: qty, itemName: drop.itemName, itemImg: drop.iconPath });
                }
                const dropLog = document.createElement('div');
                dropLog.textContent = `🎁 ${drop.itemName} x${qty} 획득!`;
                dropLog.style.color = '#ffd700';
                log.prepend(dropLog);
            }
        });
    }

    if (hp <= 0) {
        totalExp = getStageExp(stage);
        log.prepend(createLogLine('💀 체력을 모두 잃고 쓰러졌습니다...', '#f55'));
        sendExplorationResult(false);
        return;
    }

    stage++;
    document.getElementById('stage').textContent = Math.min(stage, maxStage);

    if (stage > maxStage) {
        totalExp = getStageExp(stage);
        log.prepend(createLogLine('🎉 10단계 완료!', '#9f9'));
        sendExplorationResult(true);
        return;
    }

    renderCards();
}

function getStageExp(stage) {
    if (stage <= 3) return 10;
    if (stage <= 7) return 20;
    if (stage <= 9) return 30;
    return 40;
}

function sendExplorationResult(success) {
    const payload = {
        activityType: activityType,
        exp: totalExp,
        items: gainedItems
    };

    closeExploration();

    apiRequestJson('/api/action/end-exploration', 'POST', payload)
        .then(res => {
            if (res.code === 'SUCCESS') {
                const skillInfo = res.data;

                // ✅ 탐사 결과 모달 표시
                showExplorationResultModal(success, totalExp, gainedItems);

                // ✅ 유저 스킬 업데이트 전역 갱신
                updateUserSkillInfo(skillInfo); // EXP bar 등 업데이트
                // ✅ spotList도 다시 렌더링
                if (lastSpotList.length > 0) {
                    renderSpotList(lastSpotList, skillInfo.level, skillInfo.xp);
                }
            } else {
                showMessageModal(res.message || '탐사 보상 저장 실패');
            }
        })
        .catch(e => {
            console.error('탐사 결과 저장 실패', e);
            showMessageModal('서버 오류로 탐사 보상을 저장하지 못했습니다.');
        });
}

function showExplorationResultModal(success, exp, items) {
    const explorationResultModal = document.getElementById('explorationResultModal');
    const header = document.getElementById('explorationResultHeader');
    const expEl = document.getElementById('resultExp');
    const itemList = document.getElementById('resultItemList');

    header.textContent = '탐사 완료';
    expEl.textContent = `+${exp} XP`;
    itemList.innerHTML = '';

    items.forEach(i => {
        const row = document.createElement('div');
        row.innerHTML = `<img src="${basePath}/${i.itemImg}" alt="${i.itemName}" /> <span>${i.itemName} x${i.count}</span>`;
        itemList.appendChild(row);
    });

    explorationResultModal.classList.remove('hidden');
    explorationResultModal.classList.add('show');
}

document.getElementById('closeResultBtn').onclick = () => {
    closeExploration();
};

function updateUserSkillInfo(userInfo) {
    const expPercent = Math.min(100, (userInfo.xp / userInfo.maxExp) * 100).toFixed(1);
    document.getElementById('userSkillLevel').textContent = `스킬 레벨: Lv.${userInfo.level}`;
    document.getElementById('userSkillExpFill').style.width = `${expPercent}%`;
    document.getElementById('userSkillExpText').textContent = `${userInfo.xp} / ${userInfo.maxExp} EXP`;
}

function closeExploration() {
    document.getElementById('explorationResultModal').classList.add('hidden');
    document.getElementById('explorationResultModal').classList.remove('show');
    explorationModal.classList.add('hidden');
    explorationModal.classList.remove('show');

    // ✅ spotSelectModal 다시 보이게
    document.getElementById('spotSelectModal').classList.remove('hidden');
}

function createLogLine(text, color) {
    const div = document.createElement('div');
    div.textContent = text;
    div.style.color = color;
    div.style.fontWeight = 'bold';
    return div;
}
