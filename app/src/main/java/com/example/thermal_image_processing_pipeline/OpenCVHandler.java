package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import static org.opencv.core.CvType.CV_8UC1;

public class OpenCVHandler {

    public static void equalizeHist(PGMImage img) {


        Bitmap b = DisplayHandler.generateBitmapFromPGM(img);
        Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);


        Photo.fastNlMeansDenoising(src, src);

        Imgproc.createCLAHE(3).apply(src, src);

        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);

        //src.convertTo(src,-1,1,-90);

        Utils.matToBitmap(src, b);

        img.setProcessedBitmap(b);
    }

    public static void ContrastAndBrightness(Bitmap b,  double contrast, int brightness){

        Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        src.convertTo(src,-1, contrast, brightness);
        Utils.matToBitmap(src, b);
    }
}
