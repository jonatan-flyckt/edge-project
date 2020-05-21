package com.google.ar.core.examples.java.sharedcamera;

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

<<<<<<< HEAD
=======

>>>>>>> plotting-improvements
    float confidence;
    CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarks;
    float lowX;
    float highX;
    float lowY;
    float highY;
<<<<<<< HEAD
=======
    float lowZ;
    float highZ;
    int maxNumberOfPoints;

>>>>>>> plotting-improvements
}