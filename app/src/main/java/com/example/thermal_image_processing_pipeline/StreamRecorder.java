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

        // Check if play file already exists and delete it.
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/Download/" + filename + ".play");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void writeToPlayFile(int counter) throws IOException {

        // Check if we have write permission.
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {

            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Open the file
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
            int dataRaw[] = image.getDataListRaw();

            // Convert image to something PGMIO.write() can use.
            for(int h = 0; h < image.getHeight(); h++) {
                for (int w = 0; w < image.getWidth(); w++) {
                    data[h][w] = dataRaw[(h * image.getWidth()) + w];
                }
            }

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/Download/" + filename + "~" + counter + ".PGM");

            try {

                writeToPlayFile(counter);
                PGMIO.write(data, file, image.getMaxValue());

            } catch (Exception e) {
                e.printStackTrace();
            }

            ++counter;
        }
    }
}
