package hk.model;

import java.util.ArrayList;


public class ProfileData {


    public float brightness  = SettingsModel.DEFAULT_BRIGHTNESS;
    public float sfxVolume   = SettingsModel.DEFAULT_SFX_VOLUME;
    public float musicVolume = SettingsModel.DEFAULT_MUSIC_VOLUME;
    public boolean sfxEnabled = true;
    public String language   = SettingsModel.Language.ENGLISH.name();
    public int menuThemeIndex = 0;


    public boolean vsync   = true;
    public int fpsCapIndex = 0;
    public boolean showFps = false;


    public ArrayList<String> keybinds = new ArrayList<>();


    public ArrayList<String> unlockedAchievements = new ArrayList<>();

    public ProfileData() { }
}
