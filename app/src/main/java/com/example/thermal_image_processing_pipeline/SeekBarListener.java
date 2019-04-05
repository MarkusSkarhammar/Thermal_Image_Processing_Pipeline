package com.example.thermal_image_processing_pipeline;

import android.widget.ImageView;
import android.widget.SeekBar;



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
        OpenCVHandler.Sharpening(image.getBitmap(), image.getSharpening());
        DisplayHandler.DrawCanvas(image.getBitmap(), view);

        image.setBrightness(progress);
    }

    private void Contrast(int progress) {
        image.setBitmap(image.getProcessedBitmap().copy(image.getProcessedBitmap().getConfig(), true));
        OpenCVHandler.ContrastAndBrightness(image.getBitmap(), (double)(progress * 2)/100, image.getBrightness());
        OpenCVHandler.Sharpening(image.getBitmap(), image.getSharpening());
        DisplayHandler.DrawCanvas(image.getBitmap(), view);

        image.setContrast((double)(progress * 2)/100);
    }

    private void Sharpening(int progress) {
        image.setBitmap(image.getProcessedBitmap().copy(image.getProcessedBitmap().getConfig(), true));
        OpenCVHandler.ContrastAndBrightness(image.getBitmap(), image.getContrast(), image.getBrightness());

        OpenCVHandler.Sharpening(image.getBitmap(), progress);
        image.setSharpening(progress);

        DisplayHandler.DrawCanvas(image.getBitmap(), view);

    }
}
