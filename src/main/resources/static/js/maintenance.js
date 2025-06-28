(async function () {
    try {
        const res = await fetch('/api/maintenance/server/current', {
            credentials: 'include',
            cache: 'no-store'
        });

        if (res.status === 204) {
            location.href = './index.html';
            return;
        }


        if (!res.ok) return;

        const data = await res.json();

        const start = new Date(data.startTime);
        const end = new Date(data.endTime);

        const formatDate = (d) =>
            `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`;

        const formatTime = (d) =>
            `${d.getHours()}시 ${String(d.getMinutes()).padStart(2, '0')}분`;

        document.getElementById('maintenanceTime').innerText =
            `${formatDate(start)} ${formatTime(start)} ~ ${formatTime(end)}`;


    } catch (err) {
        console.error('점검 시간 조회 실패:', err);
    }
})();