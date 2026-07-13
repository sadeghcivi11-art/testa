package hk.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.cheat.CheatCode;
import hk.model.enemy.EnemyModel;
import hk.model.world.World;


public class CheatController {

    private static final int LETHAL_DAMAGE       = 9999;
    private static final int MAX_SOUL_MASK_BONUS = 2;

    private final World world;

    public CheatController(World world) {
        this.world = world;
    }

    public void update(float delta) {
        boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        if (ctrl) {
            for (CheatCode cheat : CheatCode.values()) {
                if (Gdx.input.isKeyJustPressed(cheat.triggerKey)) trigger(cheat);
            }
        }

        PlayerModel knight = world.player;
        knight.godMode = world.cheats.isActive(CheatCode.GOD_MODE);
        knight.noclip  = world.cheats.isActive(CheatCode.NOCLIP);
    }

    private void trigger(CheatCode cheat) {
        switch (cheat) {
            case INSTA_KILL:    killAllEnemies();    break;
            case BOSS_TELEPORT: teleportToBoss();     break;
            case MAX_SOUL:      refillSoulAndMasks(); break;
            case NOCLIP:
            case GOD_MODE:      world.cheats.toggle(cheat); break;
        }
    }

    private void killAllEnemies() {
        for (EnemyModel e : world.enemies) {
            if (e.isAlive()) e.takeDamage(LETHAL_DAMAGE);
        }
        if (world.boss != null && world.boss.isAlive()) {
            world.boss.takeDamage(LETHAL_DAMAGE);
        }
    }

    private void teleportToBoss() {
        FalseKnight boss = world.boss;
        if (boss == null || !boss.isAlive()) return;
        world.player.position.set(boss.position.x - PlayerModel.WIDTH * 2f, boss.position.y);
        world.player.velocity.set(0f, 0f);
    }

    private void refillSoulAndMasks() {
        PlayerModel knight = world.player;
        knight.soul      = PlayerModel.MAX_SOUL;
        knight.maxMasks += MAX_SOUL_MASK_BONUS;
        knight.masks     = knight.maxMasks;
    }
}
