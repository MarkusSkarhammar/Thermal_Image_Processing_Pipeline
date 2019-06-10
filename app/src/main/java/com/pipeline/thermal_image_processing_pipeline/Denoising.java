package com.pipeline.thermal_image_processing_pipeline;


import com.example.thermal_image_processing_pipeline.MainActivity;

import java.util.ArrayList;

import static com.example.thermal_image_processing_pipeline.MainActivity.MAX_THREADS;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class Denoising{

    public static void MeanFilter(int[] data, int wFrom, int hFrom, int wTo, int hTo, int pos){
        wFrom++;
        if(pos == 0) hFrom++;
        wTo--;
        if(pos == MAX_THREADS) hTo--;
        for(int h = hFrom; h < hTo; h++)
            for(int w = wFrom; w < wTo; w++){
                data[((h)*str_w) + (w)] = average(data, w, h);
            }

    }

    /*public static void MedianFilter(int[] data, int wFrom, int hFrom, int wTo, int hTo){
        if(wFrom == 0) wFrom++;
        if(hFrom == 0) hFrom++;
        if(wTo == str_w) wTo--;
        if(hTo == str_h) hTo--;
        ArrayList<Integer> neighbours = new ArrayList<>();
        for(int h = hFrom; h < hTo; h++)
            for(int w = wFrom; w < wTo; w++){
                data[((h)*str_w) + (w)] = sort(getNeighbours(data, neighbours, w, h)).get(3);
            }
    }*/

    private static ArrayList<Integer> getNeighbours(int[] data, ArrayList<Integer> neighbours, int xPos, int yPos){
        neighbours.clear();
        for(int y = 0; y < 3; y++)
            for(int x = 0; x < 3; x++){
                if(y != 1 && x != 1)
                    neighbours.add(data[((yPos)*str_w) + (xPos)]);
            }
        return neighbours;
    }

    private static int average(int[] data, int xPos, int yPos){
        int total = 0;
        for(int y = 0; y < 3; y++)
            for(int x = 0; x < 3; x++){
                if(y != 1 && x != 1)
                    total += data[((yPos)*str_w) + (xPos)];
            }
        return total/8;
    }

    private static ArrayList<Integer> sort(ArrayList<Integer> list){
        Integer temp;
        for(int j = 0; j < list.size(); j++){
            for(int i = j+1; i < list.size(); i++){
                if(list.get(j) > list.get(i)){
                    temp = list.get(j);
                    list.set(j, list.get(i));
                    list.set(i, temp);
                }
            }
        }
        return list;
    }
}
