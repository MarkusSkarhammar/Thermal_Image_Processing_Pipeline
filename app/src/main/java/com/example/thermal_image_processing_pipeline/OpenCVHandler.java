package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.log.log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static com.example.thermal_image_processing_pipeline.MainActivity.CLAHE;
import static com.example.thermal_image_processing_pipeline.MainActivity.brightness;
import static com.example.thermal_image_processing_pipeline.MainActivity.contrast;
import static com.example.thermal_image_processing_pipeline.MainActivity.denoising;
import static com.example.thermal_image_processing_pipeline.MainActivity.pipeline;
import static com.example.thermal_image_processing_pipeline.MainActivity.sharpening;
import static com.example.thermal_image_processing_pipeline.MainActivity.shutterGain;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;
import static org.opencv.core.CvType.CV_16SC1;

/**
 * Class for handling OpenCv processing.
 */
public class OpenCVHandler {
    private ArrayList<SubArray> subArrays = new ArrayList<>();
    private long timeStampStart, timeStampEnd;
    /**
     * Apply equalize histogram on the image.
     * @param img The image to be processed.
     */
    public void equalizeHist(PGMImage img) {

        Mat src = new Mat (img.getWidth(), img.getHeight(), CvType.CV_8UC1);
        img.setProcessedBitmap(DisplayHandler.generateBitmapFromPGM(img));
        Utils.bitmapToMat(img.getProcessedBitmap(), src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

        // This is too slow!
        //if(denoising) Photo.fastNlMeansDenoising(src, src);

        if(CLAHE){
            timeStampStart = System.currentTimeMillis();
            Imgproc.createCLAHE(3).apply(src, src);
            timeStampEnd = System.currentTimeMillis();
            log.CLAHETime += timeStampEnd - timeStampStart;
        }

        if(denoising) {
            timeStampStart = System.currentTimeMillis();
            PixelCorrection(src);
            timeStampEnd = System.currentTimeMillis();
            log.denoiseTime += timeStampEnd - timeStampStart;
        }

        timeStampStart = System.currentTimeMillis();
        ContrastAndBrightness(src, contrast, brightness);

        Sharpening(src, sharpening);
        timeStampEnd = System.currentTimeMillis();
        log.filterTime += timeStampEnd - timeStampStart;

        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB);

        Utils.matToBitmap(src, img.getProcessedBitmap());
    }

    private void ContrastAndBrightness(Mat src,  double contrast, int brightness){

        src.convertTo(src, -1, contrast, brightness);

    }


    private void PixelCorrection(Mat src){
        Imgproc.medianBlur(src, src, 3);
    }

    private void Sharpening(Mat src, int progress){
        if(progress != 0){
            Mat array = new Mat(3, 3, CV_16SC1);
            //int[][] array = null;
            if(progress == 3){

                array.put(0 , 0, -1);
                array.put(1 , 0, -1);
                array.put(2 , 0, -1);

                array.put(0 , 1, -1);
                array.put(1 , 1,  9);
                array.put(2 , 1, -1);

                array.put(0 , 2, -1);
                array.put(1 , 2, -1);
                array.put(2 , 2, -1);


            }else if(progress == 2){

                array.put(0 , 0, 0);
                array.put(1 , 0, -1);
                array.put(2 , 0, 0);

                array.put(0 , 1, -1);
                array.put(1 , 1,  5);
                array.put(2 , 1, -1);

                array.put(0 , 2, 0);
                array.put(1 , 2, -1);
                array.put(2 , 2, 0);

            }else if(progress == 1){
                array.put(0 , 0, 1);
                array.put(1 , 0, 1);
                array.put(2 , 0, 1);

                array.put(0 , 1, 1);
                array.put(1 , 1,  -7);
                array.put(2 , 1, 1);

                array.put(0 , 2, 1);
                array.put(1 , 2, 1);
                array.put(2 , 2, 1);
            }

            Imgproc.filter2D(src, src, src.depth(), array);


        }
    }
}
