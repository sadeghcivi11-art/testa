package hk.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.List;

import hk.model.AppModel;
import hk.model.charm.Charm;
import hk.model.charm.CharmInventory;
import hk.model.menu.GameOverlayModel;
import hk.model.menu.GameOverlayModel.Overlay;
import hk.input.GameAction;
import hk.model.menu.SettingsItem;
import hk.service.AudioService;
import hk.service.SaveService;
import hk.service.InputService;


public class GameOverlayController {

    private static final float SLIDER_STEP = 0.1f;


    public interface SessionActions {
        void saveAndExit();

        void restart();
    }


    public enum PauseItem { CONTINUE, SETTINGS, CHEATS, SAVE_EXIT }


    public enum VictoryItem { RESTART, MAIN_MENU }

    private final GameOverlayModel overlay;
    private final InputService      input;
    private final CharmInventory   charms;
    private final AppModel         app;
    private final SaveService      saveService;
    private final SessionActions   session;
    private final AudioService     audio;

    public GameOverlayController(GameOverlayModel overlay, InputService input, CharmInventory charms,
                                 AppModel app, SaveService saveService, SessionActions session,
                                 AudioService audio) {
        this.overlay     = overlay;
        this.input       = input;
        this.charms      = charms;
        this.app         = app;
        this.saveService = saveService;
        this.session     = session;
        this.audio       = audio;
    }


    public boolean update(boolean dialogueActive) {
        if (overlay.awaitingRebind != null) {
            captureRebindKey();
            return true;
        }

        switch (overlay.overlay) {
            case NONE:

                if (dialogueActive) return false;
                if (input.isJustDown(GameAction.PAUSE))     { overlay.open(Overlay.PAUSE);     audio.playSfx(AudioService.UI_CONFIRM); }
                if (input.isJustDown(GameAction.INVENTORY)) { overlay.open(Overlay.INVENTORY); audio.playSfx(AudioService.UI_CONFIRM); }
                return overlay.isOpen();

            case PAUSE:        updatePause();       return true;
            case SETTINGS:     updateSettings();    return true;
            case KEYBINDS:     updateKeybinds();    return true;
            case PAUSE_CHEATS: updatePauseCheats(); return true;
            case INVENTORY:    updateInventory();   return true;
            case VICTORY:      updateVictory();     return true;
            default:           return false;
        }
    }

    private void updatePause() {
        PauseItem[] items = PauseItem.values();
        moveCursor(items.length);
        if (input.isJustDown(GameAction.PAUSE)) { overlay.close(); audio.playSfx(AudioService.UI_BACK); return; }
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) return;

