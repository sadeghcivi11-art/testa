package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public final class BrightnessRenderer {


    private static final float MAX_DARKEN = 0.85f;

    private BrightnessRenderer() { }


    public static void draw(ShapeRenderer shapes, OrthographicCamera uiCamera,
                            float brightness, float screenW, float screenH) {
        float darken = (1f - brightness) * MAX_DARKEN;
        if (darken <= 0f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(uiCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, darken);
        shapes.rect(0, 0, screenW, screenH);
        shapes.end();
    }
}
