package com.example.thermal_image_processing_pipeline;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;

public class PlotHistogram {

    /*

    In order to use, see the following example code:

        GraphView graph = (GraphView) findViewById(R.id.graph1);
        PlotHistogram.plot(img, graph);

    */

    private static int[] createHistogram(PGMImage img) {
      	/*

    	Number of pixels. Axis height calculated by counting highest number of pixels in a tone?
        ^
        |
	    |
	    +------> Tone/Grayscale, black to white. Axis length calculated by number of tones?

    	*/

        // Create an array.
        int[] tones = new int[img.getMaxValue() + 1]; // Each tone contains a number of pixels.

        // Create a histogram by scanning every pixel of the image and incrementing the relevant member in the array.
        for (int y = 0; y < img.getHeight(); ++y) {
            for (int x = 0; x < img.getWidth(); ++x) {
                ++tones[img.getDataAt(x, y)];
            }
        }

        return tones;
    }

    /**
     * Plot primary scale axis.
     * @param img
     * @param graph
     */
    public static void plot(PGMImage img, GraphView graph) {

        int[] tones = createHistogram(img);
        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();

        // Find non-zero tones and add new DataPoint.
        for (int i = 0; i < tones.length; i++) {
            if (tones[i] != 0) {
                temp.add(new DataPoint(i, tones[i]));
            }
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(temp.toArray(new DataPoint[temp.size()]));
        graph.addSeries(series);

    }

    /**
     * Plot secondary scale axis.
     * @param img
     * @param graph
     */
    public static void plotSecondary(PGMImage img, GraphView graph) {

        int[] tones = createHistogram(img);
        ArrayList<DataPoint> dp = new ArrayList<DataPoint>();

        // Find non-zero tones and add new DataPoint.
        for (int i = 0; i < tones.length; i++) {
            if (tones[i] != 0) {
                dp.add(new DataPoint(i, tones[i]));
            }
        }

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(dp.toArray(new DataPoint[dp.size()]));

        // Adjust secondary scale.
        // Secondary scale doesn't have automatic bounds. It is mandatory to set the y bounds manually.
        graph.getSecondScale().addSeries(series2);
        graph.getSecondScale().setMinY(0);
        graph.getSecondScale().setMaxY(series2.getHighestValueY());
        series2.setColor(Color.RED);

        // Adjust primary scale.
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(series2.getHighestValueY());
        graph.getViewport().setYAxisBoundsManual(true);

    }

    /**
     * Clears primary and secondary scale.
     * @param graph
     */
    public static void clear(GraphView graph) {
        graph.removeAllSeries();
        graph.clearSecondScale();
    }
}