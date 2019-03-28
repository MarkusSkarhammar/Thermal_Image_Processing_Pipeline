package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.widget.ImageView;

import java.util.ArrayList;

import static android.graphics.Bitmap.Config.RGB_565;

public class DisplayHandler {
    public static int GRAY = 0, RED = -1, BLUE = -2, GREEN = -3;

    public static Bitmap generateBitmapFromPGM(PGMImage image, int color){
        return generateBitmap(image, color);
    }

    public static Bitmap generateBitmapFromPGM(PGMImage image){

        return generateBitmap(image, GRAY);
    }

    private static Bitmap generateBitmap(PGMImage image, int color){
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),  RGB_565);


        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                bitmap.setPixel(x, y, getColor(image.getDataAt(x, y), color, image));
            }
        }
        return bitmap;
    }

    public static void DrawCanvas(Bitmap b, ImageView img){

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

    private static int getColor(int intensity, int color, PGMImage image){
        int colorValue = (int) ( (double) (((double)intensity / (double)image.getMaxValue()) * 255) );
        switch(intensity){
            case 0:
                return Color.BLACK;
            case 255:
                return Color.WHITE;
                default:
                    Color c = new Color();
                    if(color == RED)
                        return c.rgb(colorValue, 0, 0);
                    else if(color == BLUE)
                        return c.rgb(0, 0, colorValue);
                    else if(color == GREEN)
                        return c.rgb(0, colorValue, 0);
                    else
                    return c.rgb(colorValue, colorValue, colorValue);
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
