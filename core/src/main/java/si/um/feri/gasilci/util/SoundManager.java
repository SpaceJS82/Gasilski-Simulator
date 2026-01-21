package si.um.feri.gasilci.util;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private static Sound buttonClickSound;
    private static Music currentMusic;

    public static void setButtonClickSound(Sound sound) {
        buttonClickSound = sound;
    }

    public static void playButtonClick() {
        if (buttonClickSound != null) {
            buttonClickSound.play(0.5f); // 50% volume
        }
    }

    public static void playMusic(Music music, boolean loop) {
        stopMusic();
        currentMusic = music;
        if (currentMusic != null) {
            currentMusic.setLooping(loop);
            currentMusic.setVolume(0.3f); // 30% volume
            currentMusic.play();
        }
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
    }

    public static void stopAllSounds() {
        // Stop all button click sounds by stopping all sounds globally
        if (buttonClickSound != null) {
            buttonClickSound.stop();
        }
        stopMusic();
    }

    public static void dispose() {
        stopMusic();
        buttonClickSound = null;
        currentMusic = null;
    }
}
