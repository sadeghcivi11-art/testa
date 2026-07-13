package hk.view.helper;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;

import hk.model.boss.FalseKnight;
import hk.model.world.World;


public class CameraDirector {

    private static final float FOLLOW      = 10f;
    private static final float SHAKE_DECAY = 6f;

    private final OrthographicCamera camera;
    private final Viewport           viewport;

    private float baseX, baseY;
    private float shakeMag = 0f;

    public CameraDirector(OrthographicCamera camera, Viewport viewport) {
        this.camera   = camera;
        this.viewport = viewport;
        this.baseX    = camera.position.x;
        this.baseY    = camera.position.y;
    }

    public void update(World world, float delta) {
        if (world.isBossCameraLocked()) {

            Rectangle a = world.bossFightArena.bounds;
            baseX = a.x + a.width  / 2f;
            baseY = a.y + a.height / 2f;
        } else {
            float t = Math.min(1f, delta * FOLLOW);
            baseX = MathUtils.lerp(baseX, world.getCameraTargetX(), t);
            baseY = MathUtils.lerp(baseY, world.getCameraTargetY(), t);

            if (world.mapWidth > 0f && world.mapHeight > 0f) {
                float halfW = viewport.getWorldWidth()  / 2f;
                float halfH = viewport.getWorldHeight() / 2f;
                baseX = clampAxis(baseX, 0f, world.mapWidth,  halfW);
                baseY = clampAxis(baseY, 0f, world.mapHeight, halfH);
            }
        }

        float ox = 0f, oy = 0f;
        FalseKnight boss = world.boss;
        if (boss != null) {
            if (boss.shakeRequest > shakeMag) shakeMag = boss.shakeRequest;
            boss.shakeRequest = 0f;
        }
        if (shakeMag > 0.05f) {
            ox = MathUtils.random(-shakeMag, shakeMag);
            oy = MathUtils.random(-shakeMag, shakeMag);
            shakeMag *= 1f - Math.min(1f, delta * SHAKE_DECAY);
            if (shakeMag < 0.05f) shakeMag = 0f;
        }



        float x = baseX + ox, y = baseY + oy;
        int pw = com.badlogic.gdx.Gdx.graphics.getWidth();
        if (pw > 0) {
            float unitsPerPixel = viewport.getWorldWidth() / pw;
            x = Math.round(x / unitsPerPixel) * unitsPerPixel;
            y = Math.round(y / unitsPerPixel) * unitsPerPixel;
        }
        camera.position.set(x, y, 0f);
        camera.update();
    }


    private float clampAxis(float c, float min, float max, float half) {
        if (max - min <= half * 2f) return (min + max) / 2f;
        return MathUtils.clamp(c, min + half, max - half);
    }
}
