package si.um.feri.gasilci.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
    private final AssetManager assetManager = new AssetManager();

    public void load() {
        assetManager.load(AssetDescriptors.GAME_ATLAS);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.FONT);
        assetManager.load(AssetDescriptors.BUTTON_CLICK);
        assetManager.load(AssetDescriptors.TRUCK_DRIVING);
        assetManager.load(AssetDescriptors.TRUCK_SIREN);
        assetManager.load(AssetDescriptors.WATER_EXTINGUISHING);
        assetManager.load(AssetDescriptors.FIRE_AMBIENT);
        assetManager.load(AssetDescriptors.MAIN_MENU_MUSIC);
        assetManager.load(AssetDescriptors.GAMEPLAY_MUSIC);
        assetManager.finishLoading();
    }

    public TextureAtlas getAtlas() {
        return assetManager.get(AssetDescriptors.GAME_ATLAS);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public Skin getSkin() {
        return assetManager.get(AssetDescriptors.UI_SKIN);
    }

    public Sound getButtonClickSound() {
        return assetManager.get(AssetDescriptors.BUTTON_CLICK);
    }

    public Sound getTruckDrivingSound() {
        return assetManager.get(AssetDescriptors.TRUCK_DRIVING);
    }

    public Sound getTruckSirenSound() {
        return assetManager.get(AssetDescriptors.TRUCK_SIREN);
    }

    public Sound getWaterExtinguishingSound() {
        return assetManager.get(AssetDescriptors.WATER_EXTINGUISHING);
    }

    public Sound getFireAmbientSound() {
        return assetManager.get(AssetDescriptors.FIRE_AMBIENT);
    }

    public Music getMainMenuMusic() {
        return assetManager.get(AssetDescriptors.MAIN_MENU_MUSIC);
    }

    public Music getGameplayMusic() {
        return assetManager.get(AssetDescriptors.GAMEPLAY_MUSIC);
    }

    public void dispose() {
        assetManager.dispose();
    }
}
