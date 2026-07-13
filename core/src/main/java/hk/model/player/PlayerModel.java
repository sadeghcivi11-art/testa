package hk.model.player;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hk.model.charm.CharmInventory;
import hk.model.charm.CharmType;
import hk.model.physics.HasPhysic;


public class PlayerModel implements HasPhysic {




    public static final int   DEFAULT_MAX_MASKS = 5;
    public static final int   MAX_SOUL          = 99;
    public static final int   SOUL_PER_HIT      = 11;
    public static final int   SPELL_COST        = 33;
    public static final int   FOCUS_COST        = 33;
    public static final float IFRAME_DURATION   = 1.0f;
    public static final float ATTACK_DURATION   = 0.28f;
    public static final float DASH_DURATION     = 0.2f;
    public static final float FOCUS_DURATION    = 1.5f;
    public static final float FOCUS_START_DURATION = 3 * 0.12f;
    public static final float FOCUS_END_DURATION   = 3 * 0.12f;
    public static final int   MAX_AIR_JUMPS     = 1;



    public static final float WIDTH  = 6.67f;
    public static final float HEIGHT = 18.33f;


    public static final float NAIL_W = 20f;
    public static final float NAIL_H = 12f;




    public static final float MOVE_SPEED        = 90f;
    public static final float NOCLIP_SPEED      = MOVE_SPEED * 2.5f;
    public static final float JUMP_SPEED        = 190f;
    public static final float DOUBLE_JUMP_SPEED = 170f;
    public static final float DASH_SPEED        = 260f;




    public static final float RISE_GRAVITY_SCALE = 2.0f;
    public static final float FALL_GRAVITY_SCALE = 3.2f;
    public static final float JUMP_CUT           = 0.45f;
    public static final float KNIGHT_MAX_FALL    = -200f;
    public static final float COYOTE_TIME        = 0.10f;
    public static final float JUMP_BUFFER        = 0.12f;
    public static final float WALL_JUMP_PUSH     = 1.1f;
    public static final float WALL_JUMP_LOCK     = 0.14f;


    public static final float DOWN_NAIL_W          = 12f;
    public static final float DOWN_NAIL_H          = 12f;
    public static final float POGO_SPEED           = JUMP_SPEED;
    public static final float DOWN_ATTACK_DURATION = 0.30f;
    public static final float DOWN_NAIL_DURATION   = 0.07f;

    public static final float UP_NAIL_W          = 12f;
    public static final float UP_NAIL_H          = 16f;
    public static final float UP_ATTACK_DURATION = 0.30f;
    public static final float UP_NAIL_DURATION   = 0.07f;


    public static final float WALL_PROBE_W    = 2f;
    public static final float WALL_PROBE_H    = HEIGHT * 0.5f;
    public static final float WALL_SLIDE_SPEED = -40f;


    public static final float ATTACK_COOLDOWN = 0.13f;
    public static final float DASH_COOLDOWN   = 0.6f;


    public static final float CAST_DURATION = 0.45f;


    public static final float VS_WIDTH    = 8f;
    public static final float VS_HEIGHT   = 6f;
    public static final float VS_SPEED    = 150f;
    public static final float VS_LIFETIME = 7f;


    public static final float DOUBLE_JUMP_DURATION = 8 * 0.07f;


    public enum State {
        IDLE, RUN, JUMP, DOUBLE_JUMP, FALL, LANDING, DASH, ATTACK, DOWN_ATTACK, UP_ATTACK,
        FOCUS_START, FOCUS, FOCUS_END, WALL_SLIDE, CAST, SCREAM, HURT, DEAD, GET_UP
    }


    public enum Facing { LEFT, RIGHT }




    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public Facing facing = Facing.RIGHT;
    public boolean onGround = false;
    public boolean touchingWall = false;
    public int wallSide = 0;


    public final Vector2 respawnPoint = new Vector2();




    public State state = State.IDLE;




