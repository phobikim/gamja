let selectedGrowthItem = null;
let currentGrowthCharacter = null;
// 탭
const tabGrowth = document.getElementById("tab-growth");
const tabEnhance = document.getElementById("tab-enhance");
const growthContent = document.getElementById("growthContent");
const enhanceContent = document.getElementById("enhanceContent");
const enhanceInfoMessage = document.getElementById("enhanceInfoMessage");

const enhanceItemSlot = document.getElementById("enhanceItemSlot");
const enhanceItemModal = document.getElementById("enhanceItemModal");
const enhanceItemGrid = document.getElementById("enhanceItemGrid");



function openGrowthModal(character) {
    tabGrowth.addEventListener("click", switchToGrowthTab);
    tabEnhance.addEventListener("click", switchToEnhanceTab);

    // 캐릭터 정보 바인딩
    currentGrowthCharacter = character;
    document.getElementById("growthImage").src = `${basePath_image}/character/${character.imagePath}`;
    document.getElementById("growthName").textContent = character.name;
    document.getElementById("growthLevel").textContent = character.level || 1;

    const xpPercent = character.currentXp && character.maxXp
        ? (character.currentXp / character.maxXp) * 100
        : 0;
    document.getElementById("growthXpFill").style.width = `${xpPercent}%`;
    document.getElementById("growthXpText").textContent = `${character.currentXp || 0} / ${character.maxXp || 100}`;

    // 슬라이더 초기화
    const slider = document.getElementById("growthSlider");
    const sliderValue = document.getElementById("growthSliderValue");
    slider.value = 1;
    sliderValue.textContent = "1";

    slider.oninput = (e) => {
        sliderValue.textContent = e.target.value;
        updateGrowthPreview();
    };

    // 추후 강화 재료 목록 렌더링
    renderGrowthMaterialList();

    // 모달 표시
    document.getElementById("growthModal").classList.remove("hidden");
}

// 성장 모달 닫기
function closeGrowthModal() {
    loadCharacterBasicInfo();
    handleDexClick()
    document.getElementById("growthModal").classList.add("hidden");
}

// 강화 재료 렌더링 (샘플 구조)
async function fetchGrowthMaterialList() {
    const container = document.getElementById("growthMaterialList");
    container.innerHTML = "";
    try {
        const res = await apiRequest('/api/dex/growth/item-list', 'GET');
        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "강화 재료를 불러오지 못했습니다.");
            return null;
        }
        return res.data || [];
    } catch (err) {
        console.error("강화 재료 불러오기 실패:", err);
        showMessageModal("서버 오류로 아이템을 불러오지 못했습니다.");
    }
}

async function renderGrowthMaterialList() {
    const container = document.getElementById("growthMaterialList");
    container.innerHTML = "";

    const itemList = await fetchGrowthMaterialList();

    if (!itemList || itemList.length === 0) {
        container.innerHTML = `<div style="color: white;">사용 가능한 강화 재료가 없습니다.</div>`;
        return;
    }

    itemList.forEach(item => {
        const el = document.createElement("div");
        el.className = "material-card";

        const expClass = getExpClass(item.bonusExp);
        const bonusExpLabel = item.bonusExp > 0
            ? `<div class="material-exp ${expClass}">+${item.bonusExp} XP</div>`
            : "";

        el.innerHTML = `
            <img src="${basePath}${item.iconPath}" class="material-img" alt="${item.name}">
            <div class="material-info">
                <div class="material-name">
                    ${item.name} <span class="material-count">(보유: ${item.quantity})</span>
                </div>
                ${bonusExpLabel}
                <div class="material-desc">${item.description || ''}</div>
            </div>
        `;

        if (item.quantity <= 0) {
            el.classList.add("disabled");
            el.style.opacity = 0.4;
            el.style.pointerEvents = "none";
        } else {
            el.addEventListener("click", () => {
                selectedGrowthItem = item;

                const slider = document.getElementById("growthSlider");
                const sliderValue = document.getElementById("growthSliderValue");

                slider.max = item.quantity;
                slider.value = 1;
                sliderValue.textContent = "1";

                container.querySelectorAll(".material-card").forEach(c => c.classList.remove("selected"));
                el.classList.add("selected");

                updateGrowthPreview(); // ghost fill 정상 반영됨!
            });
        }

        container.appendChild(el);
    });
}


function getExpClass(bonusExp) {
    if (bonusExp >= 300) return 'exp-large'; // 대
    if (bonusExp >= 200) return 'exp-medium'; // 중
    if (bonusExp > 0) return 'exp-small'; // 소
    return '';
}

