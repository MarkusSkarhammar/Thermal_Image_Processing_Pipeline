package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;

import com.pipeline.thermal_image_processing_pipeline.Denoising;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.example.thermal_image_processing_pipeline.MainActivity.MAX_THREADS;
import static com.example.thermal_image_processing_pipeline.MainActivity.brightness;
import static com.example.thermal_image_processing_pipeline.MainActivity.contrast;
import static com.example.thermal_image_processing_pipeline.MainActivity.denoising;
import static com.example.thermal_image_processing_pipeline.MainActivity.sharpening;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;
import static org.opencv.core.CvType.CV_16SC1;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

/**
 * Class for handling OpenCv processing.
 */
public class OpenCVHandler {
    private ArrayList<SubArray> subArrays = new ArrayList<>();
    private boolean isAlive = true;
    private int[] dataAsInt;
    /**
     * Apply equalize histogram on the image.
     * @param img The image to be processed.
     */
    public void equalizeHist(PGMImage img) {
        //Bitmap b = DisplayHandler.generateBitmapFromPGM(img);

        //Mat src = new Mat (b.getWidth(), b.getHeight(), CV_8UC1);
        //Utils.bitmapToMat(b, src);

        //Denoising.MeanFilter(img.getDataList(), 0, 0, str_w, str_h);

        /*
        dataAsInt = new int[img.getHeight()*img.getWidth()];

        for(int i = 0; i < MAX_THREADS -1; i++) {
            subArrays.add(new SubArray(createThread(
                    img,
                    0,
                    0 + (str_h / MAX_THREADS) * i,
                    str_w,
                    (str_h / MAX_THREADS) * (i + 1),
                    i)
            ));
        }

        // Start the threads.
        for(SubArray sb : subArrays)
            sb.getT().start();

        // Set this thread to also do a part of the conversion.
        subArrays.add(new SubArray());
        equalizeHist(
                img,
                0,
                0 + (str_h/ MAX_THREADS) * (MAX_THREADS -1),
                str_w,
                str_h,
                MAX_THREADS -1
        );

        // Merge all the different sub arrays of int into the final int array. Also make sure the threads are dead.
        while(isAlive){
            isAlive = false;
            for(SubArray sb : subArrays){
                if(sb.getT() != null){
                    if(!sb.getT().isAlive()){
                        addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength(), false);
                    }
                    else
                        isAlive = true;
                }else{
                    addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength(), false);
                }
            }
        }

        // Reset state.
        isAlive = true;
        subArrays.clear();


        Mat src = new Mat (img.getWidth(), img.getHeight(), CvType.CV_32S);
        src.put(0, 0, img.getDataList());
        src.convertTo(src, CvType.CV_8UC1);
        //Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

        // This is too slow!
        //Photo.fastNlMeansDenoising(src, src);

        Imgproc.createCLAHE(3).apply(src, src);

        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);

        src.convertTo(src, CvType.CV_32S);
        src.put(0,0, img.getDataList());
        //src.convertTo(src,-1,1,-90);



        img.setDataList(dataAsInt);
        */
        img.setProcessedBitmap(DisplayHandler.generateBitmapFromPGM(img));
    }

    private void equalizeHist(PGMImage img, int wFrom, int hFrom, int wTo, int hTo, int pos){
        SubArray sbTemp = subArrays.get(pos);
        int length = (wTo-wFrom)*(hTo-hFrom);
        int[] tempData = new int[length];

        addDataFromArray(tempData, img.getDataListRaw(), (hFrom*str_w), length, true);

        if(denoising) Denoising.MeanFilter(tempData, 0, 0, wTo-wFrom, hTo-hFrom, pos);
        /*
        int temp = 0;
        for(int i = 0; i < tempData.length; i++){
            temp = tempData[i];
            tempData[i] = 0xff000000 | (temp << 16) | (temp << 8) | temp;
        }
        */
        //Denoising.MedianFilter(tempData, wFrom, hFrom, wTo, hTo);

        Mat src = new Mat (img.getWidth(), (hTo-hFrom), CvType.CV_32SC(3));
        src.put(0, 0, tempData);
        src.convertTo(src, CvType.CV_8UC1);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.cvtColor(src, src, Imgproc.COLOR_);

        // This is too slow!
        //if(denoising) Photo.fastNlMeansDenoising(src, src);

        Imgproc.createCLAHE(3).apply(src, src);

        PixelCorrection(src);

        ContrastAndBrightness(src, contrast, brightness);

        Sharpening(src, sharpening);

        Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);

        src.convertTo(src, CvType.CV_32SC(3));

        src.put(0,0, tempData);


        int temp;
        for(int i = 0; i < tempData.length; i++){
            temp = tempData[i];
            tempData[i] = 0xff000000 | (temp << 16) | (temp << 8) | temp;
        }

        sbTemp.setAll(tempData, (hFrom*str_w), length);
    }

    /**
     * Copy an array into another.
     * @param arrayTo Destination array.
     * @param arrayFrom Source array.
     * @param at Start at position.
     * @param length Length of the data to copy.
     */
    private void addDataFromArray(int[] arrayTo, int[] arrayFrom, int at, int length, boolean atSource) {
        if(atSource)
            System.arraycopy(arrayFrom, at, arrayTo, 0, length);
        else
            System.arraycopy(arrayFrom, 0, arrayTo, at, length);
    }

    private Thread createThread(final PGMImage img, final int wFrom, final int hFrom, final int wTo, final int hTo, final int  pos){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                equalizeHist(img, wFrom, hFrom, wTo, hTo, pos);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("OpenCV_thread_" + pos);
        return thread;
    }


    private void ContrastAndBrightness(Mat src,  double contrast, int brightness){

        src.convertTo(src, -1, contrast, brightness);

    }


    private void PixelCorrection(Mat src){
        Imgproc.medianBlur(src, src, 3);
    }

    private void Sharpening(Mat src, int progress){
        if(progress != 0){
            Mat array = new Mat(3, 3, CV_16SC1);
            //int[][] array = null;
            if(progress == 3){

                array.put(0 , 0, -1);
                array.put(1 , 0, -1);
                array.put(2 , 0, -1);

                array.put(0 , 1, -1);
                array.put(1 , 1,  9);
                array.put(2 , 1, -1);

                array.put(0 , 2, -1);
                array.put(1 , 2, -1);
                array.put(2 , 2, -1);


            }else if(progress == 2){

                array.put(0 , 0, 0);
                array.put(1 , 0, -1);
                array.put(2 , 0, 0);

                array.put(0 , 1, -1);
                array.put(1 , 1,  5);
                array.put(2 , 1, -1);

                array.put(0 , 2, 0);
                array.put(1 , 2, -1);
                array.put(2 , 2, 0);

            }else if(progress == 1){
                array.put(0 , 0, 1);
                array.put(1 , 0, 1);
                array.put(2 , 0, 1);

                array.put(0 , 1, 1);
                array.put(1 , 1,  -7);
                array.put(2 , 1, 1);

                array.put(0 , 2, 1);
                array.put(1 , 2, 1);
                array.put(2 , 2, 1);
            }

            Imgproc.filter2D(src, src, src.depth(), array);


        }
    }
}
