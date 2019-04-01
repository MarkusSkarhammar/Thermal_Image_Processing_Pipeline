package com.pipeline.thermal_image_processing_pipeline;

public class PixelTableEntry {
    int pixel, amount;

    public PixelTableEntry(int pixel){
        this.pixel = pixel;
        amount = 1;
    }

    public void add(){ amount++; };

    public int getPixel() { return pixel; };

    public int getAmount() { return amount; };
}
