

const questModal = document.getElementById('questModal');
function handleQuestClick() {
    playEffect("se_click2")
    questModal.classList.remove('hidden');
    getQuestList();
}

questModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.quest-modal-content');
    if (!inside) questModal.classList.add('hidden');
});

function closeQuestModal() {
    document.getElementById('questModal')?.classList.add('hidden');
}

async function getQuestList() {
    try {
        playEffect("se_click2");
        const res = await apiRequest(`/api/util/quest/list`, 'GET');

        if (res.code !== 'SUCCESS' || !res.data) {
            closeQuestModal();
            showMessageModal('퀘스트 리스트 생성중입니다.');
            return;
        }

        const template = document.getElementById('questItemTemplate');
        const questList = document.getElementById('questList');
        questList.innerHTML = ''; // 초기화

        res.data.forEach(quest => {
            const clone = template.content.cloneNode(true);

            // 텍스트 정보 설정
            clone.querySelector('.quest-title').textContent = quest.title;
            clone.querySelector('.quest-desc').textContent = quest.description;
            clone.querySelector('.quest-difficulty').textContent = `난이도: ${quest.difficulty}`;
            clone.querySelector('.quest-reward').textContent = `보상: ${quest.rewardValue} ${quest.rewardType}`;
            clone.querySelector('.quest-progress').textContent = `진행도: ${quest.progress || 0} / ${quest.goalCount}`;


            // 이미지 (임시 기본)
            const img = clone.querySelector('.quest-image img');
            const iconImagePath = './images/icons/'
            img.src = `${iconImagePath}icon_quest.png`;
            img.alt = quest.action;


            // 완료 여부
            if (quest.isCompleted) {
                clone.querySelector('.quest-done').classList.remove('hidden');
            } else {
                const completeArea = clone.querySelector('.quest-complete-area');
                completeArea.classList.remove('hidden');
                completeArea.dataset.id = quest.questId;
                completeArea.addEventListener('click', () => {
                    completeQuest(quest.questId);
                });
            }

            questList.appendChild(clone);
        });

        document.getElementById('questModal').classList.remove('hidden');

    } catch (err) {
        closeQuestModal();
        showMessageModal('퀘스트 정보를 불러오지 못했습니다.');
        console.error(err);
    }
}

async function completeQuest(questId) {
    try {
        const res = await apiRequest(`/api/util/quest/complete?id=${questId}`, 'POST');
        if (res.code === 200) {
            getQuestList(); // 완료 후 목록 새로고침
        } else {
            closeQuestModal();
            showMessageModal(res.message || '완료 처리 실패');
        }
    } catch (err) {
        console.error(err);
        closeQuestModal();
        showMessageModal('퀘스트 완료 처리 중 오류가 발생했습니다.');
    }
}

function updateQuestResetTime() {
    const now = new Date();
    const tomorrow = new Date();
    tomorrow.setHours(0, 0, 0, 0);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const diff = tomorrow - now;
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

    document.getElementById('questResetTime').textContent = `${hours}시간 ${minutes}분`;
}
setInterval(updateQuestResetTime, 60000);
updateQuestResetTime();

