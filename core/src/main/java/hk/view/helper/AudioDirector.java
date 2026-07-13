package hk.view.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hk.model.player.PlayerModel;
import hk.model.boss.FalseKnight;
import hk.model.charm.Charm;
import hk.model.enemy.EnemyModel;
import hk.model.world.BreakableWall;
import hk.model.world.World;
import hk.service.AudioService;


public class AudioDirector {

    private final AudioService audio;


    private boolean wasAttacking;
    private boolean wasOnGround;
    private boolean wasDashing;
    private boolean wasWallJumping;
    private boolean wasWallSliding;
    private boolean wasDoubleJumping;
    private boolean wasCasting;
    private boolean wasFocusing;
    private boolean wasAlive;
    private int     lastMasks;
    private int     lastSoul;


    private String  lastDialogueLine;
    private final Map<EnemyModel, Integer> enemyHp = new HashMap<>();
    private final Map<BreakableWall, Integer> wallHits = new HashMap<>();
    private Set<Charm> equippedSnapshot = new HashSet<>();


    private FalseKnight.State lastBossState;
    private int lastBossHealth;
    private int lastBossPhase;

    public AudioDirector(AudioService audio) {
        this.audio = audio;
    }


    public void prime(World world) {
        PlayerModel k = world.player;
        wasAttacking     = k.isAttacking;
        wasOnGround      = k.onGround;
        wasDashing       = k.dashTimer > 0f;
        wasWallJumping   = k.wallJumpLockTimer > 0f;
        wasWallSliding   = k.isWallSliding;
        wasDoubleJumping = k.isDoubleJumping;
        wasCasting       = k.isCasting;
        wasFocusing      = k.isFocusing;
        wasAlive         = k.isAlive();
        lastMasks = k.masks;
        lastSoul  = k.soul;
        lastDialogueLine = world.dialogueModel.getCurrentLine();

        enemyHp.clear();
        for (EnemyModel e : world.enemies) enemyHp.put(e, e.health);
        wallHits.clear();
        for (BreakableWall w : world.breakableWalls) wallHits.put(w, w.hitsRemaining);
        equippedSnapshot = new HashSet<>(k.charms.getEquipped());

        if (world.boss != null) {
            lastBossState  = world.boss.currentState;
            lastBossHealth = world.boss.health;
            lastBossPhase  = world.boss.phase;
        }
    }


    public void update(World world) {
        PlayerModel k = world.player;


        if (k.isAttacking && !wasAttacking) audio.playSfxVariant(AudioService.NAIL_SLASH, 5);
        wasAttacking = k.isAttacking;


        if (k.masks < lastMasks) {
            audio.playSfx(AudioService.PLAYER_HURT);
        } else if (k.masks > lastMasks && k.soul < lastSoul) {
            audio.playSfx(AudioService.FOCUS_HEAL);
        }
        lastMasks = k.masks;


        if (k.soul > lastSoul) audio.playSfxVariant(AudioService.SOUL_GAIN, 3);
        lastSoul = k.soul;


        if (k.isFocusing && !wasFocusing) audio.playSfx(AudioService.FOCUS_START);
        wasFocusing = k.isFocusing;


        boolean onGround = k.onGround;
        if (!wasOnGround && onGround) {
            audio.playSfx(AudioService.PLAYER_LAND);
        } else if (wasOnGround && !onGround && k.velocity.y > 0f) {
            audio.playSfx(AudioService.PLAYER_JUMP);
        }
        wasOnGround = onGround;

        boolean dashing = k.dashTimer > 0f;
        if (dashing && !wasDashing) audio.playSfx(AudioService.PLAYER_DASH);
        wasDashing = dashing;

        boolean wallJumping = k.wallJumpLockTimer > 0f;
        if (wallJumping && !wasWallJumping) audio.playSfx(AudioService.PLAYER_WALL_JUMP);
        wasWallJumping = wallJumping;

        boolean wallSliding = k.isWallSliding;
        if (wallSliding && !wasWallSliding) audio.playSfx(AudioService.PLAYER_WALL_SLIDE);
        wasWallSliding = wallSliding;

        boolean doubleJumping = k.isDoubleJumping;
        if (doubleJumping && !wasDoubleJumping) audio.playSfx(AudioService.PLAYER_DOUBLE_JUMP);
        wasDoubleJumping = doubleJumping;

        boolean casting = k.isCasting;
        if (casting && !wasCasting) {
            audio.playSfx(k.isScreaming ? AudioService.SPELL_HOWLING_WRAITHS : AudioService.SPELL_VENGEFUL_SPIRIT);
        }
        wasCasting = casting;

        boolean alive = k.isAlive();
        if (wasAlive && !alive) audio.playSfx(AudioService.PLAYER_DEATH);
        wasAlive = alive;


        List<Charm> equippedNow = k.charms.getEquipped();
        for (Charm c : equippedNow) {
            if (!equippedSnapshot.contains(c)) audio.playSfx(AudioService.CHARM_EQUIP);
        }
        for (Charm c : equippedSnapshot) {
            if (!equippedNow.contains(c)) audio.playSfx(AudioService.CHARM_UNEQUIP);
        }
        equippedSnapshot = new HashSet<>(equippedNow);


        for (EnemyModel e : world.enemies) {
            Integer prev = enemyHp.get(e);
            if (prev != null && e.health < prev) {
                if (e.health <= 0) audio.playSfxVariant(AudioService.ENEMY_DEATH, 3);
                else               audio.playSfx(AudioService.ENEMY_HURT);
            }
            enemyHp.put(e, e.health);
        }


        for (BreakableWall w : world.breakableWalls) {
            Integer prevHits = wallHits.get(w);
            if (prevHits != null && w.hitsRemaining < prevHits) {
                audio.playSfx(w.destroyed ? AudioService.WALL_BREAK : AudioService.WALL_HIT);
            }
            wallHits.put(w, w.hitsRemaining);
        }


        String line = world.dialogueModel.getCurrentLine();
        if (line != null && !line.equals(lastDialogueLine)) {
            audio.playSfxVariant(AudioService.ZOTE_GROWL, 5);
        }
        lastDialogueLine = line;

        updateBoss(world.boss);
    }

    private void updateBoss(FalseKnight boss) {
        if (boss == null) return;

        if (boss.health < lastBossHealth && boss.currentState != FalseKnight.State.DEAD) {
            audio.playSfx(AudioService.BOSS_HURT);
        }
        lastBossHealth = boss.health;

        if (boss.currentState != lastBossState) {
            switch (boss.currentState) {
                case MACE_SLAM                       -> audio.playSfx(AudioService.BOSS_SLAM);
                case RUNNING_CHARGE                  -> audio.playSfx(AudioService.BOSS_CHARGE);
                case OFFENSIVE_LEAP, DEFENSIVE_LEAP   -> audio.playSfx(AudioService.BOSS_LEAP);
                case JUMP_ATTACK, LANDING             -> audio.playSfx(AudioService.BOSS_LAND);
                case STUNNED                          -> audio.playSfx(AudioService.BOSS_STUN);
                case DEAD                             -> audio.playSfx(AudioService.BOSS_DEATH);
                default -> { }
            }
            lastBossState = boss.currentState;
        }

        if (boss.phase != lastBossPhase) {
            audio.playSfx(AudioService.BOSS_PHASE2);
            lastBossPhase = boss.phase;
        }
    }
}
