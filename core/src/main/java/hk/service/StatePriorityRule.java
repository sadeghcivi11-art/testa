package hk.service;

import java.util.function.Predicate;

import hk.model.player.PlayerModel;

public record StatePriorityRule(Predicate<PlayerModel> condition, PlayerModel.State state) {

    public static StatePriorityRule when(Predicate<PlayerModel> condition, PlayerModel.State state) {
        return new StatePriorityRule(condition, state);
    }
}
