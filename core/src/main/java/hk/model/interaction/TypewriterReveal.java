package hk.model.interaction;

public class TypewriterReveal {

    private static final float CHARS_PER_SECOND = 40f;

    private float revealed = 0f;

    public void reset() {
        revealed = 0f;
    }

    public void advance(float delta, int lineLength) {
        revealed = Math.min(lineLength, revealed + CHARS_PER_SECOND * delta);
    }

    public boolean isFullyRevealed(int lineLength) {
        return (int) revealed >= lineLength;
    }

    public void revealAll(int lineLength) {
        revealed = lineLength;
    }

    public String apply(String line) {
        int n = Math.min(line.length(), (int) revealed);
        return line.substring(0, n);
    }
}
