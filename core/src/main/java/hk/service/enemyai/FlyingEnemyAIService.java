package hk.service.enemyai;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import hk.model.player.PlayerModel;
import hk.model.enemy.FlyingEnemy;
import hk.service.TimerService;


public abstract class FlyingEnemyAIService {


    public final void step(FlyingEnemy e, List<Rectangle> solids, PlayerModel knight,
                           TimerService timers, float delta) {
        tick(e, timers, delta);
        update(e, solids, knight, delta);
    }


    protected abstract void tick(FlyingEnemy e, TimerService timers, float delta);


    public abstract void update(FlyingEnemy e, List<Rectangle> solids, PlayerModel knight, float delta);


    protected float distanceToKnight(FlyingEnemy e, PlayerModel knight) {
        return Vector2.dst(e.position.x + e.width / 2f, e.position.y + e.height / 2f,
                knight.position.x + PlayerModel.WIDTH / 2f, knight.position.y + PlayerModel.HEIGHT / 2f);
    }
}
