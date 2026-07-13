package hk.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.Gdx;

import hk.model.AppModel;
import hk.model.GameData;
import hk.model.player.PlayerModel;
import hk.model.ProfileData;
import hk.model.boss.FalseKnight;
import hk.model.charm.Charm;
import hk.model.charm.CharmType;
import hk.model.world.World;
import hk.util.JsonIO;
import hk.util.SqlIO;


public class SaveService {

    public static final int SLOT_COUNT = 4;

    private static final String LEGACY_PROFILE_PATH = "saves/profile.json";
    private static final String LEGACY_SLOT_PATH     = "saves/slot%d.json";



    public void saveProfile(AppModel app) {
        try {
            writeProfile(app.toProfileData());
        } catch (RuntimeException e) {
            Gdx.app.error("SaveService", "Failed to save profile; settings may not persist", e);
        }
    }

    public void loadProfile(AppModel app) {
        try {
            app.fromProfileData(readProfile());
        } catch (RuntimeException e) {
            Gdx.app.error("SaveService", "Failed to load profile; falling back to defaults", e);
        }
    }




    public GameData loadSlot(int slot) {
        try (Connection conn = SqlIO.connect()) {
            GameData data = querySlot(conn, slot);
            if (data != null) return data;


            GameData legacy = JsonIO.read(GameData.class, legacyPathFor(slot));
            if (legacy != null) writeSlot(conn, slot, legacy);
            return legacy;
        } catch (SQLException e) {
            Gdx.app.error("SaveService", "Could not load slot " + slot, e);
            return null;
        }
    }

    public boolean slotExists(int slot) {
        return loadSlot(slot) != null;
    }

    public void saveSlot(int slot, GameData data) {
        data.saveSlot = slot;
        try (Connection conn = SqlIO.connect()) {
            writeSlot(conn, slot, data);
        } catch (SQLException e) {
            Gdx.app.error("SaveService", "Could not save slot " + slot, e);
        }
    }




    public void snapshot(World world, GameData data) {
        PlayerModel knight = world.player;
        data.initialized = true;
        data.playerX  = knight.position.x;
        data.playerY  = knight.position.y;
        data.masks    = knight.masks;
        data.maxMasks = knight.maxMasks;
        data.soul     = knight.soul;
        if (world.currentMap != null) data.currentMap = world.currentMap;
        if (world.boss != null && world.boss.currentState == FalseKnight.State.DEAD) {
            data.bossDefeated = true;
        }
        data.ownedCharms.clear();
        data.equippedCharms.clear();
        for (Charm c : knight.charms.getOwned())    data.ownedCharms.add(c.type.name());
        for (Charm c : knight.charms.getEquipped()) data.equippedCharms.add(c.type.name());


        data.brokenWalls.clear();
        for (hk.model.world.BreakableWall w : world.breakableWalls) {
            if (w.destroyed) {
                data.brokenWalls.add(wallKey(w));
                data.secretWallBroken = true;
            }
        }
        data.collectedPickups.clear();
        for (hk.model.world.CharmPickup p : world.pickups) {
            if (p.collected) data.collectedPickups.add(p.type.name());
        }
    }


    public void restore(GameData data, World world) {
        PlayerModel knight = world.player;

        if (data.initialized) knight.position.set(data.playerX, data.playerY);
        knight.maxMasks = data.maxMasks;
        knight.masks    = data.masks;

        knight.soul     = Math.min(data.soul, PlayerModel.MAX_SOUL);
        for (String name : data.ownedCharms) {
            try {
                knight.charms.acquire(new Charm(CharmType.valueOf(name)));
            } catch (IllegalArgumentException ignored) {

            }
        }
        for (Charm owned : knight.charms.getOwned()) {
            if (data.equippedCharms.contains(owned.type.name())) knight.charms.equip(owned);
        }



        if (data.secretWallBroken && data.brokenWalls.isEmpty()) data.brokenWalls.add("940,754");
        for (hk.model.world.BreakableWall w : world.breakableWalls) {
            if (!w.destroyed && data.brokenWalls.contains(wallKey(w))) {
                w.destroyed     = true;
                w.hitsRemaining = 0;
                world.solids.remove(w.bounds);
            }
        }
        for (hk.model.world.CharmPickup p : world.pickups) {
            if (data.collectedPickups.contains(p.type.name())) p.collected = true;
        }




        if (data.bossDefeated && world.boss != null) {
            world.boss.health        = 0;
            world.boss.currentState  = FalseKnight.State.DEAD;
            world.boss.lastState     = FalseKnight.State.DEAD;
        }
    }


    private static String wallKey(hk.model.world.BreakableWall w) {
        return Math.round(w.bounds.x) + "," + Math.round(w.bounds.y);
    }



