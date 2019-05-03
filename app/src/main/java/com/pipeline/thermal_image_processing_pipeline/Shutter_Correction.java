package com.pipeline.thermal_image_processing_pipeline;

import com.example.thermal_image_processing_pipeline.PGMImage;

public class Shutter_Correction {
    private int[][] shutterValues;
    private int mean = 0;
    public Shutter_Correction(){
        shutterValues = null;
    }

    /**
     * Applying shutter and gain correction to an image.
     * @param image The image to
     * @param gain The camera sensors' gain correction data.
     */
    public void applyShutterAndGain(PGMImage image, final float[][] gain){
        int[][] data = image.getData();
        float temp;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
               temp = ( (data[y][x] - shutterValues[y][x]) * ( 1 + gain[y][x]) + mean);
               data[y][x] = (int)((temp));
            }
        }
    }

    /**
     * Extract the shutter image data.
     * @param shutterImage The shutter image.
     */
    public void getShutterValues(PGMImage shutterImage){
        shutterValues = shutterImage.getData();
        mean = mean(shutterValues, shutterImage);
    }

    /**
     * Extract the mean data from the shutter image.
     * @param shutter The extracted shutter data.
     * @param image The shutter image.
     * @return
     */
    private int mean(int[][] shutter, PGMImage image){
        int value = 0;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                value += shutter[y][x];
            }
        }
        return (value / (image.getWidth()*image.getHeight()));
    }
}
