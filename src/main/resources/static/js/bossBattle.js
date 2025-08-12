let bossPatternData = null;
let playerSkillImgSrc = null
let playerMaxHp = 0;                // 초기 최대 HP 캐시
let bossMaxHp = 0;                  // 보스 최대 HP 캐시
let currentPotionInfo = null;
let basePlayerPower = 0;
let currentPlayerPower = 0;

const EFFECT_IMG = {
    DAMAGE_TO_PLAYER: 'https://phobi.me/gamja.img/images/effect/boss_attack.png',
    HEAL_SELF:        'https://phobi.me/gamja.img/images/effect/boss_heal2.png',
    DEBUFF_PLAYER:    'https://phobi.me/gamja.img/images/effect/boss_debuff.png',
    PLAYER_HEAL_SELF: 'https://phobi.me/gamja.img/images/effect/healing_sprout.png',
};
let bgmStarted = false;
let usingPotion = false;

const ALLY_TIMING = {
    minDuration: 1500,   // 대사 최소 표시 시간
    lingerAfter: 500,    // 효과 끝난 뒤 화면에 더 남아있게
    fadeOutMs:  250,     // 페이드아웃 애니메이션 시간
    betweenEvents: 500   // (여러 이벤트일 때) 각 이벤트 사이 간격
};


const attackBtn = document.querySelector('#attackBtn');
const potionBtn = document.querySelector('#potionBtn');

function startBossBgm() {
    if (bgmStarted) return;
    bgmStarted = true;
    try {
        playEffect('bgm_gotcha');
    } catch (e) {
        console.warn('BGM start failed:', e);
    }
}

async function resumeAudioContextIfNeeded() {
    try {
        const ctx =
            (window.sound && sound.getAudioContext && sound.getAudioContext()) ||
            window.gamjaAudioContext || // 너가 따로 보관했다면
            null;
        if (ctx && ctx.state === 'suspended') {
            await ctx.resume();
        }
    } catch (e) {
        // noop
    }
}
function setupBgmGate() {
    const onFirstGesture = async () => {
        document.removeEventListener('pointerdown', onFirstGesture, { capture: true });
        document.removeEventListener('keydown', onFirstGesture, { capture: true });
        document.removeEventListener('touchstart', onFirstGesture, { capture: true });
        await resumeAudioContextIfNeeded();
        startBossBgm();
    };

    // 페이지 어디든 첫 입력에 반응
    document.addEventListener('pointerdown', onFirstGesture, { once: true, capture: true });
    document.addEventListener('keydown', onFirstGesture, { once: true, capture: true });
    document.addEventListener('touchstart', onFirstGesture, { once: true, passive: true, capture: true });
}

// DOM 준비되면 게이트 설치
window.addEventListener('DOMContentLoaded', setupBgmGate);
function spawnEffect(targetEl, src, variant = 'impact') {
    if (!targetEl || !src) return;
    const img = document.createElement('img');
    img.src = src;
    img.alt = 'skill-effect';
    img.className = `skill-effect ${variant}`;
    targetEl.appendChild(img);

    const styles = getComputedStyle(img);
    const durs   = styles.animationDuration.split(',').map(s => parseFloat(s)||0);
    const delays = styles.animationDelay.split(',').map(s => parseFloat(s)||0);
    const maxMs  = durs.reduce((m, dur, i) => Math.max(m, (dur + (delays[i]||0))*1000), 0);
    setTimeout(() => img.remove(), Math.max(300, maxMs + 50));
}

function getPlayerImgContainer() {
    return (
        document.querySelector('#characterLeftWrapper .char-image-container') ||
        document.querySelector('.character-left-wrapper .char-image-container')
    );
}

