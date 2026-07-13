package hk.view.anim;

import hk.engine.AnimationSet;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.CrystalGuardian.State;
import hk.service.AssetService;

public class CrystalGuardianAnimationSet extends AnimationSet<State> {

    private static final String DIR    = "Animations/CrystallizedAnimations/";
    private static final String FX_DIR = "Effects/";

    private static final float LOOP_FRAME   = 0.083f;
    private static final float CIRCLE_FRAME = 0.05f;

    private static final int LASER_BODY_FRAMES = 7;
    private static final int LASER_BEAM_FRAMES = 9;
    private static final int DYING_FRAMES      = 3;

    private final Animation<TextureRegion> laserBeam;
    private final Animation<TextureRegion> laserCircle;

    public CrystalGuardianAnimationSet(AssetService loader) {
        super(loader, State.class);
        define(State.IDLE,    DIR + "Idle.png",       5,                 LOOP_FRAME,                                          PlayMode.LOOP);
        define(State.LASER,   DIR + "Shoot.png",      LASER_BODY_FRAMES, CrystalGuardian.LASER_DURATION / LASER_BODY_FRAMES, PlayMode.NORMAL);
        define(State.ENRAGED, DIR + "Run.png",        6,                 LOOP_FRAME,                                          PlayMode.LOOP);
        define(State.DYING,   DIR + "Death Air.png",  DYING_FRAMES,      CrystalGuardian.DYING_DURATION / DYING_FRAMES,       PlayMode.NORMAL);
        define(State.DEAD,    DIR + "Death Land.png", 3,                 LOOP_FRAME,                                          PlayMode.NORMAL);

        laserBeam   = loader.load(FX_DIR + "CrystalLaser.png", LASER_BEAM_FRAMES, CrystalGuardian.LASER_DURATION / LASER_BEAM_FRAMES, PlayMode.LOOP);
        laserCircle = loader.load(FX_DIR + "LaserCircle.png",  11,                CIRCLE_FRAME,                                       PlayMode.LOOP);
    }

    public Animation<TextureRegion> getLaserBeam()   { return laserBeam; }
    public Animation<TextureRegion> getLaserCircle() { return laserCircle; }
}
