const battleModal = document.getElementById('battleModal');
const lootModal = document.getElementById('lootModal');

let battleEnded = false;
let isPlayerTurn = true; // ✅ 턴 상태 관리
let isProcessingTurn = false; // ✅ 턴 처리 중 상태 관리

window.battleState = {
    player: {
        name: '',
        maxHp: 0,
        currentHp: 0,
        power: 0,
        currentXp: 0,
        xp:0,
        maxXp:0,
        level:0,
        charImg:''
    },
    monster: {
        name: '',
        maxHp: 0,
        currentHp: 0,
        power: 0,
        drops: [],
    }
};

const logBox = document.getElementById('battleLog');
function logBattle(message, type = 'player') {
    const line = document.createElement('div');
    line.textContent = message;
    line.style.margin = '0.2rem 0';
    line.style.wordBreak = 'break-word'; // 줄바꿈 안전하게

    if (type === 'player') {
        line.style.color = '#39ff14'; // 초록
        line.style.textAlign = 'left';
    } else if (type === 'monster') {
        line.style.color = '#ff4d4d'; // 빨강
        line.style.textAlign = 'right';
    }

    logBox.appendChild(line);
    logBox.scrollTop = logBox.scrollHeight;
}
function startBattle(user, monster) {
    logBox.innerHTML = '';
    battleEnded = false;
    isPlayerTurn = true; // ✅ 플레이어 턴으로 시작
    isProcessingTurn = false; // ✅ 턴 처리 상태 초기화

    battleState.player = {
        name: user.name,
        maxHp: user.hp,
        currentHp: user.hp,
        power: user.power,
        xp:user.xp,
        maxXp: user.maxXp || 200, // user 조회 할 때, 같이 내려주건, 경험치 구하는 함수여기서 똑같이 구현하거나 해.
        lv:user.lv,
        charImg:user.charImage
    };
    battleState.monster = {
        name: monster.name,
        power: monster.monsterPower,
        maxHp: monster.monsterHp,
        currentHp: monster.monsterHp,
        drops: monster.dropItems || [],
        exp: monster.monsterXp
    };

    updateBattleUI();
    updateButtonStates(); // ✅ 버튼 상태 업데이트
    updateCardTurnStyles(); // ✅ 턴 표시 업데이트

    // ✅ 중앙에 Fight !! 문구 삽입
    const fightLine = document.createElement('div');
    fightLine.id = 'battleStartLine';
    fightLine.textContent = '🔥 Fight !!';
    fightLine.style.textAlign = 'center';
    fightLine.style.fontSize = '1.2rem';
    fightLine.style.fontWeight = 'bold';
    fightLine.style.color = '#39ff14';
    fightLine.style.margin = '0.5rem 0';
    logBox.appendChild(fightLine);
}

function updateBattleUI() {
    document.querySelector('.user-hp').textContent = battleState.player.currentHp;
    document.querySelector('.monster-hp').textContent = battleState.monster.currentHp;
}

// ✅ 카드 턴 스타일 업데이트
function updateCardTurnStyles() {
    const playerCard = document.querySelector('.user-stats');
    const monsterCard = document.querySelector('.monster-stats');

    // 기존 클래스 제거
    playerCard.classList.remove('active-turn', 'inactive-turn');
    monsterCard.classList.remove('active-turn', 'inactive-turn');

    if (battleEnded) {
        // 전투 종료 시 모든 효과 제거
        return;
    } else if (isProcessingTurn) {
        // 처리 중일 때는 현재 턴 유지하되 약간 다른 스타일
        if (isPlayerTurn) {
            playerCard.classList.add('active-turn');
            monsterCard.classList.add('inactive-turn');
        } else {
            monsterCard.classList.add('active-turn');
            playerCard.classList.add('inactive-turn');
        }
    } else if (isPlayerTurn) {
        // 플레이어 턴
        playerCard.classList.add('active-turn');
        monsterCard.classList.add('inactive-turn');
    } else {
        // 몬스터 턴
        monsterCard.classList.add('active-turn');
        playerCard.classList.add('inactive-turn');
    }
}

// ✅ 버튼 상태 업데이트 함수
function updateButtonStates() {
    const attackBtn = document.getElementById('attackBtn');
    const healBtn = document.getElementById('healBtn');

    // 플레이어 턴이고 턴 처리 중이 아닐 때만 버튼 활성화
    const canAct = isPlayerTurn && !isProcessingTurn && !battleEnded;

    if (attackBtn) {
        attackBtn.disabled = !canAct;
    }
    if (healBtn) {
        healBtn.disabled = !canAct;
    }

    updateCardTurnStyles();
}


