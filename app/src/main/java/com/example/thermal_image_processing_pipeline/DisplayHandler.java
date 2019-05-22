package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Class for handling display actions.
 */
public class DisplayHandler {

    public static Bitmap generateBitmapFromPGM(PGMImage image){

        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),  ARGB_8888);
        bitmap.setPixels(image.getDataList(), 0, bitmap.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        return bitmap;
    }

    public static void DrawCanvas(Bitmap b, ImageView img){
        if(b != null){
            Canvas canvas = new Canvas(b);

            //canvas.drawColor(Color.CYAN);
            Rect source = new Rect(0, 0, b.getWidth(), b.getHeight());
            //b = rotate(b, 90);
            //b = flipHorizontal(b);
            canvas.drawBitmap(
                    b,
                    null,
                    source,
                    null
            );

            img.setImageBitmap(b);
        }
    }

    private static Bitmap flipVertical(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    private static Bitmap flipHorizontal(Bitmap b){
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    private static Bitmap rotate(Bitmap b, int degrees){
        Matrix matrix = new Matrix();

        matrix.postRotate(degrees);

        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

}
