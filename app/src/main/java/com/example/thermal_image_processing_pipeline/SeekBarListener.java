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
        MainActivity.brightness = progress;
    }

    private void Contrast(int progress) {
        MainActivity.contrast = (double)(progress * 2) / 100;
    }

    private void Sharpening(int progress) {
        MainActivity.sharpening = progress;

    }
}
