package hk.model.cheat;

import com.badlogic.gdx.Input;


public enum CheatCode {
    INSTA_KILL    (Input.Keys.K),
    NOCLIP        (Input.Keys.N),
    BOSS_TELEPORT (Input.Keys.B),
    GOD_MODE      (Input.Keys.G),
    MAX_SOUL      (Input.Keys.M);


    public final int triggerKey;

    CheatCode(int triggerKey) {
        this.triggerKey = triggerKey;
    }


    public String comboLabel() {
        return "Ctrl + " + Input.Keys.toString(triggerKey);
    }
}
