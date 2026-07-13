package hk.model.boss;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hk.model.enemy.EnemyStats;
import hk.model.enemy.GroundEnemy;
import hk.physics.CollisionWorld;

public class FalseKnight extends GroundEnemy {

    public enum State {
        IDLE,
        MACE_ANTIC,
        MACE_SLAM,
        ATTACK_RECOVER,
        RUNNING_CHARGE,
        LEAP_ANTIC,
        OFFENSIVE_LEAP,
        JUMP_ATTACK,
        POWER_SLAM,
        SHOCKWAVE_LANDING,
        DEFENSIVE_LEAP,
        LANDING,
        STUNNED,
        STUN_RECOVER,
        DEAD
    }


    public static final int   MAX_HP        = 100;
    public static final float STUN_DURATION = 3f;

    private static final float WIDTH                = 64f;
    private static final float HEIGHT               = 52f;
    private static final float DEFAULT_WEIGHT        = 1f;
    private static final float DEFAULT_REVIVE_RANGE  = 400f;


    public static final float MACE_ANTIC_DURATION     = 0.72f;
    public static final float MACE_SLAM_DURATION      = 0.36f;
    public static final float ATTACK_RECOVER_DURATION = 0.60f;
    public static final float CHARGE_DURATION          = 2.0f;
    public static final float LEAP_ANTIC_DURATION      = 0.48f;
    public static final float LEAP_MAX_DURATION        = 2.5f;
    public static final float JUMP_ATTACK_DURATION     = 0.80f;
    public static final float LEAP_VY                 = 115f;
    public static final float LEAP_FLIGHT_TIME        = 2f * LEAP_VY / Math.abs(CollisionWorld.GRAVITY);
    public static final float SHOCKWAVE_DURATION      = 1.4f;
    public static final float DEFENSIVE_LEAP_DURATION = 1.8f;
    public static final float LANDING_DURATION        = 0.50f;
    public static final float STUN_RECOVER_DURATION   = 0.6f;


    public static final float MACE_SLAM_IDLE_DURATION    = 3.0f;
    public static final float CHARGE_IDLE_DURATION       = 2.0f;
    public static final float LEAP_IDLE_DURATION         = 2.0f;
    public static final float DEFENSIVE_LEAP_IDLE_DURATION = 1.5f;


    public static final float JUMP_COOLDOWN_DURATION = 3.0f;


    public int phase = 1;


    public State currentState        = State.IDLE;
    public State lastState           = State.IDLE;
    public State lastAttack          = null;
    public float moveTimer           = 0f;
    public float decisionTimer       = 0f;
    public float pendingIdleDuration = 0f;


    public float   chargeTargetX        = 0f;
    public float   shockwaveX           = 0f;
    public float   shockwaveSpeed       = 0f;
    public boolean shockwaveActive      = false;
    public boolean shockwaveFacingRight = true;


    public boolean vulnerable    = false;
    public boolean isStunnedYet  = false;


    public boolean pendingStun   = false;


    public int   heavyHitsTaken = 0;
    public float heavyHitWindow = 0f;


    public float jumpCooldown = 0f;


    public boolean   attackActive = false;
    public Rectangle attackHitbox = new Rectangle();
    public float     attackTimer  = 0f;
    public boolean   impactFired  = false;


    public float shakeRequest = 0f;


    public Vector2 lastGroundedPlayerPos = new Vector2();



    public FalseKnight(float x, float y) {
        super(MAX_HP, x, y, 0f, new EnemyStats(WIDTH, HEIGHT, DEFAULT_WEIGHT, DEFAULT_REVIVE_RANGE));
    }

    public boolean isPhaseTwo()      { return phase == 2; }
    public boolean atStunThreshold() { return health <= MAX_HP / 2; }

    @Override
    public Rectangle getBounds() {
        float bw = 0.5f * width;
        float bh = 0.7f * height;
        return new Rectangle(position.x + (width - bw) / 2f, position.y, bw, bh);
    }
}
