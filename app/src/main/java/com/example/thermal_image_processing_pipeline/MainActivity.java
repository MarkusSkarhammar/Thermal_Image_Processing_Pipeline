package com.example.thermal_image_processing_pipeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pipeline.thermal_image_processing_pipeline.Pipeline;

public class MainActivity extends AppCompatActivity {
    PGMImage img = null, img2 = null, shutter = null;
    Canvas canvas= null;
    Pipeline pipeline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.loadLibrary("opencv_java4");

        // Check if we have read permission
        //for(int i = 0; i < PERMISSIONS_STORAGE.length; i++){
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            //}
        }

        setContentView(R.layout.activity_main);

        /*final Button button = findViewById(R.id.button_1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView txt = findViewById(R.id.textView);
                //img = FileManagement.readFile(MainActivity.this, "Raw_raw000000");
                img = FileManagement.readFile(MainActivity.this, "shutter.raw_off000000");
                if(img != null){
                    txt.setText("First: " + img.getWidth() + ", second: " + img.getHeight());
                    TextView txt2 = findViewById(R.id.textView2);
                    String output = "";
                    for(int row = 0; row < img.getWidth() / 100; row++){
                        for(int col = 0; col < img.getHeight() / 100; col++){
                            output += " [" + img.getDataAt(row, col) + "],";
                        }
                    }
                    txt2.setText(output);
                }
            }
        });

        /*final Button button2 = findViewById(R.id.button_2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shutter = FileManagement.readFile(MainActivity.this, "Shutter_off000000");
                pipeline = new Pipeline(MainActivity.this, shutter.getWidth(), shutter.getHeight());
                pipeline.getShutterValues(shutter);

                // Image 1
                img = FileManagement.readFile(MainActivity.this, "Corri_raw000070");

                pipeline.processImage(img);
                pipeline.brightness(img, 90);

                ImageView imgView = findViewById(R.id.imageView1);
                img.draw(imgView);

                /*
                img = FileManagement.readFile(MainActivity.this, "Hawkes_Bay_original");
                imgView = findViewById(R.id.imageView2);
                if(img !=null)
                    DisplayHandler.DrawCanvas(DisplayHandler.generateBitmapFromPGM(img), imgView);

                pipeline.Tone_Mapping(img);
                imgView = findViewById(R.id.imageView3);
                if(img !=null)
                    DisplayHandler.DrawCanvas(DisplayHandler.generateBitmapFromPGM(img), imgView);

                img = FileManagement.readFile(MainActivity.this, "test_image");
                pipeline.Tone_Mapping(img);
                imgView = findViewById(R.id.imageView4);
                if(img !=null)
                    DisplayHandler.DrawCanvas(DisplayHandler.generateBitmapFromPGM(img), imgView);


            }
        });*/

        init();
    }

    private void init(){
        shutter = FileManagement.readFile(MainActivity.this, "Shutter_off000000");
        pipeline = new Pipeline(MainActivity.this, shutter.getWidth(), shutter.getHeight());
        pipeline.getShutterValues(shutter);

        // Image 1
        img = FileManagement.readFile(MainActivity.this, "Corri_raw000070");

        pipeline.processImage(img);

        ImageView imgView = findViewById(R.id.imageView1);
        img.draw(imgView);

        // Setup SeekBars for brightness, contrast and sharpening

        final SeekBar brightness=(SeekBar) findViewById(R.id.seekBar1);
        final SeekBar contrast=(SeekBar) findViewById(R.id.seekBar2);
        final SeekBar sharpening=(SeekBar) findViewById(R.id.seekBar3);

        SeekBarListener seekBarListener = new SeekBarListener(img, imgView);
        brightness.setOnSeekBarChangeListener(seekBarListener);
        contrast.setOnSeekBarChangeListener(seekBarListener);
        sharpening.setOnSeekBarChangeListener(seekBarListener);
    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


}
