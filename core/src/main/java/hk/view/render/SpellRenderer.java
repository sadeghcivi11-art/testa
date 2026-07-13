package hk.view.render;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import hk.model.combat.Projectile;
import hk.service.AssetService;


public class SpellRenderer implements Disposable {

    private static final float VS_W = 30f;
    private static final float VS_H = 14f;


    private static final float SHADOW_W = 45f;
    private static final float SHADOW_H = 14f;

    private final SpriteBatch batch;
    private final Animation<TextureRegion> vsAnim;
    private final Animation<TextureRegion> shadowAnim;

    private final Map<Projectile, Float> projectileTimes = new IdentityHashMap<>();

    public SpellRenderer(SpriteBatch batch, AssetService loader) {
        this.batch      = batch;
        this.vsAnim     = loader.load("Projectiles/SoulBall.png",   4, 0.10f, PlayMode.LOOP_PINGPONG);
        this.shadowAnim = loader.load("Projectiles/ShadowBall.png", 6, 0.10f, PlayMode.LOOP_PINGPONG);
    }

    public void draw(List<Projectile> projectiles, float delta) {
        for (Projectile p : projectiles) {
            float t = projectileTimes.getOrDefault(p, 0f) + delta;
            projectileTimes.put(p, t);

            TextureRegion frame = (p.voidArt ? shadowAnim : vsAnim).getKeyFrame(t, false);
            float ew = p.voidArt ? SHADOW_W : VS_W;
            float eh = p.voidArt ? SHADOW_H : VS_H;
            float cx = p.position.x + p.width  / 2f;
            float cy = p.position.y + p.height / 2f;
            float x  = cx - ew / 2f;
            float y  = cy - eh / 2f;
            boolean flip = p.velocity.x < 0;
            batch.draw(frame, flip ? x + ew : x, y, flip ? -ew : ew, eh);
        }

        projectileTimes.keySet().retainAll(projectiles);
    }

    @Override
    public void dispose() { }
}
