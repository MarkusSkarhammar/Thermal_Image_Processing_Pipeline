package com.pipeline.thermal_image_processing_pipeline;

public class HistogramDataEntry {
    int pixelDensity, amount, colorValuePreH, cdf, h;

    public HistogramDataEntry(int pixelDensity, int amount, int maxValue){
        this.pixelDensity = pixelDensity;
        this.cdf = 0;
        this.h = 0;
        this.amount = amount;
        this.colorValuePreH = (int)(((double)(pixelDensity) / maxValue) * 255);
    }

    public int getColorValuePreH() {
        return colorValuePreH;
    }

    public int getPixel() {
        return pixelDensity;
    }

    public int getCdf() {
        return cdf;
    }

    public int getH() {
        return h;
    }

    public void setCdf(int cdf) {
        this.cdf = cdf;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void add(){
        amount++;
    }

    public int getAmount() {
        return amount;
    }
}
