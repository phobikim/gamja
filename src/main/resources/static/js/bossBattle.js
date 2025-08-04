fetch("/boss-run.html");

const runAwayBtn = document.getElementById('runAwayBtn');
if (runAwayBtn) {
    runAwayBtn.addEventListener('click', () => {
        window.location.href = "/boss-run.html";
    });
}

// 보스 등장 애니메이션 적용
window.addEventListener('DOMContentLoaded', () => {
    const bossImg = document.querySelector('.boss-image');
    const charImg = document.querySelector('.character-left');
    if (bossImg) {
        bossImg.classList.add('float-once');

        // 애니메이션 끝나면 class 제거 (필요시)
        bossImg.addEventListener('animationend', () => {
            bossImg.classList.remove('float-once');
        });
        bossImg.addEventListener('animationend', () => {
            bossImg.classList.remove('float-once');
            bossImg.classList.add('glow-effect');
        });
    }

    if (charImg) {
        charImg.classList.add('animate-enter');
    }
});