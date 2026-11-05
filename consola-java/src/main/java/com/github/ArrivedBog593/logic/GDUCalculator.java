package com.github.ArrivedBog593.logic;

/**
 * Calcula el GDU (Growing Degree Units / Unidades de Calor) de un día.
 * <p>
 * Fórmula:
 *   Tmax_efectiva = min(tmax, tempCap)
 *   Tmin_efectiva = max(tmin, tempBase)
 *   GDU = ((Tmax_efectiva + Tmin_efectiva) / 2) - tempBase
 *   GDU = max(GDU, 0)   →  nunca negativo
 * <p>
 * tempBase: temperatura mínima para que la planta crezca (ej: 10°C para maíz)
 * tempCap:  temperatura máxima efectiva (ej: 30°C — por arriba la planta no crece más rápido)
 */
public class GDUCalculator {

    /**
     * Calcula el GDU del día con temperatura base y cap configurables.
     *
     * @param tmax     temperatura máxima del día (°C)
     * @param tmin     temperatura mínima del día (°C)
     * @param tempBase temperatura base del cultivo (°C)
     * @param tempCap  temperatura cap del cultivo (°C)
     * @return GDU del día (nunca negativo)
     */
    public static double calcular(double tmax, double tmin, double tempBase, double tempCap) {
        double tmaxEfectiva = Math.min(tmax, tempCap);
        double tminEfectiva = Math.max(tmin, tempBase);
        double gdu = ((tmaxEfectiva + tminEfectiva) / 2.0) - tempBase;
        return Math.max(gdu, 0);
    }
}