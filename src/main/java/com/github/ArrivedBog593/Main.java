package com.github.ArrivedBog593;

import com.github.ArrivedBog593.export.ExcelExporter;
import com.github.ArrivedBog593.logic.GDUCalculator;
import com.github.ArrivedBog593.model.DiaClima;
import com.github.ArrivedBog593.model.Parcela;
import com.github.ArrivedBog593.service.ClimaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Coordenadas de Los Mochis, Sinaloa
    private static final double LAT_DEFAULT = 25.7908;
    private static final double LON_DEFAULT = -108.9963;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   CALCULADORA DE GDU — Los Mochis, Sinaloa  ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        try {
            // --- 1. Nombre de la parcela ---
            String nombre = pedirTexto(sc);

            // --- 2. Fechas ---
            LocalDate fechaInicio = pedirFecha(sc, "Fecha de siembra        (dd/MM/yyyy): ", null);
            LocalDate fechaFin    = pedirFecha(sc, "Fecha final del análisis (dd/MM/yyyy): ", fechaInicio);

            // --- 3. Temperatura base ---
            double tempBase = pedirDouble(sc,
                    "Temperatura base del cultivo °C  [Enter = 10]: ", 10.0, 0, 50);

            // --- 4. Temperatura cap ---
            double tempCap = pedirDouble(sc,
                    "Temperatura cap (máx efectiva) °C [Enter = 30]: ", 30.0, tempBase, 60);

            // --- 5. GDU objetivo (opcional) ---
            System.out.print("GDU objetivo para cosecha        [Enter para omitir]: ");
            String objStr = sc.nextLine().trim();
            double gduObjetivo = -1;
            if (!objStr.isEmpty()) {
                try {
                    gduObjetivo = Double.parseDouble(objStr);
                } catch (NumberFormatException e) {
                    System.out.println("  Valor no válido — se omitirá el objetivo.");
                }
            }

            // --- Construir Parcela ---
            Parcela parcela = new Parcela(
                    nombre, LAT_DEFAULT, LON_DEFAULT,
                    fechaInicio, fechaFin,
                    tempBase, tempCap, gduObjetivo
            );

            // --- Descargar datos ---
            System.out.println();
            System.out.println("Descargando datos meteorológicos...");
            List<DiaClima> datos = ClimaService.obtenerDatos(parcela);

            if (datos.isEmpty()) {
                System.out.println("No se obtuvieron datos. Verifica las fechas y tu conexión a internet.");
                return;
            }

            System.out.println("Datos obtenidos: " + datos.size() + " días.");

            // --- Calcular GDU ---
            double acumulado = 0;
            for (DiaClima d : datos) {
                double gdu = GDUCalculator.calcular(d.getTmax(), d.getTmin(), tempBase, tempCap);
                d.setGdu(gdu);
                acumulado += gdu;
                d.setGduAcumulado(acumulado);
            }

            // --- Exportar Excel ---
            System.out.println("Generando Excel...");
            String archivo = ExcelExporter.exportar(datos, parcela);

            // --- Resumen en consola ---
            System.out.println();
            System.out.println("══════════════════════════════════════════");
            System.out.println("  RESUMEN: " + nombre);
            System.out.println("══════════════════════════════════════════");
            System.out.printf("  Período     : %s → %s%n",
                    parcela.getInicioDisplay(), parcela.getFinDisplay());
            System.out.printf("  Días        : %d%n", datos.size());
            System.out.printf("  Temp. base  : %.1f °C%n", tempBase);
            System.out.printf("  Temp. cap   : %.1f °C%n", tempCap);
            System.out.printf("  GDU total   : %.2f%n", acumulado);

            if (gduObjetivo > 0) {
                System.out.printf("  GDU objetivo: %.2f%n", gduObjetivo);
                boolean alcanzado = false;
                for (DiaClima d : datos) {
                    if (d.getGduAcumulado() >= gduObjetivo) {
                        System.out.printf("  ✅ Cosecha estimada: %s%n", d.getFecha());
                        alcanzado = true;
                        break;
                    }
                }
                if (!alcanzado) {
                    System.out.println("  ⏳ El GDU objetivo no se alcanzó en este período.");
                }
            }

            System.out.println("══════════════════════════════════════════");
            System.out.println("  Archivo generado: " + archivo);
            System.out.println("══════════════════════════════════════════");

        } catch (Exception e) {
            System.out.println();
            System.out.println("❌ Error: " + e.getMessage());
            System.out.println("   Verifica tu conexión a internet y el rango de fechas.");
            // Descomenta la siguiente línea para ver el stack trace completo al depurar:
            // e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Métodos de entrada validada
    // ---------------------------------------------------------------

    /** Pide un texto, devuelve el default si el usuario presiona Enter. */
    private static String pedirTexto(Scanner sc) {
        System.out.print("Nombre de la parcela o cultivo: ");
        String input = sc.nextLine().trim();
        return input.isEmpty() ? "Parcela" : input;
    }

    /**
     * Pide una fecha en formato dd/MM/yyyy.
     * Si despues != null, valida que la nueva fecha sea posterior a 'despues'.
     */
    private static LocalDate pedirFecha(Scanner sc, String prompt, LocalDate despues) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                LocalDate fecha = LocalDate.parse(input, FMT);
                if (despues != null && !fecha.isAfter(despues)) {
                    System.out.println("  ⚠ Debe ser posterior a " + despues.format(FMT));
                    continue;
                }
                if (fecha.isAfter(LocalDate.now())) {
                    System.out.println("  ⚠ La fecha no puede ser futura (la API solo tiene datos históricos).");
                    continue;
                }
                return fecha;
            } catch (Exception e) {
                System.out.println("  ⚠ Formato incorrecto. Usa dd/MM/yyyy — ej: 01/11/2024");
            }
        }
    }

    /**
     * Pide un número double con valor default y rango válido.
     */
    private static double pedirDouble(Scanner sc, String prompt,
                                      double defaultVal, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return defaultVal;
            try {
                double val = Double.parseDouble(input);
                if (val < min || val > max) {
                    System.out.printf("  ⚠ Debe estar entre %.0f y %.0f%n", min, max);
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  ⚠ Ingresa un número válido (ej: 10 o 10.5)");
            }
        }
    }
}