# Calculadora de GDU y riegos 🌽

Calcula los **grados día acumulados** de una parcela a partir de sus coordenadas, dice **en qué etapa va el cultivo** y sugiere un **calendario de riegos** con fechas, ventanas, lámina, volumen y horas de riego.

Es una página web de un solo archivo: no necesita servidor, cuenta ni instalación. Abre `index.html` en el navegador o úsala en línea:

**👉 https://arrivedbog593.github.io/GDU/**

Pensada para maíz blanco de riego en el norte de Sinaloa, usa el mismo modelo de programación de riego que el programa IrriModel del INIFAP, calibrado para el Valle del Fuerte.

---

## Qué hace

**Grados día**
- Coordenadas en decimal (`25.7908`, `-108.9963`) o en grados, minutos y segundos (`25°47'26.9"N`, `108 59 46.7 O`). Acepta `O` de oeste y coma decimal.
- Botones para Los Mochis, Guasave y la ubicación del teléfono.
- Preajustes de cultivo con temperatura base, tope y GDU objetivo.
- Gráfica de GDU diario y acumulado, línea de objetivo y fecha estimada en que se alcanza.

**Etapas del cultivo** (maíz)
- Etapa actual, siguiente etapa con fecha, periodo crítico de agua (floración a grano lechoso), fecha estimada de madurez fisiológica y **fecha estimada de cosecha** (madurez más los días de secado en campo).
- Aunque la siembra sea hoy, el cálculo llega hasta la cosecha: los días que todavía no tienen clima real se llenan con el promedio de los 3 años anteriores.
- **Ajuste local** con la fecha real de cosecha (o de madurez) de un ciclo pasado, para que el modelo se ajuste al híbrido y a la parcela.
- La cosecha se da como **desde cuándo se puede empezar a cosechar**; la fecha real depende también de la logística (trilladora, orden de las parcelas).

**Datos guardados**
- Al calcular, la página guarda en el teléfono la parcela, la fecha de siembra, el cultivo, los °D calibrados, el riego y los riegos anotados. La próxima vez ya están puestos; solo cambia lo que haga falta. Se borran con *Borrar los datos guardados*.
- Tira visual con todas las etapas, el día de hoy y los riegos encima.

**Riegos**
- Balance diario de agua en el suelo con lluvia y evapotranspiración reales.
- Fecha recomendada y ventana de cada riego, lámina neta y bruta, y si cae en periodo crítico.
- Volumen en m³ y horas de riego según la superficie y el gasto del canal.
- Riegos ya aplicados: se anotan con fecha y lámina, el calendario se recalcula desde ahí y cada riego se califica (en buen punto, tarde, adelantado, lámina insuficiente).
- Avisa cuál es el último riego del ciclo y si los riegos previstos no alcanzan.

**Salidas**
- CSV con el detalle diario, los riegos y los parámetros (abre directo en Excel).
- Impresión o PDF desde el navegador.

---

## Cómo usarla

1. Captura las coordenadas de la parcela y su superficie.
2. Pon la fecha de siembra. La fecha final es hasta dónde se usa clima real (por defecto, hoy); el cálculo sigue solo con promedios hasta la cosecha. Para probar un año pasado, pon la siembra de ese año y una fecha final: lo que pase de ahí se estima como si fuera ese día.
3. Elige el cultivo. Para maíz deja activada la **calibración INIFAP del Valle del Fuerte**. Si conoces la siembra y la cosecha de un ciclo pasado en la misma parcela con el mismo híbrido, úsalas en **Ajuste local** (ver abajo).
4. En riegos, elige tipo de suelo y método, la lámina bruta máxima por riego, el gasto en L/s y cuántos riegos hay contemplados.
5. Si ya se regó, anota cada riego con su fecha y lámina.
6. Presiona **Calcular acumulado**.

En el teléfono: abre la página en Chrome y elige **Agregar a pantalla principal** para tenerla como app.

### Valores de fábrica para maíz

