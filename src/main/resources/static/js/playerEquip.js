const equipItemModal = document.getElementById('equipItemModal');
const equipItemList = document.getElementById('equipItemList');
const equipEffectDetail = document.getElementById('equipEffectDetail');
const equipEffectText = document.getElementById('equipEffectText');
const equipItemName = document.getElementById('equipItemName');
let selectedEquipItem = null;
let currentEquipSlot = null;
let currentItemType = null;
let currentItemList = [];

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
    if (window.readOnlyMode) return;
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
    currentItemList = itemList;

    if (itemList.length === 0) {
        const emptyMsg = document.createElement('div');
        emptyMsg.textContent = '아이템이 없습니다';
        emptyMsg.style.gridColumn = '1 / -1'; // 🔥 그리드 전체 너비 차지
        emptyMsg.style.textAlign = 'center';
        emptyMsg.style.padding = '16px';
        emptyMsg.style.color = 'var(--dark-brown-font-color)';
        emptyMsg.style.fontWeight = 'bold';
        emptyMsg.style.fontSize = '0.9rem';
        equipItemList.appendChild(emptyMsg);
        equipEffectDetail.classList.add('hidden');
        return;
    }
    itemList.sort((a, b) => (b.equipped ? 1 : 0) - (a.equipped ? 1 : 0));

    itemList.forEach((item, index) => {
        const div = document.createElement('div');
        div.className = 'equip-item';
        div.style.backgroundImage = `url(${basePath}${item.itemPath})`;

        if (item.quantity) {
            const qtyLabel = document.createElement('div');
            qtyLabel.className = 'equip-quantity';
            qtyLabel.textContent = `x${item.quantity}`;
            div.appendChild(qtyLabel);
        }

        if (item.enhancementLevel && item.enhancementLevel > 0) {
            const enhanceLabel = createEnhanceLabel(item.enhancementLevel);
            div.appendChild(enhanceLabel);
        }

        // if (item.equipped) {
        //     const equipLabel = document.createElement('div');
        //     equipLabel.className = 'equip-equipped-label';
        //     equipLabel.textContent = '장착중';
        //     div.appendChild(equipLabel);
        // }

        div.onclick = () => showEquipEffect(item);
        equipItemList.appendChild(div);

        // ✅ 첫 번째 아이템 자동 선택
        if (index === 0) {
            showEquipEffect(item);
        }
    });
}


function showEquipEffect(item) {
    selectedEquipItem = item;
    equipEffectDetail.classList.remove('hidden');
    equipItemName.textContent = item.itemName;

    // 선택된 아이템 강조
    document.querySelectorAll('.equip-item').forEach(el => el.classList.remove('selected'));
    const allItems = Array.from(document.querySelectorAll('.equip-item'));
    const index = currentItemList.findIndex(i => i.itemId === item.itemId);
    if (index >= 0) {
        allItems[index].classList.add('selected');
    }

    const effects = [];
    if (item.bonusPower) effects.push(`공격력 +${item.bonusPower}`);
    if (item.bonusHp) effects.push(`체력 +${item.bonusHp}`);
    if (item.bonusSpeed) effects.push(`민첩 +${item.bonusSpeed}`);
    if (item.bonusSkillFish) effects.push(`낚시 체력 +${item.bonusSkillFish}`);
    if (item.bonusSkillMining) effects.push(`채광 체력 +${item.bonusSkillMining}`);
    if (item.bonusSkillWoodCutting) effects.push(`벌목 체력 +${item.bonusSkillWoodCutting}`);
    if (item.bonusSkillGathering) effects.push(`채집 체력 +${item.bonusSkillGathering}`);
    if (effects.length === 0) effects.push("효과 없음");

    equipEffectText.innerHTML = ''; // 기존 제거

    // 기본 옵션
    const baseSection = document.createElement('div');
    baseSection.className = 'equip-option-section';
    baseSection.innerHTML = `<div class="equip-option-sticker">기본 옵션</div>`;
    const baseEffects = [];

    if (item.bonusPower) baseEffects.push(`공격력 +${item.bonusPower}`);
    if (item.bonusHp) baseEffects.push(`체력 +${item.bonusHp}`);
    if (item.bonusSpeed) baseEffects.push(`민첩 +${item.bonusSpeed}`);
    if (item.bonusSkillFish) baseEffects.push(`낚시 체력 +${item.bonusSkillFish}`);
    if (item.bonusSkillMining) baseEffects.push(`채광 체력 +${item.bonusSkillMining}`);
    if (item.bonusSkillWoodCutting) baseEffects.push(`벌목 체력 +${item.bonusSkillWoodCutting}`);
    if (item.bonusSkillGathering) baseEffects.push(`채집 체력 +${item.bonusSkillGathering}`);

    if (baseEffects.length === 0) baseEffects.push("효과 없음");

    baseEffects.forEach(effect => {
        const row = document.createElement('div');
        row.className = 'equip-option-row';
        row.textContent = effect;
        baseSection.appendChild(row);
    });
    equipEffectText.appendChild(baseSection);

    // 특수 옵션
    if (item.alchemyOptions && item.alchemyOptions.length > 0) {
        const specialSection = document.createElement('div');
        specialSection.className = 'equip-option-section';
        specialSection.innerHTML = `<div class="equip-option-sticker special">특수 옵션</div>`;

        item.alchemyOptions.forEach(opt => {
            const row = document.createElement('div');
            row.className = 'equip-option-row';
            const label = getOptionLabel(opt.optionType); // 함수 따로 분리
            const suffix = opt.valueType === 'PERCENT' ? '%' : '';
            row.textContent = `${label} +${opt.optionValue}${suffix}`;
            specialSection.appendChild(row);
        });

        equipEffectText.appendChild(specialSection);
    }
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

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || "장착에 실패했습니다.");
            return;
        }
        closeEquipModal();
        await openInfoModal();

    } catch (e) {
        showMessageModal(e.message || "장착 중 오류가 발생했습니다.");
    }
}

function getOptionLabel(type) {
    switch (type) {
        case 'HP': return '체력';
        case 'ATTACK': return '공격력';
        case 'CRIT_RATE': return '치명타 확률';
        case 'CRIT_DMG': return '치명타 피해';
        case 'EXP_GAIN': return '경험치 획득량';
        case 'GOLD_GAIN': return '골드 획득량';
        default: return type;
    }
}

