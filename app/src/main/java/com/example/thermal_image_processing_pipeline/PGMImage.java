package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class PGMImage {
    private int[][] data;
    private int[][] colorValues;
    private int width, height, maxValue, brightness;
    private double contrast = 1.0;
    private Bitmap bitmap = null, processedBitmap = null;

    public PGMImage(int[][] data, int maxValue){
        this.data = data;
        this.width = data[0].length;
        this.height = data.length;
        this.maxValue = maxValue;
        this.colorValues = new int[height][width];
    }

    public int getWidth() { return width; };
    public int getHeight() { return height; };
    public int getMaxValue() { return maxValue; };

    public void setMaxValue(final int maxValue) { this.maxValue = maxValue; };

    public int[] getRowAt(int col){
        int[] newRow = new int[width];
        for(int i = 0; i < width; i++){
            newRow[i] = data[col][i];
        }
        return newRow;
    }

    public int[] getColAt(int row){
        int[] newCol = new int[height];
        for(int i = 0; i < height; i++){
            newCol[i] = data[i][height];
        }
        return newCol;
    }

    public int getDataAt(int row, int col){
        return data[col][row];
    }

    public int getColorvalueAt(int row, int col){
        return colorValues[col][row];
    }

    public void setDataAt(int x, int y, int v){
        data[y][x] = v;
    }

    public void setData(int[][] data){
        this.data = data;
    }

    public int[][] getData(){
        return data;
    }

    public void setColorValues(int[][] colorValues) {
        this.colorValues = colorValues;
    }

    public int[][] getColorValues() {
        return colorValues;
    }

    public Bitmap getProcessedBitmap() {
        return processedBitmap;
    }

    public void setProcessedBitmap(Bitmap processedBitmap) {
        this.processedBitmap = processedBitmap;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void draw(ImageView view){
        if(bitmap != null)
            DisplayHandler.DrawCanvas(bitmap, view);
        else
            DisplayHandler.DrawCanvas(processedBitmap, view);
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public double getContrast() {
        return contrast;
    }

    public void setContrast(double contrast) {
        this.contrast = contrast;
    }
}
