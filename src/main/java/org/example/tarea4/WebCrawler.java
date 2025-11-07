package org.example.tarea4;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {

    private final Map<String, Integer> diccionarioUrls = new ConcurrentHashMap<>();
    private final List<String> listaUrls = Collections.synchronizedList(new ArrayList<>());
    private int[][] matrizConexiones;
    private final Set<String> sitiosVisitados = ConcurrentHashMap.newKeySet();
    private final Set<String> urlsEnCola = ConcurrentHashMap.newKeySet();
    private final int limitePaginas;
    private final AtomicInteger contadorIndices = new AtomicInteger(0);
    private String directorioDestino;
    private ExecutorService executor;
    private final Object matrizLock = new Object();

    public WebCrawler(int limitePaginas) {
        this.limitePaginas = limitePaginas;
    }

    public WebCrawler() {
        this(50);
    }

    public void iniciarExploracion(String rutaDestino, List<String> urlsSemilla, String filtro) {
        directorioDestino = rutaDestino;
        File directorio = new File(directorioDestino);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        matrizConexiones = new int[limitePaginas][limitePaginas];

        executor = Executors.newFixedThreadPool(5);

        BlockingQueue<NodoExploracion> colaExploracion = new LinkedBlockingQueue<>();
        for (String url : urlsSemilla) {
            colaExploracion.offer(new NodoExploracion(url, 0));
            urlsEnCola.add(url);
        }

        long tiempoInicio = System.currentTimeMillis();
        //contador de hilos, como en el proyecto de progra
        AtomicInteger threadsActivos = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Future<?> future = executor.submit(() -> {
                procesarCola(colaExploracion, filtro, threadsActivos);
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Error en thread: " + e.getMessage());
            }
        }

        executor.shutdown();
        ajustarDimensionMatriz();

        long tiempoTotal = (System.currentTimeMillis() - tiempoInicio) / 1000;
        System.out.println("\nExploración completada. Total de sitios: " + sitiosVisitados.size());
        System.out.println("Tiempo total: " + tiempoTotal + " segundos (" + (tiempoTotal / 60.0) + " minutos)");
    }

    private void procesarCola(BlockingQueue<NodoExploracion> colaExploracion,
                              String filtro,
                              AtomicInteger threadsActivos) {

        int errorCount = 0;

        while (sitiosVisitados.size() < limitePaginas) {
            NodoExploracion nodoActual = null;

            try {
                nodoActual = colaExploracion.poll(2, TimeUnit.SECONDS);

                if (nodoActual == null) {
                    if (threadsActivos.get() == 0 && colaExploracion.isEmpty()) {
                        break;
                    }
                    continue;
                }

                threadsActivos.incrementAndGet();

                if (sitiosVisitados.contains(nodoActual.url) ||
                        !nodoActual.url.startsWith("http") ||
                        sitiosVisitados.size() >= limitePaginas) {
                    threadsActivos.decrementAndGet();
                    continue;
                }

                if (filtro != null && !filtro.isEmpty() && !nodoActual.url.contains(filtro)) {
                    threadsActivos.decrementAndGet();
                    continue;
                }

                procesarUrl(nodoActual, colaExploracion, filtro);

                errorCount = 0;
                threadsActivos.decrementAndGet();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                errorCount++;
                threadsActivos.decrementAndGet();

                if (errorCount > 10) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    errorCount = 0;
                }
            }
        }
    }

    private void procesarUrl(NodoExploracion nodoActual,
                             BlockingQueue<NodoExploracion> colaExploracion,
                             String filtro) {
        try {
            if (!sitiosVisitados.add(nodoActual.url)) {
                return;
            }

            System.out.println("[" + sitiosVisitados.size() + "/" + limitePaginas + "] Explorando: " + nodoActual.url);

            int indiceOrigen = registrarUrl(nodoActual.url);
            if (indiceOrigen < 0) {
                return;
            }

            Document documento = Jsoup.connect(nodoActual.url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .maxBodySize(1024 * 1024 * 2)
                    .get();

            Elements hiperenlaces = documento.select("a[href]");
            int enlacesAgregados = 0;
            int maxEnlacesPorPagina = 100;


            for (Element hiperenlace : hiperenlaces) {
                if (enlacesAgregados >= maxEnlacesPorPagina ||
                        sitiosVisitados.size() >= limitePaginas) {
                    break;
                }

                String urlDestino = hiperenlace.attr("abs:href");

                urlDestino = limpiarUrl(urlDestino);


                if (urlDestino == null || urlDestino.isEmpty() || !urlDestino.startsWith("http")) {
                    continue;
                }

                urlDestino = limpiarUrl(urlDestino);

                if (filtro != null && !filtro.isEmpty()) {
                    boolean esValido = urlDestino.contains(".una.ac.cr") ||
                            urlDestino.equals("http://una.ac.cr") ||
                            urlDestino.equals("https://una.ac.cr") ||
                            urlDestino.contains("://una.ac.cr/");
                    if (!esValido) {
                        continue;
                    }
                }

                int indiceDestino = registrarUrl(urlDestino);

                if (indiceDestino >= 0 && indiceOrigen < limitePaginas && indiceDestino < limitePaginas) {
                    synchronized (matrizLock) {
                        matrizConexiones[indiceOrigen][indiceDestino] = 1;
                    }
                    enlacesAgregados++;
                }

                if (!sitiosVisitados.contains(urlDestino) && urlsEnCola.add(urlDestino)) {
                    if (nodoActual.profundidad < 4 && sitiosVisitados.size() < limitePaginas) {
                        colaExploracion.offer(new NodoExploracion(urlDestino, nodoActual.profundidad + 1));
                    }
                }
            }

            Thread.sleep(50);

        } catch (Exception excepcion) {

        }
    }

    private int registrarUrl(String url) {
        String urlLimpia = limpiarUrl(url);
        return diccionarioUrls.computeIfAbsent(urlLimpia, k -> {
            if (contadorIndices.get() >= limitePaginas) {
                return -1;
            }
            int indice = contadorIndices.getAndIncrement();
            if (indice < limitePaginas) {
                listaUrls.add(url);
                return indice;
            }
            return -1;
        });
    }

    private String limpiarUrl(String url) {
        try {
            if (url == null || url.isEmpty()) {
                return url;
            }
            url = url.toLowerCase().trim();

            if (url.startsWith("http://")) {
                url = url.replace("http://", "https://");
            }

            int hashPos = url.indexOf('#');
            if (hashPos != -1) {
                url = url.substring(0, hashPos);
            }

            int queryPos = url.indexOf('?');
            if (queryPos != -1) {
                url = url.substring(0, queryPos);
            }

            if (url.endsWith("/")) {
                long slashCount = url.chars().filter(ch -> ch == '/').count();
                if (slashCount > 3) {
                    url = url.substring(0, url.length() - 1);
                }
            } else {

                long slashCount = url.chars().filter(ch -> ch == '/').count();
                if (slashCount == 2) {
                    url = url + "/";
                }
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }

    private void ajustarDimensionMatriz() {
        int totalUrls = listaUrls.size();
        if (totalUrls < limitePaginas) {
            int[][] matrizAjustada = new int[totalUrls][totalUrls];
            for (int i = 0; i < totalUrls; i++) {
                System.arraycopy(matrizConexiones[i], 0, matrizAjustada[i], 0, totalUrls);
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