let selectedCharacterId = null;

// 캐릭터 카드 클릭 이벤트
document.querySelectorAll('.character-card').forEach(card => {
    card.addEventListener('click', () => {
        document.querySelectorAll('.character-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        selectedCharacterId = card.dataset.characterId;
        document.getElementById('applyCharacterBtn').disabled = false;
    });
});

// 캐릭터 선택 모달 열기
function openCharacterSelectModal() {
    const modal = document.getElementById('characterSelectModal');
    const grid = document.getElementById('characterCardGrid');
    const applyBtn = document.getElementById('applyCharacterBtn');

    modal.classList.remove('hidden');
    grid.innerHTML = '';
    applyBtn.disabled = true;

    fetchOwnedCharacters();
}

// 보유 캐릭터 불러오기
async function fetchOwnedCharacters() {
    try {
        const res = await apiRequest('/api/char/owned', 'GET');

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || '감자 목록을 불러오지 못했습니다.');
            return;
        }

        renderOwnedCharacterCards(res.data.ownedDexList, res.data.representDex);

        document.getElementById('ownedCount').textContent = res.data.ownedDexCount;
        document.getElementById('totalCount').textContent = res.data.totalDexCount;
    } catch (err) {
        console.error('보유 캐릭터 로딩 실패:', err);
        showMessageModal('서버 오류로 감자 목록을 불러오지 못했습니다.');
    }
}

// 캐릭터 카드 렌더링
function renderOwnedCharacterCards(charList, representDexId) {
    const grid = document.getElementById('characterCardGrid');
    const applyBtn = document.getElementById('applyCharacterBtn');
    grid.innerHTML = '';

    charList.forEach(char => {
        const wrapper = document.createElement('div');
        wrapper.className = 'card-wrapper';

        const card = document.createElement('div');
        card.className = `character-card rarity-background-${char.rarity.toLowerCase()}`;
        card.dataset.characterId = char.dexId;

        card.innerHTML = `
            <div class="card-level">LV.${char.level}</div>
            <div class="card-attribute">${renderAttribute(char.attributeIconPath, char.attribute)}</div>
            <img class="char-image" src="${basePath_image}/character/${char.dexImage}" alt="${char.dexName}">
            <div class="card-info">
                <div class="char-name">${char.dexName}</div>
                <div class="xp-bar-container">
                    <div class="xp-bar" style="width: ${(char.xp / char.maxExp) * 100}%;"></div>
                    <div class="xp-text">${char.xp}/${char.maxExp}</div>
                </div>
                <div class="affinity-text">💛 ${char.affinity}</div>
            </div>
        `;

        card.onclick = () => {
            document.querySelectorAll('.character-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            selectedCharacterId = char.dexId;
            applyBtn.disabled = false;
            flipCardToShowStats(card, char); // 🔄 flip 동작 추가
        };

        if (char.dexId === representDexId) {
            const label = document.createElement('div');
            label.className = 'represent-label-outside';
            label.textContent = '대표';
            wrapper.appendChild(label);
        }

        wrapper.appendChild(card);
        grid.appendChild(wrapper);
    });
}

// 속성 아이콘렌더링
function renderAttribute(iconPath, name) {
    if (!iconPath) return `<span>${name}</span>`;
    return `
        <span class="attr-container" style="display:inline-flex; align-items:center; gap:4px;">
            <img src="${basePath}/${iconPath}" class="attr-icon" alt="${name}" style="width: 20px; height: 20px;">
        </span>
    `;
}

// 대표 감자 설정 버튼 클릭
document.getElementById('applyCharacterBtn').addEventListener('click', () => {
    if (!selectedCharacterId) return;
    closeCharacterSelectModal();
    fetchSetCharacters(selectedCharacterId);
});

// 대표 감자 설정 API 호출
async function fetchSetCharacters(selectedCharacterId) {
    try {
        const res = await apiRequestJson('/api/char/setDex', 'POST', {
            dexId: selectedCharacterId
        });

        if (res.code !== 'SUCCESS') {
            closeCharacterSelectModal();
            showMessageModal(res.message || '대표 감자 설정에 실패했습니다.');
        } else {
            closeCharacterSelectModal();
            await loadCharacterBasicInfo(); // 메인 화면 갱신
        }
    } catch (err) {
        console.error('대표 감자 설정 실패:', err);
        showMessageModal('서버 오류로 대표 감자 설정에 실패했습니다.');
    }
}

// ✅ 캐릭터 카드 클릭 시 뒤집기 & 스탯 보기
function flipCardToShowStats(cardElement, char) {
    const isFlipped = cardElement.classList.contains('flipped');

    if (isFlipped) {
        cardElement.classList.remove('flipped');
        cardElement.innerHTML = `
            <div class="card-level">LV.${char.level}</div>
            <div class="card-attribute">${renderAttribute(char.attributeIconPath, char.attribute)}</div>
            <img class="char-image" src="${basePath_image}/character/${char.dexImage}" alt="${char.dexName}">
            <div class="card-info">
                <div class="char-name">${char.dexName}</div>
                <div class="xp-bar-container">
                    <div class="xp-bar" style="width: ${(char.xp / char.maxExp) * 100}%;"></div>
                    <div class="xp-text">${char.xp}/${char.maxExp}</div>
                </div>
                <div class="affinity-text">💛 ${char.affinity}</div>
            </div>
        `;
    } else {
        // 뒤집기: 카드 구조는 그대로 두고 .card-info 내용만 교체
        cardElement.classList.add('flipped');
        const info = cardElement.querySelector('.card-info');
        if (info) {
            info.innerHTML = `
            <div class="card-back-name-area">${char.dexName}</div>
            <div class="card-back-stats-area">
                ATK ${char.power}<br>
                HP ${char.hp}<br>
                SPEED ${char.speed}
            </div>
        `;
            info.style.fontWeight = 'bold'
            info.style.backgroundColor = '#222';
            info.style.display = 'flex';
            info.style.flexDirection = 'column';
            info.style.justifyContent = 'center';
            info.style.alignItems = 'center';
            info.style.height = '100%';
            info.style.gap = '8px';
            info.style.paddingTop = '20px'; // 상단 여백 추가
        }
    }
}


// 모달 닫기
document.getElementById('closeModalBtn').onclick = () => {
    closeCharacterSelectModal();
};

function closeCharacterSelectModal() {
    document.getElementById('characterSelectModal').classList.add('hidden');
}
