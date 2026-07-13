package hk.service;

import com.badlogic.gdx.Gdx;

import hk.model.SettingsModel;


public final class DisplayService {

    public static void apply(SettingsModel settings) {
        Gdx.graphics.setVSync(settings.vsync);
        Gdx.graphics.setForegroundFPS(settings.fpsCap());
    }

    private DisplayService() { }
}
