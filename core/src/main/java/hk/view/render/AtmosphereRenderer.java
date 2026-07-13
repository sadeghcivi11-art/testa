package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import hk.model.world.Zone;


public class AtmosphereRenderer implements Disposable {

    private static final int   PARTICLE_COUNT = 70;
    private static final float BLEND_SECONDS  = 1.6f;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera screenCamera = new OrthographicCamera();
    private final Texture blob;

    private Zone currentZone;
    private final float[] bottomNow = new float[3];
    private final float[] topNow    = new float[3];
    private final float[] bottomTarget = new float[3];
    private final float[] topTarget    = new float[3];
    private boolean colorsSeeded = false;

    private static final class Mote {
        float x, y, vx, vy, size, alpha, phase;
        Zone.ParticleMode mode;
    }
    private final Mote[] motes = new Mote[PARTICLE_COUNT];
    private float time = 0f;


    private final com.badlogic.gdx.graphics.Color bottomColor = new com.badlogic.gdx.graphics.Color();
    private final com.badlogic.gdx.graphics.Color topColor    = new com.badlogic.gdx.graphics.Color();

    public AtmosphereRenderer() {
        int size = 32;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float c = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c, dy = (y - c) / c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = MathUtils.clamp(1f - d, 0f, 1f);
                pm.setColor(1f, 1f, 1f, a * a);
                pm.drawPixel(x, y);
            }
        }
        blob = new Texture(pm);
        blob.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();

        for (int i = 0; i < motes.length; i++) motes[i] = new Mote();
    }


    public void setZone(Zone zone, OrthographicCamera camera) {
        if (zone == null || zone == currentZone) return;
        currentZone = zone;
        System.arraycopy(zone.bottomColor, 0, bottomTarget, 0, 3);
        System.arraycopy(zone.topColor,    0, topTarget,    0, 3);
        if (!colorsSeeded) {
            System.arraycopy(bottomTarget, 0, bottomNow, 0, 3);
            System.arraycopy(topTarget,    0, topNow,    0, 3);
            colorsSeeded = true;
            for (Mote m : motes) spawnMote(m, zone.particles, camera, true);
        }

    }


    public void drawBackdrop(float delta) {
        for (int i = 0; i < 3; i++) {
            bottomNow[i] = MathUtils.lerp(bottomNow[i], bottomTarget[i],
                    Math.min(1f, delta / BLEND_SECONDS * 3f));
            topNow[i]    = MathUtils.lerp(topNow[i], topTarget[i],
                    Math.min(1f, delta / BLEND_SECONDS * 3f));
        }
        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        screenCamera.setToOrtho(false, sw, sh);
        screenCamera.update();
        shapes.setProjectionMatrix(screenCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        bottomColor.set(bottomNow[0], bottomNow[1], bottomNow[2], 1f);
        topColor.set(topNow[0], topNow[1], topNow[2], 1f);
        shapes.rect(0, 0, sw, sh, bottomColor, bottomColor, topColor, topColor);
        shapes.end();
    }


    public void drawParticles(SpriteBatch batch, OrthographicCamera camera, float delta) {
        if (currentZone == null) return;
        time += delta;

        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float left   = camera.position.x - viewW / 2f - 10f;
        float right  = camera.position.x + viewW / 2f + 10f;
        float bottom = camera.position.y - viewH / 2f - 10f;
        float top    = camera.position.y + viewH / 2f + 10f;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Mote m : motes) {
            m.x += (m.vx + MathUtils.sin(time * 0.8f + m.phase) * 2.5f) * delta;
            m.y += m.vy * delta;

            boolean out = m.x < left - 12f || m.x > right + 12f || m.y < bottom - 12f || m.y > top + 12f;
            if (out) {
                spawnMote(m, currentZone.particles, camera, false);
            }

            float flickerA = m.alpha * (0.7f + 0.3f * MathUtils.sin(time * 2.2f + m.phase * 2f));
            if (m.mode == Zone.ParticleMode.SPORES) {
                batch.setColor(0.55f, 0.95f, 0.45f, flickerA);
            } else {
                batch.setColor(0.75f, 0.78f, 0.85f, flickerA * 0.8f);
            }
            batch.draw(blob, m.x, m.y, m.size, m.size);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void spawnMote(Mote m, Zone.ParticleMode mode, OrthographicCamera camera, boolean anywhere) {
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        m.mode  = mode;
        m.phase = MathUtils.random(0f, 6.283f);
        m.x = camera.position.x + MathUtils.random(-viewW / 2f, viewW / 2f);
        if (mode == Zone.ParticleMode.SPORES) {
            m.vx = MathUtils.random(-3f, 3f);
            m.vy = MathUtils.random(3.5f, 9f);
            m.size  = MathUtils.random(0.7f, 1.8f);
            m.alpha = MathUtils.random(0.25f, 0.6f);
            m.y = anywhere ? camera.position.y + MathUtils.random(-viewH / 2f, viewH / 2f)
                           : camera.position.y - viewH / 2f - MathUtils.random(0f, 8f);
        } else {
            m.vx = MathUtils.random(-4f, 4f);
            m.vy = MathUtils.random(-6f, -1.5f);
            m.size  = MathUtils.random(0.5f, 1.4f);
            m.alpha = MathUtils.random(0.18f, 0.45f);
            m.y = anywhere ? camera.position.y + MathUtils.random(-viewH / 2f, viewH / 2f)
                           : camera.position.y + viewH / 2f + MathUtils.random(0f, 8f);
        }
    }

    @Override
    public void dispose() {
        shapes.dispose();
        blob.dispose();
    }
}
