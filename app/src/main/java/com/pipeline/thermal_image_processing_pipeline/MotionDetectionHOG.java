package com.pipeline.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import static org.opencv.core.CvType.CV_8UC1;

public class MotionDetectionHOG {

    /*

        Motion detection by means of a histogram of oriented gradients (HOG).

        In order to use, see the following example code:

            MotionDetectionHOG md = new MotionDetectionHOG();
            md.detect(imageTemp);

    */

    public MotionDetectionHOG() {
    }

    public void detect(PGMImage image) {

        // Convert image to something OpenCV can handle.
        Bitmap b = DisplayHandler.generateBitmapFromPGM(image);
        Mat currentFrame = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        // Convert to grayscale. May not be necessary if image is already grayscale.
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);

        // Initialize HOG person detector.
        HOGDescriptor hog = new HOGDescriptor();
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        // Detect people in the image.
        MatOfRect foundLocations = new MatOfRect();
        MatOfDouble foundWeights = new MatOfDouble();

        hog.detectMultiScale(currentFrame, foundLocations, foundWeights);

        // Draw the boxes.
        Rect[] facesArray = foundLocations.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(currentFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(100), 3);
        }

        // Show sensor type.
        Imgproc.putText(currentFrame, "HOG", new Point(5, 270), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,255,255),2);

        // Display frame.
        Utils.matToBitmap(currentFrame, b);
        image.setProcessedBitmap(b);

    }

}
