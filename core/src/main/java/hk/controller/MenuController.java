package hk.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;

import hk.model.AppModel;
import hk.model.GameData;
import hk.input.GameAction;
import hk.model.menu.MainMenuItem;
import hk.model.menu.MenuLayout;
import hk.model.menu.MenuModel;
import hk.model.menu.MenuState;
import hk.model.menu.SettingsItem;
import hk.service.AudioService;
import hk.service.SaveService;


public class MenuController {

    private static final float SLIDER_STEP = 0.1f;

    private final MenuModel    menu;
    private final AppModel     app;
    private final SaveService  saveService;
    private final AppNavigator navigator;
    private final MenuLayout   hitboxes;
    private final AudioService audio;



    private int lastMouseX = -1, lastMouseY = -1;
    private int draggingSlider = -1;

    public MenuController(MenuModel menu, AppModel app, SaveService saveService,
                          AppNavigator navigator, MenuLayout hitboxes, AudioService audio) {
        this.menu = menu;
        this.app = app;
        this.saveService = saveService;
        this.navigator = navigator;
        this.hitboxes = hitboxes;
        this.audio = audio;
    }

    public void update() {

        if (menu.awaitingRebind != null) {
            captureRebindKey();
            return;
        }

        updateMouse();

        switch (menu.state) {
            case MAIN:         updateMain();         break;
            case SLOTS:        updateSlots();        break;
            case SETTINGS:     updateSettings();     break;
            case KEYBINDS:     updateKeybinds();     break;
            case GUIDE:
            case ACHIEVEMENTS:
            case CHEATS:       updateReadOnlyPage(); break;
        }
    }



    private void updateMouse() {
        int mx = Gdx.input.getX();
        int myDown = Gdx.input.getY();
        float my = Gdx.graphics.getHeight() - myDown;

        boolean moved = mx != lastMouseX || myDown != lastMouseY;
        lastMouseX = mx;
        lastMouseY = myDown;


        if (draggingSlider >= 0) {
            if (!Gdx.input.isTouched()) {
                draggingSlider = -1;
            } else {
                MenuLayout.Hit grabbed = findSlider(draggingSlider);
                if (grabbed != null) {
                    setSliderValue(draggingSlider,
                            (mx - grabbed.rect.x) / grabbed.rect.width);
                }
                return;
            }
        }

        MenuLayout.Hit hit = hitboxes.at(mx, my);
        if (hit == null) return;

        if (moved && (hit.kind == MenuLayout.Kind.ROW || hit.kind == MenuLayout.Kind.SLIDER)
                && menu.selectedIndex != hit.index) {
            menu.selectedIndex = hit.index;
            audio.playSfx(AudioService.UI_MOVE);
        }

        if (!Gdx.input.justTouched()) return;
        switch (hit.kind) {
            case ROW:
                menu.selectedIndex = hit.index;
                activateSelection();
                break;
            case SLIDER:
                menu.selectedIndex = hit.index;
                draggingSlider = hit.index;
                setSliderValue(hit.index, (mx - hit.rect.x) / hit.rect.width);
                break;
            case BACK:
                goBack();
                break;
        }
    }

    private MenuLayout.Hit findSlider(int index) {

        return hitboxes.find(MenuLayout.Kind.SLIDER, index);
    }

    private void setSliderValue(int settingsRow, float ratio) {
        float v = MathUtils.clamp(ratio, 0f, 1f);
        SettingsItem[] items = SettingsItem.values();
        if (settingsRow < 0 || settingsRow >= items.length) return;
        switch (items[settingsRow]) {
            case BRIGHTNESS:   app.settings.setBrightness(v);  break;
            case SFX_VOLUME:   app.settings.setSfxVolume(v);   break;
            case MUSIC_VOLUME: app.settings.setMusicVolume(v); break;
            default: break;
        }
    }


    private void activateSelection() {
        switch (menu.state) {
            case MAIN:     activateMain();     break;
            case SLOTS:    activateSlot();     break;
            case SETTINGS: activateSetting();  break;
            case KEYBINDS: activateKeybind();  break;
            case GUIDE:
            case ACHIEVEMENTS:
            case CHEATS:   goBack();           break;
        }
    }


    private void goBack() {
        switch (menu.state) {
            case SLOTS:
            case GUIDE:
            case ACHIEVEMENTS:
            case CHEATS:
                audio.playSfx(AudioService.UI_BACK);
                menu.goTo(MenuState.MAIN);
                break;
            case SETTINGS:
                audio.playSfx(AudioService.UI_BACK);
                saveService.saveProfile(app);
                menu.goTo(MenuState.MAIN);
                break;
            case KEYBINDS:
                audio.playSfx(AudioService.UI_BACK);
                saveService.saveProfile(app);
                menu.goTo(MenuState.SETTINGS);
                break;
            default:
                break;
        }
    }



    private void updateMain() {
        moveCursor(MainMenuItem.values().length);
        if (enterPressed()) activateMain();
    }

