package com.github.ArrivedBog593.service;

import com.github.ArrivedBog593.model.DiaClima;
import com.github.ArrivedBog593.model.Parcela;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que obtiene datos históricos de temperatura desde la API de Open-Meteo.
 * API gratuita, sin registro ni API key.
 * Documentación: <a href="https://open-meteo.com/en/docs/historical-weather-api">...</a>
 */
public class ClimaService {

    private static final String BASE_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final int TIMEOUT_MS  = 15_000; // 15 segundos

    /**
     * Obtiene los datos de temperatura para una parcela.
     * Maneja valores nulos que Open-Meteo a veces devuelve por datos faltantes.
     *
     * @param parcela la parcela con lat, lon y rango de fechas
     * @return lista de DiaClima con los datos de cada día
     */
    public static List<DiaClima> obtenerDatos(Parcela parcela) throws Exception {

        String urlStr = BASE_URL
                + "?latitude="   + parcela.getLat()
                + "&longitude="  + parcela.getLon()
                + "&start_date=" + parcela.getInicioApi()
                + "&end_date="   + parcela.getFinApi()
                + "&daily=temperature_2m_max,temperature_2m_min"
                + "&timezone=auto";

        JSONObject daily = fetchDaily(urlStr);

        JSONArray fechas = daily.getJSONArray("time");
        JSONArray tmax   = daily.getJSONArray("temperature_2m_max");
        JSONArray tmin   = daily.getJSONArray("temperature_2m_min");

        List<DiaClima> lista = new ArrayList<>();

        for (int i = 0; i < fechas.length(); i++) {

            // Open-Meteo puede devolver null en días sin datos — los omitimos
            if (tmax.isNull(i) || tmin.isNull(i)) {
                System.out.printf("  ⚠ Dato faltante el %s — día omitido%n", fechas.getString(i));
                continue;
            }

            lista.add(new DiaClima(
                    fechas.getString(i),
                    tmax.getDouble(i),
                    tmin.getDouble(i)
            ));
        }

        return lista;
    }

    /** Hace la petición HTTP y devuelve el objeto "daily" del JSON. */
    private static JSONObject fetchDaily(String urlStr) throws IOException {

        HttpURLConnection con = null;

        try {
            URL url = URI.create(urlStr).toURL();
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);

            JSONObject json = getJsonObject(con);

            if (!json.has("daily")) {
                throw new IOException("La API no devolvió datos diarios. Respuesta: " + json);
            }

            return json.getJSONObject("daily");

        } finally {
            // Siempre cerrar la conexión, haya error o no
            if (con != null) con.disconnect();
        }
    }

    private static JSONObject getJsonObject(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        if (status != 200) {
            throw new IOException("La API respondió con código HTTP " + status
                    + ". Verifica las fechas o la conexión a internet.");
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
        }

        return new JSONObject(content.toString());
    }
}