fetch("/boss-run.html");
// 보스 등장 애니메이션 적용
window.addEventListener('DOMContentLoaded', () => {
    const bossImg = document.querySelector('.boss-image');
    const charImg = document.querySelector('.character-left');

    // 공격 버튼
    if (attackBtn) {
        attackBtn.addEventListener('click', async () => {
            lockPlayerArea();
            const res = await apiRequest('/api/battle/boss/player-attack', 'POST');
            const data = (res && typeof res.json === 'function') ? await res.json() : res;
            if (!data || data.code !== 'SUCCESS') {
                await showMessageModal(data?.message || '전투가 종료됩니다.');
                window.location.replace('/char.html');
                return;
            }
            playEffect("se_attack");

            const { monster, playerAttack, victory, player, allyEvents } = res.data;

            if (player?.skillImagePath) {
                playerSkillImgSrc = `${window.basePath_image}${player.skillImagePath}`;
            }

            const bossImgWrapper = document.querySelector('.boss-image-wrapper');
            if (playerSkillImgSrc) {
                spawnEffect(bossImgWrapper, playerSkillImgSrc, 'impact');
            }

            // 1) 보스 HP 반영 + 보스 흔들림 + 데미지 텍스트
            setBossHpBar(monster.hp, monster.maxHp);
            bossMaxHp = monster.maxHp ?? bossMaxHp;
            shakeElement(document.querySelector('.boss-image'), 'shake');
            showDamageEffect(playerAttack.damage, playerAttack.isCritical);

            if (victory) {
                showBossDialogue("내가 지다니...!");
                const endRes = await apiRequest('/api/battle/boss/end-boss-battle', 'POST');
                if (endRes.code === 'SUCCESS') {
                    const d = endRes.data || {};
                    const stored = {
                        reward: {
                            dexName:    d.dexName,
                            charImage:  d.charImage,
                            beforeLevel:d.beforeLevel,
                            afterLevel: d.afterLevel,
                            beforeXp:   d.beforeXp,
                            afterXp:    d.afterXp,
                            maxExp:     d.maxExp,
                            gainedXp:   d.gainedXp
                        },
                        lootItems: d.items || []
                    };
                    localStorage.setItem('bossWinData', JSON.stringify(stored));
                }
                await delay(1500);
                window.location.replace(apiPath('/boss-win.html'));
            } else {
                if (Array.isArray(allyEvents) && allyEvents.length) {
                    await handleAllyEvents(allyEvents);
                }
                await delay(500);
                await triggerBossTurn();
            }
        });
    }

    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.potion-slot, .btn-potion, #potionBtn');
        if (!btn) return;
        onUsePotion();
    });

    // 초기 데이터 가져오기
    fetchBossBattleInit().then(() => {
        // 등장 애니메이션
        if (bossImg) {
            bossImg.classList.add('float-once');
            bossImg.addEventListener('animationend', () => {
                bossImg.classList.remove('float-once');
                bossImg.classList.add('glow-effect');
            }, { once: true });
        }

        if (charImg) {
            charImg.classList.add('animate-enter');
        }
    });
});

function renderPotionSlot(potion = {}) {
    const slot  = document.querySelector('.potion-slot');
    const wrap  = slot?.querySelector('.potion-img-wrap');
    const img   = slot?.querySelector('.potion-img');
    if (!slot || !wrap || !img) return;

    currentPotionInfo = { ...(currentPotionInfo || {}), ...potion };

    const qty  = Number.isFinite(currentPotionInfo.quantity) ? currentPotionInfo.quantity : 0;
    const bHp  = Number.isFinite(currentPotionInfo.bonusHp) ? currentPotionInfo.bonusHp : 0;
    const bPow = Number.isFinite(currentPotionInfo.bonusPower) ? currentPotionInfo.bonusPower : 0;
    const path = currentPotionInfo.itemPath || '';

    // 이미지 경로 세팅
    const base = (window.basePath || '');
    img.src = path?.startsWith('http') ? path : (base + path);
    img.alt = `포션 (+${bHp} HP, +${bPow} POWER)`;
    img.title = `포션 사용: HP +${bHp}, 파워 +${bPow}`;

    // 스티커 수량 텍스트
    const label = qty > 99 ? 'x99+' : `x${qty}`;
    wrap.setAttribute('data-qty', label);

    // 비활성 표시
    if (qty <= 0) {
        slot.classList.add('disabled');
        slot.setAttribute('aria-disabled', 'true');
    } else {
        slot.classList.remove('disabled');
        slot.setAttribute('aria-disabled', 'false');
    }
}

