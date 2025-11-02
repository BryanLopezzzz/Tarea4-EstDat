package org.example.tarea4;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.util.*;

public class WebCrawler {

    private Map<String, Integer> diccionarioUrls = new HashMap<>();
    private List<String> listaUrls = new ArrayList<>();
    private int[][] matrizConexiones;
    private Set<String> sitiosVisitados = new HashSet<>();
    private int limitePaginas = 1000;
    private int contadorIndices = 0;
    private String directorioDestino;

    public WebCrawler(int limitePaginas) {
        this.limitePaginas = limitePaginas;
    }

    public WebCrawler() {
        this(1000);
    }

    public void iniciarExploracion(String rutaDestino, List<String> urlsSemilla, String filtro) {
        directorioDestino = rutaDestino;
        File directorio = new File(directorioDestino);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        matrizConexiones = new int[limitePaginas][limitePaginas];
        Queue<NodoExploracion> colaExploracion = new LinkedList<>();
        Set<String> urlsEnCola = new HashSet<>();

        for (String url : urlsSemilla) {
            colaExploracion.offer(new NodoExploracion(url, 0));
            urlsEnCola.add(url);
        }

        long tiempoInicio = System.currentTimeMillis();
        int errorCount = 0;

        while (!colaExploracion.isEmpty() && sitiosVisitados.size() < limitePaginas) {
            NodoExploracion nodoActual = colaExploracion.poll();

            if (sitiosVisitados.contains(nodoActual.url) || !nodoActual.url.startsWith("http")) {
                continue;
            }

            if (filtro != null && !filtro.isEmpty() && !nodoActual.url.contains(filtro)) {
                continue;
            }

            try {
                System.out.println("[" + sitiosVisitados.size() + "/" + limitePaginas + "] Explorando: " + nodoActual.url);

                int indiceOrigen = registrarUrl(nodoActual.url);
                sitiosVisitados.add(nodoActual.url);

                Document documento = Jsoup.connect(nodoActual.url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(3000)
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .ignoreContentType(true)
                        .maxBodySize(1024 * 1024 * 2)
                        .get();

                Elements hiperenlaces = documento.select("a[href]");
                int enlacesAgregados = 0;
                int maxEnlacesPorPagina = 100;

                for (Element hiperenlace : hiperenlaces) {
                    if (sitiosVisitados.size() >= limitePaginas || enlacesAgregados >= maxEnlacesPorPagina) {
                        break;
                    }

                    String urlDestino = hiperenlace.attr("abs:href");

                    if (urlDestino == null || urlDestino.isEmpty() || !urlDestino.startsWith("http")) {
                        continue;
                    }

                    urlDestino = limpiarUrl(urlDestino);

                    if (filtro != null && !filtro.isEmpty() && !urlDestino.contains(filtro)) {
                        continue;
                    }

                    if (sitiosVisitados.contains(urlDestino) || urlsEnCola.contains(urlDestino)) {
                        continue;
                    }

                    int indiceDestino = registrarUrl(urlDestino);

                    if (indiceOrigen < limitePaginas && indiceDestino < limitePaginas) {
                        matrizConexiones[indiceOrigen][indiceDestino] = 1;
                    }

                    if (nodoActual.profundidad < 3 && sitiosVisitados.size() < limitePaginas) {
                        colaExploracion.offer(new NodoExploracion(urlDestino, nodoActual.profundidad + 1));
                        urlsEnCola.add(urlDestino);
                        enlacesAgregados++;
                    }
                }

                Thread.sleep(20);
                errorCount = 0;

            } catch (Exception excepcion) {
                errorCount++;
                if (errorCount > 50) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    errorCount = 0;
                }
            }
        }

        ajustarDimensionMatriz();

        long tiempoTotal = (System.currentTimeMillis() - tiempoInicio) / 1000;
        System.out.println("\nExploración completada. Total de sitios: " + sitiosVisitados.size());
        System.out.println("Tiempo total: " + tiempoTotal + " segundos (" + (tiempoTotal / 60.0) + " minutos)");
    }

    private int registrarUrl(String url) {
        if (!diccionarioUrls.containsKey(url)) {
            if (contadorIndices >= limitePaginas) {
                return limitePaginas - 1;
            }
            diccionarioUrls.put(url, contadorIndices);
            listaUrls.add(url);
            contadorIndices++;
        }
        return diccionarioUrls.get(url);
    }

    private String limpiarUrl(String url) {
        try {
            int hashPos = url.indexOf('#');
            if (hashPos != -1) {
                url = url.substring(0, hashPos);
            }

            int queryPos = url.indexOf('?');
            if (queryPos != -1) {
                url = url.substring(0, queryPos);
            }

            if (url.endsWith("/") && url.length() > 1) {
                url = url.substring(0, url.length() - 1);
            }

            url = url.toLowerCase();

            return url;
        } catch (Exception e) {
            return url;
        }
    }

    private void ajustarDimensionMatriz() {
        if (contadorIndices < limitePaginas) {
            int[][] matrizAjustada = new int[contadorIndices][contadorIndices];
            for (int i = 0; i < contadorIndices; i++) {
                System.arraycopy(matrizConexiones[i], 0, matrizAjustada[i], 0, contadorIndices);
            }
            matrizConexiones = matrizAjustada;
        }
    }

    public int[][] obtenerGrafoAdyacencia() {
        return matrizConexiones;
    }

    public List<String> obtenerMapeoIndices() {
        return listaUrls;
    }

    public int obtenerTotalPaginas() {
        return listaUrls.size();
    }

    public int obtenerLimitePaginas() {
        return limitePaginas;
    }

    private static class NodoExploracion {
        String url;
        int profundidad;

        NodoExploracion(String url, int profundidad) {
            this.url = url;
            this.profundidad = profundidad;
        }
    }
}