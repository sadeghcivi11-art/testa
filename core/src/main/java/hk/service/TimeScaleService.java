package hk.service;


public class TimeScaleService {

    private float scale = 1f;


    public float scaleDelta(float delta) { return delta * scale; }

    public float getScale()              { return scale; }


    public void setScale(float scale)    { this.scale = Math.max(0.01f, scale); }


    private static final float[] COMMON_PERIODS = {
            1f / 60f, 1f / 75f, 1f / 90f, 1f / 120f, 1f / 144f, 1f / 165f, 1f / 240f
    };


    public static float snapDelta(float delta) {
        for (float period : COMMON_PERIODS) {
            if (Math.abs(delta - period) < 0.00025f) return period;
        }
        return delta;
    }
}
