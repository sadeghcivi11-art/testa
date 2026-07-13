package hk.model.cheat;

import java.util.EnumSet;


public class CheatState {

    private final EnumSet<CheatCode> active = EnumSet.noneOf(CheatCode.class);

    public boolean isActive(CheatCode cheat) {
        return active.contains(cheat);
    }

    public void toggle(CheatCode cheat) {
        if (!active.remove(cheat)) active.add(cheat);
    }
}
