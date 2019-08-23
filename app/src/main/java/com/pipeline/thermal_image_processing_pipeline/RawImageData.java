package com.pipeline.thermal_image_processing_pipeline;

public class RawImageData {
    private byte[] data;
    private double gain;

    public RawImageData(byte[] data, double gain){
        this.gain = gain;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public double getGain() {
        return gain;
    }
}
