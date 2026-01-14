package si.um.feri.gasilci.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
    private final AssetManager assetManager = new AssetManager();

    public void load() {
        assetManager.load(AssetDescriptors.GAME_ATLAS);
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.FONT);
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

    public void dispose() {
        assetManager.dispose();
    }
}
