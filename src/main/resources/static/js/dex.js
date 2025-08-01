const dexModal = document.getElementById('dexModal');
const dexTabBtns = document.querySelectorAll(".dex-tab-btn");
const dexTabContents = {
    character: document.getElementById("characterTab"),
    item: document.getElementById("itemTab"),
    monster: document.getElementById("monsterTab"),
    title : document.getElementById("titleTab"),
};
let currentDexData = null;
let selectedCharacterInDetail = null;

async function handleDexClick() {
    const valid = await checkSessionValid();
    if (!valid) return;

    playEffect("se_click2");
    currentDexData = await fetchDexMetaData();
    if (!currentDexData) return;
    if (currentDexData && currentDexData.dexList) {
        document.getElementById("dexDetailPanel").classList.remove("hidden");
    } else {
        document.getElementById("dexDetailPanel").classList.add("hidden");
    }
    // 탭 상태 초기화 (캐릭터 탭 선택)
    dexTabBtns.forEach(b => b.classList.remove("active"));
    dexTabBtns[0].classList.add("active");

    Object.keys(dexTabContents).forEach(key => {
        dexTabContents[key].classList.toggle("hidden", key !== "character");
    });
    document.querySelector(".dex-modal-container").classList.remove("title-tab-active");

    renderCardsByType("character", currentDexData.dexList);
    dexModal.classList.remove("hidden");
    renderDexTabs(); // 이벤트 바인딩은 여기에 유지
}

async function fetchDexMetaData() {
    try {
        const dexRes = await apiRequest('/api/dex/list', 'GET');
        if (dexRes.code !== 'SUCCESS') {
            closeDexModal();
            showMessageModal(dexRes.message || '도감 목록을 불러오지 못했습니다.');
            return null;
        }
        return dexRes.data;
    } catch (e) {
        console.error("도감 메타 요청 실패", e);
        return null;
    }
}

