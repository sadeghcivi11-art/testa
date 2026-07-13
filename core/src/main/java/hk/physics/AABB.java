package hk.physics;

import com.badlogic.gdx.math.Rectangle;

public record AABB(float x, float y, float width, float height) {

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }
}
