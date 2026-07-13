package hk.controller.player;

import com.badlogic.gdx.math.Rectangle;

import hk.controller.ControlDemultiplexer;
import hk.model.player.PlayerModel;
import hk.model.combat.Projectile;
import hk.model.combat.WraithBurst;
import hk.input.GameAction;
import hk.model.world.Hazard;
import hk.model.world.World;
import hk.service.CollisionService;
import hk.service.KnightActionService;
import hk.service.KnightStateService;
import hk.service.TimerService;
import hk.physics.CollisionWorld;
import hk.service.RespawnService;
import hk.service.TimeScaleService;
import hk.service.InputService;


public class PlayerController {

    private final World                 world;
    private final PlayerModel                knight;
    private final CollisionWorld         physics;
    private final TimeScaleService      timeScale;
    private final ControlDemultiplexer  demux;
    private final TimerService          timers;
    private final KnightActionService   actions;
    private final KnightStateService    stateService;
    private final CollisionService      collision;
    private final RespawnService        respawn;
    private boolean spawnConfirmed = false;
    private float   castLockTimer  = 0f;
    private boolean focusHoldConsumed = false;

    public PlayerController(World world, InputService input, CollisionWorld physics,
                            TimeScaleService timeScale, TimerService timers) {
        this.world       = world;
        this.knight      = world.player;
        this.physics     = physics;
        this.timeScale   = timeScale;
        this.timers      = timers;
        this.actions     = new KnightActionService();
        this.stateService = new KnightStateService();
        this.collision   = new CollisionService();
        this.respawn     = new RespawnService();
        this.demux       = new ControlDemultiplexer(input);
        registerControls();
    }

    private void registerControls() {


        demux.onHold (GameAction.LEFT,        dt -> actions.startMove(knight, -1))
             .onHold (GameAction.RIGHT,       dt -> actions.startMove(knight,  1))
             .onPress(GameAction.DOUBLE_JUMP, dt -> actions.doubleJump(knight))
             .onPress(GameAction.DASH,        dt -> actions.startDash(knight))
             .onPress(GameAction.ATTACK,      dt -> handleAttack())
             .onPress(GameAction.CAST,        dt -> startCast());
    }

    public void update(float delta) {
        float scaledDelta = timeScale.scaleDelta(delta);

        timers.tickKnight(knight, scaledDelta);
        castLockTimer    = Math.max(0f, castLockTimer - scaledDelta);
        knight.isCasting = castLockTimer > 0f;
        if (!knight.isCasting) knight.isScreaming = false;

        if (!knight.isAlive() && knight.deathTimer <= 0f) {
            respawn.doRespawn(knight, world);
        }

        if (!knight.isAlive()) {
            knight.velocity.set(0, 0);
            stateService.update(knight);
            return;
        }

        if (knight.noclip) {
            updateNoclip(scaledDelta);
            return;
        }


        if (knight.getUpTimer > 0f) {
            knight.velocity.x = 0f;
            physics.applyGravity(knight, scaledDelta);
            physics.moveY(knight, scaledDelta);
            physics.resolveY(knight, world.solids);
            stateService.update(knight);
            return;
        }


        if (demux.isJustPressed(GameAction.JUMP) && castLockTimer <= 0f && !world.dialogueModel.isActive()) {
            knight.jumpBufferTimer = PlayerModel.JUMP_BUFFER;
        }

        if (knight.jumpHeld
                && !demux.isDown(GameAction.JUMP) && !demux.isDown(GameAction.DOUBLE_JUMP)) {
            if (knight.velocity.y > 0f) knight.velocity.y *= PlayerModel.JUMP_CUT;
            knight.jumpHeld = false;
        }
        if (knight.velocity.y <= 0f) knight.jumpHeld = false;

        handleMovement(scaledDelta);

        if (world.dialogueModel.isActive()) {

            knight.velocity.x = 0f;
            physics.applyGravity(knight, scaledDelta);
            physics.moveY(knight, scaledDelta);
            physics.resolveY(knight, world.solids);
            stateService.update(knight);
            return;
        }

        handleFocus(scaledDelta);

        physics.applyGravity(knight, scaledDelta);
        physics.moveX(knight, scaledDelta);
        physics.resolveX(knight, world.solids);
        updateWallSlide(world.solids);
        physics.moveY(knight, scaledDelta);
        physics.resolveY(knight, world.solids);
        physics.resolveCeiling(knight, world.ceilingSolids);

        if (knight.onGround) {
            knight.refreshAirAbilities();
            knight.coyoteTimer      = PlayerModel.COYOTE_TIME;
            knight.lastWallJumpSide = 0;
            if (isSafePosition()) {
                knight.respawnPoint.set(knight.position);
                if (!spawnConfirmed) {
                    world.spawnPoint.set(knight.position);
                    spawnConfirmed = true;
                }
            }
        }

        tryConsumeJump();
        collectPickups();

        updateProjectiles(scaledDelta);
        stateService.update(knight);
    }


