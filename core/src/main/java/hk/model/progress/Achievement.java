package hk.model.progress;


public class Achievement {

    public final AchievementId id;
    public boolean unlocked = false;

    public Achievement(AchievementId id) {
        this.id = id;
    }
}
