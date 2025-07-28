
const explorationModal = document.getElementById('explorationModal');
let isExplorationEnded = false;
let gainedItems = [];

// 탐사 시작
async function openExploration(activityType, rank) {
    isExplorationEnded = false;
    gainedItems = [];

    document.getElementById('hp').textContent = '3';
    document.getElementById('stage').textContent = '1';
    document.getElementById('logMessages').innerHTML = '';
    const logPrompt = document.getElementById('logPrompt');
    if (logPrompt) {
        logPrompt.textContent = '카드를 선택하세요!';
        logPrompt.classList.remove('burning-log');
    }
    explorationModal.classList.remove('hidden');
    explorationModal.classList.add('show');

    try {
        const url = `/api/action/card-event?activity=${activityType}&rank=${rank}`;
        const res = await apiRequest(url, 'GET');

        if (res.code !== 'SUCCESS') return showMessageModal(res.message);
        const { hp, stage, currentChoices } = res.data;

        updateExplorationStatus(hp, stage);
        renderCards(currentChoices);
    } catch (e) {
        showMessageModal('서버 오류');
    }
}

function updateExplorationStatus(hp, stage) {
    document.getElementById('hp').textContent = hp;
    document.getElementById('stage').textContent = stage;
    const logPrompt = document.getElementById('logPrompt');
    if (logPrompt) {
        if (stage >= 20) {
            logPrompt.textContent = '감자단 속보: 현재 보상 세배 임계치 도달';
            logPrompt.classList.add('burning-log');
        } else if (stage >= 10) {
            logPrompt.textContent = '감자단 보고서: 이 구역, 보상 두배 현상 발생 중';
            logPrompt.classList.remove('burning-log');
        } else {
            logPrompt.textContent = '감자단 규칙 1조: 감으로 고른다';
            logPrompt.classList.remove('burning-log');
        }
    }
}


function renderCards(cardList) {
    const leftCard = document.getElementById('leftCard');
    const rightCard = document.getElementById('rightCard');
    leftCard.textContent = '';
    rightCard.textContent = '';

    if (!cardList || cardList.length < 2) {
        showMessageModal('다음 카드가 부족합니다.');
        return;
    }

    const [left, right] = cardList;

    leftCard.textContent = left.cardText;
    rightCard.textContent = right.cardText;
    leftCard.dataset.event = JSON.stringify(left);
    rightCard.dataset.event = JSON.stringify(right);

    restoreCardListeners();
}

function restoreCardListeners() {
    document.getElementById('leftCard').onclick = () => choosePath('left');
    document.getElementById('rightCard').onclick = () => choosePath('right');
}

async function choosePath(direction) {
    if (isExplorationEnded) return;

    const selected = JSON.parse(document.getElementById(direction === 'left' ? 'leftCard' : 'rightCard').dataset.event);

    logCardEvent(selected);

    const res = await apiRequestJson('/api/action/resolve-card', 'POST', {
        eventId: selected.id,
        activityType: currentActivityType
    });
    const result = res.data;
    if (!result) {
        showMessageModal(res.message || '카드 처리 실패');
        return;
    }
    if (result.itemId) {
        const { itemId, count, itemName, iconPath, multiplier } = result;
        gainedItems.push({ itemId, count, itemName, itemImg: iconPath });

        const log = document.createElement('div');
        log.classList.add('exploration-log-entry', 'log-resource');
        log.innerHTML = `<img src="${basePath}${iconPath}" alt="${itemName}"> ${itemName} x${count} 획득!`;
        document.getElementById('logMessages').prepend(log);
    }

    if (result.isEnd) {
        isExplorationEnded = true;
        sendExplorationResult();
        return;
    }
    updateExplorationStatus(result.hp, result.stage);
    renderCards(result.nextChoices);
}

