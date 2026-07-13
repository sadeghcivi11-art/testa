package hk.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import hk.KnightHollowGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new KnightHollowGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("HollowKnight");
        configuration.useVsync(true);

        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        configuration.setForegroundFPS(displayMode.refreshRate + 1);
        configuration.setWindowedMode(displayMode.width, displayMode.height);
        configuration.setWindowSizeLimits(960, 540, -1, -1);
        configuration.setMaximized(true);
        configuration.setWindowIcon("knight_icon.png");

        return configuration;
    }
}