    private void updateNoclip(float delta) {
        float vx = 0f, vy = 0f;
        if (demux.isDown(GameAction.LEFT))      vx -= PlayerModel.NOCLIP_SPEED;
        if (demux.isDown(GameAction.RIGHT))     vx += PlayerModel.NOCLIP_SPEED;
        if (demux.isDown(GameAction.JUMP))      vy += PlayerModel.NOCLIP_SPEED;
        if (demux.isDown(GameAction.LOOK_DOWN)) vy -= PlayerModel.NOCLIP_SPEED;
        if (vx != 0f) knight.facing = vx > 0 ? PlayerModel.Facing.RIGHT : PlayerModel.Facing.LEFT;
        knight.velocity.set(vx, vy);
        physics.moveX(knight, delta);
        physics.moveY(knight, delta);
        stateService.update(knight);
    }

    private void handleMovement(float delta) {
        if (knight.dashTimer > 0f) {
            knight.velocity.x = (knight.facing == PlayerModel.Facing.RIGHT) ? PlayerModel.DASH_SPEED : -PlayerModel.DASH_SPEED;
            knight.velocity.y = 0f;
        } else if (knight.knockbackTimer > 0f) {
            knight.velocity.x = knight.knockbackVx;
        } else if (knight.wallJumpLockTimer > 0f) {

            knight.velocity.x = knight.wallJumpVx;
        } else {
            knight.velocity.x = 0f;
            if (!demux.isDown(GameAction.FOCUS) && castLockTimer <= 0f)
                demux.dispatch(delta);
        }
    }


    private void tryConsumeJump() {
        if (knight.jumpBufferTimer <= 0f) return;
        if (knight.isWallSliding && knight.wallSide != knight.lastWallJumpSide) {
            actions.wallJump(knight);
        } else if (knight.onGround || (knight.coyoteTimer > 0f && knight.velocity.y <= 0f)) {
            actions.groundJump(knight);
        }
    }

    private void handleFocus(float delta) {
        boolean focusHeld = demux.isDown(GameAction.FOCUS);
        if (!focusHeld) focusHoldConsumed = false;

        boolean canFocus  = focusHeld
                && !focusHoldConsumed
                && knight.onGround
                && knight.velocity.x == 0f
                && knight.knockbackTimer <= 0f
                && knight.hurtTimer <= 0f
                && !knight.isAttacking
                && knight.soul >= PlayerModel.FOCUS_COST;

        if (!canFocus) {


            if (knight.isFocusing) knight.focusEndTimer = PlayerModel.FOCUS_END_DURATION;
            knight.isFocusing = false;
            knight.focusTimer = 0f;
            return;
        }

        knight.isFocusing  = true;
        knight.focusTimer += delta;



        if (knight.focusTimer >= knight.focusDuration()) {
            if (knight.masks < knight.maxMasks) {
                knight.spendSoul(PlayerModel.FOCUS_COST);
                knight.gainMasks(1);
                knight.healBurst = true;
            }
            knight.isFocusing    = false;
            knight.focusTimer    = 0f;
            knight.focusEndTimer = PlayerModel.FOCUS_END_DURATION;
            focusHoldConsumed    = true;
        }
    }

