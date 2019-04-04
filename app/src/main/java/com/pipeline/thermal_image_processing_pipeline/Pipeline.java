package com.pipeline.thermal_image_processing_pipeline;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.example.thermal_image_processing_pipeline.FileManagement;
import com.example.thermal_image_processing_pipeline.MainActivity;
import com.example.thermal_image_processing_pipeline.OpenCVHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;
import com.example.thermal_image_processing_pipeline.R;

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

        Shutter_Correction(image);

        OpenCVHandler.equalizeHist(image);

        OpenCVHandler.PixelCorrection(image.getProcessedBitmap());
    }

    /**
     *
     * @param image The image to apply filters on.
     * @return The image with filters applied to it.
     */
    public void applyFilters(PGMImage image){

    }

    public void ContrastAndBrightness(Bitmap b, double contrast, int brightness){
        OpenCVHandler.ContrastAndBrightness(b, contrast, brightness);
    }

    public void Shutter_Correction(PGMImage image){
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
        int sections = 8, width = image.getWidth()/sections, height = image.getHeight()/sections;
        for(int ySection = 0; ySection < sections; ySection++){
            for(int xSection = 0; xSection < sections; xSection++){
                HistogramEqualization he = new HistogramEqualization(256, xSection * width, ySection * height, width, height);
                for(int y = ySection * height; y < ySection * height + height; ++y){
                    for(int x = xSection * width; x < xSection * width + width; ++x){
                        he.add(image.getDataAt(x, y), image.getMaxValue());
                    }
                }
                //he.generateHistogramData(image.getHeight()*image.getWidth());
                he.generateHistogramData(width*height);
                //he.getHistogramEqualizationColorValues(image);
            }
        }

        //image.setHasBeenProcessed(true);
    }
}
