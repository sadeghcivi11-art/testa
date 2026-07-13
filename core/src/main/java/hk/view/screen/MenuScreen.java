package hk.view.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import hk.controller.AppNavigator;
import hk.controller.MenuController;
import hk.model.AppModel;
import hk.model.menu.MenuLayout;
import hk.model.menu.MenuModel;
import hk.model.menu.MenuState;
import hk.service.AudioService;
import hk.service.LocalizationService;
import hk.service.SaveService;
import hk.view.render.MenuRenderer;


public class MenuScreen implements Screen {

    private final MenuModel      menu;
    private final MenuController controller;
    private final MenuRenderer   renderer;
    private final AudioService   audio;

    public MenuScreen(AppModel app, LocalizationService loc, SaveService saveService,
                      AppNavigator navigator, AudioService audio) {
        this.audio = audio;
        menu       = new MenuModel();
        MenuLayout hitboxes = new MenuLayout();
        controller = new MenuController(menu, app, saveService, navigator, hitboxes, audio);
        renderer   = new MenuRenderer(app, loc, saveService, hitboxes);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        controller.update();
        renderer.draw(menu, delta);
    }

    @Override
    public void show() {

        renderer.invalidateSlotCache();
        menu.goTo(MenuState.MAIN);
        audio.playMusic(AudioService.MUSIC_MENU);
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
