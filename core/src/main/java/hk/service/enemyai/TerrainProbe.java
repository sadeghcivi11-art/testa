package hk.service.enemyai;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.GroundEnemy;

public class TerrainProbe {

    private final Rectangle probe = new Rectangle();

    public boolean isWallAhead(GroundEnemy enemy, List<Rectangle> solids) {
        boolean right = enemy.facing == Facing.RIGHT;
        float probeX  = right ? enemy.position.x + enemy.width : enemy.position.x - 1f;
        probe.set(probeX, enemy.position.y + 0.5f, 1f, enemy.height - 1f);
        for (Rectangle s : solids) {
            if (probe.overlaps(s)) return true;
        }
        return false;
    }

    public boolean isCliffAhead(GroundEnemy enemy, List<Rectangle> solids) {
        boolean right = enemy.facing == Facing.RIGHT;
        float probeX  = right ? enemy.position.x + enemy.width : enemy.position.x - 2f;
        probe.set(probeX, enemy.position.y - 2f, 2f, 2f);
        for (Rectangle s : solids) {
            if (probe.overlaps(s)) return false;
        }
        return true;
    }

    public boolean isHazardAhead(GroundEnemy enemy, List<Rectangle> hazards) {
        boolean right = enemy.facing == Facing.RIGHT;
        final float lookahead = 10f;
        float probeX  = right ? enemy.position.x + enemy.width : enemy.position.x - lookahead;
        probe.set(probeX, enemy.position.y, lookahead, enemy.height);
        for (Rectangle h : hazards) {
            if (probe.overlaps(h)) return true;
        }
        return false;
    }

    public boolean blockedAhead(GroundEnemy enemy, List<Rectangle> solids, List<Rectangle> hazards) {
        return isWallAhead(enemy, solids) || isCliffAhead(enemy, solids) || isHazardAhead(enemy, hazards);
    }
}
