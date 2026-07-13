package hk.view.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import hk.model.player.PlayerModel;


public class HudRenderer implements Disposable {


    private static final float MARGIN   = 14f;
    private static final float VESSEL_W = 110f;
    private static final float MASK_H   = 42f;
    private static final float MASK_GAP = 6f;


    private static final float ORB_CX_FRAC = 0.30f;
    private static final float ORB_CY_FRAC = 0.42f;
    private static final float ORB_H_FRAC  = 1.32f;


    private static final float BREAK_FRAME_TIME  = 0.08f;
    private static final float REFILL_FRAME_TIME = 0.08f;
    private static final float SHINE_FRAME_TIME  = 0.15f;
    private static final float FLASH_DURATION    = 0.30f;
    private static final float SOUL_LERP_RATE    = 5f;
    private static final float SOUL_GAIN_GLOW_DURATION = 0.35f;

    private static final int FILL_FRAME_FIRST = 237;
    private static final int FILL_FRAME_COUNT = 18;


    private enum SlotPhase { FULL, EMPTY, BREAKING, REFILLING }

    private final SpriteBatch        batch;
    private final OrthographicCamera uiCamera;


    private final Texture vesselTexture;
    private final Texture[] fillTextures = new Texture[FILL_FRAME_COUNT];
    private final Texture breakSheet, shineSheet, refillSheet;
    private final TextureRegion[] breakFrames, shineFrames, refillFrames;


    private SlotPhase[] slotPhase = new SlotPhase[0];
    private float[]     slotTime  = new float[0];
    private int   shownMasks    = -1;
    private float shineClock    = 0f;
    private float flashTimer    = 0f;
    private float displayedSoul = -1f;
    private int   lastKnightSoul = -1;
    private float soulGainGlow   = 0f;

    public HudRenderer() {
        batch    = new SpriteBatch();
        uiCamera = new OrthographicCamera();

        vesselTexture = smooth(new Texture(Gdx.files.internal("HUD/HUD Cln_167.png")));
        for (int i = 0; i < FILL_FRAME_COUNT; i++) {
            fillTextures[i] = smooth(new Texture(Gdx.files.internal("HUD/HUD Cln_" + (FILL_FRAME_FIRST + i) + ".png")));
        }
        breakSheet   = smooth(new Texture(Gdx.files.internal("HUD/BreakHealth.png")));
        shineSheet   = smooth(new Texture(Gdx.files.internal("HUD/FilledHealthShine.png")));
        refillSheet  = smooth(new Texture(Gdx.files.internal("HUD/HealthRefill.png")));
        breakFrames  = sliceRow(breakSheet, 6);
        shineFrames  = sliceRow(shineSheet, 5);
        refillFrames = sliceRow(refillSheet, 5);
    }


    private static Texture smooth(Texture t) {
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }


    private static TextureRegion[] sliceRow(Texture sheet, int frameCount) {
        int fw = sheet.getWidth() / frameCount;
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(sheet, i * fw, 0, fw, sheet.getHeight());
        }
        return frames;
    }

    public void draw(PlayerModel knight, float delta) {
        syncSlots(knight);
        advanceClocks(knight, delta);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        uiCamera.setToOrtho(false, sw, sh);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);

        float vesselH = VESSEL_W * vesselTexture.getHeight() / (float) vesselTexture.getWidth();
        float vesselX = MARGIN;
        float vesselY = sh - MARGIN - vesselH;

        batch.begin();
        drawVessel(vesselX, vesselY, vesselH);
        drawMasks(knight, vesselX + VESSEL_W * 0.78f, vesselY + vesselH - MASK_H);
        batch.end();
    }




    private void syncSlots(PlayerModel knight) {
        if (slotPhase.length != knight.maxMasks) {
            slotPhase = new SlotPhase[knight.maxMasks];
            slotTime  = new float[knight.maxMasks];
            shownMasks = -1;
        }
        if (shownMasks == -1) {
            for (int i = 0; i < slotPhase.length; i++) {
                slotPhase[i] = i < knight.masks ? SlotPhase.FULL : SlotPhase.EMPTY;
            }
            shownMasks = knight.masks;
            return;
        }
        if (knight.masks < shownMasks) {
            for (int i = knight.masks; i < shownMasks; i++) startPhase(i, SlotPhase.BREAKING);
            flashTimer = FLASH_DURATION;
        } else if (knight.masks > shownMasks) {
            for (int i = shownMasks; i < knight.masks; i++) startPhase(i, SlotPhase.REFILLING);
        }
        shownMasks = knight.masks;
    }

    private void startPhase(int slot, SlotPhase phase) {
        slotPhase[slot] = phase;
        slotTime[slot]  = 0f;
    }

    private void advanceClocks(PlayerModel knight, float delta) {
        shineClock += delta;
        if (flashTimer > 0f) flashTimer -= delta;

        for (int i = 0; i < slotPhase.length; i++) {
            slotTime[i] += delta;
            if (slotPhase[i] == SlotPhase.BREAKING && slotTime[i] >= breakFrames.length * BREAK_FRAME_TIME) {
                slotPhase[i] = SlotPhase.EMPTY;
            } else if (slotPhase[i] == SlotPhase.REFILLING && slotTime[i] >= refillFrames.length * REFILL_FRAME_TIME) {
                slotPhase[i] = SlotPhase.FULL;
            }
        }


        if (displayedSoul < 0f) displayedSoul = knight.soul;
        float diff = knight.soul - displayedSoul;
        if (Math.abs(diff) < 0.5f) displayedSoul  = knight.soul;
        else                       displayedSoul += diff * Math.min(1f, SOUL_LERP_RATE * delta);


        if (lastKnightSoul >= 0 && knight.soul > lastKnightSoul) soulGainGlow = SOUL_GAIN_GLOW_DURATION;
        lastKnightSoul = knight.soul;
        if (soulGainGlow > 0f) soulGainGlow = Math.max(0f, soulGainGlow - delta);
    }



    private void drawVessel(float x, float y, float vesselH) {

        float fillH = vesselH * ORB_H_FRAC;
        float orbCx = x + VESSEL_W * ORB_CX_FRAC;
        float orbCy = y + vesselH * ORB_CY_FRAC;

        float frac   = MathUtils.clamp(displayedSoul / PlayerModel.MAX_SOUL, 0f, 1f);
        float scaled = frac * (FILL_FRAME_COUNT - 1);
        int   lo     = MathUtils.clamp((int) scaled, 0, FILL_FRAME_COUNT - 1);
        int   hi     = MathUtils.clamp(lo + 1, 0, FILL_FRAME_COUNT - 1);
        float t      = scaled - lo;

        Texture fillLo = fillTextures[lo];
        float fillW    = fillH * fillLo.getWidth() / (float) fillLo.getHeight();

        batch.draw(vesselTexture, x, y, VESSEL_W, vesselH);


        if (lo == hi || t <= 0.02f) {
            batch.draw(fillLo, orbCx - fillW / 2f, orbCy - fillH / 2f, fillW, fillH);
        } else {
            batch.setColor(1f, 1f, 1f, 1f - t);
            batch.draw(fillLo, orbCx - fillW / 2f, orbCy - fillH / 2f, fillW, fillH);
            batch.setColor(1f, 1f, 1f, t);
            batch.draw(fillTextures[hi], orbCx - fillW / 2f, orbCy - fillH / 2f, fillW, fillH);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        if (soulGainGlow > 0f) {
            float a = soulGainGlow / SOUL_GAIN_GLOW_DURATION;
            float glowH = fillH * (1f + 0.18f * a);
            float glowW = glowH * fillLo.getWidth() / (float) fillLo.getHeight();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(1f, 1f, 0.9f, a * 0.55f);
            batch.draw(fillLo, orbCx - glowW / 2f, orbCy - glowH / 2f, glowW, glowH);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private void drawMasks(PlayerModel knight, float startX, float y) {

        if (flashTimer > 0f) {
            float back = 1f - flashTimer / FLASH_DURATION;
            batch.setColor(1f, 0.25f + 0.75f * back, 0.25f + 0.75f * back, 1f);
        }

        float maskW = MASK_H * breakFrames[0].getRegionWidth() / (float) breakFrames[0].getRegionHeight();
        for (int i = 0; i < slotPhase.length; i++) {
            float x = startX + i * (maskW + MASK_GAP);
            batch.draw(frameFor(i), x, y, maskW, MASK_H);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }


    private TextureRegion frameFor(int slot) {
        switch (slotPhase[slot]) {
            case BREAKING:  return frameAt(breakFrames,  slotTime[slot], BREAK_FRAME_TIME);
            case REFILLING: return frameAt(refillFrames, slotTime[slot], REFILL_FRAME_TIME);
            case EMPTY:     return breakFrames[breakFrames.length - 1];
            case FULL:
            default:
                int idx = (int) ((shineClock / SHINE_FRAME_TIME) + slot) % shineFrames.length;
                return shineFrames[idx];
        }
    }

    private static TextureRegion frameAt(TextureRegion[] frames, float time, float frameTime) {
        int idx = Math.min((int) (time / frameTime), frames.length - 1);
        return frames[idx];
    }

    @Override
    public void dispose() {
        batch.dispose();
        vesselTexture.dispose();
        for (Texture t : fillTextures) t.dispose();
        breakSheet.dispose();
        shineSheet.dispose();
        refillSheet.dispose();
    }
}