// 탭 버튼 렌더링
function renderDexTabs() {
    dexTabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const type = btn.dataset.tab;

            dexTabBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            Object.keys(dexTabContents).forEach(key => {
                dexTabContents[key].classList.toggle("hidden", key !== type);
            });

            if (type === "title") {
                document.getElementById("dexDetailPanel").classList.add("hidden");
                document.querySelector(".dex-modal-container").classList.add("title-tab-active");
            } else {
                document.getElementById("dexDetailPanel").classList.remove("hidden");
                document.querySelector(".dex-modal-container").classList.remove("title-tab-active");
            }
            if (type === "character") renderCardsByType("character", currentDexData.dexList);
            if (type === "battle") renderCardsByType("battle", currentDexData.battleEquipItemList);
            if (type === "life") renderCardsByType("life", currentDexData.lifeEquipItemsList);
            if (type === "item") renderCardsByType("item", currentDexData.itemList);
            if (type === "monster") renderCardsByType("monster", currentDexData.monsterList);
            if (type === "title") renderCardsByType("title", currentDexData.titleList);
        });
    });

    document.getElementById("dexEquipBtn").addEventListener("click", async () => {
        if (!selectedCharacterInDetail || !selectedCharacterInDetail.id) {
            showMessageModal("선택된 캐릭터 정보가 없습니다.");
            return;
        }

        await setCharacters(selectedCharacterInDetail.id);

        // 🔄 도감 데이터 새로고침
        currentDexData = await fetchDexMetaData();
        renderCardsByType("character", currentDexData.dexList); // 다시 렌더링
    });

    // 세부 아이템 카테고리 탭 클릭 이벤트
    // renderDexTabs 내부
    const itemTypeTabs = document.getElementById("itemTypeTabs");
    [...itemTypeTabs.querySelectorAll("button")].forEach(btn => {
        btn.addEventListener("click", () => {
            const category = btn.dataset.category;

            // 버튼 active 상태 갱신
            [...itemTypeTabs.querySelectorAll("button")].forEach(b =>
                b.classList.remove("active")
            );
            btn.classList.add("active");

            // 전체 아이템 목록 구성 (항상 _category 포함해서)
            const fullList = [
                ...(currentDexData.battleEquipItemList || []).map(i => ({ ...i, _category: '전투' })),
                ...(currentDexData.lifeEquipItemsList || []).map(i => ({ ...i, _category: '생활' })),
                ...(currentDexData.itemList || []).map(i => ({ ...i, _category: '기타' }))
            ];

            // 필터링 후 렌더링
            const filtered = fullList.filter(i => i._category === category);
            renderCardsByType("item", filtered);

            // 첫 번째 아이템 자동 클릭
            if (filtered.length > 0) {
                showDexDetail("item", filtered[0]);
            }
        });
    });
}
// 카드 + 상세정보 렌더링
function renderCardsByType(type, list) {
    const container = dexTabContents[type];

    container.innerHTML = "";

    // 칭호 탭
    if (type === "title") {
        list.sort((a, b) => {
            const sumA = (a.effects || []).reduce((acc, cur) => acc + (cur.effectValue || 0), 0);
            const sumB = (b.effects || []).reduce((acc, cur) => acc + (cur.effectValue || 0), 0);
            return sumA - sumB;
        });
        list.forEach(item => {

            const row = document.createElement("div");
            row.className = "title-row";

            // 왼쪽: 텍스트 정보
            const left = document.createElement("div");
            left.className = "title-text";

            // 오른쪽: 버튼 영역
            const right = document.createElement("div");
            right.className = "title-action";

            // 이름 + 효과 요약
            const titleName = document.createElement("div");
            titleName.className = "title-name";

            // ✅ 이미지 있을 경우 아이콘 추가
            if (item.iconPath) {
                const icon = document.createElement("img");
                icon.src = `${basePath}/${item.iconPath}`;
                icon.alt = "칭호 아이콘";
                icon.className = "title-icon"; // CSS에서 사이즈 조정 필요
                titleName.appendChild(icon);
            }

            const effectsSummary = item.effects.map(e => {
                const label = e.effectType === 'BONUS_ATTACK' ? '공격력 +' : '체력 +';
                return `${label}${e.effectValue}`;
            }).join(', ') || '효과 없음';

            const nameSpan = document.createElement("span");
            nameSpan.innerHTML = `${item.name} <span style="font-size: 0.7rem; color: #ffb347; margin-left: 8px;">(${effectsSummary})</span>`;
            titleName.appendChild(nameSpan);

            // 설명
            const desc = document.createElement("div");
            desc.className = "title-desc";
            desc.textContent = item.description || '';

            // 조건별 진행도 리스트
            const conditionList = document.createElement("div");
            conditionList.className = "title-conditions";
            const conditionGroup = document.createElement("div");
            conditionGroup.className = "progress-wrapper-group";
            // 조건이 없거나 NONE 타입일 경우
            if (item.counterType === 'NONE' || (item.conditions || []).length === 0) {
                const noneLabel = document.createElement("div");
                noneLabel.className = "no-condition-text"; // 필요 시 스타일링
                noneLabel.textContent = "조건 없음";
                conditionList.appendChild(noneLabel);
            } else {
                (item.conditions || []).forEach(c => {
                    const row = document.createElement("div");
                    row.className = "progress-row";

                    const label = document.createElement("span");
                    label.className = "progress-label";
                    label.textContent = c.targetName || `조건 ${c.targetId}`;

                    const bar = document.createElement("div");
                    bar.className = "progress-bar";

                    const fill = document.createElement("div");
                    fill.className = "progress-fill";
                    const percentage = Math.min((c.currentCount / c.requiredCount) * 100, 100);
                    fill.style.width = `${percentage}%`;

                    const text = document.createElement("div");
                    text.className = "progress-text";
                    text.textContent = `${c.currentCount}/${c.requiredCount}`;

                    bar.appendChild(fill);
                    bar.appendChild(text);

                    row.appendChild(label);
                    row.appendChild(bar);
                    conditionGroup.appendChild(row);
                });

                conditionList.appendChild(conditionGroup);
            }

            left.appendChild(titleName);
            left.appendChild(desc);
            left.appendChild(conditionList);

            // 버튼
            const btn = document.createElement("button");
            btn.className = "title-btn";

            if (!item.owned) {
                btn.textContent = "미획득";
                btn.disabled = !item.achieved;
                if (item.achieved) {
                    btn.textContent = "획득";
                    btn.classList.add("equipped");
                    btn.addEventListener("click", async () => {
                        await claimTitle(item.id);
                    });
                }
            } else if (item.equipped) {
                btn.textContent = "착용중";
                btn.classList.add("equipped"); // ✅ 클래스 추가!
                btn.disabled = true;
            } else {
                btn.textContent = "착용";
                btn.addEventListener("click", async () => {
                    await equipTitle(item.id);
                });
            }
            right.appendChild(btn);
            row.appendChild(left);
            row.appendChild(right);
            container.appendChild(row);
        });
        return;
    }

    // ✅ 장착 캐릭터 우선 정렬
    if (type === "character" && currentDexData.equippedDexId) {
        const equippedId = currentDexData.equippedDexId;
        list.sort((a, b) => {
            if (a.id === equippedId) return -1;
            if (b.id === equippedId) return 1;
            return 0;
        });
    }

    if (type === "item" && !list[0]?._category) {
        const fullList = [
            ...(currentDexData.battleEquipItemList || []).map(i => ({ ...i, _category: '전투' })),
            ...(currentDexData.lifeEquipItemsList || []).map(i => ({ ...i, _category: '생활' })),
            ...(currentDexData.itemList || []).map(i => ({ ...i, _category: '기타' }))
        ];
        // 전투만 필터링해서 list 에 할당
        list = fullList.filter(i => i._category === '전투');
    }



    list.forEach((item, index) => {
        const card = document.getElementById("dexSquareCardTemplate").content.cloneNode(true);
        const img = card.querySelector(".card-img");
        const stars = card.querySelector(".rarity-stars");

        // 이미지 경로 설정
        if (type === "character") {
            img.src = `${basePath_image}/character/${item.imagePath}`;
        } else {
            img.src = `${basePath}/${item.imagePath}`;
        }

        img.alt = item.name;
        stars.innerHTML = getStars(item.rarity);

        const cardEl = card.querySelector(".dex-square-card");
        const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
        img.classList.add(rarityClass);
        // 이미지 경로 설정
        img.src = type === "character"
            ? `${basePath_image}/character/${item.imagePath}`
            : `${basePath}/${item.imagePath}`;
        img.alt = item.name;
        stars.innerHTML = getStars(item.rarity);

        // ✅ 미보유 오버레이 추가
        if (type === "character" && item.owned === false) {
            const overlay = document.createElement("div");
            overlay.className = "not-owned-overlay";
            overlay.textContent = "미보유";
            cardEl.appendChild(overlay);
        }

        // ✅ 장착중 뱃지
        if (type === "character" && item.id === currentDexData.equippedDexId) {
            const equipBadge = document.createElement("div");
            equipBadge.className = "equip-badge";
            equipBadge.textContent = "장착중";
            cardEl.appendChild(equipBadge);
        }

        cardEl.addEventListener("click", () => {
            showDexDetail(type, item);
        });

        container.appendChild(card);

        if (index === 0) {
            cardEl.click();
        }
    });
}