    public int maxMasks = DEFAULT_MAX_MASKS;
    public int masks    = DEFAULT_MAX_MASKS;
    public int soul     = MAX_SOUL;
    public float iframeTimer = 0f;




    public int   airJumpsRemaining   = MAX_AIR_JUMPS;
    public boolean jumpHeld          = false;
    public float coyoteTimer         = 0f;
    public float jumpBufferTimer     = 0f;
    public float wallJumpLockTimer   = 0f;
    public float wallJumpVx          = 0f;
    public int   lastWallJumpSide    = 0;
    public float dashTimer           = 0f;
    public float dashCooldownTimer   = 0f;
    public float attackTimer         = 0f;
    public float attackCooldownTimer = 0f;
    public float focusTimer          = 0f;
    public float focusEndTimer       = 0f;
    public boolean healBurst         = false;
    public float   pogoResetTimer    = 0f;
    public boolean isDownAttacking   = false;
    public float   downNailTimer     = 0f;
    public boolean isUpAttacking     = false;
    public float   upNailTimer       = 0f;

    public static final float HURT_DURATION  = 12 * 0.06f;
    public static final float DEATH_DURATION = 18 * 0.07f;
    public static final float GET_UP_DURATION = 14 * 0.08f;

    public float   deathTimer      = 0f;
    public float   getUpTimer      = 0f;
    public float   knockbackTimer  = 0f;
    public float   knockbackVx    = 0f;
    public boolean isAttacking     = false;
    public boolean isDoubleJumping = false;
    public boolean isCasting       = false;
    public boolean isScreaming     = false;
    public boolean isFocusing      = false;
    public boolean isWallSliding   = false;
    public float   hurtTimer       = 0f;





    public boolean godMode = false;
    public boolean noclip  = false;




    public final CharmInventory charms = new CharmInventory();




    public PlayerModel() { }

    public PlayerModel(float x, float y) {
        position.set(x, y);
        respawnPoint.set(x, y);
    }







    private final Rectangle boundsRect     = new Rectangle();
    private final Rectangle swordRect      = new Rectangle();
    private final Rectangle downSwordRect  = new Rectangle();
    private final Rectangle upSwordRect    = new Rectangle();
    private final Rectangle leftProbeRect  = new Rectangle();
    private final Rectangle rightProbeRect = new Rectangle();


    public Rectangle bounds() {
        return boundsRect.set(position.x, position.y, WIDTH, HEIGHT);
    }


    public Rectangle swordBounds() {
        if (!isAttacking || isDownAttacking || isUpAttacking) return null;
        float nx = facing == Facing.RIGHT
                ? position.x + WIDTH
                : position.x - NAIL_W;
        float ny = position.y + (HEIGHT - NAIL_H) / 2f;
        return swordRect.set(nx, ny, NAIL_W, NAIL_H);
    }


    public Rectangle downSwordBounds() {
        if (downNailTimer <= 0f) return null;
        float nx = position.x + (WIDTH - DOWN_NAIL_W) / 2f;
        float ny = position.y - DOWN_NAIL_H;
        return downSwordRect.set(nx, ny, DOWN_NAIL_W, DOWN_NAIL_H);
    }


    public Rectangle upSwordBounds() {
        if (upNailTimer <= 0f) return null;
        float nx = position.x + (WIDTH - UP_NAIL_W) / 2f;
        float ny = position.y + HEIGHT;
        return upSwordRect.set(nx, ny, UP_NAIL_W, UP_NAIL_H);
    }


    public Rectangle leftWallProbe() {
        float py = position.y + (HEIGHT - WALL_PROBE_H) / 2f;
        return leftProbeRect.set(position.x - WALL_PROBE_W, py, WALL_PROBE_W, WALL_PROBE_H);
    }


    public Rectangle rightWallProbe() {
        float py = position.y + (HEIGHT - WALL_PROBE_H) / 2f;
        return rightProbeRect.set(position.x + WIDTH, py, WALL_PROBE_W, WALL_PROBE_H);
    }

