package hk.service.enemyai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.boss.FalseKnight.State;
import hk.model.combat.ArenaAttack;
import hk.model.enemy.EnemyModel.Facing;
import hk.physics.CollisionWorld;

public class BossAIService {


    public static final float CHARGE_SPEED         = 150f;
    public static final float DEFENSIVE_LEAP_VX    = 150f;
    public static final float DEFENSIVE_LEAP_VY    = 115f;


    public static final float CLOSE_RANGE = 90f;
    public static final float FAR_RANGE   = 220f;


    public static final float MACE_HITBOX_W = 20f;


    private static final float SHOCKWAVE_INITIAL_SPEED = 30f;
    private static final float SHOCKWAVE_ACCEL         = 120f;
    private static final float SHOCKWAVE_DURATION      = 4.0f;
    private static final float SHOCKWAVE_W        = 6f;
    private static final float SHOCKWAVE_H        = 10f;
    private static final int   SHOCKWAVE_DAMAGE   = 2;


    private static final int   HEAVY_HIT_THRESHOLD       = 3;
    private static final float HEAVY_HIT_WINDOW_DURATION = 6.0f;

    private static final float PHASE2_SPEED_MULT = 1.3f;
    private static final float PHASE2_ANTIC_MULT  = 0.7f;



    public void update(FalseKnight boss, PlayerModel player, List<Rectangle> solids,
                       List<ArenaAttack> arenaAttacks, boolean engaged, float delta) {
        checkStun(boss);
        switch (boss.currentState) {
            case IDLE           -> updateIdle(boss, player, engaged);
            case MACE_ANTIC      -> updateMaceAntic(boss, player);
            case MACE_SLAM       -> updateMaceSlam(boss);
            case ATTACK_RECOVER  -> updateAttackRecover(boss);
            case RUNNING_CHARGE  -> updateRunningCharge(boss, solids);
            case LEAP_ANTIC      -> updateLeapAntic(boss, player);
            case OFFENSIVE_LEAP  -> updateOffensiveLeap(boss, arenaAttacks);
            case JUMP_ATTACK     -> updateJumpAttack(boss);
            case DEFENSIVE_LEAP  -> updateDefensiveLeap(boss, player, solids, delta);
            case LANDING         -> updateLanding(boss);
            case STUNNED         -> updateStunned(boss, player, solids, delta);
            case STUN_RECOVER    -> updateStunRecover(boss, player, solids, delta);
            case DEAD            -> { }
            default              -> { }
        }
        boss.lastState = boss.currentState;
    }





    private void updateIdle(FalseKnight boss, PlayerModel player, boolean engaged) {
        if (!engaged || boss.decisionTimer > 0f) return;
        pickAndEnterNextMove(boss, player);
    }


    private void pickAndEnterNextMove(FalseKnight boss, PlayerModel player) {

        if (boss.heavyHitsTaken >= HEAVY_HIT_THRESHOLD && boss.jumpCooldown <= 0f) {
            enterDefensiveLeap(boss, player);
            return;
        }

        float dist = Math.abs(player.position.x - (boss.position.x + boss.width / 2f));

        EnumMap<State, Float> weights = new EnumMap<>(State.class);
        if (dist <= FAR_RANGE) {


            float maceWeight = MathUtils.clamp(1f - dist / FAR_RANGE, 0.15f, 0.75f);
            weights.put(State.MACE_ANTIC, maceWeight);
            weights.put(State.LEAP_ANTIC, 0.9f - maceWeight);
            if (dist > CLOSE_RANGE) weights.put(State.RUNNING_CHARGE, 0.25f);
        } else {


            weights.put(State.RUNNING_CHARGE, 0.75f);
            weights.put(State.LEAP_ANTIC,     0.25f);
        }


        if (weights.size() > 1) weights.remove(boss.lastAttack);

        State chosen = weightedRandom(weights);
        switch (chosen) {
            case MACE_ANTIC     -> enterMaceAntic(boss, player);
            case RUNNING_CHARGE -> enterRunningCharge(boss, player);
            case LEAP_ANTIC     -> enterLeapAntic(boss, player);
            default             -> { }
        }
    }

