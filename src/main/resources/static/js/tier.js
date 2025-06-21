const tierCard = document.getElementById("tierCard");
const tierModal = document.getElementById("tierModal");

// 📌 티어 카드 클릭 → 모달 열기
tierCard.addEventListener("click", () => {
    openTierModal();
});

// 📌 모달 열기: API 호출 → 렌더링
async function openTierModal() {
    try {
        const { myTier, tierList } = await fetchTierData();
        renderTierModal(myTier, tierList);
        tierModal.classList.remove("hidden");
    } catch (err) {
        console.error("티어 정보 로딩 실패", err);
        showMessageModal("티어 정보를 불러오는 데 실패했어요.");
    }
}

// 📌 API 호출: 티어 목록 + 내 티어 정보 동시 요청
async function fetchTierData() {
    const [tierRes, userRes] = await Promise.all([
        apiRequest("/api/char/tier/list", "GET"),
        apiRequest("/api/char", "GET")
    ]);

    if (tierRes.code !== "SUCCESS" || userRes.code !== "SUCCESS") {
        throw new Error("API 호출 실패");
    }

    const myTier = {
        corpsTierId: userRes.data.corpsTierId,
        corpsTierName: userRes.data.corpsTierName,
        corpsTierIcon: userRes.data.corpsTierIcon,
        corpsTierExp: userRes.data.corpsTierExp,
        corpsTierMaxExp: userRes.data.corpsTierMaxExp,
        corpsTierLevel : userRes.data.corpsTierLevel
    };
    window.currentTierList = tierRes.data;
    return {
        myTier,
        tierList: tierRes.data
    };
}

// 📌 전체 티어 모달 렌더링
function renderTierModal(myTier, tierList) {
    renderTierList(tierList, myTier);
    renderMyTierInfo(myTier);
}

// 📌 상단: 모든 티어 이미지 가로 나열
function renderTierList(tiers, myTier) {
    const list = document.getElementById("allTierList");
    list.innerHTML = "";

    tiers.forEach(tier => {
        const card = document.createElement("div");
        card.className = "tier-list-card";

        const img = document.createElement("img");
        img.src = basePath + tier.iconPath;
        img.alt = tier.name;
        img.title = tier.name;

        const name = document.createElement("div");
        name.className = "tier-list-card-name";
        name.textContent = tier.name;

        card.appendChild(img);
        card.appendChild(name);
        list.appendChild(card);

        if (tier.name === myTier.corpsTierName) {
            card.classList.add("current-tier");

            const levelBar = document.createElement("div");
            levelBar.className = "tier-level-bar";
            for (let i = 1; i <= 10; i++) {
                const slot = document.createElement("div");
                slot.className = "tier-level-slot";
                if (i <= myTier.corpsTierLevel) {
                    slot.classList.add("filled");
                }
                levelBar.appendChild(slot);
            }
            card.appendChild(levelBar);
        }
    });
}

// 📌 하단: 내 티어 정보 렌더링
function renderMyTierInfo(tier) {
    const tierIcon = document.getElementById("myTierIcon");
    const tierName = document.getElementById("myTierName");
    const tierExpFill = document.getElementById("tierExpFill");
    const tierExpText = document.getElementById("tierExpText");
    const levelBadge = document.querySelector(".tier-level-badge");
    const nextTierTextEl = document.getElementById("nextTierInfo");

    tierIcon.src = basePath + tier.corpsTierIcon;

    const tierNameBox = document.getElementById("myTierNameBox");
    tierNameBox.innerHTML = `
      <div class="tier-name-box">
        <span class="tier-name">${tier.corpsTierName}</span>
        <span class="tier-name-level">Lv. ${tier.corpsTierLevel}</span>
      </div>
    `;

    const percent = (tier.corpsTierExp / tier.corpsTierMaxExp) * 100;
    tierExpFill.style.width = `${percent}%`;
    tierExpText.textContent = `${tier.corpsTierExp} / ${tier.corpsTierMaxExp}`;

    const level = tier.corpsTierLevel;
    if (levelBadge) levelBadge.textContent = `Lv. ${level} / 10`;

    // 다음 티어 이름 가져오기
    const currentTierId = tier.corpsTierId;
    const nextTier = window.currentTierList?.find(t => t.tierId === currentTierId + 1);

    if (nextTierTextEl) {
        if (level >= 10 && nextTier) {
            nextTierTextEl.textContent = `다음 티어 "${nextTier.name}"로 승급 가능합니다!`;
        } else if (nextTier) {
            const remainLevel = 10 - level;
            nextTierTextEl.textContent = `[${nextTier.name}] 까지 ${remainLevel} 레벨 남았습니다.`;
        } else {
            nextTierTextEl.textContent = `최종 티어입니다. 축하합니다!`;
        }
    }
}

// 📌 모달 닫기
function closeTierModal() {
    tierModal.classList.add("hidden");
}
