const alchemyModal = document.getElementById("alchemyModal");
const alchemyItemSlot = document.getElementById("alchemyItemSlot");
const alchemyItemInfo = document.getElementById("alchemyItemInfo");
const alchemyExecuteBtn = document.getElementById("alchemyExecuteBtn");
const alchemyMaterialList = document.getElementById("alchemyMaterialList");
const alchemyNeedGold = document.getElementById("alchemyNeedGold");
const alchemyOptionList = document.getElementById("alchemyOptionList");

let selectedAlchemyItem = null;

async function openAlchemyModal() {
    const valid = await checkSessionValid();
    if (!valid) return;
    if (window.readOnlyMode) return;
    playEffect("se_click2");
    resetAlchemyModal();
    alchemyModal.classList.remove("hidden");
}

function closeAlchemyModal() {
    alchemyModal.classList.add("hidden");
    selectedAlchemyItem = null;
    openInfoModal();
}

alchemyItemSlot.addEventListener("click", () => {
    playEffect("se_click2");
    openAlchemyItemModal();
});

async function selectAlchemyItem(item) {
    selectedAlchemyItem = item;

    alchemyItemSlot.innerHTML = `
        <img src="${basePath}${item.iconPath}" alt="${item.name}" data-item-id="${item.id}">
    `;
    alchemyItemSlot.classList.remove("empty-slot");
    document.querySelector(".alchemy-item-name").textContent = item.name;
    alchemyItemInfo.style.display = "flex";

    // 기존 옵션 표시
    renderAlchemyOptions(item.alchemyOptions);
    // 등장 가능한 옵션 리스트
    await loadAvailableAlchemyOptions(item.id);
    // 연금 재료
    await loadAlchemyMaterials(item.id);
}

async function loadAvailableAlchemyOptions(itemId) {
    try {
        const res = await apiRequestJson("/api/alchemy/available-options", "POST", {itemId});
        const options = res.data || [];

        const container = document.getElementById("alchemyAvailableOptionList");
        container.innerHTML = "";

        if (options.length === 0) {
            container.innerHTML = `<div class="option-hint-text">등장 가능한 옵션 없음</div>`;
            return;
        }

        options.forEach(opt => {
            const label = getOptionLabel(opt.optionType);
            const min = opt.min;
            const max = opt.max;
            const unit = opt.valueType === "PERCENT" ? "%" : "";
            const el = document.createElement("div");
            el.className = "available-option-row";
            el.innerHTML = `
                <span class="option-name">${label}</span>
                <span class="option-range">+${min}~${max}${unit}</span>
            `;
            container.appendChild(el);
        });
    } catch (err) {
        console.error("가능 옵션 불러오기 실패:", err);
        showMessageModal("등장 가능한 옵션을 불러오지 못했습니다.");
    }
}

async function loadAlchemyMaterials(itemId) {
    try {
        const res = await apiRequestJson("/api/alchemy/material", "POST", { itemId });
        const data = res.data;

        // 골드
        const needGoldText = data.gold.toLocaleString();
        const ownedGoldText = data.goldOwned.toLocaleString();

        const goldLine = `필요: ${needGoldText} G / 보유: ${ownedGoldText} G`;
        alchemyNeedGold.textContent = goldLine;

        alchemyNeedGold.style.color = data.goldOwned < data.gold ? "#ff4d4d" : "gold";
        alchemyNeedGold.style.opacity = data.goldOwned < data.gold ? 0.8 : 1;

        // 재료
        alchemyMaterialList.innerHTML = "";
        data.materials.forEach(mat => {
            const el = document.createElement("div");
            const insufficient = mat.owned < mat.quantity;
            el.className = "material-card" + (insufficient ? " insufficient" : "");

            el.innerHTML = `
                <img src="${basePath}${mat.iconPath}" class="material-img">
                <div class="material-info">
                    <div class="material-name">${mat.name}</div>
                    <div class="material-count" style="color:${insufficient ? "#ff4d4d" : "#ccc"};">
                        보유: ${mat.owned} / 필요: ${mat.quantity}
                    </div>
                </div>
            `;
            alchemyMaterialList.appendChild(el);
        });

        // 버튼 활성화 여부
        const canProceed = data.materials.every(m => m.owned >= m.quantity) && data.goldOwned >= data.gold;
        alchemyExecuteBtn.disabled = !canProceed;
        alchemyExecuteBtn.classList.toggle("disabled", !canProceed);

    } catch (err) {
        console.error("연금 재료 조회 실패:", err);
        showMessageModal("재료를 불러오지 못했습니다.");
    }
}