| Campo | Valor | De dónde sale |
|---|---|---|
| Madurez para etapas y riegos | 2000 °D INIFAP | Calibrado con el ciclo 2025-26 en el Valle del Fuerte: siembra 3 de noviembre, cosecha 1 de mayo |
| GDU objetivo | 1915 GDU °C | Lo que acumuló ese mismo ciclo hasta madurez; se mueve solo al cambiar la madurez |
| Secado en campo | 30 días | Entre madurez fisiológica y cosecha |
| Lámina bruta (gravedad) | 150 mm | Riego de auxilio rodado |

Las fichas de semilla traen los GDU en °F. Para pasarlos a °C se divide entre 1.8 (sin restar 32): 2500 °F ≈ 1390 °C. Ese número es de híbridos y veranos del Medio Oeste de EE. UU.; para maíz blanco de invierno en Sinaloa, con los datos de Open-Meteo, hacen falta unos 1900–2000.

---

## Cómo calcula

### Grados día (pantalla principal)

```
GDU = ( mín(Tmáx, tope) + máx(Tmín, base) ) / 2 − base      (nunca negativo)
```

### Grados día del modelo INIFAP

El modelo de riego de maíz se calibró con grados día calculados sobre la temperatura media, sin recortar la mínima a la base. Por eso la calculadora lleva una segunda serie solo para alimentar ese modelo:

```
Tmedia ≤ 10 °C        → 0
10 < Tmedia < 30 °C   → Tmedia − 10
Tmedia ≥ 30 °C        → 20
```

Madurez fisiológica a **1451 °D** para variedades intermedias según el artículo; la página usa **2000 °D** por defecto, calibrado con una cosecha real (ajustable).

### Balance de agua en el suelo

```
Dr(hoy) = Dr(ayer) + ETc − lluvia aprovechada − riego
ETc     = Kc × ET0
TAW     = agua aprovechable del suelo (mm/m) × profundidad de raíz
RAW     = f × TAW                → al llegar Dr a RAW, toca regar
```

Lluvia aprovechada: se descuentan 2 mm y el 20 % que escurre. Un riego sugerido repone lo que le falta al suelo, hasta lo que admite la lámina máxima por la eficiencia del método.

### Calibración para maíz del Valle del Fuerte

Ojeda-Bustamante, Sifuentes y Unland (2006), con `x = GDA / 1451`:

| Modelo | Ecuación | Parámetros |
|---|---|---|
| Coeficiente de cultivo | `Kc = Kmax · erfc(((x − xKmax)/α₁)²)`, mínimo `Kco` | Kmax 1.25 · xKmax 0.59 · α₁ 0.45 · Kco 0.20 |
| Profundidad de raíz | `Pr = Pro + (Prmax − Pro)(1 − exp(−GDA²/α₂²))` | Pro 0.07 m · Prmax 1.00 m · α₂ 527 |
| Factor de abatimiento | `f = α₃ − α₄ · Kc` | α₃ 0.80 gravedad, 0.60 aspersión · α₄ 0.10 |

Con un ciclo de invierno completo el modelo da un consumo cercano a 450 mm, en línea con los 44 cm medidos en el artículo original.

### Ajuste local y cosecha

```
madurez (R6)     = día en que los °D INIFAP llegan a los °D a madurez
cosecha          = madurez + días de secado en campo
ajuste local     = °D acumulados con clima real hasta (cosecha real − días de secado)
```

Con la siembra y la cosecha reales de un ciclo pasado, la calculadora busca cuántos °D acumuló ese ciclo hasta la madurez y los pone como °D a madurez (y el GDU objetivo). Para los ciclos siguientes en la misma parcela se escriben esos valores y se deja vacía la fecha.

### Etapas fenológicas (°D acumulados, método INIFAP)

| Etapa | °D |
|---|---|
| VE · Emergencia | 109.4 |
| V10 | 576.1 |
| VT · Flor masculina | 665.2 |
| R1 · Jiloteo | 823.5 |
| R2 · Grano acuoso | 978.6 |
| R3 · Grano lechoso | 1102.3 |
| R4 · Grano masoso | 1239.8 |
| R5 · Grano dentado | 1335.5 |
| R6 · Madurez fisiológica | 1451.1 |

Si se cambian los °D a madurez para otra variedad, las etapas se escalan en proporción.

### Datos de clima

