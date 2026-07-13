package hk.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

import hk.model.SettingsModel;


public class AudioService implements Disposable {


    public static final String UI_MOVE     = "ui_move";
    public static final String UI_CONFIRM  = "ui_confirm";
    public static final String UI_BACK     = "ui_back";
    public static final String UI_SAVE     = "ui_save";
    public static final String NAIL_SLASH  = "nail";
    public static final String PLAYER_HURT = "damage";
    public static final String ENEMY_HURT  = "enemy_hurt";
    public static final String ENEMY_DEATH = "enemy_death";
    public static final String SOUL_GAIN   = "soul";
    public static final String FOCUS_HEAL  = "focus";
    public static final String FOCUS_START = "focus_start";
    public static final String WALL_BREAK  = "wall_break";
    public static final String WALL_HIT    = "wall_hit";
    public static final String ZOTE_GROWL  = "zote";


    public static final String PLAYER_JUMP        = "jump";
    public static final String PLAYER_DOUBLE_JUMP = "double_jump";
    public static final String PLAYER_WALL_JUMP   = "wall_jump";
    public static final String PLAYER_WALL_SLIDE  = "wall_slide";
    public static final String PLAYER_LAND        = "land";
    public static final String PLAYER_DASH        = "dash";
    public static final String PLAYER_DEATH       = "player_death";
    public static final String SPELL_VENGEFUL_SPIRIT = "spell_vs";
    public static final String SPELL_HOWLING_WRAITHS  = "spell_hw";


    public static final String CHARM_EQUIP        = "charm_equip";
    public static final String CHARM_UNEQUIP      = "charm_unequip";
    public static final String ACHIEVEMENT_UNLOCK = "achievement";


    public static final String BOSS_HURT   = "boss_hurt";
    public static final String BOSS_SLAM   = "boss_slam";
    public static final String BOSS_CHARGE = "boss_charge";
    public static final String BOSS_LEAP   = "boss_leap";
    public static final String BOSS_LAND   = "boss_land";
    public static final String BOSS_STUN   = "boss_stun";
    public static final String BOSS_PHASE2 = "boss_phase2";
    public static final String BOSS_DEATH  = "boss_death";


    public static final String MUSIC_MENU    = "menu";
    public static final String MUSIC_VICTORY = "victory";

    private static final String SFX_DIR   = "audio/sfx/";
    private static final String MUSIC_DIR = "audio/music/";
    private static final String[] EXTENSIONS = { ".ogg", ".mp3", ".wav" };

    private static final float CROSSFADE_SECONDS = 1.5f;

    private final SettingsModel settings;


    private final Map<String, Sound> sounds = new HashMap<>();


    private Music currentMusic, fadingOutMusic;
    private String currentMusicName;
    private float fade = 1f;

    public AudioService(SettingsModel settings) {
        this.settings = settings;
    }




    public void playSfx(String name) {
        if (!settings.sfxEnabled || settings.sfxVolume <= 0f) return;
        Sound sound = soundFor(name);
        if (sound != null) sound.play(settings.sfxVolume);
    }


    public void playSfxVariant(String name, int count) {
        if (!settings.sfxEnabled || settings.sfxVolume <= 0f) return;
        int pick = 1 + (int) (Math.random() * count);
        Sound sound = soundFor(name + "_" + pick);
        if (sound == null) sound = soundFor(name);
        if (sound != null) sound.play(settings.sfxVolume);
    }


    private Sound soundFor(String name) {
        if (sounds.containsKey(name)) return sounds.get(name);
        Sound sound = loadSound(name);
        sounds.put(name, sound);
        return sound;
    }




    public void playMusic(String name) {
        if (name != null && name.equals(currentMusicName)) return;


        if (fadingOutMusic != null) {
            fadingOutMusic.stop();
            fadingOutMusic.dispose();
        }
        fadingOutMusic = currentMusic;

        currentMusicName = name;
        currentMusic = name == null ? null : loadMusic(name);
        fade = 0f;
        if (currentMusic != null) {
            currentMusic.setLooping(true);
            currentMusic.setVolume(0f);
            currentMusic.play();
        }
    }


    public void stopMusic() {
        playMusic(null);
    }


    public void update(float delta) {
        if (fade < 1f) {
            fade = Math.min(1f, fade + delta / CROSSFADE_SECONDS);
        }
        float base = settings.musicVolume;
        if (currentMusic != null) {
            currentMusic.setVolume(base * fade);
        }
        if (fadingOutMusic != null) {
            if (fade >= 1f) {
                fadingOutMusic.stop();
                fadingOutMusic.dispose();
                fadingOutMusic = null;
            } else {
                fadingOutMusic.setVolume(base * (1f - fade));
            }
        }
    }



    private Sound loadSound(String name) {
        FileHandle file = resolve(SFX_DIR, name);
        return file == null ? null : Gdx.audio.newSound(file);
    }

    private Music loadMusic(String name) {
        FileHandle file = resolve(MUSIC_DIR, name);
        return file == null ? null : Gdx.audio.newMusic(file);
    }

    private FileHandle resolve(String dir, String name) {
        for (String ext : EXTENSIONS) {
            FileHandle file = Gdx.files.internal(dir + name + ext);
            if (file.exists()) return file;
        }
        Gdx.app.log("AudioService", "no audio file for '" + dir + name + "' — staying silent");
        return null;
    }

    @Override
    public void dispose() {
        for (Sound s : sounds.values()) {
            if (s != null) s.dispose();
        }
        sounds.clear();
        if (currentMusic != null) { currentMusic.dispose(); currentMusic = null; }
        if (fadingOutMusic != null) { fadingOutMusic.dispose(); fadingOutMusic = null; }
    }
}
