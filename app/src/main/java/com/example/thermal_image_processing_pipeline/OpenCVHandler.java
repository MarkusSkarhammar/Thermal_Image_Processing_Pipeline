package com.example.thermal_image_processing_pipeline;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8UC1;

public class OpenCVHandler {

    public static void equalizeHist(PGMImage img) {

        Mat src = new Mat(img.getHeight(), img.getWidth(), CV_8UC1);

        for (int y = 0; y < img.getHeight(); ++y) {
            for (int x = 0; x < img.getWidth(); ++x) {
                src.put(y, x, img.getDataAt(x, y));
            }
        }

        Mat dst = new Mat(img.getHeight(), img.getWidth(), CV_8UC1);
        Imgproc.equalizeHist(src, dst);

        byte[] tmp = new byte[(int) (dst.total() * dst.channels())];
        dst.get(0, 0, tmp);

        System.out.println("Content: " + tmp);

    }
}
