const dexModal = document.getElementById('dexModal');
const dexTabBtns = document.querySelectorAll(".dex-tab-btn");
const dexTabContents = {
    character: document.getElementById("characterTab"),
    battle: document.getElementById("battleEquipTab"),
    life: document.getElementById("lifeEquipTab"),
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
            if (type === "battle") renderCardsByType("battle", currentDexData.battleEquipItemList);
            if (type === "life") renderCardsByType("life", currentDexData.lifeEquipItemsList);
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
        } else {
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
    const detailStat = document.getElementById("detailStat");
    const detailEquip = document.getElementById("detailEquip");

    if (type === "character") {
        detailImg.src = `${basePath_image}/character/${item.imagePath}`;
    } else {
        detailImg.src = `${basePath}/${item.imagePath}`;
    }

    // 기본 정보 설정
    document.getElementById("detailName").textContent = item.name;
    const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
    detailImg.className = `detail-image ${rarityClass}`;
    const characterIcon = document.getElementById("characterIcon");

    if (type === "character") {
        document.querySelector("#effectCondition .effect-label").textContent = "획득처";
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
        document.getElementById("detailCondition").textContent = item.condition || "";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";

        // 캐릭터 스탯 표시
        detailStat.style.display = "flex";
        document.getElementById("detailAtk").textContent = item.basePower || 0;
        document.getElementById("detailHp").textContent = item.baseHp || 0;

        // 캐릭터 장착, 강화 버튼 표시
        detailEquip.style.display = "flex";

        // 미보유 오버레이 설정 (캐릭터 탭에만 적용)
        if (item.owned === false) {
            notOwnedOverlay.classList.remove("hidden");
        } else {
            notOwnedOverlay.classList.add("hidden");
        }

    } else if (type === "monster") {
        detailEquip.style.display = "none";
        characterIcon.style.display = "none";
        detailLevelInfo.style.display = "none";
        detailAffinityInfo.style.display = "none";
        notOwnedOverlay.classList.add("hidden");

        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";

        // ⭐ 드랍 아이템 이름 나열
        document.querySelector("#effectCondition .effect-label").textContent = "드랍템";
        const dropNames = (item.dropItemList || []).map(it => it.name).join(", ");
        document.getElementById("detailCondition").textContent = dropNames || "없음";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";

        detailStat.style.display = "flex";
        document.getElementById("detailAtk").textContent = item.basePower || 0;
        document.getElementById("detailHp").textContent = item.baseHp || 0;
    } else {
        document.querySelector("#effectCondition .effect-label").textContent = "획득처";
        // ✅ 레벨/친밀도 영역 숨김
        characterIcon.style.display = "none";
        detailLevelInfo.style.display = "none";
        detailAffinityInfo.style.display = "none";

        // ✅ 미보유 오버레이 숨김
        notOwnedOverlay.classList.add("hidden");
        // detailEffects 영역 표시
        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";
        document.getElementById("detailCondition").textContent = item.condition || "";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";
        // document.getElementById("detailRarity").textContent =
        //     type === "item" ? (item.rarity ?? "Common") : (item.rank ?? "Common");
        if(type === "item") {
            detailEquip.style.display = "none";
            detailStat.style.display = "none";
        } else {
            detailEquip.style.display = "flex";
            detailStat.style.display = "flex";
            document.getElementById("detailAtk").textContent = item.basePower || 0;
            document.getElementById("detailHp").textContent = item.baseHp || 0;
        }
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

function closeDexModal() {
    dexModal.classList.add('hidden');
}
