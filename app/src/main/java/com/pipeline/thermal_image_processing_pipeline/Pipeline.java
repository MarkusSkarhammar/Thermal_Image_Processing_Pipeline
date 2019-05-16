package com.pipeline.thermal_image_processing_pipeline;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.example.thermal_image_processing_pipeline.FileManagement;
import com.example.thermal_image_processing_pipeline.MainActivity;
import com.example.thermal_image_processing_pipeline.OpenCVHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;
import com.example.thermal_image_processing_pipeline.R;
import com.log.log;

public class Pipeline {
    private Shutter_Correction shutter;
    long timeStampStart, timeStampEnd;
    private float[][] gain;

    /**
     *
     * @param a activity
     * @param width of the image
     * @param height of the image
     */
    public Pipeline(Activity a, int width, int height){
        shutter = new Shutter_Correction();
        getGain(a, width, height);
    }

    /**
     * Extract shutter image data.
     * @param shutterImage Shutter image.
     */
    public void getShutterValues(PGMImage shutterImage){
        shutter.getShutterValues(shutterImage);
    }

    /**
     * A raw thermal images goes through the whole pipeline.
     * @param image The image to be processed.
     * @return The processed thermal image.
     */
    public void processImage(PGMImage image){

        //Shutter_Correction(image);

        //timeStampStart = System.currentTimeMillis();

        OpenCVHandler.equalizeHist(image);

        //timeStampEnd = System.currentTimeMillis();
        //Log.d("Pipeline:", " Time for histogram equalization: " + (timeStampEnd - timeStampStart) + " ms.");


        //timeStampStart = System.currentTimeMillis();

        OpenCVHandler.PixelCorrection(image.getProcessedBitmap());

        ContrastAndBrightness(image.getProcessedBitmap(), MainActivity.contrast, MainActivity.brightness);

        OpenCVHandler.Sharpening(image.getProcessedBitmap(), MainActivity.sharpening);

        //timeStampEnd = System.currentTimeMillis();
        //log.addInput2(" Time to process image: " + (timeStampEnd - timeStampStart) + " ms.");
    }

    /**
     *
     * @param image The image to apply filters on.
     * @return The image with filters applied to it.
     */
    public void applyFilters(PGMImage image){

    }

    /**
     * An image's brightness and contrast is altered.
     * @param b Bitmap to be altered.
     * @param contrast The contrast value.
     * @param brightness The brightness value.
     */
    public void ContrastAndBrightness(Bitmap b, double contrast, int brightness){
        OpenCVHandler.ContrastAndBrightness(b, contrast, brightness);
    }

    /**
     * Applying shutter correction to an image.
     * @param image The image to be altered.
     */
    public void Shutter_Correction(PGMImage image){
        shutter.applyShutterAndGain(image, gain);
        checkMaxValue(image);
    }

    /**
     * Get the gain data from a file.
     * @param a The activity.
     * @param width of the image.
     * @param height of the image.
     */
    private void getGain(Activity a, int width, int height){
        gain = FileManagement.getGain(a, "supplied", width, height);
    }

    /**
     * Get the max value from an image.
     * @param image The image who's max color value is to be checked.
     */
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
