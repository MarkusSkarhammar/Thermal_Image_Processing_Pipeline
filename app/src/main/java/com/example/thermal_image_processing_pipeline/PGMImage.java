package com.example.thermal_image_processing_pipeline;

public class PGMImage {
    private int[][] data;
    private int width, height, maxValue;

    public PGMImage(int[][] data, int maxValue){
        this.data = data;
        this.width = data.length;
        this.height = data[0].length;
        this.maxValue = maxValue;
    }

    public int getWidth() { return width; };
    public int getHeight() { return height; };
    public int getMaxValue() { return maxValue; };

    public int[] getRowAt(int col){
        int[] newRow = new int[width];
        for(int i = 0; i < width; i++){
            newRow[i] = data[i][col];
        }
        return newRow;
    }

    public int[] getColAt(int row){
        int[] newCol = new int[height];
        for(int i = 0; i < height; i++){
            newCol[i] = data[height][i];
        }
        return newCol;
    }

    public int getDataAt(int row, int col){
        return data[row][col];
    }

}
