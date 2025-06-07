const equipItemModal = document.getElementById('equipItemModal');
const equipItemList = document.getElementById('equipItemList');
const equipEffectDetail = document.getElementById('equipEffectDetail');
const equipEffectText = document.getElementById('equipEffectText');
const equipItemName = document.getElementById('equipItemName');
let selectedEquipItem = null;
let currentEquipSlot = null;
let currentItemType = null;

// 슬롯 클릭 이벤트 공통 연결
document.querySelectorAll('#combatEquipment .inventoryCell, #lifeEquipment .inventoryCell').forEach(slot => {
    slot.onclick = () => {
        const itemType = battleTabBtn.classList.contains('active') ? 'EQUIP_BATTLE' : 'EQUIP_GATHER';
        const equipSlot = slot.id.replace(/^combatSlot|^lifeSlot/, '');
        currentEquipSlot = equipSlot;
        currentItemType = itemType;
        openEquipModal(currentItemType, currentEquipSlot);
    };
});

async function openEquipModal(itemType, equipSlot) {
    playEffect("se_click2");
    equipItemModal.classList.remove('hidden');

    try {
        const res = await apiRequestJson('/api/char/get-equip-items', 'POST', {
            itemType,
            equipSlot
        });

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "장착 가능한 아이템을 불러오지 못했습니다.");
            return;
        }

        renderEquipItemList(res.data);

    } catch (err) {
        showMessageModal("아이템 요청 실패");
    }
}


function closeEquipModal() {
    equipItemModal.classList.add('hidden');
    equipEffectDetail.classList.add('hidden');
    selectedEquipItem = null;
    currentEquipSlot = null;
}
function renderEquipItemList(itemList) {
    equipItemList.innerHTML = '';

    itemList.forEach(item => {
        const div = document.createElement('div');
        div.className = 'potion-item';
        div.style.backgroundImage = `url(${basePath}${item.itemPath})`;

        const qtyLabel = document.createElement('div');
        qtyLabel.className = 'potion-quantity';
        qtyLabel.textContent = `x${item.quantity}`;
        div.appendChild(qtyLabel);

        div.onclick = () => showEquipEffect(item);
        equipItemList.appendChild(div);
    });
}


function showEquipEffect(item) {
    selectedEquipItem = item;
    equipEffectDetail.classList.remove('hidden');
    equipItemName.textContent = item.itemName;

    const effects = [];
    if (item.bonusPower) effects.push(`🗡️ 공격력 +${item.bonusPower}`);
    if (item.bonusHp) effects.push(`🩸 체력 +${item.bonusHp}`);
    if (item.bonusSpeed) effects.push(`⚡ 민첩 +${item.bonusSpeed}`);
    if (effects.length === 0) effects.push("효과 없음");

    equipEffectText.textContent = effects.join(' / ');
}


async function equipSelectedItem() {
    if (!selectedEquipItem || !currentEquipSlot) {
        showMessageModal("아이템을 선택해주세요!");
        return;
    }
    try {
        const res = await apiRequestJson('/api/char/set-equip-items', 'POST', {
            itemId: selectedEquipItem.itemId,
            itemType: currentItemType,
            equipSlot: currentEquipSlot
        });

        closeEquipModal();
        openInfoModal();

    } catch (e) {
        showMessageModal("장착 중 오류가 발생했습니다.");
    }
}

function updatePotionCountLabel(quantity) {
    const label = document.getElementById('potionCountLabel');
    if (quantity > 0) {
        label.textContent = `x${quantity}`;
        label.classList.remove('hidden');
    } else {
        label.classList.add('hidden');
    }
}