package hk.service;

import hk.model.player.PlayerModel;
import hk.model.world.World;


public class RespawnService {


    public void onHazardHit(PlayerModel knight, World world, int damage) {
        knight.loseMasks(damage);


        if (knight.isAlive()) {
            respawnToSafePoint(knight);
        }
    }


    public void doRespawn(PlayerModel knight, World world) {
        knight.position.set(world.spawnPoint);
        knight.velocity.set(0, 0);
        knight.fullHeal();
        knight.soul      = 0;
        knight.iframeTimer = PlayerModel.IFRAME_DURATION;
        knight.getUpTimer  = PlayerModel.GET_UP_DURATION;
    }


    private void respawnToSafePoint(PlayerModel knight) {
        knight.position.set(knight.respawnPoint);
        knight.velocity.set(0, 0);
    }
}
