package com.example.thermal_image_processing_pipeline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StreamRecorder {

    /*

        Frame stream recorder.

        In order to use, see the following example code:

            StreamRecorder sr = new StreamRecorder(MainActivity.this, "test", 10);
            sr.record(imageTemp);

     */

    private Activity activity;
    private String filename;
    private int frames;
    private int counter = 0;

    public StreamRecorder(Activity activity, String filename, int frames) {

        this.activity = activity;
        this.filename = filename;
        this.frames = frames;

    }

    private void writeToPlayFile(int counter) throws IOException {

        // Check if we have write permission.
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Create the file
            File file = new File(sdcard, "/Download/" + filename + ".play");
            BufferedOutputStream playFile = new BufferedOutputStream(new FileOutputStream(file, true));

            try {

                playFile.write(Integer.toString(counter).getBytes());
                playFile.write("\n".getBytes());

            } finally {
                playFile.close();
            }
        }
    }

    public void record(PGMImage image) {

        if (counter < frames) {

            int data[][] = new int[image.getHeight()][image.getWidth()];
            int y = 0;
            int x = 0;
            int maxValue = 0;

            for (int i = 0; i < image.getDataList().length; ++i) {

                data[y][x] = image.getDataList()[i];
                ++x;

                if (x == image.getWidth()) {
                    ++y;
                    x = 0;
                }
                if (maxValue < image.getDataList()[i]) {
                    maxValue = image.getDataList()[i];
                }
            }

            /*
            System.out.println("DATA CONTENT:");
            for (int[] i : data) {
                for (int j : i) {
                    System.out.print(j + " ");
                }
                System.out.println();
            }
            */

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/Download/" + filename + "~" + counter + ".PGM");

            try {

                writeToPlayFile(counter);
                PGMIO.write(data, file, maxValue);

            } catch (Exception e) {
                e.printStackTrace();
            }

            ++counter;

        }
    }

}
