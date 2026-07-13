package hk.model;

import java.util.EnumMap;

import hk.input.KeyBindings;
import hk.model.progress.Achievement;
import hk.model.progress.AchievementId;


public class AppModel {

    public final SettingsModel settings = new SettingsModel();
    public final KeyBindings   bindings = new KeyBindings();
    public final EnumMap<AchievementId, Achievement> achievements =
            new EnumMap<>(AchievementId.class);


    public int activeSlot = 0;

    public AppModel() {
        for (AchievementId id : AchievementId.values()) {
            achievements.put(id, new Achievement(id));
        }
    }


    public ProfileData toProfileData() {
        ProfileData data = new ProfileData();
        data.brightness     = settings.brightness;
        data.sfxVolume      = settings.sfxVolume;
        data.musicVolume    = settings.musicVolume;
        data.sfxEnabled     = settings.sfxEnabled;
        data.language       = settings.language.name();
        data.menuThemeIndex = settings.menuThemeIndex;
        data.vsync          = settings.vsync;
        data.fpsCapIndex    = settings.fpsCapIndex;
        data.showFps        = settings.showFps;
        data.keybinds       = bindings.snapshot();
        for (Achievement a : achievements.values()) {
            if (a.unlocked) data.unlockedAchievements.add(a.id.name());
        }
        return data;
    }


    public void fromProfileData(ProfileData data) {
        if (data == null) return;
        settings.setBrightness(data.brightness);
        settings.setSfxVolume(data.sfxVolume);
        settings.setMusicVolume(data.musicVolume);
        settings.sfxEnabled = data.sfxEnabled;
        try {
            settings.language = SettingsModel.Language.valueOf(data.language);
        } catch (IllegalArgumentException ignored) {

        }
        settings.menuThemeIndex = Math.floorMod(data.menuThemeIndex, SettingsModel.MENU_THEME_COUNT);
        settings.vsync          = data.vsync;
        settings.fpsCapIndex    = Math.floorMod(data.fpsCapIndex, SettingsModel.FPS_CAP_CHOICES.length);
        settings.showFps        = data.showFps;
        bindings.restore(data.keybinds);
        for (String name : data.unlockedAchievements) {
            try {
                achievements.get(AchievementId.valueOf(name)).unlocked = true;
            } catch (IllegalArgumentException ignored) {

            }
        }
    }
}