    private State weightedRandom(EnumMap<State, Float> weights) {
        float total = 0f;
        for (float w : weights.values()) total += w;
        float r = MathUtils.random(total);
        for (Map.Entry<State, Float> entry : weights.entrySet()) {
            r -= entry.getValue();
            if (r <= 0f) return entry.getKey();
        }
        return weights.keySet().iterator().next();
    }





    private void updateMaceAntic(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        if (boss.moveTimer <= 0f) enterMaceSlam(boss);
    }

    private void enterMaceAntic(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        boss.lastAttack   = State.MACE_ANTIC;
        boss.currentState = State.MACE_ANTIC;
        boss.moveTimer    = boss.isPhaseTwo()
                ? FalseKnight.MACE_ANTIC_DURATION * PHASE2_ANTIC_MULT
                : FalseKnight.MACE_ANTIC_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
    }

    private void updateMaceSlam(FalseKnight boss) {
        updateMaceHitbox(boss);
        requestShake(boss, 8f);
        if (boss.moveTimer <= 0f) {
            boss.attackActive = false;
            enterAttackRecover(boss, FalseKnight.MACE_SLAM_IDLE_DURATION);
        }
    }

    private void updateMaceHitbox(FalseKnight boss) {
        boss.attackActive = true;
        float maceX = boss.facing == Facing.RIGHT
                ? boss.position.x + boss.width
                : boss.position.x - MACE_HITBOX_W;
        boss.attackHitbox.set(maceX, boss.position.y, MACE_HITBOX_W, boss.height);
    }

    private void enterMaceSlam(FalseKnight boss) {
        boss.currentState = State.MACE_SLAM;
        boss.moveTimer    = FalseKnight.MACE_SLAM_DURATION;
        boss.impactFired  = false;
        boss.vulnerable   = false;
        boss.attackActive = false;
    }





    private void enterRunningCharge(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        boss.lastAttack    = State.RUNNING_CHARGE;
        boss.currentState  = State.RUNNING_CHARGE;
        boss.moveTimer     = FalseKnight.CHARGE_DURATION;
        boss.chargeTargetX = player.position.x + PlayerModel.WIDTH / 2f;
        boss.vulnerable    = false;
        boss.attackActive  = false;
    }

    private void updateRunningCharge(FalseKnight boss, List<Rectangle> solids) {
        boolean facingRight = boss.facing == Facing.RIGHT;
        float   bossCenter  = boss.position.x + boss.width / 2f;

        requestShake(boss, 2f);

        boolean reachedTarget = facingRight
                ? bossCenter >= boss.chargeTargetX
                : bossCenter <= boss.chargeTargetX;



        if (reachedTarget || boss.moveTimer <= 0f || wallAhead(boss, solids)) {
            boss.velocity.x   = 0f;
            boss.attackActive = false;
            enterAttackRecover(boss, FalseKnight.CHARGE_IDLE_DURATION);
            return;
        }

        float speed = CHARGE_SPEED * (boss.isPhaseTwo() ? PHASE2_SPEED_MULT : 1f);
        boss.velocity.x = facingRight ? speed : -speed;
        updateChargeHitbox(boss);
    }

    private final Rectangle wallProbe = new Rectangle();

    private boolean wallAhead(FalseKnight boss, List<Rectangle> solids) {
        boolean right  = boss.facing == Facing.RIGHT;
        float   probeX = right ? boss.position.x + boss.width : boss.position.x - 1f;
        wallProbe.set(probeX, boss.position.y + 0.5f, 1f, boss.height - 1f);
        for (Rectangle s : solids) {
            if (wallProbe.overlaps(s)) return true;
        }
        return false;
    }

    private void updateChargeHitbox(FalseKnight boss) {
        boss.attackActive = true;
        Rectangle b = boss.getBounds();
        boss.attackHitbox.set(b.x, b.y, b.width, b.height);
    }





    private void updateAttackRecover(FalseKnight boss) {
        if (boss.moveTimer <= 0f) enterIdle(boss);
    }

