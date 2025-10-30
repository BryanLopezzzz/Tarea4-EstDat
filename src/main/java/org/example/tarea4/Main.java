package org.example.tarea4;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            List<String> urls = Arrays.asList(
                    "https://www.revistas.una.ac.cr/index.php/uniciencia",
                    "https://www.una.ac.cr/",
                    "https://www.revistas.una.ac.cr/"
            );

            webcrawler.WebCrawler crawler = new webcrawler.WebCrawler();
            crawler.startCrawl("C:\\Users\\StevenBrenesChavarrí\\Desktop\\PROYECTO\\Documentos", urls, "una");

            System.out.println("Crawling finalizado.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
