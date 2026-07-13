package hk;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

import hk.controller.AppNavigator;
import hk.model.AppModel;
import hk.model.GameData;
import hk.service.AchievementService;
import hk.service.AudioService;
import hk.service.EventBus;
import hk.service.LocalizationService;
import hk.service.SaveService;
import hk.screens.GameScreen;
import hk.view.screen.MenuScreen;


public class KnightHollowGame extends Game implements AppNavigator {

    private AppModel            appModel;
    private SaveService         saveService;
    private LocalizationService localization;
    private AchievementService  achievements;
    private AudioService        audio;
    private EventBus            eventBus;

    private MenuScreen menuScreen;
    private GameScreen gameScreen;

    private Cursor customCursor;

    @Override
    public void create() {
        appModel     = new AppModel();
        saveService  = new SaveService();
        saveService.loadProfile(appModel);
        hk.service.DisplayService.apply(appModel.settings);
        localization = new LocalizationService(appModel.settings);
        eventBus     = new EventBus();
        achievements = new AchievementService(appModel, saveService, eventBus);
        audio        = new AudioService(appModel.settings);

        eventBus.subscribe(hk.service.AchievementUnlockedEvent.class,
                e -> Gdx.app.log("Achievements", "Unlocked: " + e.achievement().id));

        installCustomCursor();

        menuScreen = new MenuScreen(appModel, localization, saveService, this, audio);
        setScreen(menuScreen);
    }

    @Override
    public void render() {
        audio.update(Gdx.graphics.getDeltaTime());
        super.render();
    }



    @Override
    public void startSession(int slot, GameData data) {
        disposeGameScreen();
        appModel.activeSlot = slot;
        GameContext ctx = new GameContext(appModel, saveService, localization, achievements, audio, eventBus);
        gameScreen = new GameScreen(this, ctx, slot, data);
        setScreen(gameScreen);
    }

    @Override
    public void showMenu() {
        appModel.activeSlot = 0;
        setScreen(menuScreen);
        disposeGameScreen();
    }

    @Override
    public void quit() {
        saveService.saveProfile(appModel);
        Gdx.app.exit();
    }




    private void installCustomCursor() {
        try {
            int s = 32;
            Pixmap pm = new Pixmap(s, s, Pixmap.Format.RGBA8888);
            pm.setBlending(Pixmap.Blending.SourceOver);


            pm.setColor(0.05f, 0.06f, 0.10f, 1f);
            pm.fillTriangle(0, 0, 0, 24, 17, 17);
            pm.setColor(0.92f, 0.94f, 1.00f, 1f);
            pm.fillTriangle(1, 3, 1, 20, 13, 14);

            pm.setColor(0.62f, 0.80f, 1.00f, 0.9f);
            pm.fillCircle(3, 4, 2);

            customCursor = Gdx.graphics.newCursor(pm, 0, 0);
            pm.dispose();
            if (customCursor != null) Gdx.graphics.setCursor(customCursor);
        } catch (Exception e) {
            Gdx.app.log("KnightHollowGame", "Custom cursor unavailable: " + e.getMessage());
        }
    }



    private void disposeGameScreen() {
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    @Override
    public void dispose() {

        setScreen(null);
        disposeGameScreen();
        if (menuScreen != null) menuScreen.dispose();
        if (audio != null) audio.dispose();
        if (customCursor != null) customCursor.dispose();
        super.dispose();
    }
}
