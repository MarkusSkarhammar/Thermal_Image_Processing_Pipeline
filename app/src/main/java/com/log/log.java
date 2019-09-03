package com.log;

import android.app.Activity;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Custom class for displaying something onto a textView.
 */
public class log {
    private static TextView output;
    private static Activity a;
    private static ArrayList<String> inputs = new ArrayList<>();
    public static long imageDataTime, processImageTime, getImageDataTime, processImageDataTime, FPSTimeStamp = 0, shutterAndGainTime = 0, CLAHETime = 0, denoiseTime = 0, filterTime = 0;
    public static int amountInStream, totalImageAmount, imageCount, FPS;

    public static void setTextView(final TextView tv){
        output = tv;
    }

    public static void setActivity(final Activity activity){
        a = activity;
    }

    public static void setGetImageDataTime(final long input){
        getImageDataTime = input;
        imageDataTime += input;
    }

    public static void setProcessImageDataTime(final long input){
        processImageDataTime = input;
        processImageTime += input;
    }

    public static void setAmountInStream(final int input){
        amountInStream = input;
    }

    public static void writeToOutputs(){
        a.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                output.setText("Time to get image data: " +getImageDataTime + "\nTime to process image data: " + processImageDataTime +  "\nAmount in stream: " + amountInStream +
                        "\nFPS: " + FPS +
                        "\nAverage time for retrieving data: " + (imageDataTime / totalImageAmount) + " ms." +
                        "\nAverage time for processing data: " + (processImageTime / totalImageAmount) + " ms." +
                        "\nAverage time for shutter and gain: " + (shutterAndGainTime/totalImageAmount) + " ms." +
                        "\nAverage time for CLAHE: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\nAverage time to denoise: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\nAverage time to apply filter: " + (filterTime/totalImageAmount) + " ms." +
                        "\nTotal images processed: " + totalImageAmount);
            }
        });
    }

    public static void checkFPS(){
        if(FPSTimeStamp == 0) FPSTimeStamp = System.currentTimeMillis();

        if(System.currentTimeMillis() - FPSTimeStamp >= 1000){
            FPS = imageCount;
            imageCount = 0;
            FPSTimeStamp = System.currentTimeMillis();
        }
    }

}
