// 🔊 사운드 저장소
const audioMap = {
    se_click: new Audio("audio/se_click.mp3"),
    se_no_click: new Audio("audio/se_no_click.mp3"),
    se_input: new Audio("audio/se_input.mp3"),
    bgm_main: new Audio("audio/bgm_main.mp3"),
    bgm_gotcha: new Audio("audio/bgm_gotcha.mp3"),
    bgm_base: new Audio("audio/bgm_base.mp3"),
    bgm_char: new Audio("audio/bgm_char.mp3"),
    bgm_shop: new Audio("audio/bgm_shop.mp3"),
};

audioMap.bgm_main.loop = true;
audioMap.bgm_gotcha.loop = true;

let currentBGM = null;
let currentBGMName = null;
let isBGMMuted = true;

// ✅ 효과음 재생
function playEffect(name) {
    const audio = audioMap[name];
    if (!audio) return console.warn('효과음 "${name}" 없음');
    audio.currentTime = 0;
    audio.play();
}

// ✅ 사운드 토글 함수 (사용자 호출용)
async function toggleBGM(name) {
    const bgm = audioMap[name];
    if (!bgm) return console.warn(`BGM "${name}" 없음`);

    // 처음 실행하거나 다른 BGM으로 변경
    if (!currentBGM || currentBGMName !== name) {
        stopBGM(currentBGM);
        currentBGM = bgm;
        currentBGMName = name;
        currentBGM.volume = 0;
        await currentBGM.play().catch(e => console.warn("play 실패:", e));
        isBGMMuted = true;
        updateBGMButton(false);
    }

    if (isBGMMuted) {
        // 아이폰 대응: src 새로 지정 필요
        const newAudio = new Audio(`audio/${name}.mp3`);
        newAudio.loop = true;
        newAudio.volume = 1;
        await newAudio.play().catch(e => console.warn("play 실패:", e));
        stopBGM(currentBGM);
        currentBGM = newAudio;
        audioMap[name] = newAudio; // audioMap도 갱신
        isBGMMuted = false;
        updateBGMButton(true);
    } else {
        stopBGM(currentBGM);
        isBGMMuted = true;
        updateBGMButton(false);
    }
}

// ✅ 안전하게 BGM 정지
function stopBGM(audio) {
    if (!audio) return;
    try {
        audio.pause();
        audio.src = "";
        audio.load();
    } catch (e) {
        console.warn("stop 실패:", e);
    }
}

// ✅ 버튼 텍스트 갱신
function updateBGMButton(isPlaying) {
    const btn = document.getElementById("bgmToggleBtn");

    if (isPlaying) {
        btn.classList.remove("bgm-off");
        btn.classList.add("bgm-on");
    } else {
        btn.classList.remove("bgm-on");
        btn.classList.add("bgm-off");
    }
}