package si.um.feri.gasilci.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Assets {
    private final AssetManager assetManager = new AssetManager();

    public void load() {
        assetManager.load(AssetDescriptors.GAME_ATLAS);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.FONT);
        // audio can be loaded later as needed
        assetManager.finishLoading();
    }

    public TextureAtlas getAtlas() {
        return assetManager.get(AssetDescriptors.GAME_ATLAS);
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void dispose() {
        assetManager.dispose();
    }
}
