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


public class FileManagement {

    public static void writeFile(Activity a, String name, PGMImage image){

        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    public static float[][] getGain(Activity a, String filename, int width, int height){
        final float[][] gain = new float[width][height];

        // Check if we have read permission
        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            //Find the directory for the SD Card using the API
            //*Don't* hardcode "/sdcard"
            File sdcard = Environment.getExternalStorageDirectory();

            //Get the text file
            File file = new File(sdcard, "/Download/" + filename + ".lgc");

            if (file.exists()) {

                try {
                    byte[] data = Files.readAllBytes(Paths.get(file.getPath()));

                    final BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
                    int largest = 0, pos = 0, c, b1, b2;


                    for(int y = 0; y < height; ++y){
                        for(int x = 0; x < width; ++x){
                            /*b1 = data[pos];
                            b2 = data[pos+1];
                            b1 = b1 << 8;
                            b1 = (b1 | b2);
                            gain[x][y] = b1;
                            pos++;*/
                            final int readValue1 = stream.read();
                            if(readValue1 != -1){
                                final int readValue2 = stream.read();
                                if(readValue2 != -1){
                                    b1 = readValue1; b2 = readValue2;
                                    c = b1 << 8;
                                    c = (c | b2);
                                    if(c > largest)
                                        largest = c;
                                    gain[x][y] = c/(float)largest;
                                    //gain[x][y] = c;
                                }else
                                    break;
                            }else
                                break;
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

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
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
                return img;
            }
            return null;
        }
        else
            return null;
    }

}