    private void enterAttackRecover(FalseKnight boss, float idleDuration) {
        boss.currentState        = State.ATTACK_RECOVER;
        boss.moveTimer           = FalseKnight.ATTACK_RECOVER_DURATION;
        boss.pendingIdleDuration = idleDuration;
        boss.vulnerable          = true;
        boss.attackActive        = false;
    }

    private void enterIdle(FalseKnight boss) {
        boss.currentState  = State.IDLE;
        float idle         = boss.isPhaseTwo() ? boss.pendingIdleDuration * 0.5f : boss.pendingIdleDuration;
        boss.decisionTimer = idle;
        boss.vulnerable    = true;
        boss.attackActive  = false;
    }



    private void facePlayer(FalseKnight boss, PlayerModel player) {
        boss.facing = player.position.x > boss.position.x ? Facing.RIGHT : Facing.LEFT;
    }





    private void enterLeapAntic(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        boss.lastAttack   = State.LEAP_ANTIC;
        boss.currentState = State.LEAP_ANTIC;
        boss.moveTimer    = boss.isPhaseTwo()
                ? FalseKnight.LEAP_ANTIC_DURATION * PHASE2_ANTIC_MULT
                : FalseKnight.LEAP_ANTIC_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
        boss.velocity.x   = 0f;
    }

    private void updateLeapAntic(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        if (boss.moveTimer <= 0f) enterOffensiveLeap(boss, player);
    }

    private void enterOffensiveLeap(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        boss.currentState = State.OFFENSIVE_LEAP;
        boss.moveTimer    = FalseKnight.LEAP_MAX_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
        boss.velocity.y   = FalseKnight.LEAP_VY;


        float g           = Math.abs(CollisionWorld.GRAVITY);
        float targetX     = boss.lastGroundedPlayerPos.x + PlayerModel.WIDTH / 2f;
        float bossCenterX = boss.position.x + boss.width / 2f;
        float r           = targetX - bossCenterX;
        boss.velocity.x   = g * r / (2f * FalseKnight.LEAP_VY);
    }

    private void updateOffensiveLeap(FalseKnight boss, List<ArenaAttack> arenaAttacks) {

        if (boss.isOnGround() || boss.moveTimer <= 0f) {
            boss.velocity.x = 0f;
            enterJumpAttack(boss, arenaAttacks);
        }
    }

    private void enterJumpAttack(FalseKnight boss, List<ArenaAttack> arenaAttacks) {
        boss.currentState = State.JUMP_ATTACK;
        boss.moveTimer    = FalseKnight.JUMP_ATTACK_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
        requestShake(boss, 8f);

        if (boss.isPhaseTwo()) spawnShockwaves(boss, arenaAttacks);
    }

    private void spawnShockwaves(FalseKnight boss, List<ArenaAttack> arenaAttacks) {
        float bossCenter = boss.position.x + boss.width / 2f;
        float y          = boss.position.y + 2f;
        arenaAttacks.add(new ArenaAttack(bossCenter,               y, SHOCKWAVE_W, SHOCKWAVE_H, SHOCKWAVE_DAMAGE, SHOCKWAVE_DURATION,  SHOCKWAVE_INITIAL_SPEED,  SHOCKWAVE_ACCEL));
        arenaAttacks.add(new ArenaAttack(bossCenter - SHOCKWAVE_W, y, SHOCKWAVE_W, SHOCKWAVE_H, SHOCKWAVE_DAMAGE, SHOCKWAVE_DURATION, -SHOCKWAVE_INITIAL_SPEED, -SHOCKWAVE_ACCEL));
    }

    private void updateJumpAttack(FalseKnight boss) {

        requestShake(boss, 10f);
        if (boss.moveTimer <= FalseKnight.JUMP_ATTACK_DURATION / 3f) {
            boss.attackActive = true;
            float maceX = boss.facing == Facing.RIGHT
                    ? boss.position.x + boss.width
                    : boss.position.x - MACE_HITBOX_W;
            boss.attackHitbox.set(maceX, boss.position.y, MACE_HITBOX_W, boss.height);
        }

        if (boss.moveTimer <= 0f) {
            boss.attackActive = false;
            enterAttackRecover(boss, FalseKnight.LEAP_IDLE_DURATION);
        }
    }





