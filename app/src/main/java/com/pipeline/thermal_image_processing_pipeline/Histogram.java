package com.pipeline.thermal_image_processing_pipeline;

public class Histogram {
    int pixelDensity, cdf, h;

    public Histogram(int pixelDensity, int cdf, int h){
        this.pixelDensity = pixelDensity;
        this.cdf = cdf;
        this.h = h;
    }

    public int getPixelDensity() {
        return pixelDensity;
    }

    public int getCdf() {
        return cdf;
    }

    public int getH() {
        return h;
    }
}
