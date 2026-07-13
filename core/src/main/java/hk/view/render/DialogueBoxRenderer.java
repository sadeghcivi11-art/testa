package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import hk.model.interaction.DialogueModel;
import hk.service.LocalizationService;
import hk.view.helper.FontLibrary;


public class DialogueBoxRenderer implements Disposable {

    private static final float BOX_HEIGHT_RATIO = 0.25f;
    private static final float PADDING          = 18f;

    private static final Color SPEAKER = new Color(0.95f, 0.82f, 0.3f, 1f);
    private static final Color HINT    = new Color(0.7f, 0.7f, 0.7f, 1f);

    private final SpriteBatch         batch;
    private final ShapeRenderer       shapes;
    private final FontLibrary         fonts;
    private final LocalizationService loc;
    private final OrthographicCamera  uiCamera;
    private final GlyphLayout         layout;

    public DialogueBoxRenderer(FontLibrary fonts, LocalizationService loc) {
        this.fonts = fonts;
        this.loc   = loc;
        batch    = new SpriteBatch();
        shapes   = new ShapeRenderer();
        layout   = new GlyphLayout();
        uiCamera = new OrthographicCamera();
    }


    public void draw(DialogueModel dm, boolean hasPrompt) {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        uiCamera.setToOrtho(false, sw, sh);
        uiCamera.update();

        if (dm.isActive()) {
            drawBox(dm, sw, sh);
        } else if (hasPrompt) {
            drawPrompt(sw, sh);
        }
    }

    private void drawBox(DialogueModel dm, float sw, float sh) {
        float boxH = sh * BOX_HEIGHT_RATIO;
        float boxY = 0f;


        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(uiCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.82f);
        shapes.rect(0, boxY, sw, boxH);
        shapes.end();


        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.6f, 0.5f, 0.2f, 1f);
        shapes.rect(0, boxY + boxH - 2f, sw, 2f);
        shapes.end();


        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        float textAreaW = sw - PADDING * 2f;
        float cursorY   = boxY + boxH - PADDING;


        fonts.heading.setColor(SPEAKER);
        fonts.heading.draw(batch, dm.getSpeakerName(), PADDING, cursorY);


        layout.setText(fonts.heading, dm.getSpeakerName());
        cursorY -= layout.height + PADDING * 0.9f;


        fonts.item.setColor(Color.WHITE);
        fonts.item.draw(batch, dm.getVisibleLine(), PADDING, cursorY, textAreaW, Align.left, true);


        if (dm.isLineFullyRevealed()) {
            fonts.small.setColor(HINT);
            String hint = loc.get("dialogue.continueHint");
            layout.setText(fonts.small, hint);
            fonts.small.draw(batch, hint, sw - PADDING - layout.width, boxY + PADDING + layout.height);
        }

        batch.end();
    }

    private void drawPrompt(float sw, float sh) {
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        fonts.small.setColor(Color.WHITE);
        String prompt = loc.get("dialogue.talkPrompt");
        layout.setText(fonts.small, prompt);
        fonts.small.draw(batch, prompt, (sw - layout.width) / 2f, sh * 0.38f);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();

    }
}
