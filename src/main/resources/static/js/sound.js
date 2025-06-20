// 🔊 사운드 저장소
const audioMap = {
    se_click: "https://phobi.me/gamja.img/audio/se_click.mp3",
    se_click2: "https://phobi.me/gamja.img/audio/se_click2.mp3",
    se_input: "https://phobi.me/gamja.img/audio/se_input.mp3",
    se_ok: "https://phobi.me/gamja.img/audio/se_ok.mp3",
    se_coin: "https://phobi.me/gamja.img/audio/se_coin.mp3",
    se_craft: "https://phobi.me/gamja.img/audio/se_craft.mp3",
    se_attack: "https://phobi.me/gamja.img/audio/se_attack.mp3",
    bgm_main: "https://phobi.me/gamja.img/audio/bgm_main.mp3",
    bgm_gotcha: "https://phobi.me/gamja.img/audio/bgm_gotcha.mp3",
    bgm_base: "https://phobi.me/gamja.img/audio/bgm_base.mp3",
    bgm_char: "https://phobi.me/gamja.img/audio/bgm_char.mp3",
    bgm_shop: "https://phobi.me/gamja.img/audio/bgm_shop.mp3",
};


audioMap.bgm_main.loop = true;
audioMap.bgm_gotcha.loop = true;

let currentBGM = null;
let currentBGMName = null;
let isBGMMuted = true;

// ✅ 효과음 재생
// function playEffect(name) {
//     const audio = audioMap[name];
//     if (!audio) return console.warn('효과음 "${name}" 없음');
//     audio.currentTime = 0;
//     audio.volume = 0.3; // 볼륨 조절 (0.0 ~ 1.0)
//     audio.play();
// }

const audioContext = new (window.AudioContext || window.webkitAudioContext)();
const audioBufferCache = {}; // 캐시: 중복 다운로드 방지

// 사용자 최초 클릭 시 resume
let resumedOnce = false;
function tryResumeAudioContext() {
    if (resumedOnce) return;
    if (audioContext.state === 'suspended') {
        audioContext.resume().then(() => {
            console.log("✅ audioContext resumed");
            resumedOnce = true;
        });
    }
}

async function playEffect(name) {
    const url = audioMap[name];
    if (!url) return;

    try {
        let buffer = audioBufferCache[name];

        // 아직 없으면 fetch 후 decode
        if (!buffer) {
            const res = await fetch(url);
            const arrayBuffer = await res.arrayBuffer();
            buffer = await audioContext.decodeAudioData(arrayBuffer);
            audioBufferCache[name] = buffer;
        }

        const source = audioContext.createBufferSource();
        source.buffer = buffer;

        const gain = audioContext.createGain();
        gain.gain.value = 0.3; // 볼륨 설정

        source.connect(gain).connect(audioContext.destination);
        source.start();
    } catch (e) {
        console.warn("Web Audio 효과음 실패:", e);
    }
}

async function toggleBGM(name) {
    const src = audioMap[name];
    if (!src) return console.warn(`BGM "${name}" 없음`);

    // 새로 실행 or 다른 BGM으로 전환
    if (!currentBGM || currentBGMName !== name) {
        stopBGM(currentBGM);
        currentBGM = new Audio(src);
        currentBGMName = name;
        currentBGM.loop = true;
        currentBGM.volume = 1;
        currentBGM.playsInline = true;

        try {
            await currentBGM.play();
            isBGMMuted = false;
            updateBGMButton(true);
        } catch (e) {
            console.warn("BGM play 실패:", e);
        }
        return;
    }

    // 토글: 음소거 ↔ 재생
    if (isBGMMuted) {
        try {
            await currentBGM.play();
            isBGMMuted = false;
            updateBGMButton(true);
        } catch (e) {
            console.warn("BGM play 실패:", e);
        }
    } else {
        stopBGM(currentBGM);
        isBGMMuted = true;
        updateBGMButton(false);
    }
}

// ✅ BGM 정지
function stopBGM(audio) {
    if (!audio) return;
    try {
        audio.pause();
        audio.src = "";
        audio.load(); // 안전 정지
    } catch (e) {
        console.warn("stop 실패:", e);
    }
}

// ✅ BGM 버튼 갱신
function updateBGMButton(isPlaying) {
    const btn = document.getElementById("bgmToggleBtn");
    const basePath = window.basePath_image || "https://phobi.me/gamja.img/images";

    if (isPlaying) {
        btn.classList.remove("bgm-off");
        btn.classList.add("bgm-on");
        btn.style.backgroundImage = `url("${basePath}/icons/bgm_on.png")`;
    } else {
        btn.classList.remove("bgm-on");
        btn.classList.add("bgm-off");
        btn.style.backgroundImage = `url("${basePath}/icons/bgm_off.png")`;
    }
}