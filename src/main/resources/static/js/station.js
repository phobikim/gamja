const workshopSelectModal = document.getElementById('workshopSelectModal');
const workshopDetailPanel = document.getElementById('workshopDetailPanel');
const goToWorkshopBtn = document.getElementById('goToWorkshop');
const closeWorkshopSelect = document.getElementById('closeWorkshopSelect');
let selectedStation = null;
async function handleStationClick() {
    playEffect("se_click2");
    workshopSelectModal.classList.remove('hidden');
    selectedStation = null;
    goToWorkshopBtn.disabled = true;
    goToWorkshopBtn.classList.add('disabled');
    await getWorkshopStations();
}

document.getElementById('closeWorkshopSelect').addEventListener('click', () => {
    document.getElementById('workshopSelectModal').classList.add('hidden');
});
document.getElementById('changeWorkshopBtn').addEventListener('click', () => {
    document.getElementById('craftModal').classList.add('hidden');
    document.getElementById('workshopSelectModal').classList.remove('hidden');
});


// 공방 List api 호출
async function getWorkshopStations() {
    try {
        const res = await apiRequest('/api/station/list', 'GET');
        if (res.code === 'SUCCESS') {
            const data = res.data;
            renderWorkshopCards(data);
        }
    } catch (err) {
        console.error('공방 스테이션 로딩 실패:', err);
    }
}

// 카드 렌더링
function renderWorkshopCards(stations) {
    workshopDetailPanel.innerHTML = '';
    stations.forEach(station => {
        const card = document.createElement('div');
        card.className = 'workshop-card';
        card.dataset.category = station.category;

        card.innerHTML = `
            <img src="${basePath}/${station.imagePath}" class="workshop-thumb" alt="${station.name}" />
            <div class="workshop-name">${station.name}</div>
        `;

        card.addEventListener('click', () => {
            selectWorkshopCard(card, station.category);
        });

        workshopDetailPanel.appendChild(card);
    });
}

// 선택 처리
function selectWorkshopCard(cardEl, category) {
    // 모든 카드에서 선택 해제
    document.querySelectorAll('.workshop-card').forEach(card => {
        card.classList.remove('selected');
    });

    cardEl.classList.add('selected');
    selectedStation = category;
    goToWorkshopBtn.disabled = false;
    goToWorkshopBtn.classList.remove('disabled');
}
// 이동 버튼 클릭
goToWorkshopBtn.addEventListener('click', () => {
    if (!selectedStation) return;
    workshopSelectModal.classList.add('hidden');

    openCraftModal(selectedStation);

});

function openCraftModal(stationCategory) {
    document.getElementById('craftModal').classList.remove('hidden');

    // 해당 공방의 제작 리스트 로드
    fetchRecipes(stationCategory);
}

async function fetchRecipes(stationCategory) {
    try {
        const res = await apiRequest(`/api/station/recipe/${stationCategory}`, 'GET');
        if (res.code === 'SUCCESS') {
            renderRecipeList(res.data);
        }
    } catch (err) {
        console.error("제작 리스트 불러오기 실패:", err);
    }
}

