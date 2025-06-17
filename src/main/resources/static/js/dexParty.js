let selectedSlotIndex = null;
let party = [null, null, null]; // 각 슬롯에 어떤 감자가 들어갔는지
const partyModal = document.getElementById('partyModal');
const potatoListContainer = document.getElementById('potatoList');

document.getElementById('closePartyModal').addEventListener('click', () => {
    partyModal.classList.add("hidden");
    selectedSlotIndex = null;
});

async function handlePartyClick() {
    playEffect("se_click2");
    partyModal.classList.remove("hidden");
    await fetchOwnedPotatoes();
    renderPartySlots();
}
// 파티 슬롯 클릭 시
document.querySelectorAll('.party-slot').forEach((slot, index) => {
    slot.addEventListener('click', () => {
        selectedSlotIndex = index;
        playEffect("se_click1");
        // 선택 표시
        document.querySelectorAll('.party-slot').forEach(s => s.classList.remove('selected'));
        slot.classList.add('selected');
    });
});
function renderPartySlots() {
    document.querySelectorAll('.party-slot').forEach((slot, i) => {
        slot.innerHTML = '';
        if (party[i]) {
            const img = document.createElement('img');
            img.src = basePath_image + "/character/" + party[i].dexImage;
            img.alt = party[i].dexName;
            slot.appendChild(img);
        } else {
            slot.innerHTML = '<span class="plus">+</span>';
        }
    });
}

async function fetchOwnedPotatoes() {
    try {
        const res = await fetch('/api/char/owned');
        const json = await res.json();
        if (json.code === "SUCCESS") {
            const potatoList = json.data.ownedDexList;
            renderPotatoList(potatoList);
        } else {
            alert("감자 데이터를 불러오지 못했습니다.");
        }
    } catch (e) {
        console.error("에러 발생:", e);
    }
}

function renderPotatoList(potatoList) {
    const container = document.getElementById('potatoList');
    container.innerHTML = '';
    potatoList.forEach(p => {
        const card = document.createElement('div');
        card.className = 'potato-card';

        const xpPercent = Math.floor((p.xp / p.maxExp) * 100);

        card.innerHTML = `
      <div class="card-attribute">
        <img src="${basePath}/${p.attributeIconPath}" alt="${p.attribute}">
      </div>
      <img src="${basePath_image}/character/${p.dexImage}" alt="${p.dexName}" />
      <div class="card-name">${p.dexName}</div>
      <div class="card-level">Lv.${p.level}</div>
      <div class="xp-bar-container">
        <div class="xp-bar" style="width: ${xpPercent}%;"></div>
        <div class="xp-text">${p.xp} / ${p.maxExp}</div>
      </div>
    `;

        card.addEventListener('click', () => {
            if (selectedSlotIndex !== null) {
                party[selectedSlotIndex] = p;
                renderPartySlots();
                playEffect("se_select");
            }
        });

        container.appendChild(card);
    });
}