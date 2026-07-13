package hk.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public final class SqlIO {

    private static final String DB_FILE = "saves/game.db";
    private static final String LIST_DELIMITER = "|";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("sqlite-jdbc driver not on the classpath", e);
        }
    }

    private SqlIO() { }


    public static Connection connect() {
        try {
            FileHandle handle = Gdx.files.local(DB_FILE);
            handle.parent().mkdirs();
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + handle.file().getAbsolutePath());
            ensureSchema(conn);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Could not open save database", e);
        }
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS profile (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    brightness REAL, sfx_volume REAL, music_volume REAL, sfx_enabled INTEGER,
                    language TEXT, menu_theme_index INTEGER,
                    vsync INTEGER, fps_cap_index INTEGER, show_fps INTEGER,
                    keybinds TEXT, unlocked_achievements TEXT
                )""");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS save_slot (
                    slot INTEGER PRIMARY KEY CHECK (slot BETWEEN 1 AND 4),
                    current_map TEXT, initialized INTEGER,
                    player_x REAL, player_y REAL,
                    masks INTEGER, max_masks INTEGER, soul INTEGER,
                    boss_defeated INTEGER, boss_phase INTEGER,
                    owned_charms TEXT, equipped_charms TEXT, defeated_enemy_types TEXT,
                    secret_wall_broken INTEGER, broken_walls TEXT, collected_pickups TEXT,
                    deaths INTEGER, kills INTEGER, play_time_seconds REAL
                )""");
        }
    }


    public static String join(List<String> values) {
        return String.join(LIST_DELIMITER, values);
    }


    public static ArrayList<String> split(String joined) {
        ArrayList<String> out = new ArrayList<>();
        if (joined == null || joined.isEmpty()) return out;
        for (String s : joined.split("\\" + LIST_DELIMITER)) out.add(s);
        return out;
    }
}
