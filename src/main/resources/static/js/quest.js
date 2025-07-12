

const questModal = document.getElementById('questModal');
const questTabBtns = document.querySelectorAll('.quest-tab-btn');
const questListContainer = document.getElementById('questList');
let currentQuestType = 'MAIN';
let currentSubType = null;
const CHRONICLE_MAPS = {
    1: '야생들판',
    2: '고산지대',
    3: '도둑쥐소굴'
};

function getSubFilterOptions(type) {
    switch (type) {
        case 'DAILY':
            return ['REQUEST', 'HUNT'];
        case 'CHRONICLE':
            return Object.keys(CHRONICLE_MAPS);
        default:
            return [];
    }
}

function getSubFilterLabel(sub) {
    if (currentQuestType === 'CHRONICLE') {
        return CHRONICLE_MAPS[sub] || sub;
    }

    switch (sub) {
        case 'HUNT': return '토벌';
        case 'REQUEST': return '납품';
        default: return sub;
    }
}

async function handleQuestClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    playEffect("se_click2");
    questModal.classList.remove('hidden');

    const filterWrapper = document.getElementById('difficultyFilter');
    filterWrapper.innerHTML = '';

    // ✅ API 호출 경로 분기
    const endpoint = currentQuestType === 'CHRONICLE' ? '/api/quest/chronicle/list' : '/api/quest/list';
    const res = await apiRequest(endpoint, 'GET');
    if (res.code !== 'SUCCESS' || !res.data) {
        showMessageModal('퀘스트 리스트를 불러오지 못했습니다.');
        return;
    }

    renderQuestTabs(res.data); // 탭 바인딩
    if (currentQuestType === 'MAIN') {
        renderQuestList(res.data, 'MAIN');
    } else {
        setupSubFilter(currentQuestType); // 자동 클릭 포함
    }

    // ✅ 탭 UI 상태 반영
    questTabBtns.forEach(b => b.classList.remove('active'));
    const activeBtn = [...questTabBtns].find(btn => btn.dataset.type === currentQuestType);
    activeBtn?.classList.add('active');
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
            cache: 'no-store'
            return;
        }
        renderQuestTabs(res.data);
        renderQuestList(res.data, currentQuestType);
    } catch (err) {
        console.error(err);
        closeQuestModal();
        showMessageModal('퀘스트 정보를 불러오지 못했습니다.');
    }
}

