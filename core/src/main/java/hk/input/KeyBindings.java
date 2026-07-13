package hk.input;

import com.badlogic.gdx.Input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class KeyBindings {

    private final EnumMap<GameAction, Integer> keys  = new EnumMap<>(GameAction.class);
    private final KeyBindingCodec               codec = new KeyBindingCodec();

    public KeyBindings() {
        resetDefaults();
    }


    public void resetDefaults() {
        keys.put(GameAction.LEFT,      Input.Keys.LEFT);
        keys.put(GameAction.RIGHT,     Input.Keys.RIGHT);
        keys.put(GameAction.LOOK_DOWN, Input.Keys.DOWN);
        keys.put(GameAction.LOOK_UP,   Input.Keys.UP);
        keys.put(GameAction.JUMP,        Input.Keys.SPACE);
        keys.put(GameAction.DOUBLE_JUMP, Input.Keys.Z);
        keys.put(GameAction.ATTACK,    Input.Keys.X);
        keys.put(GameAction.DASH,      Input.Keys.C);
        keys.put(GameAction.CAST,      Input.Keys.S);
        keys.put(GameAction.FOCUS,     Input.Keys.A);
        keys.put(GameAction.INTERACT,  Input.Keys.E);
        keys.put(GameAction.ADVANCE,   Input.Keys.ENTER);
        keys.put(GameAction.INVENTORY, Input.Keys.I);
        keys.put(GameAction.PAUSE,     Input.Keys.ESCAPE);
    }

    public int keyFor(GameAction action) {
        Integer k = keys.get(action);
        return k == null ? Input.Keys.UNKNOWN : k;
    }


    public void rebind(GameAction action, int keycode) {
        GameAction conflict = actionFor(keycode);
        if (conflict != null && conflict != action) {
            keys.put(conflict, keys.get(action));
        }
        keys.put(action, keycode);
    }


    public GameAction actionFor(int keycode) {
        for (Map.Entry<GameAction, Integer> e : keys.entrySet()) {
            if (e.getValue() == keycode) return e.getKey();
        }
        return null;
    }


    public ArrayList<String> snapshot() {
        return codec.encode(keys);
    }


    public void restore(List<String> entries) {
        codec.decode(entries, keys);
    }
}
