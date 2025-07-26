const workshopSelectModal = document.getElementById('workshopSelectModal');
const craftModal = document.getElementById("craftModal");
const workshopDetailPanel = document.getElementById('workshopDetailPanel');
const closeWorkshopSelect = document.getElementById('closeWorkshopSelect');
const glabRegionTabRow = document.getElementById('glabRegionTabRow');

let selectedStation = null;
let selectedRecipe = null;
let currentWorkshopType = null;
let cachedRecipeList = [];
async function handleStationClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    playEffect("se_click2");
    workshopSelectModal.classList.remove('hidden');
    selectedStation = null;
    selectedRecipe = null;
    // goToWorkshopBtn.disabled = true;
    // goToWorkshopBtn.classList.add('disabled');
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
    const panel = document.getElementById('workshopDetailPanel');
    panel.innerHTML = '';
    stations.forEach(station => {
        const wrapper = document.createElement('div');
        const card = document.createElement('div');
        card.className = 'workshop-card';
        card.dataset.category = station.category;

        const labelHtml = (station.category === 'SHOP')
            ? `<div class="workshop-label">상점</div>`
            : '';

        card.innerHTML = `
          ${labelHtml}
          <img src="${basePath}/${station.imagePath}" class="workshop-thumb" alt="${station.name}" />
        `;

        // 클릭 이벤트는 카드에만
        card.addEventListener('click', () => {
            playEffect("se_click2");
            document.getElementById('workshopSelectModal').classList.add('hidden');

            if (station.category === 'SHOP') {
                openShopModal(); // 상점 열기
            } else {
                openCraftModal(station.category);
            }
        });

        // 이름 div
        const nameDiv = document.createElement('div');
        nameDiv.className = 'workshop-name';
        nameDiv.textContent = station.name;

        wrapper.appendChild(card);
        wrapper.appendChild(nameDiv);

        panel.appendChild(wrapper);
    });
}



async function openCraftModal(stationCategory, preselectedRecipe = null) {
    const valid = await checkSessionValid();
    if (!valid) return;

    selectedStation = stationCategory;
    document.getElementById('craftModal').classList.remove('hidden');

    const isBattle = stationCategory === 'BATTLE';
    const isGlab = stationCategory === 'GLAB';

    document.getElementById('equipmentTabRow').style.display = isBattle ? 'flex' : 'none';
    document.getElementById('glabRegionTabRow').style.display = isGlab ? 'flex' : 'none';

    if (stationCategory === 'GLAB') {
        document.querySelectorAll('#glabRegionTabRow .craft-tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelector('#glabRegionTabRow .craft-tab-btn[data-map-id="1"]')?.classList.add('active');
    }

    fetchRecipes(stationCategory, preselectedRecipe);
}

document.querySelectorAll('#equipmentTabRow .craft-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('#equipmentTabRow .craft-tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        currentEquipFilter = btn.dataset.type;
        const filtered = cachedRecipeList.filter(r => r.slotType === currentEquipFilter);
        renderRecipeList(filtered);
    });
});

document.querySelectorAll('#glabRegionTabRow .craft-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('#glabRegionTabRow .craft-tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const selectedMapId = parseInt(btn.dataset.mapId);
        const filtered = cachedRecipeList.filter(r => r.chronicleMapId === selectedMapId);
        renderRecipeList(filtered);
    });
});

async function fetchRecipes(stationCategory, preselectedRecipe = null) {
    try {
        const res = await apiRequest(`/api/station/${stationCategory}/recipe`, 'POST');
        if (res.code === 'SUCCESS') {
            cachedRecipeList = res.data;
            renderRecipeList(res.data, preselectedRecipe);
        }
    } catch (err) {
        console.error("제작 리스트 불러오기 실패:", err);
    }
}

