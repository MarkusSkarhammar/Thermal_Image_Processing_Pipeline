package com.example.thermal_image_processing_pipeline;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.log.log;

import static com.example.thermal_image_processing_pipeline.MainActivity.CLAHE;
import static com.example.thermal_image_processing_pipeline.MainActivity.contourArea_value;
import static com.example.thermal_image_processing_pipeline.MainActivity.denoising;
import static com.example.thermal_image_processing_pipeline.MainActivity.imageStream;
import static com.example.thermal_image_processing_pipeline.MainActivity.shutterGain;
import static com.example.thermal_image_processing_pipeline.MainActivity.stream;
import static com.example.thermal_image_processing_pipeline.MainActivity.threshold_value;
import static com.example.thermal_image_processing_pipeline.MainActivity.FalseColor;

public class GUI {

    private PGMImage imageTemp;

    // Motion sensor stuff.
    private TextView threshold, contourArea;
    private TextInputEditText thresholdInput, contourInput;
    private final String THRESHOLD_TEXT = "Threshold value: ", CONTOURAREA_TEXT = "ContourArea value: ";


    GUI(Activity a){
        init(a);
    }

    private void init(Activity a) {
        // Setup seekBars
        {
            final SeekBar brightness = a.findViewById(R.id.seekBar1);
            final SeekBar contrast = a.findViewById(R.id.seekBar2);
            final SeekBar sharpening = a.findViewById(R.id.seekBar3);
            final SeekBar sensorType = a.findViewById(R.id.seekBarSensorType);

            SeekBarListener seekBarListener = new SeekBarListener();
            brightness.setOnSeekBarChangeListener(seekBarListener);
            contrast.setOnSeekBarChangeListener(seekBarListener);
            sharpening.setOnSeekBarChangeListener(seekBarListener);
            sensorType.setProgress(0);
            sensorType.setOnSeekBarChangeListener(seekBarListener);

        }

        // Setup log
        {
            TextView txtView = a.findViewById(R.id.textView3);
            log.setTextView(txtView);
            log.setActivity(a);
        }

        // Setup motion sensor threshold and contour layout
        {
            threshold = a.findViewById(R.id.thresholdText);
            threshold.setText(THRESHOLD_TEXT + threshold_value);
            thresholdInput = a.findViewById(R.id.thresholdInput);
            Button clickButton = a.findViewById(R.id.buttonThreshold);
            clickButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateThreshold();
                }
            });

            contourArea = a.findViewById(R.id.contourAreaText);
            contourArea.setText(CONTOURAREA_TEXT + contourArea_value);
            contourInput = a.findViewById(R.id.contourInput);
            clickButton = a.findViewById(R.id.buttonContour);
            clickButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateContourArea();
                }
            });
        }

        // Setup Denoise toggle
        {
            final Switch denoise = a.findViewById(R.id.Denoisiong);
            denoise.setChecked(true);
            denoise.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    denoising = !denoising;
                }
            });
        }

        //Setup Shutter and gain correction toggle
        {
            final Switch shutterAndGain = a.findViewById(R.id.ShuttterGain);
            shutterAndGain.setChecked(true);
            shutterAndGain.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    shutterGain = !shutterGain;
                }
            });
        }

        //Setup CLAHE toggle
        {
            final Switch CLAHEToggle = a.findViewById(R.id.CLAHE);
            CLAHEToggle.setChecked(true);
            CLAHEToggle.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    CLAHE = !CLAHE;
                }
            });
        }

        //Setup False Color toggle
        {
            final Switch FalseColorToggle = a.findViewById(R.id.FalseColor);
            FalseColorToggle.setChecked(false);
            FalseColorToggle.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FalseColor = !FalseColor;
                }
            });
        }



    }

    /**
     * Change the motion sensor's threshold value.
     */
    private void updateThreshold(){
        String s = "" + thresholdInput.getText();
        try {
            if(Integer.parseInt(s) != 0){
                threshold.setText(THRESHOLD_TEXT + s);
                threshold_value = Integer.parseInt(s);
            }
        }catch (NumberFormatException e){
            thresholdInput.setText("");
        }
    }

    /**
     * Change the motion sensor's contour area value.
     */
    private void updateContourArea(){
        String s = "" + contourInput.getText();
        try {
            if(Integer.parseInt(s) != 0){
                contourArea.setText(CONTOURAREA_TEXT + s);
                contourArea_value = Integer.parseInt(s);
            }
        }catch (NumberFormatException e){
            contourInput.setText("");
        }
    }

    /**
     * Send a task for the UI thread.
     * @param imgView The imageView to be updated.
     */
    public void updateView(final Activity a, final ImageView imgView){
        a.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(imageStream != null && imageStream.size() > 0){
                    imageTemp = imageStream.get(0);
                    if(imageTemp != null && imageTemp.getProcessedBitmap() != null){
                        DisplayHandler.DrawCanvas(imageTemp.getProcessedBitmap(), imgView);
                        imageStream.remove(0);
                        log.setAmountInStream(stream.size());
                        log.checkFPS();
                        log.writeToOutputs();
                    }

                    /*
                    if(MainActivity.detectionTypeChange = true){
                        TextView textView = a.findViewById(R.id.SensorTypeText);
                        switch (detectionType){
                            case 0:
                                textView.setText("Sensor type: motion detection");
                                break;
                            case 1:
                                textView.setText("Sensor type: human detection");
                                break;
                            case 2:
                                textView.setText("Sensor type: none");
                                break;
                        }
                        MainActivity.detectionTypeChange = false;
                    }
                    */

                }
            }
        });
    }
}