function showDexDetail(type, item) {
    document.getElementById("dexDetailPanel").classList.remove("hidden");

    const detailImg = document.getElementById("detailImage");
    const notOwnedOverlay = document.getElementById("notOwnedOverlay");
    const detailEffects = document.getElementById("detailEffects");
    const detailLevelInfo = document.getElementById("detailLevelInfo");
    const detailAffinityInfo = document.getElementById("detailAffinityInfo");
    const detailStat = document.getElementById("detailStat");
    const detailEquip = document.getElementById("detailEquip");

    if (type === "character") {
        detailImg.src = `${basePath_image}/character/${item.imagePath}`;
    } else {
        detailImg.src = `${basePath}/${item.imagePath}`;
    }

    const itemTypeTabs = document.getElementById("itemTypeTabs");
    if (type === "item") {
        itemTypeTabs.classList.remove("hidden");

        [...itemTypeTabs.querySelectorAll("button")].forEach(btn => {
            btn.classList.toggle("active", btn.dataset.category === item._category);
        });
    } else {
        itemTypeTabs.classList.add("hidden");
    }

    // 기본 정보 설정
    document.getElementById("detailName").textContent = item.name;
    const rarityClass = `rarity-background-${item.rarity?.toLowerCase()}`;
    detailImg.className = `detail-image ${rarityClass}`;

    const characterIcon = document.getElementById("characterIcon");

    if (type === "character") {
        selectedCharacterInDetail = item;
        document.querySelector("#effectCondition .effect-label").textContent = "획득처";
        characterIcon.style.display = item.attributeIconPath ? "block" : "none";

        if (item.attributeIconPath) {
            characterIcon.src = `${basePath}/${item.attributeIconPath}`;
            characterIcon.style.display = "block";
        } else {
            characterIcon.style.display = "none";
        }

        // 캐릭터: 레벨, XP, 친밀도 표시
        document.getElementById("detailLevel").textContent = `LV${item.level || 1}`;
        const xpPercent = item.currentXp && item.maxXp ? (item.currentXp / item.maxXp) * 100 : 0;
        document.getElementById("detailXpFill").style.width = `${xpPercent}%`;
        document.getElementById("detailXpText").textContent = `${item.currentXp || 0}/${item.maxXp || 100}`;
        document.getElementById("detailAffinity").textContent = `${item.affinity || 0}`;
        detailLevelInfo.style.display = "flex";
        detailAffinityInfo.style.display = "flex";

        // detailEffects 영역 표시
        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";
        document.getElementById("detailCondition").textContent = item.condition || "";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";

        // 캐릭터 스탯 표시
        detailStat.style.display = "flex";
        document.getElementById("detailAtk").textContent = item.basePower || 0;
        document.getElementById("detailHp").textContent = item.baseHp || 0;

        // 캐릭터 장착, 강화 버튼 표시
        detailEquip.style.display = "flex";
        const equipBtn = document.getElementById("dexEquipBtn");

        if (item.owned === false) {
            // 미보유일 경우 버튼 숨김
            detailEquip.style.display = "none";
            notOwnedOverlay.classList.remove("hidden");
        } else {
            notOwnedOverlay.classList.add("hidden");

            // 장착 버튼
            if (item.id === currentDexData.equippedDexId) {
                equipBtn.textContent = "장착중";
                equipBtn.disabled = true;
                equipBtn.classList.add("equipped");
            } else {
                equipBtn.textContent = "장착";
                equipBtn.disabled = false;
                equipBtn.classList.remove("equipped");
            }
        }

        // 미보유 오버레이 설정 (캐릭터 탭에만 적용)
        if (item.owned === false) {
            notOwnedOverlay.classList.remove("hidden");
        } else {
            notOwnedOverlay.classList.add("hidden");
        }

    } else if (type === "monster") {
        detailEquip.style.display = "none";
        characterIcon.style.display = "none";
        detailLevelInfo.style.display = "none";
        detailAffinityInfo.style.display = "none";
        notOwnedOverlay.classList.add("hidden");

        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";

        // ⭐ 드랍 아이템 이름 나열
        document.querySelector("#effectCondition .effect-label").textContent = "드랍템";
        const dropNames = (item.dropItemList || []).map(it => it.name).join(", ");
        document.getElementById("detailCondition").textContent = dropNames || "없음";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";

        detailStat.style.display = "flex";
        document.getElementById("detailAtk").textContent = item.basePower || 0;
        document.getElementById("detailHp").textContent = item.baseHp || 0;
    } else {
        document.querySelector("#effectCondition .effect-label").textContent = "획득처";
        // ✅ 레벨/친밀도 영역 숨김
        characterIcon.style.display = "none";
        detailLevelInfo.style.display = "none";
        detailAffinityInfo.style.display = "none";
        detailEquip.style.display = "none";
        // ✅ 미보유 오버레이 숨김
        notOwnedOverlay.classList.add("hidden");

        // detailEffects 영역 표시
        detailEffects.style.display = "block";
        document.getElementById("detailDesc").textContent = item.description || "";
        document.getElementById("detailCondition").textContent = item.condition || "";
        document.getElementById("detailRarity").textContent = item.rarity || "Common";


        if (type === "item") {
            const activeItemTab = document.querySelector("#itemTypeTabs .active");
            const selectedCategory = activeItemTab?.dataset?.category;
            if (selectedCategory === "전투") {
                detailStat.style.display = "flex";
                document.getElementById("detailAtk").textContent = item.basePower || 0;
                document.getElementById("detailHp").textContent = item.baseHp || 0;
            } else {
                detailStat.style.display = "none";
            }
        }
    }
}

