package com.pipeline.thermal_image_processing_pipeline;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.thermal_image_processing_pipeline.MainActivity;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.opencv.core.CvType.CV_8UC1;

public class MotionDetectionMNET {

    /*

    Motion detection by means deep learning networks (MobileNet).

    Reference: https://docs.opencv.org/master/d0/d6c/tutorial_dnn_android.html

    In order to use, see the following example code:

        MotionDetectionMNET mdMNET = new MotionDetectionMNET();
        mdMNET.detect(imageTemp);

    NOTICE: The files "MobileNetSSD_deploy.prototxt" and "MobileNetSSD_deploy.caffemodel" must be present!

    */

    private static final String TAG = "OpenCV/MobileNet";
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};
    private Net net;

    public MotionDetectionMNET(AssetManager assets, File filesDir) {

        String proto = getPath("MobileNetSSD_deploy.prototxt", assets, filesDir);
        String weights = getPath("MobileNetSSD_deploy.caffemodel", assets, filesDir);
        net = Dnn.readNetFromCaffe(proto, weights);
        Log.i(TAG, "Network loaded successfully");

    }

    // Upload file to storage and return a path.
    private static String getPath(String file, AssetManager assets, File filesDir) {
        try {

            // Read data from assets.
            BufferedInputStream inputStream = new BufferedInputStream(assets.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            // Create copy file in storage.
            File outFile = new File(filesDir, file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();

            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();

        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }

        return "";
    }

    public void detect(PGMImage image) {

        final int IN_WIDTH = MainActivity.str_w;
        final int IN_HEIGHT = MainActivity.str_h;
        //final float WH_RATIO = (float)IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.007843;
        final double MEAN_VAL = 127.5;
        final double THRESHOLD = 0.2;

        // Get a new frame
        // Convert image to something OpenCV can handle.
        Bitmap b = image.getProcessedBitmap();
        Mat currentFrame = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2RGB);   // NOTE: Find out why this is necessary.

        // Forward image through network.
        Mat blob = Dnn.blobFromImage(currentFrame, IN_SCALE_FACTOR,
                new Size(IN_WIDTH, IN_HEIGHT),
                new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false, false);
        net.setInput(blob);
        Mat detections = net.forward();
        int cols = currentFrame.cols();
        int rows = currentFrame.rows();

        detections = detections.reshape(1, (int)detections.total() / 7);

        for (int i = 0; i < detections.rows(); ++i) {

            double confidence = detections.get(i, 2)[0];

            if (confidence > THRESHOLD) {
                int classId = (int)detections.get(i, 1)[0];
                int left   = (int)(detections.get(i, 3)[0] * cols);
                int top    = (int)(detections.get(i, 4)[0] * rows);
                int right  = (int)(detections.get(i, 5)[0] * cols);
                int bottom = (int)(detections.get(i, 6)[0] * rows);

                // Draw rectangle around detected object.
                Imgproc.rectangle(currentFrame, new Point(left, top), new Point(right, bottom), new Scalar(0, 255, 0));
                String label = classNames[classId] + ": " + confidence;
                int[] baseLine = new int[1];
                Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);

                // Draw background for label.
                Imgproc.rectangle(currentFrame, new Point(left, top - labelSize.height),
                        new Point(left + labelSize.width, top + baseLine[0]),
                        new Scalar(255, 255, 255), Imgproc.FILLED);

                // Write class name and confidence.
                Imgproc.putText(currentFrame, label, new Point(left, top),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));
            }
        }

        // Convert to grayscale. May not be necessary if image is already grayscale.
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGB2GRAY);

        // Show sensor type.
        Imgproc.putText(currentFrame, "MNET", new Point(5, 270), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0,255,255),2);

        // Display frame.
        Utils.matToBitmap(currentFrame, b);

    }
}