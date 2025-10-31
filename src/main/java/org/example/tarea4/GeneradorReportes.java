package org.example.tarea4;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

 //exporta el grafo y los resultados de PR.
public class GeneradorReportes {
    public static void generarReporteGrafo(String rutaArchivo,
                                           int[][] matrizGrafo,
                                           List<String> catalogoUrls) throws IOException {

        int dimension = matrizGrafo.length;

        try (FileWriter escritor = new FileWriter(rutaArchivo)) {
            escribirEncabezadoGrafo(escritor, dimension);
            escribirCatalogoUrls(escritor, catalogoUrls, dimension);
            escribirMatrizAdyacencia(escritor, matrizGrafo, dimension);
            escribirEstadisticasGrafo(escritor, matrizGrafo, dimension);
        }

        System.out.println(" Reporte de grafo: " + rutaArchivo);
    }

    public static void generarReporteRanking(String rutaArchivo,
                                             Map<Integer, Double> datosRanking,
                                             List<String> catalogoUrls) throws IOException {

        try (FileWriter escritor = new FileWriter(rutaArchivo)) {
            escribirEncabezadoRanking(escritor, datosRanking.size());
            escribirTablaRanking(escritor, datosRanking, catalogoUrls);
            escribirEstadisticasRanking(escritor, datosRanking, catalogoUrls);
            escribirTopPaginas(escritor, datosRanking, catalogoUrls, 10);
        }

        System.out.println(" Reporte de ranking: " + rutaArchivo);
    }

    private static void escribirEncabezadoGrafo(FileWriter w, int dimension) throws IOException {
        w.write("╔" + "═".repeat(80) + "╗\n");
        w.write("║" + centrar("REPORTE DE ESTRUCTURA DEL GRAFO WEB", 80) + "║\n");
        w.write("║" + centrar("Matriz de Adyacencia", 80) + "║\n");
        w.write("╚" + "═".repeat(80) + "╝\n\n");
        w.write("Dimensión del grafo: " + dimension + " nodos\n");
        w.write("Fecha de generación: " + new Date() + "\n");
        w.write("═".repeat(80) + "\n\n");
    }

    private static void escribirCatalogoUrls(FileWriter w, List<String> urls, int total) throws IOException {
        w.write("CATÁLOGO DE URLS INDEXADAS\n");
        w.write("─".repeat(80) + "\n\n");
        for (int i = 0; i < total; i++) {
            w.write(String.format("[%4d] %s\n", i, urls.get(i)));
        }
        w.write("\n");
    }
//esta función logra en hacer bonita la matriz, en pocas palabras
    private static void escribirMatrizAdyacencia(FileWriter w, int[][] matriz, int dim) throws IOException {
        w.write("MATRIZ DE ADYACENCIA\n");
        w.write("─".repeat(80) + "\n");
        w.write("Formato: Fila → Columna (1 = existe enlace, 0 = no existe)\n\n");

        if (dim > 50) {
            w.write("Nota: Matriz grande. Se muestra en formato comprimido.\n\n");

            for (int i = 0; i < dim; i++) {
                List<Integer> conexiones = new ArrayList<>();
                for (int j = 0; j < dim; j++) {
                    if (matriz[i][j] == 1) {
                        conexiones.add(j);
                    }
                }

                if (!conexiones.isEmpty()) {
                    w.write(String.format("[%4d] → ", i));
                    if (conexiones.size() <= 20) {
                        w.write(conexiones.toString() + "\n");
                    } else {
                        w.write(conexiones.subList(0, 20).toString() +
                                " ... (total: " + conexiones.size() + " enlaces)\n");
                    }
                }
            }

            w.write("\n" + "═".repeat(80) + "\n");
            w.write("MATRIZ COMPLETA (FORMATO BINARIO)\n");
            w.write("─".repeat(80) + "\n\n");

            for (int i = 0; i < dim; i++) {
                w.write(String.format("%4d: ", i));
                for (int j = 0; j < dim; j++) {
                    w.write(matriz[i][j] == 1 ? "1" : "0");
                    if ((j + 1) % 100 == 0 && j < dim - 1) {
                        w.write("\n      ");
                    }
                }
                w.write("\n");
            }

        } else {
            w.write("      ");
            for (int j = 0; j < dim; j++) {
                w.write(String.format("%3d ", j));
            }
            w.write("\n");

            for (int i = 0; i < dim; i++) {
                w.write(String.format("%4d  ", i));
                for (int j = 0; j < dim; j++) {
                    w.write(String.format("%3d ", matriz[i][j]));
                }
                w.write("\n");
            }
        }
        w.write("\n");
    }

