package hk.view.anim;

import hk.engine.AnimationSet;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import hk.model.enemy.CrawlidState;
import hk.model.enemy.CrawlidState.State;
import hk.service.AssetService;


public class CrawlidAnimationSet extends AnimationSet<State> {

    private static final String DIR = "Animations/CrawlidAnimation/";

    public CrawlidAnimationSet(AssetService loader) {
        super(loader, State.class);
        define(State.WALK,  DIR + "Walk.png",       4, 0.10f,                            PlayMode.LOOP);
        define(State.TURN,  DIR + "Turn.png",       2, CrawlidState.TURN_DURATION  / 2f, PlayMode.NORMAL);
        define(State.DYING, DIR + "Death Air.png",  3, CrawlidState.DYING_DURATION / 3f, PlayMode.NORMAL);
        define(State.DEAD,  DIR + "Death Land.png", 2, 0.15f,                            PlayMode.NORMAL);
    }
}
