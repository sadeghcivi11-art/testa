package hk.model;


public class SettingsModel {


    public enum Language { ENGLISH, FRENCH }


    public static final float DEFAULT_BRIGHTNESS   = 1.0f;
    public static final float DEFAULT_SFX_VOLUME   = 0.8f;
    public static final float DEFAULT_MUSIC_VOLUME = 0.6f;
    public static final int   MENU_THEME_COUNT     = 3;


    public static final int[] FPS_CAP_CHOICES = { 0, 60, 120, 144, 240 };


    public float brightness  = DEFAULT_BRIGHTNESS;
    public float sfxVolume   = DEFAULT_SFX_VOLUME;
    public float musicVolume = DEFAULT_MUSIC_VOLUME;
    public boolean sfxEnabled = true;
    public Language language  = Language.ENGLISH;
    public int menuThemeIndex = 0;


    public boolean vsync    = true;
    public int fpsCapIndex  = 0;
    public boolean showFps  = false;

    public SettingsModel() { }


    public void setBrightness(float v)  { brightness  = clamp01(v); }
    public void setSfxVolume(float v)   { sfxVolume   = clamp01(v); }
    public void setMusicVolume(float v) { musicVolume = clamp01(v); }

    public void toggleLanguage() {
        language = (language == Language.ENGLISH) ? Language.FRENCH : Language.ENGLISH;
    }

    public void nextMenuTheme() {
        menuThemeIndex = (menuThemeIndex + 1) % MENU_THEME_COUNT;
    }


    public int fpsCap() {
        return FPS_CAP_CHOICES[Math.floorMod(fpsCapIndex, FPS_CAP_CHOICES.length)];
    }


    public void cycleFpsCap(int dir) {
        fpsCapIndex = Math.floorMod(fpsCapIndex + dir, FPS_CAP_CHOICES.length);
    }


    public void resetSound() {
        sfxVolume   = DEFAULT_SFX_VOLUME;
        musicVolume = DEFAULT_MUSIC_VOLUME;
        sfxEnabled  = true;
    }


    public void resetAll() {
        brightness     = DEFAULT_BRIGHTNESS;
        language       = Language.ENGLISH;
        menuThemeIndex = 0;
        vsync          = true;
        fpsCapIndex    = 0;
        showFps        = false;
        resetSound();
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
