package hk.service.enemyai;

import hk.model.npc.Zote;


public class NpcAIService {

    private static final float ROLL_DURATION   = 3 * 0.10f;
    private static final float GET_UP_DURATION = 4 * 0.12f;


    public void update(Zote zote, float delta, float knightX) {
        if (zote == null) return;
        zote.stateTimer += delta;

        switch (zote.state) {
            case IDLE, TALK -> updateIdleOrTalk(zote, knightX);
            case FALL       -> updateFall(zote);
            case ROLL       -> updateRoll(zote);
            case GET_UP     -> updateGetUp(zote);
            case ATTACK     -> updateAttack(zote, delta, knightX);
        }
    }

    private void updateIdleOrTalk(Zote zote, float knightX) {
        zote.velocity.x = 0f;
        facePlayer(zote, knightX);
        if (!zote.isOnGround()) setState(zote, Zote.State.FALL);
    }

    private void updateFall(Zote zote) {
        if (zote.isOnGround()) setState(zote, Zote.State.GET_UP);
    }

    private void updateRoll(Zote zote) {
        zote.velocity.x = zote.knockdownDir * Zote.KNOCKDOWN_SPEED;
        if (zote.stateTimer >= ROLL_DURATION) setState(zote, Zote.State.GET_UP);
    }

    private void updateGetUp(Zote zote) {
        zote.velocity.x = 0f;
        if (zote.stateTimer >= GET_UP_DURATION) {
            setState(zote, zote.angerTimer > 0f ? Zote.State.ATTACK : Zote.State.IDLE);
        }
    }

    private void updateAttack(Zote zote, float delta, float knightX) {
        zote.angerTimer -= delta;
        if (zote.angerTimer <= 0f) {
            zote.velocity.x = 0f;
            setState(zote, Zote.State.IDLE);
            return;
        }
        float dx = knightX - (zote.position.x + Zote.WIDTH / 2f);
        facePlayer(zote, knightX);
        zote.velocity.x = Math.abs(dx) > Zote.CHASE_DEADZONE
                ? Math.signum(dx) * Zote.ATTACK_SPEED
                : 0f;
    }

    private void facePlayer(Zote zote, float knightX) {
        float cx = zote.position.x + Zote.WIDTH / 2f;
        zote.facing = knightX >= cx ? Zote.Facing.RIGHT : Zote.Facing.LEFT;
    }

    private void setState(Zote zote, Zote.State next) {
        zote.state      = next;
        zote.stateTimer = 0f;
    }
}
