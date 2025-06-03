
//실제 전투 모달
const battleModal = document.getElementById('battleModal');
// 승리 모달
const lootModal = document.getElementById('lootModal');
// 패배 모달
const defeatModal = document.getElementById('defeatModal')

let battleEnded = false;
let isPlayerTurn = true; // ✅ 턴 상태 관리
let isProcessingTurn = false; // ✅ 턴 처리 중 상태 관리

const monsterImage = document.getElementById('monsterCharacter');
const playerImage = document.getElementById('userCharacter');

window.battleState = {
    player: {
        dexName: '',
        attribute: '',
        maxHp: 0,
        currentHp: 0,
        power: 0,
        speed: 0,
        currentXp: 0,
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
        dexName: user.dexName,
        attribute : user.attribute,
        maxHp: user.hp,
        currentHp: user.hp,
        power: user.power,
        speed: user.speed,
        currentXp:user.xp,
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
    updateCardTurnStyles()


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
            // 0.2초 딜레이 후 fade-out → 0.6초 후 모달
            setTimeout(() => {
                playerImage.classList.remove('jump-in', 'hit-effect', 'fade-out');
                playerImage.style.animation = 'none';
                void playerImage.offsetWidth;
                playerImage.style.animation = '';
                playerImage.classList.add('fade-out');
            }, 200); // 🕒 fade-out 0.2초 딜레이

            setTimeout(() => {
                showDefeatModal("여기에 다시 묻히다...");
            }, 800); // 🕒 모달은 fade-out 이후 0.6초 뒤

            return;
        }

        // ✅ 몬스터 턴 종료, 플레이어 턴으로 변경
        isPlayerTurn = true;
        isProcessingTurn = false;
        updateButtonStates();
        updateCardTurnStyles()

    }, 400);
}

function showDefeatModal(text) {
    document.getElementById('defeatModal').classList.remove('hidden');
    document.getElementById('defeatSignText').innerText = text || "여기에 다시 묻히다...";
}
function hideDefeatModal() {
    document.getElementById('defeatModal').classList.add('hidden');
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

    // 전투 승리 시 몬스터 fade-out
    monsterImage.classList.remove('jump-in', 'hit-effect', 'fade-out');
    monsterImage.style.animation = 'none';

    void monsterImage.offsetWidth;
    monsterImage.style.animation = '';
    monsterImage.classList.add('fade-out');
    updateWinBox();
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


function initializeBattleScene(user, monster) {
    // ✅ 배경 이미지 설정
    const background = document.getElementById('battleBackground');
    if (background && selectedMap?.imagePath) {
        background.src = basePath + selectedMap.imagePath;
    }
    // ✅ 맵 이름 표시
    const mapNameBanner = document.getElementById('battleMapName');
    if (mapNameBanner) {
        mapNameBanner.textContent = selectedMap?.name || '전투 지역';
    }
    // ✅ 로그 배경색 설정
    const log = document.getElementById('battleLog');
    if (log) {
        // 맵 이름 or 추천 레벨에 따라 색상 지정
        const colorMap = {
            '야생 들판': '#00653f',
            '감자도둑쥐의 소굴': '#0d072b',
            '기본': '#f5f5f5'
        };
        const color = colorMap[selectedMap?.name] || colorMap['기본'];
        log.style.backgroundColor = color;
    }

    const charImage = basePath_image + "/character/";
    const userImage = document.getElementById('userCharacter');
    userImage.src = charImage + user.charImage;
    userImage.alt = user.name;

    playerImage.classList.remove('jump-in', 'fade-out', 'hit-effect');
    playerImage.style.animation = 'none';
    void playerImage.offsetWidth;
    playerImage.style.animation = '';
    playerImage.classList.add('jump-in');

    const monsterImage = document.getElementById('monsterCharacter');
    monsterImage.src = basePath + monster.imagePath;
    monsterImage.alt = monster.name;
    monsterImage.classList.remove('jump-in', 'fade-out', 'hit-effect');
    monsterImage.style.animation = 'none';

    const effectContainer = document.getElementById('monsterEffectContainer');
    const effectElement = document.getElementById('monsterEffect');

    effectElement.classList.remove(
        'effect-wild', 'effect-common', 'effect-normal',
        'effect-rare', 'effect-elite', 'effect-boss'
    );
    effectElement.classList.add('monster-effect');
    /* 등급 별 등장 색깔 다르게 */
    if (monster.rank === '보스') {
        effectElement.classList.add('effect-boss');
    } else if (monster.rank === '정예') {
        effectElement.classList.add('effect-elite');
    } else if (monster.rank === '희귀') {
        effectElement.classList.add('effect-rare');
    } else if (monster.rank === '일반') {
        effectElement.classList.add('effect-normal');
    } else if (monster.rank === '하급') {
        effectElement.classList.add('effect-common');
    } else if (monster.rank === '야생') {
        effectElement.classList.add('effect-wild');
    }

    else effectContainer.classList.add('hidden');

    monsterImage.onload = () => {
        void monsterImage.offsetWidth;
        monsterImage.style.animation = '';
        monsterImage.classList.add('jump-in');

        effectContainer.classList.remove('hidden');
        effectElement.classList.add('pop');

        setTimeout(() => {
            effectContainer.classList.add('hidden');
            effectElement.classList.remove('pop');
        }, 800);
    };

    // 스탯 UI 설정
    document.querySelector('.user-name').textContent = user.dexName;
    document.querySelector('.user-attribute').textContent = user.attribute;
    document.querySelector('.user-power').textContent = user.power;
    document.querySelector('.user-hp').textContent = user.hp;
    document.querySelector('.user-speed').textContent = user.speed;

    document.querySelector('.monster-name').textContent = monster.name;
    document.querySelector('.monster-rank').textContent = monster.rank;
    document.querySelector('.monster-hp').textContent = monster.monsterHp;
    document.querySelector('.monster-power').textContent = monster.monsterPower;
    document.querySelector('.monster-xp').textContent = monster.monsterXp;

    const rankColorClassMap = {
        '야생': 'rank-color-wild',
        '하급': 'rank-color-common',
        '일반': 'rank-color-normal',
        '희귀': 'rank-color-rare',
        '정예': 'rank-color-elite',
        '보스': 'rank-color-boss'
    };

    const rankElement = document.querySelector('.monster-rank');
    const rankColorClass = rankColorClassMap[monster.rank?.trim()];
    if (rankColorClass) rankElement.classList.add(rankColorClass);

}


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
