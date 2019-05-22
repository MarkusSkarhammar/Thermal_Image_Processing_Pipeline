package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_16SC1;
import static org.opencv.core.CvType.CV_8UC1;

/**
 * Class for handling OpenCv processing.
 */
public class OpenCVHandler {

    /**
     * Apply equalize histogram on the image.
     * @param img The image to be processed.
     */
    public static void equalizeHist(PGMImage img) {

        Bitmap b = DisplayHandler.generateBitmapFromPGM(img);

        Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

        // This is too slow!
        //Photo.fastNlMeansDenoising(src, src);

        Imgproc.createCLAHE(3).apply(src, src);

        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);

        //src.convertTo(src,-1,1,-90);

        Utils.matToBitmap(src, b);

        img.setProcessedBitmap(b);
    }

    /**
     * Apply contrast and brightness.
     * @param b The bitmap to be processed.
     * @param contrast The contrast value.
     * @param brightness The brightness value.
     */
    public static void ContrastAndBrightness(Bitmap b,  double contrast, int brightness){

        Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        src.convertTo(src,-1, contrast, brightness);
        Utils.matToBitmap(src, b);
    }

    /**
     * Apply pixel correction on a bitmap.
     * @param b The bitmap to be processed.
     */
    public static void PixelCorrection(Bitmap b){
        Mat src = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        Imgproc.medianBlur(src, src, 3);
        Utils.matToBitmap(src, b);

    }

    /**
     * Apply sharpness on a bitmap.
     * @param b The bitmap ro be processed.
     * @param progress The mode of sharpening.
     */
    public static void Sharpening(Bitmap b, int progress){
        if(progress != 0){
            Mat array = new Mat(3, 3, CV_16SC1);
            //int[][] array = null;
            if(progress == 1){

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

                array.put(0 , 0, 1);
                array.put(1 , 0, 1);
                array.put(2 , 0, 1);

                array.put(0 , 1, 1);
                array.put(1 , 1,  -7);
                array.put(2 , 1, 1);

                array.put(0 , 2, 1);
                array.put(1 , 2, 1);
                array.put(2 , 2, 1);

            }else if(progress == 3){

                array.put(0 , 0, -1);
                array.put(1 , 0, -1);
                array.put(2 , 0, -1);

                array.put(0 , 1, -1);
                array.put(1 , 1,  9);
                array.put(2 , 1, -1);

                array.put(0 , 2, -1);
                array.put(1 , 2, -1);
                array.put(2 , 2, -1);
            }

            Mat src = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
            Utils.bitmapToMat(b, src);

            Imgproc.filter2D(src, src, src.depth(), array);

            Utils.matToBitmap(src, b);


        }
    }
}
