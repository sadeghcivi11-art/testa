package hk.model.enemy.concrete;

import com.badlogic.gdx.math.Vector2;
import hk.model.combat.Laser;
import hk.model.enemy.EnemyStats;
import hk.model.enemy.GroundEnemy;

public class CrystalGuardian extends GroundEnemy {

    public enum State { IDLE, LASER, ENRAGED, DYING, DEAD }

    public static final int   HP              = 12;
    public static final float VISION          = 200f;
    public static final float ENRAGE_DURATION = 7f;
    public static final float ENRAGE_SPEED    = 45f;
    public static final float LASER_DURATION  = 1.5f;
    public static final float LASER_WINDUP    = 0.25f;
    public static final float IDLE_COOLDOWN   = 0.1f;
    public static final float DYING_DURATION  = 0.5f;
    public static final float REVIVE_RANGE    = 750f;

    private static final float WIDTH  = 14f;
    private static final float HEIGHT = 20f;
    private static final float WEIGHT = 1.5f;

    public State state = State.IDLE;


    public final Facing homeFacing;
    public boolean enraged     = false;
    public float   enrageTimer = 0f;

    public final Laser laser     = new Laser();
    public float       laserTimer = 0f;

    public float idleCooldown = 0f;


    public final Vector2 lastGroundedPlayerPos = new Vector2();

    public CrystalGuardian(float x, float y) {
        super(HP, x, y, 0f, new EnemyStats(WIDTH, HEIGHT, WEIGHT, REVIVE_RANGE));
        this.homeFacing  = this.facing;
        this.visionRange = VISION;
    }
}
