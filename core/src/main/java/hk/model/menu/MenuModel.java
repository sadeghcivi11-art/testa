package hk.model.menu;

import hk.input.GameAction;


public class MenuModel {


    public enum SlotMode { LOAD, NEW_GAME }

    public MenuState state = MenuState.MAIN;
    public int selectedIndex = 0;

    public SlotMode slotMode = SlotMode.LOAD;


    public GameAction awaitingRebind = null;


    public void goTo(MenuState next) {
        state = next;
        selectedIndex = 0;
        awaitingRebind = null;
    }


    public void moveCursor(int delta, int itemCount) {
        if (itemCount <= 0) return;
        selectedIndex = Math.floorMod(selectedIndex + delta, itemCount);
    }
}
