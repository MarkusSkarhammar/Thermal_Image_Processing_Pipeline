package com.example.thermal_image_processing_pipeline;

/**
 * Container class for the sub arrays. Used in the conversion of raw data to an int array.
 */
public class SubArray {
    private int[] data, dataRaw;
    private int start, length;
    private Thread t;

    public SubArray(Thread t){
        this.t = t;
    }

    public SubArray(){
    }

    public void setAll(int[] data, int[] dataraw, int start, int length){
        this.data = data;
        this.dataRaw = dataraw;
        this.start = start;
        this.length = length;
    }

    public int[] getData() {
        return data;
    }

    public int[] getDataRaw(){
        return dataRaw;
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
