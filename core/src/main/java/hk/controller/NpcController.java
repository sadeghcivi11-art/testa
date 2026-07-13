package hk.controller;

import java.util.List;

import com.badlogic.gdx.math.Rectangle;

import hk.model.interaction.DialogueModel;
import hk.model.interaction.Interactable;
import hk.model.npc.Zote;
import hk.model.world.World;
import hk.physics.CollisionWorld;
import hk.service.enemyai.NpcAIService;


public class NpcController {

    private final World                world;
    private final EntityPhysicsStepper stepper;
    private final NpcAIService         aiService = new NpcAIService();

    private Interactable activeInteractable = null;

    public NpcController(World world, CollisionWorld physics) {
        this.world   = world;
        this.stepper = new EntityPhysicsStepper(physics);
    }

    public void update(float delta, List<Rectangle> solids) {
        updateZotePhysics(delta, solids);
        aiService.update(world.zote, delta, world.player.position.x);

        DialogueModel dm = world.dialogueModel;
        dm.update(delta);

        if (dm.isActive()) {

            if (zoteWasInterrupted()) {
                dm.clear();
                activeInteractable = null;
            } else if (!isPlayerNearActiveNpc()) {
                dm.clear();
                onDialogueEnd();
            }
        } else {
            activeInteractable = findNearestInteractable();
        }
    }


    public void handleInteract() {
        if (!world.dialogueModel.isActive() && activeInteractable != null) {
            activeInteractable.interact(world.dialogueModel);
        }
    }


    public void handleAdvance() {
        DialogueModel dm = world.dialogueModel;
        if (!dm.isActive()) return;
        dm.advance();
        if (!dm.isActive()) onDialogueEnd();
    }


    public boolean hasPrompt() {
        return activeInteractable != null && !world.dialogueModel.isActive();
    }


    public boolean isDialogueOpen() {
        return world.dialogueModel.isActive();
    }

    private boolean zoteWasInterrupted() {
        return activeInteractable instanceof Zote zote && zote.state != Zote.State.TALK;
    }

    private boolean isPlayerNearActiveNpc() {
        return activeInteractable instanceof Zote zote
                && zote.isNearPlayer(world.player.position.x, world.player.position.y);
    }

    private Interactable findNearestInteractable() {
        Zote zote = world.zote;
        if (zote == null || !zote.canInteract()) return null;
        return zote.isNearPlayer(world.player.position.x, world.player.position.y) ? zote : null;
    }

    private void onDialogueEnd() {
        if (activeInteractable instanceof Zote zote) {
            zote.onDialogueEnd();
        }
        activeInteractable = null;
    }

    private void updateZotePhysics(float delta, List<Rectangle> solids) {
        Zote zote = world.zote;
        if (zote == null) return;
        stepper.stepFull(zote, solids, delta);
    }
}
