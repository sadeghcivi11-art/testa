package hk.controller;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.boss.FalseKnight.State;
import hk.model.combat.ArenaAttack;
import hk.physics.CollisionWorld;
import hk.service.TimerService;
import hk.service.enemyai.BossAIService;


public class BossController {

    private final BossAIService        bossAI;
    private final EntityPhysicsStepper stepper;
    private final TimerService         timers;

    public BossController(TimerService timers) {
        this.timers  = timers;
        this.bossAI  = new BossAIService();
        this.stepper = new EntityPhysicsStepper(new CollisionWorld());
    }

    public void update(FalseKnight boss, PlayerModel player, List<Rectangle> solids,
                       List<ArenaAttack> arenaAttacks, boolean engaged, float delta) {
        if (boss == null) return;
        if (boss.currentState == State.DEAD) {
            stepper.stepVerticalOnly(boss, solids, delta);
            return;
        }

        if (!boss.isAlive() && boss.currentState != State.DEAD) {
            boss.currentState = State.DEAD;
            boss.velocity.set(0, 0);
            return;
        }

        if (player.onGround)
            boss.lastGroundedPlayerPos.set(player.position.x, player.position.y);

        timers.tickBoss(boss, delta);
        bossAI.update(boss, player, solids, arenaAttacks, engaged, delta);

        stepper.stepFull(boss, solids, delta);
    }


    public void notifyHit(FalseKnight boss) {
        bossAI.notifyHit(boss);
    }
}
