package com.pipeline.thermal_image_processing_pipeline;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.FileManagement;
import com.example.thermal_image_processing_pipeline.PGMIO;
import com.example.thermal_image_processing_pipeline.PGMImage;
import com.example.thermal_image_processing_pipeline.SubArray;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static com.example.thermal_image_processing_pipeline.MainActivity.MAX_THREADS;
import static com.example.thermal_image_processing_pipeline.MainActivity.shutterGain;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class Shutter_Correction {
    private int[] shutterValues;
    private int mean = 0;
    private ArrayList<SubArray> subArrays = new ArrayList<>();

    public Shutter_Correction(){
        shutterValues = new int[str_w*str_h];
    }

    /**
     * Applying shutter and gain correction to an image.
     * @param image The image to
     * @param gain The camera sensors' gain correction data.
     */
    public static int GRAY = 0, RED = -1, BLUE = -2, GREEN = -3;
    public void applyShutterAndGain(PGMImage image, final float[] gain){
        int color = GREEN;
        int[] data = image.getDataList(), dataRaw = image.getDataListRaw();
        //byte[] colorValue = new byte[str_h*str_w];
        int colorValue = 0;
        float temp;
        for(int y = 0; y < str_h; ++y){
            for(int x = 0; x < str_w; ++x){
               if(shutterGain) temp = (int)( (data[(y*str_w) + x] - shutterValues[(y*str_w) + x]) * (1 + gain[(y*str_w) + x]) + mean);
               else temp = data[(y*str_w) + x];
                dataRaw[(y*str_w) + x] = (int)temp;
                colorValue = (byte)((temp / (double)image.getMaxValue()) * 255.0);
                data[(y*str_w) + x] = 0xff000000 | ((colorValue & 0xff) << 16) | ((colorValue & 0xff) << 8) | (colorValue & 0xff);
            }
        }
    }

    public void getShutterValuesFromStorage(Activity a){
        ArrayList<PGMImage> shutterValueSpatial = FileManagement.getShutterValuesFromStorage(a);
        shutterValues = new int[str_h*str_w];
        /*int colorValue;
        int[] data = new int[str_h*str_w];
        System.arraycopy(shutterValueSpatial.get(0).getDataList(), 0, data, 0, (str_w*str_h));
        for(int h=0; h<str_h;h++)
            for(int w=0;w<str_w;w++){
                colorValue = (int)(((double)data[(h*str_w) + w] / 4095.0) * 255);
                data[(h*str_w) + w] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
            }
        Bitmap b2 = DisplayHandler.generateBitmapFromArray(data);
        System.out.println();*/
        if(shutterValueSpatial.size() > 0 && shutterValueSpatial.get(0) != null){
            int total = 0;
            for(int h = 0; h < str_h; h++)
                for(int w = 0; w < str_w; w++){
                    for(PGMImage i : shutterValueSpatial){
                        if(i != null)
                            total += i.getDataList()[(h*str_w) + w];
                    }
                    total /= shutterValueSpatial.size();
                    //total *= 2.1;
                    shutterValues[(h*str_w) + w] = total;
                    total = 0;
                }
                mean();
        }
        /*
        for(int h=0; h<str_h;h++)
            for(int w=0;w<str_w;w++){
                colorValue = (int)(((double)shutterValues[(h*str_w) + w] / 4095.0) * 255);
                shutterValues[(h*str_w) + w] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
            }
         Bitmap b = DisplayHandler.generateBitmapFromArray(shutterValues);
            System.out.println();*/
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
