package hk.model.enemy;


public abstract class GroundEnemy extends EnemyModel {

    public float moveSpeed;
    public float turnTimer  = 0f;
    public float dyingTimer = 0f;

    protected GroundEnemy(int health, float x, float y, float moveSpeed, EnemyStats stats) {
        super(health, x, y, stats);
        this.moveSpeed = moveSpeed;
    }
}
