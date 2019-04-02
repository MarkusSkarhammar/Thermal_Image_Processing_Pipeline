package com.pipeline.thermal_image_processing_pipeline;

import android.app.Activity;

import com.example.thermal_image_processing_pipeline.FileManagement;
import com.example.thermal_image_processing_pipeline.PGMImage;

import java.io.FileInputStream;

public class Pipeline {
    private Shutter_Correction shutter;
    private float[][] gain;
    public Pipeline(Activity a, int width, int height){
        shutter = new Shutter_Correction();
        getGain(a, width, height);
    }

    public void getShutterValues(PGMImage shutterImage){
        shutter.getShutterValues(shutterImage);
    }

    /**
     *
     * @param image The image to be processed.
     * @return The processed thermal image.
     */
    public void processImage(PGMImage image){

        shutter_correction(image);

        Tone_Mapping(image);
    }

    /**
     *
     * @param image The image to apply filters on.
     * @return The image with filters applied to it.
     */
    public void applyFilters(PGMImage image){

    }

    public void shutter_correction(PGMImage image){
        shutter.applyShutterAndGain(image, gain);
        checkMaxValue(image);
    }

    private void getGain(Activity a, int width, int height){
        gain = FileManagement.getGain(a, "supplied", width, height);
    }

    private void checkMaxValue(PGMImage image){
        int maxValue = 0, data = 0;
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                if((data = image.getDataAt(x, y)) > maxValue)
                    maxValue = data;
            }
        }
        image.setMaxValue(maxValue);
    }

    public void Tone_Mapping(PGMImage image){
        HistogramEqualization he = new HistogramEqualization(256);
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                he.add(image.getDataAt(x, y), image.getMaxValue());
            }
        }
        he.generateHistogramData(image.getHeight()*image.getWidth());
        he.getHistogramEqualizationColorValues(image);
    }
}
