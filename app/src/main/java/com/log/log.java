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
    private static long getImageDataTime, processImageDataTime;
    private static int amountInStream;

    public static void setTextView(final TextView tv){
        output = tv;
    }

    public static void setActivity(final Activity activity){
        a = activity;
    }

    public static void setGetImageDataTime(final long input){
        getImageDataTime = input;
    }

    public static void setProcessImageDataTime(final long input){
        processImageDataTime = input;
    }

    public static void setAmountInStream(final int input){
        amountInStream = input;
    }

    public static void writeToOutputs(){
        a.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                output.setText("Time to get image data: " +getImageDataTime + "\nTime to process image data: " + processImageDataTime +  "\nAmount in stream: " + amountInStream);
            }
        });
    }

}
