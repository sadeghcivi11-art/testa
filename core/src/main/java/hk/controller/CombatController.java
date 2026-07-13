package hk.controller;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashSet;
import java.util.Set;

import hk.controller.enemy.EnemyController;
import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.combat.ArenaAttack;
import hk.model.combat.Projectile;
import hk.model.combat.WraithBurst;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.npc.Zote;
import hk.model.world.Hazard;
import hk.model.world.World;
import hk.service.CollisionService;
import hk.service.RespawnService;
import hk.service.TimeScaleService;


public class CombatController {

    private final World            world;
    private final PlayerModel           knight;
    private final CollisionService collision;
    private final RespawnService   respawn;
    private final EnemyController  enemyController;
    private final BossController   bossController;
    private final TimeScaleService timeScale;

    private final Set<EnemyModel> hitThisSwing     = new HashSet<>();
    private final Set<EnemyModel> hitThisDash      = new HashSet<>();
    private final Set<hk.model.world.BreakableWall> wallsHitThisSwing = new HashSet<>();
    private boolean          wasAttacking     = false;
    private boolean          hitBossThisSwing = false;
    private boolean          hitZoteThisSwing = false;
    private boolean          pogoedThisSwing  = false;

    private static final int BOSS_NAIL_DAMAGE       = 1;
    private static final int BOSS_VULNERABLE_DAMAGE = 3;

    public CombatController(World world, TimeScaleService timeScale,
                            EnemyController enemyController, BossController bossController) {
        this.world           = world;
        this.knight          = world.player;
        this.timeScale       = timeScale;
        this.enemyController = enemyController;
        this.bossController  = bossController;
        this.collision       = new CollisionService();
        this.respawn         = new RespawnService();
    }

    public void update(float delta) {
        if (!knight.isAlive()) return;
        float scaledDelta = timeScale.scaleDelta(delta);
        updateWraiths(scaledDelta);
        updateArenaAttacks(scaledDelta);
        checkCombat();
        checkHazards();
    }

    private void updateWraiths(float delta) {
        java.util.Iterator<WraithBurst> it = world.wraiths.iterator();
        while (it.hasNext()) {
            WraithBurst w = it.next();
            w.tickTimer -= delta;
            if (w.tickTimer > 0f) continue;

            for (EnemyModel e : world.enemies) {
                if (e.isAlive() && collision.enemyOverlaps(e, w.bounds()))
                    e.takeDamage(w.damage);
            }

            w.ticksRemaining--;
            if (w.ticksRemaining <= 0) {
                w.alive = false;
                it.remove();
            } else {
                w.tickTimer = WraithBurst.TICK_INTERVAL;
            }
        }
    }

    private void updateArenaAttacks(float delta) {
        java.util.Iterator<ArenaAttack> it = world.arenaAttacks.iterator();
        while (it.hasNext()) {
            ArenaAttack a = it.next();
            a.duration -= delta;
            if (a.duration <= 0f) { a.alive = false; it.remove(); continue; }
            if (a.velocityX != 0f) {
                a.velocityX  += a.accelerationX * delta;
                a.bounds.x   += a.velocityX * delta;
                boolean hitWall = false;
                for (Rectangle solid : world.solids) {
                    if (a.bounds.overlaps(solid)) { hitWall = true; break; }
                }
                if (hitWall) { a.alive = false; it.remove(); continue; }
            }
            if (!knight.isInvincible() && collision.overlaps(knight.bounds(), a.bounds)) {
                hurtKnight(a.damage, kbDir(a.bounds.x + a.bounds.width / 2f) * 110f, 0.40f, 90f);
            }
        }
    }

    private void checkCombat() {
        if (wasAttacking && !knight.isAttacking) {
            hitThisSwing.clear();
            wallsHitThisSwing.clear();
            hitBossThisSwing = false;
            hitZoteThisSwing = false;
            pogoedThisSwing  = false;
        }
        wasAttacking = knight.isAttacking;
        if (knight.dashTimer <= 0f) hitThisDash.clear();

        checkBossCombat();
        checkEnemyCombat();
        checkZoteCombat();
        checkBreakableWalls();
        checkSolidPogo();
    }


    private void checkBreakableWalls() {
        for (hk.model.world.BreakableWall wall : world.breakableWalls) {
            if (wall.destroyed || wallsHitThisSwing.contains(wall)) continue;

            Rectangle sword = knight.swordBounds();
            Rectangle down  = knight.downSwordBounds();
            Rectangle up    = knight.upSwordBounds();
            boolean sideHit = sword != null && collision.overlaps(sword, wall.bounds);
            boolean downHit = down  != null && collision.overlaps(down,  wall.bounds);
            boolean upHit   = up    != null && collision.overlaps(up,    wall.bounds);
            if (!sideHit && !downHit && !upHit) continue;

            wallsHitThisSwing.add(wall);
            if (downHit && !pogoedThisSwing) applyPogo();
            if (wall.hit()) {
                world.solids.remove(wall.bounds);
            }
        }
    }

