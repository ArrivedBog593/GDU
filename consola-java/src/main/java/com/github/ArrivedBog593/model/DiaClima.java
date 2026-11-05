package com.github.ArrivedBog593.model;

/**
 * Representa los datos climáticos y GDU de un día específico.
 * Incluye el GDU acumulado del día (suma desde el inicio hasta este día).
 */
public class DiaClima {

    private final String fecha;
    private final double tmax;
    private final double tmin;
    private double gdu;
    private double gduAcumulado; // <-- nuevo: acumulado hasta este día

    public DiaClima(String fecha, double tmax, double tmin) {
        this.fecha = fecha;
        this.tmax  = tmax;
        this.tmin  = tmin;
    }

    public String getFecha()       { return fecha; }
    public double getTmax()        { return tmax; }
    public double getTmin()        { return tmin; }
    public double getGdu()         { return gdu; }
    public double getGduAcumulado(){ return gduAcumulado; }

    public void setGdu(double gdu)                   { this.gdu = gdu; }
    public void setGduAcumulado(double gduAcumulado) { this.gduAcumulado = gduAcumulado; }
}