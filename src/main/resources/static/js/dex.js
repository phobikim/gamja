
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

    const dexData =  await fetchDexMetaData();
    if (!dexData) return;
    currentDexData = dexData;

    renderCardsByType("character", dexData.dexList);

    dexModal.classList.remove("hidden");

    renderDexTabs();
}

async function fetchDexMetaData() {
    try {
        const dexRes = await apiRequest('/api/dex/list', 'GET');
        if (dexRes.code !== 'SUCCESS') {
            closeDexModal();
            showMessageModal(dexRes.message || '도감 목록을 불러오지 못했습니다.');
            return null;
        }
        console.log("도감정보" , dexRes.data);
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

            // 디테일 영역 초기화
            hideDexDetail();

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

    if (type === "character") {
        detailImg.src = `${basePath_image}/character/${item.imagePath}`;
    } else if (type === "item") {
        detailImg.src = `${basePath}/${item.imagePath}`;
    } else if (type === "monster") {
        detailImg.src = `${basePath}/${item.imagePath}`;
    }

    document.getElementById("detailName").textContent = item.name;
    document.getElementById("detailDesc").textContent = item.description || item.desc || "";

    const extra = {
        character: `속성: ${item.attribute}`,
        item: `등급: ${item.rank}`,
        monster: `위험도: ${item.rank}`
    }[type];

    document.getElementById("detailExtra").textContent = extra;
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

function hideDexDetail() {
    document.getElementById("dexDetailPanel").classList.add("hidden");
    document.getElementById("detailImage").src = "";
    document.getElementById("detailName").textContent = "";
    document.getElementById("detailDesc").textContent = "";
    document.getElementById("detailExtra").textContent = "";
}