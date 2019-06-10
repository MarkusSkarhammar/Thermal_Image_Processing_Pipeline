package com.example.thermal_image_processing_pipeline;

import android.widget.ImageView;
import android.widget.SeekBar;



public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

    public SeekBarListener(){
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
        }else if(seekBar.getId() == R.id.seekBarSensorType){
            changeSensor(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     *
     * @param progress The new brightness value.
     */
    private void Brightness(int progress){
        MainActivity.brightness = progress;
    }

    /**
     *
     * @param progress The new contrast value.
     */
    private void Contrast(int progress) {
        MainActivity.contrast = (double)(progress * 2) / 100;
    }

    /**
     *
     * @param progress The new sharpening mode.
     */
    private void Sharpening(int progress) {
        MainActivity.sharpening = progress;
    }

    /**
     * Switch from motion sensor to human detection sensor (or vice versa).
     */
    private void changeSensor(int progress){
        MainActivity.sensorType = progress;
        MainActivity.sensorChange = true;
    }
}
