const fishingSpotModal = document.getElementById('fishingSpotModal');

fishingSpotModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.fish-select-modal-content');
    if (!inside) fishingSpotModal.classList.add('hidden');
});


function actionFish() {
    playEffect("se_click2");
    // 낚시터 선택 모달 오픈
    document.getElementById("fishingSpotModal").classList.remove("hidden");
}
function closeFishingSpotModal() {
    document.getElementById("fishingSpotModal").classList.add("hidden");
}


function selectFishingSpot(spotName) {
    closeFishingSpotModal();

    let rank = 1; // 기본값

    // spotName에 따라 랭크 매핑 (나중에 더 추가 가능)
    if (spotName === '연못') rank = 1;
    else if (spotName === '화산 지대') rank = 2;
    else if (spotName === '망령의 늪') rank = 3;

    openFishingModal(rank);
}

function openFishingModal(rank) {
    const modal = document.getElementById("fishingModal");
    const img = document.getElementById("fishingImage");

    img.src = `/images/content/fish_rank${rank}.png`;
    img.alt = `낚시터 ${rank}랭크`;
    modal.classList.remove("hidden");
}

function handleFishingClick() {
    createActionTextWithImage('./images/items/i_fish.png', 'fishingModal');
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

async function closeFishingModal() {
    playEffect("se_coin"); // 효과음
    const modal = document.getElementById("fishingModal");
    modal.classList.add("hidden");
    // 낚시용 고정 처리
    const action = "fish";
    const countEl = document.querySelectorAll("#fishingModalContent .get-item-image-text").length;
    const count = countEl || 0;

    if (count > 0) {
        try {
            const response = await apiRequestJson('/api/char/add-item', 'POST', {
                count,
                action
            });
            if (response?.data) {
                // 경험치 추가 해야하는데
                setUserInfo(response.data);
            }
        } catch (err) {
            console.error("아이템 추가 실패:", err);
            showMessageModal("아이템 추가 중 오류가 발생했습니다.");
        }
    }
    document.querySelectorAll("#fishingModalContent .get-item-image-text").forEach(el => el.remove());

}