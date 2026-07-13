package hk.controller;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;

import hk.model.physics.HasPhysic;
import hk.physics.CollisionWorld;

public class EntityPhysicsStepper {

    private final CollisionWorld physics;

    public EntityPhysicsStepper(CollisionWorld physics) {
        this.physics = physics;
    }

    public void stepFull(HasPhysic e, List<Rectangle> solids, float delta) {
        physics.applyGravity(e, delta);
        physics.moveX(e, delta);
        physics.resolveX(e, solids);
        physics.moveY(e, delta);
        physics.resolveY(e, solids);
    }

    public void stepVerticalOnly(HasPhysic e, List<Rectangle> solids, float delta) {
        physics.applyGravity(e, delta);
        physics.moveY(e, delta);
        physics.resolveY(e, solids);
    }
}
