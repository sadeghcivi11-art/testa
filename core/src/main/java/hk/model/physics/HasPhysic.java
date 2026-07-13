package hk.model.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public interface HasPhysic {

    Vector2 getPosition();
    Vector2 getVelocity();
    Rectangle getBounds();

    boolean isOnGround();
    void setOnGround(boolean onGround);


    default boolean isAffectedByGravity() {
        return true;
    }


    default float gravityScale() {
        return 1f;
    }


    default float maxFallSpeed() {
        return -115f;
    }
}
