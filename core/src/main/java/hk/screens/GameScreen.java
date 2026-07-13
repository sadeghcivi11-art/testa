package hk.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Map;

import hk.GameContext;
import hk.controller.AppNavigator;
import hk.controller.CentralController;
import hk.controller.GameOverlayController;
import hk.model.AppModel;
import hk.model.GameData;
import hk.model.player.PlayerModel;
import hk.model.menu.GameOverlayModel;
import hk.model.progress.AchievementId;
import hk.service.AchievementService;
import hk.service.AudioService;
import hk.service.LocalizationService;
import hk.service.SaveService;
import hk.service.SessionStatsService;
import hk.view.helper.AudioDirector;
import hk.view.render.AchievementPopupRenderer;
import hk.view.render.BrightnessRenderer;
import hk.view.render.GameOverlayRenderer;
import hk.view.render.HudRenderer;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.CrawlidState;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.HuskHornhead;
import hk.model.enemy.concrete.Mosscreep;
import hk.model.world.BossFightArena;
import hk.model.world.EnemySpawn;
import hk.model.world.Hazard;
import hk.model.world.World;
import hk.view.helper.CameraDirector;
import hk.service.InputService;
import hk.physics.CollisionWorld;
import hk.service.TimeScaleService;
import hk.util.Units;
import hk.model.boss.FalseKnight;
import hk.view.anim.CrawlidAnimationSet;
import hk.view.anim.CrystalGuardianAnimationSet;
import hk.view.anim.FalseKnightAnimationSet;
import hk.view.anim.HuskHornheadAnimationSet;
import hk.view.anim.MosscreepAnimationSet;
import hk.view.anim.KnightAnimationSet;
import hk.view.anim.KnightEffectSet;
import hk.view.enemy.CrawlidView;
import hk.view.render.CrystalGuardianRenderer;
import hk.view.render.FalseKnightRenderer;
import hk.view.render.HuskHornheadRenderer;
import hk.view.render.MosscreepRenderer;
import hk.view.player.PlayerView;
import hk.view.render.DebugRenderer;
import hk.view.render.EnemyRenderer;
import hk.view.render.EnvironmentRenderer;
import hk.view.world.PropView;
import hk.view.render.AtmosphereRenderer;
import hk.view.render.DialogueBoxRenderer;
import hk.view.render.SpellRenderer;
import hk.view.render.ZoteRenderer;
import hk.service.AssetService;
import hk.service.TiledLevelLoader;
import hk.view.anim.ZoteAnimationSet;
import hk.model.npc.Zote;
import hk.service.ZoteDialogueService;


public class GameScreen implements Screen {


    private static final float MIN_WORLD_WIDTH  = 352f;
    private static final float MIN_WORLD_HEIGHT = 198f;
    private static final float ARENA_HEIGHT         = 170f;
    private static final float ARENA_MAX_HALF_WIDTH = 240f;
    private static final String MAP_PATH       = "gameMaps/ForgottenCrossroads/Forgotten crossroads.tmx";
    private static final float  MAP_UNIT_SCALE = Units.UNIT_SCALE;
    private static final float  SPEEDRUN_LIMIT_SECONDS = 15 * 60f;


    private OrthographicCamera camera;
    private Viewport            viewport;
    private SpriteBatch         batch;


    private final AppNavigator       navigator;
    private final AppModel           app;
    private final SaveService        saveService;
    private final AchievementService achievements;
    private final AudioService       audio;
    private final int                slot;
    private final GameData           gameData;
    private boolean exitRequested    = false;
    private boolean restartRequested = false;
    private boolean victoryShown     = false;


    private World world;


    private CentralController     centralController;
    private TimeScaleService      timeScaleService;
    private CameraDirector        cameraDirector;
    private GameOverlayModel      overlayModel;
    private GameOverlayController overlayController;
    private AudioDirector         audioDirector;
    private SessionStatsService   statsService;


