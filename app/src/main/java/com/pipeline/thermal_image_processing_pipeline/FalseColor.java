package com.pipeline.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.COLORMAP_JET;

public class FalseColor {

    public static void color(PGMImage image){

        // Convert image to something OpenCV can handle.
        Bitmap b = image.getProcessedBitmap();
        Mat currentFrame = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        // Convert to grayscale. May not be necessary if image is already grayscale.
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);

        // Apply False Color.
        Imgproc.applyColorMap(currentFrame, currentFrame, COLORMAP_JET);

        // Display frame.
        Utils.matToBitmap(currentFrame, b);
        image.setProcessedBitmap(b);
    }

}
