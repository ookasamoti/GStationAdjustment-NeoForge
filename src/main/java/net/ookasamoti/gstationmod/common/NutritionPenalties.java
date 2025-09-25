package net.ookasamoti.gstationmod.common;

public final class NutritionPenalties {

    public record Penalty(
            int   nutrDelta,
            float hungerSatMult,
            float speedMult,
            boolean zeroSatisfaction,
            boolean eatBlocked
    ) {}

    private static final int  T_BLOCK_EAT     = 100;
    private static final int  T_NUTR_MINUS1   = 40;
    private static final int  T_ZERO_SATISF   = 50;
    private static final int  T_HS_HALF       = 60;
    private static final int  T_NUTR_MINUS2A  = 70;
    private static final int  T_SPEED_X2      = 80;
    private static final int  T_NUTR_MINUS2B  = 90;

    private static final float MULT_SPEED_12  = 1.2f;
    private static final float MULT_SPEED_20  = 2.0f;
    private static final float MULT_HS_HALF   = 0.5f;

    public static Penalty forSatiety(int s) {
        int   nutrDelta     = 0;
        float hungerSatMult = 1.0f;
        float speedMult     = 1.0f;
        boolean zeroSatisf  = false;
        boolean blocked     = false;

        if (s > T_NUTR_MINUS1) {
            nutrDelta -= 1;
            speedMult *= MULT_SPEED_12;
        }
        if (s > T_ZERO_SATISF) {
            zeroSatisf = true;
        }
        if (s > T_HS_HALF) {
            hungerSatMult *= MULT_HS_HALF;
        }
        if (s > T_NUTR_MINUS2A) {
            nutrDelta -= 2;
        }
        if (s > T_SPEED_X2) {
            speedMult *= MULT_SPEED_20;
        }
        if (s > T_NUTR_MINUS2B) {
            nutrDelta -= 2;
        }
        if (s >= T_BLOCK_EAT) {
            blocked = true;
        }

        return new Penalty(nutrDelta, hungerSatMult, speedMult, zeroSatisf, blocked);
    }

    private NutritionPenalties() {}
}
