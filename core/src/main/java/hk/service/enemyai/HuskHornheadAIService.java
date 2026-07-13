package hk.service.enemyai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.GroundEnemy;
import hk.model.enemy.concrete.HuskHornhead;
import hk.model.enemy.concrete.HuskHornhead.HuskState;
import hk.service.TimerService;


public class HuskHornheadAIService extends GroundEnemyAIService {

    private static final float LOOK_BEHIND_MIN    = 5.0f;
    private static final float LOOK_BEHIND_MAX    = 9.0f;

    private static final float VISION_BOX_HEIGHT = 60f;

    private final Rectangle visionBox = new Rectangle();

    @Override
    protected void tick(GroundEnemy e, TimerService timers, float delta) {
        timers.tickHuskHornhead((HuskHornhead) e, delta);
    }


    @Override
    public void update(GroundEnemy enemy, List<Rectangle> solids, List<Rectangle> hazards,
                       Vector2 knightPos, float delta) {
        HuskHornhead e = (HuskHornhead) enemy;
        switch (e.huskState) {
            case WALK       -> updateWalk(e, solids, hazards, knightPos, delta);
            case REST       -> updateRest(e, knightPos, delta);
            case ANTICIPATE -> updateAnticipate(e, delta);
            case CHARGE     -> updateCharge(e, solids, delta);
            case TURN       -> updateTurn(e, delta);
            case DYING      -> updateDying(e, delta);
            case DEAD       -> updateDead(e, knightPos);
        }
    }

    private void updateWalk(HuskHornhead e, List<Rectangle> solids, List<Rectangle> hazards,
                            Vector2 knightPos, float delta) {
        if (e.isDead()) { startDying(e); return; }
        if (seesPlayer(e, knightPos)) { startAnticipate(e); return; }
        if (terrain.blockedAhead(e, solids, hazards)) {
            resetLookBehind(e); startTurn(e); return;
        }
        checkLookBehind(e);
        if (e.huskState == HuskState.TURN) return;
        if (e.walkTimer <= 0f) {
            e.huskState  = HuskState.REST;
            e.restTimer  = HuskHornhead.REST_DURATION;
            e.velocity.x = 0f;
            return;
        }
        e.velocity.x = (e.facing == Facing.RIGHT ? 1f : -1f) * e.moveSpeed;
    }

    private void updateRest(HuskHornhead e, Vector2 knightPos, float delta) {
        if (e.isDead()) { startDying(e); return; }
        e.velocity.x = 0f;
        if (seesPlayer(e, knightPos)) { startAnticipate(e); return; }
        checkLookBehind(e);
        if (e.huskState == HuskState.TURN) return;
        if (e.restTimer <= 0f) {
            e.huskState  = HuskState.WALK;
            e.walkTimer  = HuskHornhead.WALK_DURATION;
        }
    }

    private void updateAnticipate(HuskHornhead e, float delta) {
        if (e.isDead()) { startDying(e); return; }
        e.velocity.x = 0f;
        if (e.anticipateTimer <= 0f) {
            e.huskState   = HuskState.CHARGE;
            e.chargeTimer = HuskHornhead.CHARGE_TIMEOUT;
            e.charging    = true;
        }
    }

    private void updateCharge(HuskHornhead e, List<Rectangle> solids, float delta) {
        if (e.isDead()) { e.charging = false; startDying(e); return; }
        if (terrain.isWallAhead(e, solids) || terrain.isCliffAhead(e, solids)) {
            e.charging = false; startTurn(e); return;
        }
        if (e.chargeTimer <= 0f) {
            e.charging = false; startTurn(e); return;
        }
        e.velocity.x = (e.facing == Facing.RIGHT ? 1f : -1f) * HuskHornhead.CHARGE_SPEED;
    }

    private void updateTurn(HuskHornhead e, float delta) {
        e.velocity.x = 0f;
        if (e.turnTimer <= 0f) {
            e.facing    = (e.facing == Facing.RIGHT) ? Facing.LEFT : Facing.RIGHT;
            e.huskState = HuskState.WALK;
            e.walkTimer = HuskHornhead.WALK_DURATION;
        }
    }

    private void updateDying(HuskHornhead e, float delta) {
        e.velocity.x = 0f;
        if (deathCycle.timerExpired(e.dyingTimer)) e.huskState = HuskState.DEAD;
    }

    private void updateDead(HuskHornhead e, Vector2 knightPos) {
        e.velocity.x = 0f;
        if (deathCycle.farEnoughToRevive(e.position, knightPos, e.reviveRange)) {
            reviveHusk(e);
        }
    }



    private void startDying(HuskHornhead e) {
        e.huskState  = HuskState.DYING;
        e.dyingTimer = HuskHornhead.DYING_DURATION;
        e.velocity.x = 0f;
    }

    private void startAnticipate(HuskHornhead e) {
        e.huskState       = HuskState.ANTICIPATE;
        e.anticipateTimer = HuskHornhead.ANTICIPATE_DURATION;
        e.velocity.x      = 0f;
    }

    private void startTurn(HuskHornhead e) {
        e.huskState  = HuskState.TURN;
        e.turnTimer  = HuskHornhead.TURN_DURATION;
        e.velocity.x = 0f;
    }

    private void reviveHusk(HuskHornhead e) {
        e.health    = e.maxHealth;
        e.huskState = HuskState.WALK;
        e.walkTimer = HuskHornhead.WALK_DURATION;
        e.charging  = false;
        e.position.set(e.spawnPosition);
        e.velocity.set(0f, 0f);
    }

    private void checkLookBehind(HuskHornhead e) {
        if (e.lookBehindTimer <= 0f) {
            resetLookBehind(e);
            startTurn(e);
        }
    }

    private void resetLookBehind(HuskHornhead e) {
        e.lookBehindTimer = MathUtils.random(LOOK_BEHIND_MIN, LOOK_BEHIND_MAX);
    }

    private boolean seesPlayer(HuskHornhead e, Vector2 knightPos) {
        boolean right = e.facing == Facing.RIGHT;
        float vx = right ? e.position.x + e.width : e.position.x - HuskHornhead.VISION;
        float vy = e.position.y + e.height / 2f - VISION_BOX_HEIGHT / 2f;
        visionBox.set(vx, vy, HuskHornhead.VISION, VISION_BOX_HEIGHT);
        return visionBox.contains(knightPos.x, knightPos.y);
    }
}
