package hk.service;

import com.badlogic.gdx.Gdx;

import hk.input.GameAction;
import hk.input.KeyBindings;


public class InputService {

    private final KeyBindings bindings;

    public InputService(KeyBindings bindings) {
        this.bindings = bindings;
    }


    public boolean isDown(GameAction action) {
        return Gdx.input.isKeyPressed(bindings.keyFor(action));
    }


    public boolean isJustDown(GameAction action) {
        return Gdx.input.isKeyJustPressed(bindings.keyFor(action));
    }

    public KeyBindings getBindings() {
        return bindings;
    }
}
