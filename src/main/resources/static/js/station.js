
const workshopModal = document.getElementById('workshopModal');
const categoryContainer = document.querySelector('.workshop-left'); // ✅ 정확한 컨테이너
const materialList = document.getElementById('material-list');
const resultImg = document.getElementById('result-img');
const resultName = document.getElementById('result-name');
const craftBtn = document.getElementById('craftButton');

const recipes = {}; // 이후에 채워질 예정
let selectedRecipe = null; // 현재 선택된 레시피
let currentStationCategory = null; // ✅ 현재 선택된 station category 저장용
let matchedRecipe = null;
let matchedCard = null;
workshopModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.workshop-modal-content');
    if (!inside) workshopModal.classList.add('hidden');
});

async function handleStationClick() {
    playEffect("se_click2");
    workshopModal.classList.remove('hidden');
    // 재료 영역 초기화
    const detail = document.getElementById('recipe-detail');
    if (detail) detail.innerHTML = '';

    // 공방 목록 조회
    await getWorkshopStations();
}

// 공방 선택
function setActiveCategory(type) {
    currentStationCategory = type; // ✅ 현재 station category 기억
    document.querySelectorAll('.category').forEach(c => {
        c.classList.toggle('active', c.dataset.type === type);
    });
}

// 공방 List api 호출
async function getWorkshopStations() {
    try {
        const res = await apiRequest(`/api/station/list/${userId}`, 'GET');
        if (res.code === 'SUCCESS') {
            const data = res.data;
            renderStationCategories(data);

            if (data.length > 0) {
                const first = data[0];
                setActiveCategory(first.category);
                loadRecipesByStation(first.category, userId);
            }
        }
    } catch (err) {
        console.error('공방 스테이션 로딩 실패:', err);
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
        img.src = st.imagePath || '/images/items/default.png';
        img.alt = st.name;
        img.onerror = () => {
            img.src = '/images/character/default.png';
        };

        categoryDiv.appendChild(img);

        categoryDiv.addEventListener('click', () => {
            playEffect("se_click2");
            setActiveCategory(st.category);
            loadRecipesByStation(st.category, userId, selectedRecipe?.recipeId || null);
        });

        categoryContainer.appendChild(categoryDiv);
    });
}

// 레시피 api 호출
async function loadRecipesByStation(stationCategory, userId, selectedRecipeId = null) {
    try {
        const res = await apiRequest(`/api/station/recipe/${stationCategory}/${userId}`, 'GET');
        if (res.code === 'SUCCESS') {
            renderRecipeCards(res.data,selectedRecipeId); // RecipeDto[]
        }
    } catch (err) {
        console.error('레시피 불러오기 실패:', err);
    }
}

// 레시피 카드 렌더 영역
function renderRecipeCards(recipes, selectedRecipeId = null) {
    const container = document.getElementById('recipe-card-list');
    container.innerHTML = '';
    let matchedRecipe = null;
    let matchedCard = null;

    // 첫 레시피 자동 선택
    renderRecipeDetail(recipes[0]);
    recipes.forEach(recipe => {
        const card = document.createElement('div');
        card.className = 'recipe-card';
        card.title = recipe.recipeName;

        const img = document.createElement('img');
        img.src = recipe.resultItemIcon || '/images/items/default.png';
        img.alt = recipe.resultItemName;

        const name = document.createElement('span');
        name.className = 'recipe-name';
        name.textContent = recipe.resultItemName;

        const owned = document.createElement('span');
        owned.className = 'recipe-owned';
        // owned.textContent = `보유: ${recipe.resultItemUserOwned}`;

        card.appendChild(img);
        card.appendChild(name);
        card.appendChild(owned);

        // ✅ 클릭 이벤트: active 표시 + 상세 표시
        card.addEventListener('click', () => {
            document.querySelectorAll('.recipe-card').forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            selectedRecipe = recipe;
            renderRecipeDetail(recipe);
        });

        container.appendChild(card);

        if (recipe.recipeId === selectedRecipeId) {
            matchedRecipe = recipe;
            matchedCard = card;
        }
    });
    if (matchedRecipe && matchedCard) {
        matchedCard.classList.add('active');
        renderRecipeDetail(matchedRecipe);
    } else if (recipes.length > 0) {
        // fallback: 첫 번째 선택
        container.firstChild.classList.add('active');
        renderRecipeDetail(recipes[0]);
        selectedRecipe = recipes[0];
    }
}

// 레시피 상세 화면 렌더링
function renderRecipeDetail(recipe) {
    const container = document.getElementById('recipe-detail');
    container.innerHTML = '';

    // ✅ 결과 아이템 제목 영역
    const resultWrapper = document.createElement('div');
    resultWrapper.style.textAlign = 'center';
    resultWrapper.style.marginBottom = '1vw';

    const resultImg = document.createElement('img');
    resultImg.src = recipe.resultItemIcon || '/images/items/default.png';
    resultImg.alt = recipe.resultItemName;
    resultImg.style.width = '48px';
    resultImg.style.height = '48px';

    const title = document.createElement('div');
    title.innerHTML = `${recipe.resultItemName} <span style="font-size: 0.9rem; color: #555;">(보유: ${recipe.resultItemUserOwned})</span>`;
    title.style.fontWeight = 'bold';
    title.style.marginTop = '0.5vw';

    const desc = document.createElement('div');
    desc.textContent = recipe.recipeDescription;
    title.style.fontWeight = 'bold';
    desc.style.fontSize = '0.9rem';
    desc.style.color = '#555';
    desc.style.marginTop = '0.2vw';

    resultWrapper.appendChild(resultImg);
    resultWrapper.appendChild(title);
    resultWrapper.appendChild(desc);


    // 재료 리스트
    const scrollArea = document.createElement('div');
    scrollArea.className = 'ingredient-scroll';

    const list = document.createElement('ul');
    list.style.listStyle = 'none';
    list.style.padding = '0';

    recipe.ingredients.forEach(ing => {
        const li = document.createElement('li');
        li.innerHTML = `
            <img src="${ing.itemIcon}" style="width:24px; height:24px; vertical-align:middle;">
            ${ing.itemName} x${ing.quantity}
            <span style="color:${ing.userOwned < ing.quantity ? 'red' : 'green'};">
              (보유: ${ing.userOwned})
            </span>
        `;
        list.appendChild(li);
    });

    scrollArea.appendChild(list);

    // 버튼
    const button = document.createElement('button');
    button.className = 'craft-button';
    button.textContent = 'CRAFT';
    button.disabled = !recipe.ingredients.every(ing => ing.userOwned >= ing.quantity);

    // 수량 검사
    const canCraft = recipe.ingredients.every(ing => ing.userOwned >= ing.quantity);
    button.disabled = !canCraft;

    button.addEventListener('click', () => {
        if (canCraft) {
            handleCraft(recipe);
        }
    });

    container.appendChild(resultWrapper);
    container.appendChild(scrollArea);
    container.appendChild(button);
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
        const url = `/api/station/craft/${userId}`
        const res = await apiRequestJson(url, 'POST', payload);

        if (res.code === 'SUCCESS') {
            loadRecipesByStation(currentStationCategory, userId, selectedRecipe?.recipeId);
        } else {
            workshopModal.classList.add('hidden')
            showMessageModal(`제작 실패: ${res.message}`);
        }
    } catch (err) {
        workshopModal.classList.add('hidden')
        showMessageModal('제작 중 오류 발생');
    }
}

