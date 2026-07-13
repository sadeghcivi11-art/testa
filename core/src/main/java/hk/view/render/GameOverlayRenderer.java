package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import hk.controller.GameOverlayController.PauseItem;
import hk.controller.GameOverlayController.VictoryItem;
import hk.model.AppModel;
import hk.model.GameData;
import hk.model.charm.Charm;
import hk.model.charm.CharmInventory;
import hk.model.charm.CharmType;
import hk.model.cheat.CheatCode;
import hk.model.cheat.CheatState;
import hk.input.GameAction;
import hk.model.menu.GameOverlayModel;
import hk.model.menu.SettingsItem;
import hk.service.LocalizationService;
import hk.view.helper.FontLibrary;


public class GameOverlayRenderer implements Disposable, CharmInventory.Listener {

    private static final Color VEIL   = new Color(0f, 0f, 0f, 0.6f);
    private static final Color PANEL  = new Color(0.08f, 0.09f, 0.14f, 0.95f);
    private static final Color ACCENT = new Color(0.75f, 0.85f, 1f, 1f);
    private static final Color TEXT   = new Color(0.93f, 0.94f, 1f, 1f);
    private static final Color DIM    = new Color(0.55f, 0.58f, 0.68f, 1f);

    private final SpriteBatch        batch;
    private final ShapeRenderer      shapes;
    private final FontLibrary        fonts;
    private final OrthographicCamera uiCamera;
    private final GlyphLayout        layout;
    private final LocalizationService loc;


    private String equippedLine = "";

    private final Map<CharmType, Texture> charmIcons = new EnumMap<>(CharmType.class);
    private final Texture charmBackboard;

    public GameOverlayRenderer(LocalizationService loc, FontLibrary fonts) {
        this.loc   = loc;
        this.fonts = fonts;
        batch    = new SpriteBatch();
        shapes   = new ShapeRenderer();
        layout   = new GlyphLayout();
        uiCamera = new OrthographicCamera();
        for (CharmType type : CharmType.values()) {
            charmIcons.put(type, loadLinear("Charms/" + type.name() + ".png"));
        }
        charmBackboard = loadLinear("Charms/charm_backboard.png");
    }

    private static Texture loadLinear(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    @Override
    public void onCharmsChanged(CharmInventory inventory) {
        StringBuilder sb = new StringBuilder();
        for (Charm c : inventory.getEquipped()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append(loc.get("charm." + c.type.name() + ".name"));
        }
        equippedLine = sb.toString();
    }

    public void draw(GameOverlayModel overlay, AppModel app, CharmInventory charms,
                     GameData stats, CheatState cheats) {
        if (!overlay.isOpen()) return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        uiCamera.setToOrtho(false, sw, sh);
        uiCamera.update();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        drawVeil(sw, sh);
        switch (overlay.overlay) {
            case PAUSE:        drawPause(overlay, sw, sh);              break;
            case SETTINGS:     drawSettings(overlay, app, sw, sh);      break;
            case KEYBINDS:     drawKeybinds(overlay, app, sw, sh);      break;
            case PAUSE_CHEATS: drawPauseCheats(sw, sh, cheats);         break;
            case INVENTORY:    drawInventory(overlay, charms, sw, sh);  break;
            case VICTORY:      drawVictory(overlay, stats, sw, sh);     break;
            default: break;
        }
    }


    private void drawVictory(GameOverlayModel overlay, GameData stats, float sw, float sh) {

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0, 0, sw, sh);
        shapes.end();

        drawCenteredTitle(loc.get("victory.title"), sw, sh * 0.80f);

        batch.begin();
        drawCentered(fonts.small, loc.get("victory.subtitle"), DIM, sw, sh * 0.70f);

        int total = (int) stats.playTimeSeconds;
        String time = String.format("%d:%02d:%02d", total / 3600, (total / 60) % 60, total % 60);
        String[] lines = {
            loc.get("victory.time")   + "   —   " + time,
            loc.get("victory.kills")  + "   —   " + stats.kills,
            loc.get("victory.deaths") + "   —   " + stats.deaths,
        };
        float y = sh * 0.60f;
        for (String line : lines) {
            drawCentered(fonts.item, line, TEXT, sw, y);
            y -= fonts.item.getLineHeight() * 1.3f;
        }

        VictoryItem[] items = VictoryItem.values();
        String[] labels = { loc.get("victory.restart"), loc.get("victory.menu") };
        y = sh * 0.34f;
        for (int i = 0; i < items.length; i++) {
            boolean selected = i == overlay.selectedIndex;
            drawMenuEntry(labels[i], selected, sw, y);
            y -= fonts.item.getLineHeight() * 1.45f;
        }
        batch.end();
    }

