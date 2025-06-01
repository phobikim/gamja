function showMessageModal(message) {
    const modal = document.getElementById('messageModal');
    const text = document.getElementById('messageText');
    const closeBtn = document.getElementById('messageCloseBtn');

    text.innerHTML = message;
    modal.classList.remove('hidden');

    closeBtn.onclick = () => {
        modal.classList.add('hidden');
    };
}

function showItemTooltip(item) {
    const tooltip = document.getElementById('itemTooltip');
    const nameElem = document.getElementById('tooltipName');
    const rarityElem = document.getElementById('tooltipRarity');
    const descElem = document.getElementById('tooltipDescription');

    nameElem.textContent = item.name || '';
    rarityElem.textContent = `희귀도: ${item.rarity || ''}`;
    descElem.textContent = item.description || '설명이 없습니다.';

    // 중앙 고정이라 위치 계산 X
    tooltip.style.top = '50%';
    tooltip.style.left = '50%';
    tooltip.style.transform = 'translate(-50%, -50%)';

    tooltip.classList.remove('hidden');
}

function hideItemTooltip() {
    const tooltip = document.getElementById('itemTooltip');
    tooltip.classList.add('hidden');
}

