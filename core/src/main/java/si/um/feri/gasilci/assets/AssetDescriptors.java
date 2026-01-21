package si.um.feri.gasilci.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetDescriptors {
    // SKIN
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<>(AssetPaths.UI_SKIN, Skin.class);

    // TEXTURES
    public static final AssetDescriptor<TextureAtlas> GAME_ATLAS =
        new AssetDescriptor<>(AssetPaths.GAME_ATLAS, TextureAtlas.class);

    // AUDIO
    public static final AssetDescriptor<Sound> GAME_OVER =
        new AssetDescriptor<>(AssetPaths.GAME_OVER, Sound.class);

    public static final AssetDescriptor<Sound> BUTTON_CLICK =
        new AssetDescriptor<>(AssetPaths.BUTTON_CLICK, Sound.class);

    public static final AssetDescriptor<Sound> TRUCK_DRIVING =
        new AssetDescriptor<>(AssetPaths.TRUCK_DRIVING, Sound.class);

    public static final AssetDescriptor<Sound> TRUCK_SIREN =
        new AssetDescriptor<>(AssetPaths.TRUCK_SIREN, Sound.class);

    public static final AssetDescriptor<Sound> WATER_EXTINGUISHING =
        new AssetDescriptor<>(AssetPaths.WATER_EXTINGUISHING, Sound.class);

    public static final AssetDescriptor<Sound> FIRE_AMBIENT =
        new AssetDescriptor<>(AssetPaths.FIRE_AMBIENT, Sound.class);

    public static final AssetDescriptor<Music> MAIN_MENU_MUSIC =
        new AssetDescriptor<>(AssetPaths.MAIN_MENU_MUSIC, Music.class);

    public static final AssetDescriptor<Music> GAMEPLAY_MUSIC =
        new AssetDescriptor<>(AssetPaths.GAMEPLAY_MUSIC, Music.class);

    // FONT
    public static final AssetDescriptor<BitmapFont> FONT =
        new AssetDescriptor<>(AssetPaths.FONT, BitmapFont.class);

    private AssetDescriptors() {}
}