    private void drawVeil(float sw, float sh) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(VEIL);
        shapes.rect(0, 0, sw, sh);
        shapes.end();
    }

    private void drawPause(GameOverlayModel overlay, float sw, float sh) {
        drawCenteredTitle(loc.get("pause.title"), sw, sh * 0.72f);

        PauseItem[] items = PauseItem.values();
        String[] labels = {
            loc.get("pause.continue"),
            loc.get("pause.settings"),
            loc.get("pause.cheats"),
            loc.get("pause.saveExit"),
        };
        batch.begin();
        float y = sh * 0.58f;
        for (int i = 0; i < items.length; i++) {
            boolean selected = i == overlay.selectedIndex;
            drawMenuEntry(labels[i], selected, sw, y);
            y -= fonts.item.getLineHeight() * 1.35f;
        }
        batch.end();
    }

    private void drawPauseCheats(float sw, float sh, CheatState cheats) {
        drawCenteredTitle(loc.get("cheats.title"), sw, sh * 0.76f);
        batch.begin();
        drawCentered(fonts.small, loc.get("cheats.hint"), DIM, sw, sh * 0.66f);

        float y = sh * 0.58f;
        for (CheatCode cheat : CheatCode.values()) {
            boolean active = cheats.isActive(cheat);
            String mark = active ? "[x] " : "[ ] ";
            String line = mark + cheat.comboLabel() + "   —   " + loc.get("cheat." + cheat.name());
            drawCentered(fonts.item, line, active ? ACCENT : TEXT, sw, y);
            y -= fonts.item.getLineHeight() * 1.3f;
        }
        batch.end();
    }

    private void drawSettings(GameOverlayModel overlay, AppModel app, float sw, float sh) {
        drawCenteredTitle(loc.get("settings.title"), sw, sh * 0.88f);

        SettingsItem[] items = SettingsItem.values();
        float rowH = fonts.item.getLineHeight() * 1.18f;
        float top  = sh * 0.76f;

        batch.begin();
        for (int i = 0; i < items.length; i++) {
            boolean selected = i == overlay.selectedIndex;
            String value = settingsValueLabel(app, items[i]);
            String line = loc.get(items[i].labelKey) + (value.isEmpty() ? "" : "   —   " + value);
            drawMenuEntry(line, selected, sw, top - i * rowH);
        }
        drawCentered(fonts.small, loc.get("common.navigate") + "    " + loc.get("common.adjust")
                + "    " + loc.get("common.select"), DIM, sw, sh * 0.10f);
        drawCentered(fonts.small, loc.get("common.back"), DIM, sw, sh * 0.06f);
        batch.end();
    }

    private String settingsValueLabel(AppModel app, SettingsItem item) {
        switch (item) {
            case BRIGHTNESS:   return Math.round(app.settings.brightness * 100) + "%";
            case SFX_VOLUME:   return Math.round(app.settings.sfxVolume * 100) + "%";
            case MUSIC_VOLUME: return Math.round(app.settings.musicVolume * 100) + "%";
            case SFX_ENABLED:  return loc.get(app.settings.sfxEnabled ? "common.on" : "common.off");
            case MENU_THEME:   return loc.get("theme." + app.settings.menuThemeIndex);
            case VSYNC:        return loc.get(app.settings.vsync ? "common.on" : "common.off");
            case FPS_CAP:      return app.settings.fpsCap() == 0
                    ? loc.get("settings.fpsUncapped") : app.settings.fpsCap() + " FPS";
            case SHOW_FPS:     return loc.get(app.settings.showFps ? "common.on" : "common.off");
            default:           return "";
        }
    }

    private void drawKeybinds(GameOverlayModel overlay, AppModel app, float sw, float sh) {
        drawCenteredTitle(loc.get("keys.title"), sw, sh * 0.92f);

        GameAction[] actions = GameAction.values();
        float rowH = fonts.small.getLineHeight() * 1.2f;
        float top  = sh * 0.82f;

        batch.begin();
        for (int i = 0; i < actions.length; i++) {
            boolean selected  = i == overlay.selectedIndex;
            boolean capturing = selected && overlay.awaitingRebind != null;
            String keyLabel = capturing ? loc.get("keys.pressAny")
                    : Input.Keys.toString(app.bindings.keyFor(actions[i]));
            String line = loc.get("action." + actions[i].name()) + "   —   " + keyLabel;
            drawSmallEntry(line, selected, sw, top - i * rowH);
        }
        boolean resetSelected = overlay.selectedIndex == actions.length;
        drawSmallEntry(loc.get("keys.reset"), resetSelected, sw, top - actions.length * rowH - rowH * 0.5f);

        drawCentered(fonts.small, loc.get("keys.hint"), DIM, sw, sh * 0.075f);
        batch.end();
    }

    private void drawInventory(GameOverlayModel overlay, CharmInventory charms, float sw, float sh) {
        drawCenteredTitle(loc.get("inv.title"), sw, sh * 0.90f);

        List<Charm> owned = charms.getOwned();
        float panelW = sw * 0.62f;
        float panelX = (sw - panelW) / 2f;
        float rowH   = fonts.item.getLineHeight() * 1.7f;
        float iconD  = rowH * 0.82f;
        float top    = sh * 0.76f;
        float pad    = rowH * 0.30f;
        float textX  = panelX + iconD + 30f;


        batch.begin();
        String notches = loc.format("inv.notches", charms.equippedCount(), CharmInventory.MAX_EQUIPPED)
                + (charms.isFull() ? "   —   " + loc.get("inv.full") : "");
        drawCentered(fonts.small, notches, charms.isFull() ? ACCENT : DIM, sw,
                top + fonts.small.getLineHeight() * 1.4f);
        batch.end();

        for (int i = 0; i < owned.size(); i++) {
            Charm charm = owned.get(i);
            boolean selected = i == overlay.selectedIndex;
            boolean equipped = charms.isEquipped(charm);
            float rowY = top - i * rowH;

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(PANEL.r, PANEL.g, PANEL.b, selected ? 1f : 0.75f);
            shapes.rect(panelX, rowY - rowH + 4f, panelW, rowH - 4f);
            if (equipped) {
                shapes.setColor(ACCENT);
                shapes.rect(panelX + 6f, rowY - rowH + 10f, 4f, rowH - 16f);
            }
            shapes.end();


            float iconCx = panelX + 20f + iconD / 2f;
            float iconCy = rowY - rowH / 2f + 2f;
            batch.begin();
            batch.setColor(1f, 1f, 1f, selected ? 1f : 0.85f);
            batch.draw(charmBackboard, iconCx - iconD / 2f, iconCy - iconD / 2f, iconD, iconD);
            if (!equipped) batch.setColor(0.55f, 0.55f, 0.62f, 0.85f);
            float iconArt = iconD * 0.74f;
            batch.draw(charmIcons.get(charm.type), iconCx - iconArt / 2f, iconCy - iconArt / 2f,
                    iconArt, iconArt);
            batch.setColor(Color.WHITE);

            fonts.item.setColor(selected ? ACCENT : TEXT);
            fonts.item.draw(batch, loc.get("charm." + charm.type.name() + ".name"),
                    textX, rowY - pad);
            fonts.small.setColor(DIM);
            String state = loc.get(equipped ? "inv.equipped" : "inv.notEquipped");
            layout.setText(fonts.small, state);
            fonts.small.draw(batch, state, panelX + panelW - layout.width - 12f, rowY - pad * 1.5f);
            batch.end();
        }


        batch.begin();
        if (!owned.isEmpty()) {
            Charm selectedCharm = owned.get(Math.min(overlay.selectedIndex, owned.size() - 1));
            fonts.small.setColor(TEXT);
            fonts.small.draw(batch, loc.get("charm." + selectedCharm.type.name() + ".desc"),
                    panelX, top - owned.size() * rowH - fonts.small.getLineHeight() * 0.8f,
                    panelW, Align.left, true);
        }
        float hintLine = fonts.small.getLineHeight();
        drawCentered(fonts.small, equippedLine, ACCENT, sw, hintLine * 2.6f);
        drawCentered(fonts.small, loc.get("inv.hint"), DIM, sw, hintLine * 1.3f);
        batch.end();
    }


    private void drawMenuEntry(String label, boolean selected, float sw, float y) {
        drawMenuEntry(fonts.item, label, selected, sw, y);
    }


    private void drawSmallEntry(String label, boolean selected, float sw, float y) {
        drawMenuEntry(fonts.small, label, selected, sw, y);
    }

    private void drawMenuEntry(BitmapFont font, String label, boolean selected, float sw, float y) {
        font.setColor(selected ? ACCENT : TEXT);
        layout.setText(font, label);
        float x = (sw - layout.width) / 2f;
        font.draw(batch, label, x, y);
        if (selected) font.draw(batch, ">", x - layout.height * 1.6f, y);
    }

    private void drawCentered(BitmapFont font, String text, Color color, float sw, float y) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, (sw - layout.width) / 2f, y);
    }

    private void drawCenteredTitle(String text, float sw, float y) {
        batch.begin();
        drawCentered(fonts.heading, text, ACCENT, sw, y);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        for (Texture t : charmIcons.values()) t.dispose();
        charmBackboard.dispose();

    }
}
