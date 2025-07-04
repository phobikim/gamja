const chronicleModal = document.getElementById('chronicleModal');
const closeChronicleBtn = document.getElementById('closeChronicleBtn');

async function openChronicleModal(map) {
    battleMapSelectModal.classList.add('hidden');
    chronicleModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    await fetchChronicleList(map.id);  // 연대기 API 호출 및 렌더링
}


async function fetchChronicleList(mapId) {
    try {
        const res = await apiRequest(`/api/chronicle/list?mapId=${mapId}`, 'GET');
        if (!res.data) {
            showMessageModal("이 맵에는 등록된 연대기 항목이 없습니다!");
            closeChronicleModal();  // 모달 닫기 추가
            return;
        }
        if (res.code === 'SUCCESS') {
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

    const barFill = document.querySelector('.chronicle-progress-fill');
    const barText = document.querySelector('.chronicle-progress-text');

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

    // 전체 진행률 바
    barFill.style.width = `${summary.totalPercent}%`;
    barText.textContent = `총 진행률 ${Math.floor(summary.totalPercent)}%`;

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


closeChronicleBtn.onclick = closeChronicleModal;
function closeChronicleModal() {
    chronicleModal.classList.add('hidden');
    battleMapSelectModal.classList.remove('hidden');
    document.body.style.overflow = '';
}
