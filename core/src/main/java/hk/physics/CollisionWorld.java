package hk.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.physics.HasPhysic;


public class CollisionWorld {

    public static final float GRAVITY  = -225f;
    public static final float MAX_FALL = -115f;


    public void applyGravity(HasPhysic entity, float scaledDelta) {
        if (!entity.isAffectedByGravity()) return;
        Vector2 v = entity.getVelocity();
        v.y += GRAVITY * entity.gravityScale() * scaledDelta;
        float maxFall = entity.maxFallSpeed();
        if (v.y < maxFall) v.y = maxFall;
    }


    public void moveX(HasPhysic entity, float delta) {
        entity.getPosition().x += entity.getVelocity().x * delta;
    }


    public void moveY(HasPhysic entity, float delta) {
        entity.getPosition().y += entity.getVelocity().y * delta;
    }


    public void resolveX(HasPhysic entity, List<Rectangle> solids) {
        Rectangle b = entity.getBounds();
        for (Rectangle s : solids) {
            if (!b.overlaps(s)) continue;

            float offsetX = b.x - entity.getPosition().x;
            if (entity.getVelocity().x > 0)      entity.getPosition().x = s.x - offsetX - b.width;
            else if (entity.getVelocity().x < 0) entity.getPosition().x = s.x + s.width - offsetX;
            entity.getVelocity().x = 0f;
            b = entity.getBounds();
        }
    }


    public void resolveCeiling(HasPhysic entity, List<Rectangle> ceilings) {
        if (entity.getVelocity().y <= 0f) return;
        Rectangle b = entity.getBounds();
        for (Rectangle s : ceilings) {
            if (!b.overlaps(s)) continue;
            entity.getPosition().y = s.y - b.height;
            entity.getVelocity().y = 0f;
            b = entity.getBounds();
        }
    }


    public boolean touchesWall(Rectangle probe, List<Rectangle> solids) {
        for (Rectangle s : solids) {
            if (probe.overlaps(s)) return true;
        }
        return false;
    }


    public void resolveY(HasPhysic entity, List<Rectangle> solids) {
        entity.setOnGround(false);
        Rectangle b = entity.getBounds();
        for (Rectangle s : solids) {
            if (!b.overlaps(s)) continue;
            if (entity.getVelocity().y > 0) {
                entity.getPosition().y = s.y - b.height;
            } else if (entity.getVelocity().y < 0) {
                entity.getPosition().y = s.y + s.height;
                entity.setOnGround(true);
            }
            entity.getVelocity().y = 0f;
            b = entity.getBounds();
        }
    }
}
