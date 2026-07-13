package hk.model.world;


public class PropSpawn {

    public final String sprite;
    public final float x, y;
    public final float width, height;
    public final float parallax;
    public final boolean sway;
    public final boolean flicker;
    public final boolean foreground;

    public PropSpawn(String sprite, float x, float y, float width, float height,
                     float parallax, boolean sway, boolean flicker, boolean foreground) {
        this.sprite     = sprite;
        this.x          = x;
        this.y          = y;
        this.width      = width;
        this.height     = height;
        this.parallax   = parallax;
        this.sway       = sway;
        this.flicker    = flicker;
        this.foreground = foreground;
    }
}