        switch (items[overlay.selectedIndex]) {
            case CONTINUE:  overlay.close();                   audio.playSfx(AudioService.UI_BACK);    break;
            case SETTINGS:  overlay.open(Overlay.SETTINGS);     audio.playSfx(AudioService.UI_CONFIRM); break;
            case CHEATS:    overlay.open(Overlay.PAUSE_CHEATS); audio.playSfx(AudioService.UI_CONFIRM); break;
            case SAVE_EXIT: session.saveAndExit();              audio.playSfx(AudioService.UI_SAVE);    break;
        }
    }


    private void moveCursor(int itemCount) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))   { overlay.moveCursor(-1, itemCount); audio.playSfx(AudioService.UI_MOVE); }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) { overlay.moveCursor(+1, itemCount); audio.playSfx(AudioService.UI_MOVE); }
    }

    private void updateSettings() {
        SettingsItem[] items = SettingsItem.values();
        moveCursor(items.length);
        if (input.isJustDown(GameAction.PAUSE)) {
            saveService.saveProfile(app);
            overlay.open(Overlay.PAUSE);
            audio.playSfx(AudioService.UI_BACK);
            return;
        }

        int dir = horizontalDir();
        switch (items[overlay.selectedIndex]) {
            case BRIGHTNESS:
                if (dir != 0) { app.settings.setBrightness(app.settings.brightness + dir * SLIDER_STEP); audio.playSfx(AudioService.UI_MOVE); }
                break;
            case SFX_VOLUME:
                if (dir != 0) { app.settings.setSfxVolume(app.settings.sfxVolume + dir * SLIDER_STEP); audio.playSfx(AudioService.UI_MOVE); }
                break;
            case MUSIC_VOLUME:
                if (dir != 0) { app.settings.setMusicVolume(app.settings.musicVolume + dir * SLIDER_STEP); audio.playSfx(AudioService.UI_MOVE); }
                break;
            case FPS_CAP:
                if (dir != 0) { app.settings.cycleFpsCap(dir); applyDisplaySettings(); return; }
                break;
            case SFX_ENABLED:
            case RESET_SOUND:
            case MENU_THEME:
            case VSYNC:
            case SHOW_FPS:
                if (dir != 0) { activateSetting(); return; }
                break;
            default:
                break;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) activateSetting();
    }

    private void activateSetting() {
        audio.playSfx(AudioService.UI_CONFIRM);
        switch (SettingsItem.values()[overlay.selectedIndex]) {
            case SFX_ENABLED: app.settings.sfxEnabled = !app.settings.sfxEnabled; break;
            case RESET_SOUND: app.settings.resetSound();                          break;
            case MENU_THEME:  app.settings.nextMenuTheme();                       break;
            case VSYNC:
                app.settings.vsync = !app.settings.vsync;
                applyDisplaySettings();
                break;
            case FPS_CAP:
                app.settings.cycleFpsCap(+1);
                applyDisplaySettings();
                break;
            case SHOW_FPS: app.settings.showFps = !app.settings.showFps; break;
            case KEYBINDS: overlay.open(Overlay.KEYBINDS);                break;
            default: break;
        }
    }

    private void updateKeybinds() {
        GameAction[] actions = GameAction.values();
        moveCursor(actions.length + 1);
        if (input.isJustDown(GameAction.PAUSE)) {
            saveService.saveProfile(app);
            overlay.open(Overlay.SETTINGS);
            audio.playSfx(AudioService.UI_BACK);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            app.bindings.resetDefaults();
            saveService.saveProfile(app);
            audio.playSfx(AudioService.UI_CONFIRM);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) activateKeybind();
    }

    private void activateKeybind() {
        audio.playSfx(AudioService.UI_CONFIRM);
        GameAction[] actions = GameAction.values();
        if (overlay.selectedIndex == actions.length) {
            app.bindings.resetDefaults();
            saveService.saveProfile(app);
        } else {
            overlay.awaitingRebind = actions[overlay.selectedIndex];
        }
    }

    private void captureRebindKey() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            overlay.awaitingRebind = null;
            audio.playSfx(AudioService.UI_BACK);
            return;
        }
        for (int keycode = 1; keycode < Input.Keys.MAX_KEYCODE; keycode++) {
            if (Gdx.input.isKeyJustPressed(keycode)) {
                app.bindings.rebind(overlay.awaitingRebind, keycode);
                overlay.awaitingRebind = null;
                saveService.saveProfile(app);
                audio.playSfx(AudioService.UI_CONFIRM);
                return;
            }
        }
    }

    private int horizontalDir() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT))  return -1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) return +1;
        return 0;
    }

    private void applyDisplaySettings() {
        hk.service.DisplayService.apply(app.settings);
    }


    private void updateVictory() {
        VictoryItem[] items = VictoryItem.values();
        moveCursor(items.length);
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) return;

        audio.playSfx(AudioService.UI_CONFIRM);
        switch (items[overlay.selectedIndex]) {
            case RESTART:   session.restart();     break;
            case MAIN_MENU: session.saveAndExit(); break;
        }
    }

    private void updatePauseCheats() {
        if (input.isJustDown(GameAction.PAUSE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            overlay.open(Overlay.PAUSE);
            audio.playSfx(AudioService.UI_BACK);
        }
    }

    private void updateInventory() {
        List<Charm> owned = charms.getOwned();
        moveCursor(owned.size());
        if (input.isJustDown(GameAction.INVENTORY) || input.isJustDown(GameAction.PAUSE)) {
            overlay.close();
            audio.playSfx(AudioService.UI_BACK);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !owned.isEmpty()) {
            Charm charm = owned.get(overlay.selectedIndex);

            if (charms.isEquipped(charm)) charms.unequip(charm);
            else                          charms.equip(charm);
        }
    }
}
