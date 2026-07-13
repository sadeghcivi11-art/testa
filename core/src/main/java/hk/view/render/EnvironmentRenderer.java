package hk.view.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hk.model.enemy.EnemyModel;
import hk.model.world.BreakableWall;
import hk.model.world.CharmPickup;
import hk.model.world.World;


public class EnvironmentRenderer implements Disposable {

    private static final float VEIL_FADE_SECONDS = 1.2f;
    private static final float HIT_SHAKE_SECONDS = 0.18f;

    private final SpriteBatch batch;
    private final Texture wallTexture;
    private final Texture charmTexture;
    private final Texture pixel;
    private final Texture glow;


    private final Map<BreakableWall, Integer> lastHits  = new IdentityHashMap<>();
    private final Map<BreakableWall, Float>   hitShake  = new IdentityHashMap<>();


    private final Map<Rectangle, Float> veilAlpha = new IdentityHashMap<>();

    private float time = 0f;


    private static final class Particle {
        float x, y, vx, vy, size, life, maxLife;
        float r, g, b;
        boolean glow;
    }
    private final List<Particle> particles = new ArrayList<>();

    private final Map<EnemyModel, Boolean> enemyAliveLast = new IdentityHashMap<>();
    private Boolean bossAliveLast = null;

    public EnvironmentRenderer(SpriteBatch batch) {
        this.batch  = batch;
        wallTexture  = loadOrNull("Effects/break_wall.png");
        charmTexture = loadOrNull("Effects/void_heart.png");

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        glow = makeSoftBlob(64);
    }

