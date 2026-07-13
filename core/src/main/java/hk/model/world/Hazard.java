package hk.model.world;

import com.badlogic.gdx.math.Rectangle;


public class Hazard extends BoundedWorldObject {

    public final int damage;

    public Hazard(Rectangle bounds, int damage) {
        super(bounds);
        this.damage = damage;
    }
}
