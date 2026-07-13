package hk.model.menu;


public enum MainMenuItem {
    START        ("menu.start"),
    NEW_GAME     ("menu.newgame"),
    SETTINGS     ("menu.settings"),
    GUIDE        ("menu.guide"),
    ACHIEVEMENTS ("menu.achievements"),
    CHEATS       ("menu.cheats"),
    LANGUAGE     ("menu.language"),
    QUIT         ("menu.quit");


    public final String labelKey;

    MainMenuItem(String labelKey) {
        this.labelKey = labelKey;
    }
}
