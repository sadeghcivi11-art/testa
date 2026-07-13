package hk.model.world;

import com.badlogic.gdx.math.Rectangle;

public abstract class BoundedWorldObject {

    public final Rectangle bounds;

    protected BoundedWorldObject(Rectangle bounds) {
        this.bounds = bounds;
    }

    public float centerX() { return bounds.x + bounds.width  / 2f; }
    public float centerY() { return bounds.y + bounds.height / 2f; }
}
