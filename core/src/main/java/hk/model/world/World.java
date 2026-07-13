package hk.model.world;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.combat.ArenaAttack;
import hk.model.combat.Projectile;
import hk.model.combat.WraithBurst;
import hk.model.enemy.EnemyModel;
import hk.model.interaction.DialogueModel;
import hk.model.npc.Zote;


public class World {

    public PlayerModel player;
    public final Vector2 spawnPoint = new Vector2();
    public String currentMap;
    public float  mapWidth, mapHeight;

    public final List<Rectangle>      solids         = new ArrayList<>();
    public final List<Rectangle>      ceilingSolids  = new ArrayList<>();
    public final List<EnemyModel>          enemies        = new ArrayList<>();
    public final List<Hazard>         hazards        = new ArrayList<>();
    public final List<Exit>           exits          = new ArrayList<>();
    public final List<BreakableWall>  breakableWalls = new ArrayList<>();
    public final List<Rectangle>      secretRooms    = new ArrayList<>();
    public final List<CharmPickup>    pickups        = new ArrayList<>();
    public final List<PropSpawn>      props          = new ArrayList<>();
    public final List<Zone>           zones          = new ArrayList<>();
    public final List<Projectile>     projectiles    = new ArrayList<>();
    public final List<WraithBurst>    wraiths        = new ArrayList<>();
    public final List<ArenaAttack>    arenaAttacks   = new ArrayList<>();

    public Zote          zote;
    public FalseKnight   boss;
    public final DialogueModel dialogueModel = new DialogueModel();
    public final hk.model.cheat.CheatState cheats = new hk.model.cheat.CheatState();


    public boolean inBossFightArea = false;
    public BossFightArena bossFightArena;

    public World(PlayerModel player) {
        this.player = player;
        spawnPoint.set(player.position);
    }


    public Zone zoneAt(float x, float y) {
        for (Zone z : zones) {
            if (z.bounds.contains(x, y)) return z;
        }
        return zones.isEmpty() ? null : zones.get(zones.size() - 1);
    }


    public boolean isBossCameraLocked() {
        return inBossFightArea && bossFightArena != null && bossFightArena.isBossThere();
    }


    public float getCameraTargetX() {
        return isBossCameraLocked() ? bossFightArena.centerX()
                                    : player.position.x + PlayerModel.WIDTH / 2f;
    }


    public float getCameraTargetY() {
        return isBossCameraLocked() ? bossFightArena.centerY()
                                    : player.position.y + PlayerModel.HEIGHT / 2f;
    }
}
