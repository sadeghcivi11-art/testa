package hk.service.enemyai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.player.PlayerModel;
import hk.model.combat.Laser;
import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.GroundEnemy;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.CrystalGuardian.State;
import hk.service.TimerService;

public class CrystalGuardianAIService extends GroundEnemyAIService {

    private static final float SIGHT_HALF_HEIGHT = 50f;
    private static final float FACE_DEADZONE     = 6f;

    @Override
    protected void tick(GroundEnemy e, TimerService timers, float delta) {
        timers.tickCrystalGuardian((CrystalGuardian) e, delta);
    }


    @Override
    protected Vector2 aimPos(GroundEnemy e, PlayerModel knight) {
        CrystalGuardian cg = (CrystalGuardian) e;
        if (knight.onGround) cg.lastGroundedPlayerPos.set(knight.position);
        return cg.lastGroundedPlayerPos;
    }

    @Override
    public void update(GroundEnemy enemy, List<Rectangle> solids, List<Rectangle> hazards,
                       Vector2 playerPos, float delta) {
        if (!(enemy instanceof CrystalGuardian cg)) return;

        if (!cg.isAlive() && cg.state != State.DYING && cg.state != State.DEAD) {
            enterDying(cg);
            return;
        }

        switch (cg.state) {
            case IDLE    -> updateIdle(cg, playerPos, delta);
            case LASER   -> updateLaser(cg, delta);
            case ENRAGED -> updateEnraged(cg, playerPos, solids, hazards, delta);
            case DYING   -> updateDying(cg, delta);
            case DEAD    -> updateDead(cg, playerPos);
        }
    }

    private void updateIdle(CrystalGuardian e, Vector2 playerPos, float delta) {
        e.velocity.x   = 0f;
        e.laser.active = false;

        if (e.idleCooldown > 0f) return;

        if (seesPlayer(e, playerPos)) {
            e.state = State.LASER;
            e.laserTimer   = CrystalGuardian.LASER_DURATION;
            e.laser.active = false;
        }
    }

    private void updateLaser(CrystalGuardian e, float delta) {
        e.velocity.x = 0f;

        float elapsed  = CrystalGuardian.LASER_DURATION - e.laserTimer;
        boolean firing = elapsed >= CrystalGuardian.LASER_WINDUP;
        e.laser.active = firing;
        if (firing) refreshLaserBounds(e);

        if (e.laserTimer <= 0f) {
            e.laser.active = false;
            e.state = State.ENRAGED;
            e.enraged      = true;
            e.enrageTimer  = CrystalGuardian.ENRAGE_DURATION;
        }
    }

    private void updateEnraged(CrystalGuardian e, Vector2 playerPos, List<Rectangle> solids,
                               List<Rectangle> hazards, float delta) {
        e.laser.active = false;

        if (e.enrageTimer <= 0f) {
            e.enraged      = false;
            e.velocity.x   = 0f;
            e.facing       = e.homeFacing;
            e.state = State.IDLE;
            e.idleCooldown = CrystalGuardian.IDLE_COOLDOWN;
            return;
        }

        float dx = playerPos.x - (e.position.x + e.width / 2f);
        if (Math.abs(dx) <= FACE_DEADZONE) {
            e.velocity.x = 0f;
            return;
        }

        e.facing     = dx > 0 ? Facing.RIGHT : Facing.LEFT;
        e.velocity.x = terrain.blockedAhead(e, solids, hazards)
                ? 0f
                : (e.facing == Facing.RIGHT ? 1f : -1f) * CrystalGuardian.ENRAGE_SPEED;
    }

    private void updateDying(CrystalGuardian e, float delta) {
        e.velocity.x   = 0f;
        e.laser.active = false;
        if (deathCycle.timerExpired(e.dyingTimer)) {
            e.state = State.DEAD;
        }
    }

    private void updateDead(CrystalGuardian e, Vector2 playerPos) {
        e.velocity.x   = 0f;
        e.laser.active = false;
        if (deathCycle.farEnoughToRevive(e.spawnPosition, playerPos, e.reviveRange)) {
            e.health       = e.maxHealth;
            e.position.set(e.spawnPosition);
            e.facing       = e.homeFacing;
            e.idleCooldown = 0f;
            e.state = State.IDLE;
        }
    }

    private boolean seesPlayer(CrystalGuardian e, Vector2 playerPos) {
        float cx = e.position.x + e.width / 2f;
        float dx = playerPos.x - cx;

        if (e.facing == Facing.RIGHT && dx < 0) return false;
        if (e.facing == Facing.LEFT  && dx > 0) return false;
        if (Math.abs(dx) > CrystalGuardian.VISION) return false;

        return Math.abs(playerPos.y - e.position.y) < SIGHT_HALF_HEIGHT;
    }

    private void refreshLaserBounds(CrystalGuardian e) {
        float beamY = e.position.y + e.height / 2f - Laser.HEIGHT / 2f;
        if (e.facing == Facing.RIGHT) {
            e.laser.bounds.set(e.position.x + e.width, beamY, Laser.RANGE, Laser.HEIGHT);
        } else {
            e.laser.bounds.set(e.position.x - Laser.RANGE, beamY, Laser.RANGE, Laser.HEIGHT);
        }
    }

    @Override
    public void notifyHit(GroundEnemy e) {
        if (!(e instanceof CrystalGuardian cg)) return;
        if (cg.state == CrystalGuardian.State.ENRAGED
                || cg.state == CrystalGuardian.State.DYING
                || cg.state == CrystalGuardian.State.DEAD) return;
        cg.laser.active = false;
        cg.state        = CrystalGuardian.State.ENRAGED;
        cg.enraged      = true;
        cg.enrageTimer  = CrystalGuardian.ENRAGE_DURATION;
    }

    private void enterDying(CrystalGuardian e) {
        e.state = State.DYING;
        e.dyingTimer   = CrystalGuardian.DYING_DURATION;
        e.velocity.x   = 0f;
        e.laser.active = false;
    }
}
