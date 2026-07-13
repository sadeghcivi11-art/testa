package hk.model.enemy;


public class CrawlidState extends GroundEnemy {

    public enum State { WALK, TURN, DYING, DEAD }

    public static final int   HP           = 2;
    public static final float SPEED        = 40f;
    public static final float REVIVE_RANGE = 700f;

    private static final float WIDTH  = 12f;
    private static final float HEIGHT = 7f;
    private static final float WEIGHT = 1f;

    public static final float TURN_DURATION  = 2 * 0.10f;
    public static final float DYING_DURATION = 3 * 0.12f;

    public State state = State.WALK;

    public CrawlidState(float x, float y) {
        super(HP, x, y, SPEED, new EnemyStats(WIDTH, HEIGHT, WEIGHT, REVIVE_RANGE));
    }
}
