package hk.controller;

import hk.model.GameData;


public interface AppNavigator {


    void startSession(int slot, GameData data);


    void showMenu();


    void quit();
}
