package hk.model.npc;

import hk.model.enemy.EnemyStats;
import hk.model.enemy.GroundEnemy;
import hk.model.interaction.DialogueModel;
import hk.model.interaction.Interactable;
import hk.service.ZoteDialogueService;


public class Zote extends GroundEnemy implements Interactable {

    public static final float WIDTH           = 10f;
    public static final float HEIGHT          = 20f;
    public static final float INTERACT_RANGE  = 50f;
    public static final float ANGER_DURATION  = 4f;
    public static final float ATTACK_SPEED    = 45f;
    public static final float CHASE_DEADZONE  = 6f;
    public static final float KNOCKDOWN_SPEED = 40f;
    private static final float DEFAULT_REVIVE_RANGE = 400f;

    public enum State { IDLE, TALK, FALL, GET_UP, ATTACK, ROLL }

    public State   state            = State.IDLE;
    public float   stateTimer       = 0f;
    public float   angerTimer       = 0f;
    public float   knockdownDir     = 0f;
    public boolean exhaustedInitial = false;
    public boolean showingInitial   = false;
    public int     preceptIndex     = 0;

    private final ZoteDialogueService dialogueService;

    public Zote(float x, float y, ZoteDialogueService dialogueService) {
        super(Integer.MAX_VALUE, x, y, 0f, new EnemyStats(WIDTH, HEIGHT, Float.MAX_VALUE, DEFAULT_REVIVE_RANGE));
        this.dialogueService = dialogueService;
    }

    @Override
    public void interact(DialogueModel dm) {
        state = State.TALK;
        if (!exhaustedInitial) {
            showingInitial = true;
            dm.start("Zote", dialogueService.initialDialogue());
        } else {
            showingInitial = false;
            dm.start("Zote", dialogueService.precept(preceptIndex));
            preceptIndex++;
        }
    }


    public void onDialogueEnd() {
        if (showingInitial) exhaustedInitial = true;
        showingInitial = false;
        if (state == State.TALK) state = State.IDLE;
    }


    public boolean isNearPlayer(float knightX, float knightY) {
        float cx = position.x + WIDTH  / 2f;
        float cy = position.y + HEIGHT / 2f;
        float dx = cx - knightX;
        float dy = cy - knightY;
        return dx * dx + dy * dy <= INTERACT_RANGE * INTERACT_RANGE;
    }


    public boolean canInteract() {
        return state == State.IDLE;
    }


    public void onHit(float dir) {
        state        = State.ROLL;
        stateTimer   = 0f;
        knockdownDir = dir;
        angerTimer   = ANGER_DURATION;
    }

    @Override
    public void takeDamage(int amount) {
        onHit(0f);
    }
}
