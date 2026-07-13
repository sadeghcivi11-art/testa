package hk.view.anim;

import hk.engine.AnimationSet;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import hk.model.enemy.concrete.HuskHornhead;
import hk.model.enemy.concrete.HuskHornhead.HuskState;
import hk.service.AssetService;


public class HuskHornheadAnimationSet extends AnimationSet<HuskState> {

    private static final String DIR = "Animations/HuskHornheadAnimation/";

    public HuskHornheadAnimationSet(AssetService loader) {
        super(loader, HuskState.class);
        define(HuskState.WALK,       DIR + "Walk.png",              7,  0.10f,                                  PlayMode.LOOP);
        define(HuskState.REST,       DIR + "Idle.png",              6,  0.12f,                                  PlayMode.LOOP);
        define(HuskState.TURN,       DIR + "Turn.png",              2,  HuskHornhead.TURN_DURATION       / 2f,  PlayMode.NORMAL);
        define(HuskState.ANTICIPATE, DIR + "Attack Anticipate.png", 5,  HuskHornhead.ANTICIPATE_DURATION / 5f,  PlayMode.NORMAL);
        define(HuskState.CHARGE,     DIR + "Attack Lunge.png",      12, 0.06f,                                  PlayMode.LOOP);
        define(HuskState.DYING,      DIR + "Death Land.png",        8,  HuskHornhead.DYING_DURATION      / 8f,  PlayMode.NORMAL);
        define(HuskState.DEAD,       DIR + "Death Land.png",        8,  HuskHornhead.DYING_DURATION      / 8f,  PlayMode.NORMAL);
    }
}
