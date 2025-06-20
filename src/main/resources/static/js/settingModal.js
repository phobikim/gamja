const settingModal = document.getElementById('settingModal');
const bgGrid = document.getElementById('bgGrid');
const applyBtn = document.getElementById('applyBgBtn');
let selectedBgId = null;
let ownedMap = {}; // 보유 여부 확인용

// 모달 열기
document.getElementById('playerInfoCard').addEventListener('click', async () => {
    const res = await apiRequest('/api/char/background/list', 'GET');
    if (res.code !== 'SUCCESS') return;

    bgGrid.innerHTML = '';
    selectedBgId = null;
    ownedMap = {};

    res.data.forEach(bg => {
        ownedMap[bg.id] = bg.owned;

        const card = document.createElement('div');
        card.className = 'bg-card';
        card.dataset.bgId = bg.id;

        const img = document.createElement('img');
        img.src = basePath + bg.imageUrl;
        img.alt = bg.name;

        card.appendChild(img);

        if (!bg.owned) {
            const overlay = document.createElement('div');
            overlay.className = 'bg-overlay';
            overlay.textContent = '미보유';
            card.appendChild(overlay);
        }

        card.addEventListener('click', () => {
            if (!bg.owned) return;

            document.querySelectorAll('.bg-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            selectedBgId = bg.id;
            applyBtn.disabled = false;
        });

        bgGrid.appendChild(card);
    });

    applyBtn.disabled = true;
    settingModal.classList.remove('hidden');
});

// 닫기
document.getElementById('closeSettingModal').addEventListener('click', () => {
    settingModal.classList.add('hidden');
});

// 적용
applyBtn.addEventListener('click', async () => {
    if (!selectedBgId || !ownedMap[selectedBgId]) return;

    const res = await apiRequestJson('/api/char/background/select', 'POST', {
        backgroundId: selectedBgId
    });

    if (res.code === 'SUCCESS') {
        settingModal.classList.add('hidden');
        location.reload(); // 새로고침으로 적용
    } else {
        showMessageModal('배경 변경 실패: ' + res.message);
    }
});