package hk.model.menu;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


public class MenuLayout {


    public enum Kind {
        ROW,
        SLIDER,
        BACK
    }

    public static class Hit {
        public final Kind kind;
        public final int index;
        public final Rectangle rect;

        Hit(Kind kind, int index, Rectangle rect) {
            this.kind = kind;
            this.index = index;
            this.rect = rect;
        }
    }

    private final Array<Hit> hits = new Array<>();


    public void clear() {
        hits.clear();
    }


    public void add(Kind kind, int index, float x, float y, float width, float height) {
        hits.add(new Hit(kind, index, new Rectangle(x, y, width, height)));
    }


    public Hit find(Kind kind, int index) {
        for (Hit hit : hits) {
            if (hit.kind == kind && hit.index == index) return hit;
        }
        return null;
    }


    public Hit at(float x, float y) {
        for (int i = hits.size - 1; i >= 0; i--) {
            if (hits.get(i).rect.contains(x, y)) return hits.get(i);
        }
        return null;
    }
}
