fetch("/boss-run.html");
fetch("/css/common.css");
const img = new Image();
img.src = "https://phobi.me/gamja.img/images/monster/boss_gg.png";

const runAwayBtn = document.getElementById('runAwayBtn');
if (runAwayBtn) {
    runAwayBtn.addEventListener('click', () => {
        window.location.href = "/boss-run.html";
    });
}