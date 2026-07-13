package hk.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hk.model.GameData;
import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.enemy.EnemyModel;
import hk.model.progress.AchievementId;
import hk.model.world.World;


public class SessionStatsService {

    private final Map<EnemyModel, Boolean> aliveLastFrame = new HashMap<>();
    private final Set<String> requiredTypes = new HashSet<>();
    private boolean playerWasAlive = true;
    private boolean bossWasAlive = true;


    public void prime(World world, GameData data) {
        aliveLastFrame.clear();
        requiredTypes.clear();
        for (EnemyModel e : world.enemies) {
            aliveLastFrame.put(e, e.isAlive());
            requiredTypes.add(typeName(e));
        }
        if (world.boss != null) {
            requiredTypes.add(FalseKnight.class.getSimpleName());
            bossWasAlive = world.boss.currentState != FalseKnight.State.DEAD;
        }
        playerWasAlive = world.player.isAlive();
    }


    public void update(World world, GameData data, AchievementService achievements) {
        for (EnemyModel e : world.enemies) {
            Boolean wasAlive = aliveLastFrame.get(e);
            boolean alive = e.isAlive();
            if (Boolean.TRUE.equals(wasAlive) && !alive) {
                data.kills++;
                String type = typeName(e);
                if (!data.defeatedEnemyTypes.contains(type)) data.defeatedEnemyTypes.add(type);
            }
            aliveLastFrame.put(e, alive);
        }

        if (world.boss != null) {
            boolean alive = world.boss.currentState != FalseKnight.State.DEAD;
            if (bossWasAlive && !alive) {
                data.kills++;
                String type = FalseKnight.class.getSimpleName();
                if (!data.defeatedEnemyTypes.contains(type)) data.defeatedEnemyTypes.add(type);
            }
            bossWasAlive = alive;
        }

        PlayerModel player = world.player;
        if (playerWasAlive && !player.isAlive()) data.deaths++;
        playerWasAlive = player.isAlive();

        if (!requiredTypes.isEmpty() && data.defeatedEnemyTypes.containsAll(requiredTypes)) {
            achievements.unlock(AchievementId.TRUE_HUNTER);
        }
    }

    private static String typeName(EnemyModel e) {
        return e.getClass().getSimpleName();
    }
}