    private void checkSolidPogo() {
        if (pogoedThisSwing || knight.onGround) return;
        Rectangle down = knight.downSwordBounds();
        if (down == null) return;
        for (Rectangle solid : world.solids) {
            if (collision.overlaps(down, solid)) {
                applyPogo();
                return;
            }
        }
    }

    private void checkBossCombat() {
        FalseKnight boss = world.boss;
        if (boss == null || !boss.isAlive()) return;

        if (knight.isAttacking && !hitBossThisSwing && collision.swordHits(knight, boss)) {
            hitBossThisSwing = true;
            knight.addSoul(knight.soulPerHit());

            int damage = (boss.vulnerable ? BOSS_VULNERABLE_DAMAGE : BOSS_NAIL_DAMAGE)
                    + (knight.nailDamage() - 1);
            boss.takeDamage(damage);
            bossController.notifyHit(boss);
        }

        if (!pogoedThisSwing) {
            Rectangle down = knight.downSwordBounds();
            if (down != null && collision.overlaps(down, boss.getBounds())) {
                applyPogo();
            }
        }

        if (!hitBossThisSwing) {
            Rectangle up = knight.upSwordBounds();
            if (up != null && collision.overlaps(up, boss.getBounds())) {
                hitBossThisSwing = true;
                knight.addSoul(knight.soulPerHit());
                int damage = (boss.vulnerable ? BOSS_VULNERABLE_DAMAGE : BOSS_NAIL_DAMAGE)
                        + (knight.nailDamage() - 1);
                boss.takeDamage(damage);
                bossController.notifyHit(boss);
            }
        }

        if (!bossIsHarmful(boss)) return;

        if (boss.attackActive && !knight.isInvincible()
                && collision.overlaps(knight.bounds(), boss.attackHitbox)) {
            hurtKnight(1, kbDir(boss.position.x + boss.width / 2f) * 100f, 0.32f, 80f);
        }

        if (!knight.isInvincible() && collision.enemyTouchesKnight(boss, knight)) {
            hurtKnight(1, kbDir(boss.position.x + boss.width / 2f) * 90f, 0.30f, 70f);
        }

        if (boss.shockwaveActive && !knight.isInvincible()) {
            Rectangle shockRect = new Rectangle(boss.shockwaveX - 3f, boss.position.y, 6f, boss.height);
            if (collision.overlaps(knight.bounds(), shockRect)) {
                hurtKnight(2, kbDir(boss.shockwaveX) * 110f, 0.40f, 90f);
                boss.shockwaveActive = false;
            }
        }
    }

    private boolean bossIsHarmful(FalseKnight boss) {
        return switch (boss.currentState) {
            case MACE_SLAM, RUNNING_CHARGE, JUMP_ATTACK -> true;
            default -> false;
        };
    }

