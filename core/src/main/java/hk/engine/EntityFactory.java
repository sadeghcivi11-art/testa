package hk.engine;

public interface EntityFactory<T> {
    T create(float x, float y);
}