async function onUsePotion() {
    if (usingPotion) return;
    if (getPlayerArea()?.classList.contains('is-locked')) return;

    const slot = document.querySelector('.potion-slot');
    usingPotion = true;
    document.querySelector('.potion-slot')?.classList.add('disabled');


    try {
        const res  = await apiRequest('/api/battle/use-potion', 'POST');
        const resp = (res && typeof res.json === 'function') ? await res.json() : res;

        if (!resp || resp.code !== 'SUCCESS') {
            (typeof showMessageModal === 'function' ? showMessageModal : alert)
            (resp?.message || '포션을 사용할 수 없습니다.');
            if (resp?.message?.includes('전투 중이 아닙니다')) {
                window.location.replace('/char.html');
            }
            document.querySelector('.potion-slot')?.classList.remove('disabled');
            return;
        }

        const d = resp.data || {};
        const { playerHp, maxHp, quantity, bonusHp, bonusPower } = d;

        currentPotionInfo = currentPotionInfo || {};
        currentPotionInfo.quantity   = resp.data.quantity;
        currentPotionInfo.bonusHp    = resp.data.bonusHp;
        currentPotionInfo.bonusPower = resp.data.bonusPower;

        renderPotionSlot(currentPotionInfo);

        if (typeof playerHp === 'number') {
            const max = typeof maxHp === 'number' ? maxHp : (playerMaxHp || 1);
            updatePlayerHp(playerHp, max);
            playerMaxHp = max;
        }

        const qty  = currentPotionInfo?.quantity;
        if (slot && (!Number.isFinite(qty) || qty > 0)) slot.classList.remove('disabled');

        if (bonusPower > 0) {
            // showPowerBuff(`+${bonusPower} POWER`);
            // adjustAttackPower(+bonusPower); // 버튼 뱃지 갱신
        }
        playEffect("se_click2");
    } catch (err) {
        console.error('[use-potion] error:', err);
        (typeof showMessageModal === 'function' ? showMessageModal : alert)
        ('연결 오류로 포션 사용에 실패했습니다.');
    } finally {
        usingPotion = false;
        if (slot && (currentPotionInfo?.quantity ?? 0) > 0) {
            slot.classList.remove('disabled');
        }
    }
}

function showPowerBuff(txt = '+POWER') {
    const container =
        document.querySelector('.character-left-wrapper .char-image-container') ||
        document.querySelector('#characterLeftWrapper .char-image-container') ||
        document.querySelector('.char-image-container');

    if (!container) return;

    // 플로팅 텍스트
    const tag = document.createElement('div');
    tag.className = 'buff-effect';
    tag.textContent = txt;
    container.appendChild(tag);
    setTimeout(() => tag.remove(), 1000);
}



const runAwayBtn = document.getElementById('runAwayBtn');
if (runAwayBtn) {
    runAwayBtn.addEventListener('click', async () => {
        await apiRequest('/api/battle/boss/end-boss-battle?outcome=escape', 'POST');
        window.location.replace(apiPath('/boss-run.html'));

    });
}

async function fetchBossBattleInit() {
    try {
        const result = await apiRequest("/api/battle/boss/start-boss-battle", "POST");

        if (result.code === "SUCCESS") {
            renderBossBattleInit(result.data);
            bossPatternData = result.data.patterns;
        } else {
            console.warn(" API 실패:", result.message);
        }
    } catch (err) {
        console.error("보스 배틀 정보 불러오기 실패:", err.message || err);
    }
}

