package com.google.ar.core.examples.java.sharedcamera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;

    private int landMarkArraySize = 30000;

    public static ArrayList<Landmark> landMarkArray = new ArrayList<>(10000);

    public static ArrayList<Landmark> cameraLandMarkArray = new ArrayList<>(1000);

//    public ArrayList<Landmark> landMarkCacheArray = new ArrayList<>(1000);

    public float confidenceThreshold = (float) 0.4;

    public LandmarksHelper() {
        landMarkArray.add(new Landmark(0, 0, 1));
        cameraLandMarkArray.add(new Landmark(0, 0, 1));
    }

    private int cleanCount = 0;

    float lowX, highX, lowY, highY;

    public static class Landmark {
        public final float x;
        public final float y;
        public final float con;

        public Landmark(float x, float y, float con) {
            this.x = x;
            this.y = y;
            this.con = con;
        }
    }

    public ArrayList<Landmark> getLandMarkArray(){
        return new ArrayList<Landmark>(landMarkArray);
    }

    public ArrayList<Landmark> getCameraLandMarkArray(){
        return new ArrayList<Landmark>(cameraLandMarkArray);
    }

    public void addCameraLandMark(float x, float y){
        cameraLandMarkArray.add(new Landmark(x, y, 1));
    }

    public void addLandmarks(float[] pointBuffer) {

//        Log.d("Point buffer", String.valueOf(pointBuffer.length));

        for (int i = 0; i < pointBuffer.length; i = i + BYTES_PER_FLOAT) {

            float fx = pointBuffer[i + 0];
            //float fy = pointBuffer[i + 1];
            float fy = pointBuffer[i + 2];
            float fcon = pointBuffer[i + 3];


            Landmark newLandmark = new Landmark(fx, fy, fcon);
            landMarkArray.add(newLandmark);
        }

        Log.d("STUFF", "--------------------------------------------------");

        Log.d("Point buffer", String.valueOf(landMarkArray.size()));

        cleanCount++;

        if (cleanCount > 20) {
            cleanCount = 0;
            cleanLandmarkArray();

            Log.d("Points after clean", String.valueOf(landMarkArray.size()));
        }

        Log.d("STUFF", "--------------------------------------------------");
    }

    public boolean isBeingCleaned = false;

    public void cleanLandmarkArray() {

        isBeingCleaned = true;
        purgeLandmarkArraySize();
        purgeLandMarkArrayOutliers();
        //updateExtremePoints(landMarkArray.get(0).x, landMarkArray.get(0).x, landMarkArray.get(0).y, landMarkArray.get(0).y);
        isBeingCleaned = false;
    }

    //boolean isFirstTime = true;
    public void updateExtremePoints(float lowestX, float highestX, float lowestY, float highestY){
        /*if (isFirstTime) {
            isFirstTime = false;
            lowX = landMarkArray.get(0).x;
            highX = landMarkArray.get(0).x;
            lowY = landMarkArray.get(0).y;
            highY = landMarkArray.get(0).y;
        }*/

        lowX = lowestX;
        highX = highestX;
        lowY = lowestY;
        highY = highestY;

        for (LandmarksHelper.Landmark landmark: cameraLandMarkArray){
            if (landmark.x > highX)
                highX = landmark.x;
            if (landmark.x < lowX)
                lowX = landmark.x;
            if (landmark.y > highY)
                highY = landmark.y;
            if (landmark.y < lowY)
                lowY = landmark.y;
        }
    }

    public void purgeLandmarkArraySize() {
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.con, o1.con));

        if (landMarkArray.size() > landMarkArraySize) {
            landMarkArray.subList(landMarkArraySize, landMarkArray.size()-1).clear();
        }
    }

    public void purgeLandMarkArrayOutliers() {

        int percentile = 100;

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.x, o1.x));
        landMarkArray.subList(0, landMarkArray.size() / percentile).clear();
        landMarkArray.subList(landMarkArray.size() - landMarkArray.size() / percentile, landMarkArray.size() - 1).clear();
        float xLowest, xHighest;
        xLowest = landMarkArray.get(0).x;
        xHighest = landMarkArray.get(landMarkArray.size()-1).x;


        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.y, o1.y));

        landMarkArray.subList(0, landMarkArray.size() / percentile).clear();
        landMarkArray.subList(landMarkArray.size() - landMarkArray.size() / percentile, landMarkArray.size() - 1).clear();
        float yLowest, yHighest;
        yLowest = landMarkArray.get(0).y;
        yHighest = landMarkArray.get(landMarkArray.size()-1).y;

        //updateExtremePoints(xLowest, xHighest, yLowest, yHighest);
        updateExtremePoints(-5, 5, -5, 5);
    }

    public void increaseConfidenceThreshold() {
        this.confidenceThreshold += 0.05;
    }
}







