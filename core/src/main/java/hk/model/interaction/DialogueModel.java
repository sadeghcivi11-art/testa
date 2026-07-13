package hk.model.interaction;


public class DialogueModel {

    private String   speakerName = "";
    private String[] lines       = null;
    private int      cursor      = 0;

    private final TypewriterReveal reveal = new TypewriterReveal();


    public void start(String speakerName, String... lines) {
        this.speakerName = speakerName;
        this.lines       = lines;
        this.cursor      = 0;
        reveal.reset();
    }


    public boolean isActive() {
        return lines != null && cursor < lines.length;
    }

    public String getSpeakerName() {
        return isActive() ? speakerName : "";
    }

    public String getCurrentLine() {
        return isActive() ? lines[cursor] : null;
    }


    public String getVisibleLine() {
        if (!isActive()) return null;
        return reveal.apply(lines[cursor]);
    }


    public boolean isLineFullyRevealed() {
        return isActive() && reveal.isFullyRevealed(lines[cursor].length());
    }


    public void update(float delta) {
        if (!isActive()) return;
        reveal.advance(delta, lines[cursor].length());
    }


    public void advance() {
        if (!isActive()) return;
        if (!isLineFullyRevealed()) {
            reveal.revealAll(lines[cursor].length());
            return;
        }
        cursor++;
        reveal.reset();
        if (cursor >= lines.length) clear();
    }

    public void clear() {
        lines       = null;
        cursor      = 0;
        reveal.reset();
        speakerName = "";
    }
}
