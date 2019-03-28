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
    }

    /**
     *
     * @param image The image to apply filters on.
     * @return The image with filters applied to it.
     */
    public void applyFilters(PGMImage image){

    }

    private void shutter_correction(PGMImage image){
        shutter.applyShutter(image, gain);
    }

    private void getGain(Activity a, int width, int height){
        gain = FileManagement.getGain(a, "supplied", width, height);
    }
}
