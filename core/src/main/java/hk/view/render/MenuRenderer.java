package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import hk.model.AppModel;
import hk.model.GameData;
import hk.model.cheat.CheatCode;
import hk.input.GameAction;
import hk.model.menu.MainMenuItem;
import hk.model.menu.MenuLayout;
import hk.model.menu.MenuModel;
import hk.model.menu.MenuState;
import hk.model.menu.SettingsItem;
import hk.model.progress.Achievement;
import hk.model.progress.AchievementId;
import java.util.EnumMap;
import java.util.Map;
import hk.service.LocalizationService;
import hk.service.SaveService;
import hk.view.helper.FontLibrary;


public class MenuRenderer implements Disposable {



    private enum ParticleMode { FOG, SPORES, RAIN }


    private static class Theme {
        final Color bgTint, accent, text, dim, panel, particle;
        final ParticleMode mode;
        Theme(Color bgTint, Color accent, Color text, Color dim, Color panel,
              Color particle, ParticleMode mode) {
            this.bgTint = bgTint; this.accent = accent; this.text = text;
            this.dim = dim; this.panel = panel; this.particle = particle; this.mode = mode;
        }
    }




    private static final Theme[] THEMES = {
        new Theme(new Color(0.62f, 0.68f, 0.85f, 1f), new Color(0.62f, 0.80f, 1.00f, 1f),
                  new Color(0.93f, 0.95f, 1.00f, 1f), new Color(0.52f, 0.57f, 0.70f, 1f),
                  new Color(0.02f, 0.04f, 0.09f, 0.72f), new Color(0.65f, 0.75f, 0.95f, 1f),
                  ParticleMode.FOG),
        new Theme(new Color(0.47f, 0.82f, 0.52f, 1f), new Color(0.66f, 1.00f, 0.62f, 1f),
                  new Color(0.92f, 1.00f, 0.92f, 1f), new Color(0.48f, 0.62f, 0.48f, 1f),
                  new Color(0.02f, 0.07f, 0.03f, 0.72f), new Color(0.62f, 0.95f, 0.55f, 1f),
                  ParticleMode.SPORES),
        new Theme(new Color(0.62f, 0.52f, 0.92f, 1f), new Color(0.84f, 0.66f, 1.00f, 1f),
                  new Color(0.96f, 0.93f, 1.00f, 1f), new Color(0.56f, 0.50f, 0.68f, 1f),
                  new Color(0.05f, 0.03f, 0.10f, 0.72f), new Color(0.78f, 0.72f, 1.00f, 1f),
                  ParticleMode.RAIN),
    };

    private static final String[] THEME_NAME_KEYS = { "theme.0", "theme.1", "theme.2" };

    private static final float PAGE_FADE_SECONDS  = 0.30f;
    private static final float LOGO_FADE_SECONDS  = 1.40f;
    private static final float LOGO_ASPECT        = 540f / 1235f;



    private final SpriteBatch        batch;
    private final ShapeRenderer      shapes;
    private final OrthographicCamera uiCamera;
    private final GlyphLayout        glyphs;
    private final FontLibrary        fonts;

    private final Texture bgTexture;
    private final Texture logoTexture;
    private final Texture glowTexture;
    private final Texture softTexture;
    private final Texture streakTexture;
    private final Map<AchievementId, Texture> achievementIcons = new EnumMap<>(AchievementId.class);



    private final AppModel            app;
    private final LocalizationService loc;
    private final SaveService         saveService;
    private final MenuLayout          hitboxes;



    private float time;
    private float pageTime;
    private MenuState lastState = null;
    private float pageAlpha;
    private float markerY = -1f;
    private float markerHalfWidth;

    private Particle[] particles;
    private ParticleMode particleMode;


    private final GameData[] slotCache = new GameData[SaveService.SLOT_COUNT];
    private boolean slotCacheValid = false;

    private static class Particle {
        float x, y, size, vx, vy, alpha, phase;
    }

