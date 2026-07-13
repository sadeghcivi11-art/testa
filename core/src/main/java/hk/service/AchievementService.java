package hk.service;

import java.util.ArrayList;
import java.util.List;

import hk.model.AppModel;
import hk.model.progress.Achievement;
import hk.model.progress.AchievementId;


public class AchievementService {


    public interface Listener {
        void onAchievementUnlocked(Achievement achievement);
    }

    private final AppModel app;
    private final SaveService saveService;
    private final EventBus eventBus;
    private final List<Listener> listeners = new ArrayList<>();

    public AchievementService(AppModel app, SaveService saveService, EventBus eventBus) {
        this.app = app;
        this.saveService = saveService;
        this.eventBus = eventBus;
    }

    public void unlock(AchievementId id) {
        Achievement achievement = app.achievements.get(id);
        if (achievement.unlocked) return;
        achievement.unlocked = true;
        saveService.saveProfile(app);
        for (Listener l : listeners) l.onAchievementUnlocked(achievement);
        eventBus.publish(new AchievementUnlockedEvent(achievement));
    }

    public boolean isUnlocked(AchievementId id) {
        return app.achievements.get(id).unlocked;
    }

    public void addListener(Listener l)    { if (!listeners.contains(l)) listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }
}
