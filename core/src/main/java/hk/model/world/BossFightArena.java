package hk.model.world;

import com.badlogic.gdx.math.Rectangle;


public class BossFightArena extends BoundedWorldObject {

    public boolean bossThere;

    public boolean   gatesClosed = false;
    public boolean   triggered   = false;
    public Rectangle leftGate;
    public Rectangle rightGate;
    public Rectangle ceiling;

    public BossFightArena(Rectangle bounds, boolean bossThere) {
        super(bounds);
        this.bossThere = bossThere;
    }

    public boolean isBossThere() { return bossThere; }
}
