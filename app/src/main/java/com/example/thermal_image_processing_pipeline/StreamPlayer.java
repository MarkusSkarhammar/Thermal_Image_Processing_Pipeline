package com.example.thermal_image_processing_pipeline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class StreamPlayer {

    /*

        Frame stream player.

     */

    private Activity activity;
    private String filename;
    private ArrayList<Integer> playList = new ArrayList<Integer>();
    private int counter = 0;
    private int last = 0;

    public StreamPlayer(Activity a, String filename) {

        this.activity = a;
        this.filename = filename;

        // Check if we have read permission
        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Get the text file
            File file = new File(sdcard, "/Download/" + filename + ".play");

            if (file.exists()) {
                try {

                    Scanner scan = new Scanner(file);

                    while (scan.hasNextLine()) {
                        playList.add(Integer.parseInt(scan.nextLine()));
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                last = playList.get(playList.size() - 1);
            }
        }
    }

    public void play() {

        for (int i = 0; i < last; ++i) {
            String f = filename + "~" + i;
            MainActivity.imageStreamOffline.add(FileManagement.readFile(activity, f));
        }
    }
}