    private static void escribirEstadisticasGrafo(FileWriter w, int[][] matriz, int dim) throws IOException {
        w.write("═".repeat(80) + "\n");
        w.write("ESTADÍSTICAS DEL GRAFO\n");
        w.write("─".repeat(80) + "\n");

        int totalAristas = 0;
        int gradoMaxSalida = 0;
        int nodosAislados = 0;
        int[] gradosEntrada = new int[dim];

        for (int i = 0; i < dim; i++) {
            int gradoSalida = 0;
            for (int j = 0; j < dim; j++) {
                if (matriz[i][j] == 1) {
                    gradoSalida++;
                    totalAristas++;
                    gradosEntrada[j]++;
                }
            }
            gradoMaxSalida = Math.max(gradoMaxSalida, gradoSalida);
            if (gradoSalida == 0) nodosAislados++;
        }

        int gradoMaxEntrada = Arrays.stream(gradosEntrada).max().orElse(0);
        double densidad = (double) totalAristas / (dim * dim);

        w.write(String.format("1- Total de nodos: %d\n", dim));
        w.write(String.format("2- Total de aristas: %d\n", totalAristas));
        w.write(String.format("3- Grado promedio de salida: %.2f\n", (double) totalAristas / dim));
        w.write(String.format("4- Grado máximo de salida: %d\n", gradoMaxSalida));
        w.write(String.format("5- Grado máximo de entrada: %d\n", gradoMaxEntrada));
        w.write(String.format("6- Nodos sin enlaces salientes: %d\n", nodosAislados));
        w.write(String.format("7- Densidad del grafo: %.6f\n", densidad));
        w.write("\n");
    }

    private static void escribirEncabezadoRanking(FileWriter w, int total) throws IOException {
        w.write("╔" + "═".repeat(100) + "╗\n");
        w.write("║" + centrar("RESULTADOS DEL ALGORITMO PAGERANK", 100) + "║\n");
        w.write("╚" + "═".repeat(100) + "╝\n\n");
        w.write("Factor de amortiguación: 0.85\n");
        w.write("Páginas analizadas: " + total + "\n");
        w.write("Fecha: " + new Date() + "\n");
        w.write("═".repeat(100) + "\n\n");
    }

    private static void escribirTablaRanking(FileWriter w, Map<Integer, Double> ranking,
                                             List<String> urls) throws IOException {
        w.write("RANKING COMPLETO DE PÁGINAS\n");
        w.write("─".repeat(100) + "\n");
        w.write(String.format("%-8s %-18s %s\n", "Posición", "Puntuación", "URL"));
        w.write("─".repeat(100) + "\n");

        List<Map.Entry<Integer, Double>> ordenado = new ArrayList<>(ranking.entrySet());
        ordenado.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int posicion = 1;
        for (Map.Entry<Integer, Double> entrada : ordenado) {
            w.write(String.format("%-8d %-18.12f %s\n",
                    posicion++,
                    entrada.getValue(),
                    urls.get(entrada.getKey())));
        }
        w.write("\n");
    }

    private static void escribirEstadisticasRanking(FileWriter w, Map<Integer, Double> ranking,
                                                    List<String> urls) throws IOException {
        w.write("═".repeat(100) + "\n");
        w.write("ESTADÍSTICAS DE PAGERANK\n");
        w.write("─".repeat(100) + "\n");

        List<Map.Entry<Integer, Double>> ordenado = new ArrayList<>(ranking.entrySet());
        ordenado.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        double suma = ranking.values().stream().mapToDouble(Double::doubleValue).sum();
        double promedio = suma / ranking.size();
        double max = ordenado.get(0).getValue();
        double min = ordenado.get(ordenado.size() - 1).getValue();

        w.write(String.format(" 1- Suma total de puntuaciones: %.12f\n", suma));
        w.write(String.format(" 2- Puntuación promedio: %.12f\n", promedio));
        w.write(String.format(" 3- Puntuación máxima: %.12f\n", max));
        w.write(String.format("  -> URL: %s\n", urls.get(ordenado.get(0).getKey())));
        w.write(String.format(" 4- Puntuación mínima: %.12f\n", min));
        w.write("\n");
    }

    private static void escribirTopPaginas(FileWriter w, Map<Integer, Double> ranking,
                                           List<String> urls, int cantidad) throws IOException {
        w.write("═".repeat(100) + "\n");
        w.write("TOP " + cantidad + " PÁGINAS MÁS IMPORTANTES\n");
        w.write("─".repeat(100) + "\n\n");

        List<Map.Entry<Integer, Double>> ordenado = new ArrayList<>(ranking.entrySet());
        ordenado.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (int i = 0; i < Math.min(cantidad, ordenado.size()); i++) {
            Map.Entry<Integer, Double> entrada = ordenado.get(i);
            w.write(String.format("%2d. [%.12f] %s\n",
                    i + 1,
                    entrada.getValue(),
                    urls.get(entrada.getKey())));
        }
        w.write("\n");
    }

    private static String centrar(String texto, int ancho) {
        int padding = (ancho - texto.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + texto +
                " ".repeat(Math.max(0, ancho - texto.length() - padding));
    }
}