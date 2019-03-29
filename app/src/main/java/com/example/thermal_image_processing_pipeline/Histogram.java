package com.example.thermal_image_processing_pipeline;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Histogram {

    private static int[] createHistogram(PGMImage img) {

    	/*

    	Number of pixels. Axis height calculated by counting highest number of pixels in a tone?
        ^
        |
    	|
	    |
	    |
	    |-------------------> Tone/Grayscale, black to white. Axis length calculated by number of tones?

    	*/

        // Create an array.
        int[] tones = new int[img.getMaxValue()]; // Each tone contains a number of pixels.
        int axisHeight = 0;

        // Create a histogram by scanning every pixel of the image and incrementing the relevant member in the array.

        for (int y = 0; y < img.getHeight(); ++y) {
            for (int x = 0; y < img.getWidth(); ++y) {
                ++tones[img.getDataAt(y,x)];
                if (img.getDataAt(y,x) > axisHeight) {
                    axisHeight = img.getDataAt(y,x);
                }
            }
        }

        return tones;
    }

    public static void draw(PGMImage img, GraphView graph) {

        createHistogram(img);

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(0, -1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);

        // draw values on top
        series.setDrawValuesOnTop(true);
    }

}
