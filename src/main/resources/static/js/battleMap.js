// 맵 선택 모달
const battleMapSelectModal = document.getElementById('battleMapSelectModal');
const mapListContainer = document.getElementById('mapListScroll');
const startBattleBtn = document.getElementById('startBattleBtn');
const closeBattleBtn = document.getElementById('closeBattleBtn');
let selectedMap = null;
let currentDifficulty = 'NORMAL';
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

    window.cachedMapList = mapRes.data;

    // 모든 group 내부 map에 groupId 주입
    mapRes.data.forEach(group => {
        group.maps.forEach(m => m.groupId = group.groupId);
    });

    // 상단엔 NORMAL만 보여줌
    const normalMaps = [];
    mapRes.data.forEach(group => {
        const normalMap = group.maps.find(m => m.difficulty === 'NORMAL');
        if (normalMap) {
            normalMaps.push(normalMap);
        }
    });
    renderMapList(normalMaps);

}

document.querySelectorAll('.difficulty-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.difficulty-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentDifficulty = btn.dataset.difficulty;

        const originalList = window.cachedMapList || [];
        const filtered = originalList.filter(m => m.difficulty === currentDifficulty);
        renderMapList(filtered);
    });
});

function renderMapList(mapList) {
    mapListContainer.innerHTML = '';

    mapList.forEach((map, idx) => {
        const card = document.createElement('div');
        card.className = 'map-card';
        card.innerHTML = `<img src="${basePath}/${map.imagePath}" class="map-thumbnail" alt="${map.name}">`;

        card.addEventListener('click', (e) => {
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
            document.body.style.overflow = '';
            window.selectedMap = selectedMap;
            window.startBattleFromMap(selectedMap);
        }
    };
}

closeBattleBtn.onclick = () => {
    battleMapSelectModal.classList.add('hidden');
};

function updateMapDetail(map, triggeredByTabClick = false) {
    const group = window.cachedMapList.find(g => g.groupId === map.groupId);
    const normalMap = group.maps.find(m => m.difficulty === 'NORMAL');
    const hardMap = group.maps.find(m => m.difficulty === 'HARD');


    // 탭 렌더링
    const tabBox = document.getElementById('difficultyTabBox');
    if (hardMap) {
        tabBox.innerHTML = `
            <button class="difficulty-tab ${map.difficulty === 'NORMAL' ? 'active' : ''}" data-type="NORMAL">일반</button>
            <button class="difficulty-tab ${map.difficulty === 'HARD' ? 'active' : ''}" data-type="HARD">시험</button>
        `;

        tabBox.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', () => {
                const selected = group.maps.find(m => m.difficulty === btn.dataset.type);
                if (selected) {
                    selectedMap = selected;
                    updateMapDetail(selected, true);
                }

                tabBox.querySelectorAll('button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
            });
        });

    } else {
        tabBox.innerHTML = '';
    }

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
    if (triggeredByTabClick) {
        const selectedCard = document.querySelector('.map-card.selected img');
        if (selectedCard) {
            selectedCard.src = `${basePath}/${map.imagePath}`;
            selectedCard.alt = map.name;
        }
    }
}

function injectChronicleIcon() {
    const container = document.getElementById('chronicleIconContainer');
    if (!container) return;

    if (container.querySelector('img')) return;

    const icon = document.createElement('img');
    icon.src = `${basePath_image}/icons/chronicle_book.png`;
    icon.alt = '감자연대기';
    icon.classList.add('chronicle-icon');

    icon.addEventListener('click', (e) => {
        e.stopPropagation();
        playEffect("se_click2");
        openChronicleModal(selectedMap.id);
    });

    container.appendChild(icon);
}