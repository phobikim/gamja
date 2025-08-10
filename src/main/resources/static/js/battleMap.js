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

    fetch("/boss-enter.html");
    fetch("/css/common.css");
    const img = new Image();
    img.src = "https://phobi.me/gamja.img/images/monster/boss_gg.png";
    img.src = "https://phobi.me/gamja.img/images/backgrounds/bg_boss_gg_map.png";

    playEffect("se_click2")
    document.body.style.overflow = 'hidden';
    battleMapSelectModal.classList.remove('hidden');

    currentDifficulty = 'NORMAL';

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
            const group = window.cachedMapList.find(g => g.groupId === map.groupId);
            const targetMap = group.maps.find(m => m.difficulty === currentDifficulty) || map;

            selectedMap = targetMap;
            updateMapDetail(targetMap);

            document.querySelectorAll('.map-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
        });

        mapListContainer.appendChild(card);

        if (idx === 0) {
            const group = window.cachedMapList.find(g => g.groupId === map.groupId);
            const targetMap = group.maps.find(m => m.difficulty === currentDifficulty) || map;

            selectedMap = targetMap;
            updateMapDetail(targetMap);
            card.classList.add('selected');
        }
    });

    startBattleBtn.onclick = () => {
        if (selectedMap) {
            battleMapSelectModal.classList.add('hidden');
            document.body.style.overflow = '';
            window.selectedMap = selectedMap;

            // 보스맵
            if (selectedMap.id === 6 && selectedMap.difficulty === 'BOSS') {
                window.location.replace("/boss-enter.html");
                return;
            }

            // 일반 전투 흐름
            window.startBattleFromMap(selectedMap);
        }
    };
}

closeBattleBtn.onclick = () => {
    battleMapSelectModal.classList.add('hidden');
    selectedMap = null;
    currentDifficulty = 'NORMAL';
};

function updateMapDetail(map, triggeredByTabClick = false) {
    const group = window.cachedMapList.find(g => g.groupId === map.groupId);

    // 탭 렌더링
    const tabBox = document.getElementById('difficultyTabBox');
    tabBox.innerHTML = '';

    const difficultyNames = {
        'NORMAL': '일반',
        'HARD': '시험',
        'BOSS': '보스'
    };

    const difficulties = group.maps.map(m => m.difficulty); // ["NORMAL", "HARD", "BOSS"] 등

    if (difficulties.length > 1) {
        difficulties.forEach(diff => {
            const btn = document.createElement('button');
            btn.className = 'difficulty-tab';
            btn.dataset.type = diff;
            btn.textContent = difficultyNames[diff] || diff;

            if (diff === currentDifficulty) {
                btn.classList.add('active');
            }

            btn.addEventListener('click', () => {
                const selected = group.maps.find(m => m.difficulty === diff);
                if (selected) {
                    playEffect("se_click2");
                    selectedMap = selected;
                    currentDifficulty = diff;
                    updateMapDetail(selected, true);
                }
            });

            tabBox.appendChild(btn);
        });
    }

    applyMapEntryRequirement(map);

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

function applyMapEntryRequirement(map) {
    const mapDetailPanel = document.getElementById('mapDetailPanel');
    const startBtn = document.getElementById('startBattleBtn');

    // 초기화
    mapDetailPanel.classList.remove('locked');
    startBtn.classList.remove('disabled');
    startBtn.disabled = false;
    const oldOverlay = mapDetailPanel.querySelector('.lock-overlay');
    if (oldOverlay) oldOverlay.remove();

    // 조건 처리
    if ((map.difficulty === 'HARD' || map.difficulty === 'BOSS') && map.entryAllowed === false) {
        mapDetailPanel.classList.add('locked');
        startBtn.classList.add('disabled');
        startBtn.disabled = true;

        const overlay = document.createElement('div');
        overlay.className = 'lock-overlay';
        overlay.textContent = map.requiredMessage || '입장 조건을 충족해야 합니다';
        mapDetailPanel.appendChild(overlay);
    }
}