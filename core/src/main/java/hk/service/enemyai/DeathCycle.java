package hk.service.enemyai;

import com.badlogic.gdx.math.Vector2;

public class DeathCycle {

    public boolean timerExpired(float dyingTimer) {
        return dyingTimer <= 0f;
    }

    public boolean farEnoughToRevive(Vector2 anchor, Vector2 other, float reviveRange) {
        return Vector2.dst(anchor.x, anchor.y, other.x, other.y) > reviveRange;
    }
}
