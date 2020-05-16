package com.google.ar.core.examples.java.sharedcamera;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GridZones extends ConcurrentHashMap<String, GridInfo> {

}

class GridInfo{

    public GridInfo(){
        landmarks = new CopyOnWriteArrayList<LandmarksHelper.Landmark>();
    }

    float confidence;
    CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarks;
    float lowX;
    float highX;
    float lowY;
    float highY;
}