function setupSubFilter(type) {
    const filterWrapper = document.getElementById('difficultyFilter');
    filterWrapper.innerHTML = '';

    const subOptions = getSubFilterOptions(type);
    if (!subOptions.length) {
        filterWrapper.classList.add('hidden');
        return;
    }

    filterWrapper.classList.remove('hidden');

    subOptions.forEach((sub, idx) => {
        const btn = document.createElement('button');
        btn.className = 'difficulty-btn';
        btn.dataset.sub = sub;
        btn.textContent = getSubFilterLabel(sub);
        btn.onclick = async () => {
            document.querySelectorAll('.difficulty-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentSubType = sub;
            if (currentQuestType === 'CHRONICLE') {
                renderChronicleSummary(currentSubType); // ✅ 여기에 추가
            }
            // ✅ API 호출 경로 분기
            const endpoint = currentQuestType === 'CHRONICLE' ? '/api/quest/chronicle/list' : '/api/quest/list';
            const res = await apiRequest(endpoint, 'GET');
            if (res.code === 'SUCCESS' && res.data) {
                renderQuestList(res.data, currentQuestType);
            }
        };
        filterWrapper.appendChild(btn);

        // ✅ 첫 버튼은 자동 클릭 (탭 클릭 시)
        if (idx === 0) btn.click();
    });

    currentSubType = subOptions[0]; // 자동 선택 상태 설정

    if (type !== 'CHRONICLE') {
        const summaryContainer = document.getElementById('chronicleSummaryBarContainer');
        summaryContainer.innerHTML = '';
    }
}
function renderQuestTabs() {
    questTabBtns.forEach(btn => {
        btn.removeEventListener('click', btn.clickHandler);
        btn.clickHandler = async () => {
            questTabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentQuestType = btn.dataset.type;

            const diffFilter = document.getElementById('difficultyFilter');
            const summaryContainer = document.getElementById('chronicleSummaryBarContainer');
            summaryContainer.innerHTML = '';

            if (currentQuestType === 'MAIN') {
                diffFilter.classList.add('hidden');
                await getQuestList(); // ✅ MAIN은 즉시 API 호출
            } else {
                setupSubFilter(currentQuestType); // ✅ DAILY/CHRONICLE은 하위탭에서 호출
            }
        };
        btn.addEventListener('click', btn.clickHandler);
    });
}

function renderQuestList(list, type) {
    questListContainer.innerHTML = '';

    let filtered = [];

    if (currentQuestType === 'MAIN') {
        filtered = list.filter(q => q.type === 'MAIN');
    } else if (currentQuestType === 'DAILY') {
        filtered = list.filter(q => !q.chronicleFlag && q.type === currentSubType);
    } else if (currentQuestType === 'CHRONICLE') {
        filtered = list.filter(q => String(q.mapGroupId) === String(currentSubType))
    }
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

        // 난이도 라벨 붙이기
        if (quest.difficulty) {
            const label = document.createElement('span');
            label.className = `quest-difficulty-label ${quest.difficulty.toLowerCase()}`;
            label.textContent = getDifficultyText(quest.difficulty);
            title.appendChild(label);
        }

        const desc = document.createElement('div');
        desc.className = 'quest-desc';
        desc.textContent = quest.description;

        const conditionText = document.createElement('div');
        conditionText.className = 'quest-conditionText';
        conditionText.textContent = quest.conditionText || '';

        const conditionGroup = document.createElement('div');
        conditionGroup.className = 'quest-conditions';

        (quest.conditions || []).forEach(cond => {
            const row = document.createElement('div');
            row.className = 'quest-condition-row';

            const label = document.createElement('span');
            label.className = 'condition-label';
            label.innerHTML = getConditionLabel(cond);

            const bar = document.createElement('div');
            bar.className = 'quest-progress-bar';
            bar.style.position = 'relative';

            const fill = document.createElement('div');
            fill.className = 'quest-progress-fill';

            // ✅ 기준 값 분기
            let effectiveCount = cond.currentCount;

            if (cond.counterType === 'DELIVER_ITEM') {
                if (quest.allowPartialDelivery) {
                    effectiveCount = cond.currentCount; // 연대기 전용: 저장된 납품 진행도 기준
                } else {
                    effectiveCount = cond.deliverableCount; // 일반 퀘스트: 가방 보유량 기준
                }
            }

            const percentage = Math.min((effectiveCount / cond.requiredCount) * 100, 100);
            fill.style.width = `${percentage}%`;
            bar.appendChild(fill);

            // ✅ ghost-fill (조건부)
            const showGhost = (
                quest.allowPartialDelivery &&
                currentQuestType === 'CHRONICLE' &&
                cond.counterType === 'DELIVER_ITEM' &&
                cond.deliverableCount > 0
            );

            if (showGhost) {
                const ghost = document.createElement('div');
                ghost.className = 'quest-progress-ghost-fill';
                const ghostPercentage = Math.min(((cond.currentCount + cond.deliverableCount) / cond.requiredCount) * 100, 100);
                ghost.style.width = `${ghostPercentage}%`;
                bar.appendChild(ghost);
            }

            const text = document.createElement('div');
            text.className = 'quest-progress-text';
            let effectiveTextCount = cond.currentCount;

            if (cond.counterType === 'DELIVER_ITEM') {
                if (quest.allowPartialDelivery) {
                    effectiveTextCount = cond.currentCount + cond.deliverableCount;
                } else {
                    effectiveTextCount = cond.deliverableCount;
                }
            }

            text.textContent = `${effectiveTextCount}/${cond.requiredCount}`;

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
            // ✅ 이미 완료된 퀘스트 → 보상 받기 버튼 노출
            const isChronicle = currentQuestType === 'CHRONICLE';
            const btn = createButton(
                '보상 받기',
                'quest-claim-btn',
                () => isChronicle
                    ? completeChronicleQuest(quest.id)
                    : completeQuest(quest.id)
            );
            buttonWrapper.appendChild(btn);

        } else if (
            currentQuestType === 'CHRONICLE' &&
            !quest.allowPartialDelivery &&
            (quest.conditions || []).every(cond =>
                cond.counterType === 'DELIVER_ITEM' &&
                cond.deliverableCount >= cond.requiredCount
            )
        ) {
            // ✅ 연대기 퀘스트인데 등록 불가, 그리고 가방에 충분히 있음 → 즉시 완료 가능 → 보상 받기
            const btn = createButton('보상 받기', 'quest-claim-btn', () => completeChronicleQuest(quest.id));
            buttonWrapper.appendChild(btn);

        } else if (
            currentQuestType === 'CHRONICLE' &&
            quest.allowPartialDelivery &&
            !quest.repeated &&
            !quest.achieved &&
            (quest.conditions || []).every(cond =>
                cond.counterType === 'DELIVER_ITEM' &&
                (cond.currentCount + cond.deliverableCount) >= cond.requiredCount
            )
        ) {
            // ✅ 등록 가능한 연대기 퀘스트인데, 현재 수량 + 가방 수량 합치면 완료 가능함 → 보상 받기
            const btn = createButton('보상 받기', 'quest-claim-btn', () => completeChronicleQuest(quest.id));
            buttonWrapper.appendChild(btn);

        } else if (
            currentQuestType === 'CHRONICLE' &&
            quest.allowPartialDelivery &&
            (quest.conditions || []).some(cond =>
                cond.counterType === 'DELIVER_ITEM' &&
                cond.deliverableCount >= 1 &&
                (cond.currentCount + cond.deliverableCount) < cond.requiredCount
            )
        ) {
            // ⏳ 연대기 퀘스트이고 등록 가능 + 아직 전부는 못 채웠지만 일부 납품 가능 → 등록 버튼
            const btn = createButton('등록', 'quest-partial-submit-btn', () => progressChronicleQuest(quest.id));
            buttonWrapper.appendChild(btn);

        } else {
            // 🕐 그 외 모든 경우 → 퀘스트 진행중 (버튼 없음, 텍스트만 표시)
            const progress = document.createElement('span');
            progress.className = 'quest-in-progress';
            progress.textContent = '진행중';
            buttonWrapper.appendChild(progress);
        }

        actionArea.appendChild(statusArea);
        actionArea.appendChild(buttonWrapper);

        wrapper.appendChild(title);
        wrapper.appendChild(desc);
        if (quest.conditionText) {
            const container = document.createElement('div');
            container.className = 'quest-conditionText';

            const label = document.createElement('div');
            label.className = 'quest-conditionText-badge';
            label.textContent = '수행 방법';

            const text = document.createElement('div');
            text.className = 'quest-conditionText-body';
            text.textContent = quest.conditionText;

            container.appendChild(label);
            container.appendChild(text);
            wrapper.appendChild(container);
        }
        wrapper.appendChild(conditionGroup);
        wrapper.appendChild(actionArea);

        questListContainer.appendChild(wrapper);
    });
}