Todo viene de [Open-Meteo](https://open-meteo.com/), sin API key: temperatura máxima y mínima, lluvia y evapotranspiración de referencia (FAO-56). Se combinan tres fuentes:

- **Archivo histórico** para el periodo pasado.
- **Pronóstico** para los últimos 92 días (el histórico va con unos 5 días de retraso) y los próximos 16.
- **Promedio de los 3 años anteriores** (temperatura máxima, mínima y evapotranspiración, día por día del calendario) para todo lo que va más allá del pronóstico, hasta la cosecha. En esos días se supone que no llueve, a propósito, para no retrasar un riego.

Antes, lo que pasaba del último dato se proyectaba con el ritmo de las dos últimas semanas. En una siembra de noviembre ese ritmo es el de días todavía calientes y adelantaba la madurez a febrero o marzo; con los promedios el invierno sí cuenta.

La altitud de la parcela no hace falta: Open-Meteo ya corrige la temperatura con la altura del terreno de su modelo (13 m en el Valle del Fuerte).

---

## Limitaciones

Es una herramienta de planeación, no un sensor. El modelo no sabe si el suelo de una parcela está compactado, si el riego anterior mojó parejo o si hay capa dura; la experiencia de quien trabaja la parcela manda sobre el número.

- La calibración local existe solo para **maíz**. Los demás preajustes usan coeficientes genéricos de FAO-56, y los valores de frijol son de referencia, no de una calibración local.
- Para **goteo** en maíz no hay factor de abatimiento publicado; se usa 0.50 como extrapolación.
- Entre emergencia y V10 la tabla de etapas original tiene etapas intermedias que no se pudieron recuperar; ese tramo se muestra como desarrollo vegetativo.
- Las fechas posteriores al pronóstico son estimaciones y se recorren solas conforme llegan datos reales.
- Los 2000 °D de fábrica salen de un solo ciclo y una parcela; conviene comprobarlos con otros años (siembra de ese año, fecha final después de mayo, ajuste local vacío) y, si no cuadran, recalibrar.
- Los 1451 °D del artículo son de una variedad intermedia; un híbrido más tardío necesita más. El secado en campo (30 días por defecto) depende de la humedad a la que se coseche. Las dos cosas se corrigen de una vez con el ajuste local por fecha de cosecha.
- Las temperaturas de Open-Meteo son de un modelo con celdas de varios kilómetros, no de una estación; cerca de la costa pueden salir algo más templadas que en la parcela. El ajuste local también absorbe ese sesgo.

---

## Estructura del repositorio

```
index.html        La calculadora completa (HTML, CSS y JavaScript en un solo archivo)
README.md
consola-java/     Versión original de consola en Java (ver abajo)
```

### Versión de consola (Java)

La primera versión del proyecto: pide coordenadas y fechas por consola, consulta Open-Meteo, calcula los GDU y exporta a Excel. Solo calcula grados día; todo lo demás está en `index.html`.

Ábrela en IntelliJ IDEA desde la carpeta `consola-java` y ejecuta `Main`. Las pruebas de la fórmula de GDU corren con:

```bash
cd consola-java
gradle test
```

Requiere Java 17 o superior (por JUnit 6) y Gradle.

---

## Publicar en GitHub Pages

1. En el repositorio: **Settings → Pages**.
2. En *Source*, **Deploy from a branch**, rama `master`, carpeta `/ (root)`.
3. En uno o dos minutos queda en `https://arrivedbog593.github.io/GDU/`. Cada push actualiza la página.

---

## Referencias

- Ojeda-Bustamante, W., Sifuentes-Ibarra, E. y Unland, H. (2006). Programación integral del riego en maíz en el norte de Sinaloa, México. *Agrociencia*, 40(1), 13-25.
- Sifuentes Ibarra, E. *IrriModel: programación integral y gestión del riego a través de internet*. Manual de usuario. INIFAP, Campo Experimental Valle del Fuerte.
- Allen, R. G., Pereira, L. S., Raes, D. y Smith, M. (1998). *Crop evapotranspiration*. FAO Irrigation and Drainage Paper 56.
- Datos meteorológicos: [Open-Meteo.com](https://open-meteo.com/), licencia CC BY 4.0.
