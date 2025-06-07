const dexModal = document.getElementById('dexModal');
const dexTabBtns = document.querySelectorAll(".dex-tab-btn");
const dexTabContents = {
    character: document.getElementById("characterTab"),
    item: document.getElementById("itemTab"),
    monster: document.getElementById("monsterTab"),
};
let currentDexData = null;

async function handleDexClick() {
    playEffect("se_click2");

    currentDexData = await fetchDexMetaData();
    if (!currentDexData) return;

    // 탭 상태 초기화 (캐릭터 탭 선택)
    dexTabBtns.forEach(b => b.classList.remove("active"));
    dexTabBtns[0].classList.add("active");

    Object.keys(dexTabContents).forEach(key => {
        dexTabContents[key].classList.toggle("hidden", key !== "character");
    });

    renderCardsByType("character", currentDexData.dexList);
    dexModal.classList.remove("hidden");
    renderDexTabs(); // 이벤트 바인딩은 여기에 유지
}

async function fetchDexMetaData() {
    try {
        const dexRes = await apiRequest('/api/dex/list', 'GET');
        if (dexRes.code !== 'SUCCESS') {
            closeDexModal();
            showMessageModal(dexRes.message || '도감 목록을 불러오지 못했습니다.');
            return null;
        }
        return dexRes.data;
    } catch (e) {
        console.error("도감 메타 요청 실패", e);
        return null;
    }
}

// 탭 버튼 렌더링
function renderDexTabs() {
    dexTabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const type = btn.dataset.tab;
            dexTabBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            Object.keys(dexTabContents).forEach(key => {
                dexTabContents[key].classList.toggle("hidden", key !== type);
            });

            if (type === "character") renderCardsByType("character", currentDexData.dexList);
            if (type === "item") renderCardsByType("item", currentDexData.itemList);
            if (type === "monster") renderCardsByType("monster", currentDexData.monsterList);
        });
    });
}
// 카드 + 상세정보 렌더링
function renderCardsByType(type, list) {
    const container = dexTabContents[type];
    container.innerHTML = "";

    list.forEach((item, index) => {
        const card = document.getElementById("dexSquareCardTemplate").content.cloneNode(true);
        const img = card.querySelector(".card-img");
        const stars = card.querySelector(".rarity-stars");

        // 이미지 경로 설정
        if (type === "character") {
            img.src = `${basePath_image}/character/${item.imagePath}`;
        } else if (type === "item") {
            img.src = `${basePath}/${item.imagePath}`;
        } else if (type === "monster") {
            img.src = `${basePath}/${item.imagePath}`;
        }

        img.alt = item.name;
        stars.innerHTML = getStars(item.rarity);

        const cardEl = card.querySelector(".dex-square-card");
        const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
        img.classList.add(rarityClass);

        cardEl.addEventListener("click", () => {
            showDexDetail(type, item);
        });

        container.appendChild(card);

        if (index === 0) {
            setTimeout(() => cardEl.click(), 0);
        }
    });
}


function showDexDetail(type, item) {
    document.getElementById("dexDetailPanel").classList.remove("hidden");

    const detailImg = document.getElementById("detailImage");
    const notOwnedOverlay = document.getElementById("notOwnedOverlay");
    const detailEffects = document.getElementById("detailEffects");
    const detailLevelInfo = document.querySelector(".detail-level-info");
    const detailAffinityInfo = document.getElementById("detailAffinityInfo");

    if (type === "character") {
        detailImg.src = `${basePath_image}/character/${item.imagePath}`;
    } else if (type === "item") {
        detailImg.src = `${basePath}/${item.imagePath}`;
    } else if (type === "monster") {
        detailImg.src = `${basePath}/${item.imagePath}`;
    }

    // 기본 정보 설정
    document.getElementById("detailName").textContent = item.name;
    const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
    detailImg.className = `detail-image ${rarityClass}`;
    const characterIcon = document.getElementById("characterIcon");
    if (type === "character") {
        characterIcon.style.display = item.attributeIconPath ? "block" : "none";

        if (item.attributeIconPath) {
            characterIcon.src = `${basePath}/${item.attributeIconPath}`;
            characterIcon.style.display = "block";
        } else {
            characterIcon.style.display = "none";
        }

        // 캐릭터: 레벨, XP, 친밀도 표시
        document.getElementById("detailLevel").textContent = `LV${item.level || 1}`;
        const xpPercent = item.currentXp && item.maxXp ? (item.currentXp / item.maxXp) * 100 : 0;
        document.getElementById("detailXpFill").style.width = `${xpPercent}%`;
        document.getElementById("detailXpText").textContent = `${item.currentXp || 0}/${item.maxXp || 100}`;
        document.getElementById("detailAffinity").textContent = `${item.affinity || 0}`;
        detailLevelInfo.style.display = "flex";
        detailAffinityInfo.style.display = "flex";


        // detailEffects 영역 표시
        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";

        // 미보유 오버레이 설정 (캐릭터 탭에만 적용)
        if (item.owned === false) {
            notOwnedOverlay.classList.remove("hidden");
        } else {
            notOwnedOverlay.classList.add("hidden");
        }
    } else {
        // ✅ 레벨/친밀도 영역 숨김
        characterIcon.style.display = "none";
        detailLevelInfo.style.display = "none";
        detailAffinityInfo.style.display = "none";
        // ✅ 미보유 오버레이 숨김
        notOwnedOverlay.classList.add("hidden");
        // detailEffects 영역 표시
        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";
        document.getElementById("detailRarity").textContent =
            type === "item" ? (item.rarity ?? "Common") : (item.rank ?? "Common");
    }
}

function getStars(rarity) {
    const starMap = {
        "COMMON": 1,
        "UNCOMMON": 2,
        "RARE": 3,
        "EPIC": 4,
        "LEGENDARY": 5
    };
    return "★".repeat(starMap[rarity] || 0);
}

dexModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.dex-modal-container');
    if (!inside) dexModal.classList.add('hidden');
});

function closeDexModal() {
    dexModal.classList.add('hidden');
}