function renderBossBattleInit(data) {
    if (!data) return;

    const { player, monster, map } = data;

    // 1. 캐릭터 정보 세팅
    const charImg = document.querySelector('.character-left');
    const charName = document.querySelector('.character-left-wrapper .char-name-label');
    const charHpText = document.querySelector('.char-hp-text');
    const charHpFill = document.querySelector('.char-hp-fill');

    if (charImg && player.charImage) {
        charImg.src = `${window.basePath_image}/character/${player.charImage}`;
    }
    if (charName) {
        charName.textContent = player.dexName;
    }
    playerMaxHp = player.maxHp ?? player.hp ?? 0;
    if (charHpText) {
        charHpText.textContent = `${player.hp} / ${playerMaxHp}`;
    }
    if (charHpFill) {
        charHpFill.style.width = `100%`;
    }
    if (player?.skillImagePath) {
        playerSkillImgSrc = `${window.basePath_image}${player.skillImagePath}`;
    }
    const pPower = Number(player.power ?? player.attack ?? player.attackPower ?? 0);
    const pBonus = Number(player.bonusPower ?? 0);
    basePlayerPower = pPower;
    setAttackPower(basePlayerPower + pBonus, { flash: false });

    // 2. 몬스터 정보 세팅
    const bossImg = document.querySelector('.boss-image');
    const bossName = document.querySelector('.boss-name');

    if (bossImg && monster.imagePath) {
        bossImg.src = `${window.basePath}${monster.imagePath}`;
    }
    if (bossName) {
        bossName.textContent = `보스 ${monster.name}`;
    }

    bossMaxHp = monster.maxHp ?? monster.hp ?? 0;
    setBossHpBar(bossMaxHp, bossMaxHp);

    // 3. 맵 배경 이미지 세팅
    const bgImg = document.querySelector('.boss-map-img');
    if (bgImg && map?.background) {
        bgImg.src = `${window.basePath}${map.background}`;
    }
    renderPotionSlot(player?.potion);

}

// === [수정] 보스 턴: 서버 스펙(bossTurn) 응답에 맞춤 ===
async function triggerBossTurn() {
    const res = await apiRequest('/api/battle/boss/monster-attack', 'POST'); // bossTurn 엔드포인트
    if (res.code !== 'SUCCESS') return;

    const { dialogue, skill = {}, effects = {}, player = {}, monster = {}, allyEvents = [] } = res.data || {};

    // 1) 대사 출력
    if (dialogue) {
        showBossDialogue(dialogue);
    }

    // 2) 피해/회복 반영
    const dmg = effects.damageToPlayer || 0;
    const healBoss = effects.healToBoss || 0;
    const debuff = effects.debuffPlayerPower || 0;

    const type =
    skill?.type ||(dmg > 0 ? 'DAMAGE_TO_PLAYER' : healBoss > 0 ? 'HEAL_SELF' : debuff > 0 ? 'DEBUFF_PLAYER' : null);

    const playerImgContainer = getPlayerImgContainer();
    const bossImgWrapper = document.querySelector('.boss-image-wrapper');

    // 1) 보스 데미지 → 플레이어 이미지 위치
    if (type === 'DAMAGE_TO_PLAYER' && dmg > 0) {
        spawnEffect(getPlayerImgContainer(), EFFECT_IMG.DAMAGE_TO_PLAYER, 'boss-impact');
        shakeElement(document.querySelector('.character-left'), 'shake-weak');
        showBossAttackEffect(dmg);
    }

    // 2) 보스 회복 → 보스 이미지 위
    if (type === 'HEAL_SELF' && healBoss > 0) {
        const bossHpFill = document.querySelector('.boss-hp-bar-fill');
        if (bossHpFill) {
            bossHpFill.classList.remove('healed');
            bossHpFill.offsetWidth;
            bossHpFill.classList.add('healed');
        }
        spawnEffect(bossImgWrapper, EFFECT_IMG.HEAL_SELF, 'heal-boss');
    }

    // 3) 보스 디버프 → 플레이어 이미지 위치
    if (type === 'DEBUFF_PLAYER' && debuff > 0) {
        spawnEffect(playerImgContainer, EFFECT_IMG.DEBUFF_PLAYER, 'boss-impact');
        adjustAttackPower(-debuff);
    }

    // HP 반영
    const pMax = player.maxHp ?? playerMaxHp;
    if (typeof player.hp === 'number') {
        updatePlayerHp(player.hp, pMax);
        playerMaxHp = pMax;
    }
    if (typeof monster.hp === 'number') {
        const mMax = monster.maxHp ?? bossMaxHp;
        setBossHpBar(monster.hp, mMax);
        bossMaxHp = mMax;
    }

    if (player.defeat) {
        await apiRequest('/api/battle/boss/end-boss-battle', 'POST');
        await delay(1500);
        window.location.replace(apiPath('/boss-run.html'));

    }
    if (Array.isArray(allyEvents) && allyEvents.length) {
        await handleAllyEvents(allyEvents);
    }
    if (!player.defeat) {
        unlockPlayerArea();
    }
}


