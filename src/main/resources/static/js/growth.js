let selectedGrowthItem = null;
let currentGrowthCharacter = null;
function openGrowthModal(character) {
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

        // 클릭 시: 선택 처리 + 슬라이더 값 변경
        el.addEventListener("click", () => {
            selectedGrowthItem = item;

            // 슬라이더 max 설정
            const slider = document.getElementById("growthSlider");
            slider.max = item.quantity;
            slider.value = 1;
            document.getElementById("growthSliderValue").textContent = "1";

            // 기존 선택 해제
            const allCards = container.querySelectorAll(".material-card");
            allCards.forEach(c => c.classList.remove("selected"));
            el.classList.add("selected");
        });

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
    const qty = parseInt(document.getElementById("growthSlider").value || 1);

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
    fillEl.style.background = level > baseLevel ? "#f54291" : "#fa6719";
}