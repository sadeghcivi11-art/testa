package hk.view.anim;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.player.PlayerModel;
import hk.service.AssetService;


public class KnightAnimationSet {

    private static final String DIR = "Animations/KnightAnimations/";

    private final Map<PlayerModel.State, Animation<TextureRegion>> animations =
            new EnumMap<>(PlayerModel.State.class);


    private final Animation<TextureRegion> shadowDash;

    public KnightAnimationSet(AssetService loader) {
        animations.put(PlayerModel.State.IDLE,       loader.load(DIR + "Idle.png",          9,  0.10f, PlayMode.LOOP));
        animations.put(PlayerModel.State.RUN, loader.loadFrames(DIR + "Run.png", 13,
                new int[]{12, 11, 10, 9, 8, 7, 6, 5, 4}, 0.10f, PlayMode.LOOP));
        Animation<TextureRegion> airborne = loader.load(DIR + "Airborne.png", 12, 0.10f, PlayMode.LOOP_PINGPONG);
        animations.put(PlayerModel.State.JUMP, airborne);
        animations.put(PlayerModel.State.FALL, airborne);

        animations.put(PlayerModel.State.DASH,       loader.load(DIR + "Dash.png", 12, PlayerModel.DASH_DURATION / 12f, PlayMode.NORMAL));

        animations.put(PlayerModel.State.ATTACK,      loader.load(DIR + "Slash.png",        5,  PlayerModel.ATTACK_DURATION / 5f, PlayMode.NORMAL));
        animations.put(PlayerModel.State.DOWN_ATTACK, loader.load(DIR + "DownSlash.png",   5,  PlayerModel.DOWN_ATTACK_DURATION / 5f, PlayMode.NORMAL));
        animations.put(PlayerModel.State.UP_ATTACK,   loader.load(DIR + "UpSlash.png",     5,  PlayerModel.UP_ATTACK_DURATION / 5f,   PlayMode.NORMAL));
        animations.put(PlayerModel.State.DOUBLE_JUMP, loader.load(DIR + "Double Jump.png",  8,  PlayerModel.DOUBLE_JUMP_DURATION / 8f,  PlayMode.NORMAL));
        animations.put(PlayerModel.State.FOCUS_START, loader.load(DIR + "Focus Start.png",  3,  PlayerModel.FOCUS_START_DURATION / 3f,   PlayMode.NORMAL));
        animations.put(PlayerModel.State.FOCUS,      loader.load(DIR + "Focus.png",         4,  0.10f,                              PlayMode.LOOP));
        animations.put(PlayerModel.State.FOCUS_END,  loader.load(DIR + "Focus End.png",     3,  PlayerModel.FOCUS_END_DURATION / 3f,     PlayMode.NORMAL));
        animations.put(PlayerModel.State.WALL_SLIDE, loader.load(DIR + "Wall Slide.png",    4,  0.10f,                              PlayMode.LOOP));
        animations.put(PlayerModel.State.CAST,       loader.load(DIR + "Fireball Cast.png", 9,  PlayerModel.CAST_DURATION / 9f,          PlayMode.NORMAL));
        animations.put(PlayerModel.State.SCREAM,     loader.load(DIR + "Scream.png",        7,  PlayerModel.CAST_DURATION / 7f,          PlayMode.NORMAL));
        animations.put(PlayerModel.State.HURT,       loader.load(DIR + "Idle Hurt.png",     12, PlayerModel.HURT_DURATION / 12f,         PlayMode.NORMAL));
        animations.put(PlayerModel.State.DEAD,       loader.load(DIR + "Death.png",         18, PlayerModel.DEATH_DURATION / 18f,        PlayMode.NORMAL));
        animations.put(PlayerModel.State.GET_UP,     loader.load(DIR + "GetUpToIdle.png",   14, PlayerModel.GET_UP_DURATION / 14f,       PlayMode.NORMAL));

        shadowDash = loader.load(DIR + "ShadowDash.png", 11, PlayerModel.DASH_DURATION / 11f, PlayMode.NORMAL);
    }

    public Animation<TextureRegion> get(PlayerModel.State state) {
        return animations.get(state);
    }

    public Animation<TextureRegion> getShadowDash() {
        return shadowDash;
    }
}