function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function showDamageEffect(damage, isCritical) {
    const bossImg = document.querySelector('.boss-image');
    const damageText = document.createElement('div');
    damageText.className = `damage-effect ${isCritical ? 'critical' : 'normal'}`;
    damageText.textContent = `${damage}${isCritical ? ' ⚡' : ''}`;

    bossImg.parentElement.appendChild(damageText);

    setTimeout(() => {
        damageText.remove();
    }, 900);
}

function showBossAttackEffect(damage) {
    const charWrapper = document.getElementById('characterLeftWrapper');
    const damageText = document.createElement('div');
    damageText.className = 'damage-effect boss-attack';
    damageText.textContent = `-${damage}`;

    charWrapper.appendChild(damageText);

    setTimeout(() => {
        damageText.remove();
    }, 900);
}

function ensurePlayerHpChip() {
    const bar = document.querySelector('.char-hp-bar');
    if (!bar) return null;
    let chip = bar.querySelector('.char-hp-chip');
    if (!chip) {
        chip = document.createElement('div');
        chip.className = 'char-hp-chip';
        chip.style.width = '100%';
        bar.insertBefore(chip, bar.firstChild);
    }
    return chip;
}


function updatePlayerHp(hp, maxHp) {
    const hpText = document.querySelector('.char-hp-text');
    const bar    = document.querySelector('.char-hp-bar');
    const fill   = document.querySelector('.char-hp-fill');
    const chip   = ensurePlayerHpChip();

    const safeMax     = Math.max(1, Number(maxHp) || 1);
    const prevHpValue = Number(bar?.dataset.hpValue ?? safeMax);
    const prevPercent = parseFloat(bar?.dataset.hpPercent) || (prevHpValue / safeMax * 100);
    const newPercent  = Math.max(0, Math.min(100, (hp / safeMax) * 100));
    const isHeal      = hp > prevHpValue;

    if (hpText) hpText.textContent = `${hp} / ${safeMax}`;

    if (fill) {
        fill.style.width = `${newPercent}%`;
        // 데미지/회복 반짝
        fill.classList.remove('hit','heal'); void fill.offsetWidth;
        fill.classList.add(isHeal ? 'heal' : 'hit');
    }

    if (chip) {
        if (isHeal) {
            // 회복: 두 레이어 함께 증가(버벅임 방지)
            chip.style.width = `${newPercent}%`;
        } else {
            // 데미지: 칩이 늦게 따라감
            chip.style.width = `${prevPercent}%`;
            requestAnimationFrame(() => {
                setTimeout(() => { chip.style.width = `${newPercent}%`; }, 120);
            });
        }
    }

    // 회복 이펙트 & +숫자
    if (isHeal) {
        const container = getPlayerImgContainer();
        spawnEffect(container, EFFECT_IMG.PLAYER_HEAL_SELF, 'heal-player');

        // +숫자
        const healed = hp - prevHpValue;
        if (container && healed > 0) {
            const tag = document.createElement('div');
            tag.className = 'heal-effect';
            container.appendChild(tag);
            setTimeout(() => tag.remove(), 1000);
        }
    }

    if (bar) {
        bar.dataset.hpValue   = String(hp);
        bar.dataset.hpPercent = newPercent.toFixed(2);
    }
}

