package hk.controller.enemy;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.model.player.PlayerModel;
import hk.model.enemy.CrawlidState;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.FlyingEnemy;
import hk.model.enemy.GroundEnemy;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.HuskHornhead;
import hk.model.enemy.concrete.Mosscreep;
import hk.model.world.Hazard;
import hk.physics.CollisionWorld;
import hk.service.TimerService;
import hk.service.enemyai.CrystalGuardianAIService;
import hk.service.enemyai.FlyingEnemyAIService;
import hk.service.enemyai.GroundEnemyAIService;
import hk.service.enemyai.HuskHornheadAIService;
import hk.service.enemyai.MosscreepAIService;


public class EnemyController {

    private final CollisionWorld physics;
    private final TimerService  timers;


    private final Map<Class<?>, GroundEnemyAIService> aiByType       = new HashMap<>();
    private final Map<Class<?>, FlyingEnemyAIService> flyingAiByType = new HashMap<>();



    private final List<Rectangle> hazardRects = new ArrayList<>();
    private List<Hazard>          hazardSource;

    public EnemyController(TimerService timers) {
        this.timers  = timers;
        this.physics = new CollisionWorld();
        aiByType.put(CrawlidState.class,    new CrawlidController());
        aiByType.put(HuskHornhead.class,    new HuskHornheadAIService());
        aiByType.put(CrystalGuardian.class, new CrystalGuardianAIService());
        flyingAiByType.put(Mosscreep.class, new MosscreepAIService());
    }

    public void update(List<EnemyModel> enemies, List<Rectangle> solids, List<Hazard> hazards,
                       PlayerModel knight, float scaledDelta) {
        if (hazards != hazardSource || hazards.size() != hazardRects.size()) {
            hazardRects.clear();
            for (Hazard h : hazards) hazardRects.add(h.bounds);
            hazardSource = hazards;
        }
        for (EnemyModel e : enemies) {
            if (e instanceof GroundEnemy ge) {
                GroundEnemyAIService ai = aiByType.get(e.getClass());
                if (ai == null) continue;

                ai.step(ge, solids, hazardRects, knight, timers, scaledDelta);


                if (ge.knockbackTimer > 0f) ge.velocity.x = ge.knockbackVx;
                physics.applyGravity(ge, scaledDelta);
                physics.moveX(ge, scaledDelta);
                physics.resolveX(ge, solids);
                physics.moveY(ge, scaledDelta);
                physics.resolveY(ge, solids);
            } else if (e instanceof FlyingEnemy fe) {
                FlyingEnemyAIService ai = flyingAiByType.get(e.getClass());
                if (ai == null) continue;

                ai.step(fe, solids, knight, timers, scaledDelta);

                if (fe.knockbackTimer > 0f) fe.velocity.x = fe.knockbackVx;
                physics.applyGravity(fe, scaledDelta);
                physics.moveX(fe, scaledDelta);
                physics.resolveX(fe, solids);
                physics.moveY(fe, scaledDelta);
                physics.resolveY(fe, solids);
            }
        }
    }

    public void notifyHit(EnemyModel e) {
        GroundEnemyAIService ai = aiByType.get(e.getClass());
        if (ai != null && e instanceof GroundEnemy ge) ai.notifyHit(ge);
    }
}