function getConditionLabel(cond) {
    const name = cond.targetName;
    const nameSpan = `<span class="condition-target">${name}</span>`; // 색상 적용용

    switch (cond.counterType) {
        case 'CHARACTER_DRAW':
            return `[동료 모집]<br>${nameSpan}`;
        case 'MONSTER_KILL':
            return `[몬스터 처치]<br>${nameSpan}`;
        case 'ITEM_CRAFT':
            return `[아이템 제작]<br>${nameSpan}`;
        case 'LIFE_ACTION':
            return `${name}`; // ✅ 예외: 한 줄
        case 'EQUIP_ITEM':
            return `[아이템 장착]<br>${nameSpan}`;
        case 'EQUIP_TITLE':
            return `[칭호 장착]<br>${nameSpan}`;
        case 'DELIVER_ITEM':
            return `[아이템 배달]<br>${nameSpan}`;
        default:
            return '기타 조건';
    }
}


async function completeQuest(questId) {
    try {
        const res = await apiRequestJson('/api/quest/complete-quest', 'POST', { questId });
        if (res.code === 'SUCCESS') {
            const random = res.data?.rewards?.find(r => r.rewardType === 'RANDOM_ITEM');
            if (random) {
                showMessageModal(`${random.message}`);
            } else {
                showMessageModal('보상을 받았습니다!');
            }
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
        case 'RANDOM_ITEM': return `운에 따라 ${reward.itemName} x1 획득 가능`;
        default: return `보상 +${reward.amount}`;
    }
}

function getDifficultyText(grade) {
    switch (grade) {
        case 'EASY': return '쉬움';
        case 'NORMAL': return '중간';
        case 'HARD': return '어려움';
        default: return '';
    }
}

async function progressChronicleQuest(questId) {
    try {
        const res = await apiRequestJson('/api/quest/chronicle/progress-quest', 'POST', { questId });
        if (res.code === 'SUCCESS') {
            showMessageModal('연대기에 무사히 등록했어요!');

            const endpoint = currentQuestType === 'CHRONICLE' ? '/api/quest/chronicle/list' : '/api/quest/list';
            const res2 = await apiRequest(endpoint, 'GET');
            if (res2.code === 'SUCCESS' && res2.data) {
                renderQuestList(res2.data, currentQuestType);
                await renderChronicleSummary(currentSubType);
            }

        } else {
            showMessageModal(res.message || '등록 처리 실패');
        }
    } catch (err) {
        console.error(err);
        showMessageModal('서버 오류로 등록 처리에 실패했습니다.');
    }
}

async function completeChronicleQuest(questId) {
    try {
        const res = await apiRequestJson('/api/quest/chronicle/complete-quest', 'POST', { questId });
        if (res.code === 'SUCCESS') {
            const random = res.data?.rewards?.find(r => r.rewardType === 'RANDOM_ITEM');
            if (random) {
                showMessageModal(`${random.message}`);
            } else {
                showMessageModal('보상을 받았습니다!');
            }
            const endpoint = currentQuestType === 'CHRONICLE' ? '/api/quest/chronicle/list' : '/api/quest/list';
            const res2 = await apiRequest(endpoint, 'GET');
            if (res2.code === 'SUCCESS' && res2.data) {
                renderQuestList(res2.data, currentQuestType);
                await renderChronicleSummary(currentSubType);
            }
        } else {
            showMessageModal(res.message || '보상 처리 실패');
        }
    } catch (err) {
        console.error(err);
        showMessageModal('서버 오류로 보상을 받지 못했습니다.');
    }
}

async function renderChronicleSummary(mapId) {
    mapId = Number(mapId);
    const container = document.getElementById('chronicleSummaryBarContainer');
    container.innerHTML = ''; // 초기화

    const res = await apiRequest(`/api/chronicle/progress?mapId=${mapId}`, 'GET');
    if (res.code !== 'SUCCESS' || !res.data?.summary) return;

    const summary = res.data.summary;
    const percent = summary.totalPercent ?? 0;
    const isCompleted = summary.completed === true;
    const isReady = !isCompleted && percent >= 100;


    // 전체 wrapper
    const wrapper = document.createElement('div');
    wrapper.className = 'quest-chronicle-summary-bar';

    // 텍스트 라벨 + 아이콘
    const label = document.createElement('div');
    label.className = 'quest-chronicle-summary-text';

    const icon = document.createElement('img');
    icon.src = 'https://phobi.me/gamja.img/images/icons/chronicle_book.png';
    icon.alt = 'progress';
    icon.className = 'quest-chronicle-summary-icon';

    const mapName = CHRONICLE_MAPS[mapId] || '???';
    const spanText = document.createElement('span');
    spanText.textContent = `감자 연대기 [${mapName}] 진행률`;

    label.appendChild(icon);
    label.appendChild(spanText);

    // 진행률 바
    const bar = document.createElement('div');
    bar.className = 'quest-chronicle-progress-bar';

    const fill = document.createElement('div');
    fill.className = 'quest-chronicle-progress-fill';
    fill.style.width = `${Math.min(percent, 100)}%`;

    const textSpan = document.createElement('span');
    textSpan.className = 'quest-chronicle-progress-text';
    textSpan.textContent = `${percent.toFixed(1)}%`;
    wrapper.classList.add(
        isCompleted ? 'done' : isReady ? 'ready' : 'progress'
    );
    if (isCompleted) {
        fill.style.display = 'none';
        textSpan.textContent = `감자 연대기 [${mapName}] 완료됨`;
        textSpan.style.color = '#9cffb2';
    } else if (isReady) {
        fill.style.display = 'none';
        textSpan.textContent = `[${mapName}] 탐험 뱃지 수령 가능`;
        textSpan.style.color = '#ffd54f';
    } else {
        fill.style.width = `${Math.min(percent, 100)}%`;
        textSpan.textContent = `${percent.toFixed(1)}%`;
    }

    bar.appendChild(fill);
    bar.appendChild(textSpan);

    // 최종 조립
    wrapper.appendChild(label);
    wrapper.appendChild(bar);
    container.appendChild(wrapper);

    container.onclick = () => {
        openChronicleModal(mapId);
    };
}

function createButton(text, className, onClick) {
    const btn = document.createElement('button');
    btn.className = className;
    btn.textContent = text;
    btn.onclick = onClick;
    return btn;
}