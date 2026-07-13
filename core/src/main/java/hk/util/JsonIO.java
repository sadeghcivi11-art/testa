package hk.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;


public final class JsonIO {

    private static final Json JSON = new Json();
    static {
        JSON.setOutputType(JsonWriter.OutputType.json);
    }

    private JsonIO() { }


    public static void write(Object obj, String localPath) {
        FileHandle file = Gdx.files.local(localPath);
        file.writeString(JSON.prettyPrint(obj), false);
    }


    public static <T> T read(Class<T> type, String localPath) {
        FileHandle file = Gdx.files.local(localPath);
        if (!file.exists()) return null;
        return JSON.fromJson(type, file.readString());
    }


    public static boolean exists(String localPath) {
        return Gdx.files.local(localPath).exists();
    }
}
