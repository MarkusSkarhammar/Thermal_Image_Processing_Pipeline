package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Custom representation class for a thermal image.
 */
public class PGMImage {
    private int width, height, maxValue;
    private Bitmap processedBitmap = null;
    private int[] dataList, dataListRaw;

    /**
     *
     * @param dataList The raw image data.
     */
    public PGMImage(int[] dataList, int[] dataListRaw){
        this.width = 384;
        this.height = 288;
        this.dataList = dataList;
        this.dataListRaw = dataListRaw;
    }

    public PGMImage(int[] dataList){
        this.width = 384;
        this.height = 288;
        this.dataListRaw = new int[width*height];
        this.dataList = new int[width*height];
        int pos = 0;
        for(int i : dataList){
            this.dataListRaw[pos] = i;
            pos++;
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getMaxValue() {

        if (maxValue != 0) {        // Check that we haven't already done this.
            return maxValue;
        }

        for (int i : dataListRaw) {
            if (maxValue < i) {
                maxValue = i;
            }
        }

        return maxValue;
    }

    public Bitmap getProcessedBitmap() {
        return processedBitmap;
    }

    public void setProcessedBitmap(Bitmap processedBitmap) {
        this.processedBitmap = processedBitmap;
    }

    public int[] getDataList() {
        return dataList;
    }

    public int[] getDataListRaw() {
        return dataListRaw;
    }

    public void setDataList(int[] dataList) {
        this.dataList = dataList;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
