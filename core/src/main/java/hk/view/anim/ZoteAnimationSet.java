package hk.view.anim;

import hk.engine.AnimationSet;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import hk.model.npc.Zote;
import hk.service.AssetService;


public class ZoteAnimationSet extends AnimationSet<Zote.State> {

    private static final String DIR = "Animations/ZoteAnimations/";

    public ZoteAnimationSet(AssetService loader) {
        super(loader, Zote.State.class);
        define(Zote.State.IDLE,   DIR + "Idle.png",   5, 0.12f,  PlayMode.LOOP);
        define(Zote.State.TALK,   DIR + "Talk.png",   5, 0.10f,  PlayMode.LOOP);
        define(Zote.State.FALL,   DIR + "Fall.png",   5, 0.10f,  PlayMode.NORMAL);
        define(Zote.State.GET_UP, DIR + "Get Up.png", 4, 0.12f,  PlayMode.NORMAL);
        define(Zote.State.ATTACK, DIR + "Attack.png", 4, 0.067f, PlayMode.LOOP);
        define(Zote.State.ROLL,   DIR + "Roll.png",   3, 0.10f,  PlayMode.NORMAL);
    }
}