    private AssetService          animationHelper;
    private PlayerView           knightRenderer;
    private CrawlidView          crawlidRenderer;
    private HuskHornheadRenderer     huskHornheadRenderer;
    private CrystalGuardianRenderer  crystalGuardianRenderer;
    private MosscreepRenderer        mosscreepRenderer;
    private final Map<Class<?>, EnemyRenderer> enemyRenderers = new HashMap<>();
    private FalseKnightRenderer      falseKnightRenderer;
    private SpellRenderer            spellRenderer;
    private DebugRenderer            debugRenderer;
    private EnvironmentRenderer      environmentRenderer;
    private PropView             propRenderer;
    private AtmosphereRenderer       atmosphereRenderer;
    private hk.model.world.Zone      currentZone;
    private ZoteRenderer             zoteRenderer;
    private DialogueBoxRenderer      dialogueBoxRenderer;
    private HudRenderer              hudRenderer;
    private GameOverlayRenderer      overlayRenderer;
    private AchievementPopupRenderer popupRenderer;
    private hk.view.render.ZoneTitleRenderer zoneTitleRenderer;
    private ShapeRenderer            uiShapes;
    private OrthographicCamera       uiCamera;
    private hk.view.helper.FontLibrary                 uiFonts;
    private com.badlogic.gdx.graphics.g2d.GlyphLayout  fpsGlyphs;
    private TiledLevelLoader                mapHelper;
    private OrthogonalTiledMapRenderer mapRenderer;



    private static final Rectangle GREENPATH_WEST  = new Rectangle(210f, 700f, 440f, 100f);
    private static final Rectangle GREENPATH_EAST  = new Rectangle(650f, 655f, 350f, 145f);
    private static final Rectangle BOSSARENA_BOUNDS = new Rectangle(262f, 250f, 364f, 186f);

