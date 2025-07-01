const chronicleModal = document.getElementById('chronicleModal');
const closeChronicleBtn = document.getElementById('closeChronicleBtn');

// 모달 열기 (어디선가 호출)
function openChronicleModal() {
    battleMapSelectModal.classList.add('hidden');
    chronicleModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';

    // 🧪 예시 카드 삽입 (임시)
    const collectList = document.getElementById('chronicleCollectList');
    const cookList = document.getElementById('chronicleCookList');
    collectList.innerHTML = '';
    cookList.innerHTML = '';

    for (let i = 0; i < 10; i++) {
        const card = document.createElement('div');
        card.className = 'tier-list-card';
        card.innerHTML = `
      <img src="/assets/sample_collect.png" alt="수집품${i}">
      <div class="tier-list-card-name">수집${i+1}</div>
    `;
        collectList.appendChild(card);
    }

    for (let i = 0; i < 5; i++) {
        const card = document.createElement('div');
        card.className = 'tier-list-card';
        card.innerHTML = `
      <img src="/assets/sample_cook.png" alt="요리${i}">
      <div class="tier-list-card-name">요리${i+1}</div>
    `;
        cookList.appendChild(card);
    }
}

closeChronicleBtn.onclick = () => {
    chronicleModal.classList.add('hidden');
    battleMapSelectModal.classList.remove('hidden');
    document.body.style.overflow = '';
};
