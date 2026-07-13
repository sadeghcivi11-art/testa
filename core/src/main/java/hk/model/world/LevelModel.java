package hk.model.world;

import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public record LevelModel(
        List<Rectangle> solids,
        List<Rectangle> hazards,
        List<Rectangle> breakables,
        List<Rectangle> secretRooms,
        List<EnemySpawn> enemySpawns,
        List<EnemySpawn> npcSpawns,
        List<EnemySpawn> pickupSpawns,
        List<PropSpawn> propSpawns,
        EnemySpawn bossSpawn,
        float mapWidthUnits,
        float mapHeightUnits
) {
}
