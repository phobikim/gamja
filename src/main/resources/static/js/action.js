/* 액션 맵 선택 모달 */
let selectedSpot = null;
const spotModal = document.getElementById('spotSelectModal');

/* 실제 액션 모달 */
const activityModal = document.getElementById('activityModal');

let currentActivityType = null;
let currentSpotRank = 1;
let droppedItems = {};
let dropTable = [];
let gainedExp = 0;


async function actionGather(activityType) {
    playEffect("se_click2");
    currentActivityType = activityType;
    selectedSpot = null;
    document.getElementById("spotListScroll").innerHTML = '';
    document.getElementById("startActionBtn").disabled = true;

    try {
        const result = await apiRequestJson(`/api/action/${activityType}`, 'GET');
        if (result.code !== 'SUCCESS') {
            showMessageModal(result.message || "스팟 목록을 불러오는 데 실패했습니다.");
            return;
        }

        const filteredSpots = (result.data || []).filter(spot => spot.category === activityType);
        if (filteredSpots.length === 0) {
            showMessageModal(`${activityType}에 해당하는 장소가 없습니다.`);
            return;
        }

        const { level, exp } = filteredSpots[0];
        renderSpotList(filteredSpots, level, exp);
        spotModal.classList.remove("hidden");
    } catch (e) {
        console.error("스팟 목록을 불러오는 데 실패했습니다.", e);
    }
}

