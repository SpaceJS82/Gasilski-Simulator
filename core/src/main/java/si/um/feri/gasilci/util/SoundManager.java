package si.um.feri.gasilci.util;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private static Sound buttonClickSound;
    private static Music currentMusic;
    
    // Volume settings
    private static boolean masterSoundEnabled = true;
    private static float masterVolume = 1.0f;
    private static float buttonVolume = 1.0f;
    private static float truckDrivingVolume = 1.0f;
    private static float truckSirenVolume = 1.0f;
    private static float waterExtinguishingVolume = 1.0f;
    private static float fireAmbientVolume = 1.0f;

    public static void setButtonClickSound(Sound sound) {
        buttonClickSound = sound;
    }

    public static void playButtonClick() {
        if (buttonClickSound != null && masterSoundEnabled) {
            buttonClickSound.play(masterVolume * buttonVolume);
        }
    }

    public static void playMusic(Music music, boolean loop) {
        stopMusic();
        currentMusic = music;
        if (currentMusic != null && masterSoundEnabled) {
            currentMusic.setLooping(loop);
            currentMusic.setVolume(masterVolume * 0.3f);
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
    
    // Master sound control
    public static void setMasterSoundEnabled(boolean enabled) {
        masterSoundEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }
    
    public static boolean isMasterSoundEnabled() {
        return masterSoundEnabled;
    }
    
    // Master volume
    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.setVolume(masterVolume * 0.3f);
        }
    }
    
    public static float getMasterVolume() {
        return masterVolume;
    }
    
    // Individual volume controls
    public static void setButtonVolume(float volume) {
        buttonVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public static float getButtonVolume() {
        return buttonVolume;
    }
    
    public static void setTruckDrivingVolume(float volume) {
        truckDrivingVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public static float getTruckDrivingVolume() {
        return truckDrivingVolume;
    }
    
    public static void setTruckSirenVolume(float volume) {
        truckSirenVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public static float getTruckSirenVolume() {
        return truckSirenVolume;
    }
    
    public static void setWaterExtinguishingVolume(float volume) {
        waterExtinguishingVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public static float getWaterExtinguishingVolume() {
        return waterExtinguishingVolume;
    }
    
    public static void setFireAmbientVolume(float volume) {
        fireAmbientVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public static float getFireAmbientVolume() {
        return fireAmbientVolume;
    }
    
    // Calculate final volume for specific sound effects
    public static float calculateTruckDrivingVolume() {
        return masterSoundEnabled ? masterVolume * truckDrivingVolume : 0f;
    }
    
    public static float calculateTruckSirenVolume() {
        return masterSoundEnabled ? masterVolume * truckSirenVolume : 0f;
    }
    
    public static float calculateWaterExtinguishingVolume() {
        return masterSoundEnabled ? masterVolume * waterExtinguishingVolume : 0f;
    }
    
    public static float calculateFireAmbientVolume() {
        return masterSoundEnabled ? masterVolume * fireAmbientVolume : 0f;
    }
}
