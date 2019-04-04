package com.example.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_16SC1;
import static org.opencv.core.CvType.CV_8UC1;


public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private PGMImage image = null;
    private ImageView view = null;

    public SeekBarListener(PGMImage image, ImageView view){
        this.image = image;
        this.view =  view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(seekBar.getId() == R.id.seekBar1){
            Brightness(progress);
        }
        else if(seekBar.getId() == R.id.seekBar2){
            Contrast(progress);
        }
        else if(seekBar.getId() == R.id.seekBar3){
            Sharpening(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void Brightness(int progress){
        image.setBitmap(image.getProcessedBitmap().copy(image.getProcessedBitmap().getConfig(), true));
        OpenCVHandler.ContrastAndBrightness(image.getBitmap(), image.getContrast(), progress);
        DisplayHandler.DrawCanvas(image.getBitmap(), view);

        image.setBrightness(progress);
    }

    private void Contrast(int progress) {
        image.setBitmap(image.getProcessedBitmap().copy(image.getProcessedBitmap().getConfig(), true));
        OpenCVHandler.ContrastAndBrightness(image.getBitmap(), (double)(progress * 2)/100, image.getBrightness());
        DisplayHandler.DrawCanvas(image.getBitmap(), view);

        image.setContrast((double)(progress * 2)/100);
    }

    private void Sharpening(int progress) {
        image.setBitmap(image.getProcessedBitmap().copy(image.getProcessedBitmap().getConfig(), true));
        OpenCVHandler.ContrastAndBrightness(image.getBitmap(), image.getContrast(), image.getBrightness());
        if(progress != 0){
            Mat array = new Mat(3, 3, CV_16SC1);
            //int[][] array = null;
            if(progress == 1){

                array.put(0 , 0, -1);
                array.put(1 , 0, -1);
                array.put(2 , 0, -1);

                array.put(0 , 1, -1);
                array.put(1 , 1,  9);
                array.put(2 , 1, -1);

                array.put(0 , 2, -1);
                array.put(1 , 2, -1);
                array.put(2 , 2, -1);


            }else if(progress == 2){

                array.put(0 , 0, 1);
                array.put(1 , 0, 1);
                array.put(2 , 0, 1);

                array.put(0 , 1, 1);
                array.put(1 , 1,  -7);
                array.put(2 , 1, 1);

                array.put(0 , 2, 1);
                array.put(1 , 2, 1);
                array.put(2 , 2, 1);

            }else if(progress == 3){

                array.put(0 , 0, -1);
                array.put(1 , 0, -1);
                array.put(2 , 0, -1);

                array.put(0 , 1, -1);
                array.put(1 , 1,  9);
                array.put(2 , 1, -1);

                array.put(0 , 2, -1);
                array.put(1 , 2, -1);
                array.put(2 , 2, -1);
            }

            Bitmap b = DisplayHandler.generateBitmapFromPGM(image);
            Mat src = new Mat(b.getWidth(), b.getHeight(), CV_8UC1);
            Utils.bitmapToMat(b, src);
            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);

            //Imgproc.Laplacian(src, src, CV_8UC1, 3, 1, 0);
            Imgproc.filter2D(src, src, src.depth(), array);

            Imgproc.cvtColor(src, src, Imgproc.COLOR_GRAY2RGB, 4);
            Utils.matToBitmap(src, image.getBitmap());


        }

        DisplayHandler.DrawCanvas(image.getBitmap(), view);

    }
}
