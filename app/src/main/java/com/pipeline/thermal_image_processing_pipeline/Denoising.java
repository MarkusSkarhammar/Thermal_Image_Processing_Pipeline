package com.pipeline.thermal_image_processing_pipeline;


import java.util.ArrayList;

import static com.example.thermal_image_processing_pipeline.MainActivity.str_h;
import static com.example.thermal_image_processing_pipeline.MainActivity.str_w;

public class Denoising{

    public static void MeanFilter(int[] data, int wFrom, int hFrom, int wTo, int hTo){
        if(wFrom == 0) wFrom++;
        if(hFrom == 0) hFrom++;
        if(wTo == str_w) wTo--;
        if(hTo == str_h) hTo--;

        for(int h = hFrom; h < hTo; h++)
            for(int w = wFrom; w < wTo; w++){
                data[((h)*str_w) + (w)] = (int)average(getNeighbours(data, w, h));
            }

    }

    public static void MedianFilter(int[] data, int wFrom, int hFrom, int wTo, int hTo){
        if(wFrom == 0) wFrom++;
        if(hFrom == 0) hFrom++;
        if(wTo == str_w) wTo--;
        if(hTo == str_h) hTo--;

        for(int h = hFrom; h < hTo; h++)
            for(int w = wFrom; w < wTo; w++){
                data[((h)*str_w) + (w)] = sort(getNeighbours(data, w, h)).get(3);
            }
    }

    private static ArrayList<Integer> getNeighbours(int[] data, int xPos, int yPos){
        ArrayList<Integer> neighbours = new ArrayList<>();
        for(int y = 0; y < 3; y++)
            for(int x = 0; x < 3; x++){
                if(y != 1 && x != 1)
                    neighbours.add(data[((yPos)*str_w) + (xPos)]);
            }
        return neighbours;
    }

    private static double average(ArrayList<Integer> list){
        int total = 0;
        for(Integer i : list){
            total += i;
        }
        return total/list.size();
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
