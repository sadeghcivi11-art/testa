package hk.engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public record SpriteQuad(float x, float y, float width, float height) {

    public static SpriteQuad centered(TextureRegion frame, float spriteHeight,
                                      float footCenterX, float footY, boolean flip) {
        float fh = spriteHeight;
        float fw = fh * (frame.getRegionWidth() / (float) frame.getRegionHeight());
        float x  = footCenterX - fw / 2f;
        return flip
                ? new SpriteQuad(x + fw, footY, -fw, fh)
                : new SpriteQuad(x, footY, fw, fh);
    }

    public void draw(SpriteBatch batch, TextureRegion frame) {
        batch.draw(frame, x, y, width, height);
    }
}
