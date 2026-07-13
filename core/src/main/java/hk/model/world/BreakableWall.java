package hk.model.world;

import com.badlogic.gdx.math.Rectangle;


public class BreakableWall extends BoundedWorldObject {

    public static final int HITS_REQUIRED = 3;

    public int hitsRemaining = HITS_REQUIRED;
    public boolean destroyed = false;

    public BreakableWall(Rectangle bounds) {
        super(bounds);
    }


    public boolean hit() {
        if (destroyed) return false;
        if (--hitsRemaining <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }
}