function updateGrowthPreview() {
    if (!selectedGrowthItem || !currentGrowthCharacter) return;

    const baseLevel = currentGrowthCharacter.level || 1;
    const baseXp = currentGrowthCharacter.currentXp || 0;
    let maxXp = currentGrowthCharacter.maxXp || 100;
    const bonusXp = selectedGrowthItem.bonusExp || 0;
    const qty = Math.max(1, parseInt(document.getElementById("growthSlider").value || "1"));

    let totalXp = baseXp + bonusXp * qty;
    let level = baseLevel;

    while (totalXp >= maxXp) {
        totalXp -= maxXp;
        level += 1;
        maxXp += 20;
    }
    const percent = (totalXp / maxXp) * 100;

    const levelSpan = document.getElementById("growthLevel");
    if (level > baseLevel) {
        levelSpan.innerHTML = `${baseLevel} → <span style="color: #f54291;">${level}</span>`;
    } else {
        levelSpan.textContent = level;
    }

    document.getElementById("growthXpText").textContent = `${totalXp} / ${maxXp}`;
    const fillEl = document.getElementById("growthXpFill");

    fillEl.style.width = `${percent}%`;
    const changed = (level > baseLevel) || (totalXp !== baseXp);
    fillEl.style.background = changed ? "#f54291" : "#fa6719";
}

function renderGrowthResult() {
    if (!currentGrowthCharacter) return;

    const level = currentGrowthCharacter.level || 1;
    const xp = currentGrowthCharacter.currentXp || 0;
    const maxXp = currentGrowthCharacter.maxXp || 0;
    const percent = (xp / maxXp) * 100;

    document.getElementById("growthLevel").textContent = level;
    document.getElementById("growthXpText").textContent = `${xp} / ${maxXp}`;
    const fillEl = document.getElementById("growthXpFill");
    fillEl.style.width = `${percent}%`;
    fillEl.style.background = "#fa6719";
}

document.getElementById("growthExecuteBtn").addEventListener("click", async () => {
    if (!selectedGrowthItem) {
        showMessageModal("먼저 강화 재료를 선택해주세요!");
        return;
    }

    const qty = parseInt(document.getElementById("growthSlider").value || "0");
    if (qty <= 0) {
        showMessageModal("사용할 수량을 설정해주세요!");
        return;
    }

    const payload = {
        dexId: currentGrowthCharacter.id,
        itemId: selectedGrowthItem.itemId,
        quantity: qty
    };

    const res = await apiRequestJson("/api/dex/growth/execute", "POST", payload);
    if (res.code === "SUCCESS") {
        showMessageModal("감자가 강해졌다!");
        const xpDto = res.data; // level, xp, maxXp
        currentGrowthCharacter.level = xpDto.level;
        currentGrowthCharacter.currentXp = xpDto.xp;
        currentGrowthCharacter.maxXp = xpDto.maxExp;

        await renderGrowthMaterialList();

        selectedGrowthItem = null;
        document.getElementById("growthSlider").value = 0;
        document.getElementById("growthSliderValue").textContent = "0";

        renderGrowthResult();
    } else {
        showMessageModal(res.message || "성장에 실패했습니다.");
    }
});

function switchToGrowthTab() {
    tabGrowth.classList.add("selected");
    tabEnhance.classList.remove("selected");
    growthContent.classList.remove("hidden");
    enhanceContent.classList.add("hidden");
}

function switchToEnhanceTab() {
    tabEnhance.classList.add("selected");
    tabGrowth.classList.remove("selected");
    growthContent.classList.add("hidden");
    enhanceContent.classList.remove("hidden");
    enhanceInfoMessage.textContent = "강화할 아이템을 선택해주세요.";

}


// 강화 슬롯 클릭 → 아이템 모달 열기
enhanceItemSlot.addEventListener("click", () => {
    openEnhanceItemModal();
});

async function openEnhanceItemModal() {
    try {
        const res = await apiRequest('/api/char/battle', 'GET');

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "장착 아이템 정보를 불러오지 못했습니다.");
            return;
        }

        const equippedItems = res.data?.equippedItems || [];
        renderEnhanceItemGrid(equippedItems);
        enhanceItemModal.classList.remove("hidden");

    } catch (err) {
        console.error("장착 아이템 불러오기 실패:", err);
        showMessageModal("서버 오류로 아이템을 불러오지 못했습니다.");
    }
}

