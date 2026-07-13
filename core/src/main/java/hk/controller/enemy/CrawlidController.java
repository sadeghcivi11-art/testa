package hk.controller.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.enemy.CrawlidState;
import hk.model.enemy.CrawlidState.State;
import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.GroundEnemy;
import hk.service.TimerService;
import hk.service.enemyai.GroundEnemyAIService;


public class CrawlidController extends GroundEnemyAIService {

    @Override
    protected void tick(GroundEnemy e, TimerService timers, float delta) {
        timers.tickGroundEnemy(e, delta);
    }

    @Override
    public void update(GroundEnemy enemy, List<Rectangle> solids, List<Rectangle> hazards,
                       Vector2 knightPos, float delta) {
        if (!(enemy instanceof CrawlidState c)) return;
        switch (c.state) {
            case WALK  -> updateWalk(c, solids, hazards, delta);
            case TURN  -> updateTurn(c, delta);
            case DYING -> updateDying(c, delta);
            case DEAD  -> updateDead(c, knightPos);
        }
    }

    private void updateWalk(CrawlidState c, List<Rectangle> solids, List<Rectangle> hazards, float delta) {
        if (c.isDead()) {
            c.state      = State.DYING;
            c.dyingTimer = CrawlidState.DYING_DURATION;
            c.velocity.x = 0f;
            return;
        }
        if (terrain.blockedAhead(c, solids, hazards)) {
            c.state      = State.TURN;
            c.turnTimer  = CrawlidState.TURN_DURATION;
            c.velocity.x = 0f;
            return;
        }
        c.velocity.x = (c.facing == Facing.RIGHT ? 1f : -1f) * c.moveSpeed;
    }

    private void updateTurn(CrawlidState c, float delta) {
        c.velocity.x = 0f;
        if (c.turnTimer <= 0f) {
            c.facing = (c.facing == Facing.RIGHT) ? Facing.LEFT : Facing.RIGHT;
            c.state  = State.WALK;
        }
    }

    private void updateDying(CrawlidState c, float delta) {
        c.velocity.x = 0f;
        if (deathCycle.timerExpired(c.dyingTimer)) c.state = State.DEAD;
    }

    private void updateDead(CrawlidState c, Vector2 knightPos) {
        c.velocity.x = 0f;
        if (deathCycle.farEnoughToRevive(c.position, knightPos, c.reviveRange)) {
            c.health = c.maxHealth;
            c.state  = State.WALK;
            c.position.set(c.spawnPosition);
            c.velocity.set(0f, 0f);
        }
    }
}
