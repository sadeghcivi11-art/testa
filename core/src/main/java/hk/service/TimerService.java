package hk.service;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.GroundEnemy;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.HuskHornhead;


public class TimerService {

    public void tickKnight(PlayerModel k, float delta) {
        if (k.dashTimer > 0f)           k.dashTimer           -= delta;
        if (k.dashCooldownTimer > 0f)   k.dashCooldownTimer   -= delta;
        if (k.attackTimer > 0f)         k.attackTimer         -= delta;
        if (k.attackCooldownTimer > 0f) k.attackCooldownTimer -= delta;
        if (k.iframeTimer > 0f)         k.iframeTimer         -= delta;
        if (k.hurtTimer > 0f)           k.hurtTimer           -= delta;
        if (k.pogoResetTimer > 0f)      k.pogoResetTimer      -= delta;
        if (k.downNailTimer  > 0f)      k.downNailTimer       -= delta;
        if (k.upNailTimer    > 0f)      k.upNailTimer         -= delta;
        if (k.deathTimer > 0f)          k.deathTimer          -= delta;
        if (k.getUpTimer > 0f)          k.getUpTimer          -= delta;
        if (k.knockbackTimer > 0f)      k.knockbackTimer      -= delta;
        if (k.coyoteTimer > 0f)         k.coyoteTimer         -= delta;
        if (k.jumpBufferTimer > 0f)     k.jumpBufferTimer     -= delta;
        if (k.wallJumpLockTimer > 0f)   k.wallJumpLockTimer   -= delta;
        if (k.attackTimer <= 0f)      {
            k.isAttacking = false; k.isDownAttacking = false; k.isUpAttacking = false;
            k.downNailTimer = 0f; k.upNailTimer = 0f;
        }
    }

    public void tickEnemy(EnemyModel e, float delta) {
        if (e.knockbackTimer > 0f) e.knockbackTimer -= delta;
    }

    public void tickMosscreep(hk.model.enemy.concrete.Mosscreep m, float delta) {
        tickEnemy(m, delta);
        if (m.prepTimer     > 0f) m.prepTimer     -= delta;
        if (m.diveTimer     > 0f) m.diveTimer     -= delta;
        if (m.dyingTimer    > 0f) m.dyingTimer    -= delta;
        if (m.aggroCooldown > 0f) m.aggroCooldown -= delta;
    }

    public void tickGroundEnemy(GroundEnemy e, float delta) {
        tickEnemy(e, delta);
        if (e.turnTimer  > 0f) e.turnTimer  -= delta;
        if (e.dyingTimer > 0f) e.dyingTimer -= delta;
    }

    public void tickHuskHornhead(HuskHornhead e, float delta) {
        tickGroundEnemy(e, delta);
        e.lookBehindTimer              -= delta;
        if (e.walkTimer       > 0f) e.walkTimer       -= delta;
        if (e.restTimer       > 0f) e.restTimer       -= delta;
        if (e.anticipateTimer > 0f) e.anticipateTimer -= delta;
        if (e.chargeTimer     > 0f) e.chargeTimer     -= delta;
    }

    public void tickCrystalGuardian(CrystalGuardian e, float delta) {
        tickGroundEnemy(e, delta);
        if (e.idleCooldown > 0f) e.idleCooldown -= delta;
        if (e.laserTimer   > 0f) e.laserTimer   -= delta;
        if (e.enrageTimer  > 0f) e.enrageTimer  -= delta;
    }

    public void tickBoss(FalseKnight boss, float delta) {
        tickGroundEnemy(boss, delta);
        if (boss.decisionTimer  > 0f) boss.decisionTimer  -= delta;
        if (boss.moveTimer      > 0f) boss.moveTimer      -= delta;
        if (boss.attackTimer    > 0f) boss.attackTimer    -= delta;
        if (boss.jumpCooldown   > 0f) boss.jumpCooldown   -= delta;
        if (boss.heavyHitWindow > 0f) boss.heavyHitWindow -= delta;
    }
}