alchemyExecuteBtn.addEventListener("click", async () => {
    if (!selectedAlchemyItem || alchemyExecuteBtn.disabled) return;

    // 중복 방지
    alchemyExecuteBtn.disabled = true;
    alchemyExecuteBtn.classList.add("disabled");

    playEffect("se_craft");

    // 1. 옵션 초기화 + 반짝임
    alchemyOptionList.innerHTML = "";
    alchemyItemInfo.classList.add("animate");

    try {
        const res = await apiRequestJson("/api/alchemy/execute", "POST", {
            itemId: selectedAlchemyItem.id
        });

        if (res.code === "SUCCESS") {
            // 2. 옵션 딜레이 표시
            setTimeout(() => {
                // showAlchemyMessage("연금 완료");
                renderAlchemyOptions(res.data); // 하나씩 fade-in
                loadCharacterBasicInfo();
                loadAlchemyMaterials(selectedAlchemyItem.id);
                alchemyItemInfo.classList.remove("animate");

                // 버튼 다시 활성화
                alchemyExecuteBtn.disabled = false;
                alchemyExecuteBtn.classList.remove("disabled");
            }, 800);
        } else {
            showMessageModal(res.message || "연금에 실패했습니다.");
            alchemyItemInfo.classList.remove("animate");
            alchemyExecuteBtn.disabled = false;
            alchemyExecuteBtn.classList.remove("disabled");
        }
    } catch (err) {
        console.error("연금 실행 실패:", err);
        showMessageModal("서버 오류로 연금에 실패했습니다.");
        alchemyItemInfo.classList.remove("animate");
        alchemyExecuteBtn.disabled = false;
        alchemyExecuteBtn.classList.remove("disabled");
    }
});

function showAlchemyMessage(text) {
    const container = document.querySelector("#alchemyModal .alchemy-body");

    const msg = document.createElement("div");
    msg.className = "alchemy-result-message";
    msg.textContent = text;

    container.style.position = "relative";
    container.appendChild(msg);

    setTimeout(() => msg.remove(), 1000);
}
// 7. 옵션 결과 표시
function renderAlchemyOptions(optionList) {
    alchemyOptionList.innerHTML = ""; // 초기화
    if (!optionList || optionList.length === 0) {
        alchemyOptionList.innerHTML = `<div class="alchemy-option-empty">옵션이 없습니다.</div>`;
        return;
    }

    optionList.forEach((opt, index) => {
        const value = opt.optionValue ?? opt.value;
        const valueText = (opt.valueType === "PERCENT") ? `+${value}%` : `+${value}`;
        const label = getOptionLabel(opt.optionType);

        const el = document.createElement("div");
        el.className = "alchemy-option fade-in";
        el.style.animationDelay = `${index * 0.2}s`; // 순차 등장
        el.innerHTML = `
            <div class="option-name">${label}</div>
            <div class="option-value">${valueText}</div>
        `;
        alchemyOptionList.appendChild(el);
    });
}

// 8. 옵션명 매핑
function getOptionLabel(type) {
    switch (type) {
        case "HP": return "체력";
        case "ATTACK": return "공격력";
        case 'DEFENSE': return '방어력';
        case "CRIT_RATE": return "치명타 확률";
        case "CRIT_DMG": return "치명타 피해";
        case "EXP_GAIN": return "경험치 획득량";
        case "GOLD_GAIN": return "골드 획득량";
        default: return type;
    }
}

function resetAlchemyModal() {
    // 슬롯 초기화
    alchemyItemSlot.innerHTML = `<span class="plus-icon">+</span><div class="alchemy-effect-layer"></div>`;
    alchemyItemSlot.classList.add("empty-slot");

    // 이름/텍스트 영역 초기화
    document.querySelector(".alchemy-item-name").textContent = "아이템을 선택하세요.";

    // 현재 옵션 + 등장 옵션 초기화
    alchemyOptionList.innerHTML = "";
    document.getElementById("alchemyAvailableOptionList").innerHTML = "";

    // 재료 및 골드 초기화
    alchemyMaterialList.innerHTML = "";
    alchemyNeedGold.textContent = "-";

    // 정보 패널 숨김
    alchemyItemInfo.style.display = "none";

    // 버튼 비활성화
    alchemyExecuteBtn.disabled = true;
    alchemyExecuteBtn.classList.add("disabled");
}

async function openAlchemyItemModal() {
    try {
        const res = await apiRequest('/api/char/battle', 'GET');

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "장착 아이템 정보를 불러오지 못했습니다.");
            return;
        }

        const equippedItems = res.data?.equippedItems || [];
        renderAlchemyItemGrid(equippedItems);
        enhanceItemModal.classList.remove("hidden");

    } catch (err) {
        console.error("장착 아이템 불러오기 실패:", err);
        showMessageModal("서버 오류로 아이템을 불러오지 못했습니다.");
    }
}


function renderAlchemyItemGrid(items) {
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

        cell.appendChild(img);
        cell.appendChild(label);

        if (item.enhancementLevel && item.enhancementLevel > 0) {
            const enhanceLabel = createEnhanceLabel(item.enhancementLevel);
            cell.appendChild(enhanceLabel);
        }

        cell.addEventListener("click", () => {
            selectAlchemyItem(item);
            closeItemModal();
        });

        enhanceItemGrid.appendChild(cell);
    });
    const addSlot = document.createElement("div");
    addSlot.className = "enhance-item-cell enhance-add-slot";
    addSlot.innerHTML = `<div class="enhance-item-plus">+</div>`;
    enhanceItemGrid.appendChild(addSlot);
}