package com.github.ArrivedBog593.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GDUCalculatorTest {

    private static final double TEMP_BASE = 10.0;
    private static final double TEMP_CAP  = 30.0;
    private static final double DELTA     = 0.001;

    @Test
    void calculaGduNormal() {
        // tmax=25, tmin=15 → media=20 → GDU = 20 - 10 = 10
        double gdu = GDUCalculator.calcular(25, 15, TEMP_BASE, TEMP_CAP);
        assertEquals(10.0, gdu, DELTA);
    }

    @Test
    void aplicaCapEnTmax() {
        // tmax=35 → efectiva=30, tmin=15 → media=22.5 → GDU = 22.5 - 10 = 12.5
        double gdu = GDUCalculator.calcular(35, 15, TEMP_BASE, TEMP_CAP);
        assertEquals(12.5, gdu, DELTA);
    }

    @Test
    void aplicaBaseEnTmin() {
        // tmax=25, tmin=5 → efectiva_min=10, media=17.5 → GDU = 17.5 - 10 = 7.5
        double gdu = GDUCalculator.calcular(25, 5, TEMP_BASE, TEMP_CAP);
        assertEquals(7.5, gdu, DELTA);
    }

    @Test
    void nuncaNegativo() {
        // Día muy frío: tmax=8, tmin=2 → ambas capeadas a base, media=10, GDU = 0
        double gdu = GDUCalculator.calcular(8, 2, TEMP_BASE, TEMP_CAP);
        assertEquals(0.0, gdu, DELTA);
    }

    @Test
    void diasConAmbosLimites() {
        // tmax=35, tmin=2 → tmax_ef=30, tmin_ef=10 → media=20 → GDU=10
        double gdu = GDUCalculator.calcular(35, 2, TEMP_BASE, TEMP_CAP);
        assertEquals(10.0, gdu, DELTA);
    }

    @Test
    void exactamenteEnElCap() {
        // tmax=30, tmin=10 → media=20 → GDU=10
        double gdu = GDUCalculator.calcular(30, 10, TEMP_BASE, TEMP_CAP);
        assertEquals(10.0, gdu, DELTA);
    }
}
