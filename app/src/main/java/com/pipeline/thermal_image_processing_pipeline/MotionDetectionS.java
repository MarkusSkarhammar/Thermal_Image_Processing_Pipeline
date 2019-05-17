package com.pipeline.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;

public class MotionDetectionS {

    /*

        Motion detection by means of background frame subtraction.

        In order to use, see the following example code:

            MotionDetectionS md = new MotionDetectionS();
            md.detect(imageTemp);

     */

    private Mat backgroundFrame = null;

    public MotionDetectionS() {
    }

    public void clearBackgroundFrame() {
        backgroundFrame = null;
    }

    public void detect(PGMImage image) {

        // Convert image to something OpenCV can handle.
        Bitmap b = DisplayHandler.generateBitmapFromPGM(image);
        Mat currentFrame = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        // Convert to grayscale. May not be necessary if image is already grayscale.
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);

        // Apply filter to reduce noise.
        Imgproc.GaussianBlur(currentFrame, currentFrame, new Size(21, 21), 0);

        // Check if we have a valid background frame. If not, save one.
        if (backgroundFrame == null) {
            backgroundFrame = currentFrame;
        }

        // Compute the difference between the background frame and current frame.
        // I.e. delta = background - current
        Mat deltaFrame = new Mat();
        Mat thresh = new Mat();

        Core.absdiff(backgroundFrame, currentFrame, deltaFrame);
        Imgproc.threshold(deltaFrame, thresh, 25, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1, -1), 2);

        // Find contours.
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // If the number of contours exceeds a certain amount? Then we've detected motion.
        for(int i = 0; i < contours.size(); ++i) {
            if(Imgproc.contourArea(contours.get(i)) < 500) {
                continue;
            }

            System.out.println("Motion detected!!!");

        }
    }
}
