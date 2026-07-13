package hk.engine;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

import hk.service.AssetService;

public abstract class AnimationSet<S extends Enum<S>> {

    protected final AssetService loader;
    private final Map<S, Animation<TextureRegion>> animations;

    protected AnimationSet(AssetService loader, Class<S> stateType) {
        this.loader     = loader;
        this.animations = new EnumMap<>(stateType);
    }

    protected void define(S state, String path, int frameCount, float frameDuration, PlayMode mode) {
        animations.put(state, loader.load(path, frameCount, frameDuration, mode));
    }

    public Animation<TextureRegion> get(S state) {
        return animations.get(state);
    }
}
