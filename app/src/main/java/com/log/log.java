package com.log;

import android.app.Activity;
import android.widget.TextView;


import java.util.ArrayList;

public class log {
    private static TextView output;
    private static Activity a;
    private static ArrayList<String> inputs = new ArrayList<>();
    private static String o, o2, o3;

    public static void setTextView(final TextView tv){
        output = tv;
    }

    public static void setActivity(final Activity activity){
        a = activity;
    }

    public static void addInput(final String input){
        o = input;
        writeToOutputs();
    }

    public static void addInput2(final String input){
        o2 = input;
        writeToOutputs();
    }

    public static void addInput3(final String input){
        o3 = input;
        writeToOutputs();
    }

    private static void writeToOutputs(){
        a.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                output.setText(o + "\n" + o2 + "\n" + o3);
            }
        });
    }

}
