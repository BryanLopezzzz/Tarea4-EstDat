package org.example.tarea4;

import java.util.*;

public class PageRanking {
    private final double coeficienteAmortiguacion;
    private final double toleranciaConvergencia;
    private final int limiteIteraciones;

    public PageRanking(double coeficienteAmortiguacion, double toleranciaConvergencia, int limiteIteraciones) {
        this.coeficienteAmortiguacion = coeficienteAmortiguacion;
        this.toleranciaConvergencia = toleranciaConvergencia;
        this.limiteIteraciones = limiteIteraciones;
    }

    public Map<Integer, Double> computarRanking(int[][] grafoAdyacencia, int totalNodos) {

        double[] vectorRanking = new double[totalNodos];
        double[] vectorRankingNuevo = new double[totalNodos];

        double valorUniforme = 1.0 / totalNodos;
        Arrays.fill(vectorRanking, valorUniforme);
        int[] gradosSalida = calcularGradosSalida(grafoAdyacencia, totalNodos);

        // el iterativo puede ser de otra manera
        int ciclo = 0;
        double errorAcumulado;

        do {
            errorAcumulado = 0.0;

            for (int nodoDestino = 0; nodoDestino < totalNodos; nodoDestino++) {
                double sumaContribuciones = 0.0;

                for (int nodoOrigen = 0; nodoOrigen < totalNodos; nodoOrigen++) {
                    if (grafoAdyacencia[nodoOrigen][nodoDestino] == 1) {
                        if (gradosSalida[nodoOrigen] > 0) {
                            sumaContribuciones += vectorRanking[nodoOrigen] / gradosSalida[nodoOrigen];
                        }
                    }
                }

                // se aplica la formula: PR(A) = (1-d)/N + d * suma
                double rankingActualizado = ((1.0 - coeficienteAmortiguacion) / totalNodos) +
                        (coeficienteAmortiguacion * sumaContribuciones);
                vectorRankingNuevo[nodoDestino] = rankingActualizado;

                errorAcumulado += Math.abs(rankingActualizado - vectorRanking[nodoDestino]);
            }

            System.arraycopy(vectorRankingNuevo, 0, vectorRanking, 0, totalNodos);
            ciclo++;

            System.out.printf("  Ciclo %d → Error acumulado: %.8f\n", ciclo, errorAcumulado);

        } while (errorAcumulado > toleranciaConvergencia && ciclo < limiteIteraciones);

        System.out.println("\nConvergencia alcanzada después de " + ciclo + " iteraciones");
        Map<Integer, Double> rankingFinal = new HashMap<>();
        for (int i = 0; i < totalNodos; i++) {
            rankingFinal.put(i, vectorRanking[i]);
        }

        aplicarNormalizacion(rankingFinal);

        return rankingFinal;
    }
    private int[] calcularGradosSalida(int[][] grafo, int dimension) {
        int[] grados = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            int contador = 0;
            for (int j = 0; j < dimension; j++) {
                if (grafo[i][j] == 1) {
                    contador++;
                }
            }
            grados[i] = contador;
        }
        return grados;
    }

    private void aplicarNormalizacion(Map<Integer, Double> ranking) {
        double sumaTotal = ranking.values().stream().mapToDouble(Double::doubleValue).sum();
        ranking.replaceAll((k, v) -> v / sumaTotal);
    }

    public List<Map.Entry<Integer, Double>> ordenarPorRanking(Map<Integer, Double> ranking) {
        List<Map.Entry<Integer, Double>> listaOrdenada = new ArrayList<>(ranking.entrySet());
        listaOrdenada.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return listaOrdenada;
    }
}
