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
import android.os.Environment;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

import static android.graphics.Bitmap.Config.ARGB_4444;


public class FileManagement {

    public static void writeFile(Activity a, String name, PGMImage image, Canvas canvas, ImageView imgView){

        int permission = ActivityCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //if (permission == PackageManager.PERMISSION_GRANTED) {

        /*ByteBuffer byteBuffer = ByteBuffer.allocate(img.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(img);
        */

        //File sdcard = Environment.getExternalStorageDirectory();

        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        int[] img = null;
        Bitmap _bitmapScaled = Bitmap.createBitmap(image.getWidth(), image.getHeight(),  Bitmap.Config.ARGB_8888);

        for(int i = 0; i < image.getHeight(); i++){
            img = image.getRowAt(i);
            _bitmapScaled.setPixels(img, 0, img.length, 0, i, img.length, 1);
        }
        img = image.getRowAt(0);
        System.out.println("img value: " + img[0]);
        _bitmapScaled.getPixels(img, 0, img.length, 0, 0, 1, 1);
        /*for(int i = 0; i < img.length; i++){
            if(img[i] != 0)
                System.out.println("img value: " + img[i]);
        }*/
        //_bitmapScaled = FileManagement.toGrayscale(_bitmapScaled);
        //_bitmapScaled.setPixel(50, 50, img[50]);
        DrawCanvas(_bitmapScaled, imgView);
        /*}else{
            System.out.println("Saknar Permission");
        }*/
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

    public static void DrawCanvas(Bitmap b, ImageView img){

        Canvas canvas = new Canvas(b);

        //canvas.drawColor(Color.CYAN);

        canvas.drawBitmap(
                b,
                0,
                0,
                null
        );


        img.setImageBitmap(b);

    }
}

