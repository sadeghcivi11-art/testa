package hk.controller;

import java.util.EnumMap;
import java.util.Map;

import hk.input.GameAction;
import hk.service.InputService;


public class ControlDemultiplexer {

    private final InputService input;
    private final Map<GameAction, Control> heldControls    = new EnumMap<>(GameAction.class);
    private final Map<GameAction, Control> pressedControls = new EnumMap<>(GameAction.class);

    public ControlDemultiplexer(InputService input) {
        this.input = input;
    }


    public ControlDemultiplexer onHold(GameAction action, Control control) {
        heldControls.put(action, control);
        return this;
    }


    public ControlDemultiplexer onPress(GameAction action, Control control) {
        pressedControls.put(action, control);
        return this;
    }


    public boolean isDown(GameAction action) {
        return input.isDown(action);
    }


    public boolean isJustPressed(GameAction action) {
        return input.isJustDown(action);
    }


    public void dispatch(float delta) {
        for (Map.Entry<GameAction, Control> e : heldControls.entrySet()) {
            if (input.isDown(e.getKey())) e.getValue().execute(delta);
        }
        for (Map.Entry<GameAction, Control> e : pressedControls.entrySet()) {
            if (input.isJustDown(e.getKey())) e.getValue().execute(delta);
        }
    }
}
