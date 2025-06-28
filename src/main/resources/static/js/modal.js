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


function hideItemTooltip() {
    const tooltip = document.getElementById('itemTooltip');
    tooltip.classList.add('hidden');
}

document.getElementById('itemTooltipClose').addEventListener('click', hideItemTooltip);

function showSessionExpiredModal() {
    const modal = document.getElementById('sessionExpiredModal');
    const confirmBtn = document.getElementById('sessionExpiredConfirm');
    modal.classList.remove('hidden');

    confirmBtn.onclick = () => {
        location.href = '/index.html';
    };
}