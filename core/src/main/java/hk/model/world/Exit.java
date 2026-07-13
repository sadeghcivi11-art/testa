package hk.model.world;

import com.badlogic.gdx.math.Rectangle;


public class Exit extends BoundedWorldObject {

    public final String targetMap;
    public final String targetSpawn;

    public Exit(Rectangle bounds, String targetMap, String targetSpawn) {
        super(bounds);
        this.targetMap = targetMap;
        this.targetSpawn = targetSpawn;
    }
}
