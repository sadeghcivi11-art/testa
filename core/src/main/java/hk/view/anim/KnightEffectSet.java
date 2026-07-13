package hk.view.anim;

import java.util.EnumMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.player.PlayerModel;
import hk.service.AssetService;


public class KnightEffectSet {

    private static final String DIR = "Effects/";

    public enum Effect { DASH, SLASH, DOWN_SLASH, UP_SLASH, CAST_BLAST, SCREAM, SCREAM_VOID, FOCUS_GET }


    public static class EffectDef {
        public final Animation<TextureRegion> animation;
        public final float w;
        public final float h;
        public final float x;
        public final float y;
        public final boolean isLocked;
        public final boolean facesRight;
        public final boolean foreground;
        public final boolean flipVertical;

        public EffectDef(Animation<TextureRegion> animation,
                         float w, float h, float x, float y, boolean isLocked) {
            this(animation, w, h, x, y, isLocked, false, false);
        }

        public EffectDef(Animation<TextureRegion> animation,
                         float w, float h, float x, float y,
                         boolean isLocked, boolean facesRight, boolean foreground) {
            this(animation, w, h, x, y, isLocked, facesRight, foreground, false);
        }

        public EffectDef(Animation<TextureRegion> animation,
                         float w, float h, float x, float y,
                         boolean isLocked, boolean facesRight, boolean foreground, boolean flipVertical) {
            this.animation    = animation;
            this.w            = w;
            this.h            = h;
            this.x            = x;
            this.y            = y;
            this.isLocked     = isLocked;
            this.facesRight   = facesRight;
            this.foreground   = foreground;
            this.flipVertical = flipVertical;
        }
    }

    private final Map<Effect, EffectDef> effects = new EnumMap<>(Effect.class);

    public KnightEffectSet(AssetService loader) {

        effects.put(Effect.DASH, new EffectDef(
            loader.load(DIR + "Dash Effect.png", 8, PlayerModel.DASH_DURATION / 8f, PlayMode.NORMAL),
             50f,  27f,  -40f,  -4f,  false
        ));

        effects.put(Effect.SLASH, new EffectDef(
            loader.load(DIR + "SlashEffect.png", 6, 0.04f, PlayMode.NORMAL),
            62f, 34f, -28f,  -4f,  true
        ));

        effects.put(Effect.DOWN_SLASH, new EffectDef(
            loader.load(DIR + "DownSlashEffect.png", 6, PlayerModel.DOWN_ATTACK_DURATION / 6f, PlayMode.NORMAL),
            18f, 20f, -6f, -20f, true
        ));

        effects.put(Effect.UP_SLASH, new EffectDef(
            loader.load(DIR + "DownSlashEffect.png", 6, PlayerModel.UP_ATTACK_DURATION / 6f, PlayMode.NORMAL),
            18f, 20f, -6f, PlayerModel.HEIGHT, true, false, false, true
        ));




        effects.put(Effect.CAST_BLAST, new EffectDef(
            loader.load(DIR + "BlastSoul.png", 8, PlayerModel.CAST_DURATION / 8f, PlayMode.NORMAL),
            42f, 40f, -30f, -10f, false, true, true
        ));


        effects.put(Effect.SCREAM, new EffectDef(
            loader.load(DIR + "SoulScream.png", 13, PlayerModel.CAST_DURATION / 13f, PlayMode.NORMAL),
            38f, 40f, (PlayerModel.WIDTH - 38f) / 2f, -2f, true
        ));
        effects.put(Effect.SCREAM_VOID, new EffectDef(
            loader.load(DIR + "ShadowScream.png", 14, PlayerModel.CAST_DURATION / 14f, PlayMode.NORMAL),
            38f, 40f, (PlayerModel.WIDTH - 38f) / 2f, -2f, true
        ));




        effects.put(Effect.FOCUS_GET, new EffectDef(
            loader.load("Animations/KnightAnimations/Focus Get.png", 6, 0.07f, PlayMode.NORMAL),
            46.9f, 25f, (PlayerModel.WIDTH - 46.9f) / 2f, 0.17f, true, false, true
        ));
    }

    public EffectDef get(Effect effect) {
        return effects.get(effect);
    }
}
