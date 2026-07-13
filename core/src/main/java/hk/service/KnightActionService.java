package hk.service;

import hk.model.player.PlayerModel;


public class KnightActionService {

    public void startMove(PlayerModel knight, float direction) {
        knight.velocity.x = direction * PlayerModel.MOVE_SPEED;
        knight.facing     = direction > 0 ? PlayerModel.Facing.RIGHT : PlayerModel.Facing.LEFT;
    }


    public void groundJump(PlayerModel knight) {
        knight.velocity.y      = PlayerModel.JUMP_SPEED;
        knight.jumpHeld        = true;
        knight.coyoteTimer     = 0f;
        knight.jumpBufferTimer = 0f;
    }

    public void doubleJump(PlayerModel knight) {
        if (knight.onGround || !knight.canAirJump()) return;
        knight.velocity.y = PlayerModel.DOUBLE_JUMP_SPEED;
        knight.airJumpsRemaining--;
        knight.isDoubleJumping = true;
        knight.jumpHeld        = true;
    }


    public void wallJump(PlayerModel knight) {
        knight.velocity.y        = PlayerModel.JUMP_SPEED * 0.95f;
        knight.wallJumpVx        = -knight.wallSide * PlayerModel.MOVE_SPEED * PlayerModel.WALL_JUMP_PUSH;
        knight.velocity.x        = knight.wallJumpVx;
        knight.wallJumpLockTimer = PlayerModel.WALL_JUMP_LOCK;
        knight.lastWallJumpSide  = knight.wallSide;
        knight.jumpHeld          = true;
        knight.jumpBufferTimer   = 0f;
    }

    public void startDash(PlayerModel knight) {
        if (!knight.canDash()) return;
        knight.dashTimer         = knight.dashDuration();
        knight.dashCooldownTimer = knight.dashCooldown();
        knight.velocity.y        = 0f;
        knight.velocity.x        = (knight.facing == PlayerModel.Facing.RIGHT) ? PlayerModel.DASH_SPEED : -PlayerModel.DASH_SPEED;
    }

    public void startAttack(PlayerModel knight) {
        if (!knight.canAttack()) return;
        knight.attackTimer         = knight.attackDuration();
        knight.attackCooldownTimer = knight.attackCooldown();
        knight.isAttacking         = true;
        knight.isDownAttacking     = false;
        knight.isUpAttacking       = false;
    }

    public void startDownAttack(PlayerModel knight) {
        if (!knight.canAttack()) return;
        knight.attackTimer         = PlayerModel.DOWN_ATTACK_DURATION;
        knight.attackCooldownTimer = knight.attackCooldown();
        knight.isAttacking         = true;
        knight.isDownAttacking     = true;
        knight.isUpAttacking       = false;
        knight.downNailTimer       = PlayerModel.DOWN_NAIL_DURATION;
    }

    public void startUpAttack(PlayerModel knight) {
        if (!knight.canAttack()) return;
        knight.attackTimer         = PlayerModel.UP_ATTACK_DURATION;
        knight.attackCooldownTimer = knight.attackCooldown();
        knight.isAttacking         = true;
        knight.isDownAttacking     = false;
        knight.isUpAttacking       = true;
        knight.upNailTimer         = PlayerModel.UP_NAIL_DURATION;
    }
}