function doAttack() {
    if (!isPlayerTurn || isProcessingTurn || battleEnded) {
        console.log('공격 불가:', { isPlayerTurn, isProcessingTurn, battleEnded });
        return;
    }
    playEffect("se_attack");
    // ✅ 턴 처리 시작
    isProcessingTurn = true;
    updateButtonStates();

    const damage = battleState.player.power;
    battleState.monster.currentHp -= damage;
    // 타격 애니메이션: 몬스터
    applyHitEffect('.monster-character');
    showDamageText('.monster-container', damage);``

    logBattle(`플레이어의 공격! ${damage}의 피해`, 'player');
    updateBattleUI();

    if (battleState.monster.currentHp <= 0) {
        winBattle();
        return;
    }

    // ✅ 플레이어 턴 종료, 몬스터 턴으로 변경
    isPlayerTurn = false;
    updateButtonStates();

    // 500ms 후 몬스터 반격
    setTimeout(() => {
        monsterTurn();
    }, 500);
}

function monsterTurn() {
    // ✅ 몬스터 턴 검증
    if (isPlayerTurn || battleEnded) {
        console.log('몬스터 턴 불가:', { isPlayerTurn, battleEnded });
        return;
    }
    const damage = Math.floor(Math.random() * battleState.monster.power) + 1;

    // 타격 애니메이션: 플레이어
    applyHitEffect('.player-character');
    showDamageText('.player-container', damage);

    battleState.player.currentHp -= damage;
    logBattle(`몬스터의 공격! ${damage}의 피해`, 'monster');

    setTimeout(() => {
        updateBattleUI();

        if (battleState.player.currentHp <= 0) {
            showMessageModal("패배했습니다...");
            closeBattleModal();
            return;
        }

        // ✅ 몬스터 턴 종료, 플레이어 턴으로 변경
        isPlayerTurn = true;
        isProcessingTurn = false;
        updateButtonStates();

    }, 400);
}


function doDefend() {
    const player = document.querySelector(".player-container");
    const effect = document.createElement("img");
    effect.className = "heal-frame-effect";
    effect.src = "https://phobi.me/gamja.img/images/effect/1.png"; // 초기 프레임
    player.appendChild(effect);
    const framePaths = [
        "https://phobi.me/gamja.img/images/effect/1.png",
        "https://phobi.me/gamja.img/images/effect/2.png",
        "https://phobi.me/gamja.img/images/effect/3.png",
        "https://phobi.me/gamja.img/images/effect/4.png"
    ];
    let index = 0;
    const interval = setInterval(() => {
        index++;
        if (index >= framePaths.length) {
            clearInterval(interval);
            effect.remove();
            return;
        }
        effect.src = framePaths[index];
    }, 200);
}

function doHeal() {
    // ✅ 턴 검증
    if (!isPlayerTurn || isProcessingTurn || battleEnded) {
        return;
    }

    showMessageModal("도망쳤습니다!");
    closeBattleModal();
}

function winBattle() {
    battleEnded = true;
    isProcessingTurn = false;
    updateBattleUI();
    updateButtonStates();
    updateWinBox();

    // const dropList = battleState.monster.drops;
    // const expReward = battleState.monster.exp || 0; // 처치 경험치
    //
    // const randomItem = dropList[Math.floor(Math.random() * dropList.length)];
    // const payload = {
    //     activityType: "ATTACK",
    //     exp:expReward,
    //     items: [{ itemId: randomItem.id, count: 1 }]
    // };
    //
    // apiRequestJson(`/api/action/addItems/${userId}`, 'POST', payload)
    //     .then(res => {
    //         if (res.code === 'SUCCESS') {
    //             const imgTag = `<img src="${basePath + randomItem.iconPath}" alt="${randomItem.name}"
    //             style="width: 48px; height: 48px; image-rendering: pixelated; vertical-align: middle;">`;
    //
    //             const message = `
    //           <div style="text-align: center; font-size: 1.3rem;">
    //             ${imgTag} 획득
    //             <div style="color: gold;">+${expReward} EXP</div>
    //           </div>
    //         `;
    //             showMessageModal(message);
    //         } else {
    //             showMessageModal("아이템 획득 처리 실패");
    //         }
    //         setUserInfo(res.data);
    //         closeBattleModal();
    //     });


}

function closeBattleModal() {
    document.getElementById('battleModal').classList.add('hidden');
    document.body.style.overflow = '';
    // ✅ 모달 닫을 때 상태 초기화
    battleEnded = false;
    isPlayerTurn = true;
    isProcessingTurn = false;
}

battleModal.addEventListener('click', (e) => {
    const inside = e.target.closest('.battle-modal-content');
    if (!inside) {
        // closeBattleModal();
        document.body.style.overflow = '';
    }
});