function renderRecipeList(recipeList, preselectedRecipe = null) {
    const container = document.getElementById('craftRecipeList');
    container.innerHTML = '';

    let filteredList = recipeList;
    if (selectedStation === 'BATTLE') {
        const activeTab = document.querySelector('#equipmentTabRow .craft-tab-btn.active');
        if (activeTab) {
            const type = activeTab.dataset.type;
            recipeList = recipeList.filter(r => r.slotType === type);
        }
    } else if (selectedStation === 'GLAB') {
        const activeTab = document.querySelector('#glabRegionTabRow .craft-tab-btn.active');
        if (activeTab) {
            const mapId = parseInt(activeTab.dataset.mapId);
            recipeList = recipeList.filter(r => r.chronicleMapId === mapId);
        }
    }

    let firstCard = null;
    let selectedCard = null;

    recipeList.forEach(recipe => {
        const card = document.createElement('div');
        card.className = `recipe-card`;
        // ▶ 분리된 이미지 아이콘
        const icon = document.createElement('img');
        icon.src = `${basePath}${recipe.resultItemIcon}`;
        icon.alt = recipe.resultItemName;
        icon.className = 'recipe-icon';

        // ▶ 분리된 이름 요소
        const name = document.createElement('div');
        name.className = 'recipe-name';
        name.textContent = recipe.resultItemName;

        // ▶ 카드 조립
        card.appendChild(icon);
        card.appendChild(name);

        // ▶ 클릭 이벤트
        card.addEventListener('click', () => {
            document.querySelectorAll('.recipe-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            selectedRecipe = recipe;
            renderRecipeDetail(recipe);
        });

        container.appendChild(card);

        if (!firstCard) firstCard = card;

        if (preselectedRecipe && recipe.recipeId === preselectedRecipe.recipeId) {
            selectedCard = card;
            selectedRecipe = recipe;
        }
    });

    if (selectedCard) {
        selectedCard.classList.add('active');
        renderRecipeDetail(selectedRecipe);
    } else if (firstCard) {
        firstCard.classList.add('active');
        selectedRecipe = recipeList[0];
        renderRecipeDetail(recipeList[0]);
    }
}



function renderRecipeDetail(selectedRecipe) {
    const container = document.getElementById('craftDetailPanel');
    container.innerHTML = '';

    const scrollBox = document.createElement('div');
    scrollBox.className = 'craft-detail-scroll';

    // ▶ 아이템 정보
    const infoBox = document.createElement('div');
    infoBox.className = 'detail-info-box';

    const icon = document.createElement('img');
    icon.src = basePath + selectedRecipe.resultItemIcon;
    icon.alt = selectedRecipe.resultItemName;
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
    name.innerHTML = `${selectedRecipe.resultItemName} <span style="font-size: 0.85rem; color: #ccc;">(보유: ${selectedRecipe.resultItemUserOwned})</span>`;

    const statRow = document.createElement('div');
    statRow.className = 'recipe-stat-row';

    const atk = selectedRecipe.basePower ?? 0;
    const hp = selectedRecipe.baseHp ?? 0;
    const spd = selectedRecipe.baseSpeed ?? 0;

    if (atk !== 0 || hp !== 0 || spd !== 0) {
        if (atk !== 0) statRow.innerHTML += `<span class="recipe-stat-label">ATK</span><span class="recipe-stat-value">${atk}</span>`;
        if (hp !== 0) statRow.innerHTML += `<span class="recipe-stat-label">HP</span><span class="recipe-stat-value">${hp}</span>`;
        if (spd !== 0) statRow.innerHTML += `<span class="recipe-stat-label">SPD</span><span class="recipe-stat-value">${spd}</span>`;
        infoBox.appendChild(statRow);
    }

    const desc = document.createElement('div');
    desc.className = 'detail-desc';
    desc.style.fontSize = '0.85rem';
    desc.style.color = '#ccc';
    desc.style.textAlign = 'center';
    desc.style.marginTop = '0.5rem';
    desc.style.fontWeight = 'bold';
    desc.textContent = selectedRecipe.recipeDescription;

    infoBox.appendChild(icon);
    infoBox.appendChild(name);
    if (statRow.innerHTML) infoBox.appendChild(statRow);
    infoBox.appendChild(desc);
    scrollBox.appendChild(infoBox);

    // ▶ 재료 리스트
    const materialBox = document.createElement('div');
    materialBox.className = 'ingredient-list';

    // 재료 row 캐시용
    const materialRows = [];

    selectedRecipe.ingredients.forEach(ing => {
        const row = document.createElement('div');
        row.style.display = 'flex';
        row.style.flexDirection = 'column'; // ← 수직 정렬로 변경
        row.style.margin = '0.3rem 0';
        row.style.fontSize = '0.85rem';
        row.style.color = 'var(--dark-1-font-color)';

        const topRow = document.createElement('div');
        topRow.style.display = 'flex';
        topRow.style.alignItems = 'center';
        topRow.style.justifyContent = 'space-between';

        const left = document.createElement('div');
        left.style.display = 'flex';
        left.style.alignItems = 'center';
        left.style.gap = '0.5rem';

        const ingImg = document.createElement('img');
        ingImg.src = basePath + ing.itemIcon;
        ingImg.style.width = '32px';
        ingImg.style.height = '32px';
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

        topRow.appendChild(left);
        topRow.appendChild(owned);
        row.appendChild(topRow);

        // 획득경로 표시
        if (ing.condition) {
            const conditionDiv = document.createElement('div');
            conditionDiv.className = 'ingredient-condition-box';
            conditionDiv.textContent = ing.condition;
            row.appendChild(conditionDiv);
        }

        materialBox.appendChild(row);

        materialRows.push({ row, ing, ingText, owned });
    });

    scrollBox.appendChild(materialBox);
    // ▶ 최대 제작 수량 계산
    let maxCraftCount = Infinity;
    selectedRecipe.ingredients.forEach(ing => {
        const possible = Math.floor(ing.userOwned / ing.quantity);
        if (possible < maxCraftCount) maxCraftCount = possible;
    });
    if (maxCraftCount < 1) maxCraftCount = 1;

    // ▶ 수량 슬라이더
    const quantityBox = document.createElement('div');
    quantityBox.className = 'quantity-control';
    quantityBox.style.marginTop = '1rem';
    quantityBox.style.padding = '0 1rem';
    quantityBox.style.textAlign = 'center';

    const quantityLabel = document.createElement('div');
    quantityLabel.className = 'quantity-label';
    quantityLabel.innerHTML = `제작 수량: <span id="craftQuantity">1</span>`;
    quantityLabel.style.fontWeight = 'bold';
    quantityLabel.style.marginBottom = '0.5rem';
    quantityLabel.style.color = 'var(--dark-1-font-color)';

    const slider = document.createElement('input');
    slider.type = 'range';
    slider.min = 1;
    slider.max = maxCraftCount;
    slider.value = 1;
    slider.id = 'craftSlider';
    slider.style.width = '100%';

    // ▶ 슬라이더 이벤트 - 수량, 재료 실시간 반영
    slider.addEventListener('input', () => {
        const count = parseInt(slider.value);
        document.getElementById('craftQuantity').textContent = count;

        materialRows.forEach(({ ing, ingText, owned }) => {
            const totalNeed = ing.quantity * count;
            ingText.textContent = `${ing.itemName} ×${totalNeed}`;
            owned.style.color = ing.userOwned >= totalNeed ? 'lightgreen' : 'red';
        });
    });

    quantityBox.appendChild(quantityLabel);
    quantityBox.appendChild(slider);
    scrollBox.appendChild(quantityBox);

    // ▶ 제작 & 닫기 버튼
    const buttonRow = document.createElement('div');
    buttonRow.className = 'craft-button-row';
    buttonRow.style.display = 'flex';
    buttonRow.style.justifyContent = 'center';
    buttonRow.style.gap = '1rem';
    buttonRow.style.marginTop = '1rem';

    const craftBtn = document.createElement('button');
    craftBtn.className = 'craft-button';
    craftBtn.textContent = 'CRAFT';

    const canCraft = selectedRecipe.ingredients.every(ing => ing.userOwned >= ing.quantity);
    craftBtn.disabled = !canCraft;

    craftBtn.addEventListener('click', () => {
        const count = parseInt(document.getElementById('craftSlider')?.value || '1');
        if (canCraft) handleCraft(selectedRecipe, count);
    });

    const closeBtn = document.createElement('button');
    closeBtn.className = 'craft-button close';
    closeBtn.textContent = '닫기';
    closeBtn.addEventListener('click', () => {
        document.getElementById('craftModal').classList.add('hidden');
    });

    buttonRow.appendChild(craftBtn);
    buttonRow.appendChild(closeBtn);

    container.appendChild(scrollBox);
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


// 제작 api 호출
async function handleCraft(selectedRecipe, quantity = 1) {
    if (!selectedRecipe || !selectedStation) {
        console.error('station category 또는 recipe가 설정되지 않았습니다.');
        return;
    }

    playEffect("se_craft")

    const count = quantity; // 또는 외부에서 받은 quantity 그대로 사용

    const payload = {
        resultItemId: selectedRecipe.resultItemId,
        resultQuantity: quantity
    };

    try {
        const url = `/api/station/${selectedStation}/craft`;
        const res = await apiRequestJson(url, 'POST', payload);

        if (res.code === 'SUCCESS') {
            showCraftSuccessEffect();
        } else {
            showMessageModal(`제작 실패`);
        }
    } catch (err) {
        showMessageModal('제작 중 오류 발생');
    }
}

function showCraftSuccessEffect(message = '제작 성공!') {
    const container = document.getElementById('craftDetailPanel');
    if (!container) return;

    const floatText = document.createElement('div');
    floatText.className = 'craft-float-text';
    floatText.textContent = message;

    floatText.style.position = 'absolute';
    floatText.style.left = '50%';
    floatText.style.top = '50%';
    floatText.style.transform = 'translate(-50%, -50%)';

    container.appendChild(floatText);

    setTimeout(() => {
        floatText.remove();
        fetchRecipes(selectedStation, selectedRecipe);
    }, 1000);
}