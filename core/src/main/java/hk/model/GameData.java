package hk.model;

import java.util.ArrayList;

import hk.model.player.PlayerModel;


public class GameData {

    public int    saveSlot      = 1;
    public String currentMap    = "maps/crossroads.tmx";


    public boolean initialized = false;


    public float playerX = 100f, playerY = 64f;
    public int   masks    = PlayerModel.DEFAULT_MAX_MASKS;
    public int   maxMasks = PlayerModel.DEFAULT_MAX_MASKS;
    public int   soul     = PlayerModel.MAX_SOUL;


    public boolean bossDefeated = false;
    public int     bossPhase    = 1;


    public ArrayList<String> ownedCharms          = new ArrayList<>();
    public ArrayList<String> equippedCharms       = new ArrayList<>();
    public ArrayList<String> defeatedEnemyTypes   = new ArrayList<>();




    public boolean secretWallBroken = false;
    public ArrayList<String> brokenWalls = new ArrayList<>();
    public ArrayList<String> collectedPickups = new ArrayList<>();


    public int   deaths = 0;
    public int   kills  = 0;
    public float playTimeSeconds = 0f;

    public GameData() { }


    public static GameData newGame() {
        GameData data = new GameData();
        data.ownedCharms.add(hk.model.charm.CharmType.QUICK_FOCUS.name());
        data.ownedCharms.add(hk.model.charm.CharmType.HEAVY_BLOW.name());
        data.ownedCharms.add(hk.model.charm.CharmType.UNBREAKABLE_STRENGTH.name());
        return data;
    }
}
