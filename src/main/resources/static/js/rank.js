

const rankModal = document.getElementById('rankModal');
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

            const charImagePath = `${basePath_image}`+ /character/;
            const iconImagePath = `${basePath_image}` + /icons/;
            const tierIconEl = clone.querySelector('.rank-tier-icon');

            const titleIconEl = clone.querySelector('.rank-title-icon');

            if (entry.titleIconPath) {
                titleIconEl.src = `${basePath}${entry.titleIconPath}`;
                titleIconEl.classList.add('title-icon');
            } else {
                titleIconEl.remove();
            }

            const titleNameEl = clone.querySelector('.rank-title-name');
            if (titleNameEl) {
                if (entry.titleName) {
                    titleNameEl.textContent = `[${entry.titleName}]`;  // ✅ 대괄호 추가
                    titleNameEl.classList.add('rank-rank-title-name');
                } else {
                    titleNameEl.remove();
                }
            }

            tierIconEl.src = `${basePath}` + entry.corpsTierIconPath;
            tierIconEl.classList.add('tier-icon');

            if (index < 3) {
                const img = document.createElement('img');
                img.src = `${iconImagePath}rank_${index + 1}.png`;
                img.alt = `${index + 1}위`;
                img.className = 'rank-medal';
                rankNumEl.appendChild(img);
            } else {
                rankNumEl.textContent = `${index + 1} 위`;
            }

           // 각 요소 채우기
            clone.querySelector('.rank-avatar').src = charImagePath + `${entry.characterImage}`;
            clone.querySelector('.rank-username').textContent = entry.username;
            clone.querySelector('.rank-total').textContent = `${entry.total} 점`;


            // 내 랭킹 체크
            if (entry.mine) {
                rankItem.classList.add('my-rank');

                if (index >= 4) {
                    const myFixedClone = template.content.cloneNode(true);
                    const myFixedItem = myFixedClone.querySelector('.rank-item');
                    myFixedItem.classList.add('my-rank');
                    myFixedClone.querySelector('.rank-number').textContent = `${index + 1} 위`;
                    myFixedClone.querySelector('.rank-avatar').src = charImagePath + `${entry.characterImage}`;
                    myFixedClone.querySelector('.rank-username').textContent = entry.username;
                    myFixedClone.querySelector('.rank-total').textContent = `${entry.total} 점`;
                    myContainer.innerHTML = '';
                    myContainer.appendChild(myFixedClone);
                    myContainer.classList.remove('hidden');
                }
            }

            // 내 랭킹 고정 제거 로직
            if (entry.mine && index < 4) {
                myContainer.innerHTML = '';
                myContainer.classList.add('hidden');
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