    public GameScreen(AppNavigator navigator, GameContext ctx, int slot, GameData gameData) {
        AppModel           app          = ctx.appModel();
        LocalizationService loc         = ctx.localization();
        SaveService        saveService  = ctx.saveService();
        AchievementService achievements = ctx.achievements();
        AudioService       audio        = ctx.audio();

        this.navigator    = navigator;
        this.app          = app;
        this.saveService  = saveService;
        this.achievements = achievements;
        this.audio        = audio;
        this.slot         = slot;
        this.gameData     = gameData;

        camera   = new OrthographicCamera();
        viewport = new ExtendViewport(MIN_WORLD_WIDTH, MIN_WORLD_HEIGHT, camera);
        batch    = new SpriteBatch();

        world = new World(new PlayerModel(77f, 400f));
        world.currentMap = MAP_PATH;
        camera.position.set(world.getCameraTargetX(), world.getCameraTargetY(), 0);

        InputService   input   = new InputService(app.bindings);
        CollisionWorld physics = new CollisionWorld();
        timeScaleService  = new TimeScaleService();
        centralController = new CentralController(world, input, physics, timeScaleService);
        cameraDirector    = new CameraDirector(camera, viewport);

        mapHelper = new TiledLevelLoader();
        try {
            mapHelper.loadMap(MAP_PATH, MAP_UNIT_SCALE);
            world.solids.addAll(mapHelper.getSolids());
            world.mapWidth  = mapHelper.getMapWidthUnits();
            world.mapHeight = mapHelper.getMapHeightUnits();
            for (Rectangle r : mapHelper.getHazards())
                world.hazards.add(new Hazard(r, 1));
            for (EnemySpawn spawn : mapHelper.getEnemySpawns()) {
                EnemyModel e = createEnemy(spawn);
                if (e != null) world.enemies.add(e);
            }
            for (EnemySpawn spawn : mapHelper.getNpcSpawns()) {
                if ("zote".equals(spawn.type)) {
                    world.zote = new Zote(spawn.x, spawn.y, new ZoteDialogueService());
                }
            }
            for (Rectangle r : mapHelper.getBreakables()) {
                hk.model.world.BreakableWall wall = new hk.model.world.BreakableWall(r);
                world.breakableWalls.add(wall);
                world.solids.add(wall.bounds);
            }
            world.secretRooms.addAll(mapHelper.getSecretRooms());
            for (EnemySpawn spawn : mapHelper.getPickupSpawns()) {
                try {
                    world.pickups.add(new hk.model.world.CharmPickup(
                            hk.model.charm.CharmType.valueOf(spawn.type), spawn.x, spawn.y));
                } catch (IllegalArgumentException bad) {
                    Gdx.app.error("GameScreen", "Unknown charm pickup type: " + spawn.type);
                }
            }
            world.props.addAll(mapHelper.getPropSpawns());
            mapRenderer = new OrthogonalTiledMapRenderer(mapHelper.getMap(), MAP_UNIT_SCALE, batch);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Could not load map '" + MAP_PATH + "': " + e.getMessage());
        }


        world.zones.add(new hk.model.world.Zone("greenpath", GREENPATH_WEST,
                hk.model.world.Zone.ParticleMode.SPORES,
                new float[] { 0.05f, 0.10f, 0.06f }, new float[] { 0.10f, 0.22f, 0.11f }));
        world.zones.add(new hk.model.world.Zone("greenpath", GREENPATH_EAST,
                hk.model.world.Zone.ParticleMode.SPORES,
                new float[] { 0.05f, 0.10f, 0.06f }, new float[] { 0.10f, 0.22f, 0.11f }));
        world.zones.add(new hk.model.world.Zone("bossarena", BOSSARENA_BOUNDS,
                hk.model.world.Zone.ParticleMode.DUST,
                new float[] { 0.08f, 0.04f, 0.05f }, new float[] { 0.16f, 0.08f, 0.10f }));
        world.zones.add(new hk.model.world.Zone("crossroads",
                new Rectangle(0f, 0f, 1000f, 800f), hk.model.world.Zone.ParticleMode.DUST,
                new float[] { 0.04f, 0.05f, 0.09f }, new float[] { 0.10f, 0.12f, 0.19f }));



        saveService.restore(gameData, world);
        camera.position.set(world.getCameraTargetX(), world.getCameraTargetY(), 0);
        saveService.snapshot(world, gameData);
        saveService.saveSlot(slot, gameData);

        overlayModel      = new GameOverlayModel();
        overlayController = new GameOverlayController(overlayModel, input, world.player.charms,
                app, saveService,
                new GameOverlayController.SessionActions() {
                    @Override public void saveAndExit() { exitRequested = true; }
                    @Override public void restart()     { restartRequested = true; }
                }, audio);


        victoryShown = gameData.bossDefeated;

        audioDirector = new AudioDirector(audio);
        audioDirector.prime(world);
        statsService = new SessionStatsService();
        statsService.prime(world, gameData);

        currentZone = world.zoneAt(world.player.position.x + PlayerModel.WIDTH / 2f,
                                   world.player.position.y + PlayerModel.HEIGHT / 2f);
        if (currentZone != null) audio.playMusic(currentZone.name);

        animationHelper = new AssetService();
        knightRenderer           = new PlayerView(batch, camera, new KnightAnimationSet(animationHelper), new KnightEffectSet(animationHelper));
        crawlidRenderer          = new CrawlidView(batch, new CrawlidAnimationSet(animationHelper));
        huskHornheadRenderer     = new HuskHornheadRenderer(batch, new HuskHornheadAnimationSet(animationHelper));
        crystalGuardianRenderer  = new CrystalGuardianRenderer(batch, new CrystalGuardianAnimationSet(animationHelper));
        mosscreepRenderer        = new MosscreepRenderer(batch, camera, new MosscreepAnimationSet(animationHelper));
        enemyRenderers.put(CrawlidState.class,         crawlidRenderer);
        enemyRenderers.put(HuskHornhead.class,    huskHornheadRenderer);
        enemyRenderers.put(CrystalGuardian.class, crystalGuardianRenderer);
        enemyRenderers.put(Mosscreep.class,       mosscreepRenderer);
        falseKnightRenderer      = new FalseKnightRenderer(batch, new FalseKnightAnimationSet(animationHelper));
        spellRenderer            = new SpellRenderer(batch, animationHelper);
        debugRenderer            = new DebugRenderer(batch);
        environmentRenderer      = new EnvironmentRenderer(batch);
        propRenderer             = new PropView(batch);
        atmosphereRenderer       = new AtmosphereRenderer();
        atmosphereRenderer.setZone(currentZone, camera);
        zoteRenderer             = new ZoteRenderer(batch, new ZoteAnimationSet(animationHelper));
        uiFonts                  = new hk.view.helper.FontLibrary();
        dialogueBoxRenderer      = new DialogueBoxRenderer(uiFonts, loc);
        hudRenderer              = new HudRenderer();
        overlayRenderer          = new GameOverlayRenderer(loc, uiFonts);
        popupRenderer            = new AchievementPopupRenderer(loc, uiFonts, audio);
        zoneTitleRenderer        = new hk.view.render.ZoneTitleRenderer(loc);
        if (currentZone != null) zoneTitleRenderer.show(currentZone.name);
        uiShapes                 = new ShapeRenderer();
        uiCamera                 = new OrthographicCamera();
        fpsGlyphs                = new com.badlogic.gdx.graphics.g2d.GlyphLayout();


        world.player.charms.addListener(overlayRenderer);
        overlayRenderer.onCharmsChanged(world.player.charms);
        achievements.addListener(popupRenderer);
    }

