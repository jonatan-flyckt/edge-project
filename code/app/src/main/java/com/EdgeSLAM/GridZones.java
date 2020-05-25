package com.EdgeSLAM;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//Class to hold the meta info about the grids using a hashmap with the grid bounds as keys
public class GridZones extends ConcurrentHashMap<String, GridInfo> {
}

class GridInfo{

    public GridInfo(int limitPerZone, float x, float y){
        landmarks = new CopyOnWriteArrayList<LandmarksHelper.Landmark>();
        lowX = x;
        highX = x;
        lowY = y;
        highY = y;
        maxNumberOfPoints = limitPerZone;
    }

    float confidence;
    CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarks;
    float lowX;
    float highX;
    float lowY;
    float highY;
    int maxNumberOfPoints;
}