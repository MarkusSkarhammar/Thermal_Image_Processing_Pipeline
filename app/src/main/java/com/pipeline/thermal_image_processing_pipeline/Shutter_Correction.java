package com.pipeline.thermal_image_processing_pipeline;

import android.app.Activity;

import com.example.thermal_image_processing_pipeline.FileManagement;
import com.example.thermal_image_processing_pipeline.PGMImage;

import java.util.ArrayList;

import static com.example.thermal_image_processing_pipeline.MainActivity.shutterGain;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class Shutter_Correction {
    private int[] shutterValues;
    private int mean = 0;

    public Shutter_Correction(){
        shutterValues = null;
    }

    /**
     * Applying shutter and gain correction to an image.
     * @param image The image to
     * @param gain The camera sensors' gain correction data.
     */
    public void applyShutterAndGain(PGMImage image, final float[] gain){

        int[] data = image.getDataListRaw();
        float temp;
        for(int y = 0; y < str_h; ++y){
            for(int x = 0; x < str_w; ++x){
               if(shutterGain) temp = ( (data[(y*str_w) + x] - shutterValues[(y*str_w) + x]) * (1 + gain[(y*str_w) + x]) + mean);
               else temp = data[(y*str_w) + x];
               data[(y*str_w) + x] = (int)(((double)temp / 4095.0) * 255);
            }
        }

    }

    public void getShutterValuesFromStorage(Activity a){
        ArrayList<PGMImage> shutterValueSpatial = FileManagement.getShutterValuesFromStorage(a);
        shutterValues = new int[str_h*str_w];
        int total = 0;
        for(int h = 0; h < str_h; h++)
            for(int w = 0; w < str_w; w++){
                for(PGMImage i : shutterValueSpatial){
                    total += i.getDataList()[(h*str_w) + w];
                }
                total /= shutterValueSpatial.size();
                shutterValues[(h*str_w) + w] = total;
                total = 0;
            }
        mean();
    }


    public int[] getShutterValues(){
        return shutterValues;
    }


    private void mean(){
        int value = 0;
        for(int y = 0; y < str_h; ++y){
            for(int x = 0; x < str_w; ++x){
                value += shutterValues[(str_w * y) + x];
            }
        }
        mean = (value / (str_h*str_w));
    }

    public int getMean() {
        return mean;
    }
}
