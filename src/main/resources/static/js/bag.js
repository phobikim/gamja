

const bagModal = document.getElementById('bagModal');

bagModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.bag-modal-content');
    if (!inside) bagModal.classList.add('hidden');
});
async function loadBagItems() {
    const bagList = document.getElementById('bagList');
    bagList.innerHTML = '';

    // 1. 빈칸 먼저 만들기
    const slots = [];
    for (let i = 0; i < 30; i++) {
        const li = document.createElement('li');
        li.className = 'bag-slot';
        slots.push(li);
        bagList.appendChild(li);
    }
    try {
        const res = await apiRequest(`/api/util/item/list/${userId}`, 'GET');

        if (res.code === 'SUCCESS') {
            const data = res.data;
            let insertIndex = 0;

            // 제외 항목 제외하고 처리
            Object.entries(data).forEach(([key, value]) => {
                if (key === 'id' || key === 'money' || value === 0) return;
                if (insertIndex >= 50) return; // 슬롯 초과 방지

                const slot = slots[insertIndex];
                slot.innerHTML = ''; // 기존 초기화

                // 이미지 wrapper 생성
                const wrapper = document.createElement('div');
                wrapper.className = 'item-image-wrapper';

                const img = document.createElement('img');
                img.src = `/images/items/i_${key}.png`;
                img.alt = key;
                img.className = 'item-icon';
                wrapper.appendChild(img); // 이미지 wrapper 안에 이미지 삽입

                // (선택) 이미지가 없을 경우 fallback 처리
                img.onerror = () => {
                    img.src = '/images/character/default.png';
                };

                const span = document.createElement('span');
                span.className = 'item-count';
                span.textContent = `${value}`;

                slot.appendChild(wrapper); // wrapper 삽입
                slot.appendChild(span);

                insertIndex++;
            });
        }
    } catch (err) {
        console.error('가방 아이템 로딩 실패:', err);
    }

}

async function handleBagClick() {
    playEffect("se_click2")
    bagModal.classList.remove('hidden');
    await loadBagItems();
}