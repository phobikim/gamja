const chronicleModal = document.getElementById('chronicleModal');
const closeChronicleBtn = document.getElementById('closeChronicleBtn');
let currentChronicleMapId = null;
let currentChronicleMapName = '';
async function openChronicleModal(mapId) {
    battleMapSelectModal.classList.add('hidden');
    chronicleModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    const content = document.querySelector('#chronicleModal .chronicle-modal-content');
    if (content) content.scrollTop = 0;
    await fetchChronicleList(mapId);  // 연대기 API 호출 및 렌더링
}


async function fetchChronicleList(mapId) {
    try {
        currentChronicleMapId = mapId;
        const res = await apiRequest(`/api/chronicle/list?mapId=${mapId}`, 'GET');
        if (!res.data) {
            showMessageModal("앗! 이 연대기는 아직 먼지 털고 있는 중이에요… 조금만 기다려줘요!");
            closeChronicleModal();  // 모달 닫기 추가
            return;
        }
        if (res.code === 'SUCCESS') {
            currentChronicleMapName = res.data.mapName;
            renderChronicleList(res.data);
        } else {
            showMessageModal(res.message || '연대기 항목을 불러오지 못했습니다.');
        }
    } catch (err) {
        console.error(err);
        showMessageModal('서버 오류가 발생했습니다.');
    }
}

function renderChronicleList(data) {
    const collectList = document.getElementById('chronicleCollectList');
    const cookList = document.getElementById('chronicleCookList');
    const monsterList = document.getElementById('chronicleMonsterList');

    const collectDesc = document.getElementById('collectDescBoard');
    const cookDesc = document.getElementById('foodDescBoard');
    const monsterDesc = document.getElementById('monsterDescBoard');

    const collectFill = document.getElementById('collectProgressFill');
    const collectText = document.getElementById('collectProgressText');
    const foodFill = document.getElementById('foodProgressFill');
    const foodText = document.getElementById('foodProgressText');
    const monsterFill = document.getElementById('monsterProgressFill');
    const monsterText = document.getElementById('monsterProgressText');



    // 초기화
    collectList.innerHTML = '';
    cookList.innerHTML = '';
    monsterList.innerHTML = '';
    collectDesc.textContent = '';
    cookDesc.textContent = '';
    monsterDesc.textContent = '';

    const summary = data.summary;
    const list = data.list;

    updateChronicleProgressUI(summary);
    // 세부 진행률 바
    summary.details.forEach(detail => {
        const percent = Math.floor(detail.percent);
        if (detail.type === 'DROP') {
            collectFill.style.width = `${percent}%`;
            collectText.textContent = `진행률 ${percent}%`;
        } else if (detail.type === 'FOOD') {
            foodFill.style.width = `${percent}%`;
            foodText.textContent = `진행률 ${percent}%`;
        } else if (detail.type === 'MONSTER') {
            monsterFill.style.width = `${percent}%`;
            monsterText.textContent = `진행률 ${percent}%`;
        }
    });

    let firstItemCard = null;
    let firstFoodCard = null;
    let firstMonsterCard = null;

    list.forEach(item => {
        const card = document.createElement('div');
        card.className = 'chronicle-card';

        if (item.progressCount === 0) card.classList.add('no-progress');
        if (item.completed) card.classList.add('completed');

        card.innerHTML = `
            <img src="${basePath}/${item.icon}" alt="${item.name}" class="chronicle-card-icon">
            <div class="chronicle-card-name">${item.name}</div>
            <div class="chronicle-card-progress">
                ${item.progressCount} / ${item.requiredCount}
            </div>
        `;

        card.onclick = () => {
            const desc = item.desc || '설명이 없습니다.';
            if (item.type === 'DROP') {
                collectDesc.innerText = desc;
            } else if (item.type === 'FOOD') {
                cookDesc.innerText = desc;
            } else if (item.type === 'MONSTER') {
                monsterDesc.innerText = desc;
            }
        };

        if (item.type === 'DROP') {
            collectList.appendChild(card);
            if (!firstItemCard) firstItemCard = card;
        } else if (item.type === 'FOOD') {
            cookList.appendChild(card);
            if (!firstFoodCard) firstFoodCard = card;
        } else if (item.type === 'MONSTER') {
            monsterList.appendChild(card);
            if (!firstMonsterCard) firstMonsterCard = card;
        }
    });

    if (firstItemCard) firstItemCard.click();
    if (firstFoodCard) firstFoodCard.click();
    if (firstMonsterCard) firstMonsterCard.click();
}


function updateChronicleProgressUI(summary) {
    const summaryTitle = document.querySelector('.chronicle-summary-text');
    summaryTitle.textContent = '';
    summaryTitle.textContent = `감자 연대기 [${currentChronicleMapName}] 진행률`;

    const totalPercent = Math.floor(summary.totalPercent);
    const isCompleted = summary.completed === true;

    const barFill = document.querySelector('.chronicle-progress-fill');
    const barText = document.querySelector('.chronicle-progress-text');
    const completeBtn = document.getElementById('chronicleCompleteBtn');

    completeBtn.classList.remove('hidden', 'chronicle-btn-ready', 'chronicle-btn-done');
    completeBtn.disabled = false;
    completeBtn.onclick = null;

    if (isCompleted) {
        // ✅ 보상 이미 수령함
        barFill.style.display = 'none';
        barText.style.display = 'none';
        completeBtn.textContent = `${currentChronicleMapName} 전문가 등록 완료`;
        completeBtn.classList.add('chronicle-btn-done');
        completeBtn.disabled = true;

    } else if (totalPercent >= 100) {
        barFill.style.display = 'none';
        barText.style.display = 'none';
        completeBtn.textContent = `${currentChronicleMapName} 탐험 뱃지 받기`;
        completeBtn.classList.add('chronicle-btn-ready');

        completeBtn.onclick = async () => {
            try {
                const res = await apiRequestJson('/api/chronicle/complete', 'POST', {
                    mapId: currentChronicleMapId,
                });
                if (res.code === 'SUCCESS') {
                    showMessageModal(
                        `[${currentChronicleMapName}] 탐험 뱃지를 획득했습니다!`
                    );
                    fetchChronicleList(currentChronicleMapId);
                } else {
                    showMessageModal(res.message || '보상 수령에 실패했습니다.');
                }
            } catch (err) {
                console.error(err);
                showMessageModal('서버 오류가 발생했습니다.');
            }
        };
    } else {
        // 진행 중
        barFill.style.display = 'block';
        barText.style.display = 'block';
        barFill.style.width = `${totalPercent}%`;
        barText.textContent = `총 진행률 ${totalPercent}%`;
        completeBtn.classList.add('hidden');
    }
}


closeChronicleBtn.onclick = closeChronicleModal;
function closeChronicleModal() {
    chronicleModal.classList.add('hidden');
    // battleMapSelectModal.classList.remove('hidden');
    document.body.style.overflow = '';
}
