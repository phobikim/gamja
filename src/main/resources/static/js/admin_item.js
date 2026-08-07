window.onload = async function () {
    await loadItems();
    await loadRecipes();
};
const basePath = 'https://phobi.my/gamja.img';
// 📦 아이템 목록 불러오기
async function loadItems() {
    const res = await fetch("/api/admin/items");
    const items = await res.json();
    const tbody = document.querySelector("#itemTable tbody");
    tbody.innerHTML = items.map(item => `
      <tr id="itemRow_${item.id}">
        <td>${item.id}</td>
        <td><img src="${basePath}/${item.iconPath}" width="32" /></td>
        <td>${item.name}</td>
        <td>${item.rarity}</td>
        <td>${item.itemType}</td>
        <td>${item.equipSlot}</td>
        <td><button onclick="editItem(${item.id})">수정</button></td>
      </tr>
    `).join('');
}

// 🍳 레시피 목록 불러오기
async function loadRecipes() {
    const res = await fetch("/api/admin/recipes");
    const recipes = await res.json();
    const tbody = document.querySelector("#recipeTable tbody");
    tbody.innerHTML = recipes.map(r => `
      <tr id="recipeRow_${r.recipeId}">
        <td>${r.recipeId}</td>
        <td>${r.recipeName}</td>
        <td>${r.grade}</td>
        <td>${r.stationCategory}</td>
        <td><img src="${basePath}/${r.resultItemIcon}" width="32" /><br>${r.resultItemName}</td>
        <td>
          <ul style="padding-left: 0; list-style: none;">
            ${r.ingredients.map(i => `
              <li>
                <img src="${basePath}/${i.itemIcon}" width="24" /> ${i.itemName} x ${i.quantity}
              </li>
            `).join('')}
          </ul>
        </td>
        <td><button onclick='editRecipe(${JSON.stringify(r)})'>수정</button></td>
      </tr>
    `).join('');
}

// 아이템 수정 폼 열기
function editItem(id) {
    fetch("/api/admin/items")
        .then(res => res.json())
        .then(items => {
            const item = items.find(i => i.id === id);
            openItemForm(item, `itemRow_${item.id}`);
        });
}

// 아이템 추가 폼 열기
function openItemForm(item = null, afterRowId = null) {
    const formRow = document.getElementById("itemFormRow");
    const tbody = document.querySelector("#itemTable tbody");

    if (afterRowId) {
        const refRow = document.getElementById(afterRowId);
        refRow.insertAdjacentElement("afterend", formRow);
    } else {
        // 추가용 → 맨 위에 삽입
        const firstRow = tbody.querySelector("tr");
        if (firstRow) {
            firstRow.insertAdjacentElement("beforebegin", formRow);
        } else {
            tbody.appendChild(formRow);
        }
    }
    formRow.classList.remove("hidden");

    if (item) {
        document.getElementById("itemId").value = item.id;
        document.getElementById("itemName").value = item.name;
        document.getElementById("itemDesc").value = item.description || "";
        document.getElementById("itemRank").value = item.rank;
        document.getElementById("itemRarity").value = item.rarity;
        document.getElementById("itemType").value = item.itemType;
        document.getElementById("itemSlot").value = item.equipSlot;
        document.getElementById("itemIcon").value = item.iconPath?.replace("/images/items/", "").replace(".png", "") || "";
    } else {
        document.getElementById("itemFormRow").querySelectorAll("input, select").forEach(el => el.value = "");
        document.getElementById("itemRarity").value = "COMMON";
        document.getElementById("itemType").value = "GATHER_MATERIAL";
        document.getElementById("itemSlot").value = "NONE";
    }
}

// 아이템 저장
async function saveItem() {
    const id = document.getElementById("itemId").value;
    const iconName = document.getElementById("itemIcon").value.trim();
    const iconPath = iconName ? `/images/items/${iconName}.png` : null;

    const data = {
        name: document.getElementById("itemName").value,
        description: document.getElementById("itemDesc").value,
        rank: parseInt(document.getElementById("itemRank").value),
        rarity: document.getElementById("itemRarity").value,
        itemType: document.getElementById("itemType").value,
        equipSlot: document.getElementById("itemSlot").value,
        iconPath: iconPath
    };

    const url = id ? `/api/admin/items/${id}` : "/api/admin/items";
    const method = id ? "PUT" : "POST";

    await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });

    alert("아이템 저장 완료");
    document.getElementById("itemFormRow").classList.add("hidden");
    await loadItems();
}

// 레시피 수정 폼 열기
function editRecipe(recipe) {
    openRecipeForm(recipe, `recipeRow_${recipe.recipeId}`);
}

// 레시피 추가 폼 열기
function openRecipeForm(recipe = null, afterRowId = null) {
    const formRow = document.getElementById("recipeFormRow");
    const tbody = document.querySelector("#recipeTable tbody");

    if (afterRowId) {
        const refRow = document.getElementById(afterRowId);
        refRow.insertAdjacentElement("afterend", formRow);
    } else {
        // 추가용 → 맨 위에 삽입
        const firstRow = tbody.querySelector("tr");
        if (firstRow) {
            firstRow.insertAdjacentElement("beforebegin", formRow);
        } else {
            tbody.appendChild(formRow);
        }
    }
    formRow.classList.remove("hidden");

    if (recipe) {
        document.getElementById("recipeId").value = recipe.recipeId;
        document.getElementById("recipeName").value = recipe.recipeName;
        document.getElementById("recipeGrade").value = recipe.grade;
        document.getElementById("recipeStation").value = recipe.stationCategory || "";
        document.getElementById("resultItemId").value = recipe.resultItemId;
        for (let i = 1; i <= 4; i++) {
            const ing = recipe.ingredients[i - 1];
            document.getElementById(`ingredientItem${i}`).value = ing ? ing.itemId : "";
            document.getElementById(`ingredientQty${i}`).value = ing ? ing.quantity : "";
        }
    } else {
        document.getElementById("recipeFormRow").querySelectorAll("input, select").forEach(el => el.value = "");
        document.getElementById("recipeGrade").value = "COMMON";
    }
}

// 레시피 저장
async function saveRecipe() {
    const id = document.getElementById("recipeId").value;

    const recipe = {
        id: id ? parseInt(id) : null,
        name: document.getElementById("recipeName").value,
        grade: document.getElementById("recipeGrade").value,
        stationCategory: document.getElementById("recipeStation").value,
        resultItemId: parseInt(document.getElementById("resultItemId").value),
        ingredientItemId1: parseInt(document.getElementById("ingredientItem1").value) || null,
        ingredientQuantity1: parseInt(document.getElementById("ingredientQty1").value) || null,
        ingredientItemId2: parseInt(document.getElementById("ingredientItem2").value) || null,
        ingredientQuantity2: parseInt(document.getElementById("ingredientQty2").value) || null,
        ingredientItemId3: parseInt(document.getElementById("ingredientItem3").value) || null,
        ingredientQuantity3: parseInt(document.getElementById("ingredientQty3").value) || null,
        ingredientItemId4: parseInt(document.getElementById("ingredientItem4").value) || null,
        ingredientQuantity4: parseInt(document.getElementById("ingredientQty4").value) || null
    };

    const url = id ? `/api/admin/recipes/${id}` : "/api/admin/recipes";
    const method = id ? "PUT" : "POST";

    await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(recipe)
    });

    alert("레시피 저장 완료");
    document.getElementById("recipeFormRow").classList.add("hidden");
    await loadRecipes();
}
