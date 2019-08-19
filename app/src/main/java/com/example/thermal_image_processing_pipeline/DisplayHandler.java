package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import static android.graphics.Bitmap.Config.ARGB_4444;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.example.thermal_image_processing_pipeline.MainActivity.shutterGain;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

/**
 * Class for handling display actions.
 */
public class DisplayHandler {

    /**
     * Convert data to a bitmap.
     * @param image The image containing data.
     * @return A bit map.
     */
    public static Bitmap generateBitmapFromPGM(PGMImage image){

        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),  ARGB_4444);
        bitmap.setPixels(image.getDataList(), 0, bitmap.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        return bitmap;
    }

    public static Bitmap generateBitmapFromArray(int[] dataIn){


        int[] data = new int[dataIn.length];
        System.arraycopy(dataIn, 0, data, 0, dataIn.length);
        int colorValue;
        float temp;
        for(int y = 0; y < str_h; ++y){
            for(int x = 0; x < str_w; ++x){
                temp = data[(y*str_w) + x];
                colorValue = (int)(((double)temp / 4095.0) * 255);
                data[(y*str_w) + x] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(str_w, str_h,  ARGB_8888);
        bitmap.setPixels(data, 0, bitmap.getWidth(), 0, 0, str_w, str_h);
        return bitmap;
    }

    /**
     * Draw onto a canvas.
     * @param b The bitmap with which to draw.
     * @param img The imageView to with which to display the canvas.
     */
    public static void DrawCanvas(Bitmap b, ImageView img){
        if(b != null){
            Canvas canvas = new Canvas(b);

            //canvas.drawColor(Color.CYAN);
            Rect source = new Rect(0, 0, b.getWidth(), b.getHeight());
            //b = rotate(b, 90);
            //b = flipHorizontal(b);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            canvas.drawBitmap(
                    b,
                    null,
                    source,
                    paint
            );

            img.setImageBitmap(b);
        }
    }

    /**
     * Flip a bitmap vertically.
     * @param b The bitmap to flip.
     * @return A flipped bitmap.
     */
    private static Bitmap flipVertical(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    /**
     * Flip a bitmap horizontally.
     * @param b The bitmap to flip.
     * @return A flipped bitmap.
     */
    private static Bitmap flipHorizontal(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    /**
     * Rotate a bitmap.
     * @param b The bitmap to rotate.
     * @param degrees The degrees of rotation.
     * @return A rotated bitmap.
     */
    private static Bitmap rotate(Bitmap b, int degrees){
        Matrix matrix = new Matrix();

        matrix.postRotate(degrees);

        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

}
