package com.example.thermal_image_processing_pipeline;

public class SubArray {
    private int[] data;
    private int start, length;

    public SubArray(int[] data, int start, int length){
        this.data = data;
        this.start = start;
        this.length = length;
    }

    public int[] getData() {
        return data;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }
}
