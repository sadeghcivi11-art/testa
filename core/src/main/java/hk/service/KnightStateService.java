package hk.service;

import java.util.List;

import hk.model.player.PlayerModel;


public class KnightStateService {

    private static final float STILL_VELOCITY_EPSILON = 1f;

    private static final List<StatePriorityRule> PRIORITY = List.of(
        StatePriorityRule.when(k -> !k.isAlive(),                       PlayerModel.State.DEAD),
        StatePriorityRule.when(k -> k.getUpTimer > 0f,                  PlayerModel.State.GET_UP),
        StatePriorityRule.when(k -> k.hurtTimer > 0f,                   PlayerModel.State.HURT),
        StatePriorityRule.when(k -> k.dashTimer > 0f,                   PlayerModel.State.DASH),
        StatePriorityRule.when(k -> k.isAttacking && k.isDownAttacking, PlayerModel.State.DOWN_ATTACK),
        StatePriorityRule.when(k -> k.isAttacking && k.isUpAttacking,   PlayerModel.State.UP_ATTACK),
        StatePriorityRule.when(k -> k.isAttacking,                      PlayerModel.State.ATTACK),
        StatePriorityRule.when(k -> k.isFocusing && k.focusTimer < PlayerModel.FOCUS_START_DURATION,
                                                                         PlayerModel.State.FOCUS_START),
        StatePriorityRule.when(k -> k.isFocusing,                       PlayerModel.State.FOCUS),
        StatePriorityRule.when(k -> k.focusEndTimer > 0f && k.onGround
                                        && Math.abs(k.velocity.x) <= STILL_VELOCITY_EPSILON,
                                                                         PlayerModel.State.FOCUS_END),
        StatePriorityRule.when(k -> k.isCasting && k.isScreaming,       PlayerModel.State.SCREAM),
        StatePriorityRule.when(k -> k.isCasting,                        PlayerModel.State.CAST),
        StatePriorityRule.when(k -> k.isDoubleJumping,                  PlayerModel.State.DOUBLE_JUMP),
        StatePriorityRule.when(k -> k.isWallSliding,                    PlayerModel.State.WALL_SLIDE),
        StatePriorityRule.when(k -> !k.onGround && k.velocity.y > 0,    PlayerModel.State.JUMP),
        StatePriorityRule.when(k -> !k.onGround,                        PlayerModel.State.FALL),
        StatePriorityRule.when(k -> Math.abs(k.velocity.x) > STILL_VELOCITY_EPSILON,
                                                                         PlayerModel.State.RUN)
    );

    public void update(PlayerModel knight) {
        if (knight.onGround || knight.velocity.y <= 0f) knight.isDoubleJumping = false;

        knight.state = PlayerModel.State.IDLE;
        for (StatePriorityRule rule : PRIORITY) {
            if (rule.condition().test(knight)) {
                knight.state = rule.state();
                break;
            }
        }
    }
}