    private void checkEnemyCombat() {
        for (EnemyModel e : world.enemies) {
            if (!e.isAlive()) continue;

            if (knight.isAttacking && !hitThisSwing.contains(e) && collision.swordHits(knight, e)) {
                e.takeDamage(knight.nailDamage());
                hitThisSwing.add(e);
                knight.addSoul(knight.soulPerHit());
                boolean heavy    = knight.hasCharm(hk.model.charm.CharmType.HEAVY_BLOW);
                float dir        = (e.position.x + e.width / 2f) >= knight.position.x ? 1f : -1f;
                e.knockbackVx    = dir * (knockbackMagnitude(heavy ? 220f : 120f, e) / e.weight);
                e.knockbackTimer = heavy ? 0.30f : 0.20f;
                e.velocity.y     = 20f / e.weight;
                enemyController.notifyHit(e);
            }


            if (knight.isShadowDashing() && !hitThisDash.contains(e)
                    && collision.enemyTouchesKnight(e, knight)) {
                hitThisDash.add(e);
                e.takeDamage(knight.nailDamage());
                float dir        = (e.position.x + e.width / 2f) >= knight.position.x ? 1f : -1f;
                e.knockbackVx    = dir * (knockbackMagnitude(120f, e) / e.weight);
                e.knockbackTimer = 0.20f;
                enemyController.notifyHit(e);
            }

            if (!pogoedThisSwing) {
                Rectangle down = knight.downSwordBounds();
                if (down != null && collision.overlaps(down, e.getBounds())) {
                    e.takeDamage(knight.nailDamage());
                    knight.addSoul(knight.soulPerHit());
                    applyPogo();
                }
            }

            if (!hitThisSwing.contains(e)) {
                Rectangle up = knight.upSwordBounds();
                if (up != null && collision.overlaps(up, e.getBounds())) {
                    e.takeDamage(knight.nailDamage());
                    hitThisSwing.add(e);
                    knight.addSoul(knight.soulPerHit());
                    boolean heavy = knight.hasCharm(hk.model.charm.CharmType.HEAVY_BLOW);
                    float dir     = (e.position.x + e.width / 2f) >= knight.position.x ? 1f : -1f;
                    e.knockbackVx    = dir * (knockbackMagnitude(heavy ? 220f : 120f, e) / e.weight);
                    e.knockbackTimer = heavy ? 0.30f : 0.20f;
                    enemyController.notifyHit(e);
                }
            }

            if (!knight.isInvincible() && collision.enemyTouchesKnight(e, knight)) {
                hurtKnight(1, kbDir(e.position.x + e.width / 2f) * 80f, 0.30f, 60f);
            }

            for (Projectile p : world.projectiles) {
                if (p.alive && collision.enemyOverlaps(e, p.bounds())) {
                    e.takeDamage(p.damage);
                    if (!p.piercing) p.alive = false;
                }
            }

            if (e instanceof CrystalGuardian cg) {
                if (cg.laser.active && !knight.isInvincible()
                        && collision.overlaps(knight.bounds(), cg.laser.bounds)) {
                    hurtKnight(1, kbDir(cg.position.x + cg.width / 2f) * 80f, 0.30f, 40f);
                }
            }
        }
    }

    private void checkZoteCombat() {
        Zote zote = world.zote;
        if (zote == null) return;


        float hitDir = Math.signum((zote.position.x + zote.width / 2f)
                - (knight.position.x + knight.bounds().width / 2f));

        if (knight.isAttacking && !hitZoteThisSwing && collision.swordHits(knight, zote)) {
            hitZoteThisSwing = true;
            knight.addSoul(knight.soulPerHit());
            zote.onHit(hitDir);
        }

        if (!pogoedThisSwing && !hitZoteThisSwing) {
            Rectangle down = knight.downSwordBounds();
            if (down != null && collision.overlaps(down, zote.getBounds())) {
                hitZoteThisSwing = true;
                zote.onHit(hitDir);
                applyPogo();
            }
        }

        if (!hitZoteThisSwing) {
            Rectangle up = knight.upSwordBounds();
            if (up != null && collision.overlaps(up, zote.getBounds())) {
                hitZoteThisSwing = true;
                knight.addSoul(knight.soulPerHit());
                zote.onHit(hitDir);
            }
        }
    }

    private void checkHazards() {
        Rectangle down = knight.downSwordBounds();
        for (Hazard h : world.hazards) {
            if (!pogoedThisSwing && down != null && collision.overlaps(down, h.bounds)) {
                applyPogo();
                return;
            }
            if (!knight.isInvincible() && collision.overlaps(knight.bounds(), h.bounds)) {
                respawn.onHazardHit(knight, world, h.damage);
                return;
            }
        }
    }

    private static final float KNOCKBACK_RANGE     = PlayerModel.NAIL_W;
    private static final float KNOCKBACK_NEAR_MULT = 1.4f;
    private static final float KNOCKBACK_FAR_MULT  = 0.6f;

    private float knockbackMagnitude(float base, EnemyModel e) {
        float dist = Math.abs((e.position.x + e.width / 2f)
                - (knight.position.x + PlayerModel.WIDTH / 2f));
        float proximity = MathUtils.clamp(1f - dist / KNOCKBACK_RANGE, 0f, 1f);
        float mult = KNOCKBACK_FAR_MULT + (KNOCKBACK_NEAR_MULT - KNOCKBACK_FAR_MULT) * proximity;
        return base * mult;
    }

    private void applyPogo() {
        pogoedThisSwing       = true;
        knight.velocity.y     = PlayerModel.POGO_SPEED;
        knight.pogoResetTimer = 0.1f;
        knight.refreshAirAbilities();
    }

    private void hurtKnight(int damage, float kbVx, float kbTimer, float kbVy) {
        knight.isFocusing     = false;
        knight.focusTimer     = 0f;
        knight.loseMasks(damage);
        knight.knockbackVx    = kbVx;
        knight.knockbackTimer = kbTimer;
        knight.velocity.x     = kbVx;
        knight.velocity.y     = kbVy;
    }

    private float kbDir(float sourceX) {
        return knight.position.x >= sourceX ? 1f : -1f;
    }
}
