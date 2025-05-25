
const dexModal = document.getElementById('dexModal');
const dexList = document.getElementById('dexList');
const dexCardTemplate = document.getElementById('dexCardTemplate');
const dexOverlay = document.getElementById('cardOverlay');
const dexPagination = document.getElementById('dexPagination');

let fullDexList = [];
const itemsPerPage = 15;
dexModal.addEventListener('click', (e) => {
    if (e.target.classList.contains('dex-page-btn')) return;
    const inside = e.target.closest('.dex-modal-content');
    if (!inside) dexModal.classList.add('hidden');
});
async function handleDexClick() {
    playEffect("se_click2");
    try {
        const res = await fetch('/api/dex/list');
        const { data } = await res.json();
        // renderDexCardsWithPaging(data, 1);  // 페이징 버전
        renderDexCards(data); // 스크롤 버전
        dexModal.classList.remove('hidden');
        dexOverlay.style.display = 'none';
    } catch (err) {
        showMessageModal('도감 정보를 불러올 수 없습니다.');
        console.error(err);
    }
}

// 3. 도감 카드 렌더링
// function renderDexCardsWithPaging(data, page = 1) {
//     fullDexList = data;
//     const start = (page - 1) * itemsPerPage;
//     const paged = data.slice(start, start + itemsPerPage);
//     renderDexCards(paged);
//     renderPagination(data.length, page);
// }

function renderDexCards(data) {
    dexList.innerHTML = ''; // 기존 카드 제거
    const CharImagePath = '/character/'
    data.forEach(item => {
        const clone = dexCardTemplate.content.cloneNode(true);
        const card = clone.querySelector('.dex-card');
        const frontImg = clone.querySelector('.dex-img');
        const name = clone.querySelector('.dex-name');
        const rank = clone.querySelector('.dex-rank');
        const condition = clone.querySelector('.dex-condition');
        const btn = clone.querySelector('.apply-btn');

        if (!item.owned) card.classList.add('unowned');

        frontImg.src = basePath_image + CharImagePath + `${item.image}`;
        frontImg.alt = item.name;
        if (!item.owned) frontImg.classList.add('unowned');

        name.textContent = item.name;
        rank.textContent = `등급: ${item.rank}`;
        condition.textContent = item.description;
        btn.textContent = item.owned ? '적용하기' : '미획득';
        btn.dataset.dexid = item.id;
        btn.disabled = !item.owned;
        btn.classList.toggle('disabled', !item.owned);

        // 카드 클릭 → 확대
        card.addEventListener('click', () => {
            dexOverlay.innerHTML = '';
            dexOverlay.style.display = 'flex';

            const preview = card.cloneNode(true);
            preview.classList.add('enlarged-preview');

            preview.addEventListener('click', () => {
                preview.classList.toggle('flipped');
            });

            dexOverlay.appendChild(preview);
        });

        dexList.appendChild(clone);

    });
}

// function renderPagination(total, current) {
//     dexPagination.innerHTML = '';
//     const totalPages = Math.ceil(total / itemsPerPage);
//     for (let i = 1; i <= totalPages; i++) {
//         const btn = document.createElement('button');
//         btn.className = 'dex-page-btn';
//         if (i === current) btn.classList.add('active');
//         btn.textContent = i;
//         btn.addEventListener('click', () => renderDexCardsWithPaging(fullDexList, i));
//         dexPagination.appendChild(btn);
//     }
// }

// 도감 미리보기 → 닫기
dexOverlay.addEventListener('click', (e) => {
    playEffect("se_click2");
    // 1. apply-btn 누른 경우 → 적용
    if (e.target.classList.contains('apply-btn') && !e.target.classList.contains('disabled')) {
        const dexId = e.target.dataset.dexid;
        applyDexImage(dexId);
        return;
    }

    // 2. 클릭한 위치가 overlay 자체인 경우만 닫기 (배경 눌렀을 때만)
    if (e.target === dexOverlay) {
        dexOverlay.style.display = 'none';
        const preview = document.querySelector('.enlarged-preview');
        if (preview) preview.remove();
    }
});


// 적용하기 버튼 처리
async function applyDexImage(dexId) {
    try {
        const res = await fetch('/api/char/setImage', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ dexId })
        });
        const result = await res.json();

        if (result.code === 'SUCCESS') {
            showMessageModal('대표 감자가 변경되었습니다!');

            if (result.data?.characterImage) {
                mainCharacter.src = `./images/character/${result.data.characterImage}`;
            }
            dexOverlay.style.display = 'none';
            dexModal.classList.add('hidden');
        } else {
            showMessageModal(result.message || '적용 실패');
        }
    } catch (err) {
        showMessageModal('적용 중 오류 발생');
        console.error(err);
    }
}

// 모달 바깥 클릭 → 닫기
