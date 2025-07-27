const enhanceModal = document.getElementById("enhanceModal");
const enhanceExecuteBtn = document.getElementById("enhanceExecuteBtn");

const enhanceContent = document.getElementById("enhanceContent");
// const enhanceInfoMessage = document.getElementById("enhanceInfoMessage");
const enhanceItemSlot = document.getElementById("enhanceItemSlot");
const enhanceItemModal = document.getElementById("enhanceItemModal");
const enhanceItemGrid = document.getElementById("enhanceItemGrid");

async function openEnhanceModal() {
    const valid = await checkSessionValid();
    if (!valid) return;
    playEffect("se_click2");

    const isMax = document.getElementById("enhanceMaxMessage").classList.contains("hidden") === false;
    if (isMax) {
        enhanceExecuteBtn.textContent = "강화 완료";
        enhanceExecuteBtn.classList.add("disabled");
        enhanceExecuteBtn.disabled = true;
        return;
    }
    enhanceExecuteBtn.textContent = "강화";
    enhanceExecuteBtn.disabled = false;
    enhanceExecuteBtn.classList.remove("disabled");

    const goldEl = document.getElementById("enhanceNeedGold").textContent;
    const goldMatch = goldEl.match(/필요:\s?([\d,]+)/);
    const ownedMatch = goldEl.match(/보유:\s?([\d,]+)/);

    const needGold = goldMatch ? parseInt(goldMatch[1].replace(/,/g, '')) : 0;
    const ownedGold = ownedMatch ? parseInt(ownedMatch[1].replace(/,/g, '')) : 0;

    const materials = Array.from(document.querySelectorAll("#enhanceMaterialList .material-card")).map(card => {
        const countEl = card.querySelector(".material-count");
        const text = countEl?.textContent || "";
        const [, ownedStr, requiredStr] = text.match(/보유:\s?(\d+)\s?\/\s?필요:\s?(\d+)/) || [];
        return {
            owned: parseInt(ownedStr || "0"),
            quantity: parseInt(requiredStr || "0")
        };
    });

    updateEnhanceButtonState(materials, ownedGold, needGold);
    // enhanceInfoMessage.textContent = "강화할 아이템을 선택해주세요.";

    enhanceModal.classList.remove('hidden');

}

// 강화 슬롯 클릭 → 아이템 모달 열기
enhanceItemSlot.addEventListener("click", () => {
    playEffect("se_click2");
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

        cell.appendChild(img);
        cell.appendChild(label);

        if (item.enhancementLevel && item.enhancementLevel > 0) {
            const enhanceLabel = createEnhanceLabel(item.enhancementLevel);
            cell.appendChild(enhanceLabel);
        }

        cell.addEventListener("click", () => {
            selectEnhanceItem(item);
            closeItemModal();
        });

        enhanceItemGrid.appendChild(cell);
    });
    const addSlot = document.createElement("div");
    addSlot.className = "enhance-item-cell enhance-add-slot";
    addSlot.innerHTML = `<div class="enhance-item-plus">+</div>`;
    enhanceItemGrid.appendChild(addSlot);

}