function logCardEvent(event) {
    const logBox = document.getElementById('logMessages');
    const entry = document.createElement('div');
    entry.textContent = event.eventMessage;
    entry.classList.add('exploration-log-entry');
    if (event.eventType === 'TRAP') entry.classList.add('log-trap');
    else if (event.eventType === 'RESOURCE') entry.classList.add('log-resource');
    logBox.prepend(entry);
}

function sendExplorationResult() {
    apiRequestJson('/api/action/end-exploration', 'POST', {
        activityType: currentActivityType
    }).then(res => {
        if (res.code === 'SUCCESS') {
            const skillInfo = res.data;

            // 서버에서 내려준 stage, gainedExp, items 사용
            const stage = skillInfo.stage;
            const exp = skillInfo.gainedExp;
            const items = skillInfo.items || [];

            showExplorationResultModal(stage, exp, items);
            updateUserSkillInfo(skillInfo);
            actionGather(currentActivityType);
        } else {
            showMessageModal(res.message);
        }
    }).catch(() => {
        showMessageModal('서버 오류');
    });

    closeExploration();
}


function showExplorationResultModal(stage, exp, items) {
    const modal = document.getElementById('explorationResultModal');
    const expEl = document.getElementById('resultExp');
    const itemList = document.getElementById('resultItemList');

    expEl.textContent = `+${exp} XP`;
    itemList.innerHTML = '';

    items.forEach(i => {
        const row = document.createElement('div');
        row.innerHTML = `<img src="${basePath}/${i.iconPath}" alt="${i.itemName}" /> <span>${i.itemName} x${i.count}</span>`;
        itemList.appendChild(row);
    });

    const oldBtn = document.getElementById('closeResultBtn');
    const newBtn = oldBtn.cloneNode(true);
    oldBtn.replaceWith(newBtn);

    newBtn.onclick = () => {
        closeExploration();
    };

    modal.classList.remove('hidden');
    modal.classList.add('show');

    setTimeout(() => {
        const btn = document.getElementById('closeResultBtn');
        if (!btn || typeof btn.onclick !== 'function') {
            console.warn('[Fallback] 탐험 결과 닫기 버튼 초기화 재시도');
            const retryBtn = btn ? btn.cloneNode(true) : document.createElement('button');
            retryBtn.id = 'closeResultBtn';
            retryBtn.textContent = '닫기';
            retryBtn.onclick = () => {
                closeExploration();
            };
            if (btn) {
                btn.replaceWith(retryBtn);
            } else {
                modal.appendChild(retryBtn);
            }
        }
    }, 1500); // 1.5초 뒤에 확인
}

document.getElementById('closeResultBtn').onclick = () => {
    closeExploration();
};

function updateUserSkillInfo(userInfo) {
    const expPercent = Math.min(100, (userInfo.xp / userInfo.maxExp) * 100).toFixed(1);

    const levelEl = document.getElementById('userSkillLevel');
    const barEl = document.getElementById('userSkillExpFill');
    const textEl = document.getElementById('userSkillExpText');

    if (levelEl) levelEl.textContent = `스킬 레벨: Lv.${userInfo.level}`;
    if (barEl) barEl.style.width = `${expPercent}%`;
    if (textEl) textEl.textContent = `${userInfo.xp} / ${userInfo.maxExp} EXP`;
}

function closeExploration() {
    document.getElementById('explorationResultModal').classList.add('hidden');
    document.getElementById('explorationResultModal').classList.remove('show');
    explorationModal.classList.add('hidden');
    explorationModal.classList.remove('show');

    //  spotSelectModal 다시 보이게
    document.getElementById('spotSelectModal').classList.remove('hidden');
}

function createLogLine(text, color) {
    const div = document.createElement('div');
    div.textContent = text;
    div.style.color = color;
    div.style.fontWeight = 'bold';
    return div;
}

function handleManualExplorationEnd() {
    if (isExplorationEnded) return;
    isExplorationEnded = true;

    sendExplorationResult();
}