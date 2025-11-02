package org.example.tarea4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sistema {
    private final AnalisisPR configuracion;
    private WebCrawler analizador;
    private int[][] grafoWeb;
    private List<String> mapeoUrls;
    private Map<Integer, Double> rankingPaginas;

    public Sistema(AnalisisPR configuracion) {
        this.configuracion = configuracion;
        this.analizador = new WebCrawler();
    }

    public void ejecutarAnalisisCompleto() {
        mostrarBanner();

        // lo primero es rastreo y construcción del grafo
        ejecutarFaseRastreo();
        //lo segundo es calcular el PR
        ejecutarFaseCalculoRanking();
        //luego los resultados
        ejecutarFaseExportacion();
        mostrarResumenFinal();
    }

    private void ejecutarFaseRastreo() {
        imprimirEncabezadoFase("FASE 1: CONSTRUCCIÓN DEL GRAFO WEB");

        System.out.println("Sitios iniciales a analizar:");
        configuracion.getUrlsIniciales().forEach(url ->
                System.out.println("  → " + url));
        System.out.println();

        System.out.println("Parámetros de rastreo:");
        System.out.println("  1- Patrón de filtrado: '" + configuracion.getPatronFiltro() + "'");
        System.out.println("  2- Límite de páginas: " + analizador.obtenerLimitePaginas());
        System.out.println("  3- Profundidad máxima: 3 niveles");
        System.out.println();

        analizador.iniciarExploracion(
                configuracion.getRutaSalida(),
                configuracion.getUrlsIniciales(),
                configuracion.getPatronFiltro()
        );

        grafoWeb = analizador.obtenerGrafoAdyacencia();
        mapeoUrls = analizador.obtenerMapeoIndices();

        System.out.println("\n Grafo web construido exitosamente");
        System.out.println("  * Dimensión: " + grafoWeb.length + "×" + grafoWeb.length);
        System.out.println();
    }

    private void ejecutarFaseCalculoRanking() {
        imprimirEncabezadoFase("FASE 2: ALGORITMO DE RANKING");

        System.out.println("Parámetros del algoritmo:");
        System.out.println("  1- Factor de amortiguación: " + configuracion.getFactorAmortiguacion());
        System.out.println("  2- Umbral de convergencia: " + configuracion.getUmbralConvergencia());
        System.out.println("  3- Iteraciones máximas: " + configuracion.getIteracionesMaximas());
        System.out.println();

        PageRanking motor = new PageRanking(
                configuracion.getFactorAmortiguacion(),
                configuracion.getUmbralConvergencia(),
                configuracion.getIteracionesMaximas()
        );

        rankingPaginas = motor.computarRanking(grafoWeb, grafoWeb.length);

        System.out.println("\n Ranking calculado exitosamente");
        mostrarTopPaginas(5);
        System.out.println();
    }

    private void ejecutarFaseExportacion() {
        imprimirEncabezadoFase("FASE 3: GENERACIÓN DE REPORTES");

        try {
            String rutaMatriz = configuracion.getRutaSalida() + "/matriz_adyacencia.txt";
            String rutaRanking = configuracion.getRutaSalida() + "/pagerank_resultados.txt";

            GeneradorReportes.generarReporteGrafo(rutaMatriz, grafoWeb, mapeoUrls);
            GeneradorReportes.generarReporteRanking(rutaRanking, rankingPaginas, mapeoUrls);

            System.out.println(" Reportes generados exitosamente");
            System.out.println("  1- " + rutaMatriz);
            System.out.println("  2- " + rutaRanking);
            System.out.println();

        } catch (Exception e) {
            System.err.println("Error al generar reportes: " + e.getMessage());
        }
    }

    private void mostrarTopPaginas(int cantidad) {
        System.out.println("\nTop " + cantidad + " páginas por importancia:");
        System.out.println("─".repeat(80));

        List<Map.Entry<Integer, Double>> ordenado = new ArrayList<>(rankingPaginas.entrySet());
        ordenado.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < Math.min(cantidad, ordenado.size()); i++) {
            Map.Entry<Integer, Double> entrada = ordenado.get(i);
            String url = mapeoUrls.get(entrada.getKey());
            System.out.printf("  %d. [%.6f] %s\n", i + 1, entrada.getValue(),
                    acortarUrl(url, 60));
        }
    }

    private String acortarUrl(String url, int maxLen) {
        return url.length() > maxLen ? url.substring(0, maxLen) + "..." : url;
    }

    private void imprimirEncabezadoFase(String titulo) {
        System.out.println("╔" + "═".repeat(78) + "╗");
        System.out.println("║ " + centrarTexto(titulo, 76) + " ║");
        System.out.println("╚" + "═".repeat(78) + "╝");
        System.out.println();
    }

    private String centrarTexto(String texto, int ancho) {
        int padding = (ancho - texto.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + texto +
                " ".repeat(Math.max(0, ancho - texto.length() - padding));
    }

    private void mostrarBanner() {
        System.out.println("\n╔" + "═".repeat(78) + "╗");
        System.out.println("║" + centrarTexto("SISTEMA DE ANÁLISIS PAGERANK", 78) + "║");
        System.out.println("║" + centrarTexto("Análisis de Grafos Web con Matriz de Adyacencia", 78) + "║");
        System.out.println("╚" + "═".repeat(78) + "╝");
        System.out.println();
    }

    private void mostrarResumenFinal() {
        System.out.println("╔" + "═".repeat(78) + "╗");
        System.out.println("║ " + centrarTexto("ANÁLISIS COMPLETADO", 76) + " ║");
        System.out.println("╚" + "═".repeat(78) + "╝");
        System.out.println();
        System.out.println("Resumen de ejecución:");
        System.out.println("  1- Páginas analizadas: " + mapeoUrls.size());
        System.out.println("  2- Enlaces detectados: " + contarEnlaces());
        System.out.println("  3- Estructura: Matriz " + grafoWeb.length + "×" + grafoWeb.length);
        System.out.println("  4- Archivos generados: 2");
        System.out.println();
    }

    private int contarEnlaces() {
        int total = 0;
        for (int i = 0; i < grafoWeb.length; i++) {
            for (int j = 0; j < grafoWeb[i].length; j++) {
                total += grafoWeb[i][j];
            }
        }
        return total;
    }

    public int[][] obtenerGrafoWeb() {
        return grafoWeb;
    }

    public Map<Integer, Double> obtenerRankingPaginas() {
        return rankingPaginas;
    }
}
