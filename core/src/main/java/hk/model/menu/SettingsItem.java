package hk.model.menu;


public enum SettingsItem {
    BRIGHTNESS   ("settings.brightness"),
    SFX_VOLUME   ("settings.sfx"),
    MUSIC_VOLUME ("settings.music"),
    SFX_ENABLED  ("settings.sfxEnabled"),
    RESET_SOUND  ("settings.resetSound"),
    MENU_THEME   ("settings.theme"),
    VSYNC        ("settings.vsync"),
    FPS_CAP      ("settings.fpsCap"),
    SHOW_FPS     ("settings.showFps"),
    KEYBINDS     ("settings.keybinds");


    public final String labelKey;

    SettingsItem(String labelKey) {
        this.labelKey = labelKey;
    }
}