    private Texture loadOrNull(String path) {
        try {
            Texture t = new Texture(path);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (Exception e) {
            return null;
        }
    }




    public void drawWorld(World world, float delta) {
        time += delta;
        for (BreakableWall wall : world.breakableWalls) {
            updateWallState(wall, delta);
            if (!wall.destroyed) drawWall(wall);
        }
        trackEnemyDeaths(world);
        drawPickups(world);
        drawParticles(delta);
    }

    private void trackEnemyDeaths(World world) {
        for (EnemyModel e : world.enemies) {
            Boolean wasAlive = enemyAliveLast.get(e);
            boolean alive    = e.isAlive();
            if (Boolean.TRUE.equals(wasAlive) && !alive) spawnDeathBurst(e.getBounds());
            enemyAliveLast.put(e, alive);
        }
        if (world.boss != null) {
            boolean alive = world.boss.isAlive();
            if (Boolean.TRUE.equals(bossAliveLast) && !alive) spawnDeathBurst(world.boss.getBounds());
            bossAliveLast = alive;
        }
    }

    private void updateWallState(BreakableWall wall, float delta) {
        Integer prev = lastHits.get(wall);
        int now = wall.hitsRemaining;
        if (prev == null) {
            lastHits.put(wall, now);
            return;
        }
        if (now < prev) {
            if (wall.destroyed) spawnCollapseBurst(wall.bounds);
            else                hitShake.put(wall, HIT_SHAKE_SECONDS);
        }
        lastHits.put(wall, now);

        Float shake = hitShake.get(wall);
        if (shake != null) {
            shake -= delta;
            if (shake <= 0f) hitShake.remove(wall);
            else             hitShake.put(wall, shake);
        }
    }

    private void drawWall(BreakableWall wall) {
        Rectangle b = wall.bounds;
        float ox = 0f;
        Float shake = hitShake.get(wall);
        if (shake != null) {
            ox = MathUtils.sin(shake * 90f) * 1.2f * (shake / HIT_SHAKE_SECONDS);
        }

        int damage = BreakableWall.HITS_REQUIRED - wall.hitsRemaining;
        float shade = 1f - damage * 0.12f;

        if (wallTexture != null) {
            batch.setColor(shade, shade, shade, 1f);
            batch.draw(wallTexture, b.x + ox, b.y, b.width, b.height);
        } else {
            batch.setColor(0.32f * shade, 0.30f * shade, 0.28f * shade, 1f);
            batch.draw(pixel, b.x + ox, b.y, b.width, b.height);
        }


        batch.setColor(0.06f, 0.05f, 0.05f, 0.85f);
        for (int i = 0; i < damage * 3; i++) {
            float fx = b.x + ox + b.width  * ((i * 37 % 23) / 23f);
            float fy = b.y      + b.height * ((i * 53 % 19) / 19f);
            float s  = 1.4f + (i * 29 % 5) * 0.5f;
            batch.draw(pixel, fx, fy, s, s * 0.6f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawPickups(World world) {
        for (CharmPickup p : world.pickups) {
            if (p.collected) continue;
            float bob = MathUtils.sin(time * 2.4f) * 1.6f;
            float cx = p.position.x + CharmPickup.SIZE / 2f;
            float cy = p.position.y + CharmPickup.SIZE / 2f + bob;


            float halo = CharmPickup.SIZE * (2.6f + MathUtils.sin(time * 3.1f) * 0.25f);
            batch.setColor(0.55f, 0.45f, 0.85f, 0.35f);
            batch.draw(glow, cx - halo / 2f, cy - halo / 2f, halo, halo);
            batch.setColor(1f, 1f, 1f, 1f);

            float s = CharmPickup.SIZE * 1.4f;
            if (charmTexture != null) {
                batch.draw(charmTexture, cx - s / 2f, cy - s / 2f, s, s);
            } else {
                batch.setColor(0.1f, 0.05f, 0.2f, 1f);
                batch.draw(pixel, cx - s / 2f, cy - s / 2f, s, s);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }
    }



    private void spawnCollapseBurst(Rectangle b) {

        for (int i = 0; i < 30; i++) {
            Particle p = new Particle();
            p.x = b.x + MathUtils.random(b.width);
            p.y = b.y + MathUtils.random(b.height);
            p.vx = MathUtils.random(-55f, 55f);
            p.vy = MathUtils.random(15f, 95f);
            p.size = MathUtils.random(0.8f, 2.2f);
            p.maxLife = p.life = MathUtils.random(0.5f, 1.0f);
            float t = MathUtils.random(0.75f, 1f);
            p.r = 0.42f * t; p.g = 0.34f * t; p.b = 0.26f * t;
            particles.add(p);
        }

        for (int i = 0; i < 9; i++) {
            Particle p = new Particle();
            p.x = b.x + MathUtils.random(b.width);
            p.y = b.y + MathUtils.random(b.height);
            p.vx = MathUtils.random(-35f, 35f);
            p.vy = MathUtils.random(-10f, 55f);
            p.size = MathUtils.random(2.5f, 4.5f);
            p.maxLife = p.life = MathUtils.random(0.7f, 1.2f);
            float t = MathUtils.random(0.8f, 1f);
            p.r = 0.30f * t; p.g = 0.28f * t; p.b = 0.26f * t;
            particles.add(p);
        }
    }

    private void spawnDeathBurst(Rectangle b) {
        float cx = b.x + b.width / 2f;
        float cy = b.y + b.height / 2f;
        for (int i = 0; i < 22; i++) {
            Particle p = new Particle();
            float angle = MathUtils.random(360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(30f, 90f);
            p.x  = cx;
            p.y  = cy;
            p.vx = MathUtils.cos(angle) * speed;
            p.vy = MathUtils.sin(angle) * speed;
            p.size = MathUtils.random(1.6f, 3.4f);
            p.maxLife = p.life = MathUtils.random(0.4f, 0.85f);
            float t = MathUtils.random(0.7f, 1f);
            p.r = 0.95f * t; p.g = 0.85f * t; p.b = 0.55f * t;
            p.glow = true;
            particles.add(p);
        }
    }

    private void drawParticles(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life -= delta;
            if (p.life <= 0f) { it.remove(); continue; }
            p.vy -= 220f * delta;
            p.x  += p.vx * delta;
            p.y  += p.vy * delta;
            float a = Math.min(1f, p.life / (p.maxLife * 0.4f));
            batch.setColor(p.r, p.g, p.b, a);
            if (p.glow) {
                float s = p.size * 3f;
                batch.draw(glow, p.x - s / 2f, p.y - s / 2f, s, s);
            } else {
                batch.draw(pixel, p.x, p.y, p.size, p.size);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }




    public void drawVeils(World world, float delta) {
        if (world.secretRooms.isEmpty()) return;
        batch.begin();
        for (Rectangle room : world.secretRooms) {
            boolean sealed = isRoomSealed(world, room);
            float alpha = veilAlpha.getOrDefault(room, 1f);
            alpha = sealed ? 1f : Math.max(0f, alpha - delta / VEIL_FADE_SECONDS);
            veilAlpha.put(room, alpha);
            if (alpha <= 0f) continue;

            batch.setColor(0.01f, 0.01f, 0.02f, alpha);
            batch.draw(pixel, room.x, room.y, room.width, room.height);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private boolean isRoomSealed(World world, Rectangle room) {
        boolean anyIntact = false, matchedIntact = false;
        for (BreakableWall w : world.breakableWalls) {
            if (w.destroyed) continue;
            anyIntact = true;

            if (w.bounds.x < room.x + room.width  + 4f && w.bounds.x + w.bounds.width  > room.x - 4f
             && w.bounds.y < room.y + room.height + 4f && w.bounds.y + w.bounds.height > room.y - 4f) {
                matchedIntact = true;
            }
        }

        return matchedIntact || (anyIntact && !hasMatchingWall(world, room));
    }

    private boolean hasMatchingWall(World world, Rectangle room) {
        for (BreakableWall w : world.breakableWalls) {
            if (w.bounds.x < room.x + room.width  + 4f && w.bounds.x + w.bounds.width  > room.x - 4f
             && w.bounds.y < room.y + room.height + 4f && w.bounds.y + w.bounds.height > room.y - 4f) {
                return true;
            }
        }
        return false;
    }




    private static Texture makeSoftBlob(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float c = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c, dy = (y - c) / c;
                float d = (float) Math.sqrt(dx * dx + dy * dy);
                float a = MathUtils.clamp(1f - d, 0f, 1f);
                a = a * a;
                pm.setColor(1f, 1f, 1f, a);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    @Override
    public void dispose() {
        if (wallTexture  != null) wallTexture.dispose();
        if (charmTexture != null) charmTexture.dispose();
        pixel.dispose();
        glow.dispose();
    }
}
