let selectedCharacterId = null;

// 캐릭터 카드 클릭 이벤트
document.querySelectorAll('.character-card').forEach(card => {
    card.addEventListener('click', () => {
        // 기존 선택 해제
        document.querySelectorAll('.character-card').forEach(c => c.classList.remove('selected'));

        // 새로운 선택
        card.classList.add('selected');
        selectedCharacterId = card.dataset.characterId;

        // 적용 버튼 활성화
        document.getElementById('applyCharacterBtn').disabled = false;
    });
});


// 보유 캐릭터를 받아서 카드 렌더링
function openCharacterSelectModal() {
    const modal = document.getElementById('characterSelectModal');
    const grid = document.getElementById('characterCardGrid');
    const applyBtn = document.getElementById('applyCharacterBtn');

    modal.classList.remove('hidden');
    grid.innerHTML = '';
    applyBtn.disabled = true;

    fetchOwnedCharacters();
}


async function fetchOwnedCharacters() {
    try {
        const res = await apiRequest('/api/char/owned', 'GET');

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || '캐릭터 목록을 불러오지 못했습니다.');
            return;
        }

        renderOwnedCharacterCards(res.data.ownedDexList);
    } catch (err) {
        console.error('보유 캐릭터 로딩 실패:', err);
        showMessageModal('서버 오류로 캐릭터 목록을 불러오지 못했습니다.');
    }
}

function renderOwnedCharacterCards(charList) {
    const grid = document.getElementById('characterCardGrid');
    const applyBtn = document.getElementById('applyCharacterBtn');
    const template = document.getElementById('characterCardTemplate');
    // 기존 카드 초기화
    grid.innerHTML = '';

    charList.forEach(char => {
        const card = document.createElement('div');
        card.className = `character-card rarity-background-${char.rarity.toLowerCase()}`;
        card.dataset.characterId = char.dexId;

        card.innerHTML = `
                    <div class="card-level">LV.${char.level}</div>
                    <div class="card-attribute ${char.attribute}">${getAttributeIcon(char.attribute)}</div>
                    <img class="char-image" src="${basePath_image}/character/${char.dexImage}" alt="${char.dexName}">
                    <div class="card-info">
                        <div class="char-name">${char.dexName}</div>
                        <div class="xp-bar-container">
                            <div class="xp-bar" style="width: ${(char.xp / char.maxExp) * 100}%;"></div>
                            <div class="xp-text">${char.xp}/${char.maxExp}</div>
                        </div>
                    </div>
                `;

        card.onclick = () => {
            document.querySelectorAll('.character-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            selectedCharacterId = char.dexId; // characterDexId → dexId로 변경
            applyBtn.disabled = false;
        };


        grid.appendChild(card);
        document.getElementById('ownedCount').textContent = charList.length;
        document.getElementById('totalCount').textContent = '120'; // 또는 서버에서 받은 총 개수
    });
}

// 속성 아이콘 반환 함수
function getAttributeIcon(attribute) {
    const icons = {
        fire: '🔥',
        water: '💧',
        earth: '🌍',
        air: '💨',
        light: '✨',
        dark: '🌙'
    };
    return icons[attribute] || '⚡';

}




document.getElementById('applyCharacterBtn').addEventListener('click', () => {
    if (!selectedCharacterId) return;

    closeCharacterSelectModal();
    fetchSetCharacters(selectedCharacterId);
});

async function fetchSetCharacters(selectedCharacterId) {
    try {
        const res = await apiRequestJson('/api/char/setDex', 'POST', selectedCharacterId);

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || '대표 캐릭터 설정에 실패했습니다.');
            await fetchOwnedCharacters();
        }

    } catch (err) {
        console.error('보유 캐릭터 로딩 실패:', err);
        showMessageModal('서버 오류로 대표 캐릭터 설정에 실패했습니다.');
    }
}

document.getElementById('closeModalBtn').onclick = () => {
    document.getElementById('characterSelectModal').classList.add('hidden');
};

function closeCharacterSelectModal() {
    document.getElementById('characterSelectModal').classList.add('hidden');
}