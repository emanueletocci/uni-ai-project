package it.unisa.diem.ai.torcs.agent;

public class BaseDriver {
    /* Costanti di cambio marcia */
    private static final int[] GEAR_UP = {5000, 6000, 6000, 6500, 7000, 0};
    private static final int[] GEAR_DOWN = {0, 2500, 3000, 3000, 3500, 3500};

    /* Costanti del filtro ABS */
    private static final float[] WHEEL_RADIUS = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    private static final float ABS_SLIP = 2.0f;
    private static final float ABS_RANGE = 3.0f;
    private static final float ABS_MIN_SPEED = 3.0f;

    /* Costanti di frizione */
    private static final float CLUTCH_MAX = 0.5f;
    private static final float CLUTCH_DELTA = 0.05f;
    private static final float CLUTCH_RANGE = 0.82f;
    private static final float CLUTCH_DELTA_TIME = 0.02f;
    private static final float CLUTCH_DELTA_RACED = 10;
    private static final float CLUTCH_DEC = 0.01f;
    private static final float CLUTCH_MAX_MODIFIER = 1.3f;
    private static final float CLUTCH_MAX_TIME = 1.5f;

    /* Costanti di sterzata */
    private static final float STEER_LOCK = (float) Math.PI/4; // 0.785398
    private static final float STEER_SENSITIVITY_OFFSET = 80.0f;
    private static final float WHEEL_SENSITIVITY = 1.0f;


}
