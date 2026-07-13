package hk.service.enemyai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.player.PlayerModel;
import hk.model.enemy.GroundEnemy;
import hk.service.TimerService;


public abstract class GroundEnemyAIService {

    protected final TerrainProbe terrain    = new TerrainProbe();
    protected final DeathCycle   deathCycle = new DeathCycle();


    public final void step(GroundEnemy e, List<Rectangle> solids, List<Rectangle> hazards,
                           PlayerModel knight, TimerService timers, float delta) {
        tick(e, timers, delta);
        update(e, solids, hazards, aimPos(e, knight), delta);
    }


    protected abstract void tick(GroundEnemy e, TimerService timers, float delta);


    protected Vector2 aimPos(GroundEnemy e, PlayerModel knight) {
        return knight.position;
    }


    public abstract void update(GroundEnemy enemy, List<Rectangle> solids, List<Rectangle> hazards,
                                Vector2 knightPos, float delta);


    public void notifyHit(GroundEnemy e) { }
}
