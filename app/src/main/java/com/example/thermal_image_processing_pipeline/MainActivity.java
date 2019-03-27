package com.example.thermal_image_processing_pipeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    PGMImage img = null;
    Canvas canvas= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we have read permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        final Button button = findViewById(R.id.button_1);
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

        final Button button2 = findViewById(R.id.button_2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Image 1
                img = FileManagement.readFile(MainActivity.this, "bridge");
                ImageView imgView = findViewById(R.id.imageView1);
                if(img != null)
                    DisplayHandler.DrawCanvas(DisplayHandler.generateBitmapFromPGM(img, DisplayHandler.GREEN), imgView);

                // Image 2
                img = FileManagement.readFile(MainActivity.this, "bridge");
                imgView = findViewById(R.id.imageView2);
                if(img !=null)
                    DisplayHandler.DrawCanvas(DisplayHandler.generateBitmapFromPGM(img, DisplayHandler.BLUE), imgView);
            }
        });
    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


}
