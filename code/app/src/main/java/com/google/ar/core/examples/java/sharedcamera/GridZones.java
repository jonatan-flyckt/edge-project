package com.google.ar.core.examples.java.sharedcamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GridZones extends ConcurrentHashMap<String, GridInfo> {

}

class GridInfo{

    public GridInfo(){
        landmarks = new CopyOnWriteArrayList<LandmarksHelper.Landmark>();
    }

    /*public void resetSums(){
        //confList.clear();
        //lmSum = 0;
        confRemovalThreshold = 0;
    }*/

    float confidence;
    //int landmarkCount;
    CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarks;
    //int lmSum;
    //float confRemovalThreshold;
    float lowX;
    float highX;
    float lowY;
    float highY;
    boolean isBeingCleaned = false;
}