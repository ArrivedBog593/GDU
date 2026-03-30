package com.github.ArrivedBog593.export;

import com.github.ArrivedBog593.model.DiaClima;
import com.github.ArrivedBog593.model.Parcela;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta los datos de GDU a un archivo Excel (.xlsx) con:
 *  - Hoja de datos con columna de GDU acumulado
 *  - Hoja de resumen
 *  - Gráfica de línea del GDU acumulado
 *  - Nombre de archivo dinámico basado en la parcela y fecha de generación
 */
public class ExcelExporter {

    public static String exportar(List<DiaClima> datos, Parcela parcela) throws Exception {

        String nombreArchivo = generarNombreArchivo(parcela);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // --- Estilos ---
            CellStyle estiloHeader  = crearEstiloHeader(wb);
            CellStyle estiloFecha   = crearEstiloFecha(wb);
            CellStyle estiloDecimal = crearEstiloDecimal(wb);
            CellStyle estiloResumen = crearEstiloResumen(wb);

            // --- Hoja 1: Datos ---
            XSSFSheet sheetDatos = wb.createSheet("Datos GDU");
            crearHojaDatos(sheetDatos, datos, estiloHeader, estiloFecha, estiloDecimal);

            // --- Hoja 2: Resumen ---
            XSSFSheet sheetResumen = wb.createSheet("Resumen");
            crearHojaResumen(sheetResumen, datos, parcela, estiloResumen, estiloHeader);

            // --- Gráfica de GDU acumulado en hoja de datos ---
            crearGrafica(sheetDatos, datos.size());

            // --- Guardar ---
            try (FileOutputStream fos = new FileOutputStream(nombreArchivo)) {
                wb.write(fos);
            }
        }

        return nombreArchivo;
    }

    // ---------------------------------------------------------------
    // Hoja de datos
    // ---------------------------------------------------------------

    private static void crearHojaDatos(XSSFSheet sheet, List<DiaClima> datos,
                                       CellStyle estiloHeader, CellStyle estiloFecha,
                                       CellStyle estiloDecimal) {

        // Encabezados
        Row header = sheet.createRow(0);
        String[] cols = {"Fecha", "T_max (°C)", "T_min (°C)", "T_media (°C)", "GDU día", "GDU acumulado"};
        for (int c = 0; c < cols.length; c++) {
            Cell cell = header.createCell(c);
            cell.setCellValue(cols[c]);
            cell.setCellStyle(estiloHeader);
        }

        // Datos
        for (int i = 0; i < datos.size(); i++) {
            DiaClima d = datos.get(i);
            Row row = sheet.createRow(i + 1);

            Cell cFecha = row.createCell(0);
            cFecha.setCellValue(d.getFecha());
            cFecha.setCellStyle(estiloFecha);

            setCeldaDecimal(row, 1, d.getTmax(),                   estiloDecimal);
            setCeldaDecimal(row, 2, d.getTmin(),                   estiloDecimal);
            setCeldaDecimal(row, 3, (d.getTmax() + d.getTmin()) / 2.0, estiloDecimal);
            setCeldaDecimal(row, 4, d.getGdu(),                    estiloDecimal);
            setCeldaDecimal(row, 5, d.getGduAcumulado(),           estiloDecimal);
        }

        // Ancho automático
        for (int c = 0; c < cols.length; c++) {
            sheet.autoSizeColumn(c);
        }
    }

    // ---------------------------------------------------------------
    // Hoja de resumen
    // ---------------------------------------------------------------

    private static void crearHojaResumen(XSSFSheet sheet, List<DiaClima> datos,
                                         Parcela parcela, CellStyle estiloResumen,
                                         CellStyle estiloHeader) {
        double gduTotal = datos.isEmpty() ? 0 : datos.get(datos.size() - 1).getGduAcumulado();
        int diasAnalizados = datos.size();

        String[][] filas = {
                {"Parcela / cultivo",   parcela.getNombre()},
                {"Ubicación",           "Lat " + parcela.getLat() + ", Lon " + parcela.getLon()},
                {"Fecha de siembra",    parcela.getInicioDisplay()},
                {"Fecha final",         parcela.getFinDisplay()},
                {"Días analizados",     String.valueOf(diasAnalizados)},
                {"Temperatura base",    parcela.getTempBase() + " °C"},
                {"Temperatura cap",     parcela.getTempCap()  + " °C"},
                {"GDU total acumulado", String.format("%.2f", gduTotal)},
                {"GDU objetivo",        parcela.tieneObjetivo()
                        ? String.valueOf(parcela.getGduObjetivo())
                        : "No especificado"},
                {"Fecha estimada cosecha", calcularFechaObjetivo(datos, parcela)},
        };

        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("RESUMEN DE GDU — " + parcela.getNombre());
        titleCell.setCellStyle(estiloHeader);

        for (int i = 0; i < filas.length; i++) {
            Row row = sheet.createRow(i + 2);
            Cell clave = row.createCell(0);
            Cell valor = row.createCell(1);
            clave.setCellValue(filas[i][0]);
            valor.setCellValue(filas[i][1]);
            clave.setCellStyle(estiloResumen);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static String calcularFechaObjetivo(List<DiaClima> datos, Parcela parcela) {
        if (!parcela.tieneObjetivo()) return "N/A";
        for (DiaClima d : datos) {
            if (d.getGduAcumulado() >= parcela.getGduObjetivo()) {
                return d.getFecha();
            }
        }
        return "No alcanzado en el período";
    }

    // ---------------------------------------------------------------
    // Gráfica de línea — GDU acumulado
    // ---------------------------------------------------------------

    private static void crearGrafica(XSSFSheet sheet, int numDatos) {

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(
                0, 0, 0, 0,
                0, numDatos + 3,
                8, numDatos + 22
        );

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("GDU Acumulado");
        chart.setTitleOverlay(false);

        // Eje X: categorías (fechas como strings)
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Fecha");

        // Eje Y: valores numéricos
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("GDU Acumulado");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        // Fuente de datos — columna A (fechas) y columna F (GDU acumulado)
        // filas 1..numDatos (índice 0-based, fila 0 = header)
        XDDFCategoryDataSource fechas = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(1, numDatos, 0, 0));

        XDDFNumericalDataSource<Double> acumulados = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(1, numDatos, 5, 5));

        XDDFLineChartData lineData = (XDDFLineChartData)
                chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        lineData.setVaryColors(false);

        XDDFLineChartData.Series series = (XDDFLineChartData.Series)
                lineData.addSeries(fechas, acumulados);
        series.setTitle("GDU Acumulado", null);
        series.setSmooth(true);
        series.setMarkerStyle(MarkerStyle.NONE);

        chart.plot(lineData);
    }

    // ---------------------------------------------------------------
    // Utilidades de estilo
    // ---------------------------------------------------------------

    private static void setCeldaDecimal(Row row, int col, double val, CellStyle estilo) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(estilo);
    }

    private static CellStyle crearEstiloHeader(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font fh = wb.createFont();
        fh.setBold(true);
        fh.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(fh);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle crearEstiloFecha(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle crearEstiloDecimal(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private static CellStyle crearEstiloResumen(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    // ---------------------------------------------------------------
    // Nombre dinámico del archivo
    // ---------------------------------------------------------------

    private static String generarNombreArchivo(Parcela parcela) {
        String nombreLimpio = parcela.getNombre()
                .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ_\\-]", "_");
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return nombreLimpio + "_GDU_" + timestamp + ".xlsx";
    }
}