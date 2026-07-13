package hk.model.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CharmInventory {


    public interface Listener {
        void onCharmsChanged(CharmInventory inventory);
    }


    public static final int MAX_EQUIPPED = 3;

    private final List<Charm> owned    = new ArrayList<>();
    private final List<Charm> equipped = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();


    public void acquire(Charm charm) {
        if (owned.contains(charm)) return;
        owned.add(charm);
        notifyListeners();
    }


    public boolean equip(Charm charm) {
        if (!canEquip(charm)) return false;
        equipped.add(charm);
        notifyListeners();
        return true;
    }


    public boolean unequip(Charm charm) {
        boolean removed = equipped.remove(charm);
        if (removed) notifyListeners();
        return removed;
    }

    public void addListener(Listener l)    { if (!listeners.contains(l)) listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }

    private void notifyListeners() {
        for (Listener l : listeners) l.onCharmsChanged(this);
    }

    public boolean canEquip(Charm charm) {
        return owned.contains(charm) && !equipped.contains(charm) && !isFull();
    }

    public boolean isEquipped(Charm charm) { return equipped.contains(charm); }


    public boolean isEquippedType(CharmType type) {
        for (Charm c : equipped) {
            if (c.type == type) return true;
        }
        return false;
    }

    public boolean isFull()                { return equipped.size() >= MAX_EQUIPPED; }
    public int     equippedCount()         { return equipped.size(); }
    public int     freeNotches()           { return MAX_EQUIPPED - equipped.size(); }


    public List<Charm> getOwned()    { return Collections.unmodifiableList(owned); }
    public List<Charm> getEquipped() { return Collections.unmodifiableList(equipped); }
}
