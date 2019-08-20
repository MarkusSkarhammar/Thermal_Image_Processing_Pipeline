package com.example.thermal_image_processing_pipeline;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

public class saveImage {

    public static void saveBitmap(Bitmap b, String filename) {

        Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(b, tmp);
        Imgproc.cvtColor(tmp, tmp, CvType.CV_8UC1);
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Download/" + filename + ".png", tmp);

    }

    public static void savePGM(PGMImage image, String filename) {

        int list[] = image.getDataList();
        int height = image.getHeight();
        int width = image.getWidth();

        int data[][] = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y][x] = list[y + x];
            }
        }

        savePGM(data, filename);
    }

    public static void savePGM(int[] vector, String filename) {

        int width = 384;
        int height = 288;

        int data[][] = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y][x] = vector[y + x];
            }
        }

        savePGM(data, filename);
    }


    public static void savePGM(int[][] array, String filename) {

        File file = new File(Environment.getExternalStorageDirectory() + "/Download/" + filename + ".pgm");
        try {
            PGMIO.write(array, file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private static int findMaxVal(int[][] array) {
        int maxVal = 0;

        for (int a[] : array) {
            for (int i : a) {
                if (i > maxVal) {
                    maxVal = i;
                }
            }
        }

        if (maxVal <= 255) {
            maxVal = 255;
        } else if (maxVal <= 65535) {
            maxVal = 65535;
        }

        return maxVal;
    }
}
