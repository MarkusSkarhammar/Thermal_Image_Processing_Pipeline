package com.example.thermal_image_processing_pipeline;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class Histogram {

    private static void createHistogram(PGMImage img, GraphView graph) {

    	/*

    	Number of pixels. Axis height calculated by counting highest number of pixels in a tone?
        ^
        |
	    |
	    +------> Tone/Grayscale, black to white. Axis length calculated by number of tones?

    	*/

        // Create an array.
        int[] tones = new int[img.getMaxValue()]; // Each tone contains a number of pixels.
        int yAxisHeight = 0;

        // Create a histogram by scanning every pixel of the image and incrementing the relevant member in the array.

        for (int y = 0; y < img.getHeight(); ++y) {
            for (int x = 0; y < img.getWidth(); ++y) {
                ++tones[img.getDataAt(y, x)];
                if (img.getDataAt(y, x) > yAxisHeight) {
                    yAxisHeight = img.getDataAt(y, x);
                }
            }
        }

        DataPoint[] dp = new DataPoint[tones.length];
        for (int i : tones) {
            dp[i] = new DataPoint(tones[i], yAxisHeight);
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dp);
        graph.addSeries(series);

        // draw values on top
        series.setDrawValuesOnTop(true);

    }
}