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

    //Inicia la exploracion desde los URLss
    public void iniciarExploracion(String rutaDestino, List<String> urlsSemilla, String filtro) {
        directorioDestino = rutaDestino;
        File directorio = new File(directorioDestino);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        //esto es la matriz de adyacencia
        matrizConexiones = new int[limitePaginas][limitePaginas];

        Queue<NodoExploracion> colaExploracion = new LinkedList<>();
        for (String url : urlsSemilla) {
            colaExploracion.offer(new NodoExploracion(url, 0));
        }

        while (!colaExploracion.isEmpty() && sitiosVisitados.size() < limitePaginas) {
            NodoExploracion nodoActual = colaExploracion.poll();

            if (sitiosVisitados.contains(nodoActual.url) || !nodoActual.url.startsWith("http")) {
                continue;
            }

            if (filtro == null || filtro.isEmpty() || nodoActual.url.contains(filtro)) {
                try {
                    System.out.println("[" + sitiosVisitados.size() + "/" + limitePaginas + "] Explorando: " + nodoActual.url);

                    int indiceOrigen = registrarUrl(nodoActual.url);
                    sitiosVisitados.add(nodoActual.url);

                    Document documento = Jsoup.connect(nodoActual.url)
                            .userAgent("Mozilla/5.0 (compatible; AnalizadorWeb/1.0)")
                            .timeout(5000)
                            .followRedirects(true)
                            .get();

                    Elements hiperenlaces = documento.select("a[href]");
                    for (Element hiperenlace : hiperenlaces) {
                        String urlDestino = hiperenlace.attr("abs:href");

                        if (urlDestino != null && !urlDestino.isEmpty() && urlDestino.startsWith("http")) {
                            urlDestino = limpiarUrl(urlDestino);

                            int indiceDestino = registrarUrl(urlDestino);
                            if (indiceOrigen < limitePaginas && indiceDestino < limitePaginas) {
                                matrizConexiones[indiceOrigen][indiceDestino] = 1;
                            }

                            if (!sitiosVisitados.contains(urlDestino) &&
                                    (filtro == null || filtro.isEmpty() || urlDestino.contains(filtro)) &&
                                    nodoActual.profundidad < 3) {
                                colaExploracion.offer(new NodoExploracion(urlDestino, nodoActual.profundidad + 1));
                            }
                        }
                    }

                    Thread.sleep(100);

                } catch (Exception excepcion) {
                    System.out.println("⚠ Advertencia en " + nodoActual.url + ": " + excepcion.getMessage());
                }
            }
        }

        ajustarDimensionMatriz();

        System.out.println("\n✓ Exploración completada. Total de sitios: " + sitiosVisitados.size());
    }

//registra un url en la index
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
            url = url.split("#")[0];
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
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

   //clase interna que puede ser externa si queremos
    private static class NodoExploracion {
        String url;
        int profundidad;

        NodoExploracion(String url, int profundidad) {
            this.url = url;
            this.profundidad = profundidad;
        }
    }
}
