package hk.model.world;

import com.badlogic.gdx.math.Rectangle;


public class Zone {


    public enum ParticleMode { DUST, SPORES }

    public final String name;
    public final Rectangle bounds;
    public final ParticleMode particles;


    public final float[] bottomColor;
    public final float[] topColor;

    public Zone(String name, Rectangle bounds, ParticleMode particles,
                float[] bottomColor, float[] topColor) {
        this.name        = name;
        this.bounds      = bounds;
        this.particles   = particles;
        this.bottomColor = bottomColor;
        this.topColor    = topColor;
    }
}
