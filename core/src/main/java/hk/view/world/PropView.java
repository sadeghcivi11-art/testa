package hk.view.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.model.world.PropSpawn;


public class PropView implements Disposable {

    private final SpriteBatch batch;
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, TextureRegion> regions = new HashMap<>();
    private final Texture glow;
    private float time = 0f;

    public PropView(SpriteBatch batch) {
        this.batch = batch;


        int size = 64;
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
        glow = new Texture(pm);
        glow.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
    }


    public void drawBackground(List<PropSpawn> props, OrthographicCamera camera, float delta) {
        time += delta;
        for (PropSpawn p : props) {
            if (!p.foreground) drawProp(p, camera);
        }
    }


    public void drawForeground(List<PropSpawn> props, OrthographicCamera camera) {
        for (PropSpawn p : props) {
            if (p.foreground) drawProp(p, camera);
        }
    }

    private void drawProp(PropSpawn p, OrthographicCamera camera) {
        Texture tex = textureFor(p.sprite);
        if (tex == null) return;


        Vector2 parallax = ParallaxBackground.offset(camera, p);
        float x = p.x + parallax.x, y = p.y + parallax.y;


        float phase = p.x * 0.37f + p.y * 0.19f;

        float brightness = 1f;
        if (p.flicker) {
            brightness = 0.82f + 0.18f * MathUtils.sin(time * 7.3f + phase)
                               * MathUtils.sin(time * 2.1f + phase * 1.7f);


            float halo = Math.max(p.width, p.height) * (3.1f + MathUtils.sin(time * 1.6f + phase) * 0.3f);
            float cx = x + p.width / 2f, cy = y + p.height * 0.72f;
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(0.95f, 0.78f, 0.42f, 0.28f * brightness);
            batch.draw(glow, cx - halo / 2f, cy - halo / 2f, halo, halo);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        batch.setColor(brightness, brightness, brightness, 1f);
        if (p.sway) {
            float angle = MathUtils.sin(time * 1.4f + phase) * 4.5f;
            batch.draw(regions.get(p.sprite), x, y, p.width / 2f, 0f, p.width, p.height, 1f, 1f, angle);
        } else {
            batch.draw(tex, x, y, p.width, p.height);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private Texture textureFor(String path) {
        if (textures.containsKey(path)) return textures.get(path);
        Texture t = null;
        try {
            t = new Texture(path);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("PropView", "Missing prop sprite '" + path + "'");
        }
        textures.put(path, t);
        if (t != null) regions.put(path, new TextureRegion(t));
        return t;
    }

    @Override
    public void dispose() {
        for (Texture t : textures.values()) {
            if (t != null) t.dispose();
        }
        textures.clear();
        glow.dispose();
    }
}
