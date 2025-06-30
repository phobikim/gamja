let currentNoticeIndex = 0;
let noticeList = [];


document.getElementById('noticeIconBtn').addEventListener('click', openNoticeModal);
async function openNoticeModal() {
    const valid = await checkSessionValid();
    if (!valid) return;

    const res = await apiRequest('/api/maintenance/notices', 'GET');
    if (res.code !== 'SUCCESS') {
        showMessageModal('공지 불러오기 실패');
        return;
    }

    noticeList = res.data;
    currentNoticeIndex = 0;

    // 좌우 버튼 이미지 세팅
    const leftBtn = document.getElementById('noticePrevBtn');
    const rightBtn = document.getElementById('noticeNextBtn');

    leftBtn.style.backgroundImage = `url('${basePath_image}/icons/notice_btn_left.png')`;
    rightBtn.style.backgroundImage = `url('${basePath_image}/icons/notice_btn_right.png')`;

    renderNoticePanel();
    document.getElementById('noticeModal').classList.remove('hidden');
}

function closeNoticeModal() {
    document.getElementById('noticeModal').classList.add('hidden');
}

function renderNoticePanel() {
    const notice = noticeList[currentNoticeIndex];
    if (!notice) return;

    // 제목, 날짜, 내용
    document.getElementById('noticeTitle').textContent = `${notice.title}`;
    document.getElementById('noticeDuration').textContent = formatNoticeTime(notice);
    document.getElementById('noticeContent').textContent = notice.content;

    // 페이지 표시
    const pageEl = document.getElementById('noticePageIndicator');
    if (pageEl) {
        pageEl.textContent = `${currentNoticeIndex + 1} / ${noticeList.length}`;
    }

    // 좌우 버튼
    document.getElementById('noticePrevBtn').style.display =
        currentNoticeIndex > 0 ? 'block' : 'none';
    document.getElementById('noticeNextBtn').style.display =
        currentNoticeIndex < noticeList.length - 1 ? 'block' : 'none';

    // 패치노트 리스트 처리
    const patchListEl = document.getElementById('noticePatchList');
    patchListEl.innerHTML = ''; // 초기화

    if (Array.isArray(notice.patchNotes) && notice.patchNotes.length > 0) {
        patchListEl.classList.remove('hidden');

        notice.patchNotes.forEach(note => {
            const item = document.createElement('div');
            item.className = 'patch-note-card';
            item.textContent = `${note}`;
            patchListEl.appendChild(item);
        });
    } else {
        patchListEl.classList.add('hidden');
    }
}

document.getElementById('noticePrevBtn').addEventListener('click', () => {
    if (currentNoticeIndex > 0) {
        currentNoticeIndex--;
        renderNoticePanel();
    }
});

document.getElementById('noticeNextBtn').addEventListener('click', () => {
    if (currentNoticeIndex < noticeList.length - 1) {
        currentNoticeIndex++;
        renderNoticePanel();
    }
});

function formatDate(dateStr) {
    const d = new Date(dateStr);
    return `${d.getFullYear()}.${(d.getMonth() + 1).toString().padStart(2, '0')}.${d
        .getDate()
        .toString()
        .padStart(2, '0')}`;
}

function formatNoticeTime(notice) {
    const type = notice.type;
    const start = new Date(notice.startTime);
    const end = new Date(notice.endTime);

    const formatDate = (d) =>
        `${d.getFullYear()}.${(d.getMonth() + 1).toString().padStart(2, '0')}.${d.getDate().toString().padStart(2, '0')}`;

    const formatTime = (d) =>
        `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;

    if (type === 'SYSTEM') {
        return `${formatDate(start)} (${formatTime(start)} ~ ${formatTime(end)})`;
    }

    if (type === 'EVENT' || type === 'UPDATE') {
        return `${formatDate(start)} ~ ${formatDate(end)}`;
    }

    // MAINTENANCE나 기타 → 표시 없음
    return '공지';
}

