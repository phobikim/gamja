// 맵 선택 모달
const battleMapSelectModal = document.getElementById('battleMapSelectModal');
const mapListContainer = document.getElementById('mapListScroll');
const startBattleBtn = document.getElementById('startBattleBtn');
const closeBattleBtn = document.getElementById('closeBattleBtn');
let selectedMap = null;
let currentMonsterList = [];

async function handleAttackClick() {
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
        if (selectedMap) selectBattleMap(selectedMap);
    };
}

closeBattleBtn.onclick = () => {
    battleMapSelectModal.classList.add('hidden');
};

function updateMapDetail(map) {
    document.querySelector('.map-name').textContent = map.name;
    document.querySelector('.map-level').textContent = `Level ${map.recommendedLevel || '-'}`;
    document.querySelector('.map-desc').textContent = map.desc || '-';

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

async function selectBattleMap(map) {
    battleMapSelectModal.classList.add('hidden');
    battleModal.classList.remove('hidden');

    const userRes = await apiRequest('/api/battle/user-stat', 'GET');
    if (userRes.code !== 'SUCCESS') {
        battleModal.classList.add('hidden');
        showMessageModal(userRes.message || "유저 정보를 불러오지 못했습니다.");
        return;
    }

    const user = userRes.data;

    // ✅ 몬스터 리스트 요청 후 저장
    const monsterRes = await apiRequest(`/api/battle/monster_stat?mapId=${map.id}`, 'GET');
    if (monsterRes.code !== 'SUCCESS') {
        battleModal.classList.add('hidden');
        showMessageModal(monsterRes.message || "몬스터 정보를 불러오지 못했습니다.");
        return;
    }
    currentMonsterList = monsterRes.data;
    if (!currentMonsterList || currentMonsterList.length === 0) {
        battleModal.classList.add('hidden');
        showMessageModal("해당 맵에 등장하는 몬스터가 없습니다.");
        return;
    }

    // ✅ 랜덤 몬스터 선택 후 전투 시작
    const selectedMonster = getRandomMonster();
    initializeBattleScene(user, selectedMonster);
    startBattle(user, selectedMonster);
}

function getRandomMonster() {
    if (!currentMonsterList || currentMonsterList.length === 0) return null;
    const index = Math.floor(Math.random() * currentMonsterList.length);
    return currentMonsterList[index];
}

battleMapSelectModal.addEventListener('click', (e) => {
    // 툴팁 또는 툴팁 부모를 클릭했다면 무시
    if (e.target.closest('.custom-tooltip') || e.target.closest('.tooltip-wrapper')) return;

    const inside = e.target.closest('.map-select-modal-content');
    if (!inside) battleMapSelectModal.classList.add('hidden');
});