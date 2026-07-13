package hk.view.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import hk.model.world.PropSpawn;

public final class ParallaxBackground {

    private ParallaxBackground() { }

    public static Vector2 offset(OrthographicCamera camera, PropSpawn p) {
        if (p.parallax >= 1f) return new Vector2(0f, 0f);
        float dx = (camera.position.x - (p.x + p.width / 2f)) * (1f - p.parallax);
        float dy = (camera.position.y - (p.y + p.height / 2f)) * (1f - p.parallax) * 0.5f;
        return new Vector2(dx, dy);
    }
}
