package hk.service;

import com.badlogic.gdx.math.Rectangle;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.boss.FalseKnight.State;
import hk.model.world.BossFightArena;
import hk.model.world.World;


public class BossArenaService {

    private static final float GATE_WIDTH      = 8f;
    private static final float GATE_OVERHEIGHT = 120f;
    private static final float TRIGGER_MARGIN  = 10f;
    private static final float INTRO_DELAY     = 1.0f;

    public void update(World world, float delta) {
        BossFightArena arena = world.bossFightArena;
        if (arena == null) return;

        FalseKnight boss = world.boss;


        if (boss == null || boss.isDead() || boss.currentState == State.DEAD) {
            openGates(world, arena);
            arena.bossThere       = false;
            world.inBossFightArea = false;
            return;
        }

        PlayerModel player = world.player;
        float playerCenterX = player.position.x + PlayerModel.WIDTH / 2f;
        float playerFeetY   = player.position.y;
        boolean inX = playerCenterX > arena.bounds.x + TRIGGER_MARGIN
                   && playerCenterX < arena.bounds.x + arena.bounds.width - TRIGGER_MARGIN;


        boolean inY = playerFeetY >= arena.bounds.y - 24f
                   && playerFeetY <= arena.bounds.y + arena.bounds.height;
        boolean playerInside = inX && inY;

        if (!arena.gatesClosed) {


            if (playerInside && boss.isAlive() && player.isAlive()) {
                closeGates(world, arena);
                arena.triggered       = true;
                arena.bossThere       = true;
                world.inBossFightArea = true;
                boss.decisionTimer    = INTRO_DELAY;
            }
        } else {

            if (!player.isAlive()) resetFight(world, arena);
        }
    }

    private void closeGates(World world, BossFightArena arena) {
        float h   = arena.bounds.height + GATE_OVERHEIGHT;
        float top = arena.bounds.y + arena.bounds.height;
        arena.leftGate  = new Rectangle(arena.bounds.x - GATE_WIDTH, arena.bounds.y, GATE_WIDTH, h);
        arena.rightGate = new Rectangle(arena.bounds.x + arena.bounds.width, arena.bounds.y, GATE_WIDTH, h);
        arena.ceiling   = new Rectangle(arena.bounds.x, top, arena.bounds.width, GATE_WIDTH);
        world.solids.add(arena.leftGate);
        world.solids.add(arena.rightGate);
        world.ceilingSolids.add(arena.ceiling);
        arena.gatesClosed = true;
    }

    private void openGates(World world, BossFightArena arena) {
        if (arena.leftGate  != null) { world.solids.remove(arena.leftGate);       arena.leftGate  = null; }
        if (arena.rightGate != null) { world.solids.remove(arena.rightGate);      arena.rightGate = null; }
        if (arena.ceiling   != null) { world.ceilingSolids.remove(arena.ceiling); arena.ceiling   = null; }
        arena.gatesClosed = false;
    }


    private void resetFight(World world, BossFightArena arena) {
        openGates(world, arena);
        arena.triggered       = false;
        arena.bossThere       = false;
        world.inBossFightArea = false;

        FalseKnight boss = world.boss;
        if (boss == null) return;
        boss.health   = FalseKnight.MAX_HP;
        boss.phase    = 1;
        boss.position.set(boss.spawnPosition);
        boss.velocity.set(0f, 0f);
        boss.currentState        = State.IDLE;
        boss.lastState           = State.IDLE;
        boss.moveTimer           = 0f;
        boss.decisionTimer       = 0f;
        boss.isStunnedYet        = false;
        boss.pendingStun         = false;
        boss.vulnerable          = false;
        boss.shockwaveActive     = false;
        boss.heavyHitsTaken      = 0;
        boss.heavyHitWindow      = 0f;
        boss.jumpCooldown        = 0f;
        boss.attackActive        = false;
        boss.attackTimer         = 0f;
        boss.impactFired         = false;
        boss.shakeRequest        = 0f;
    }
}
