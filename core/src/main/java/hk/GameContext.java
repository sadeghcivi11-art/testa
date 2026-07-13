package hk;

import hk.model.AppModel;
import hk.service.AchievementService;
import hk.service.AudioService;
import hk.service.EventBus;
import hk.service.LocalizationService;
import hk.service.SaveService;

public record GameContext(
        AppModel appModel,
        SaveService saveService,
        LocalizationService localization,
        AchievementService achievements,
        AudioService audio,
        EventBus eventBus
) {
}
