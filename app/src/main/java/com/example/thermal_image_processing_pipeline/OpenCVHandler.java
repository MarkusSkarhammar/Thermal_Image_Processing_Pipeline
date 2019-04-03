package com.example.thermal_image_processing_pipeline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.nio.ByteBuffer;

import androidx.core.app.ActivityCompat;

import static org.opencv.core.CvType.CV_8UC1;

public class OpenCVHandler {

    public static void equalizeHist(PGMImage img, Activity a, ImageView view) {

        /*Mat src = new Mat(img.getHeight(), img.getWidth(), CV_8UC1);
        Utils.bitmapToMat(DisplayHandler.generateBitmapFromPGM(img), src);
        Imgproc.cvtColor(src, src, CV_8UC1);*/

        Bitmap b = DisplayHandler.generateBitmapFromPGM(img);
        Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

        String path = Environment.getExternalStorageDirectory().getPath()+"/Gallery/";
        System.out.println("" + path);
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().getPath()+"/Download/" + "before.jpg", src);

        Photo.fastNlMeansDenoising(src, src);

        //Imgproc.equalizeHist(src, src);
        Imgproc.createCLAHE(3).apply(src, src);

            //there could be some processing
        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);
        
        Utils.matToBitmap(src, b);

        DisplayHandler.DrawCanvas(b, view);


        //Mat dst = new Mat(src.rows(), src.cols(), src.type());

        //Imgproc.equalizeHist(src, dst);
        /*
        Mat source = Imgcodecs.imread("GrayScaleParrot.png",
                Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat destination = new Mat(source.rows(), source.cols(), source.type());
        Imgproc.equalizeHist(source, destination);
        Imgcodecs.imwrite("enhancedParrot.jpg", destination);
        */


        path = Environment.getExternalStorageDirectory().getPath()+"/Gallery/";
        System.out.println("" + path);
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory().getPath()+"/Download/" + "after.jpg", src);


    }
}
