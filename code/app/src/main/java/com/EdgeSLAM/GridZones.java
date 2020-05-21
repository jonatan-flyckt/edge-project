package com.EdgeSLAM;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    float lowZ;
    float highZ;
    int maxNumberOfPoints;
}