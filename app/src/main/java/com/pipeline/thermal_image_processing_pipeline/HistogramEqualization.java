package com.pipeline.thermal_image_processing_pipeline;

import java.util.ArrayList;

public class HistogramEqualization {
    private ArrayList<PixelTableEntry> pixelTable = new ArrayList<PixelTableEntry>();
    private ArrayList<Histogram> equalization = new ArrayList<Histogram>();

    public HistogramEqualization(){

    }

    public void add(int pixelDensity){
        if(this.pixelTable.size() > 0){
            for(PixelTableEntry p : this.pixelTable){
                if(p.getPixel() == pixelDensity){
                    p.add();
                    break;
                }
            }
        }
        else
            this.pixelTable.add(new PixelTableEntry(pixelDensity));
    }

    public void sortPixelTable(){
        //for()
    }
}
