

const rankModal = document.getElementById('rankModal');
const userId = localStorage.getItem('userId');
const rankListContainer = document.getElementById("rankList");
const template = document.getElementById("rankItemTemplate");
const myContainer = document.getElementById("myRankContainer");

function handleRankClick() {
    rankModal.classList.remove('hidden');
    getRankData();
}


async function getRankData() {
    try {
        playEffect("se_click2");
        const res = await apiRequest(`/api/util/rank`, 'GET');

        if (res.code !== 'SUCCESS' || !res.data) {
            showMessageModal('랭킹 집계 중입니다.');
            return;
        }

        rankListContainer.innerHTML = ''; // 초기화

        res.data.forEach((entry, index) => {
            const clone = template.content.cloneNode(true);
            const rankNumEl = clone.querySelector('.rank-number');
            const rankItem = clone.querySelector('.rank-item');

            const charImagePath = './images/character/'
            const iconImagePath = './images/icons/'


            if (index < 3) {
                const img = document.createElement('img');
                img.src = `${iconImagePath}rank_${index + 1}.png`;
                img.alt = `${index + 1}위`;
                img.className = 'rank-medal';
                rankNumEl.appendChild(img);
            } else {
                rankNumEl.textContent = `${index + 1} 위`;
            }

            // 컬러 클래스 부여
            if (index === 0) rankItem.classList.add('rank-1');
            else if (index === 1) rankItem.classList.add('rank-2');
            else if (index === 2) rankItem.classList.add('rank-3');
            else rankItem.classList.add('default-rank');


            // 각 요소 채우기
            clone.querySelector('.rank-avatar').src = charImagePath + `${entry.characterImage}`;
            clone.querySelector('.rank-username').textContent = entry.username;
            clone.querySelector('.rank-total').textContent = `${entry.total} 점`;


            // 내 랭킹 체크
            if (entry.id && userId && String(entry.id) === String(userId)) {
                rankItem.classList.add('my-rank');

                if (index >= 5) {
                    // 하단 고정용도 복사 생성 (내 점수가 최신 상태 유지)
                    const myFixedClone = clone.cloneNode(true);
                    myFixedClone.querySelector('.rank-item').classList.add('my-rank');

                    myContainer.innerHTML = '';
                    myContainer.appendChild(myFixedClone);
                    myContainer.classList.remove('hidden');
                }
            }

            rankListContainer.appendChild(clone);
        });
    } catch (err) {
        showMessageModal('랭킹 정보를 불러오지 못했습니다.');
        console.error(err);
    }
}


// 모달 바깥 클릭 → 닫기
rankModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.rank-modal-content');
    if (!inside) rankModal.classList.add('hidden');
});

