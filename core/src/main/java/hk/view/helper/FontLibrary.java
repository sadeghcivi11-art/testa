package hk.view.helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;


public class FontLibrary implements Disposable {

    private static final String TITLE_TTF = "fonts/Cinzel.ttf";
    private static final String BODY_TTF  = "fonts/EBGaramond.ttf";


    private static final float TITLE_FRACTION   = 0.075f;
    private static final float HEADING_FRACTION = 0.052f;
    private static final float ITEM_FRACTION    = 0.034f;
    private static final float SMALL_FRACTION   = 0.023f;

    private FreeTypeFontGenerator titleGenerator;
    private FreeTypeFontGenerator bodyGenerator;

        public BitmapFont title;
       public BitmapFont heading;
         public BitmapFont item;
     public BitmapFont small;

    private int generatedForHeight = -1;

    public FontLibrary() {
        FileHandle titleFile = Gdx.files.internal(TITLE_TTF);
        FileHandle bodyFile  = Gdx.files.internal(BODY_TTF);
        if (titleFile.exists()) titleGenerator = new FreeTypeFontGenerator(titleFile);
        if (bodyFile.exists())  bodyGenerator  = new FreeTypeFontGenerator(bodyFile);
        if (titleGenerator == null || bodyGenerator == null) {
            Gdx.app.log("FontLibrary", "TTF missing under assets/fonts/ — using built-in font");
        }
        ensure(Gdx.graphics.getHeight());
    }


    public void ensure(int screenHeight) {
        int h = Math.max(screenHeight, 240);
        if (h == generatedForHeight) return;
        generatedForHeight = h;

        disposeFonts();
        title   = make(titleGenerator, Math.round(h * TITLE_FRACTION),   true);
        heading = make(titleGenerator, Math.round(h * HEADING_FRACTION), true);
        item    = make(bodyGenerator,  Math.round(h * ITEM_FRACTION),    false);
        small   = make(bodyGenerator,  Math.round(h * SMALL_FRACTION),   false);
    }

    private BitmapFont make(FreeTypeFontGenerator generator, int size, boolean titled) {
        if (generator == null) {

            BitmapFont fallback = new BitmapFont();
            fallback.getData().setScale(Math.max(0.5f, size / 15f));
            fallback.getData().markupEnabled = false;
            return fallback;
        }
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = Math.max(8, size);
        p.color = Color.WHITE;
        p.incremental = true;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        if (titled) {

            p.shadowOffsetX = 0;
            p.shadowOffsetY = Math.max(1, size / 24);
            p.shadowColor   = new Color(0f, 0f, 0f, 0.6f);
        } else {
            p.shadowOffsetX = 0;
            p.shadowOffsetY = 1;
            p.shadowColor   = new Color(0f, 0f, 0f, 0.45f);
        }
        return generator.generateFont(p);
    }

    private void disposeFonts() {
        if (title   != null) title.dispose();
        if (heading != null) heading.dispose();
        if (item    != null) item.dispose();
        if (small   != null) small.dispose();
        title = heading = item = small = null;
    }

    @Override
    public void dispose() {
        disposeFonts();

        if (titleGenerator != null) { titleGenerator.dispose(); titleGenerator = null; }
        if (bodyGenerator  != null) { bodyGenerator.dispose();  bodyGenerator  = null; }
    }
}
