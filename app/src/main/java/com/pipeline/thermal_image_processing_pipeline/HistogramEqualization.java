package com.pipeline.thermal_image_processing_pipeline;

import com.example.thermal_image_processing_pipeline.PGMImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


public class HistogramEqualization {
    private ArrayList<HistogramDataEntry> pixelTable2 = new ArrayList<>();
    private HashMap<Integer, HistogramDataEntry> pixelTable = new HashMap<>();
    private boolean dataGenerated = false;
    private int L;

    /**
     *
     * @param L the number of colors available
     */
    public HistogramEqualization(int L){
        this.L = L;
    }

    public void getHistogramEqualizationColorValues(PGMImage image){
        int[][] colorValues = new int[image.getHeight()][image.getWidth()];
        for(int y = 0; y < image.getHeight(); ++y){
            for(int x = 0; x < image.getWidth(); ++x){
                colorValues[y][x] = getHColorValues(image.getDataAt(x, y));
            }
        }
        image.setColorValues(colorValues);
    }

    private int getHColorValues(int intensity){
        if(pixelTable.get(Math.abs(intensity)) != null)
            return pixelTable.get(Math.abs(intensity)).getH();
        else
            return 0;
    }

    public void add(int pixelDensity, int maxValue){
        pixelDensity = (pixelDensity);
        if(this.pixelTable.get(pixelDensity) != null){
            this.pixelTable.get(pixelDensity).add();
        }else
            this.pixelTable.put(pixelDensity, new HistogramDataEntry(pixelDensity, 1, maxValue));
    }


    public void generateHistogramData(int n){
        if(!dataGenerated){
            float CDFAmount = 0;
            ArrayList<HistogramDataEntry> list = new ArrayList<>(pixelTable.values());
            Collections.sort(list, new sortList());
            for(HistogramDataEntry p : list){
                CDFAmount += (float)p.getAmount();
                p.setCdf(CDFAmount);
            }
            float cdfMin = getLowest(list);
            for(HistogramDataEntry p : list){
                p.setH(Math.round(((float)p.getCdf()-cdfMin)/(n-cdfMin)*(L-1)));
            }
            dataGenerated = true;
        }
    }


    private float getLowest(ArrayList<HistogramDataEntry> list){
        return list.get(0).getCdf();
    }

    private class sortList implements Comparator<HistogramDataEntry> {
        @Override
        public int compare(HistogramDataEntry a, HistogramDataEntry b) {
            return  (a.getPixel() < b.getPixel() ) ? -1 : a.getPixel() == b.getPixel() ? 0 : 1;
        }
    }

}
