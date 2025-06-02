const mapModal = document.getElementById('mapSelectModal');

function openMapModal() {
    mapModal.classList.remove('hidden');
}
mapModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.spot-select-modal-content');
    if (!inside) mapModal.classList.add('hidden');
});

function closeMapModal() {
    document.getElementById('mapSelectModal').classList.add('hidden');
}
