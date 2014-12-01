package jordanlw.gdxGame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

class DesktopGame {
    public static void initGame() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
        config.width = (int) Game.windowSize.x;
        config.height = (int) Game.windowSize.y;
        config.title = "gdxGame";
        config.forceExit = false;
        new LwjglApplication(new Game(), config);
    }
}