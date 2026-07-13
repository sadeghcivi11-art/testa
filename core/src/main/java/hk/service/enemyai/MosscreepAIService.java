package hk.service.enemyai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.player.PlayerModel;
import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.FlyingEnemy;
import hk.model.enemy.concrete.Mosscreep;
import hk.model.enemy.concrete.Mosscreep.State;
import hk.service.TimerService;


public class MosscreepAIService extends FlyingEnemyAIService {

    private static final float BOB_FREQUENCY = 3.2f;
    private static final float BOB_SPEED     = 9f;
    private static final float HOME_EPSILON  = 5f;

    @Override
    protected void tick(FlyingEnemy e, TimerService timers, float delta) {
        timers.tickMosscreep((Mosscreep) e, delta);
    }

    @Override
    public void update(FlyingEnemy enemy, List<Rectangle> solids, PlayerModel knight, float delta) {
        if (!(enemy instanceof Mosscreep m)) return;


        if (m.isDead() && m.state != State.DYING && m.state != State.DEAD) {
            m.state      = State.DYING;
            m.dyingTimer = Mosscreep.DYING_DURATION;
            m.velocity.set(0f, 0f);
            return;
        }

        switch (m.state) {
            case FLY     -> updateFly(m, knight, delta);
            case STARTLE -> updateStartle(m);
            case DIVE    -> updateDive(m);
            case RECOVER -> updateRecover(m, delta);
            case DYING   -> updateDying(m);
            case DEAD    -> updateDead(m, knight);
        }
    }

    private void updateFly(Mosscreep m, PlayerModel knight, float delta) {

        m.bobPhase += delta * BOB_FREQUENCY;
        float dir = m.facing == Facing.RIGHT ? 1f : -1f;
        if (m.position.x > m.spawnPosition.x + Mosscreep.PATROL_RANGE)  dir = -1f;
        if (m.position.x < m.spawnPosition.x - Mosscreep.PATROL_RANGE)  dir = 1f;
        m.facing     = dir > 0 ? Facing.RIGHT : Facing.LEFT;
        m.velocity.x = dir * m.moveSpeed;
        m.velocity.y = MathUtils.sin(m.bobPhase) * BOB_SPEED
                + (m.spawnPosition.y - m.position.y) * 0.6f;


        if (m.aggroCooldown <= 0f && knight.isAlive()
                && distanceToKnight(m, knight) < m.visionRange) {
            m.spottedPlayer = true;
            m.lastKnownPlayerPos.set(knight.position.x + PlayerModel.WIDTH / 2f,
                                     knight.position.y + PlayerModel.HEIGHT / 2f);
            m.preparing = true;
            m.prepTimer = Mosscreep.STARTLE_DURATION;
            m.velocity.set(0f, 0f);
            m.facing = m.lastKnownPlayerPos.x >= m.position.x + m.width / 2f
                    ? Facing.RIGHT : Facing.LEFT;
            m.state = State.STARTLE;
        }
    }

    private void updateStartle(Mosscreep m) {
        m.velocity.set(0f, 0f);
        if (m.prepTimer > 0f) return;

        m.preparing = false;
        m.diveVelocity.set(m.lastKnownPlayerPos.x - (m.position.x + m.width / 2f),
                           m.lastKnownPlayerPos.y - (m.position.y + m.height / 2f));
        if (m.diveVelocity.isZero(0.001f)) m.diveVelocity.set(m.facing == Facing.RIGHT ? 1f : -1f, 0f);
        m.diveVelocity.nor().scl(Mosscreep.DIVE_SPEED);
        m.diveTimer = Mosscreep.DIVE_MAX_TIME;
        m.velocity.set(m.diveVelocity);
        m.state     = State.DIVE;
    }

    private void updateDive(Mosscreep m) {


        boolean hitWall = (m.diveVelocity.x != 0f && m.velocity.x == 0f && m.diveTimer < Mosscreep.DIVE_MAX_TIME)
                       || (m.diveVelocity.y != 0f && m.velocity.y == 0f && m.diveTimer < Mosscreep.DIVE_MAX_TIME);
        if (hitWall || m.diveTimer <= 0f) {
            m.aggroCooldown = Mosscreep.AGGRO_COOLDOWN;
            m.spottedPlayer = false;
            m.state = State.RECOVER;
            return;
        }
        m.velocity.set(m.diveVelocity);
        m.facing = m.velocity.x >= 0f ? Facing.RIGHT : Facing.LEFT;
    }

    private void updateRecover(Mosscreep m, float delta) {

        float dx = m.spawnPosition.x - m.position.x;
        float dy = m.spawnPosition.y - m.position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < HOME_EPSILON) {
            m.state = State.FLY;
            return;
        }
        m.velocity.set(dx / dist * m.moveSpeed, dy / dist * m.moveSpeed);
        m.facing = dx >= 0f ? Facing.RIGHT : Facing.LEFT;
    }

    private void updateDying(Mosscreep m) {

        if (m.dyingTimer <= 0f && m.isOnGround()) m.state = State.DEAD;
    }

    private void updateDead(Mosscreep m, PlayerModel knight) {
        m.velocity.x = 0f;
        float dist = Vector2.dst(m.position.x, m.position.y, knight.position.x, knight.position.y);
        if (dist > m.reviveRange) {
            m.health = m.maxHealth;
            m.state  = State.FLY;
            m.position.set(m.spawnPosition);
            m.velocity.set(0f, 0f);
            m.aggroCooldown = 0f;
            m.spottedPlayer = false;
            m.preparing     = false;
        }
    }
}