    private void writeProfile(ProfileData d) {
        String sql = """
            INSERT INTO profile (id, brightness, sfx_volume, music_volume, sfx_enabled, language,
                menu_theme_index, vsync, fps_cap_index, show_fps, keybinds, unlocked_achievements)
            VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                brightness=excluded.brightness, sfx_volume=excluded.sfx_volume,
                music_volume=excluded.music_volume, sfx_enabled=excluded.sfx_enabled,
                language=excluded.language, menu_theme_index=excluded.menu_theme_index,
                vsync=excluded.vsync, fps_cap_index=excluded.fps_cap_index, show_fps=excluded.show_fps,
                keybinds=excluded.keybinds, unlocked_achievements=excluded.unlocked_achievements
            """;
        try (Connection conn = SqlIO.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, d.brightness);
            ps.setFloat(2, d.sfxVolume);
            ps.setFloat(3, d.musicVolume);
            ps.setInt(4, d.sfxEnabled ? 1 : 0);
            ps.setString(5, d.language);
            ps.setInt(6, d.menuThemeIndex);
            ps.setInt(7, d.vsync ? 1 : 0);
            ps.setInt(8, d.fpsCapIndex);
            ps.setInt(9, d.showFps ? 1 : 0);
            ps.setString(10, SqlIO.join(d.keybinds));
            ps.setString(11, SqlIO.join(d.unlockedAchievements));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save profile", e);
        }
    }

    private ProfileData readProfile() {
        try (Connection conn = SqlIO.connect()) {
            ProfileData data = queryProfile(conn);
            if (data != null) return data;


            ProfileData legacy = JsonIO.read(ProfileData.class, LEGACY_PROFILE_PATH);
            if (legacy != null) writeProfile(legacy);
            return legacy;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load profile", e);
        }
    }

    private ProfileData queryProfile(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM profile WHERE id = 1")) {
            if (!rs.next()) return null;
            ProfileData d = new ProfileData();
            d.brightness     = rs.getFloat("brightness");
            d.sfxVolume      = rs.getFloat("sfx_volume");
            d.musicVolume    = rs.getFloat("music_volume");
            d.sfxEnabled     = rs.getInt("sfx_enabled") != 0;
            d.language       = rs.getString("language");
            d.menuThemeIndex = rs.getInt("menu_theme_index");
            d.vsync          = rs.getInt("vsync") != 0;
            d.fpsCapIndex    = rs.getInt("fps_cap_index");
            d.showFps        = rs.getInt("show_fps") != 0;
            d.keybinds             = SqlIO.split(rs.getString("keybinds"));
            d.unlockedAchievements = SqlIO.split(rs.getString("unlocked_achievements"));
            return d;
        }
    }

    private void writeSlot(Connection conn, int slot, GameData data) throws SQLException {
        String sql = """
            INSERT INTO save_slot (slot, current_map, initialized, player_x, player_y, masks,
                max_masks, soul, boss_defeated, boss_phase, owned_charms, equipped_charms,
                defeated_enemy_types, secret_wall_broken, broken_walls, collected_pickups,
                deaths, kills, play_time_seconds)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(slot) DO UPDATE SET
                current_map=excluded.current_map, initialized=excluded.initialized,
                player_x=excluded.player_x, player_y=excluded.player_y,
                masks=excluded.masks, max_masks=excluded.max_masks, soul=excluded.soul,
                boss_defeated=excluded.boss_defeated, boss_phase=excluded.boss_phase,
                owned_charms=excluded.owned_charms, equipped_charms=excluded.equipped_charms,
                defeated_enemy_types=excluded.defeated_enemy_types,
                secret_wall_broken=excluded.secret_wall_broken, broken_walls=excluded.broken_walls,
                collected_pickups=excluded.collected_pickups, deaths=excluded.deaths,
                kills=excluded.kills, play_time_seconds=excluded.play_time_seconds
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot);
            ps.setString(2, data.currentMap);
            ps.setInt(3, data.initialized ? 1 : 0);
            ps.setFloat(4, data.playerX);
            ps.setFloat(5, data.playerY);
            ps.setInt(6, data.masks);
            ps.setInt(7, data.maxMasks);
            ps.setInt(8, data.soul);
            ps.setInt(9, data.bossDefeated ? 1 : 0);
            ps.setInt(10, data.bossPhase);
            ps.setString(11, SqlIO.join(data.ownedCharms));
            ps.setString(12, SqlIO.join(data.equippedCharms));
            ps.setString(13, SqlIO.join(data.defeatedEnemyTypes));
            ps.setInt(14, data.secretWallBroken ? 1 : 0);
            ps.setString(15, SqlIO.join(data.brokenWalls));
            ps.setString(16, SqlIO.join(data.collectedPickups));
            ps.setInt(17, data.deaths);
            ps.setInt(18, data.kills);
            ps.setFloat(19, data.playTimeSeconds);
            ps.executeUpdate();
        }
    }

    private GameData querySlot(Connection conn, int slot) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM save_slot WHERE slot = ?")) {
            ps.setInt(1, slot);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                GameData d = new GameData();
                d.saveSlot     = slot;
                d.currentMap   = rs.getString("current_map");
                d.initialized  = rs.getInt("initialized") != 0;
                d.playerX      = rs.getFloat("player_x");
                d.playerY      = rs.getFloat("player_y");
                d.masks        = rs.getInt("masks");
                d.maxMasks     = rs.getInt("max_masks");
                d.soul         = rs.getInt("soul");
                d.bossDefeated = rs.getInt("boss_defeated") != 0;
                d.bossPhase    = rs.getInt("boss_phase");
                d.ownedCharms        = SqlIO.split(rs.getString("owned_charms"));
                d.equippedCharms     = SqlIO.split(rs.getString("equipped_charms"));
                d.defeatedEnemyTypes = SqlIO.split(rs.getString("defeated_enemy_types"));
                d.secretWallBroken   = rs.getInt("secret_wall_broken") != 0;
                d.brokenWalls        = SqlIO.split(rs.getString("broken_walls"));
                d.collectedPickups   = SqlIO.split(rs.getString("collected_pickups"));
                d.deaths           = rs.getInt("deaths");
                d.kills            = rs.getInt("kills");
                d.playTimeSeconds  = rs.getFloat("play_time_seconds");
                return d;
            }
        }
    }

    private String legacyPathFor(int slot) {
        return String.format(LEGACY_SLOT_PATH, slot);
    }
}
