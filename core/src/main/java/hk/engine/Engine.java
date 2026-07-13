package hk.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Engine<E extends Entity> {

    public interface Controller<E> {
        void update(E entity, float delta);
    }

    private final List<E> entities = new ArrayList<>();
    private final Controller<E> controller;
    private final BiConsumer<E, Float> renderer;

    public Engine(Controller<E> controller, BiConsumer<E, Float> renderer) {
        this.controller = controller;
        this.renderer   = renderer;
    }

    public void spawn(E entity) {
        entities.add(entity);
    }

    public void update(float delta) {
        for (E e : entities) {
            if (e.isAlive()) controller.update(e, delta);
        }
    }

    public void render(float delta) {
        for (E e : entities) {
            renderer.accept(e, delta);
        }
    }

    public List<E> entities() {
        return entities;
    }
}
