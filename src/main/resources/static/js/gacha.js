let isGachaInProgress = false;

function handleLaboratoryClick() {
    const overlay = document.getElementById("resultCardOverlay");
    const modalContent = document.querySelector('.gacha-modal-content');

    // 초기화
    overlay.classList.remove("show");
    modalContent.className = "gacha-modal-content";
    modalContent.style.backgroundImage = "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_enter.png')";

    const modal = document.getElementById("gachaModal");
    modal.classList.remove("hidden");
    document.body.style.overflow = "hidden";
}

function getBackgroundByRarity(rarity) {
    const map = {
        COMMON: "common",
        UNCOMMON: "uncommon",
        RARE: "rare",
        EPIC: "epic",
        LEGENDARY: "legendary"
    };
    return map[rarity?.toUpperCase()] || "common";
}

function getBackgroundImageByRarity(rarity) {
    const map = {
        COMMON: "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_common.png')",
        UNCOMMON: "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_uncommon.png')",
        RARE: "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_rare.png')",
        EPIC: "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_epic.png')",
        LEGENDARY: "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_legendary.png')"
    };
    return map[rarity?.toUpperCase()] || map.COMMON;
}

async function handleGachaClick() {
    if (isGachaInProgress) return;
    isGachaInProgress = true;

    const button = document.getElementById("gachaButton");
    const peelingLabel = document.getElementById("peelingLabel");
    const modalContent = document.querySelector('.gacha-modal-content');

    // 1. 버튼 상태 변경
    button.disabled = true;
    button.textContent = "감정중...";

    // 2. "감자 깎는중" 라벨 표시
    peelingLabel.classList.remove("hidden");

    try {
        // 3. API 호출 (비동기)
        const res = await apiRequest("/api/char/gacha", "GET");

        if (res.code !== "SUCCESS") {
            peelingLabel.classList.add("hidden");
            showMessageModal(res.message || "감정 실패");
            resetGachaButton();
            return;
        }

        // 4. 3초 후 라벨 숨기고 배경 변경
        setTimeout(() => {
            peelingLabel.classList.add("hidden");

            // 배경 이미지 변경 (rarity 기반)
            const rarityClass = getBackgroundByRarity(res.data.rarity);
            const backgroundImage = getBackgroundImageByRarity(res.data.rarity);

            modalContent.className = `gacha-modal-content ${rarityClass}`;
            modalContent.style.backgroundImage = backgroundImage;

            // 5. 배경 변경 후 2초 뒤 결과 카드 표시
            setTimeout(() => {
                showGachaResult(res.data);
            }, 1000);

        }, 2000);

    } catch (e) {
        peelingLabel.classList.add("hidden");
        showMessageModal("서버 오류 발생");
        resetGachaButton();
    }
}

function showGachaResult(resultData) {
    const overlay = document.getElementById("resultCardOverlay");
    const card = document.getElementById("resultCard");
    const typeLabel = document.getElementById("resultTypeLabel");

    // 1. 카드 애니메이션 초기화
    card.className = "result-card-reset";

    // 2. 기존 빛 효과 제거
    const existingFlare = card.querySelector('.flare-effect');
    if (existingFlare) existingFlare.remove();

    // 3. DOM reflow 강제
    void card.offsetHeight;

    // 4. rarity 기반 카드 클래스 적용
    const rarityClass = getBackgroundByRarity(resultData.rarity);
    card.className = `result-card ${rarityClass}`;

    // 5. 새로운 빛 효과 추가
    const flare = document.createElement('div');
    flare.className = 'flare-effect';
    card.appendChild(flare);

    // 6. NEW/DUPLICATE 라벨 설정
    if (resultData.resultType === 'NEW') {
        typeLabel.textContent = 'NEW';
        typeLabel.className = 'result-type-label new';
    } else {
        typeLabel.textContent = 'DUPLICATE';
        typeLabel.className = 'result-type-label duplicate';
    }

    // 7. 별점 설정
    const rarityStars = {
        common: 1,
        uncommon: 2,
        rare: 3,
        epic: 4,
        legendary: 5
    };
    const starCount = rarityStars[resultData.rarity.toLowerCase()] || 1;
    const starRating = document.getElementById('starRating');
    starRating.innerHTML = '';
    for (let i = 0; i < starCount; i++) {
        starRating.innerHTML += '<span class="star">★</span>';
    }

    // 8. 캐릭터 정보 설정
    document.getElementById('gachaDexName').textContent = resultData.name;
    const characterImage = document.getElementById('characterImage');
    characterImage.src = `${basePath_image}/character/${resultData.image}`;
    characterImage.className = 'character-image';
    document.getElementById('characterAttribute').textContent = resultData.attribute;

    // 9. 결과 오버레이 표시
    setTimeout(() => {
        overlay.classList.add('show');
    }, 200);
}

function resetGachaButton() {
    document.getElementById("gachaButton").disabled = false;
    document.getElementById("gachaButton").textContent = "감정하기";
    const modalContent = document.querySelector('.gacha-modal-content');
    modalContent.style.backgroundImage = "url('https://phobi.me/gamja.img/images/backgrounds/bg_gacha_enter.png')";
    isGachaInProgress = false;
}

function closeGachaModal() {
    const modal = document.getElementById("gachaModal");
    const modalContent = document.querySelector('.gacha-modal-content');

    modal.classList.add("hidden");
    document.body.style.overflow = "";

    // 상태 초기화
    resetGachaButton();
}

function closeResultCardOnly() {
    document.getElementById("resultCardOverlay").classList.remove("show");
    resetGachaButton();
}