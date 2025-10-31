package org.example.tarea4;

import java.util.List;

public class AnalisisPR {
    private final List<String> urlsIniciales;
    private final String patronFiltro;
    private final String rutaSalida;
    private final double factorAmortiguacion;
    private final double umbralConvergencia;
    private final int iteracionesMaximas;

    private AnalisisPR(Builder constructor) {
        this.urlsIniciales = constructor.urlsIniciales;
        this.patronFiltro = constructor.patronFiltro;
        this.rutaSalida = constructor.rutaSalida;
        this.factorAmortiguacion = constructor.factorAmortiguacion;
        this.umbralConvergencia = constructor.umbralConvergencia;
        this.iteracionesMaximas = constructor.iteracionesMaximas;
    }

    public List<String> getUrlsIniciales() {
        return urlsIniciales;
    }

    public String getPatronFiltro() {
        return patronFiltro;
    }

    public String getRutaSalida() {
        return rutaSalida;
    }

    public double getFactorAmortiguacion() {
        return factorAmortiguacion;
    }

    public double getUmbralConvergencia() {
        return umbralConvergencia;
    }

    public int getIteracionesMaximas() {
        return iteracionesMaximas;
    }

    public static class Builder {
        private List<String> urlsIniciales;
        private String patronFiltro = "";
        private String rutaSalida = System.getProperty("user.dir");
        private double factorAmortiguacion = 0.85;
        private double umbralConvergencia = 0.0001;
        private int iteracionesMaximas = 100;

        public Builder conUrlsIniciales(List<String> urls) {
            this.urlsIniciales = urls;
            return this;
        }

        public Builder conPatronFiltro(String patron) {
            this.patronFiltro = patron;
            return this;
        }

        public Builder conRutaSalida(String ruta) {
            this.rutaSalida = ruta;
            return this;
        }

        public Builder conFactorAmortiguacion(double factor) {
            this.factorAmortiguacion = factor;
            return this;
        }

        public Builder conUmbralConvergencia(double umbral) {
            this.umbralConvergencia = umbral;
            return this;
        }

        public Builder conIteracionesMaximas(int iteraciones) {
            this.iteracionesMaximas = iteraciones;
            return this;
        }

        public AnalisisPR construir() {
            if (urlsIniciales == null || urlsIniciales.isEmpty()) {
                throw new IllegalStateException("Debe especificar al menos una URL inicial");
            }
            return new AnalisisPR(this);
        }
    }
}
