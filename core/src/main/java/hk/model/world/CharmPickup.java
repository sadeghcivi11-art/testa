package hk.model.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hk.model.charm.CharmType;


public class CharmPickup {

    public static final float SIZE = 10f;

    public final CharmType type;
    public final Vector2 position = new Vector2();
    public boolean collected = false;

    private final Rectangle boundsRect = new Rectangle();

    public CharmPickup(CharmType type, float x, float y) {
        this.type = type;
        position.set(x, y);
    }

    public Rectangle bounds() {
        return boundsRect.set(position.x, position.y, SIZE, SIZE);
    }
}
