package com.pipeline.thermal_image_processing_pipeline;

import com.example.thermal_image_processing_pipeline.PGMImage;

public class Shutter_Correction {
    private int[][] shutterValues;
    private int mean;
    public Shutter_Correction(){
        shutterValues = null;
    }

    public void applyShutter(PGMImage image, final float[][] gain){
        int[][] data = image.getData();
        float temp = 0;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                temp = ( Math.abs((float)(data[x][y] - shutterValues[x][y])) * (gain[x][y] + 10)) + mean;
                data[x][y] += (int)temp;
            }
        }
    }

    public void getShutterValues(PGMImage shutterImage){
        shutterValues = shutterImage.getData();
        mean = mean(shutterValues, shutterImage);
        /*int[][] data = shutterImage.getData();
        shutterValues = new int[shutterImage.getWidth()][shutterImage.getHeight()];
        for(int y = 0; y < shutterImage.getHeight(); ++y){
            for(int x = 0; x < shutterImage.getWidth(); ++x){
                shutterValues[x][y] = data[x][y];
            }
        }*/
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
