package com.example.thermal_image_processing_pipeline;

/**
 * Container class for the sub arrays. Used in the conversion of raw data to an int array.
 */
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
}