    public MenuRenderer(AppModel app, LocalizationService loc, SaveService saveService,
                        MenuLayout hitboxes) {
        this.app = app;
        this.loc = loc;
        this.saveService = saveService;
        this.hitboxes = hitboxes;

        batch    = new SpriteBatch();
        shapes   = new ShapeRenderer();
        glyphs   = new GlyphLayout();
        uiCamera = new OrthographicCamera();
        fonts    = new FontLibrary();

        bgTexture   = loadLinear("menu/bg_main.png");
        logoTexture = loadLinear("menu/title_logo.png");
        glowTexture = loadLinear("menu/glow_burst.png");
        softTexture   = makeSoftBlob(128);
        streakTexture = makeStreak(4, 64);
        for (AchievementId id : AchievementId.values()) {
            achievementIcons.put(id, loadLinear("Achievements/" + id.name() + ".png"));
        }
    }

    private static Texture loadLinear(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }


    private static Texture makeSoftBlob(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - r) / r, dy = (y - r) / r;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = MathUtils.clamp(1f - d, 0f, 1f);
                pm.setColor(1f, 1f, 1f, a * a);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }


    private static Texture makeStreak(int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = y / (float) (h - 1);
            float a = MathUtils.clamp(1f - Math.abs(t - 0.5f) * 2f, 0f, 1f);
            for (int x = 0; x < w; x++) {
                float ax = x == 0 || x == w - 1 ? 0.4f : 1f;
                pm.setColor(1f, 1f, 1f, a * ax);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        pm.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }


    public void invalidateSlotCache() {
        slotCacheValid = false;
    }



    public void draw(MenuModel menu, float delta) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        fonts.ensure((int) sh);
        uiCamera.setToOrtho(false, sw, sh);
        uiCamera.update();
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        time += delta;
        if (menu.state != lastState) {
            lastState = menu.state;
            pageTime = 0f;
            markerY = -1f;
        }
        pageTime += delta;
        pageAlpha = MathUtils.clamp(pageTime / PAGE_FADE_SECONDS, 0f, 1f);
        pageAlpha = pageAlpha * pageAlpha * (3f - 2f * pageAlpha);

        Theme theme = THEMES[Math.floorMod(app.settings.menuThemeIndex, THEMES.length)];
        hitboxes.clear();

        drawBackdrop(theme, sw, sh, delta);

        switch (menu.state) {
            case MAIN:         drawMain(menu, theme, sw, sh, delta);     break;
            case SLOTS:        drawSlots(menu, theme, sw, sh, delta);    break;
            case SETTINGS:     drawSettings(menu, theme, sw, sh, delta); break;
            case KEYBINDS:     drawKeybinds(menu, theme, sw, sh, delta); break;
            case GUIDE:        drawGuide(theme, sw, sh);                 break;
            case ACHIEVEMENTS: drawAchievements(theme, sw, sh);          break;
            case CHEATS:       drawCheats(theme, sw, sh);                break;
        }

        BrightnessRenderer.draw(shapes, uiCamera, app.settings.brightness, sw, sh);

        if (app.settings.showFps) {
            String fps = Gdx.graphics.getFramesPerSecond() + " FPS";
            batch.begin();
            fonts.small.setColor(1f, 1f, 1f, 0.7f);
            glyphs.setText(fonts.small, fps);
            fonts.small.draw(batch, fps, sw - glyphs.width - sh * 0.02f, sh - sh * 0.018f);
            batch.end();
        }
    }



    private void drawBackdrop(Theme theme, float sw, float sh, float delta) {

        float scale = Math.max(sw / bgTexture.getWidth(), sh / bgTexture.getHeight());
        float bw = bgTexture.getWidth() * scale, bh = bgTexture.getHeight() * scale;
        batch.begin();
        batch.setColor(theme.bgTint.r, theme.bgTint.g, theme.bgTint.b, 1f);
        batch.draw(bgTexture, (sw - bw) / 2f, (sh - bh) / 2f, bw, bh);
        batch.setColor(Color.WHITE);
        batch.end();

        drawParticles(theme, sw, sh, delta);


        Color clear = new Color(0f, 0f, 0f, 0f);
        Color floor = new Color(0f, 0f, 0f, 0.60f);
        Color roof  = new Color(0f, 0f, 0f, 0.40f);
        beginShapes();
        shapes.rect(0, 0, sw, sh * 0.38f, floor, floor, clear, clear);
        shapes.rect(0, sh * 0.75f, sw, sh * 0.25f, clear, clear, roof, roof);
        shapes.end();
    }

    private void drawParticles(Theme theme, float sw, float sh, float delta) {
        if (particles == null || particleMode != theme.mode) {
            initParticles(theme.mode, sw, sh);
        }
        boolean rain = theme.mode == ParticleMode.RAIN;
        Texture tex = rain ? streakTexture : softTexture;

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Particle p : particles) {
            p.x += p.vx * delta;
            p.y += p.vy * delta;

            float m = p.size;
            if (p.x < -m) p.x = sw + m; else if (p.x > sw + m) p.x = -m;
            if (p.y < -m) p.y = sh + m; else if (p.y > sh + m) p.y = -m;

            float twinkle = 0.75f + 0.25f * MathUtils.sin(time * 1.7f + p.phase);
            batch.setColor(theme.particle.r, theme.particle.g, theme.particle.b,
                           p.alpha * twinkle);
            if (rain) {
                batch.draw(tex, p.x, p.y, p.size * 0.06f, p.size);
            } else {
                batch.draw(tex, p.x - p.size / 2f, p.y - p.size / 2f, p.size, p.size);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void initParticles(ParticleMode mode, float sw, float sh) {
        particleMode = mode;
        int count = mode == ParticleMode.FOG ? 26 : mode == ParticleMode.SPORES ? 70 : 110;
        particles = new Particle[count];
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = MathUtils.random(0f, sw);
            p.y = MathUtils.random(0f, sh);
            p.phase = MathUtils.random(0f, MathUtils.PI2);
            switch (mode) {
                case FOG:
                    p.size  = sh * MathUtils.random(0.25f, 0.65f);
                    p.vx    = sh * MathUtils.random(-0.03f, 0.03f);
                    p.vy    = sh * MathUtils.random(-0.008f, 0.008f);
                    p.alpha = MathUtils.random(0.03f, 0.08f);
                    break;
                case SPORES:
                    p.size  = sh * MathUtils.random(0.004f, 0.012f);
                    p.vx    = sh * MathUtils.random(-0.02f, 0.02f);
                    p.vy    = sh * MathUtils.random(0.01f, 0.05f);
                    p.alpha = MathUtils.random(0.25f, 0.7f);
                    break;
                case RAIN:
                    p.size  = sh * MathUtils.random(0.04f, 0.09f);
                    p.vx    = sh * MathUtils.random(-0.06f, -0.02f);
                    p.vy    = sh * MathUtils.random(-1.6f, -0.9f);
                    p.alpha = MathUtils.random(0.10f, 0.30f);
                    break;
            }
            particles[i] = p;
        }
    }



    private void drawMain(MenuModel menu, Theme theme, float sw, float sh, float delta) {

        float logoAlpha = MathUtils.clamp(time / LOGO_FADE_SECONDS, 0f, 1f) * pageAlpha;
        float logoW = Math.min(sw * 0.52f, sh * 0.95f);
        float logoH = logoW * LOGO_ASPECT;
        float logoCx = sw / 2f, logoCy = sh * 0.775f;

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        float pulse = 0.30f + 0.10f * MathUtils.sin(time * 1.3f);
        float glowSize = logoW * (1.15f + 0.05f * MathUtils.sin(time * 0.8f));
        batch.setColor(theme.accent.r, theme.accent.g, theme.accent.b, pulse * logoAlpha);
        batch.draw(glowTexture, logoCx - glowSize / 2f, logoCy - glowSize / 2f, glowSize, glowSize);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, logoAlpha);
        batch.draw(logoTexture, logoCx - logoW / 2f, logoCy - logoH / 2f, logoW, logoH);
        batch.setColor(Color.WHITE);
        batch.end();


        MainMenuItem[] items = MainMenuItem.values();
        float rowH = sh * 0.058f;
        float y = sh * 0.545f;
        float selY = y, selHalfWidth = 100f;

        batch.begin();
        for (int i = 0; i < items.length; i++) {
            boolean selected = i == menu.selectedIndex;
            String label = loc.get(items[i].labelKey);
            glyphs.setText(fonts.item, label);
            float x = (sw - glyphs.width) / 2f;
            setFont(fonts.item, selected ? theme.accent : theme.text, 1f);
            fonts.item.draw(batch, label, x, y);
            hitboxes.add(MenuLayout.Kind.ROW, i,
                    x - rowH, y - glyphs.height - rowH * 0.35f, glyphs.width + rowH * 2f, rowH);
            if (selected) {
                selY = y - glyphs.height / 2f;
                selHalfWidth = glyphs.width / 2f;
            }
            y -= rowH;
        }
        batch.end();

        drawSelectionMarker(theme, sw, selY, selHalfWidth, rowH, delta);


        String controls = loc.get("menu.controls") + ":   "
                + keyName(GameAction.LEFT) + " / " + keyName(GameAction.RIGHT) + "    "
                + loc.get("action.JUMP") + " " + keyName(GameAction.JUMP) + "    "
                + loc.get("action.DASH") + " " + keyName(GameAction.DASH) + "    "
                + loc.get("action.ATTACK") + " " + keyName(GameAction.ATTACK);
        batch.begin();
        setFont(fonts.small, theme.dim, 1f);
        glyphs.setText(fonts.small, controls);
        fonts.small.draw(batch, controls, (sw - glyphs.width) / 2f, sh * 0.045f + glyphs.height);
        batch.end();
    }


    private void drawSelectionMarker(Theme theme, float sw, float targetY, float halfWidth,
                                     float rowH, float delta) {
        if (markerY < 0f) { markerY = targetY; markerHalfWidth = halfWidth; }
        float k = 1f - (float) Math.exp(-14f * delta);
        markerY += (targetY - markerY) * k;
        markerHalfWidth += (halfWidth - markerHalfWidth) * k;

        float cx = sw / 2f;
        float glowW = markerHalfWidth * 2f + rowH * 3f;
        float glowH = rowH * 1.7f;

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(theme.accent.r, theme.accent.g, theme.accent.b, 0.16f * pageAlpha);
        batch.draw(softTexture, cx - glowW / 2f, markerY - glowH / 2f, glowW, glowH);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
        batch.end();

        float d = rowH * 0.16f;
        float px = markerHalfWidth + rowH * 0.9f;
        beginShapes();
        shapeColor(theme.accent, 0.9f);
        shapes.triangle(cx - px - d, markerY, cx - px + d, markerY + d, cx - px + d, markerY - d);
        shapes.triangle(cx + px + d, markerY, cx + px - d, markerY + d, cx + px - d, markerY - d);
        shapes.end();
    }

    private void drawSlots(MenuModel menu, Theme theme, float sw, float sh, float delta) {
        refreshSlotCacheIfNeeded();
        drawPageTitle(theme, loc.get(menu.slotMode == MenuModel.SlotMode.NEW_GAME
                ? "slot.title.new" : "slot.title.load"), sw, sh);

        float cardW = sw * 0.46f;
        float cardH = sh * 0.105f;
        float gap   = sh * 0.022f;
        float x     = (sw - cardW) / 2f;
        float top   = sh * 0.74f;

        for (int i = 0; i < SaveService.SLOT_COUNT; i++) {
            float cy = top - i * (cardH + gap);
            boolean selected = i == menu.selectedIndex;
            GameData data = slotCache[i];

            beginShapes();
            shapeColor(theme.panel, 1f);
            shapes.rect(x, cy - cardH, cardW, cardH);
            if (selected) {
                shapeColor(theme.accent, 0.95f);
                float b = Math.max(2f, sh * 0.003f);
                shapes.rect(x, cy - cardH, cardW, b);
                shapes.rect(x, cy - b, cardW, b);
                shapes.rect(x, cy - cardH, b, cardH);
                shapes.rect(x + cardW - b, cy - cardH, b, cardH);
            }
            shapes.end();
            hitboxes.add(MenuLayout.Kind.ROW, i, x, cy - cardH, cardW, cardH);

            batch.begin();
            setFont(fonts.heading, selected ? theme.accent : theme.dim, 1f);
            fonts.heading.draw(batch, String.valueOf(i + 1),
                    x + cardW * 0.045f, cy - cardH * 0.28f);
            String info = data == null
                    ? loc.get("slot.empty")
                    : loc.format("slot.info", data.masks, data.soul,
                                 formatTime(data.playTimeSeconds));
            setFont(fonts.item, data == null ? theme.dim : theme.text, 1f);
            fonts.item.draw(batch, info, x + cardW * 0.16f, cy - cardH * 0.38f);
            batch.end();
        }

        drawFooterHint(theme, loc.get(menu.slotMode == MenuModel.SlotMode.NEW_GAME
                ? "slot.hint.new" : "slot.hint.load"), sw, sh);
        drawBackHint(theme, sw, sh);
    }

    private void drawSettings(MenuModel menu, Theme theme, float sw, float sh, float delta) {
        drawPageTitle(theme, loc.get("settings.title"), sw, sh);

        SettingsItem[] items = SettingsItem.values();
        float labelX = sw * 0.26f;
        float valueX = sw * 0.56f;
        float valueW = sw * 0.19f;
        float rowH   = sh * 0.058f;
        float y      = sh * 0.72f;
        float selY = y, selHalf = sw * 0.24f;

        for (int i = 0; i < items.length; i++) {
            boolean selected = i == menu.selectedIndex;
            SettingsItem item = items[i];

            batch.begin();
            setFont(fonts.item, selected ? theme.accent : theme.text, 1f);
            fonts.item.draw(batch, loc.get(item.labelKey), labelX, y);
            batch.end();

            switch (item) {
                case BRIGHTNESS:   drawSlider(theme, valueX, y, valueW, sh, i,
                                              app.settings.brightness);  break;
                case SFX_VOLUME:   drawSlider(theme, valueX, y, valueW, sh, i,
                                              app.settings.sfxVolume);   break;
                case MUSIC_VOLUME: drawSlider(theme, valueX, y, valueW, sh, i,
                                              app.settings.musicVolume); break;
                case SFX_ENABLED:  drawChoice(theme, valueX, y, loc.get(
                        app.settings.sfxEnabled ? "common.on" : "common.off"));           break;
                case MENU_THEME:   drawChoice(theme, valueX, y, loc.get(
                        THEME_NAME_KEYS[app.settings.menuThemeIndex % THEME_NAME_KEYS.length])); break;
                case VSYNC:        drawChoice(theme, valueX, y, loc.get(
                        app.settings.vsync ? "common.on" : "common.off"));                       break;
                case FPS_CAP:      drawChoice(theme, valueX, y, app.settings.fpsCap() == 0
                        ? loc.get("settings.fpsUncapped")
                        : app.settings.fpsCap() + " FPS");                                       break;
                case SHOW_FPS:     drawChoice(theme, valueX, y, loc.get(
                        app.settings.showFps ? "common.on" : "common.off"));                     break;
                default: break;
            }

            hitboxes.add(MenuLayout.Kind.ROW, i, labelX - rowH * 0.4f, y - rowH * 0.75f,
                    valueX + valueW + sw * 0.08f - labelX, rowH);
            if (selected) selY = y - fonts.item.getCapHeight() / 2f;
            y -= rowH;
        }

        drawSideMarker(theme, labelX - sw * 0.02f, selY, sh, delta);
        drawFooterHint(theme, loc.get("common.navigate") + "      " + loc.get("common.adjust")
                + "      " + loc.get("common.select"), sw, sh);
        drawBackHint(theme, sw, sh);
    }

    private void drawSlider(Theme theme, float x, float rowY, float w, float sh,
                            int rowIndex, float value) {
        float h = Math.max(3f, sh * 0.006f);
        float y = rowY - fonts.item.getCapHeight() / 2f - h / 2f;

        beginShapes();
        shapeColor(theme.dim, 0.45f);
        shapes.rect(x, y, w, h);
        shapeColor(theme.accent, 0.95f);
        shapes.rect(x, y, w * value, h);
        shapes.end();


        float knob = h * 6f;
        batch.begin();
        batch.setColor(theme.accent.r, theme.accent.g, theme.accent.b, pageAlpha);
        batch.draw(softTexture, x + w * value - knob / 2f, y + h / 2f - knob / 2f, knob, knob);
        setFont(fonts.small, theme.text, 1f);
        fonts.small.draw(batch, Math.round(value * 100) + "%", x + w + sh * 0.018f,
                rowY - fonts.item.getCapHeight() / 2f + fonts.small.getCapHeight() / 2f);
        batch.setColor(Color.WHITE);
        batch.end();


        hitboxes.add(MenuLayout.Kind.SLIDER, rowIndex, x, y - knob, w, knob * 3f);
    }

    private void drawChoice(Theme theme, float x, float rowY, String value) {
        batch.begin();
        setFont(fonts.item, theme.dim, 1f);
        fonts.item.draw(batch, "<", x, rowY);
        setFont(fonts.item, theme.text, 1f);
        glyphs.setText(fonts.item, value);
        fonts.item.draw(batch, value, x + fonts.item.getSpaceXadvance() * 3f, rowY);
        setFont(fonts.item, theme.dim, 1f);
        fonts.item.draw(batch, ">", x + fonts.item.getSpaceXadvance() * 6f + glyphs.width, rowY);
        batch.end();
    }

    private void drawKeybinds(MenuModel menu, Theme theme, float sw, float sh, float delta) {
        drawPageTitle(theme, loc.get("keys.title"), sw, sh);

        GameAction[] actions = GameAction.values();
        float labelX = sw * 0.30f;
        float keyX   = sw * 0.60f;
        float rowH   = sh * 0.0485f;
        float y      = sh * 0.755f;
        float selY   = y;

        batch.begin();
        for (int i = 0; i < actions.length; i++) {
            boolean selected  = i == menu.selectedIndex;
            boolean capturing = selected && menu.awaitingRebind != null;

            setFont(fonts.small, selected ? theme.accent : theme.text, 1.25f);
            fonts.small.draw(batch, loc.get("action." + actions[i].name()), labelX, y);
            if (capturing) {
                float blink = 0.55f + 0.45f * MathUtils.sin(time * 6f);
                setFont(fonts.small, theme.accent, blink);
                fonts.small.draw(batch, loc.get("keys.pressAny"), keyX, y);
            } else {
                setFont(fonts.small, selected ? theme.text : theme.dim, 1f);
                fonts.small.draw(batch, Input.Keys.toString(app.bindings.keyFor(actions[i])), keyX, y);
            }
            hitboxes.add(MenuLayout.Kind.ROW, i, labelX - rowH * 0.4f, y - rowH * 0.72f,
                    keyX + sw * 0.14f - labelX, rowH);
            if (selected) selY = y - fonts.small.getCapHeight() / 2f;
            y -= rowH;
        }


        boolean resetSelected = menu.selectedIndex == actions.length;
        setFont(fonts.item, resetSelected ? theme.accent : theme.text, 1f);
        fonts.item.draw(batch, loc.get("keys.reset"), labelX, y);
        hitboxes.add(MenuLayout.Kind.ROW, actions.length, labelX - rowH * 0.4f, y - rowH * 0.8f,
                sw * 0.44f, rowH * 1.1f);
        if (resetSelected) selY = y - fonts.item.getCapHeight() / 2f;
        batch.end();

        drawSideMarker(theme, labelX - sw * 0.02f, selY, sh, delta);
        drawFooterHint(theme, loc.get("keys.hint"), sw, sh);
        drawBackHint(theme, sw, sh);
    }

    private void drawGuide(Theme theme, float sw, float sh) {
        drawPageTitle(theme, loc.get("guide.title"), sw, sh);


        float colW = sw * 0.34f;
        float leftX = sw * 0.115f, rightX = sw * 0.545f;
        float topY = sh * 0.76f;
        int sections = 6;

        batch.begin();
        for (int s = 0; s < sections; s++) {
            float x = s < 3 ? leftX : rightX;
            if (s % 3 == 0) topY = sh * 0.76f;

            String head = loc.get("guide.s" + (s + 1) + ".title");
            String body = loc.get("guide.s" + (s + 1) + ".body");

            setFont(fonts.item, theme.accent, 1f);
            fonts.item.draw(batch, head, x, topY);
            topY -= fonts.item.getLineHeight() * 1.05f;

            setFont(fonts.small, theme.text, 1f);
            glyphs.setText(fonts.small, body, fonts.small.getColor(), colW, Align.left, true);
            fonts.small.draw(batch, body, x, topY, colW, Align.left, true);
            topY -= glyphs.height + fonts.small.getLineHeight() * 1.4f;
        }
        batch.end();



        String controls = loc.get("guide.controlsLabel") + ":   "
                + keyName(GameAction.LEFT) + "/" + keyName(GameAction.RIGHT) + " " + loc.get("guide.moveLabel") + "    "
                + keyName(GameAction.JUMP) + " " + loc.get("action.JUMP") + "    "
                + keyName(GameAction.DASH) + " " + loc.get("action.DASH") + "    "
                + keyName(GameAction.ATTACK) + " " + loc.get("action.ATTACK");
        batch.begin();
        setFont(fonts.small, theme.dim, 1f);
        glyphs.setText(fonts.small, controls);
        fonts.small.draw(batch, controls, (sw - glyphs.width) / 2f, sh * 0.10f);
        batch.end();

        drawBackHint(theme, sw, sh);
    }

    private void drawAchievements(Theme theme, float sw, float sh) {
        drawPageTitle(theme, loc.get("ach.title"), sw, sh);

        AchievementId[] ids = AchievementId.values();
        float cardW = sw * 0.5f;
        float cardH = sh * 0.095f;
        float gap   = sh * 0.016f;
        float x     = (sw - cardW) / 2f;
        float top   = sh * 0.76f;

        for (int i = 0; i < ids.length; i++) {
            Achievement a = app.achievements.get(ids[i]);
            float cy = top - i * (cardH + gap);
            float alpha = a.unlocked ? 1f : 0.4f;

            beginShapes();
            shapeColor(theme.panel, alpha + 0.15f);
            shapes.rect(x, cy - cardH, cardW, cardH);
            shapes.end();


            float ix = x + cardH * 0.55f, iy = cy - cardH / 2f, r = cardH * 0.34f;
            Texture icon = achievementIcons.get(ids[i]);
            batch.begin();
            if (a.unlocked) batch.setColor(1f, 1f, 1f, pageAlpha);
            else             batch.setColor(0.42f, 0.42f, 0.42f, 0.7f * pageAlpha);
            batch.draw(icon, ix - r, iy - r, r * 2f, r * 2f);
            batch.setColor(Color.WHITE);
            batch.end();

            batch.begin();
            setFont(fonts.item, a.unlocked ? theme.accent : theme.dim, alpha + 0.25f);
            fonts.item.draw(batch, loc.get("achievement." + ids[i].name() + ".title"),
                    x + cardH * 1.2f, cy - cardH * 0.22f);
            setFont(fonts.small, theme.text, alpha);
            fonts.small.draw(batch, loc.get("achievement." + ids[i].name() + ".desc"),
                    x + cardH * 1.2f, cy - cardH * 0.60f);
            String state = loc.get(a.unlocked ? "ach.unlocked" : "ach.locked");
            setFont(fonts.small, theme.dim, 1f);
            glyphs.setText(fonts.small, state);
            fonts.small.draw(batch, state, x + cardW - glyphs.width - cardH * 0.35f,
                    cy - cardH * 0.22f);
            batch.end();
        }
        drawBackHint(theme, sw, sh);
    }

    private void drawCheats(Theme theme, float sw, float sh) {
        drawPageTitle(theme, loc.get("cheats.title"), sw, sh);

        batch.begin();
        setFont(fonts.item, theme.dim, 1f);
        glyphs.setText(fonts.item, loc.get("cheats.hint"));
        fonts.item.draw(batch, loc.get("cheats.hint"), (sw - glyphs.width) / 2f, sh * 0.755f);

        float comboX = sw * 0.30f;
        float descX  = sw * 0.47f;
        float rowH   = sh * 0.062f;
        float y      = sh * 0.67f;
        for (CheatCode cheat : CheatCode.values()) {
            setFont(fonts.item, theme.accent, 1f);
            fonts.item.draw(batch, cheat.comboLabel(), comboX, y);
            setFont(fonts.item, theme.text, 1f);
            fonts.item.draw(batch, loc.get("cheat." + cheat.name()), descX, y);
            y -= rowH;
        }
        batch.end();
        drawBackHint(theme, sw, sh);
    }




    private void drawPageTitle(Theme theme, String text, float sw, float sh) {
        batch.begin();
        setFont(fonts.heading, theme.text, 1f);
        glyphs.setText(fonts.heading, text);
        fonts.heading.draw(batch, text, (sw - glyphs.width) / 2f, sh * 0.905f);
        batch.end();

        beginShapes();
        shapeColor(theme.accent, 0.65f);
        float lineW = Math.max(glyphs.width * 1.3f, sw * 0.24f);
        shapes.rect((sw - lineW) / 2f, sh * 0.905f - fonts.heading.getCapHeight()
                - sh * 0.022f, lineW, Math.max(1.5f, sh * 0.0018f));
        shapes.end();
    }


    private void drawSideMarker(Theme theme, float x, float targetY, float sh, float delta) {
        if (markerY < 0f) markerY = targetY;
        markerY += (targetY - markerY) * (1f - (float) Math.exp(-14f * delta));
        float d = sh * 0.011f;
        beginShapes();
        shapeColor(theme.accent, 0.9f);
        shapes.triangle(x - d, markerY + d, x - d, markerY - d, x + d, markerY);
        shapes.end();
    }

    private void drawFooterHint(Theme theme, String text, float sw, float sh) {
        batch.begin();
        setFont(fonts.small, theme.dim, 1f);
        glyphs.setText(fonts.small, text);
        fonts.small.draw(batch, text, (sw - glyphs.width) / 2f, sh * 0.075f);
        batch.end();
    }

    private void drawBackHint(Theme theme, float sw, float sh) {
        String text = loc.get("common.back");
        batch.begin();
        setFont(fonts.small, theme.dim, 1f);
        glyphs.setText(fonts.small, text);
        float x = sw * 0.02f, y = sh * 0.045f + glyphs.height;
        fonts.small.draw(batch, text, x, y);
        batch.end();
        hitboxes.add(MenuLayout.Kind.BACK, 0, x - 8f, y - glyphs.height - 12f,
                glyphs.width + 16f, glyphs.height + 24f);
    }




    private void setFont(BitmapFont font, Color c, float alpha) {
        font.setColor(c.r, c.g, c.b, MathUtils.clamp(c.a * alpha, 0f, 1f) * pageAlpha);
    }

    private void shapeColor(Color c, float alpha) {
        shapes.setColor(c.r, c.g, c.b, MathUtils.clamp(c.a * alpha, 0f, 1f) * pageAlpha);
    }

    private void beginShapes() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    private String keyName(GameAction action) {
        return "[" + Input.Keys.toString(app.bindings.keyFor(action)) + "]";
    }

    private void refreshSlotCacheIfNeeded() {
        if (slotCacheValid) return;
        for (int i = 0; i < SaveService.SLOT_COUNT; i++) {
            slotCache[i] = saveService.loadSlot(i + 1);
        }
        slotCacheValid = true;
    }

    private String formatTime(float seconds) {
        int total = (int) seconds;
        return String.format("%d:%02d", total / 60, total % 60);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        fonts.dispose();
        bgTexture.dispose();
        logoTexture.dispose();
        glowTexture.dispose();
        softTexture.dispose();
        streakTexture.dispose();
        for (Texture t : achievementIcons.values()) t.dispose();
    }
}