    @Override
    public void render(float delta) {
        delta = TimeScaleService.snapDelta(delta);
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        boolean frozen = overlayController.update(world.dialogueModel.isActive());
        if (exitRequested) {
            saveService.snapshot(world, gameData);
            saveService.saveSlot(slot, gameData);
            navigator.showMenu();
            return;
        }
        if (restartRequested) {

            GameData fresh = GameData.newGame();
            saveService.saveSlot(slot, fresh);
            navigator.startSession(slot, fresh);
            return;
        }
        if (!frozen) {
            centralController.update(delta);
            cameraDirector.update(world, delta);
            gameData.playTimeSeconds += delta;
            statsService.update(world, gameData, achievements);
            audioDirector.update(world);
            checkAchievements();


            hk.model.world.Zone zone = world.zoneAt(
                    world.player.position.x + PlayerModel.WIDTH / 2f,
                    world.player.position.y + PlayerModel.HEIGHT / 2f);
            if (zone != null && zone != currentZone) {


                boolean newArea = currentZone == null || !zone.name.equals(currentZone.name);
                currentZone = zone;
                if (newArea) {
                    audio.playMusic(zone.name);
                    zoneTitleRenderer.show(zone.name);
                }
            }
        }
        atmosphereRenderer.setZone(currentZone, camera);


        float scaledDelta = frozen ? 0f : timeScaleService.scaleDelta(delta);

        atmosphereRenderer.drawBackdrop(delta);
        if (mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render(mapHelper.getBackgroundLayers());
        }
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        propRenderer.drawBackground(world.props, camera, scaledDelta);
        environmentRenderer.drawWorld(world, scaledDelta);
        for (EnemyModel e : world.enemies) {
            EnemyRenderer r = enemyRenderers.get(e.getClass());
            if (r != null) r.draw(e, scaledDelta);
        }
        falseKnightRenderer.draw(world.boss, scaledDelta);
        falseKnightRenderer.drawShockwaves(world.arenaAttacks);
        zoteRenderer.draw(world.zote, scaledDelta);
        knightRenderer.draw(world.player, scaledDelta);
        spellRenderer.draw(world.projectiles, scaledDelta);
        atmosphereRenderer.drawParticles(batch, camera, scaledDelta);
        debugRenderer.draw(world);
        batch.end();
        if (mapRenderer != null) {
            mapRenderer.render(mapHelper.getForegroundLayers());
        }
        batch.begin();
        propRenderer.drawForeground(world.props, camera);
        batch.end();

        environmentRenderer.drawVeils(world, scaledDelta);

        hudRenderer.draw(world.player, scaledDelta);
        dialogueBoxRenderer.draw(world.dialogueModel,
                centralController.getNpcController().hasPrompt());
        overlayRenderer.draw(overlayModel, app, world.player.charms, gameData, world.cheats);
        popupRenderer.update(delta);

        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();
        zoneTitleRenderer.draw(batch, uiCamera, delta);
        BrightnessRenderer.draw(uiShapes, uiCamera, app.settings.brightness,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (app.settings.showFps) {
            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            com.badlogic.gdx.graphics.g2d.BitmapFont fpsFont = uiFonts.small;
            fpsFont.setColor(1f, 1f, 1f, 0.7f);
            fpsGlyphs.setText(fpsFont, Gdx.graphics.getFramesPerSecond() + " FPS");
            fpsFont.draw(batch, fpsGlyphs,
                    Gdx.graphics.getWidth() - fpsGlyphs.width - 14f,
                    Gdx.graphics.getHeight() - 10f);
            fpsFont.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }
    }


    private void checkAchievements() {
        if (world.boss != null && world.boss.currentState == FalseKnight.State.DEAD) {
            gameData.bossDefeated = true;
            achievements.unlock(AchievementId.DEFEAT_FALSE_KNIGHT);
            achievements.unlock(AchievementId.COMPLETION);
            if (gameData.playTimeSeconds <= SPEEDRUN_LIMIT_SECONDS) {
                achievements.unlock(AchievementId.SPEEDRUN);
            }
            if (!victoryShown) {
                victoryShown = true;
                saveService.snapshot(world, gameData);
                saveService.saveSlot(slot, gameData);
                overlayModel.open(GameOverlayModel.Overlay.VICTORY);
                audio.playMusic(AudioService.MUSIC_VICTORY);
            }
        }
        if (world.zote != null && world.zote.state == Zote.State.ATTACK) {
            achievements.unlock(AchievementId.CUSTOM);
        }
    }

    private EnemyModel createEnemy(EnemySpawn spawn) {
        switch (spawn.type) {
            case "crawlid":      return new CrawlidState(spawn.x, spawn.y);
            case "huskhornhead": return new HuskHornhead(spawn.x, spawn.y);
            case "crystallized": return new CrystalGuardian(spawn.x, spawn.y);
            case "mosscreep":    return new Mosscreep(spawn.x, spawn.y);
            case "falseknight": {
                FalseKnight fk = new FalseKnight(spawn.x, spawn.y);
                world.boss = fk;
                world.bossFightArena = buildBossArena(fk, spawn.x, spawn.y);
                return null;
            }
            default:
                Gdx.app.error("GameScreen", "Unknown enemy type: " + spawn.type);
                return null;
        }
    }

    private BossFightArena buildBossArena(FalseKnight boss, float spawnX, float spawnY) {
        float centerX = spawnX + boss.width / 2f;
        Rectangle floor = findFloorUnder(centerX, spawnY);

        if (floor == null) {
            Gdx.app.error("GameScreen", "Boss arena: no floor under spawn — boss not spawned");
            world.boss = null;
            return null;
        }

        float left   = floor.x;
        float right  = floor.x + floor.width;
        float bottom = floor.y + floor.height;

        if (right - left > ARENA_MAX_HALF_WIDTH * 2f) {
            float c = MathUtils.clamp(centerX, left + ARENA_MAX_HALF_WIDTH, right - ARENA_MAX_HALF_WIDTH);
            left  = c - ARENA_MAX_HALF_WIDTH;
            right = c + ARENA_MAX_HALF_WIDTH;
        }

        Rectangle bounds = new Rectangle(left, bottom, right - left, ARENA_HEIGHT);
        Gdx.app.log("GameScreen", "Boss arena bounds: " + bounds);
        return new BossFightArena(bounds, false);
    }

    private Rectangle findFloorUnder(float x, float yAbove) {
        Rectangle best = null;
        float bestTop = -Float.MAX_VALUE;
        for (Rectangle s : world.solids) {
            if (x < s.x || x > s.x + s.width) continue;
            float top = s.y + s.height;
            if (top > yAbove) continue;
            if (top > bestTop) { bestTop = top; best = s; }
        }
        return best;
    }

    @Override public void show() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (zoneTitleRenderer != null) zoneTitleRenderer.resize(height);
        if (uiFonts           != null) uiFonts.ensure(height);
    }

    @Override
    public void dispose() {
        achievements.removeListener(popupRenderer);
        if (hudRenderer             != null) hudRenderer.dispose();
        if (overlayRenderer         != null) overlayRenderer.dispose();
        if (popupRenderer           != null) popupRenderer.dispose();
        if (zoneTitleRenderer       != null) zoneTitleRenderer.dispose();
        if (uiShapes                != null) uiShapes.dispose();
        if (uiFonts                 != null) uiFonts.dispose();
        if (mapRenderer             != null) mapRenderer.dispose();
        if (mapHelper               != null) mapHelper.dispose();
        if (environmentRenderer     != null) environmentRenderer.dispose();
        if (propRenderer            != null) propRenderer.dispose();
        if (atmosphereRenderer      != null) atmosphereRenderer.dispose();
        if (knightRenderer          != null) knightRenderer.dispose();
        if (debugRenderer           != null) debugRenderer.dispose();
        if (dialogueBoxRenderer     != null) dialogueBoxRenderer.dispose();
        if (animationHelper         != null) animationHelper.dispose();
        if (batch                   != null) batch.dispose();
    }
}