function renderEnhanceItemGrid(items) {
    enhanceItemGrid.innerHTML = "";

    const filteredItems = items.filter(item => item.equipSlot !== 'POTION');

    filteredItems.forEach(item => {
        const cell = document.createElement("div");
        cell.className = "enhance-item-cell";

        const img = document.createElement("img");
        img.src = `${basePath}${item.iconPath}`;
        img.alt = item.name;

        // rarity 등급 클래스 부여
        const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
        img.classList.add(rarityClass);
        img.style.borderRadius = "12px";

        // 장비 슬롯 라벨
        const labelText = getEquipSlotLabel(item.equipSlot);
        const label = document.createElement("div");
        label.className = "enhance-item-sticker";
        label.textContent = labelText;

        // 구성
        cell.appendChild(img);
        cell.appendChild(label);

        cell.addEventListener("click", () => {
            selectEnhanceItem(item);
            closeEnhanceItemModal();
        });

        enhanceItemGrid.appendChild(cell);
    });
}

function selectEnhanceItem(item) {
    // 슬롯 아이콘 갱신
    enhanceItemSlot.innerHTML = `
        <img src="${basePath}${item.iconPath}" alt="${item.name}">
    `;
    const img = enhanceItemSlot.querySelector("img");
    const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
    img.classList.add(rarityClass);
    img.style.borderRadius = "12px";
    enhanceItemSlot.classList.remove("empty-slot");

    // 정보 표시
    document.getElementById("enhanceItemInfo").classList.remove("hidden");
    document.querySelector(".enhance-item-name").textContent = item.name;
    document.querySelector(".enhance-item-lv").textContent = `+${item.enhancementLevel || 0}`;

    // 스탯
    document.getElementById("enhanceItemAtk").textContent = item.bonusPower || 0;
    document.getElementById("enhanceItemHp").textContent = item.bonusHp || 0;

    // XP 바
    const xp = item.enhancementXp || 0;
    const maxXp = 100;
    const percent = (xp / maxXp) * 100;

    document.getElementById("enhanceXpFill").style.width = `${percent}%`;
    document.getElementById("enhanceXpText").textContent = `${xp} / ${maxXp}`;

    loadEnhanceMaterials(item.id);
}

function closeEnhanceItemModal() {
    enhanceItemModal.classList.add("hidden");
}

function getEquipSlotLabel(slot) {
    switch (slot?.toUpperCase()) {
        case 'WEAPON': return '무기';
        case 'HELMET': return '모자';
        case 'ARMOR': return '상의';
        case 'PANTS': return '하의';
        case 'SHOES': return '신발';
        case 'RING': return '반지';
        case 'NECK': return '목걸이';
        default: return '';
    }
}

async function loadEnhanceMaterials(itemId) {
    try {
        const res = await apiRequestJson('/api/enhance/material', 'POST', {
            itemId: itemId
        });
        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "강화 재료를 불러오지 못했습니다.");
            return;
        }

        const data = res.data;
        const materialList = data.materials || [];

        // 골드 표시
        const needGold = data.gold;
        const ownedGold = data.goldOwned;
        const goldTextEl = document.getElementById("enhanceNeedGold");

        goldTextEl.textContent = `필요: ${needGold.toLocaleString()} G / 보유: ${ownedGold.toLocaleString()} G`;

        if (ownedGold < needGold) {
            goldTextEl.style.color = "#ff5252"; // 빨간색
            goldTextEl.style.opacity = 0.8;
        } else {
            goldTextEl.style.color = "gold";
            goldTextEl.style.opacity = 1;
        }

        // 성공 확률 표시
        document.getElementById("enhanceSuccessRate").textContent = data.successRate + " %";
        // 재료 목록 렌더링
        const container = document.getElementById("enhanceMaterialList");
        container.innerHTML = "";

        materialList.forEach(mat => {
            const el = document.createElement("div");
            el.className = "material-card";

            const isInsufficient = mat.owned < mat.quantity;
            if (isInsufficient) {
                el.classList.add("insufficient");
                el.style.opacity = 0.6;
            }
            el.innerHTML = `
                <img src="${basePath}${mat.iconPath}" class="material-img" alt="${mat.name}">
                <div class="material-info">
                    <div class="material-name">${mat.name}</div>
                    <div class="material-count" style="color: ${isInsufficient ? '#ff5252' : '#ccc'};">
                        보유: ${mat.owned} / 필요: ${mat.quantity}
                    </div>
                </div>
            `;

            if (isInsufficient) {
                el.style.opacity = 0.6;
            }

            container.appendChild(el);
        });


    } catch (err) {
        console.error("강화 재료 불러오기 실패:", err);
        showMessageModal("서버 오류로 강화 재료를 불러오지 못했습니다.");
    }
}
