package hk.engine;

import java.util.IdentityHashMap;
import java.util.Map;

public class StateTimeTracker<K, S> {

    private final Map<K, S>     previousStates = new IdentityHashMap<>();
    private final Map<K, Float> stateTimes     = new IdentityHashMap<>();

    public S previousState(K key) {
        return previousStates.get(key);
    }

    public float advance(K key, S state, float delta, boolean keepTimeAcrossTransition) {
        S prev = previousStates.get(key);
        float time = stateTimes.getOrDefault(key, 0f);

        if (state != prev) {
            time = keepTimeAcrossTransition ? time : 0f;
            previousStates.put(key, state);
        } else {
            time += delta;
        }
        stateTimes.put(key, time);
        return time;
    }
}
