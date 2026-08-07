let isGachaInProgress = false;

async function handleLaboratoryClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    const overlay = document.getElementById("resultCardOverlay");
    const modalContent = document.querySelector('.gacha-modal-content');

    // 초기화
    overlay.classList.remove("show");
    modalContent.className = "gacha-modal-content";
    modalContent.style.backgroundImage = "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_enter.png')";

    const modal = document.getElementById("gachaModal");
    modal.classList.remove("hidden");
    document.body.style.overflow = "hidden";

    // 4. 서버에서 최신 감자 개수 조회
    await checkPotatoCount()


}

async function checkPotatoCount() {
    // 3. API 호출 (비동기)
    const res = await apiRequest("/api/char/ticketCount", "GET");

    if (res.code !== "SUCCESS") {
        showMessageModal(res.message || "조회 실패");
    }
    const count = res.data.unappraisedCount || 0;
    // UI 업데이트
    updatePotatoCountDisplay(count);
    updateGachaButtonState(count);
}

// 감자 개수 표시 업데이트
function updatePotatoCountDisplay(count) {
    const counterSpan = document.getElementById("unappraisedCountLabel");
    const counter = document.querySelector('.gacha-counter');

    // 카운트 변경 애니메이션
    counterSpan.classList.add('count-changed');
    setTimeout(() => {
        counterSpan.classList.remove('count-changed');
    }, 400);

    // 텍스트 업데이트
    counterSpan.textContent = `x ${count}`;

    // 빈 상태 클래스 관리
    if (count === 0) {
        counter.classList.add('empty');
    } else {
        counter.classList.remove('empty');
    }
}

// 감정하기 버튼 상태 업데이트
function updateGachaButtonState(count) {
    const gachaButton = document.getElementById("gachaButton");
    const multiButton = document.getElementById("multiGachaButton");


    if (count === 0) {
        // 감자가 없을 때 - 버튼 비활성화
        gachaButton.disabled = true;
        gachaButton.textContent = "감자 부족(1)";
        gachaButton.style.backgroundColor = "#666";
        gachaButton.style.cursor = "not-allowed";
        gachaButton.style.opacity = "0.6";
    } else {
        // 감자가 있을 때 - 버튼 활성화
        gachaButton.disabled = false;
        gachaButton.textContent = "감정하기";
        gachaButton.style.backgroundColor = "#fa6719";
        gachaButton.style.cursor = "pointer";
        gachaButton.style.opacity = "1";

    }

    if (multiButton) {
        if (count < 10) {
            multiButton.disabled = true;
            multiButton.textContent = "감자 부족(10)";
            multiButton.style.backgroundColor = "#666";
            multiButton.style.cursor = "not-allowed";
            multiButton.style.opacity = "0.6";
        } else {
            multiButton.disabled = false;
            multiButton.textContent = "10개 감정하기";
            multiButton.style.backgroundColor = "#fa6719";
            multiButton.style.cursor = "pointer";
            multiButton.style.opacity = "1";
        }
    }
}

// 클라이언트에서 감자 개수 감소 (즉시 반응을 위해)
function decreasePotatoCount() {
    const counterSpan = document.getElementById("unappraisedCountLabel");
    const currentCount = parseInt(counterSpan.textContent.replace('x ', '')) || 0;

    if (currentCount > 0) {
        const newCount = currentCount - 1;
        updatePotatoCountDisplay(newCount);
        updateGachaButtonState(newCount);
    }
}


function getBackgroundImageByRarity(rarity) {
    const map = {
        COMMON: "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_common.png')",
        UNCOMMON: "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_uncommon.png')",
        RARE: "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_rare.png')",
        EPIC: "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_epic.png')",
        LEGENDARY: "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_legendary.png')"
    };
    return map[rarity?.toUpperCase()] || map.COMMON;
}

