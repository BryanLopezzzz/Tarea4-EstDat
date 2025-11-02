package org.example.tarea4;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        AnalisisPR config = new AnalisisPR.Builder()
                .conUrlsIniciales(Arrays.asList(
                        "https://www.revistas.una.ac.cr/index.php/uniciencia",
                        "https://www.una.ac.cr/",
                        "https://www.revistas.una.ac.cr/"
                ))
                .conPatronFiltro("una.ac.cr")
                .conFactorAmortiguacion(0.85)
                .conUmbralConvergencia(0.0001)
                .conIteracionesMaximas(100)
                .construir();

        Sistema coordinador = new Sistema(config);
        coordinador.ejecutarAnalisisCompleto();
    }
}
