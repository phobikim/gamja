// 맵 선택 모달
const battleMapSelectModal = document.getElementById('battleMapSelectModal');
const mapListContainer = document.getElementById('mapListScroll');
const startBattleBtn = document.getElementById('startBattleBtn');
const closeBattleBtn = document.getElementById('closeBattleBtn');
let selectedMap = null;

async function handleAttackClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    playEffect("se_click2")
    document.body.style.overflow = 'hidden';
    battleMapSelectModal.classList.remove('hidden');
    // 1. 전투 맵 리스트 조회
    const mapRes = await apiRequest('/api/battle/map-list', 'GET');
    if (mapRes.code !== 'SUCCESS') {
        showMessageModal(mapRes.message || "맵 정보를 불러오지 못했습니다.");
        return;
    }

    renderMapList(mapRes.data);

}

function renderMapList(mapList) {
    mapListContainer.innerHTML = '';
    battleMapSelectModal.classList.remove('hidden');

    mapList.forEach((map, idx) => {
        const card = document.createElement('div');
        card.className = 'map-card';
        card.innerHTML = `<img src="${basePath}/${map.imagePath}" class="map-thumbnail" alt="${map.name}">`;

        card.addEventListener('click', (e) => {
            // ✅ 이벤트 전파 차단
            e.stopPropagation();

            selectedMap = map;
            updateMapDetail(map);
            document.querySelectorAll('.map-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
        });
        mapListContainer.appendChild(card);

        if (idx === 0) {
            selectedMap = map;
            updateMapDetail(map);
            card.classList.add('selected');
        }
    });

    startBattleBtn.onclick = () => {
        if (selectedMap) {
            battleMapSelectModal.classList.add('hidden');
            document.body.style.overflow = ''; // body 스크롤 복원
            window.selectedMap = selectedMap;
            window.startBattleFromMap(selectedMap);
        }
    };
}

closeBattleBtn.onclick = () => {
    battleMapSelectModal.classList.add('hidden');
};

function updateMapDetail(map) {
    document.getElementById('battleSelectMapName').textContent = map.name;
    document.getElementById('battleSelectMapLevel').textContent = `Level ${map.recommendedLevel || '-'}`;
    document.getElementById('battleSelectMapDesc').textContent = map.desc || '-';

    // ENEMIES 영역
    const monsterBox = document.getElementById('mapMonsters');
    monsterBox.innerHTML = '';
    map.monsters.forEach(mon => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('tooltip-wrapper');
        const img = document.createElement('img');
        img.src = basePath + mon.monsterImg;
        img.alt = mon.name;
        // 등급 기반 클래스 매핑
        const rankClassMap = {
            '희귀': 'rank-rare',
            '정예': 'rank-elite',
            '보스': 'rank-boss'
        };
        const rankClass = rankClassMap[mon.rank?.trim()];
        if (rankClass) img.classList.add(rankClass);

        const tooltip = document.createElement('div');
        tooltip.className = 'custom-tooltip';
        // ✅ 이름 + 줄바꿈 + 설명
        tooltip.innerHTML = `<strong>[${mon.name}]</strong><br>${mon.desc || ''}`;

        wrapper.addEventListener('click', (e) => e.stopPropagation());
        wrapper.appendChild(img);
        wrapper.appendChild(tooltip);
        monsterBox.appendChild(wrapper);
    });

    // REWARDS 영역
    const rewardBox = document.getElementById('mapRewards');
    rewardBox.innerHTML = '';
    map.rewards.forEach(item => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('tooltip-wrapper');
        const img = document.createElement('img');
        img.src = basePath + item.iconPath;
        img.alt = item.name;
        // 희귀도 기반 클래스 매핑
        const rarityClassMap = {
            'RARE': 'rarity-rare',
            'EPIC': 'rarity-epic',
            'LEGENDARY': 'rarity-legendary'
        };
        const rarity = item.rarity?.trim()?.toUpperCase();
        const rarityClass = rarityClassMap[rarity];
        if (rarityClass) img.classList.add(rarityClass);
        const tooltip = document.createElement('div');
        tooltip.className = 'custom-tooltip';
        tooltip.innerHTML = `<strong>[${item.name}]</strong><br>${item.description || ''}`;

        wrapper.addEventListener('click', (e) => e.stopPropagation());
        wrapper.appendChild(img);
        wrapper.appendChild(tooltip);
        rewardBox.appendChild(wrapper);
    });
}
