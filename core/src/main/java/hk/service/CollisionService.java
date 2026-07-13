package hk.service;

import com.badlogic.gdx.math.Rectangle;

import hk.model.player.PlayerModel;
import hk.model.enemy.EnemyModel;


public class CollisionService {


    public boolean overlaps(Rectangle a, Rectangle b) {
        return a.overlaps(b);
    }


    public boolean swordHits(PlayerModel knight, EnemyModel enemy) {
        Rectangle sword = knight.swordBounds();
        return sword != null && sword.overlaps(enemy.getBounds());
    }


    public boolean enemyTouchesKnight(EnemyModel enemy, PlayerModel knight) {
        return enemy.getBounds().overlaps(knight.bounds());
    }


    public boolean enemyOverlaps(EnemyModel enemy, Rectangle region) {
        return enemy.getBounds().overlaps(region);
    }
}
