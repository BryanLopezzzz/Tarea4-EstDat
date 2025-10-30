package webcrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class WebCrawler {

    private Set<String> visited = new HashSet<>();
    private int maxDepth = 6;
    private String outputFolder;
    private Set<String> stopWords = new HashSet<>(Arrays.asList(
            "el", "la", "de", "y", "que", "a", "en", "un", "una"
    ));

    public void startCrawl(String ruta, List<String> rootUrls, String patron) {
        outputFolder = ruta;
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (String url : rootUrls) {
            crawl(url, 0, patron);
        }
    }

    private void crawl(String url, int depth, String patron) {
        try {
            if (depth > maxDepth || visited.contains(url) || !url.startsWith("http")) {
                return;
            }

            if (patron == null || patron.isEmpty() || url.contains(patron)) {
                System.out.println("[" + depth + "] Visitando: " + url);
                visited.add(url);

                // Obtener HTML (simulado)
                String html = ""; // Aquí iría la descarga con HttpClient o Jsoup

                // Guardar HTML (desactivado)
                String safeName = toSafeFilename(url);
                // FileWriter htmlFile = new FileWriter(new File(outputFolder, safeName + ".html"));
                // htmlFile.write(html);
                // htmlFile.close();

                // Extraer texto y normalizar (simulado)
                String plainText = extractTextFromHtml(html);
                String normalizedText = normalizeText(plainText);

                try (FileWriter writer = new FileWriter(new File(outputFolder, safeName + ".txt"))) {
                    writer.write(normalizedText);
                }

                // Obtener enlaces y continuar (simulado)
                List<String> links = new ArrayList<>(); // Aquí iría Jsoup.select("a[href]")

                for (String link : links) {
                    String fullUrl = resolveUrl(url, link);
                    crawl(fullUrl, depth + 1, patron);
                }
            }
        } catch (Exception ex) {
            System.out.println("Error en " + url + ": " + ex.getMessage());
        }
    }

    private String extractTextFromHtml(String html) {
        // Aquí se usaría Jsoup.parse(html).text()
        return html;
    }

    private String normalizeText(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String clean = lower.replaceAll("[^\\p{L}\\p{Nd}]+", " ");
        StringBuilder noStop = new StringBuilder();

        for (String word : clean.split(" ")) {
            if (!stopWords.contains(word) && !word.isEmpty()) {
                noStop.append(word).append(" ");
            }
        }

        return noStop.toString().trim();
    }

    private String toSafeFilename(String url) {
        return Base64.getEncoder().encodeToString(url.getBytes());
    }

    private String resolveUrl(String baseUrl, String href) {
        try {
            URL base = new URL(baseUrl);
            URL full = new URL(base, href);
            return full.toString();
        } catch (MalformedURLException e) {
            return "";
        }
    }
}
