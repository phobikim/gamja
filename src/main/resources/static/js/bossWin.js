// helper
const $  = (sel, root=document) => root.querySelector(sel);

// basePath_image 기본값 보정
window.basePath_image = window.basePath_image || '';

function updateRewardUI({ dexName, charImage, beforeLevel, afterLevel, beforeXp, afterXp, maxExp, gainedXp }) {
    const charImg     = $('.reward-char-img');
    const levelEl     = $('.reward-char-level');
    const currentXpEl = $('.current-xp');
    const maxXpEl     = $('.max-xp');
    const xpGainEl    = $('.xp-gain-text');
    const xpBarFill   = $('.xp-bar-fill');

    if (charImg && charImage) {
        charImg.src = `${basePath_image}/character/${charImage}`;
        charImg.alt = dexName || '캐릭터';
    }

    const beforeLv = Number.isFinite(beforeLevel) ? beforeLevel : 1;
    const afterLv  = Number.isFinite(afterLevel)  ? afterLevel  : beforeLv;
    const bXp = Number.isFinite(beforeXp) ? beforeXp : 0;
    const aXp = Number.isFinite(afterXp)  ? afterXp  : bXp;
    const max = Math.max(1, Number.isFinite(maxExp) ? maxExp : 1);
    const gain = Number.isFinite(gainedXp) ? gainedXp : 0;

    levelEl && (levelEl.textContent = afterLv);
    currentXpEl && (currentXpEl.textContent = aXp);
    maxXpEl && (maxXpEl.textContent = max);
    xpGainEl && (xpGainEl.textContent = `+${gain} XP`);

    if (afterLv > beforeLv) {
        levelEl?.classList.add('level-up-highlight');
        setTimeout(() => levelEl?.classList.remove('level-up-highlight'), 500);
    }

    if (xpBarFill) {
        const startXp = (afterLv > beforeLv) ? 0 : bXp;
        const startPercent = (startXp / max) * 100;
        const endPercent   = (aXp / max) * 100;
        xpBarFill.style.width = `${startPercent}%`;
        setTimeout(() => { xpBarFill.style.width = `${endPercent}%`; }, 250);
    }
}

function renderLootItems(items = []) {
    const wrap = $('.loot-items-list');
    if (!wrap) return;

    wrap.innerHTML = '';

    if (!Array.isArray(items) || items.length === 0) {
        const msg = document.createElement('p');
        msg.style.opacity = .8;
        msg.style.margin = '10px 0 0';
        msg.textContent = '획득한 전리품이 없습니다.';
        wrap.appendChild(msg);
        return;
    }

    items.forEach((it, idx) => {
        const card = document.createElement('div');
        card.className = 'loot-item-card animate-in';
        card.style.transitionDelay = `${Math.min(idx * 60, 400)}ms`;
        card.innerHTML = `
          <img class="loot-item-image" src="${basePath+it.iconPath || ''}" alt="">
          <div class="loot-item-name">${it.name || '???'}</div>
          <div class="loot-item-count">x${it.count ?? 1}</div>
        `;
        wrap.appendChild(card);
    });
}

function initWinPage() {
    let data = null;
    try {
        const raw = localStorage.getItem('bossWinData');
        data = raw ? JSON.parse(raw) : null;
    } catch (_) { /* noop */ }

    if (data?.reward) {
        updateRewardUI(data.reward);
    } else {
        updateRewardUI({ beforeLevel:1, afterLevel:1, beforeXp:0, afterXp:0, maxExp:100, gainedXp:0 });
    }
    renderLootItems(data?.lootItems || []);

    // 메인화면 가기
    $('.btn-main')?.addEventListener('click', () => {
        try { playEffect?.('se_click2'); } catch (_) {}
        window.location.replace('/char.html');
    });
}

document.addEventListener('DOMContentLoaded', initWinPage);
