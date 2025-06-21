//
//
// /* 실제 액션 모달 */
// const activityModal = document.getElementById('activityModal');
//
// async function handleActivityClick() {
//     increaseCombo();
//     const tooltip = document.querySelector('.activity-click-tooltip');
//     if (tooltip) tooltip.remove();
//
//     if (dropTable.length === 0) return;
//
//     const rand = Math.random();
//     let sum = 0;
//     for (const drop of dropTable) {
//         sum += drop.dropRate;
//         if (rand <= sum) {
//             const quantity = getRandomInt(drop.minQuantity, drop.maxQuantity);
//             if (!droppedItems[drop.itemId]) {
//                 droppedItems[drop.itemId] = { count: quantity, name: drop.name, iconPath: drop.iconPath };
//             } else {
//                 droppedItems[drop.itemId].count += quantity;
//             }
//             const expPerItem = drop.expReward || 0;
//             gainedExp += quantity * expPerItem;
//             createActionTextWithImage(drop.iconPath, 'activityModal');
//             break;
//         }
//     }
//     renderGainedItems();
//
// }
// function getRandomInt(min, max) {
//     return Math.floor(Math.random() * (max - min + 1)) + min;
// }
//
// async function completeActivity() {
//     playEffect("se_coin");
//     activityModal.classList.add("hidden");
//
//     const items = Object.entries(droppedItems).map(([itemId, { count }]) => ({
//         itemId: parseInt(itemId),
//         count
//     }));
//     if (items.length === 0) return;
//
//     const roundedExp = Math.floor(gainedExp);
//     try {
//         const response = await apiRequestJson(`/api/action/addItems`, 'POST', {
//             activityType: currentActivityType,
//             items,
//             exp: roundedExp,
//             maxCombo: maxCombo
//         });
//         loadCharacterBasicInfo();
//     } catch (e) {
//         showMessageModal("아이템 추가 중 오류가 발생했습니다.");
//     }
//
//     droppedItems = {};
//     comboCount = 0;
//     maxCombo = 0;
//     removeComboVisual();
//     document.querySelectorAll("#activityModalContent .get-item-image-text").forEach(el => el.remove());
// }
//
//
// function createActionTextWithImage(imgSrc, modalId) {
//
//     const actionWrapper = document.createElement('div');
//     actionWrapper.className = 'get-item-image-text';
//
//     const img = document.createElement('img');
//     img.src = basePath + imgSrc;
//     img.alt = '+1 item';
//     img.className = 'get-item-image';
//
//     const plusOne = document.createElement('span');
//     plusOne.textContent = '+1';
//     plusOne.className = 'get-item-plusone';
//
//     actionWrapper.appendChild(img);
//     actionWrapper.appendChild(plusOne);
//
//     document.querySelector(`#${modalId}Content`).appendChild(actionWrapper);
//
//     setTimeout(() => {
//         actionWrapper.remove();
//     }, 1000);
// }
//
// function renderGainedItems() {
//     const container = document.getElementById('gainedItems');
//     container.innerHTML = ''; // ✅ 항상 초기화하고 새로 생성
//
//     dropTable.forEach(drop => {
//         const count = droppedItems[drop.itemId]?.count || 0;
//
//         const wrapper = document.createElement('div');
//         wrapper.className = 'gained-item';
//         wrapper.dataset.itemId = drop.itemId;
//
//         const img = document.createElement('img');
//         img.src = basePath + drop.iconPath;
//         img.alt = drop.name;
//
//         const countSpan = document.createElement('span');
//         countSpan.textContent = `x${count}`;
//         countSpan.className = 'gained-count';
//
//         wrapper.appendChild(img);
//         wrapper.appendChild(countSpan);
//         container.appendChild(wrapper);
//     });
//
//     // 수량만 갱신
//     dropTable.forEach(drop => {
//         const count = droppedItems[drop.itemId]?.count || 0;
//         const wrapper = container.querySelector(`[data-item-id='${drop.itemId}']`);
//         if (wrapper) {
//             const countSpan = wrapper.querySelector('.gained-count');
//             countSpan.textContent = `x${count}`;
//         }
//     });
// }
//
//
// function increaseCombo() {
//     comboCount++;
//
//     const maxDisplayEl = document.getElementById('maxComboDisplay');
//     const currentMaxText = maxDisplayEl.textContent.replace(/[^0-9]/g, '');
//     const currentMaxValue = parseInt(currentMaxText, 10) || 0;
//
//     if (comboCount > currentMaxValue) {
//         maxDisplayEl.textContent = `MAX: ${comboCount}`;
//         maxCombo = comboCount; // 이 값은 최종 제출용
//     }
//
//     renderComboVisual(comboCount);
//
//     if (comboTimer) clearTimeout(comboTimer);
//     comboTimer = setTimeout(() => {
//         comboCount = 0;
//         removeComboVisual();
//     }, 1500);
// }
//
//
// function renderComboVisual(count) {
//     if (count < 2) return;
//
//     const comboEl = document.getElementById('comboDisplay');
//     if (!comboEl) return;
//
//     comboEl.classList.remove('hidden');
//     comboEl.textContent = `COMBO ${count}`;
//
//     comboEl.style.color = getComboColor(count);
//
//     comboEl.classList.remove('combo-pop');
//     void comboEl.offsetWidth;
//     comboEl.classList.add('combo-pop');
// }
// function removeComboVisual() {
//     const el = document.getElementById('comboDisplay');
//     if (el) el.classList.add('hidden'); // ✅ 삭제 말고 숨기기만
// }
//
// function getComboColor(count) {
//     if (count >= 200) return '#ff1aff'; // 보라
//     if (count >= 150) return '#00ffff'; // 청록
//     if (count >= 100) return '#ffff00'; // 노랑
//     if (count >= 50)  return '#fa6719'; // 주황
//     return '#9cffb2'; // 감자색
// }
//
//
//
// function preloadDropImages() {
//     dropTable.forEach(drop => {
//         const img = new Image();
//         img.src = basePath + drop.iconPath;
//     });
// }
