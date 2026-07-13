package hk.view.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.combat.ArenaAttack;
import hk.model.combat.Projectile;
import hk.model.combat.WraithBurst;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.world.World;


public class DebugRenderer implements Disposable {

    public static boolean DEBUG = false;

    private static final float BORDER = 0.5f;

    private final SpriteBatch batch;
    private final Texture     pixel;

    public DebugRenderer(SpriteBatch batch) {
        this.batch = batch;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fill();
        this.pixel = new Texture(pm);
        pm.dispose();
    }

    public void draw(World world) {
        if (!DEBUG) return;

        drawKnight(world.player);
        drawEnemies(world);
        drawBoss(world.boss);
        drawProjectiles(world);
    }

    private static final float R = 0.2f, G = 0.6f, B = 1f;
    private static final float SWORD_R = 1f,   SWORD_G = 1f, SWORD_B = 0f;
    private static final float DOWN_R  = 0.5f, DOWN_G  = 1f, DOWN_B  = 0f;
    private static final float UP_R    = 0f,   UP_G    = 0.8f, UP_B  = 1f;

    private void drawKnight(PlayerModel k) {
        drawBox(k.position.x, k.position.y, PlayerModel.WIDTH, PlayerModel.HEIGHT, R, G, B);
        drawSwordHitboxIfActive(k.swordBounds(), SWORD_R, SWORD_G, SWORD_B);
        drawSwordHitboxIfActive(k.downSwordBounds(), DOWN_R, DOWN_G, DOWN_B);
        drawSwordHitboxIfActive(k.upSwordBounds(), UP_R, UP_G, UP_B);
        if (!k.onGround) drawWallProbes(k);
    }

    private void drawSwordHitboxIfActive(Rectangle bounds, float r, float g, float b) {
        if (bounds != null) drawBox(bounds.x, bounds.y, bounds.width, bounds.height, r, g, b);
    }

    private void drawWallProbes(PlayerModel k) {
        Rectangle lp = k.leftWallProbe();
        Rectangle rp = k.rightWallProbe();
        drawBox(lp.x, lp.y, lp.width, lp.height, 1f, 1f, 1f);
        drawBox(rp.x, rp.y, rp.width, rp.height, 1f, 1f, 1f);
    }

    private void drawEnemies(World world) {
        for (EnemyModel e : world.enemies) {
            if (!e.isAlive()) continue;


            drawBox(e.position.x, e.position.y, e.width, e.height, 0f, 1f, 0f);


            if (e instanceof CrystalGuardian cg) {
                if (cg.laser.active) {
                    Rectangle lb = cg.laser.bounds;
                    drawBox(lb.x, lb.y, lb.width, lb.height, 1f, 0.5f, 0f);
                }
            }
        }
    }

    private void drawBoss(FalseKnight boss) {
        if (boss == null || !boss.isAlive()) return;

        Rectangle bounds = boss.getBounds();

        drawBox(bounds.x, bounds.y, bounds.width, bounds.height, 1f, 0f, 0f);


        if (boss.attackActive) {
            Rectangle a = boss.attackHitbox;
            drawBox(a.x, a.y, a.width, a.height, 1f, 0.5f, 0f);
        }


        if (boss.shockwaveActive)
            drawBox(boss.shockwaveX - 3f, boss.position.y, 6f, boss.height, 1f, 1f, 0f);
    }

    private void drawProjectiles(World world) {

        for (Projectile p : world.projectiles) {
            if (p.alive)
                drawBox(p.position.x, p.position.y, p.width, p.height, 0f, 1f, 1f);
        }


        for (WraithBurst w : world.wraiths) {
            if (w.alive)
                drawBox(w.position.x, w.position.y, WraithBurst.WIDTH, WraithBurst.HEIGHT, 1f, 0f, 1f);
        }


        for (ArenaAttack a : world.arenaAttacks) {
            if (a.alive)
                drawBox(a.bounds.x, a.bounds.y, a.bounds.width, a.bounds.height, 1f, 0.5f, 0f);
        }
    }

    private void drawBox(float x, float y, float w, float h, float r, float g, float b) {
        batch.setColor(r, g, b, 0.8f);
        batch.draw(pixel, x,               y,               w,      BORDER);
        batch.draw(pixel, x,               y + h - BORDER,  w,      BORDER);
        batch.draw(pixel, x,               y,               BORDER, h);
        batch.draw(pixel, x + w - BORDER,  y,               BORDER, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void dispose() {
        pixel.dispose();
    }
}
