package hk.controller;

import hk.controller.enemy.EnemyController;
import hk.controller.player.PlayerController;
import hk.input.GameAction;
import hk.model.world.World;
import hk.service.BossArenaService;
import hk.physics.CollisionWorld;
import hk.service.TimeScaleService;
import hk.service.TimerService;
import hk.service.InputService;


public class CentralController {


    private static final float MAX_STEP = 1f / 30f;

    private final World            world;
    private final TimeScaleService timeScale;

    private final PlayerController playerController;
    private final CombatController combatController;
    private final EnemyController  enemyController;
    private final BossController   bossController;
    private final BossArenaService bossArenaService;
    private final NpcController    npcController;
    private final CheatController  cheatController;

    public CentralController(World world, InputService input,
                             CollisionWorld physics, TimeScaleService timeScale) {
        this.world    = world;
        this.timeScale = timeScale;

        TimerService timers = new TimerService();
        enemyController  = new EnemyController(timers);
        bossController   = new BossController(timers);
        bossArenaService = new BossArenaService();
        playerController = new PlayerController(world, input, physics, timeScale, timers);
        combatController = new CombatController(world, timeScale, enemyController, bossController);
        npcController    = new NpcController(world, physics);
        cheatController  = new CheatController(world);
        playerController.getDemux().onPress(GameAction.INTERACT, dt -> npcController.handleInteract());
        playerController.getDemux().onPress(GameAction.ADVANCE,  dt -> npcController.handleAdvance());
    }

    public void update(float delta) {
        delta = Math.min(delta, MAX_STEP);
        float scaledDelta = timeScale.scaleDelta(delta);

        cheatController.update(delta);
        npcController.update(scaledDelta, world.solids);
        playerController.update(delta);
        combatController.update(delta);
        bossArenaService.update(world, scaledDelta);
        enemyController.update(world.enemies, world.solids, world.hazards, world.player, scaledDelta);
        bossController.update(world.boss, world.player, world.solids, world.arenaAttacks, world.inBossFightArea, scaledDelta);
    }

    public ControlDemultiplexer getDemux() {
        return playerController.getDemux();
    }

    public NpcController getNpcController() {
        return npcController;
    }
}
