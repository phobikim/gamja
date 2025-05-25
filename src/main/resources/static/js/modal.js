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

