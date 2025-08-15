const settingModal     = document.getElementById('settingModal');
// 배경
const bgGrid           = document.getElementById('bgGrid');
const applyBgBtn       = document.getElementById('applyBgBtn');

// 테두리
const borderGrid       = document.getElementById('borderGrid');
const applyBorderBtn   = document.getElementById('applyBorderBtn');

// 상태
let selectedBgId = null;
let selectedBorderId = null;

let ownedBgMap = {};     // { [bgId]: boolean }
let ownedBorderMap = {}; // { [borderId]: boolean }

async function fetchBackgroundSkins(){
    return apiRequest('/api/skin/background/list', 'GET');
}

async function selectBackgroundSkin(backgroundId){
    return apiRequestJson('/api/skin/background/select', 'POST', { backgroundId });
}

async function fetchBorderSkins(){
    return apiRequest('/api/skin/border/list', 'GET'); // res.code, res.data[]
}

async function selectBorderSkin(borderId){
    return apiRequestJson('/api/skin/border/select', 'POST', { borderId });
}

function renderSkinGrid(containerEl, list, ownedMap, type){
    containerEl.innerHTML = '';
    Object.keys(ownedMap).forEach(k => delete ownedMap[k]);

    if (type === 'border') {
        const noneCard = document.createElement('div');
        noneCard.className = 'skin-card skin-none-card';
        noneCard.dataset.skinId = '';
        noneCard.dataset.skinType = 'border';

        // 아이콘 + 텍스트
        const noneWrap = document.createElement('div');
        noneWrap.className = 'skin-none-wrap';

        const icon = document.createElement('div');
        icon.className = 'skin-none-icon';
        icon.textContent = '✕';

        const label = document.createElement('div');
        label.className = 'skin-none-label';
        label.textContent = '적용 안함';

        noneWrap.appendChild(icon);
        noneWrap.appendChild(label);
        noneCard.appendChild(noneWrap);

        noneCard.addEventListener('click', () => {
            document.querySelectorAll('#' + containerEl.id + ' .skin-card')
                .forEach(c => c.classList.remove('selected'));
            noneCard.classList.add('selected');
            selectedBorderId = null;
            applyBorderBtn.disabled = false;
        });

        containerEl.appendChild(noneCard);
    }

    list.forEach(item => {
        ownedMap[item.id] = item.owned;

        const card = document.createElement('div');
        card.className = 'skin-card';
        card.dataset.skinId = String(item.id);
        card.dataset.skinType = type;

        const img = document.createElement('img');
        img.src = basePath + item.imageUrl; // 공통 imageUrl 사용
        img.alt = item.name || (type === 'bg' ? '배경' : '테두리');

        card.appendChild(img);

        if (!item.owned) {
            const overlay = document.createElement('div');
            overlay.className = 'skin-overlay';
            overlay.textContent = '미보유';
            card.appendChild(overlay);
        }

        card.addEventListener('click', () => handleCardSelect(card, ownedMap));
        containerEl.appendChild(card);
    });
}

function handleCardSelect(cardEl, ownedMap){
    const skinId = Number(cardEl.dataset.skinId);
    const skinType = cardEl.dataset.skinType;

    if (!ownedMap[skinId]) return; // 미보유 시 무시

    // 동일 섹션의 선택만 초기화
    const container = skinType === 'bg' ? bgGrid : borderGrid;
    container.querySelectorAll('.skin-card').forEach(c => c.classList.remove('selected'));
    cardEl.classList.add('selected');

    if (skinType === 'bg'){
        selectedBgId = skinId;
        applyBgBtn.disabled = !selectedBgId || !ownedBgMap[selectedBgId];
    } else {
        selectedBorderId = skinId;
        applyBorderBtn.disabled = !selectedBorderId || !ownedBorderMap[selectedBorderId];
    }
}

document.getElementById('playerInfoCard').addEventListener('click', async () => {
    const valid = await checkSessionValid();
    if (!valid) return;

    selectedBgId = null;
    selectedBorderId = null;
    applyBgBtn.disabled = true;
    applyBorderBtn.disabled = true;

    try {
        const [bgRes, borderRes] = await Promise.all([
            fetchBackgroundSkins(),
            fetchBorderSkins()
        ]);

        if (bgRes.code === 'SUCCESS') {
            renderSkinGrid(bgGrid, bgRes.data || [], ownedBgMap, 'bg');
        } else {
            bgGrid.innerHTML = '<div class="skin-empty">배경 데이터를 불러오지 못했습니다.</div>';
        }

        if (borderRes.code === 'SUCCESS') {
            renderSkinGrid(borderGrid, borderRes.data || [], ownedBorderMap, 'border');
        } else {
            borderGrid.innerHTML = '<div class="skin-empty">테두리 데이터를 불러오지 못했습니다.</div>';
        }

        settingModal.classList.remove('hidden');
    } catch (e) {
        console.error(e);
        showMessageModal('스킨 정보를 불러오는 중 문제가 발생했습니다.');
    }
});


applyBgBtn.addEventListener('click', async () => {
    if (!selectedBgId || !ownedBgMap[selectedBgId]) return;

    const res = await selectBackgroundSkin(selectedBgId);
    if (res.code === 'SUCCESS') {
        showMessageModal('배경이 적용되었습니다.');
        loadCharacterBasicInfo();
    } else {
        showMessageModal('배경 변경 실패: ' + res.message);
    }
});

applyBorderBtn.addEventListener('click', async () => {
    const payload = { borderId: selectedBorderId };
    const res = await selectBorderSkin(selectedBorderId);

    if (res.code === 'SUCCESS') {
        showMessageModal(selectedBorderId ? '테두리가 적용되었습니다.' : '테두리가 제거되었습니다.');
        loadCharacterBasicInfo();
    } else {
        showMessageModal('테두리 변경 실패: ' + res.message);
    }
});

// ====== 닫기 ======
document.getElementById('closeSettingModal').addEventListener('click', () => {
    settingModal.classList.add('hidden');
});