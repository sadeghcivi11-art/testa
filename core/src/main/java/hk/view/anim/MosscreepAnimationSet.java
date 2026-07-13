package hk.view.anim;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

import hk.model.enemy.concrete.Mosscreep;
import hk.model.enemy.concrete.Mosscreep.State;
import hk.service.AssetService;


public class MosscreepAnimationSet {

    private static final String DIR = "Animations/MosquitoAnimation/";

    private final Map<State, Animation<TextureRegion>> animations = new EnumMap<>(State.class);

    public MosscreepAnimationSet(AssetService loader) {
        animations.put(State.FLY,     loader.load(DIR + "Idle.png",              8, 0.09f, PlayMode.LOOP));
        animations.put(State.RECOVER, loader.load(DIR + "Idle.png",              8, 0.09f, PlayMode.LOOP));
        animations.put(State.STARTLE, loader.load(DIR + "Attack Anticipate.png", 6,
                Mosscreep.STARTLE_DURATION / 6f, PlayMode.NORMAL));
        animations.put(State.DIVE,    loader.load(DIR + "Attack.png",           3, 0.05f, PlayMode.LOOP));
        animations.put(State.DYING,   loader.load(DIR + "Death Air.png",        3,
                Mosscreep.DYING_DURATION / 3f, PlayMode.NORMAL));
        animations.put(State.DEAD,    loader.load(DIR + "Death Land.png",       2, 0.15f, PlayMode.NORMAL));
    }

    public Animation<TextureRegion> get(State state) {
        return animations.get(state);
    }
}