function setBossHpBar(currentHp, maxHp) {
    const bossHpText = document.getElementById('bossHpText');
    const bossHpFill = document.querySelector('.boss-hp-bar-fill');

    if (bossHpText) {
        bossHpText.textContent = `${currentHp} / ${maxHp}`;
    }

    // 페이즈 색 유지(1000단위 분할), 없으면 그냥 전체 퍼센트
    const total = maxHp || 1;
    const overallPercent = Math.max(0, Math.min(100, (currentHp / total) * 100));


    const rawPhase = Math.floor(currentHp / 1000);
    const mappedPhase = ((rawPhase - 1) % 5 + 5) % 5 + 1;
    const phaseHp = currentHp % 1000 || (currentHp === 0 ? 0 : 1000);
    const fillPercent = maxHp > 1000 ? Math.min(100, (phaseHp / 1000) * 100) : overallPercent;

    if (bossHpFill) {
        bossHpFill.style.width = `${fillPercent}%`;
        bossHpFill.className = 'boss-hp-bar-fill';
        bossHpFill.classList.add(`hp-phase-${mappedPhase}`);
        bossHpFill.classList.add('hit');

        setTimeout(() => bossHpFill.classList.remove('hit'), 200);
    }
}

function showBossDialogue(text) {
    const dialogueBox = document.getElementById('bossDialogue');
    if (!dialogueBox) return;

    dialogueBox.textContent = `GG : ${text}"`;
    dialogueBox.classList.add('showing');

}

function shakeElement(el, cls = 'shake') {
    if (!el) return;
    el.classList.remove(cls); // 재적용 위해 제거
    // 강제 리플로우
    // eslint-disable-next-line no-unused-expressions
    el.offsetWidth;
    el.classList.add(cls);
    el.addEventListener('animationend', () => el.classList.remove(cls), { once: true });
}

function getPlayerArea() {
    return document.querySelector('.player-battle-area');
}
function lockPlayerArea(msg = '보스턴입니다.') {
    const area = getPlayerArea();
    if (!area) return;
    area.setAttribute('data-overlay', msg);
    area.classList.add('is-locked');
}
function unlockPlayerArea() {
    const area = getPlayerArea();
    if (!area) return;
    area.classList.remove('is-locked');
    area.removeAttribute('data-overlay');
}

function ensureAttackPowerChip() {
    const btn = document.getElementById('attackBtn');
    if (!btn) return null;
    let chip = btn.querySelector('.power-chip');
    if (!chip) {
        chip = document.createElement('span');
        chip.className = 'power-chip';
        chip.setAttribute('aria-label', '현재 공격력');
        btn.appendChild(chip);
    }
    return chip;
}

function setAttackPower(power, { flash = true } = {}) {
    currentPlayerPower = Math.max(0, Number(power) || 0);
    const chip = ensureAttackPowerChip();
    if (!chip) return;
    chip.textContent = `ATK ${currentPlayerPower}`;
    if (flash) {
        chip.classList.remove('flash'); // 재적용
        chip.offsetWidth;
        chip.classList.add('flash');
    }
}

function adjustAttackPower(delta, opts) {
    setAttackPower((currentPlayerPower || 0) + (Number(delta) || 0), opts);
}

function showAllyDialogue(text, { name, duration = 1500 } = {}) {
    const box = document.getElementById('allyDialogue');
    const nameEl = document.getElementById('allyName');
    if (!box) return;

    if (nameEl && name) nameEl.textContent = name;

    box.textContent = text;
    box.classList.add('showing');
    const showMs = Math.max(duration, ALLY_TIMING.minDuration);
    // 재설정용 타이머 보관
    clearTimeout(box._hideTimer);
    box._hideTimer = setTimeout(() => {
        box.classList.remove('showing');
    }, showMs);
}