    private void activateMain() {
        audio.playSfx(AudioService.UI_CONFIRM);
        switch (MainMenuItem.values()[menu.selectedIndex]) {
            case START:
                menu.slotMode = MenuModel.SlotMode.LOAD;
                menu.goTo(MenuState.SLOTS);
                break;
            case NEW_GAME:
                menu.slotMode = MenuModel.SlotMode.NEW_GAME;
                menu.goTo(MenuState.SLOTS);
                break;
            case SETTINGS:     menu.goTo(MenuState.SETTINGS);     break;
            case GUIDE:        menu.goTo(MenuState.GUIDE);        break;
            case ACHIEVEMENTS: menu.goTo(MenuState.ACHIEVEMENTS); break;
            case CHEATS:       menu.goTo(MenuState.CHEATS);       break;
            case LANGUAGE:
                app.settings.toggleLanguage();
                saveService.saveProfile(app);
                break;
            case QUIT:
                navigator.quit();
                break;
        }
    }

    private void updateSlots() {
        moveCursor(SaveService.SLOT_COUNT);
        if (backPressed()) { goBack(); return; }
        if (enterPressed()) activateSlot();
    }

    private void activateSlot() {
        int slot = menu.selectedIndex + 1;
        if (menu.slotMode == MenuModel.SlotMode.NEW_GAME) {
            audio.playSfx(AudioService.UI_CONFIRM);
            GameData fresh = newGameData();
            saveService.saveSlot(slot, fresh);
            navigator.startSession(slot, fresh);
        } else {
            GameData data = saveService.loadSlot(slot);
            if (data != null) {
                audio.playSfx(AudioService.UI_CONFIRM);
                navigator.startSession(slot, data);
            }

        }
    }

    private void updateSettings() {
        SettingsItem[] items = SettingsItem.values();
        moveCursor(items.length);
        if (backPressed()) { goBack(); return; }

        int dir = horizontalDir();
        SettingsItem item = items[menu.selectedIndex];
        switch (item) {
            case BRIGHTNESS:
                if (dir != 0) app.settings.setBrightness(app.settings.brightness + dir * SLIDER_STEP);
                break;
            case SFX_VOLUME:
                if (dir != 0) app.settings.setSfxVolume(app.settings.sfxVolume + dir * SLIDER_STEP);
                break;
            case MUSIC_VOLUME:
                if (dir != 0) app.settings.setMusicVolume(app.settings.musicVolume + dir * SLIDER_STEP);
                break;
            case SFX_ENABLED:
            case RESET_SOUND:
            case MENU_THEME:
            case VSYNC:
            case SHOW_FPS:
                if (dir != 0) { activateSetting(); return; }
                break;
            case FPS_CAP:
                if (dir != 0) {
                    app.settings.cycleFpsCap(dir);
                    applyDisplaySettings();
                    audio.playSfx(AudioService.UI_CONFIRM);
                    return;
                }
                break;
            default:
                break;
        }
        if (enterPressed()) activateSetting();
    }

    private void activateSetting() {
        switch (SettingsItem.values()[menu.selectedIndex]) {
            case SFX_ENABLED:
                app.settings.sfxEnabled = !app.settings.sfxEnabled;
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case RESET_SOUND:
                app.settings.resetSound();
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case MENU_THEME:
                app.settings.nextMenuTheme();
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case VSYNC:
                app.settings.vsync = !app.settings.vsync;
                applyDisplaySettings();
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case FPS_CAP:
                app.settings.cycleFpsCap(+1);
                applyDisplaySettings();
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case SHOW_FPS:
                app.settings.showFps = !app.settings.showFps;
                audio.playSfx(AudioService.UI_CONFIRM);
                break;
            case KEYBINDS:
                audio.playSfx(AudioService.UI_CONFIRM);
                menu.goTo(MenuState.KEYBINDS);
                break;
            default:
                break;
        }
    }

    private void updateKeybinds() {
        GameAction[] actions = GameAction.values();
        moveCursor(actions.length + 1);

        if (backPressed()) { goBack(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            app.bindings.resetDefaults();
            audio.playSfx(AudioService.UI_CONFIRM);
            return;
        }
        if (enterPressed()) activateKeybind();
    }

    private void activateKeybind() {
        GameAction[] actions = GameAction.values();
        audio.playSfx(AudioService.UI_CONFIRM);
        if (menu.selectedIndex == actions.length) {
            app.bindings.resetDefaults();
        } else {
            menu.awaitingRebind = actions[menu.selectedIndex];
        }
    }

    private void updateReadOnlyPage() {
        if (backPressed() || enterPressed()) goBack();
    }



    private void captureRebindKey() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            menu.awaitingRebind = null;
            audio.playSfx(AudioService.UI_BACK);
            return;
        }
        for (int keycode = 1; keycode < Input.Keys.MAX_KEYCODE; keycode++) {
            if (Gdx.input.isKeyJustPressed(keycode)) {
                app.bindings.rebind(menu.awaitingRebind, keycode);
                menu.awaitingRebind = null;
                saveService.saveProfile(app);
                audio.playSfx(AudioService.UI_CONFIRM);
                return;
            }
        }
    }



    private void moveCursor(int itemCount) {
        int before = menu.selectedIndex;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))   menu.moveCursor(-1, itemCount);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) menu.moveCursor(+1, itemCount);
        if (menu.selectedIndex != before) audio.playSfx(AudioService.UI_MOVE);
    }

    private int horizontalDir() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT))  return -1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) return +1;
        return 0;
    }

    private boolean enterPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }

    private boolean backPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    private void applyDisplaySettings() {
        hk.service.DisplayService.apply(app.settings);
    }

    private GameData newGameData() {
        return GameData.newGame();
    }
}