function selectEnhanceItem(item) {
    // 슬롯 아이콘 갱신
    enhanceItemSlot.innerHTML = `
        <img src="${basePath}${item.iconPath}" alt="${item.name}" data-item-id="${item.id}">
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


    enhanceExecuteBtn.dataset.enhanceItemId = item.id;
    enhanceExecuteBtn.dataset.isFree = xp >= 100 ? "true" : "false";
    enhanceExecuteBtn.textContent = xp >= 100 ? "무료강화" : "강화";

    loadEnhanceMaterials(item.id);
}




enhanceExecuteBtn.addEventListener("click", async () => {
    await handleEnhanceExecute();
});

// 성장 실행
async function handleEnhanceExecute() {
    const img = enhanceItemSlot.querySelector("img");
    if (!img) {
        showMessageModal("강화할 아이템을 선택해주세요!");
        return;
    }
    const itemId = parseInt(img.getAttribute("data-item-id"));
    const isFree = enhanceExecuteBtn.dataset.isFree === "true";
    if (!itemId) {
        showMessageModal("강화할 아이템 정보가 유효하지 않습니다.");
        return;
    }
    playEffect("se_craft");

    const apiUrl = isFree ? "/api/enhance/execute-free" : "/api/enhance/execute";
    const res = await apiRequestJson(apiUrl, "POST", { itemId });

    if (res.code === "SUCCESS") {
        const result = res.data;
        playEnhanceEffect(result.success);
        showEnhanceMessage(result.success ? "강화 성공!" : "강화 실패...", result.success);

        await loadEnhanceMaterials(itemId);
        document.querySelector(".enhance-item-lv").textContent = `+${result.level}`;
        document.getElementById("enhanceItemAtk").textContent = result.bonusPower || 0;
        document.getElementById("enhanceItemHp").textContent = result.bonusHp || 0;
        document.getElementById("enhanceXpText").textContent = `${result.xp} / 100`;
        document.getElementById("enhanceXpFill").style.width = `${(result.xp / 100) * 100}%`;

        enhanceExecuteBtn.textContent = result.xp >= 100 ? "무료강화" : "강화";
        enhanceExecuteBtn.dataset.isFree = result.xp >= 100 ? "true" : "false";
    } else {
        showMessageModal(res.message || "강화에 실패했습니다.");
    }
}


function playEnhanceEffect(success) {
    const slot = document.getElementById("enhanceItemSlot");

    // 이펙트 레이어 없으면 생성
    let effect = slot.querySelector(".enhance-effect-layer");
    if (!effect) {
        effect = document.createElement("div");
        effect.className = "enhance-effect-layer";
        slot.appendChild(effect);
    }

    // 기존 클래스 제거
    effect.classList.remove("effect-success", "effect-fail");

    // 트리거
    setTimeout(() => {
        effect.classList.add(success ? "effect-success" : "effect-fail");

        setTimeout(() => {
            effect.classList.remove("effect-success", "effect-fail");
        }, 600);
    }, 10);
}


function showEnhanceMessage(text, success) {
    const topArea = document.querySelector("#enhanceContent .growth-detail-top");
    const parent = topArea.parentElement;

    const msg = document.createElement("div");
    msg.className = `enhance-result-message ${success ? "success" : "fail"}`;
    msg.textContent = text;

    // 부모에 position relative 적용 필요
    parent.style.position = "relative";
    parent.appendChild(msg);

    setTimeout(() => msg.remove(), 1000);
}

function resetEnhanceTab() {
    // 슬롯 비우기
    enhanceItemSlot.innerHTML = `<span class="plus-icon">+</span><div class="enhance-effect-layer"></div>`;
    enhanceItemSlot.classList.add("empty-slot");

    // 강화 정보 초기화
    const infoBox = document.getElementById("enhanceItemInfo");
    infoBox.classList.remove("hidden");

    document.querySelector(".enhance-item-name").textContent = "아이템을 선택하세요.";
    document.querySelector(".enhance-item-lv").textContent = "+0";
    document.getElementById("enhanceItemAtk").textContent = "0";
    document.getElementById("enhanceItemHp").textContent = "0";
    document.getElementById("enhanceXpFill").style.width = "0%";
    document.getElementById("enhanceXpText").textContent = "0 / 100";

    document.getElementById("enhanceItemAtkNext").textContent = "";
    document.getElementById("enhanceItemHpNext").textContent = "";

    // 강화 재료 / 골드 / 확률 초기화
    document.getElementById("enhanceNeedGold").textContent = "-";
    document.getElementById("enhanceSuccessRate").textContent = "-";
    document.getElementById("enhanceMaterialList").innerHTML = "";
    document.getElementById("enhanceMaxMessage").classList.add("hidden");

    // 버튼 초기화
    enhanceExecuteBtn.disabled = true;
    enhanceExecuteBtn.textContent = "강화";
    enhanceExecuteBtn.classList.add("disabled");
    enhanceExecuteBtn.dataset.isFree = "false";
    delete enhanceExecuteBtn.dataset.enhanceItemId;
}

function updateEnhanceButtonState(materials, ownedGold, needGold) {

    const hasAllMaterials = materials.every(mat => mat.owned >= mat.quantity);
    const hasEnoughGold = ownedGold >= needGold;

    if (hasAllMaterials && hasEnoughGold) {
        enhanceExecuteBtn.disabled = false;
        enhanceExecuteBtn.classList.remove("disabled");
    } else {
        enhanceExecuteBtn.disabled = true;
        enhanceExecuteBtn.classList.add("disabled");
    }
}

async function loadEnhanceMaterials(itemId) {
    const materialBox = document.getElementById("enhanceMaterialBox");
    const container = document.getElementById("enhanceMaterialList");
    const goldTextEl = document.getElementById("enhanceNeedGold");
    const rateEl = document.getElementById("enhanceSuccessRate");
    const maxMsgEl = document.getElementById("enhanceMaxMessage");
    const itemLvEl = document.querySelector(".enhance-item-lv");

    try {
        const res = await apiRequestJson('/api/enhance/material', 'POST', {
            itemId: itemId
        });

        const data = res.data;
        const materials = data?.materials || [];

        /* 최대 강화 */
        if (materials.length === 0) {
            // 일반 재료 영역 숨기고 메시지 출력
            container.classList.add("hidden");
            goldTextEl.parentElement.classList.add("hidden");
            rateEl.parentElement.classList.add("hidden");

            maxMsgEl.innerHTML = `감자단 인증! <strong>최대 강화 완료</strong>`;
            maxMsgEl.classList.remove("hidden");

            // 🔹 레벨 표시를 +N (MAX) 로 바꿈
            const currentLv = data.currentLevel ?? 0;
            itemLvEl.innerHTML = `+${currentLv} <span style="color: gold;">(MAX)</span>`;

            // 🔹 다음 스탯 미리보기 제거
            document.getElementById("enhanceItemAtkNext").textContent = "";
            document.getElementById("enhanceItemHpNext").textContent = "";

            // 버튼도 비활성화
            enhanceExecuteBtn.disabled = true;
            enhanceExecuteBtn.textContent = "강화 완료";
            enhanceExecuteBtn.classList.add("disabled");
            return;
        }

        container.classList.remove("hidden");
        goldTextEl.parentElement.classList.remove("hidden");
        rateEl.parentElement.classList.remove("hidden");
        maxMsgEl.classList.add("hidden");

        const currPower = parseInt(document.getElementById("enhanceItemAtk").textContent) || 0;
        const currHp = parseInt(document.getElementById("enhanceItemHp").textContent) || 0;

        const nextPower = data.nextStat?.bonusPower ?? currPower;
        const nextHp = data.nextStat?.bonusHp ?? currHp;

        const currentLv = data.currentLevel ?? 0;
        const nextLv = data.nextLevel ?? (currentLv + 1);

        if (nextLv > currentLv) {
            itemLvEl.innerHTML = `+${currentLv} → <span style="color:#7cf0ff;">+${nextLv}</span>`;
        } else {
            itemLvEl.textContent = `+${currentLv}`;
        }

        if (nextPower > currPower) {
            document.getElementById("enhanceItemAtkNext").textContent = `→ ${nextPower}`;
        } else {
            document.getElementById("enhanceItemAtkNext").textContent = "";
        }

        if (nextHp > currHp) {
            document.getElementById("enhanceItemHpNext").textContent = `→ ${nextHp}`;
        } else {
            document.getElementById("enhanceItemHpNext").textContent = "";
        }

        // 골드 표시
        const needGold = data.gold;
        const ownedGold = data.goldOwned;

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
        container.innerHTML = "";

        materials.forEach(mat => {
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
                    <div class="material-name">
                        ${mat.name}
                        ${mat.condition ? `<span class="material-condition">(${mat.condition})</span>` : ""}
                    </div>
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

        updateEnhanceButtonState(materials, ownedGold, needGold);

    } catch (err) {
        console.error("강화 재료 불러오기 실패:", err);
        showMessageModal("서버 오류로 강화 재료를 불러오지 못했습니다.");
    }
}

function closeEnhanceModal() {
    loadCharacterBasicInfo();
    enhanceModal.classList.add("hidden");
    characterModal.classList.remove("hidden");
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

function closeItemModal() {
    enhanceItemModal.classList.add("hidden");
}