package hk.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

import hk.model.world.EnemySpawn;
import hk.model.world.LevelModel;
import hk.model.world.PropSpawn;


public class TiledLevelLoader implements Disposable {


    private static final String SOLIDS_LAYER = "solids";


    private static final String FOREGROUND_LAYER = "foreground";

    private static final String HAZARDS_LAYER    = "hazards";
    private static final String ENEMIES_LAYER    = "enemies";
    private static final String BOSS_LAYER       = "boss";
    private static final String NPC_LAYER        = "npc";
    private static final String BREAKABLES_LAYER = "breakables";
    private static final String SECRET_LAYER     = "secret";
    private static final String PICKUPS_LAYER    = "pickups";
    private static final String PROPS_LAYER      = "props";

    private TiledMap map;
    private final List<Rectangle>  solids       = new ArrayList<>();
    private final List<Rectangle>  hazards      = new ArrayList<>();
    private final List<Rectangle>  breakables   = new ArrayList<>();
    private final List<Rectangle>  secretRooms  = new ArrayList<>();
    private final List<EnemySpawn> pickupSpawns = new ArrayList<>();
    private final List<hk.model.world.PropSpawn> propSpawns = new ArrayList<>();
    private final List<EnemySpawn> enemySpawns  = new ArrayList<>();
    private final List<EnemySpawn> npcSpawns    = new ArrayList<>();
    private EnemySpawn             bossSpawn    = null;
    private int[] backgroundLayers = new int[0];
    private int[] foregroundLayers = new int[0];
    private float mapWidthUnits  = 0f;
    private float mapHeightUnits = 0f;


    public void loadMap(String path, float unitScale) {
        if (map != null) map.dispose();
        solids.clear();
        hazards.clear();
        breakables.clear();
        secretRooms.clear();
        pickupSpawns.clear();
        propSpawns.clear();
        enemySpawns.clear();
        npcSpawns.clear();
        bossSpawn = null;

        map = new TmxMapLoader().load(path);

        MapProperties mp = map.getProperties();
        Integer w  = mp.get("width", Integer.class);
        Integer h  = mp.get("height", Integer.class);
        Integer tw = mp.get("tilewidth", Integer.class);
        Integer th = mp.get("tileheight", Integer.class);
        if (w != null && tw != null) mapWidthUnits  = w * tw * unitScale;
        if (h != null && th != null) mapHeightUnits = h * th * unitScale;

        loadRectLayer(SOLIDS_LAYER,     unitScale, solids);
        loadRectLayer(HAZARDS_LAYER,    unitScale, hazards);
        loadRectLayer(BREAKABLES_LAYER, unitScale, breakables);
        loadRectLayer(SECRET_LAYER,     unitScale, secretRooms);
        loadEnemySpawnLayer(unitScale);
        loadBossSpawnLayer(unitScale);
        loadNpcSpawnLayer(unitScale);
        loadPickupLayer(unitScale);
        loadPropLayer(unitScale);

        splitLayersAtForeground();
    }


    private void splitLayersAtForeground() {
        List<Integer> background = new ArrayList<>();
        List<Integer> foreground = new ArrayList<>();
        boolean inForeground = false;

        for (int i = 0; i < map.getLayers().size(); i++) {
            MapLayer layer = map.getLayers().get(i);
            if (FOREGROUND_LAYER.equals(layer.getName())) inForeground = true;
            (inForeground ? foreground : background).add(i);
        }

        backgroundLayers = background.stream().mapToInt(Integer::intValue).toArray();
        foregroundLayers = foreground.stream().mapToInt(Integer::intValue).toArray();
    }