    private void startCast() {
        if (world.dialogueModel.isActive()) return;
        if (!knight.canCast()) return;
        knight.spendSoul(PlayerModel.SPELL_COST);
        castLockTimer = PlayerModel.CAST_DURATION;

        knight.isScreaming = demux.isDown(GameAction.LOOK_UP);
        if (knight.isScreaming) castHowlingWraiths();
        else                    castVengefulSpirit();
    }

    private void castVengefulSpirit() {
        boolean right = knight.facing == PlayerModel.Facing.RIGHT;
        float vx = right ? PlayerModel.VS_SPEED : -PlayerModel.VS_SPEED;
        float x  = right ? knight.position.x + PlayerModel.WIDTH : knight.position.x - PlayerModel.VS_WIDTH;
        float y  = knight.position.y + (PlayerModel.HEIGHT - PlayerModel.VS_HEIGHT) / 2f;
        Projectile vs = new Projectile(x, y, vx, 0f, PlayerModel.VS_WIDTH, PlayerModel.VS_HEIGHT,
                knight.spellDamage(), true, PlayerModel.VS_LIFETIME);
        vs.voidArt = knight.hasCharm(hk.model.charm.CharmType.VOID_HEART);
        world.projectiles.add(vs);
    }

    private void castHowlingWraiths() {
        float x = knight.position.x + (PlayerModel.WIDTH - WraithBurst.WIDTH) / 2f;
        float y = knight.position.y + PlayerModel.HEIGHT;
        world.wraiths.add(new WraithBurst(x, y, knight.spellDamage()));
    }

    private void updateProjectiles(float delta) {
        java.util.Iterator<Projectile> it = world.projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.lifeTimer -= delta;
            if (p.lifeTimer <= 0f) { p.alive = false; }
            if (!p.alive) { it.remove(); continue; }

            p.position.x += p.velocity.x * delta;
            p.position.y += p.velocity.y * delta;

            Rectangle pb = p.bounds();
            for (Rectangle solid : world.solids) {
                if (pb.overlaps(solid)) { p.alive = false; break; }
            }
            if (!p.alive) it.remove();
        }
    }


    private void collectPickups() {
        for (hk.model.world.CharmPickup p : world.pickups) {
            if (!p.collected && collision.overlaps(knight.bounds(), p.bounds())) {
                p.collected = true;
                knight.charms.acquire(new hk.model.charm.Charm(p.type));
            }
        }
    }

    private boolean isSafePosition() {
        for (Hazard h : world.hazards) {
            if (collision.overlaps(knight.bounds(), h.bounds)) return false;
        }
        return true;
    }

    private void updateWallSlide(java.util.List<com.badlogic.gdx.math.Rectangle> solids) {
        if (knight.onGround || knight.dashTimer > 0f || knight.velocity.y > 0f) {
            endWallSlide();
            return;
        }
        boolean leftPressed  = demux.isDown(GameAction.LEFT);
        boolean rightPressed = demux.isDown(GameAction.RIGHT);
        boolean onLeftWall   = leftPressed  && physics.touchesWall(knight.leftWallProbe(),  solids);
        boolean onRightWall  = rightPressed && physics.touchesWall(knight.rightWallProbe(), solids);
        if (onLeftWall || onRightWall) {
            knight.isWallSliding = true;
            knight.touchingWall  = true;
            knight.wallSide      = onLeftWall ? -1 : 1;
            if (knight.velocity.y < PlayerModel.WALL_SLIDE_SPEED)
                knight.velocity.y = PlayerModel.WALL_SLIDE_SPEED;
        } else {
            endWallSlide();
        }
    }

    private void endWallSlide() {
        knight.isWallSliding = false;
        knight.touchingWall  = false;
        knight.wallSide      = 0;
    }

    private void handleAttack() {
        if (world.dialogueModel.isActive()) return;
        if (!knight.onGround && demux.isDown(GameAction.LOOK_DOWN)) {
            actions.startDownAttack(knight);
        } else if (demux.isDown(GameAction.LOOK_UP)) {
            actions.startUpAttack(knight);
        } else {
            actions.startAttack(knight);
        }
    }

    public ControlDemultiplexer getDemux() {
        return demux;
    }
}
