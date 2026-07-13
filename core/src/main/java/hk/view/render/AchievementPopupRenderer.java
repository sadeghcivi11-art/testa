package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayDeque;
import java.util.Deque;

import hk.model.progress.Achievement;
import hk.service.AchievementService;
import hk.service.AudioService;
import hk.service.LocalizationService;
import hk.view.helper.FontLibrary;


public class AchievementPopupRenderer implements Disposable, AchievementService.Listener {

    private static final float SLIDE_TIME  = 0.35f;
    private static final float HOLD_TIME   = 3.0f;
    private static final float MARGIN      = 16f;

    private final SpriteBatch        batch;
    private final ShapeRenderer      shapes;
    private final FontLibrary        fonts;
    private final OrthographicCamera uiCamera;
    private final GlyphLayout        layout;
    private final LocalizationService loc;
    private final AudioService       audio;

    private final Deque<Achievement> queue = new ArrayDeque<>();
    private Achievement current = null;
    private float timer = 0f;

    public AchievementPopupRenderer(LocalizationService loc, FontLibrary fonts, AudioService audio) {
        this.loc   = loc;
        this.fonts = fonts;
        this.audio = audio;
        batch    = new SpriteBatch();
        shapes   = new ShapeRenderer();
        layout   = new GlyphLayout();
        uiCamera = new OrthographicCamera();
    }

    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        queue.addLast(achievement);
        audio.playSfx(AudioService.ACHIEVEMENT_UNLOCK);
    }


    public void update(float delta) {
        if (current == null) {
            current = queue.pollFirst();
            timer = 0f;
            if (current == null) return;
        }
        timer += delta;
        float total = SLIDE_TIME + HOLD_TIME + SLIDE_TIME;
        if (timer >= total) {
            current = null;
            return;
        }
        draw(slideOffset(total));
    }


    private float slideOffset(float total) {
        if (timer < SLIDE_TIME)          return 1f - timer / SLIDE_TIME;
        if (timer > total - SLIDE_TIME)  return (timer - (total - SLIDE_TIME)) / SLIDE_TIME;
        return 0f;
    }

    private void draw(float offset) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        uiCamera.setToOrtho(false, sw, sh);
        uiCamera.update();


        String label = loc.get("ach.popup");
        String title = loc.get("achievement." + current.id.name() + ".title");
        float pad    = fonts.small.getLineHeight() * 0.6f;
        layout.setText(fonts.item, title);
        float cardW  = Math.max(layout.width + pad * 3f, sw * 0.18f);
        float cardH  = fonts.small.getLineHeight() + fonts.item.getLineHeight() + pad * 2.2f;

        float x = sw - MARGIN - cardW + offset * (cardW + MARGIN);
        float y = sh - MARGIN - cardH;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(uiCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.09f, 0.14f, 0.95f);
        shapes.rect(x, y, cardW, cardH);
        shapes.setColor(0.75f, 0.85f, 1f, 1f);
        shapes.rect(x, y, 4f, cardH);
        shapes.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        fonts.small.setColor(0.75f, 0.85f, 1f, 1f);
        fonts.small.draw(batch, label, x + pad * 1.5f, y + cardH - pad);
        fonts.item.setColor(0.95f, 0.96f, 1f, 1f);
        fonts.item.draw(batch, title, x + pad * 1.5f, y + pad + fonts.item.getCapHeight());
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();

    }
}