/* 조력자 등장/퇴장 간단 제어 */
function showAllyImage(src, name) {
    const wrap = document.getElementById('characterRightWrapper');
    const img  = wrap?.querySelector('.character-right');
    const nameEl = document.getElementById('allyName');
    if (!wrap || !img) return;

    if (src) img.src = src;
    if (nameEl && name) nameEl.textContent = name;

    wrap.classList.remove('hidden', 'ally-exit');
    wrap.classList.remove('ally-enter'); void wrap.offsetWidth; wrap.classList.add('ally-enter');
}

function hideAlly() {
    const wrap = document.getElementById('characterRightWrapper');
    if (!wrap) return;
    wrap.classList.remove('ally-enter');
    wrap.classList.add('ally-exit');
    setTimeout(() => {
        wrap.classList.add('hidden');
        const box = document.getElementById('allyDialogue');
        if (box) {
            box.classList.remove('showing');
            clearTimeout(box._hideTimer);
        }
    }, ALLY_TIMING.fadeOutMs);
}

async function handleAllyEvents(events = []) {
    if (!Array.isArray(events) || events.length === 0) return;
    lockPlayerArea('감자 조력자가 나타났다!!');

    for (let i = 0; i < events.length; i++) {
        await playAllyEvent(events[i]);
        if (i < events.length - 1) {
            await delay(ALLY_TIMING.betweenEvents);
        }
    }
    await delay(ALLY_TIMING.lingerAfter);
    hideAlly();
    unlockPlayerArea();
}

function playAllyEvent(ev = {}) {
    return new Promise(async (resolve) => {
        const name = ev.name || '조력 감자';
        const img  = `${basePath_image}/character/${ev.image || ''}`;
        const dur  = Number(ev.duration ?? 1000);
        const beforeMs = Number(450);

        // 1) 등장 + 대사
        showAllyImage(img, name);
        if (ev.dialogue) showAllyDialogue(ev.dialogue, { name, duration: dur });

        // 2) 효과 적용 타이밍
        await delay(beforeMs);

        // (a) 힐
        if (typeof ev.heal === 'number' && ev.heal > 0) {
            const nextHp  = ev.playerHp ?? Math.min(playerMaxHp, (Number(document.querySelector('.char-hp-bar')?.dataset.hpValue) || 0) + ev.heal);
            const nextMax = ev.playerMaxHp ?? playerMaxHp;
            updatePlayerHp(nextHp, nextMax);
        }

        // (b) 공격력 복구
        if (ev.type === 'HELP_RESTORE' || ev.restore === 'FULL') {
            setAttackPower(basePlayerPower, { flash: true });
            showPowerBuff('공격력 복구!');
        } else if (typeof ev.restore === 'number' && ev.restore !== 0) {
            adjustAttackPower(ev.restore, { flash: true });
            showPowerBuff(`${ev.restore > 0 ? '+' : ''}${ev.restore} POWER`);
        }

        // (c) 대신 공격 (보스 피해)
        if (typeof ev.damage === 'number' && ev.damage > 0) {
            const bossWrap = document.querySelector('.boss-image-wrapper');
            // 조력자 전용 이펙트 (없으면 플레이어 임팩트 재사용)
            spawnEffect(bossWrap, (window.playerSkillImgSrc || EFFECT_IMG.DAMAGE_TO_PLAYER), 'impact');

            if (typeof ev.monsterHp === 'number' && typeof ev.monsterMaxHp === 'number') {
                setBossHpBar(ev.monsterHp, ev.monsterMaxHp);
                bossMaxHp = ev.monsterMaxHp;
            }
            // 데미지 텍스트
            showDamageEffect(ev.damage, false);
        }

        await delay(Math.max(600, dur) + ALLY_TIMING.lingerAfter);
        resolve();
    });


}