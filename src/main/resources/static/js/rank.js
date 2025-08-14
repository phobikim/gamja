

const rankModal = document.getElementById('rankModal');
const rankListContainer = document.getElementById("rankList");
const template = document.getElementById("rankItemTemplate");
const myContainer = document.getElementById("myRankContainer");

async function handleRankClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

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
            const rankItem = clone.querySelector('.rank-item');

            rankItem.addEventListener('click', async () => {
                const username = entry.username;
                try {
                    const [basicRes, battleRes] = await Promise.all([
                        fetch(`/api/char/${encodeURIComponent(username)}`),
                        fetch(`/api/char/${encodeURIComponent(username)}/battle`)
                    ]);
                    const basicData = await basicRes.json();
                    const battleData = await battleRes.json();
                    if (basicData.code !== 'SUCCESS' || battleData.code !== 'SUCCESS') {
                        showMessageModal('캐릭터 정보를 불러올 수 없습니다.');
                        return;
                    }
                    const charData = {
                        ...basicData.data,
                        battleStat: battleData.data
                    };

                    openCharacterModal(charData, true);
                } catch (e) {
                    console.error(e);
                    showMessageModal('캐릭터 정보 조회 중 오류가 발생했습니다.');
                }
            });

            const rankNumEl = clone.querySelector('.rank-number');
            const charImagePath = `${basePath_image}`+ /character/;
            const iconImagePath = `${basePath_image}` + /icons/;
            const skinImagePath = `${basePath}`;

            const titleGroupEl = clone.querySelector('.rank-title-group');
            const tierIconEl = clone.querySelector('.rank-tier-icon');
            const titleIconEl = clone.querySelector('.rank-title-icon');
            const titleNameEl = clone.querySelector('.rank-title-name');


            if (entry.titleIconPath || entry.titleName) {
                if (entry.titleIconPath) {
                    titleIconEl.src = `${basePath}${entry.titleIconPath}`;
                    titleIconEl.classList.add('title-icon');
                } else {
                    titleIconEl.remove();
                }

                if (entry.titleName) {
                    titleNameEl.textContent = `[${entry.titleName}]`;
                } else {
                    titleNameEl.remove();
                }
            } else {
                // ✅ 칭호가 아예 없을 경우
                if (titleGroupEl) {
                    titleGroupEl.style.display = 'none';
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
            applyRankAvatarSkin(
                clone,
                charImagePath + `${entry.characterImage}`,
                entry.borderSkinImageUrl ?
                    (entry.borderSkinImageUrl.startsWith('http') ? entry.borderSkinImageUrl : `${basePath}${entry.borderSkinImageUrl}`)
                    : null
            );
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
                    applyRankAvatarSkin(
                        myFixedClone,
                        charImagePath + `${entry.characterImage}`,
                        entry.borderSkinImageUrl ?
                            (entry.borderSkinImageUrl.startsWith('http') ? entry.borderSkinImageUrl : `${basePath}${entry.borderSkinImageUrl}`)
                            : null
                    );
                    myFixedClone.querySelector('.rank-username').textContent = entry.username;
                    myFixedClone.querySelector('.rank-total').textContent = `${entry.total} 점`;

                    const myTitleGroup = myFixedClone.querySelector('.rank-title-group');
                    const myTitleIcon = myFixedClone.querySelector('.rank-title-icon');
                    const myTitleName = myFixedClone.querySelector('.rank-title-name');

                    if (entry.titleIconPath || entry.titleName) {
                        if (entry.titleIconPath && myTitleIcon) {
                            myTitleIcon.src = `${basePath}${entry.titleIconPath}`;
                        } else if (myTitleIcon) {
                            myTitleIcon.remove();
                        }

                        if (entry.titleName && myTitleName) {
                            myTitleName.textContent = `[${entry.titleName}]`;
                        } else if (myTitleName) {
                            myTitleName.remove();
                        }
                    } else {
                        // ✅ 칭호가 없으면 간판 숨기기
                        if (myTitleGroup) {
                            myTitleGroup.style.display = 'none';
                        }
                    }

                    // ✅ 티어 아이콘
                    const myTierIcon = myFixedClone.querySelector('.rank-tier-icon');
                    if (entry.corpsTierIconPath && myTierIcon) {
                        myTierIcon.src = `${basePath}${entry.corpsTierIconPath}`;
                    }

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

function applyRankAvatarSkin(containerEl, imagePath, skinPath) {
    // containerEl: '.rank-item' 클론 루트
    const wrapper = containerEl.querySelector('.rank-avatar-wrapper');
    const avatar  = containerEl.querySelector('.rank-avatar');
    const border  = containerEl.querySelector('.rank-border-skin');

    if (avatar) {
        avatar.src = imagePath || '';

        if (skinPath) {
            // ✅ skin 있을 때
            border.style.backgroundImage = `url('${skinPath}')`;
            avatar.classList.remove('no-skin');
            avatar.classList.add('with-skin');
        } else {
            // ✅ skin 없을 때
            border.style.removeProperty('background-image');
            avatar.classList.remove('with-skin');
            avatar.classList.remove('rank-avatar-wrapper');
            avatar.classList.add('no-skin');
        }
    }
}