async function handleGachaClick() {
    if (isGachaInProgress) return;

    // 감정 시작 전 감자 개수 재확인
    const counterSpan = document.getElementById("unappraisedCountLabel");
    const currentCount = parseInt(counterSpan.textContent.replace('x ', '')) || 0;
    if (currentCount === 0) {
        showMessageModal("감자가 없습니다!");
        return;
    }

    isGachaInProgress = true;

    const button = document.getElementById("gachaButton");
    const peelingLabel = document.getElementById("peelingLabel");
    const modalContent = document.querySelector('.gacha-modal-content');

    // 1. 버튼 상태 변경
    button.disabled = true;
    peelingLabel.textContent = "감자 깎는 중...";
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
        // 보유 감자 개수 감소
        decreasePotatoCount();

        // 4. 3초 후 라벨 숨기고 배경 변경
        setTimeout(() => {
            peelingLabel.classList.add("hidden");

            // 배경 이미지 변경 (rarity 기반)
            const rarityClass = getBackgroundByRarity(res.data.rarity.rarity);
            const backgroundImage = getBackgroundImageByRarity(res.data.rarity.rarity);

            modalContent.className = `gacha-modal-content ${rarityClass}`;
            modalContent.style.backgroundImage = backgroundImage;

            // 5. 배경 변경 후 2초 뒤 결과 카드 표시
            setTimeout(() => {
                showGachaResult(res.data);
            }, 1000);

        }, 2000);

        setTimeout(async () => {
            await checkPotatoCount();
        }, 4000); // 감정 완료 후 재조회

        loadCharacterBasicInfo();
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

    overlay.classList.add("show");
    overlay.classList.remove("multi-gacha-mode");
    // 1. 완전한 카드 초기화
    card.className = "result-card-reset";
    card.style.animation = "none";

    // 2. 기존 빛 효과 모두 제거
    const existingFlares = card.querySelectorAll('.flare-effect');
    existingFlares.forEach(flare => flare.remove());

    // 3. 강제 DOM reflow (매우 중요!)
    void card.offsetHeight;
    void card.offsetWidth;

    // 4. rarity 기반 카드 클래스 적용
    const rarityClass = getBackgroundByRarity(resultData.rarity.rarity);
    card.className = `result-card ${rarityClass}`;

    // 5. 애니메이션 강제 재시작
    setTimeout(() => {
        card.style.animation = "cardFlip 0.8s ease-out forwards";
    }, 10);

    // 6. 빛 효과 추가 (카드 애니메이션 시작 후 약간의 딜레이)
    setTimeout(() => {
        const flare = document.createElement('div');
        flare.className = 'flare-effect';
        card.appendChild(flare);
    }, 100);

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
    const starCount = rarityStars[resultData.rarity.rarity.toLowerCase()] || 1;
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
    const attributeIconImg = document.getElementById('characterAttributeIcon');
    if (resultData.attributeIconPath) {
        attributeIconImg.src = `${basePath}/${resultData.attributeIconPath}`;
        attributeIconImg.alt = resultData.attribute || '';
        attributeIconImg.style.display = 'block';
    } else {
        attributeIconImg.style.display = 'none';
    }

    // 9. 결과 오버레이 표시
    setTimeout(() => {
        overlay.classList.add('show');
    }, 200);
}

async function handleMultiGachaClick() {
    if (isGachaInProgress) return;

    const count = parseInt(document.getElementById("unappraisedCountLabel").textContent.replace("x ", ""));
    if (count < 10) {
        showMessageModal("감자가 10개 이상 있어야 해!");
        const multiButton = document.getElementById("multiGachaButton");
        multiButton.disabled = true;
        multiButton.textContent = "감자 부족(10)";
        multiButton.style.backgroundColor = "#666";
        multiButton.style.cursor = "not-allowed";
        multiButton.style.opacity = "0.6";
        return;
    }

    isGachaInProgress = true;

    const peelingLabel = document.getElementById("peelingLabel");
    const multiButton = document.getElementById("multiGachaButton");
    const modalContent = document.querySelector(".gacha-modal-content");

    peelingLabel.textContent = "감자를 정신없이 깎는 중...";
    peelingLabel.classList.remove("hidden");

    multiButton.disabled = true;

    try {
        const res = await apiRequest("/api/char/gacha/multi", "GET");
        if (res.code !== "SUCCESS") throw new Error(res.message || "감정 실패");

        for (let i = 0; i < 10; i++) decreasePotatoCount();

        const results = res.data;
        const highest = getHighestRarity(results.map(r => r.rarity.rarity));

        // ✅ 라벨 2초 보여주고 나서 배경 바꾸기 + 결과 보여주기
        setTimeout(() => {
            peelingLabel.classList.add("hidden");

            // 배경은 이때 변경
            modalContent.className = `gacha-modal-content ${getBackgroundByRarity(highest)}`;
            modalContent.style.backgroundImage = getBackgroundImageByRarity(highest);

            // 카드 보여주기
            showMultiGachaResults(results);
        }, 2000);

    } catch (e) {
        peelingLabel.classList.add("hidden");
        showMessageModal(e.message || "서버 오류 발생");
        resetGachaButton();
    } finally {
        await checkPotatoCount();
    }
}

function getHighestRarity(rarities) {
    const order = ["COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"];
    return rarities.sort((a, b) => order.indexOf(b) - order.indexOf(a))[0];
}

function showMultiGachaResults(resultList) {
    const modalContent = document.querySelector(".gacha-modal-content");
    modalContent.classList.add("multi-gacha-mode");

    const overlay = document.getElementById("resultCardOverlay");
    overlay.innerHTML = ""; // 기존 카드 제거
    overlay.classList.add("show", "multi-gacha-mode");

    const grid = document.createElement("div");
    grid.className = "multi-gacha-grid";
    overlay.appendChild(grid);

    // ✅ 확인 버튼 먼저 그려놓기
    const confirm = document.createElement("button");
    confirm.className = "confirm-button";
    confirm.textContent = "확인";
    confirm.disabled = true; // ✅ 처음엔 비활성화
    confirm.onclick = () => {
        overlay.classList.remove("show");
        modalContent.classList.remove("multi-gacha-mode");
        resetGachaButton();
        checkPotatoCount();
    };
    overlay.appendChild(confirm); // ✅ 버튼은 카드보다 먼저 append

    // 카드 순차 등장
    resultList.forEach((result, index) => {
        const card = createResultCardElement(result);
        card.style.opacity = "0";
        grid.appendChild(card);

        setTimeout(() => {
            card.style.opacity = "1";
            card.style.animation = "cardFlip 0.8s ease-out forwards";

            setTimeout(() => {
                const flare = document.createElement("div");
                flare.className = "flare-effect";
                card.appendChild(flare);
            }, 100);
        }, index * 250);
    });

    // ✅ 마지막 카드 등장 후 버튼 활성화
    const totalDelay = resultList.length * 250 + 1000;
    setTimeout(() => {
        confirm.disabled = false;
        confirm.textContent = "확인";
    }, totalDelay);
}

