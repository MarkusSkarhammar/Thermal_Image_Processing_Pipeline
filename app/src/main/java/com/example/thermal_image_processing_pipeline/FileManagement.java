package com.example.thermal_image_processing_pipeline;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

import static android.graphics.Bitmap.Config.RGB_565;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;


public class FileManagement {

    public static void writeFile(Activity a, String name){

        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    public static float[] getGain(Activity a, String filename, int width, int height){
        final float[] gain = new float[height*width];

        // Check if we have read permission
        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Get the text file
            File file = new File(sdcard, "/Download/" + filename + ".lgc");

            if (file.exists()) {
                int largest = 0, c, b1, b2;
                try {
                    //byte[] data = Files.readAllBytes(Paths.get(file.getPath()));
                    final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));

                    for(int y = 0; y < height; ++y){
                        for(int x = 0; x < width; ++x){
                            final int readValue1 = stream.read();
                            if(readValue1 != -1){
                                final int readValue2 = stream.read();
                                if(readValue2 != -1){
                                    b2 = readValue1; b1 = readValue2;
                                    c = b1 << 8;
                                    c = (c | b2);
                                    if(c > largest)
                                        largest = c;
                                    gain[(y*str_w) + x] = c;
                                }else
                                    break;
                            }else
                                break;
                        }
                    }
                    for(int y = 0; y < height; ++y){
                        for(int x = 0; x < width; ++x){
                            gain[(y*str_w)+x] /= largest;
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return gain;
            }

        }

        return gain;
    }

    private static FloatBuffer fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).asFloatBuffer();
    }

    public static ArrayList<PGMImage> getShutterValuesFromStorage(Activity a){
        ArrayList<PGMImage> shutterImages = new ArrayList<>();

        for(int i = 1; i <= MainActivity.NBR_SHUTTER_IMAGES; i++){
            //Get the a shutter file
            shutterImages.add(readFile(a, "shutter" + (i)));
        }
        //shutterImages.add(readFile(a, "shutterTest"));

        return shutterImages;
    }

    public static PGMImage readFile(Activity a, String filename) {

        // Check if we have read permission
        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Get the text file
            File file = new File(sdcard, "/Download/" + filename + ".PGM");

            System.out.println("File exists: " + file.toString());
            System.out.println("File exists: " + file.exists());

            PGMImage img = null;

            if (file.exists()) {

                try {
                    img = PGMIO.read(file);
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                    System.out.println(e);
                }
            }
            return img;
        }
        else
            return null;
    }

}

