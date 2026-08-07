// js/achieve.js
// 업적 모달: 데이터 로드 + 렌더 + 이벤트

(function(){
    const CATEGORY = ['GROWTH','ADVENTURE','CRAFT','COLLECTION','EVENT'];

    // 엘리먼트
    const achieveModal = document.getElementById('achieveModal');

    // Fame HUD
    const fameNameEl  = document.getElementById('fameName');
    const fameLevelEl = document.getElementById('fameLevel');
    const fameDescEl  = document.getElementById('fameDesc');
    const famePointEl = document.getElementById('famePoint');
    const fameXpFill  = document.getElementById('fameXpFill');
    const fameXpText  = document.getElementById('fameXpText');

    // Tabs
    const tabButtons = document.querySelectorAll('.achieve-tab-btn');

    // 리스트 / 상세
    const seriesListEl   = document.getElementById('achieveSeriesList');
    const emptyStateEl   = document.getElementById('achieveEmptyState');
    const detailHeaderEl = document.getElementById('achieveDetailHeader');
    const seriesNameEl   = document.getElementById('seriesName');
    const seriesDescEl   = document.getElementById('seriesDesc');
    const seriesProgFill = document.getElementById('seriesProgressFill');
    const seriesProgText = document.getElementById('seriesProgressText');
    const entryListEl    = document.getElementById('achieveEntryList');

    // 템플릿
    const tplSeries = document.getElementById('achieveSeriesItemTemplate');
    const tplEntry  = document.getElementById('achieveEntryRowTemplate');

    // 상태
    let currentCategory = 'GROWTH';
    let cachedSeriesByCategory = {}; // {category: seriesList[]}
    let currentSeries = null;

    // 외부에서 호출
    window.openAchieveModal = async function openAchieveModal() {
        achieveModal.classList.remove('hidden');
        await ensureFame();
        await switchCategory(currentCategory);
        // 첫 시리즈 자동 선택
        const first = seriesListEl.querySelector('.achieve-series-card');
        if (first) first.click();
        lockBodyScroll(true);
    };

    window.closeAchieveModal = function closeAchieveModal() {
        achieveModal.classList.add('hidden');
        lockBodyScroll(false);
    };

    function lockBodyScroll(lock){
        document.body.style.overflow = lock ? 'hidden' : '';
    }

    // 탭 클릭
    tabButtons.forEach(btn=>{
        btn.addEventListener('click', async ()=>{
            tabButtons.forEach(b=>b.classList.remove('active'));
            btn.classList.add('active');
            currentCategory = btn.dataset.category || 'GROWTH';
            await switchCategory(currentCategory);
            // 상세 초기화
            detailHeaderEl.classList.add('hidden');
            entryListEl.innerHTML = '';
        });
    });

    // ========== API ==========
    async function fetchUserFame(){
        const url = apiPath('/api/achievement/userFame');
        const res = await apiRequest(url, 'GET');
        if (res?.success) return res.data;
        throw new Error('명성 정보 로드 실패');
    }

    async function fetchSeriesByCategory(category){
        // 백엔드는 소문자 path: /api/achievement/category/growth
        const path = category.toLowerCase();
        const url = apiPath(`/api/achievement/category/${path}`);
        const res = await apiRequest(url, 'GET');
        if (res?.success) return res.data || [];
        return [];
    }

    // ========== Fame 렌더 ==========
    async function ensureFame(){
        try{
            const data = await fetchUserFame();
            const { fameName, fameDesc, fameLevel, xp, maxXp, famePoint } = data || {};
            fameNameEl.textContent  = fameName ?? '-';
            fameDescEl.textContent  = fameDesc ?? '';
            fameLevelEl.textContent = `Lv.${fameLevel ?? 1}`;
            famePointEl.textContent = (famePoint ?? 0).toLocaleString();

            const pct = (maxXp && maxXp>0) ? Math.min(100, Math.floor((xp/maxXp)*100)) : 0;
            fameXpFill.style.width = pct + '%';
            fameXpText.textContent = `${xp ?? 0} / ${maxXp ?? 0}`;
        }catch(e){
            console.warn('명성 정보 로드 실패:', e);
            // 표시만 초기화
            fameNameEl.textContent  = '명성';
            fameDescEl.textContent  = '정보를 불러오지 못했습니다.';
            fameLevelEl.textContent = 'Lv.-';
            famePointEl.textContent = '0';
            fameXpFill.style.width  = '0%';
            fameXpText.textContent  = '0 / 0';
        }
    }

    // ========== Series 리스트 ==========
    async function switchCategory(category){
        // 캐시 활용
        if (!cachedSeriesByCategory[category]) {
            const list = await fetchSeriesByCategory(category);
            cachedSeriesByCategory[category] = list;
        }
        renderSeriesList(cachedSeriesByCategory[category] || []);
    }

    function renderSeriesList(seriesList){
        seriesListEl.innerHTML = '';
        emptyStateEl.classList.toggle('hidden', seriesList.length>0);

        seriesList.forEach(series=>{
            const node = document.importNode(tplSeries.content, true);
            const card   = node.querySelector('.achieve-series-card');
            const head   = node.querySelector('.series-card-head');
            const title  = node.querySelector('.series-card-title');
            const desc   = node.querySelector('.series-card-desc');
            const cat    = node.querySelector('.series-card-category');
            const pbar   = node.querySelector('.mini-progress-fill');
            const ptxt   = node.querySelector('.mini-progress-text');
            const body   = node.querySelector('.series-card-body');
            const listEl = node.querySelector('.series-entry-list');

            card.dataset.seriesId = series.id;
            title.textContent = series.name || '-';
            desc.textContent  = series.description || '';
            cat.textContent   = toKoreanCategory(series.category || currentCategory);

            // 진행도
            const {progressCount, totalCount, pct} = calcSeriesProgress(series);
            pbar.style.width = `${pct}%`;
            ptxt.textContent = `${progressCount}/${totalCount}`;

            // 상세 엔트리 미리 렌더 (처음엔 접힘)
            renderEntryListInto(series.entries || [], listEl);

            // 클릭으로 토글 (아코디언: 하나만 열리게)
            head.addEventListener('click', ()=>{
                const isOpen = card.classList.contains('open');
                // 다른 카드 닫기
                seriesListEl.querySelectorAll('.achieve-series-card.open').forEach(el=>{
                    if (el!==card) el.classList.remove('open');
                });
                // 현재 토글
                card.classList.toggle('open', !isOpen);
            });

            seriesListEl.appendChild(node);
        });
    }

    function renderEntryListInto(entries, containerEl){
        containerEl.innerHTML = '';
        if (!entries.length){
            containerEl.innerHTML = `<div class="achieve-empty small">엔트리가 없습니다.</div>`;
            return;
        }

        entries
            .slice()
            .sort((a,b)=> (a.orderInSeries||0)-(b.orderInSeries||0))
            .forEach(entry=>{
                const node = document.importNode(tplEntry.content, true);
                const row   = node.querySelector('.entry-row');
                const desc  = node.querySelector('.entry-desc');
                const req   = node.querySelector('.entry-require');
                const rbox  = node.querySelector('.entry-rewards');
                const btn   = node.querySelector('.entry-claim-btn');
                const badge = node.querySelector('.entry-sticker');

                row.dataset.entryId = entry.id;
                desc.textContent = entry.description || '-';
                req.textContent  = requireText(entry);

                // 리워드
                rbox.innerHTML = '';
                (entry.rewards || []).forEach(r=>{
                    const span = document.createElement('span');
                    span.className = 'reward-chip';
                    span.textContent = rewardText(r);
                    rbox.appendChild(span);
                });

                // 상태/라벨
                const st = getEntryStatus(entry);
                badge.textContent = st.text;
                badge.className = `entry-sticker ${st.className}`;

                // 수령 버튼
                const claimable = st.key === 'COMPLETED';
                btn.disabled = !claimable;
                btn.classList.toggle('claimable', claimable);
                btn.addEventListener('click', ()=> {
                    if (!claimable) return;
                    claimEntry(entry.id);
                });

                containerEl.appendChild(node);
            });
    }

    /** entry.user로 상태 추정 → 라벨/색상 매핑 */
    function getEntryStatus(entry){
        const u = entry.user || null;

        // enabled false면 비활성
        if (entry.enabled === false) {
            return { key:'DISABLED', text:'비활성', className:'status-disabled' };
        }

        // 백엔드 값 가정:
        // u.completed: 완료 여부
        // u.claimed: 보상 수령 여부
        // u.progressValue: 진행 수치(있을 수도 있음)
        if (u?.claimed) {
            return { key:'CLAIMED', text:'수령완료', className:'status-claimed' };
        }
        if (u?.completed) {
            return { key:'COMPLETED', text:'완료', className:'status-completed' };
        }
        if (typeof u?.progressValue === 'number' && u.progressValue > 0) {
            return { key:'IN_PROGRESS', text:'진행중', className:'status-progress' };
        }
        return { key:'NOT_STARTED', text:'미진행', className:'status-default' };
    }


    function calcSeriesProgress(series){
        const entries = series?.entries || [];
        const total = entries.length;
        // 백엔드에서 user 진행이 entry.user 형태로 올 수도, 안 올 수도 있음.
        // 여기선 user?.status === 'COMPLETED' 또는 user?.claimed === true 등을 가정.
        let done = 0;
        for (const e of entries){
            const u = e.user;
            if (!u) continue;
            if (u.completed === true || u.status === 'COMPLETED' || u.claimed === true) done++;
        }
        const pct = total>0 ? Math.floor((done/total)*100) : 0;
        return {progressCount:done, totalCount:total, pct};
    }

    // ========== Series 상세 ==========
    function selectSeries(series){
        currentSeries = series;

        detailHeaderEl.classList.remove('hidden');
        seriesNameEl.textContent = series.name || '-';
        seriesDescEl.textContent = series.description || '';

        const {progressCount, totalCount, pct} = calcSeriesProgress(series);
        seriesProgFill.style.width = `${pct}%`;
        seriesProgText.textContent = `${progressCount} / ${totalCount}`;

        renderEntryList(series.entries || []);
        // 선택 강조
        seriesListEl.querySelectorAll('.achieve-series-card').forEach(el=>el.classList.remove('selected'));
        const sel = seriesListEl.querySelector(`.achieve-series-card[data-series-id="${series.id}"]`);
        if (sel) sel.classList.add('selected');
    }

    function renderEntryList(entries){
        entryListEl.innerHTML = '';
        if (!entries.length){
            entryListEl.innerHTML = `<div class="achieve-empty small">해당 시리즈에 등록된 엔트리가 없습니다.</div>`;
            return;
        }

        entries
            .slice()
            .sort((a,b)=> (a.orderInSeries||0)-(b.orderInSeries||0))
            .forEach(entry=>{
                const node = document.importNode(tplEntry.content, true);
                const row   = node.querySelector('.entry-row');
                const desc  = node.querySelector('.entry-desc');
                const req   = node.querySelector('.entry-require');
                const rbox  = node.querySelector('.entry-rewards');
                const btn   = node.querySelector('.entry-claim-btn');

                row.dataset.entryId = entry.id;
                desc.textContent = entry.description || '-';
                req.textContent  = requireText(entry);

                // 리워드 뱃지
                rbox.innerHTML = '';
                (entry.rewards || []).forEach(r=>{
                    const span = document.createElement('span');
                    span.className = 'reward-chip';
                    // 가장 기본: FAME_POINT / GOLD / TITLE 등 추후 확장
                    span.textContent = rewardText(r);
                    rbox.appendChild(span);
                });

                // 진행 상태 -> 수령 가능 여부
                const u = entry.user;
                const claimable = !!(u && u.completed === true && u.claimed !== true);
                btn.disabled = !claimable;
                btn.classList.toggle('claimable', claimable);

                btn.addEventListener('click', ()=> {
                    if (!claimable) return;
                    claimEntry(entry.id);
                });

                entryListEl.appendChild(node);
            });
    }

    function toKoreanCategory(cat){
        switch(String(cat).toUpperCase()){
            case 'GROWTH': return '성장';
            case 'ADVENTURE': return '모험';
            case 'CRAFT': return '제작';
            case 'COLLECTION': return '수집';
            case 'EVENT': return '이벤트';
            default: return cat;
        }
    }

    function requireText(entry){
        // 대표적으로 REACH_LEVEL만 우선. 추후 타입별 문구 확장.
        // 캐릭터 아이콘까지 넣으려면 entry.characterId로 이미지 불러와도 됨.
        const type = entry.requirementType || '';
        if (type === 'REACH_LEVEL'){
            return `레벨 ${entry.requirementValue} 달성`;
        }
        return type;
    }

    function rewardText(r){
        const t = r.rewardType || '';
        if (t === 'FAME_POINT') return `명성 +${r.amount || 0}`;
        if (t === 'GOLD')       return `골드 +${r.amount || 0}`;
        if (t === 'TITLE')      return `칭호 획득`;
        return t;
    }

    // 수령 (엔드포인트 미정 → TODO)
    async function claimEntry(entryId){
        try{
            // TODO: 백엔드 확정되면 수정
            // const res = await apiRequestJson(apiPath(`/api/achievement/claim`), 'POST', { entryId });
            // if (!res?.success) throw new Error(res?.message || '수령 실패');

            showMessage('준비중입니다. (claim API 확정 필요)');
        }catch(e){
            console.warn(e);
            showMessage('보상 수령에 실패했습니다.');
        }
    }

    // 공용 메시지 모달 사용
    function showMessage(msg){
        const modal = document.getElementById('messageModal');
        const text  = document.getElementById('messageText');
        const btn   = document.getElementById('messageCloseBtn');
        if (!modal || !text || !btn) return alert(msg);
        text.textContent = msg;
        modal.classList.remove('hidden');
        btn.onclick = ()=> modal.classList.add('hidden');
    }

    // ESC 닫기
    document.addEventListener('keydown', (e)=>{
        if (e.key === 'Escape' && !achieveModal.classList.contains('hidden')){
            closeAchieveModal();
        }
    });

    // 캐릭터 모달에서 열기 버튼(이미 존재) 연결: openAchieveModalBtn
    const openBtn = document.getElementById('openAchieveModalBtn');
    if (openBtn){
        openBtn.addEventListener('click', ()=>{
            const characterModal = document.getElementById('characterModal');
            if (characterModal) characterModal.classList.add('hidden');
            openAchieveModal();
        });
    }
})();