function createResultCardElement(resultData) {
    const card = document.createElement("div");
    const rarityStr = resultData.rarity.rarity;
    const rarityClass = getBackgroundByRarity(rarityStr);
    card.className = `result-card ${rarityClass}`;

    // resultType 라벨
    const typeLabel = document.createElement("div");
    typeLabel.className = `result-type-label ${resultData.resultType === 'NEW' ? 'new' : 'duplicate'}`;
    typeLabel.textContent = resultData.resultType;
    card.appendChild(typeLabel);

    // 캐릭터 이미지
    const imgWrapper = document.createElement("div");
    imgWrapper.className = "character-image-container";

    const img = document.createElement("img");
    img.src = `${basePath_image}/character/${resultData.image}`;
    img.className = "character-image";
    imgWrapper.appendChild(img);

    // 속성 아이콘
    if (resultData.attributeIconPath) {
        const attrWrap = document.createElement("div");
        attrWrap.className = "attribute-icon-wrapper";
        const attrImg = document.createElement("img");
        attrImg.className = "attribute-icon";
        attrImg.src = `${basePath}/${resultData.attributeIconPath}`;
        attrImg.alt = resultData.attribute;
        attrWrap.appendChild(attrImg);
        imgWrapper.appendChild(attrWrap);
    }

    card.appendChild(imgWrapper);

    // 이름
    const nameSpan = document.createElement("span");
    nameSpan.className = "character-name";
    nameSpan.textContent = resultData.name;
    card.appendChild(nameSpan);

    // 별점
    const starWrap = document.createElement("div");
    starWrap.className = "star-rating";

    const stars = {
        common: 1,
        uncommon: 2,
        rare: 3,
        epic: 4,
        legendary: 5
    };
    const count = stars[rarityStr.toLowerCase()] || 1; // ✅ 안전하게 처리
    for (let i = 0; i < count; i++) {
        const star = document.createElement("span");
        star.className = "star";
        star.textContent = "★";
        starWrap.appendChild(star);
    }
    card.appendChild(starWrap);

    return card;
}


function resetGachaButton() {
    const count = parseInt(document.getElementById("unappraisedCountLabel").textContent.replace('x ', '')) || 0;

    updateGachaButtonState(count);

    const overlay = document.getElementById("resultCardOverlay");

    // ✅ 단일 카드용 기본 구조 복원
    overlay.innerHTML = `
        <div id="resultCard" class="result-card">
            <div class="flare-effect"></div>
            <div id="resultTypeLabel" class="result-type-label new">NEW</div>
            <div class="character-image-container">
                <img id="characterImage" class="character-image">
                <div class="attribute-icon-wrapper">
                    <img id="characterAttributeIcon" class="attribute-icon" />
                </div>
                <span class="character-name" id="gachaDexName">???</span>
            </div>
            <div class="star-rating" id="starRating"></div>
            <button class="confirm-button" onclick="closeResultCardOnly()">확인</button>
        </div>
    `;

    // ✅ 단일 감정 버튼
    const gachaButton = document.getElementById("gachaButton");
    gachaButton.disabled = false;
    gachaButton.textContent = "감정하기";

    // ✅ 10개 감정 버튼도 같이 활성화
    const multiButton = document.getElementById("multiGachaButton");
    if (multiButton) {
        if (count >= 10) {
            multiButton.disabled = false;
            multiButton.textContent = "10개 감정하기";
            multiButton.style.backgroundColor = "#fa6719";
            multiButton.style.opacity = "1";
            multiButton.style.cursor = "pointer";
        } else {
            multiButton.disabled = true;
            multiButton.textContent = "감자 부족(10)";
            multiButton.style.backgroundColor = "#666";
            multiButton.style.opacity = "0.6";
            multiButton.style.cursor = "not-allowed";
        }
    }

    const modalContent = document.querySelector('.gacha-modal-content');
    modalContent.style.backgroundImage = "url('https://phobi.my/gamja.img/images/backgrounds/bg_gacha_enter.png')";
    modalContent.classList.remove("multi-gacha-mode");
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

    // 결과 확인 후 감자 개수 재조회
    setTimeout(async () => {
        await checkPotatoCount();
    }, 500);
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
