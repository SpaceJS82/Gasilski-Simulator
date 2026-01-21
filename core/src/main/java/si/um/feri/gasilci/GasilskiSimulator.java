package si.um.feri.gasilci;

import com.badlogic.gdx.Game;

import si.um.feri.gasilci.screens.MenuScreen;

public class GasilskiSimulator extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        // Dispose current screen first
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
        System.out.println("GasilskiSimulator: Main game disposed");
    }
}