    private void loadEnemySpawnLayer(float unitScale) {
        MapLayer layer = map.getLayers().get(ENEMIES_LAYER);
        if (layer == null) {
            Gdx.app.log("TiledLevelLoader", "No '" + ENEMIES_LAYER + "' layer found in map");
            return;
        }
        for (MapObject obj : layer.getObjects()) {
            MapProperties props = obj.getProperties();



            String type = props.get("enemy", String.class);
            if (type == null || type.isEmpty()) type = props.get("type", String.class);
            if (type == null || type.isEmpty()) continue;

            if (type.equalsIgnoreCase("SpawnPoint")) continue;

            float x, y;
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                x = r.x;
                y = r.y;
            } else {
                Float px = props.get("x", Float.class);
                Float py = props.get("y", Float.class);
                if (px == null || py == null) continue;
                x = px;
                y = py;
            }
            Gdx.app.log("TiledLevelLoader", "Enemy spawn: type=" + type + " at (" + (x * unitScale) + ", " + (y * unitScale) + ")");
            enemySpawns.add(new EnemySpawn(x * unitScale, y * unitScale, type.toLowerCase()));
        }
    }


    private void loadBossSpawnLayer(float unitScale) {
        MapLayer layer = map.getLayers().get(BOSS_LAYER);
        if (layer == null) return;
        for (MapObject obj : layer.getObjects()) {
            MapProperties props = obj.getProperties();
            String type = props.get("type", String.class);
            if (type == null) type = props.get("enemy", String.class);
            if (!"falseknight".equalsIgnoreCase(type)) continue;

            float x, y;
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                x = r.x; y = r.y;
            } else {
                Float px = props.get("x", Float.class);
                Float py = props.get("y", Float.class);
                if (px == null || py == null) continue;
                x = px; y = py;
            }
            bossSpawn = new EnemySpawn(x * unitScale, y * unitScale, "falseknight");
            Gdx.app.log("TiledLevelLoader", "Boss spawn: falseknight at ("
                    + (x * unitScale) + ", " + (y * unitScale) + ")");
            return;
        }
    }


    private void loadNpcSpawnLayer(float unitScale) {
        MapLayer layer = map.getLayers().get(NPC_LAYER);
        if (layer == null) {
            Gdx.app.log("TiledLevelLoader", "No '" + NPC_LAYER + "' layer found in map");
            return;
        }
        for (MapObject obj : layer.getObjects()) {
            MapProperties props = obj.getProperties();
            String type = props.get("npc", String.class);
            if (type == null || type.isEmpty()) type = props.get("type", String.class);
            if (type == null || type.isEmpty()) type = props.get("name", String.class);
            if (type == null || type.isEmpty()) continue;

            float x, y;
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                x = r.x; y = r.y;
            } else {
                Float px = props.get("x", Float.class);
                Float py = props.get("y", Float.class);
                if (px == null || py == null) continue;
                x = px; y = py;
            }
            Gdx.app.log("TiledLevelLoader", "NPC spawn: type=" + type + " at ("
                    + (x * unitScale) + ", " + (y * unitScale) + ")");
            npcSpawns.add(new EnemySpawn(x * unitScale, y * unitScale, type.toLowerCase()));
        }
    }


    private void loadPickupLayer(float unitScale) {
        MapLayer layer = map.getLayers().get(PICKUPS_LAYER);
        if (layer == null) return;
        for (MapObject obj : layer.getObjects()) {
            MapProperties props = obj.getProperties();
            String charm = props.get("charm", String.class);
            if (charm == null || charm.isEmpty()) charm = props.get("type", String.class);
            if (charm == null || charm.isEmpty()) continue;

            float x, y;
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                x = r.x; y = r.y;
            } else {
                Float px = props.get("x", Float.class);
                Float py = props.get("y", Float.class);
                if (px == null || py == null) continue;
                x = px; y = py;
            }
            pickupSpawns.add(new EnemySpawn(x * unitScale, y * unitScale, charm.toUpperCase()));
        }
    }


    private void loadPropLayer(float unitScale) {
        MapLayer layer = map.getLayers().get(PROPS_LAYER);
        if (layer == null) return;
        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject rectObj)) continue;
            MapProperties props = obj.getProperties();
            String sprite = props.get("sprite", String.class);
            if (sprite == null || sprite.isEmpty()) continue;

            Rectangle r = rectObj.getRectangle();
            float parallax = parseFloat(props.get("parallax", String.class), 1f);
            propSpawns.add(new hk.model.world.PropSpawn(sprite,
                    r.x * unitScale, r.y * unitScale, r.width * unitScale, r.height * unitScale,
                    parallax,
                    "1".equals(props.get("sway", String.class)),
                    "1".equals(props.get("flicker", String.class)),
                    "1".equals(props.get("fg", String.class))));
        }
    }

    private static float parseFloat(String s, float fallback) {
        if (s == null) return fallback;
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void loadRectLayer(String layerName, float unitScale, List<Rectangle> out) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) return;
        for (MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                out.add(new Rectangle(r.x * unitScale, r.y * unitScale,
                                      r.width * unitScale, r.height * unitScale));
            }
        }
    }


    public List<Rectangle> getSolids() {
        return solids;
    }


    public List<Rectangle> getHazards() {
        return hazards;
    }


    public List<Rectangle> getBreakables() {
        return breakables;
    }


    public List<Rectangle> getSecretRooms() {
        return secretRooms;
    }


    public List<EnemySpawn> getPickupSpawns() {
        return pickupSpawns;
    }


    public List<hk.model.world.PropSpawn> getPropSpawns() {
        return propSpawns;
    }


    public List<EnemySpawn> getEnemySpawns() {
        return enemySpawns;
    }


    public EnemySpawn getBossSpawn() {
        return bossSpawn;
    }


    public List<EnemySpawn> getNpcSpawns() {
        return npcSpawns;
    }


    public TiledMap getMap() {
        return map;
    }


    public float getMapWidthUnits() {
        return mapWidthUnits;
    }


    public float getMapHeightUnits() {
        return mapHeightUnits;
    }


    public int[] getBackgroundLayers() {
        return backgroundLayers;
    }


    public int[] getForegroundLayers() {
        return foregroundLayers;
    }


    public LevelModel toLevelModel() {
        return new LevelModel(solids, hazards, breakables, secretRooms,
                enemySpawns, npcSpawns, pickupSpawns, propSpawns, bossSpawn,
                mapWidthUnits, mapHeightUnits);
    }

    @Override
    public void dispose() {
        if (map != null) {
            map.dispose();
            map = null;
        }
        solids.clear();
    }
}