async function handleAttackClick() {
    playEffect("se_click2")
    document.body.style.overflow = 'hidden';
    document.getElementById('battleModal').classList.remove('hidden');


    // 1. 유저 스탯 정보 가져오기
    const userRes = await apiRequestJson(`/api/attack/user-stat/${userId}`, 'GET');
    if (userRes.code !== 'SUCCESS') {
        battleModal.classList.add('hidden');
        showMessageModal(userRes.message || "유저 정보를 불러오지 못했습니다.");
        return;
    }
    const user = userRes.data;

    // 2. 몬스터 목록 가져오기
    const monsterRes = await apiRequestJson('/api/attack/monster_list', 'GET');
    if (monsterRes.code !== 'SUCCESS') {
        battleModal.classList.add('hidden');
        showMessageModal(monsterRes.message || "몬스터 정보를 불러오지 못했습니다.");
        return;
    }

    const monsters = monsterRes.data;
    if (!monsters || monsters.length === 0) {
        battleModal.classList.add('hidden');
        showMessageModal("출현 가능한 몬스터가 없습니다.");
        return;
    }


    // 3. 랜덤 몬스터 선택
    const monster = monsters[Math.floor(Math.random() * monsters.length)];

    // 4. 이미지 교체
    const userImage = document.getElementById('userCharacter');
    const charImage = basePath_image + "/character/"
    userImage.src = charImage + user.charImage;
    userImage.alt = user.name;

    const monsterImage = document.getElementById('monsterCharacter');
    monsterImage.src = basePath + monster.imagePath;
    monsterImage.alt = monster.name;

    // 5. 스탯 영역 세팅
    document.querySelector('.user-name').textContent = `${user.name}`;
    document.querySelector('.user-level').textContent = user.lv;
    document.querySelector('.user-hp').textContent = user.hp;
    document.querySelector('.user-power').textContent = user.power;

    document.querySelector('.monster-name').textContent = `${monster.name}`;
    document.querySelector('.monster-rank').textContent = monster.rank;
    document.querySelector('.monster-hp').textContent = monster.monsterHp;
    document.querySelector('.monster-power').textContent = monster.monsterPower;
    document.querySelector('.monster-xp').textContent = monster.monsterXp;

    const rankText = monster.rank;
    const rankElement = document.querySelector('.monster-rank');
    // 색상 매핑
    const rankColorMap = {
        '야생': '#39ff14',   // 연두
        '병사': '#2196f3',   // 파랑
        '변이': '#9c27b0'    // 보라
    };

    // 해당 rank에 맞는 색상 적용
    rankElement.style.color = rankColorMap[rankText] || '#000'; // 기본은 검정

    const dropContainer = document.querySelector('.monster-drops');
    dropContainer.innerHTML = ''; // 기존 내용 초기화

    (monster.dropItems || []).forEach(item => {
        const img = document.createElement('img');
        img.src = basePath + item.iconPath;
        img.alt = item.name;
        img.title = item.name;
        img.classList.add('drop-icon');
        if (item.rarity) {
            img.classList.add(`rarity-${item.rarity.toLowerCase()}`);
        }
        img.addEventListener('click', (e) => {
            showTooltip(e, item);
        });
        dropContainer.appendChild(img);
    });

    if (dropContainer.children.length === 0) {
        dropContainer.textContent = '없음';
    }

    // 기존 값
    const basePower = Number(user.power) || 0;
    const levelBonus = Number(user.lv) || 0;
    const totalPower = basePower + levelBonus;

    const powerEl = document.querySelector('.user-power');
    powerEl.innerHTML = `${totalPower} (<span style="color:#fff8dc;">${basePower}</span> + <span style="color:#39ff14;">${levelBonus}</span>)`;
    user.power = totalPower;
    startBattle(user, monster);
}

function showTooltip(event, item) {
    const tooltip = document.getElementById('itemTooltip');
    const content = document.getElementById('itemTooltipContent');
    // 기존 툴팁 강제 닫기 (안 보이게)
    tooltip.classList.add('hidden');

    tooltip.classList.remove('hidden');

    const rarity = item.rarity || 'COMMON';
    const rarityClass = `rarity-${rarity.toLowerCase()}`;

    content.innerHTML = `
      <div style="text-align: center;"><strong>[${item.name}]</strong></div><br>
      희귀도: <span class="rarity-text ${rarityClass}">${rarity}</span><br>
      ${item.description || '설명이 없습니다.'}
    `;

    // 모달 안에 고정 배치되도록 설정 (모달이 relative 여야 함)
    const modal = document.getElementById('battleModal');
    modal.appendChild(tooltip);
}

// 외부 클릭 시 툴팁 닫기
document.addEventListener('click', (e) => {
    const tooltip = document.getElementById('itemTooltip');
    if (!e.target.classList.contains('drop-icon')) {
        tooltip.classList.add('hidden');
    }
});

function applyHitEffect(targetSelector) {
    const el = document.querySelector(targetSelector);
    if (!el) return;
    el.classList.remove('hit-effect'); // 기존 animation 제거
    void el.offsetWidth; // 강제 리플로우 (다시 animation 적용 가능하게)
    el.classList.add('hit-effect');
}

function showDamageText(targetSelector, damage) {
    const container = document.querySelector(targetSelector);
    if (!container) return;

    const dmg = document.createElement('div');
    dmg.className = 'damage-float';
    dmg.textContent = `-${damage}`;

    // 위치 조정 (가운데 위쪽)
    dmg.style.left = '50%';
    dmg.style.top = '0';

    container.appendChild(dmg);

    // 1초 뒤 제거
    setTimeout(() => {
        dmg.remove();
    }, 1000);
}
