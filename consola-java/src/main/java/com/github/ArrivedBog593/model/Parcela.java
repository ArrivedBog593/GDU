package com.github.ArrivedBog593.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Representa una parcela o cultivo con toda su configuración.
 * Centraliza los parámetros que antes estaban hardcodeados en Main.
 */
public class Parcela {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String nombre;
    private final double lat;
    private final double lon;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final double tempBase;
    private final double tempCap;      // temperatura máxima efectiva (cap)
    private final double gduObjetivo;  // -1 si no se especificó

    public Parcela(String nombre, double lat, double lon,
                   LocalDate fechaInicio, LocalDate fechaFin,
                   double tempBase, double tempCap, double gduObjetivo) {
        this.nombre       = nombre;
        this.lat          = lat;
        this.lon          = lon;
        this.fechaInicio  = fechaInicio;
        this.fechaFin     = fechaFin;
        this.tempBase     = tempBase;
        this.tempCap      = tempCap;
        this.gduObjetivo  = gduObjetivo;
    }

    // --- Getters ---
    public String     getNombre()      { return nombre; }
    public double     getLat()         { return lat; }
    public double     getLon()         { return lon; }
    public double     getTempBase()    { return tempBase; }
    public double     getTempCap()     { return tempCap; }
    public double     getGduObjetivo() { return gduObjetivo; }
    public boolean    tieneObjetivo()  { return gduObjetivo > 0; }

    /** Devuelve la fecha de inicio en formato yyyy-MM-dd para la API */
    public String getInicioApi() { return fechaInicio.toString(); }

    /** Devuelve la fecha final en formato yyyy-MM-dd para la API */
    public String getFinApi()    { return fechaFin.toString(); }

    /** Devuelve la fecha de inicio en formato legible */
    public String getInicioDisplay() { return fechaInicio.format(FMT); }

    /** Devuelve la fecha final en formato legible */
    public String getFinDisplay()    { return fechaFin.format(FMT); }
}