    private void enterDefensiveLeap(FalseKnight boss, PlayerModel player) {
        facePlayer(boss, player);
        boss.lastAttack   = State.DEFENSIVE_LEAP;
        boss.currentState = State.DEFENSIVE_LEAP;
        boss.moveTimer    = FalseKnight.DEFENSIVE_LEAP_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
        boss.jumpCooldown = FalseKnight.JUMP_COOLDOWN_DURATION;
        boss.heavyHitsTaken = 0;

        float speedMult = boss.isPhaseTwo() ? PHASE2_SPEED_MULT : 1f;
        boss.velocity.x = (boss.facing == Facing.RIGHT ? -DEFENSIVE_LEAP_VX : DEFENSIVE_LEAP_VX) * speedMult;
        boss.velocity.y = DEFENSIVE_LEAP_VY * speedMult;
    }

    private void updateDefensiveLeap(FalseKnight boss, PlayerModel player, List<Rectangle> solids, float delta) {
        if (boss.isOnGround() || boss.moveTimer <= 0f) {
            boss.velocity.x = 0f;
            boss.pendingIdleDuration = FalseKnight.DEFENSIVE_LEAP_IDLE_DURATION;
            enterLanding(boss);
        }
    }

    private void enterLanding(FalseKnight boss) {
        boss.currentState = State.LANDING;
        boss.moveTimer    = FalseKnight.LANDING_DURATION;
        boss.vulnerable   = true;
        boss.attackActive = false;
    }

    private void updateLanding(FalseKnight boss) {
        if (boss.moveTimer <= 0f) enterIdle(boss);
    }





    private void checkStun(FalseKnight boss) {
        if (boss.isStunnedYet) return;
        if (boss.currentState == State.STUNNED
                || boss.currentState == State.STUN_RECOVER
                || boss.currentState == State.DEAD) return;

        if (boss.health <= FalseKnight.MAX_HP / 2) boss.pendingStun = true;



        if (boss.pendingStun && boss.isOnGround() && isSafeToInterrupt(boss)) {
            enterStunned(boss);
        }
    }

    private boolean isSafeToInterrupt(FalseKnight boss) {
        return switch (boss.currentState) {
            case IDLE, ATTACK_RECOVER, LANDING -> true;
            default -> false;
        };
    }

    private void enterStunned(FalseKnight boss) {
        boss.isStunnedYet = true;
        boss.pendingStun  = false;
        boss.currentState = State.STUNNED;
        boss.moveTimer    = FalseKnight.STUN_DURATION;
        boss.vulnerable   = true;
        boss.attackActive = false;
        boss.velocity.set(0f, 0f);
    }

    private void updateStunned(FalseKnight boss, PlayerModel player, List<Rectangle> solids, float delta) {
        if (boss.moveTimer <= 0f) enterStunRecover(boss);
    }

    private void enterStunRecover(FalseKnight boss) {
        boss.currentState = State.STUN_RECOVER;
        boss.moveTimer    = FalseKnight.STUN_RECOVER_DURATION;
        boss.vulnerable   = false;
        boss.attackActive = false;
    }

    private void updateStunRecover(FalseKnight boss, PlayerModel player, List<Rectangle> solids, float delta) {
        if (boss.moveTimer <= 0f) {
            boss.phase               = 2;
            boss.pendingIdleDuration = FalseKnight.MACE_SLAM_IDLE_DURATION;
            enterIdle(boss);
        }
    }



    public void notifyHit(FalseKnight boss) {
        if (boss.heavyHitWindow <= 0f) boss.heavyHitsTaken = 0;
        boss.heavyHitsTaken++;
        boss.heavyHitWindow = HEAVY_HIT_WINDOW_DURATION;
    }

    protected void requestShake(FalseKnight boss, float intensity) {
        if (intensity > boss.shakeRequest) boss.shakeRequest = intensity;
    }
}
