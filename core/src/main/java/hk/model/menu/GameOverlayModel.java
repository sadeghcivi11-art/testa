package hk.model.menu;

import hk.input.GameAction;


public class GameOverlayModel {

    public enum Overlay { NONE, PAUSE, PAUSE_CHEATS, SETTINGS, KEYBINDS, INVENTORY, VICTORY }

    public Overlay overlay = Overlay.NONE;
    public int selectedIndex = 0;


    public GameAction awaitingRebind = null;

    public boolean isOpen() { return overlay != Overlay.NONE; }

    public void open(Overlay which) {
        overlay = which;
        selectedIndex = 0;
    }

    public void close() {
        overlay = Overlay.NONE;
        selectedIndex = 0;
    }

    public void moveCursor(int delta, int itemCount) {
        if (itemCount <= 0) return;
        selectedIndex = Math.floorMod(selectedIndex + delta, itemCount);
    }
}