function renderRecipeList(recipeList) {
    const container = document.getElementById('craftRecipeList');
    container.innerHTML = '';

    recipeList.forEach(recipe => {
        const card = document.createElement('div');
        card.className = `recipe-card ${getRarityBackgroundClass(recipe.grade)}`; // 💡 클래스 추가

        card.innerHTML = `
            <img src="${basePath}${recipe.resultItemIcon}" class="recipe-icon" />
            <div class="recipe-name">${recipe.resultItemName}</div>
        `;

        card.addEventListener('click', () => {
            document.querySelectorAll('.recipe-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            renderRecipeDetail(recipe);
        });

        container.appendChild(card);
    });

    if (recipeList.length > 0) {
        container.firstChild.classList.add('active');
        renderRecipeDetail(recipeList[0]);
    }
}


function renderRecipeDetail(recipe) {
    const container = document.getElementById('craftDetailPanel');
    container.innerHTML = '';

    // ✅ 스크롤 전체 감싸는 영역
    const scrollBox = document.createElement('div');
    scrollBox.className = 'craft-detail-scroll';
    // ▶ 아이템 아이콘 + 이름 + 설명
    const infoBox = document.createElement('div');
    infoBox.className = 'detail-info-box'; // 이 안에 아이콘, 이름, 설명

    const icon = document.createElement('img');
    icon.src = basePath + recipe.resultItemIcon;
    icon.alt = recipe.resultItemName;
    icon.className = 'detail-icon';
    icon.style.width = '48px';
    icon.style.height = '48px';
    icon.style.display = 'block';
    icon.style.margin = '0 auto 0.5rem';

    const name = document.createElement('div');
    name.style.color = 'var(--dark-1-font-color)'
    name.className = 'detail-title';
    name.style.fontWeight = 'bold';
    name.style.textAlign = 'center';
    name.style.fontSize = '1rem';
    name.innerHTML = `${recipe.resultItemName} <span style="font-size: 0.85rem; color: #ccc;">(보유: ${recipe.resultItemUserOwned})</span>`;

    const desc = document.createElement('div');
    desc.className = 'detail-desc';
    desc.style.fontSize = '0.85rem';
    desc.style.color = '#ccc';
    desc.style.textAlign = 'center';
    desc.style.marginTop = '0.5rem';
    desc.style.fontWeight = 'bold';
    desc.textContent = recipe.recipeDescription;

    infoBox.appendChild(icon);
    infoBox.appendChild(name);
    infoBox.appendChild(desc);

    // ▶ 재료 리스트
    const materialBox = document.createElement('div');
    materialBox.className = 'ingredient-list'; // ✅ 이름 변경

    recipe.ingredients.forEach(ing => {
        const row = document.createElement('div');
        row.style.display = 'flex';
        row.style.alignItems = 'center';
        row.style.justifyContent = 'space-between';
        row.style.margin = '0.3rem 0';
        row.style.fontSize = '0.85rem';
        row.style.color = 'var(--dark-1-font-color)'

        const left = document.createElement('div');
        left.style.display = 'flex';
        left.style.alignItems = 'center';
        left.style.gap = '0.5rem';

        const ingImg = document.createElement('img');
        ingImg.src = basePath + ing.itemIcon;
        ingImg.style.width = '24px';
        ingImg.style.height = '24px';
        ingImg.style.imageRendering = 'pixelated';

        const ingText = document.createElement('span');
        ingText.textContent = `${ing.itemName} ×${ing.quantity}`;
        ingText.style.fontWeight = 'bold';

        left.appendChild(ingImg);
        left.appendChild(ingText);

        const owned = document.createElement('span');
        owned.textContent = `(보유: ${ing.userOwned})`;
        owned.style.fontWeight = 'bold';
        owned.style.color = ing.userOwned >= ing.quantity ? 'lightgreen' : 'red';

        row.appendChild(left);
        row.appendChild(owned);
        materialBox.appendChild(row);
    });

    // ▶ 전체 스크롤 영역에 append
    scrollBox.appendChild(infoBox);
    scrollBox.appendChild(materialBox);

    // ▶ 버튼
    const buttonRow = document.createElement('div');
    buttonRow.className = 'craft-button-row'; // ✅ 클래스 지정
    buttonRow.style.display = 'flex';
    buttonRow.style.justifyContent = 'center';
    buttonRow.style.gap = '1rem';
    buttonRow.style.marginTop = '1rem';

    const craftBtn = document.createElement('button');
    craftBtn.className = 'craft-button';
    craftBtn.textContent = 'CRAFT';
    const canCraft = recipe.ingredients.every(ing => ing.userOwned >= ing.quantity);
    craftBtn.disabled = !canCraft;
    craftBtn.addEventListener('click', () => {
        if (canCraft) handleCraft(recipe);
    });

    const closeBtn = document.createElement('button');
    closeBtn.className = 'craft-button close';
    closeBtn.textContent = '닫기';
    closeBtn.addEventListener('click', () => {
        document.getElementById('craftModal').classList.add('hidden');
    });

    // ▶ 조립

    container.appendChild(scrollBox);

    buttonRow.appendChild(craftBtn);
    buttonRow.appendChild(closeBtn);
    container.appendChild(buttonRow);
}

function getRarityBackgroundClass(grade) {
    switch (grade?.toUpperCase()) {
        case 'COMMON': return 'rarity-background-common';
        case 'UNCOMMON': return 'rarity-background-uncommon';
        case 'RARE': return 'rarity-background-rare';
        case 'EPIC': return 'rarity-background-epic';
        case 'LEGENDARY': return 'rarity-background-legendary';
        default: return 'rarity-background-common';
    }
}

// 공방 리스트 렌더 영역
function renderStationCategories(stations) {
    categoryContainer.innerHTML = '';

    stations.forEach(st => {
        const categoryDiv = document.createElement('div');
        categoryDiv.className = 'category';
        categoryDiv.dataset.type = st.category;
        categoryDiv.dataset.stationId = st.id;
        categoryDiv.title = st.name; // ✅ 마우스 올리면 툴팁

        const img = document.createElement('img');
        img.src = basePath + st.imagePath || '/images/items/default.png';
        img.alt = st.name;
        img.onerror = () => {
            img.src = '/images/character/default.png';
        };

        categoryDiv.appendChild(img);

        categoryDiv.addEventListener('click', () => {
            playEffect("se_click2");
            setActiveCategory(st.category);
            loadRecipesByStation(st.category,selectedRecipe?.recipeId || null);
        });

        categoryContainer.appendChild(categoryDiv);
    });
}



// 제작 api 호출
async function handleCraft(recipe) {
    playEffect("se_craft")
    if (!currentStationCategory) {
        console.error('station category가 설정되지 않았습니다.');
        return;
    }

    const payload = {
        resultItemId: recipe.resultItemId,
        resultQuantity: 1, // 현재는 고정 1개
        ingredients: recipe.ingredients.map(ing => ({
            itemId: ing.itemId,
            quantity: ing.quantity
        }))
    };

    try {
        const url = '/api/station/craft'
        const res = await apiRequestJson(url, 'POST', payload);

        if (res.code === 'SUCCESS') {
            // ✅ 애니메이션은 즉시 실행 (DOM 조작 없음)
            const btn = document.querySelector('.craft-button');
            if (btn) showCraftSuccessEffect(btn);

            // ✅ 새로고침은 바로 실행
            loadRecipesByStation(currentStationCategory, selectedRecipe?.recipeId);

        } else {
            workshopModal.classList.add('hidden')
            showMessageModal(`제작 실패: ${res.message}`);
        }
    } catch (err) {
        workshopModal.classList.add('hidden')
        showMessageModal('제작 중 오류 발생');
    }
}

function showCraftSuccessEffect(buttonElement, message = '제작 성공!') {
    const floatText = document.createElement('div');
    floatText.className = 'craft-float-text';
    floatText.textContent = message;

    const rect = buttonElement.getBoundingClientRect();
    const modalRect = workshopModal.getBoundingClientRect();

    floatText.style.left = `${rect.left + rect.width / 2 - modalRect.left}px`;
    floatText.style.top = `${rect.top - modalRect.top}px`;
    floatText.style.transform = 'translateX(-50%)'; // 이거 유지!

    const effectLayer = document.getElementById('craft-effect-layer');
    effectLayer.appendChild(floatText);

    setTimeout(() => {
        floatText.remove();
    }, 1000);
}