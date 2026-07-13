package hk.model.charm;


public class Charm {

    public final CharmType type;

    public Charm(CharmType type) {
        this.type = type;
    }



    @Override
    public boolean equals(Object o) {
        return o instanceof Charm c && c.type == type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