    public boolean isAlive()       { return masks > 0; }
    public boolean isInvincible()  { return godMode || noclip || iframeTimer > 0f || isShadowDashing(); }
    public boolean canDash()       { return dashCooldownTimer <= 0f && dashTimer <= 0f; }
    public boolean canAttack()     { return attackTimer <= 0f && attackCooldownTimer <= 0f; }
    public boolean canCast()       { return soul >= SPELL_COST; }
    public boolean canAirJump()    { return airJumpsRemaining > 0; }






    public boolean hasCharm(CharmType type) { return charms.isEquippedType(type); }


    public float attackDuration() { return hasCharm(CharmType.QUICK_SLASH) ? ATTACK_DURATION * 0.65f : ATTACK_DURATION; }
    public float attackCooldown() { return hasCharm(CharmType.QUICK_SLASH) ? ATTACK_COOLDOWN * 0.35f : ATTACK_COOLDOWN; }


    public float dashCooldown()   { return hasCharm(CharmType.DASHMASTER) ? DASH_COOLDOWN * 0.5f : DASH_COOLDOWN; }


    public float dashDuration()   { return hasCharm(CharmType.SHARP_SHADOW) ? DASH_DURATION * 1.2f : DASH_DURATION; }


    public float focusDuration()  { return hasCharm(CharmType.QUICK_FOCUS) ? FOCUS_DURATION * 0.6f : FOCUS_DURATION; }


    public int   nailDamage()     { return hasCharm(CharmType.UNBREAKABLE_STRENGTH) ? 2 : 1; }


    public int   soulPerHit()     { return hasCharm(CharmType.SOUL_CATCHER) ? SOUL_PER_HIT + 6 : SOUL_PER_HIT; }


    public int   spellDamage()    { return hasCharm(CharmType.VOID_HEART) ? 3 : 2; }


    public boolean isShadowDashing() { return dashTimer > 0f && hasCharm(CharmType.SHARP_SHADOW); }


    @Override public Vector2   getPosition() { return position; }
    @Override public Vector2   getVelocity() { return velocity; }
    @Override public Rectangle getBounds()   { return bounds(); }
    @Override public boolean   isOnGround()  { return onGround; }
    @Override public void      setOnGround(boolean onGround) { this.onGround = onGround; }

    @Override public boolean   isAffectedByGravity() { return dashTimer <= 0f; }


    @Override public float gravityScale() {
        if (isWallSliding) return RISE_GRAVITY_SCALE;
        return (velocity.y > 0f && jumpHeld) ? RISE_GRAVITY_SCALE : FALL_GRAVITY_SCALE;
    }

    @Override public float maxFallSpeed() { return KNIGHT_MAX_FALL; }






    public void addSoul(int amount) {
        soul = clamp(soul + amount, 0, MAX_SOUL);
    }


    public boolean spendSoul(int amount) {
        if (soul < amount) return false;
        soul -= amount;
        return true;
    }


    public void loseMasks(int amount) {
        if (!isAlive() || isInvincible() || amount <= 0) return;


        boolean unbreakable = hasCharm(CharmType.UNBREAKABLE_STRENGTH);
        if (unbreakable && amount >= 2) amount--;
        masks = clamp(masks - amount, 0, maxMasks);
        if (!isAlive()) {
            deathTimer  = DEATH_DURATION;
            hurtTimer   = 0f;
            iframeTimer = 0f;
        } else {
            iframeTimer = IFRAME_DURATION * (unbreakable ? 1.35f : 1f);
            hurtTimer   = HURT_DURATION;
        }
    }


    public void gainMasks(int amount) {
        masks = clamp(masks + amount, 0, maxMasks);
    }


    public void fullHeal() {
        masks = maxMasks;
    }


    public void refreshAirAbilities() {
        airJumpsRemaining = MAX_AIR_JUMPS;
        dashCooldownTimer = 0f;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }
}
