package com.google.ar.core.examples.java.sharedcamera;

import java.util.ArrayList;
import java.util.HashMap;

public class GridZones extends HashMap<String, GridInfo> {

}

class GridInfo{

    public GridInfo(){
        landmarks = new ArrayList<LandmarksHelper.Landmark>();
    }

    /*public void resetSums(){
        //confList.clear();
        //lmSum = 0;
        confRemovalThreshold = 0;
    }*/

    float confidence;
    //int landmarkCount;
    ArrayList<LandmarksHelper.Landmark> landmarks;
    //int lmSum;
    //float confRemovalThreshold;
    float lowX;
    float highX;
    float lowY;
    float highY;
}