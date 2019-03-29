package com.pipeline.thermal_image_processing_pipeline;

import com.example.thermal_image_processing_pipeline.PGMImage;

public class Shutter_Correction {
    private int[][] shutterValues;
    private int mean = 0;
    public Shutter_Correction(){
        shutterValues = null;
    }

    public void applyShutterAndGain(PGMImage image, final float[][] gain){
        int[][] data = image.getData();
        float temp = 0;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
               temp = ( Math.abs((float)(data[x][y] - shutterValues[x][y])) * (gain[x][y] + 5) + mean);
                //data[x][y] = (int)(data[x][y] + gain[x][y]) - shutterValues[x][y];
                data[x][y] = (int)temp;
            }
        }
    }

    public void getShutterValues(PGMImage shutterImage){
        shutterValues = shutterImage.getData();
        mean = mean(shutterValues, shutterImage);
    }

    private int mean(int[][] shutter, PGMImage image){
        int value = 0;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                value += shutter[x][y];
            }
        }
        return (value / (image.getWidth()*image.getHeight()));
    }
}
