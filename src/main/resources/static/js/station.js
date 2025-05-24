
const workshopModal = document.getElementById('workshopModal');
const categoryContainer = document.querySelector('.workshop-left'); // ✅ 정확한 컨테이너
const materialList = document.getElementById('material-list');
const resultImg = document.getElementById('result-img');
const resultName = document.getElementById('result-name');
const craftBtn = document.getElementById('craftButton');

const recipes = {}; // 이후에 채워질 예정
let currentType = null;


// 공방 선택
function setActiveCategory(type) {
    playEffect("se_click")
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
            setActiveCategory(st.category);
            loadRecipesByStation(st.category, userId);
        });

        categoryContainer.appendChild(categoryDiv);
    });
}

// 레시피 api 호출
async function loadRecipesByStation(stationCategory) {
    try {
        const res = await apiRequest(`/api/station/recipe/${stationCategory}/${userId}`, 'GET');
        if (res.code === 'SUCCESS') {
            renderRecipeCards(res.data); // RecipeDto[]
        }
    } catch (err) {
        console.error('레시피 불러오기 실패:', err);
    }
}

// 레시피 카드 렌더 영역
function renderRecipeCards(recipes) {
    const container = document.getElementById('recipe-card-list');
    container.innerHTML = '';

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
            renderRecipeDetail(recipe);
        });

        container.appendChild(card);


    });
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
            showMessageModal("제작하시겠어요?")
        }
    });

    container.appendChild(resultWrapper);
    container.appendChild(scrollArea);
    container.appendChild(button);
}

craftBtn.addEventListener('click', () => {
    if (canCraft) {
        handleCraft(recipe);
    }
});
async function handleCraft(recipe) {
    const payload = {
        resultItemId: recipe.resultItemId,
        resultQuantity: 1, // 현재는 고정 1개
        ingredients: recipe.ingredients.map(ing => ({
            itemId: ing.itemId,
            quantity: ing.quantity
        }))
    };

    try {
        const res = await apiRequestJson(`/api/station/userId/craft/${userId}`, 'POST', payload);

        if (res.code === 'SUCCESS') {
            showMessageModal('제작 성공');
            // ✅ 제작 후 다시 레시피 목록을 새로고침하거나 인벤토리 갱신 필요 시:
            // loadRecipesByStation(currentType, userId);
        } else {
            showMessageModal(`제작 실패: ${res.message}`);
        }
    } catch (err) {
        showMessageModal('제작 중 오류 발생');
    }
}

