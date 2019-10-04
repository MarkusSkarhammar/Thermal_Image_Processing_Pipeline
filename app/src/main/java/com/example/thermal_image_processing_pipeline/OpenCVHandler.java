package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.log.log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;

import static android.graphics.Bitmap.Config.ARGB_8888;
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

        Bitmap b = DisplayHandler.generateBitmapFromPGM(img);
        img.setProcessedBitmap(b);

        //FileManagement.createPGM(MainActivity.activity, "RawImage", b);

        Mat src = new Mat ( b.getHeight(), b.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

        // This is too slow!
        //if(denoising) Photo.fastNlMeansDenoising(src, src);


        if(denoising) {
            timeStampStart = System.currentTimeMillis();
            PixelCorrection(src);
            timeStampEnd = System.currentTimeMillis();
            log.denoiseTime += timeStampEnd - timeStampStart;
        }

        if(CLAHE){
            timeStampStart = System.currentTimeMillis();
            Imgproc.createCLAHE(3).apply(src, src);
            timeStampEnd = System.currentTimeMillis();
            log.CLAHETime += timeStampEnd - timeStampStart;
        }

        timeStampStart = System.currentTimeMillis();
        ContrastAndBrightness(src, contrast, brightness);

        Sharpening(src, sharpening);
        timeStampEnd = System.currentTimeMillis();
        log.filterTime += timeStampEnd - timeStampStart;

        //Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);
        //src.convertTo(src, CvType.CV_32S);

        Utils.matToBitmap(src, b);

        //src.get(0, 0, img.getColorData());

        //img.setProcessedBitmap(DisplayHandler.generateBitmapFromPGM(img));
        /*int[] data = img.getDataList();
        int pos = 0;
        for(byte b : img.getColorData()) {
            //data[pos] = b;
            data[pos] = 0xff000000 | ((b & 0xff) << 16) | ((b & 0xff) << 8) | (b & 0xff);
            pos++;
        }
        img.setProcessedBitmap(DisplayHandler.generateBitmapFromPGM(img));

        try {
            PGMIO.write(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    private void ContrastAndBrightness(Mat src,  double contrast, int brightness){

        src.convertTo(src, -1, contrast, brightness);

    }


    private void PixelCorrection(Mat src){
        Imgproc.medianBlur(src, src, 3);
    }

    private void Sharpening(Mat src, int progress){
        if(progress != 0){

            Mat kernel = new Mat(3, 3, CV_16SC1);

            if (progress == 1) {

                // Minor sharpening.

                kernel.put(0 , 0, 0);
                kernel.put(0 , 1, -1);
                kernel.put(0 , 2, 0);

                kernel.put(1 , 0, -1);
                kernel.put(1 , 1,  5);
                kernel.put(1 , 2, -1);

                kernel.put(2 , 0, 0);
                kernel.put(2 , 1, -1);
                kernel.put(2 , 2, 0);

            } else if (progress == 2) {

                // Sharpening.

                kernel.put(0 , 0, -1);
                kernel.put(0 , 1, -1);
                kernel.put(0 , 2, -1);

                kernel.put(1 , 0, -1);
                kernel.put(1 , 1,  9);
                kernel.put(1 , 2, -1);

                kernel.put(2 , 0, -1);
                kernel.put(2 , 1, -1);
                kernel.put(2 , 2, -1);

            } else if (progress == 3) {

                // Excessive sharpening.

                kernel.put(0 , 0, 1);
                kernel.put(1 , 0, 1);
                kernel.put(2 , 0, 1);

                kernel.put(0 , 1, 1);
                kernel.put(1 , 1,  -7);
                kernel.put(2 , 1, 1);

                kernel.put(0 , 2, 1);
                kernel.put(1 , 2, 1);
                kernel.put(2 , 2, 1);

                /*

                // Edge enhancement.
                // NOTE: Doesn't work. Black image.

                kernel = new Mat(5, 5, CvType.CV_16SC1);

                kernel.put(0, 0, -1);
                kernel.put(0, 1, -1);
                kernel.put(0, 2, -1);
                kernel.put(0, 3, -1);
                kernel.put(0, 4, -1);

                kernel.put(1, 0, -1);
                kernel.put(1, 1, 2);
                kernel.put(1, 2, 2);
                kernel.put(1, 3, 2);
                kernel.put(1, 4, -1);

                kernel.put(2, 0, -1);
                kernel.put(2, 1, 2);
                kernel.put(2, 2, 8);
                kernel.put(2, 3, 2);
                kernel.put(2, 4, -1);

                kernel.put(3, 0, -1);
                kernel.put(3, 1, 2);
                kernel.put(3, 2, 2);
                kernel.put(3, 3, 2);
                kernel.put(3, 4, -1);

                kernel.put(4, 0, -1);
                kernel.put(4, 1, -1);
                kernel.put(4, 2, -1);
                kernel.put(4, 3, -1);
                kernel.put(4, 4, -1);

                Core.divide(1.0/8.0, kernel, kernel);
                */

            }

            Imgproc.filter2D(src, src, src.depth(), kernel);

        }
    }
}
