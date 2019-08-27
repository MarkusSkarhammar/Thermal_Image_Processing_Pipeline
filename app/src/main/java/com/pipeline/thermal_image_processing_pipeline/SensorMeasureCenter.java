package com.pipeline.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.MainActivity;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC1;

public class SensorMeasureCenter {

    /*

    Measure center of screen and report average value.

    In order to use, see the following example code:

        SensorMeasureCenter sMC = new SensorMeasureCenter();
        sMC.detect(imageTemp);

     */

    public SensorMeasureCenter() {
    }

    public void detect(PGMImage image) {

        int pixels = 4;
        int start_height = (MainActivity.str_h / 2) - pixels;
        int stop_height = (MainActivity.str_h / 2) + pixels;
        int start_width = (MainActivity.str_w / 2) - pixels;
        int stop_width = (MainActivity.str_w / 2) + pixels;

        int value = 0;
        int loops = 0;

        for (int height = start_height; height <= stop_height; height++) {
            for (int width = start_width; width <= stop_width; width++) {
                value += (image.getDataListRaw()[(height * MainActivity.str_w) + width]);
                loops++;
            }
        }

        value = value / loops;              // Doesn't seem to be working as it should. Find out why and fix this.

        // Convert image to something OpenCV can handle.
        Bitmap b = DisplayHandler.generateBitmapFromPGM(image);
        Mat currentFrame = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        // Convert to grayscale. May not be necessary if image is already grayscale.
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);

        // Show sensor type.
        Imgproc.putText(currentFrame, "CENTER: " + value, new Point(5, 270), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,255,255),2);

        Imgproc.rectangle(currentFrame, new Point(start_width - 1, start_height - 1), new Point(start_width + pixels + 1, start_height + pixels + 1), new Scalar(0, 255, 0));

        Utils.matToBitmap(currentFrame, b);
        image.setProcessedBitmap(b);
    }
}
