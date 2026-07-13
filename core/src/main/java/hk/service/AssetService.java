package hk.service;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;


public class AssetService implements Disposable {

    private final Map<String, Texture> textures = new HashMap<>();


    public Animation<TextureRegion> load(String path, int frameCount, float frameDuration,
                                         Animation.PlayMode mode) {
        Texture sheet = texture(path);
        int frameWidth  = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        return build(sheet, frameWidth, frameHeight, frameDuration, mode);
    }


    public Animation<TextureRegion> loadRange(String path, int totalFrames, int firstFrame,
                                              int lastFrame, float frameDuration,
                                              Animation.PlayMode mode) {
        int[] indices = new int[Math.max(0, lastFrame - firstFrame + 1)];
        for (int i = 0; i < indices.length; i++) indices[i] = firstFrame + i;
        return loadFrames(path, totalFrames, indices, frameDuration, mode);
    }


    public Animation<TextureRegion> loadFrames(String path, int totalFrames, int[] indices,
                                               float frameDuration, Animation.PlayMode mode) {
        Texture sheet = texture(path);
        int frameWidth  = sheet.getWidth() / totalFrames;
        int frameHeight = sheet.getHeight();
        TextureRegion[] row = TextureRegion.split(sheet, frameWidth, frameHeight)[0];
        Array<TextureRegion> frames = new Array<>();
        for (int i : indices) {
            if (i >= 0 && i < row.length) frames.add(row[i]);
        }
        return new Animation<>(frameDuration, frames, mode);
    }


    public Animation<TextureRegion> loadGrid(String path, int cols, int rows, float frameDuration,
                                             Animation.PlayMode mode) {
        Texture sheet = texture(path);
        int frameWidth  = sheet.getWidth()  / cols;
        int frameHeight = sheet.getHeight() / rows;
        return build(sheet, frameWidth, frameHeight, frameDuration, mode);
    }

    private Animation<TextureRegion> build(Texture sheet, int frameWidth, int frameHeight,
                                           float frameDuration, Animation.PlayMode mode) {
        TextureRegion[][] grid = TextureRegion.split(sheet, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>();
        for (TextureRegion[] row : grid) {
            for (TextureRegion cell : row) {
                frames.add(cell);
            }
        }
        return new Animation<>(frameDuration, frames, mode);
    }

    private Texture texture(String path) {
        Texture t = textures.get(path);
        if (t == null) {
            t = new Texture(path);


            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            textures.put(path, t);
        }
        return t;
    }

    @Override
    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
    }
}
