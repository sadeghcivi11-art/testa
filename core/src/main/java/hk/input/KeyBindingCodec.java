package hk.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyBindingCodec {

    public ArrayList<String> encode(Map<GameAction, Integer> keys) {
        ArrayList<String> out = new ArrayList<>();
        for (Map.Entry<GameAction, Integer> e : keys.entrySet()) {
            out.add(e.getKey().name() + "=" + e.getValue());
        }
        return out;
    }

    public void decode(List<String> entries, Map<GameAction, Integer> target) {
        if (entries == null) return;
        for (String entry : entries) {
            int sep = entry.indexOf('=');
            if (sep <= 0) continue;
            try {
                GameAction action = GameAction.valueOf(entry.substring(0, sep));
                target.put(action, Integer.parseInt(entry.substring(sep + 1)));
            } catch (IllegalArgumentException ignored) {
                // malformed/unknown entry: skip it, matching original lenient restore behavior
            }
        }
    }
}
