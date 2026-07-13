package hk.view.anim;

import hk.engine.AnimationSet;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.boss.FalseKnight;
import hk.model.boss.FalseKnight.State;
import hk.service.AssetService;


public class FalseKnightAnimationSet extends AnimationSet<State> {

    private static final String DIR = "Animations/FalseKnightAnimation/";

    private static final float IDLE_FRAME = 0.08f;

    private static final int MACE_ANTIC_FRAMES     = 6;
    private static final int MACE_SLAM_FRAMES      = 3;
    private static final int ATTACK_RECOVER_FRAMES = 5;
    private static final int JUMP_FRAMES           = 4;
    private static final int STUNNED_FRAMES        = 5;
    private static final int STUN_RECOVER_FRAMES   = 6;

    private final TextureRegion shockwaveRegion;

    public FalseKnightAnimationSet(AssetService loader) {
        super(loader, State.class);
        shockwaveRegion = loader.load("Effects/Shockwave_002.png", 1, 1f, PlayMode.NORMAL).getKeyFrame(0f);

        define(State.IDLE,            DIR + "Idle.png",           5, IDLE_FRAME, PlayMode.LOOP);
        define(State.RUNNING_CHARGE,  DIR + "Run.png",            5, 0.10f,      PlayMode.LOOP);

        define(State.MACE_ANTIC,      DIR + "Attack Antic.png",   MACE_ANTIC_FRAMES,
                FalseKnight.MACE_ANTIC_DURATION / MACE_ANTIC_FRAMES, PlayMode.NORMAL);
        define(State.MACE_SLAM,       DIR + "Attack.png",         MACE_SLAM_FRAMES,
                FalseKnight.MACE_SLAM_DURATION / MACE_SLAM_FRAMES, PlayMode.NORMAL);
        define(State.ATTACK_RECOVER,  DIR + "Attack Recover.png", ATTACK_RECOVER_FRAMES,
                FalseKnight.ATTACK_RECOVER_DURATION / ATTACK_RECOVER_FRAMES, PlayMode.NORMAL);

        define(State.LEAP_ANTIC,      DIR + "Jump Antic.png",     3,
                FalseKnight.LEAP_ANTIC_DURATION / 5f, PlayMode.NORMAL);
        define(State.OFFENSIVE_LEAP,  DIR + "Jump.png",           JUMP_FRAMES, 0.10f, PlayMode.LOOP);
        define(State.DEFENSIVE_LEAP,  DIR + "Jump.png",           JUMP_FRAMES, 0.10f, PlayMode.LOOP);
        define(State.JUMP_ATTACK,     DIR + "Jump Attack.png",    8,
                FalseKnight.JUMP_ATTACK_DURATION / 8f, PlayMode.NORMAL);

        define(State.STUNNED,         DIR + "Body.png",           STUNNED_FRAMES, 0.15f, PlayMode.LOOP);
        define(State.LANDING,         DIR + "Land.png",           5,
                FalseKnight.LANDING_DURATION / 5f, PlayMode.NORMAL);
        define(State.STUN_RECOVER,    DIR + "Stun Recover.png",   STUN_RECOVER_FRAMES,
                FalseKnight.STUN_RECOVER_DURATION / STUN_RECOVER_FRAMES, PlayMode.NORMAL);
        define(State.DEAD,            DIR + "Body.png",           STUNNED_FRAMES, 0.15f, PlayMode.LOOP);
    }

    public TextureRegion getShockwaveRegion() { return shockwaveRegion; }
}
