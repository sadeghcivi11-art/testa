package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Disposable;

import hk.service.LocalizationService;
import hk.view.helper.FontLibrary;


public class ZoneTitleRenderer implements Disposable {

    private static final float FADE_IN  = 0.7f;
    private static final float HOLD     = 2.0f;
    private static final float FADE_OUT = 1.1f;
    private static final float TOTAL    = FADE_IN + HOLD + FADE_OUT;

    private final LocalizationService loc;
    private final FontLibrary fonts = new FontLibrary();
    private final GlyphLayout glyphs = new GlyphLayout();
    private final Texture white;

    private String  text;
    private float   time = TOTAL;

    public ZoneTitleRenderer(LocalizationService loc) {
        this.loc = loc;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        white = new Texture(pm);
        pm.dispose();
    }


    public void show(String zoneName) {
        String key = "zone." + zoneName;
        String localized = loc.get(key);
        text = key.equals(localized) ? zoneName : localized;
        time = 0f;
    }


    public void resize(int screenHeight) {
        fonts.ensure(screenHeight);
    }


    public void draw(SpriteBatch batch, OrthographicCamera uiCamera, float delta) {
        if (text == null || time >= TOTAL) return;
        time += delta;

        float alpha;
        if      (time < FADE_IN)        alpha = Interpolation.fade.apply(time / FADE_IN);
        else if (time < FADE_IN + HOLD) alpha = 1f;
        else                            alpha = Interpolation.fade.apply(
                                            Math.max(0f, 1f - (time - FADE_IN - HOLD) / FADE_OUT));

        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        BitmapFont font = fonts.title;
        glyphs.setText(font, text);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(0.94f, 0.95f, 0.98f, alpha);
        float ty = sh * 0.74f + glyphs.height;
        font.draw(batch, glyphs, (sw - glyphs.width) / 2f, ty);

        float lw = glyphs.width * 0.86f;
        batch.setColor(0.94f, 0.95f, 0.98f, alpha * 0.55f);
        batch.draw(white, (sw - lw) / 2f, ty - glyphs.height - sh * 0.018f, lw, Math.max(1f, sh / 540f));
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        fonts.dispose();
        white.dispose();
    }
}
