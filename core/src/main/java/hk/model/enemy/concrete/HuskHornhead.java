package hk.model.enemy.concrete;

import hk.model.enemy.EnemyStats;
import hk.model.enemy.GroundEnemy;


public class HuskHornhead extends GroundEnemy {

    public enum HuskState { WALK, REST, ANTICIPATE, CHARGE, TURN, DYING, DEAD }

    public static final int   HP           = 5;
    public static final float SPEED        = 30f;
    public static final float CHARGE_SPEED = 70f;
    public static final float VISION       = 220f;
    public static final float REVIVE_RANGE = 750f;

    private static final float WIDTH  = 12f;
    private static final float HEIGHT = 15f;
    private static final float WEIGHT = 2f;

    public static final float WALK_DURATION       = 3.0f;
    public static final float REST_DURATION       = 2.0f;
    public static final float ANTICIPATE_DURATION = 5 * 0.10f;
    public static final float CHARGE_TIMEOUT      = 3.0f;
    public static final float TURN_DURATION       = 2 * 0.12f;
    public static final float DYING_DURATION      = 8 * 0.12f;

    public HuskState huskState       = HuskState.WALK;
    public boolean   charging        = false;
    public float     lookBehindTimer = 6.0f;
    public float     walkTimer       = 3.0f;
    public float     restTimer       = 0f;
    public float     anticipateTimer = 0f;
    public float     chargeTimer     = 0f;

    public HuskHornhead(float x, float y) {
        super(HP, x, y, SPEED, new EnemyStats(WIDTH, HEIGHT, WEIGHT, REVIVE_RANGE));
        this.visionRange = VISION;
    }
}
