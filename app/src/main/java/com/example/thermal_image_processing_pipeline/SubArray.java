package com.example.thermal_image_processing_pipeline;

public class SubArray {
    private int[] data;
    private int start, length;
    private Thread t;

    public SubArray(Thread t){
        this.t = t;
    }

    public SubArray(){
    }

    public void setAll(int[] data, int start, int length){
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

    public Thread getT() {
        return t;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
