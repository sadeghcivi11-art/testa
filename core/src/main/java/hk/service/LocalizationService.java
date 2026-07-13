package hk.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

import hk.model.SettingsModel;


public class LocalizationService {

    private static final String BUNDLE_PATH = "i18n/strings";

    private final SettingsModel settings;
    private I18NBundle bundle;
    private SettingsModel.Language loadedLanguage;

    public LocalizationService(SettingsModel settings) {
        this.settings = settings;
        reload();
    }


    public String get(String key) {
        refreshIfLanguageChanged();
        try {
            return bundle.get(key);
        } catch (Exception e) {
            return key;
        }
    }


    public String format(String key, Object... args) {
        refreshIfLanguageChanged();
        try {
            return bundle.format(key, args);
        } catch (Exception e) {
            return key;
        }
    }


    public void reload() {
        Locale locale = settings.language == SettingsModel.Language.FRENCH
                ? Locale.FRENCH : Locale.ENGLISH;
        bundle = I18NBundle.createBundle(Gdx.files.internal(BUNDLE_PATH), locale, "UTF-8");
        loadedLanguage = settings.language;
    }



    private void refreshIfLanguageChanged() {
        if (settings.language != loadedLanguage) reload();
    }
}
