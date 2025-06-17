

const questModal = document.getElementById('questModal');
const questTabBtns = document.querySelectorAll('.quest-tab-btn');
const questListContainer = document.getElementById('questList');

function handleQuestClick() {
    playEffect("se_click2");
    questModal.classList.remove('hidden');
    getQuestList();
}

questModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.quest-modal-container');
    if (!inside) questModal.classList.add('hidden');
});

function closeQuestModal() {
    questModal?.classList.add('hidden');
}

async function getQuestList() {
    try {
        const res = await apiRequest(`/api/quest/list`, 'GET');
        if (res.code !== 'SUCCESS' || !res.data) {
            showMessageModal('퀘스트 리스트를 불러오지 못했습니다.');
            return;
        }
        renderQuestTabs(res.data);
        renderQuestList(res.data, 'MAIN');
    } catch (err) {
        console.error(err);
        closeQuestModal();
        showMessageModal('퀘스트 정보를 불러오지 못했습니다.');
    }
}

function renderQuestTabs(questList) {
    questTabBtns.forEach(btn => {
        btn.removeEventListener('click', btn.clickHandler);
        btn.clickHandler = () => {
            questTabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const selectedType = btn.dataset.type;
            renderQuestList(questList, selectedType);
        };
        btn.addEventListener('click', btn.clickHandler);
    });
}

function renderQuestList(list, type) {
    questListContainer.innerHTML = '';
    const filtered = list.filter(q => q.type === type);
    if (filtered.length === 0) {
        questListContainer.innerHTML = '<div style="text-align: center; padding: 40px; color: #ccc;">해당 타입의 퀘스트가 없습니다.</div>';
        return;
    }
    filtered.forEach(quest => {
        const wrapper = document.createElement('div');
        wrapper.className = 'quest-entry';

        const title = document.createElement('div');
        title.className = 'quest-title';
        title.textContent = quest.name;

        const desc = document.createElement('div');
        desc.className = 'quest-desc';
        desc.textContent = quest.description;

        const conditionGroup = document.createElement('div');
        conditionGroup.className = 'quest-conditions';

        (quest.conditions || []).forEach(cond => {
            const row = document.createElement('div');
            row.className = 'quest-condition-row';

            const label = document.createElement('span');
            label.className = 'condition-label';
            label.textContent = getConditionLabel(cond);

            const bar = document.createElement('div');
            bar.className = 'quest-progress-bar';

            const fill = document.createElement('div');
            fill.className = 'quest-progress-fill';
            const percentage = Math.min((cond.currentCount / cond.requiredCount) * 100, 100);
            fill.style.width = `${percentage}%`;

            const text = document.createElement('div');
            text.className = 'quest-progress-text';
            text.textContent = `${cond.currentCount}/${cond.requiredCount}`;

            bar.appendChild(fill);
            row.appendChild(label);
            row.appendChild(bar);
            row.appendChild(text);
            conditionGroup.appendChild(row);
        });

        const actionArea = document.createElement('div');
        actionArea.className = 'quest-action';

        const statusArea = document.createElement('div');
        statusArea.className = 'quest-status';
        statusArea.style.flexGrow = 1;

        const rewardsArea = document.createElement('div');
        rewardsArea.className = 'quest-rewards';

        (quest.rewards || []).forEach(r => {
            const rewardEl = document.createElement('span');
            rewardEl.className = 'quest-reward-item';
            rewardEl.textContent = getRewardText(r);
            rewardsArea.appendChild(rewardEl);
        });

        statusArea.appendChild(rewardsArea);
        const buttonWrapper = document.createElement('div');
        if (quest.achieved) {
            const btn = document.createElement('button');
            btn.className = 'quest-claim-btn';
            btn.textContent = '보상 받기';
            btn.onclick = () => completeQuest(quest.id);
            buttonWrapper.appendChild(btn);
        } else {
            const progress = document.createElement('span');
            progress.className = 'quest-in-progress';
            progress.textContent = '진행중';
            buttonWrapper.appendChild(progress);
        }
        actionArea.appendChild(statusArea);
        actionArea.appendChild(buttonWrapper);

        wrapper.appendChild(title);
        wrapper.appendChild(desc);
        wrapper.appendChild(conditionGroup);
        wrapper.appendChild(actionArea);

        questListContainer.appendChild(wrapper);
    });
}

function getConditionLabel(cond) {
    switch (cond.counterType) {
        case 'CHARACTER_DRAW': return '동료 모집';
        case 'MONSTER_KILL': return `몬스터 처치 (${cond.targetName})`;
        case 'ITEM_CRAFT': return `아이템 제작 (${cond.targetName})`;
        case 'LIFE_ACTION': return `${cond.targetName}`;
        case 'EQUIP_ITEM' : return `아이템 장착 (${cond.targetName})`;
        case 'EQUIP_TITLE' : return `칭호 장착 (${cond.targetName})`;
        case 'DELIVER_ITEM' : return `아이템 배달 (${cond.targetName})`;
        default: return '기타 조건';
    }
}

async function completeQuest(questId) {
    try {
        const res = await apiRequestJson('/api/quest/complete-quest', 'POST', { questId });
        if (res.code === 'SUCCESS') {
            showMessageModal('보상을 받았습니다!');
            getQuestList();
            await loadCharacterBasicInfo?.(); // 메인 상태 갱신
        } else {
            showMessageModal(res.message || '보상 처리 실패');
        }
    } catch (err) {
        console.error(err);
        showMessageModal('서버 오류로 보상을 받지 못했습니다.');
    }
}

function getRewardText(reward) {
    switch (reward.rewardType) {
        case 'ITEM': return `${reward.itemName} x ${reward.amount} `;
        case 'EXP': return `경험치 +${reward.amount}`;
        case 'COIN': return `코인 +${reward.amount}`;
        default: return `보상 +${reward.amount}`;
    }
}