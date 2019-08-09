package com.pipeline.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import static org.opencv.core.Core.DFT_SCALE;
import static org.opencv.core.Core.dft;
import static org.opencv.core.Core.idft;
import static org.opencv.core.Core.log;
import static org.opencv.core.Core.magnitude;
import static org.opencv.core.Core.mulSpectrums;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.Core.pow;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;

import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.List;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC1;

public class FilterPeriodic {

    /*

        Periodic Noise Removing Filter
        https://docs.opencv.org/3.4/d2/d0b/tutorial_periodic_noise_removing_filter.html

        WIP - Not tested

     */

    public void filter(PGMImage image) {

        // Convert image to something OpenCV can handle.
        Bitmap b = DisplayHandler.generateBitmapFromPGM(image);
        Mat currentFrame = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
        Utils.bitmapToMat(b, currentFrame);

        currentFrame.convertTo(currentFrame, CV_32F);

        // it needs to process even image only
        Rect roi = new Rect(0, 0, currentFrame.cols() & -2, currentFrame.rows() & -2);
        currentFrame = new Mat(currentFrame, roi);

        // PSD calculation (start)
        Mat imgPSD = new Mat();
        calcPSD(currentFrame, imgPSD, 0);
        fftshift(imgPSD, imgPSD);
        normalize(imgPSD, imgPSD, 0, 255, Core.NORM_MINMAX);
        // PSD calculation (stop)

        //H calculation (start)
        Mat H = new Mat(roi.size(), CV_32F, new Scalar(1));
        final int r = 21;
        synthesizeFilterH(H, new Point(705, 458), r);
        synthesizeFilterH(H, new Point(850, 391), r);
        synthesizeFilterH(H, new Point(993, 325), r);
        //H calculation (stop)

        // filtering (start)
        Mat imgOut = new Mat();
        fftshift(H, H);
        filter2DFreq(currentFrame, imgOut, H);
        // filtering (stop)

        imgOut.convertTo(imgOut, CV_8UC1);
        normalize(imgOut, imgOut, 0, 255, Core.NORM_MINMAX);
        // imwrite("result.jpg", imgOut);
        // imwrite("PSD.jpg", imgPSD);
        fftshift(H, H);
        normalize(H, H, 0, 255, Core.NORM_MINMAX);
        // imwrite("filter.jpg", H);

    }

    private void fftshift(final Mat inputImg, Mat outputImg)
    {
        outputImg = inputImg.clone();

        int cx = outputImg.cols() / 2;
        int cy = outputImg.rows() / 2;

        Mat q0 = new Mat(outputImg, new Rect(0, 0, cx, cy));
        Mat q1 = new Mat(outputImg, new Rect(cx, 0, cx, cy));
        Mat q2 = new Mat(outputImg, new Rect(0, cy, cx, cy));
        Mat q3 = new Mat(outputImg, new Rect(cx, cy, cx, cy));
        Mat tmp = new Mat();

        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);
        q2.copyTo(q1);
        tmp.copyTo(q2);
    }

    Mat filter2DFreq(final Mat inputImg, Mat outputImg, final Mat H)
    {
        Mat src1 = inputImg.clone();
        Mat src2 = Mat.zeros(inputImg.size(), CV_32F);
        Mat complexI = new Mat();
        List<Mat> planes = Arrays.asList(src1, src2);
        merge(planes, complexI);

        dft(complexI, complexI, DFT_SCALE);

        Mat src3 = H.clone();
        Mat src4 = Mat.zeros(H.size(), CV_32F);
        Mat complexH = new Mat();
        List<Mat> planesH = Arrays.asList(src3, src4);
        merge(planesH, complexH);

        Mat complexIH = new Mat();
        mulSpectrums(complexI, complexH, complexIH, 0);
        idft(complexIH, complexIH);
        split(complexIH, planes);
        outputImg = planes.get(0);

        return outputImg;
    }
    void synthesizeFilterH(Mat inputOutput_H, Point center, int radius)
    {
        Point c2 = center, c3 = center, c4 = center;
        c2.y = inputOutput_H.rows() - center.y;
        c3.x = inputOutput_H.cols() - center.x;
        c4 = new Point(c3.x,c2.y);

        Scalar color = new Scalar(0);

        Imgproc.circle(inputOutput_H, center, radius, color, -1, 8);
        Imgproc.circle(inputOutput_H, c2, radius, color, -1, 8);
        Imgproc.circle(inputOutput_H, c3, radius, color, -1, 8);
        Imgproc.circle(inputOutput_H, c4, radius, color, -1, 8);
    }

    // Function calculates PSD(Power spectrum density) by fft with two flags
    // flag = 0 means to return PSD
    // flag = 1 means to return log(PSD)
    void calcPSD(final Mat inputImg, Mat outputImg, boolean flag)
    {
        Mat src1 = inputImg.clone();
        Mat src2 = Mat.zeros(inputImg.size(), CV_32F);
        Mat complexI = new Mat();
        List<Mat> planes = Arrays.asList(src1, src2);
        merge(planes, complexI);

        dft(complexI, complexI);
        split(complexI, planes);            // planes[0] = Re(DFT(I)), planes[1] = Im(DFT(I))

        // planes[0].at<float>(0) = 0;
        // planes[1].at<float>(0) = 0;

        // compute the PSD = sqrt(Re(DFT(I))^2 + Im(DFT(I))^2)^2
        Mat imgPSD = new Mat();
        magnitude(planes.get(0), planes.get(1), imgPSD);        //imgPSD = sqrt(Power spectrum density)
        pow(imgPSD, 2, imgPSD);                         //it needs ^2 in order to get PSD
        outputImg = imgPSD;

        /*
        if (flag) {
            Mat imglogPSD = new Mat();
            imglogPSD = imgPSD + Scalar.all(1);     // ?
            log(imglogPSD, imglogPSD);
            outputImg = imglogPSD;
        }
        */

    }
}
