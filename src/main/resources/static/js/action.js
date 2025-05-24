
const spotModal = document.getElementById('spotSelectModal');
const activityModal = document.getElementById('activityModal');

let currentActivityType = null; // FISHING, MINING, GATHERING, WOODCUTTING
let ActivityImagePath = null;
let currentSpotRank = 1;
let droppedItems = {};
let dropTable = []; // 서버에서 받은 드랍 테이블 캐싱
let gainedExp = 0; // ✅ 누적 경험치
spotModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.spot-select-modal-content');
    if (!inside) spotModal.classList.add('hidden');
});

activityModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.action-modal-content');
    if (!inside) activityModal.classList.add('hidden');
});

async function actionGather(activityType) {
    playEffect("se_click2");
    currentActivityType = activityType;

    const listContainer = document.getElementById("spotButtonList");
    listContainer.innerHTML = '';

    try {
        const result  = await apiRequestJson(`/api/action/${activityType}/${userId}`, 'GET');

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

        const skillInfoDiv = document.getElementById("userSkillInfo");
        const maxExp = 100 + (level - 1) * 20;
        const expPercent = Math.min(100, (exp / maxExp) * 100).toFixed(1);

        skillInfoDiv.innerHTML = `
            <div class="skill-level">현재 스킬 레벨: <strong>Lv.${level}</strong></div>
            <div class="skill-exp-bar">
                <div class="skill-exp-fill" style="width: ${expPercent}%"></div>
                <div class="skill-exp-text">${exp} / ${maxExp} EXP</div>
            </div>
        `;

        filteredSpots.forEach(spot => {
            const btn = document.createElement('button');
            btn.textContent = `${spot.rank}랭크 ${spot.displayName}`;

            const isLocked = level < spot.requiredLevel;

            if (isLocked) {
                btn.disabled = true;
                btn.classList.add('spot-button-disabled');
                btn.title = `요구 레벨 Lv.${spot.requiredLevel} 이상 필요`;

                const lockOverlay = document.createElement('div');
                lockOverlay.className = 'spot-lock-overlay';
                lockOverlay.textContent = `Lv.${spot.requiredLevel}에 잠금 해제`;

                btn.appendChild(lockOverlay);
            } else {
                btn.onclick = () => selectSpot(spot.rank, spot.iconPath, spot.displayName);
            }

            listContainer.appendChild(btn);
        });

        document.getElementById("spotSelectTitle").textContent = `${activityType} 장소 선택`;
        document.getElementById("spotSelectModal").classList.remove("hidden");
    } catch (e) {
        console.error("스팟 목록을 불러오는 데 실패했습니다.", e);
    }
}

async function selectSpot(rank, imagePath, displayName) {
    currentSpotRank = rank;
    closeSpotSelectModal();

    try {
        const response = await apiRequestJson(`/api/action/${currentActivityType}/${rank}/${userId}`, 'GET');
        dropTable = response?.data || [];
    } catch (e) {
        console.error("드랍 테이블 조회 실패:", e);
        dropTable = [];
    }

    openActivityModal(currentActivityType, rank, imagePath, displayName);
}

function closeSpotSelectModal() {
    document.getElementById("spotSelectModal").classList.add("hidden");
}

function openActivityModal(activityType, rank, imagePath, displayName) {
    droppedItems = {};

    const modal = document.getElementById("activityModal");
    const img = document.getElementById("activityImage");

    img.src = imagePath || '';
    img.alt = `${displayName} (${activityType} ${rank}랭크)`;

    modal.classList.remove("hidden");
}

async function handleActivityClick() {
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
        const response = await apiRequestJson(`/api/action/addItems/${userId}`, 'POST', {
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
    img.src = imgSrc;
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

