let selectedGrowthItem = null;
let currentGrowthCharacter = null;

const growthModal = document.getElementById("growthModal");
const executeBtn = document.getElementById("growthExecuteBtn");

async function openGrowthModal(character) {
    const valid = await checkSessionValid();
    if (!valid) return;
    if (window.readOnlyMode) return;

    playEffect("se_click2");

    // 캐릭터 정보 바인딩
    currentGrowthCharacter = character;
    document.getElementById("growthImage").src = `${basePath_image}/character/${character.characterImage}`;
    document.getElementById("growthName").textContent = character.dexName;
    document.getElementById("growthLevel").textContent = character.level || 1;

    const xpPercent = character.xp && character.maxExp
        ? (character.xp / character.maxExp) * 100
        : 0;
    document.getElementById("growthXpFill").style.width = `${xpPercent}%`;
    document.getElementById("growthXpText").textContent = `${character.xp || 0} / ${character.maxExp || 100}`;

    // 슬라이더 초기화
    const slider = document.getElementById("growthSlider");
    const sliderValue = document.getElementById("growthSliderValue");
    slider.value = 1;
    sliderValue.textContent = "1";

    slider.oninput = (e) => {
        sliderValue.textContent = e.target.value;
        updateGrowthPreview();
    };

    await renderGrowthMaterialList();
    growthModal.classList.remove('hidden');
}

// 성장 모달 닫기
function closeGrowthModal() {
    loadCharacterBasicInfo();
    growthModal.classList.add("hidden");
    characterModal.classList.remove("hidden");
}

executeBtn.addEventListener("click", async () => {
    await handleGrowthExecute();
});


// 성장 실행
async function handleGrowthExecute() {
    if (!selectedGrowthItem) {
        showMessageModal("먼저 강화 재료를 선택해주세요!");
        return;
    }
    const qty = parseInt(document.getElementById("growthSlider").value || "0");
    if (qty <= 0) {
        showMessageModal("사용할 수량을 설정해주세요!");
        return;
    }

    playEffect("se_craft");
    const payload = {
        dexId: currentGrowthCharacter.dexId,
        itemId: selectedGrowthItem.itemId,
        quantity: qty
    };

    const res = await apiRequestJson("/api/dex/growth/execute", "POST", payload);
    if (res.code === "SUCCESS") {
        showMessageModal("감자가 강해졌다!");
        const xpDto = res.data;
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
}

// 강화 재료 렌더링
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

                updateGrowthPreview();
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
    const baseXp = currentGrowthCharacter.xp || 0;
    let maxXp = currentGrowthCharacter.maxExp || 100;
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
    const xp = currentGrowthCharacter.xp || 0;
    const maxXp = currentGrowthCharacter.maxExp || 0;
    const percent = (xp / maxXp) * 100;

    document.getElementById("growthLevel").textContent = level;
    document.getElementById("growthXpText").textContent = `${xp} / ${maxXp}`;
    const fillEl = document.getElementById("growthXpFill");
    fillEl.style.width = `${percent}%`;
    fillEl.style.background = "#fa6719";
}