function renderSpotList(spots, level, exp) {
    const container = document.getElementById("spotListScroll");
    const startBtn = document.getElementById("startActionBtn");
    const maxExp = 100 + (level - 1) * 20;
    const expPercent = Math.min(100, (exp / maxExp) * 100).toFixed(1);

    // 기본 스킬 정보 표시
    document.getElementById('userSkillLevel').textContent = `스킬 레벨: Lv.${level}`;
    document.getElementById('userSkillExpFill').style.width = `${expPercent}%`;
    document.getElementById('userSkillExpText').textContent = `${exp} / ${maxExp} EXP`;

    container.innerHTML = '';
    selectedSpot = null;
    startBtn.disabled = true;

    spots.forEach((spot, idx) => {
        const card = document.createElement('div');
        card.className = 'map-card';
        card.innerHTML = `<img src="${basePath + spot.iconPath}" class="map-thumbnail" alt="${spot.displayName}">`;

        card.addEventListener('click', (e) => {
            e.stopPropagation();
            selectedSpot = spot;
            updateSpotDetail(spot);
            document.querySelectorAll('.map-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            const canEnter = level >= spot.requiredLevel;
            startBtn.disabled = !canEnter;

            // ✅ 진입 불가능 시 버튼에 안내도 추가할 수 있음
            startBtn.textContent = canEnter ? '⛏ 활동 시작' : `🚫 Lv.${spot.requiredLevel} 이상 필요`;
        });

        container.appendChild(card);

        if (idx === 0) {
            selectedSpot = spot;
            updateSpotDetail(spot);
            card.classList.add('selected');

            const canEnter = level >= spot.requiredLevel;
            startBtn.disabled = !canEnter;
            startBtn.textContent = canEnter ? '⛏ 활동 시작' : `🚫 Lv.${spot.requiredLevel} 이상 필요`;
        }
    });

    startBtn.onclick = () => {
        if (level < selectedSpot.requiredLevel) {
            showMessageModal(`해당 장소는 Lv.${selectedSpot.requiredLevel} 이상부터 입장 가능합니다.`);
            return;
        }
        if (selectedSpot) selectSpot(selectedSpot.rank, selectedSpot.iconPath, selectedSpot.displayName);
    };
}

function updateSpotDetail(spot) {
    document.getElementById('spotSelectName').textContent = spot.displayName;
    document.getElementById('spotSelectDescription').textContent = spot.description || '-';
    document.getElementById('spotRequiredLevel').textContent = spot.requiredLevel
}


async function selectSpot(rank, imagePath, displayName) {
    currentSpotRank = rank;
    dropTable = [];
    droppedItems = {};
    gainedExp = 0;

    closeSpotSelectModal();

    try {
        const response = await apiRequestJson(`/api/action/${currentActivityType}/${rank}`, 'GET');
        dropTable = response?.data || [];
    } catch (e) {
        console.error("드랍 테이블 조회 실패:", e);
        dropTable = [];
    }

    preloadDropImages();
    openActivityModal(currentActivityType, rank, imagePath, displayName);
}

function closeSpotSelectModal() {
    document.getElementById("spotSelectModal").classList.add("hidden");
}

function openActivityModal(activityType, rank, imagePath, displayName) {
    droppedItems = {};

    const modal = document.getElementById("activityModal");
    const img = document.getElementById("activityImage");
    const title = document.getElementById("activityLocationName");

    img.src = basePath + imagePath || '';
    img.alt = `${displayName} (${activityType} ${rank}랭크)`;
    title.textContent = `${displayName}`;

    // renderPossibleItems();
    renderGainedItems();    // ✅ 항상 dropTable 기준 x0으로 초기화

    modal.classList.remove("hidden");
}

async function handleActivityClick() {
    const tooltip = document.querySelector('.activity-click-tooltip');
    if (tooltip) tooltip.remove();

    if (dropTable.length === 0) return;

    const rand = Math.random();
    let sum = 0;
    for (const drop of dropTable) {
        sum += drop.dropRate;
        if (rand <= sum) {
            const quantity = getRandomInt(drop.minQuantity, drop.maxQuantity);
            if (!droppedItems[drop.itemId]) {
                droppedItems[drop.itemId] = { count: quantity, name: drop.name, iconPath: drop.iconPath };
            } else {
                droppedItems[drop.itemId].count += quantity;
            }
            const expPerItem = drop.expReward || 0;
            gainedExp += quantity * expPerItem;
            createActionTextWithImage(drop.iconPath, 'activityModal');
            break;
        }
    }
    renderGainedItems();

}
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

async function completeActivity() {
    playEffect("se_coin");
    activityModal.classList.add("hidden");

    const items = Object.entries(droppedItems).map(([itemId, { count }]) => ({
        itemId: parseInt(itemId),
        count
    }));
    if (items.length === 0) return;

    const roundedExp = Math.floor(gainedExp);
    try {
        const response = await apiRequestJson(`/api/action/addItems`, 'POST', {
            activityType: currentActivityType,
            items,
            exp: roundedExp
        });
        if (response?.data) {
            setUserInfo(response.data);
        }
    } catch (e) {
        showMessageModal("아이템 추가 중 오류가 발생했습니다.");
    }

    droppedItems = {};
    document.querySelectorAll("#activityModalContent .get-item-image-text").forEach(el => el.remove());
}




function createActionTextWithImage(imgSrc, modalId) {

    const actionWrapper = document.createElement('div');
    actionWrapper.className = 'get-item-image-text';

    const img = document.createElement('img');
    img.src = basePath + imgSrc;
    img.alt = '+1 item';
    img.className = 'get-item-image';

    const plusOne = document.createElement('span');
    plusOne.textContent = '+1';
    plusOne.className = 'get-item-plusone';

    actionWrapper.appendChild(img);
    actionWrapper.appendChild(plusOne);

    document.querySelector(`#${modalId}Content`).appendChild(actionWrapper);

    setTimeout(() => {
        actionWrapper.remove();
    }, 1000);
}

function renderGainedItems() {
    const container = document.getElementById('gainedItems');
    container.innerHTML = ''; // ✅ 항상 초기화하고 새로 생성

    dropTable.forEach(drop => {
        const count = droppedItems[drop.itemId]?.count || 0;

        const wrapper = document.createElement('div');
        wrapper.className = 'gained-item';
        wrapper.dataset.itemId = drop.itemId;

        const img = document.createElement('img');
        img.src = basePath + drop.iconPath;
        img.alt = drop.name;

        const countSpan = document.createElement('span');
        countSpan.textContent = `x${count}`;
        countSpan.className = 'gained-count';

        wrapper.appendChild(img);
        wrapper.appendChild(countSpan);
        container.appendChild(wrapper);
    });

    // 수량만 갱신
    dropTable.forEach(drop => {
        const count = droppedItems[drop.itemId]?.count || 0;
        const wrapper = container.querySelector(`[data-item-id='${drop.itemId}']`);
        if (wrapper) {
            const countSpan = wrapper.querySelector('.gained-count');
            countSpan.textContent = `x${count}`;
        }
    });
}

function preloadDropImages() {
    dropTable.forEach(drop => {
        const img = new Image();
        img.src = basePath + drop.iconPath;
    });
}

// 모달 외부 클릭 시 닫기
spotModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.map-select-modal-content');
    if (!inside) spotModal.classList.add('hidden');
});

// 닫기 버튼
document.getElementById('closeActionBtn').onclick = () => {
    spotModal.classList.add('hidden');
};