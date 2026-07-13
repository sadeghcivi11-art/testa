package hk.model.enemy;


public abstract class FlyingEnemy extends EnemyModel {

    public float moveSpeed;


    public boolean preparing = false;
    public float   prepTimer = 0f;

    public boolean enraged = false;

    protected FlyingEnemy(int health, float x, float y, float moveSpeed, float visionRange, EnemyStats stats) {
        super(health, x, y, stats);
        this.moveSpeed   = moveSpeed;
        this.visionRange = visionRange;
    }
}