async function setCharacters(selectedCharacterId) {
    try {
        const res = await apiRequestJson('/api/char/setDex', 'POST', {
            dexId: selectedCharacterId
        });

        if (res.code !== 'SUCCESS') {
            showMessageModal(res.message || '대표 감자 설정에 실패했습니다.');
        } else {
            await loadCharacterBasicInfo(); // 메인 화면 갱신
            // ✅ 장착 버튼 텍스트 갱신
            const equipBtn = document.getElementById("dexEquipBtn");

            if (selectedCharacterInDetail) {
                selectedCharacterInDetail.equipped = true;
            }
        }
    } catch (err) {
        console.error('대표 감자 설정 실패:', err);
        showMessageModal('서버 오류로 대표 감자 설정에 실패했습니다.');
    }
}

function getStars(rarity) {
    const starMap = {
        "COMMON": 1,
        "UNCOMMON": 2,
        "RARE": 3,
        "EPIC": 4,
        "LEGENDARY": 5
    };
    return "★".repeat(starMap[rarity] || 0);
}

function closeDexModal() {
    dexModal.classList.add('hidden');
    document.querySelector(".dex-modal-container").classList.remove("title-tab-active");
}

async function claimTitle(titleId) {
    try {
        const res = await apiRequestJson('/api/action/title/claim', 'POST', { titleId });
        if (res.code === 'SUCCESS') {
            showMessageModal("칭호를 획득했습니다!");
            // 데이터 새로고침
            currentDexData = await fetchDexMetaData();
            renderCardsByType("title", currentDexData.titleList);
        } else {
            showMessageModal(res.message || '획득 실패');
        }
    } catch (err) {
        console.error("칭호 획득 실패", err);
        showMessageModal("서버 오류로 칭호를 획득하지 못했습니다.");
    }
}

async function equipTitle(titleId) {
    try {
        const res = await apiRequestJson('/api/action/title/equip', 'POST', { titleId });
        if (res.code === 'SUCCESS') {
            showMessageModal("칭호를 착용했습니다!");
            currentDexData = await fetchDexMetaData();
            renderCardsByType("title", currentDexData.titleList);
            loadCharacterBasicInfo();
        } else {
            showMessageModal(res.message || '착용 실패');
        }
    } catch (err) {
        console.error("칭호 착용 실패", err);
        showMessageModal("서버 오류로 칭호를 착용하지 못했습니다.");
    }
}
