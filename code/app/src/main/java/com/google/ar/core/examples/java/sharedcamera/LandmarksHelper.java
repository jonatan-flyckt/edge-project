package com.google.ar.core.examples.java.sharedcamera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;

    //private int landMarkArraySize = 30000;

    //public static ArrayList<Landmark> landMarkArray = new ArrayList<>(10000);

    public static ArrayList<Landmark> cameraLandMarkArray = new ArrayList<>(1000);

    GridZones gridZones = new GridZones();

    ArrayList<String> zonesUpdatedThisIteration = new ArrayList<String>();

//    public ArrayList<Landmark> landMarkCacheArray = new ArrayList<>(1000);

    public float confidenceThreshold = (float) 0.4;

    public LandmarksHelper() {
        //landMarkArray.add(new Landmark(0, 0, 1));
        lowX = -5;
        lowY = -5;
        highX = 5;
        highY = 5;
        GridInfo gridInfo = new GridInfo();
        gridInfo.landmarks.add(new Landmark(0, 0, 0));
        gridZones.put(keyFromGridBounds(0,0,0,0), gridInfo);
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

    /*public ArrayList<Landmark> getLandMarkArray(){
        return new ArrayList<Landmark>(landMarkArray);
    }*/

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


            isBeingCleaned = true;
            if (fx < highX+1 && fx > lowX-1 && fy < highY+1 && fy > lowY-1 && fcon > 0.15) {
                Landmark newLandmark = new Landmark(fx, fy, fcon);
                String key = keyFromGridBounds((int) newLandmark.x, (int) newLandmark.x + 1, (int) newLandmark.y, (int) newLandmark.y + 1);
                if (gridZones.get(key) == null){
                    gridZones.put(key, new GridInfo());
                }
                GridInfo gridInfo = gridZones.get(key);
                gridInfo.landmarks.add(newLandmark);
                gridZones.put(key, gridInfo);
                if (!zonesUpdatedThisIteration.contains(key))
                    zonesUpdatedThisIteration.add(key);
            }
            isBeingCleaned = false;
            //landMarkArray.add(newLandmark);
        }

        //Log.d("STUFF", "--------------------------------------------------");

        //Log.d("Point buffer", String.valueOf(landMarkArray.size()));

        cleanCount++;

        if (cleanCount > 20) {
            cleanCount = 0;
            cleanLandmarks();
            zonesUpdatedThisIteration.clear();

            //Log.d("Points after clean", String.valueOf(landMarkArray.size()));
        }

        Log.d("STUFF", "--------------------------------------------------");
    }

    public boolean isBeingCleaned = false;

    public void cleanLandmarks() {

        isBeingCleaned = true;
        //purgeLandmarkArraySize();
        //purgeLandMarkArrayOutliers();

        refineGridLandmarks(500);
        removeGridsWithFewPoints(10);
        updateExtremePoints();
        isBeingCleaned = false;
    }

    void removeGridsWithFewPoints(int threshold){

    }

    public void updateExtremePoints(){
        for (GridInfo gridInfo: gridZones.values()){
            if (gridInfo.lowX < lowX)
                lowX = gridInfo.lowX;
            if (gridInfo.highX > highX)
                highX = gridInfo.highX;
            if (gridInfo.lowY < lowY)
                lowY = gridInfo.lowY;
            if (gridInfo.highY > highY)
                highY = gridInfo.highY;
        }
    }

    /*public void purgeLandmarkArraySize() {
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.con, o1.con));

        if (landMarkArray.size() > landMarkArraySize) {
            landMarkArray.subList(landMarkArraySize, landMarkArray.size()-1).clear();
            Log.d("EH big fucking array", "big fucking array");
        }
    }*/
/*
    public void purgeLandMarkArrayOutliers() {
        int percentile = 50;

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o1.x, o2.x));
        float xLowPercentile = landMarkArray.get(landMarkArray.size() / percentile).x;
        float xHighPercentile = landMarkArray.get(landMarkArray.size() - landMarkArray.size() / percentile).x;
        int xLowIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.x < xLowPercentile - 1)
                xLowIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, xLowIndex).clear();
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.x, o1.x));
        for (Landmark landmark: landMarkArray.subList(0, 9)){
            Log.d("topfive", String.valueOf(landmark.x));
        }
        Log.d("topfive", "-");
        int xHighIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.x > xHighPercentile + 1)
                xHighIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, xHighIndex).clear();
        float xLowest, xHighest;
        xHighest = landMarkArray.get(0).x;
        xLowest = landMarkArray.get(landMarkArray.size()-1).x;

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o1.y, o2.y));
        float yLowPercentile = landMarkArray.get(landMarkArray.size() / percentile).y;
        float yHighPercentile = landMarkArray.get(landMarkArray.size() - landMarkArray.size() / percentile).y;
        int yLowIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.y < yLowPercentile - 1)
                yLowIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, yLowIndex).clear();
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.y, o1.y));
        int yHighIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.y > yHighPercentile + 1)
                yHighIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, yHighIndex).clear();
        float yLowest, yHighest;
        yHighest = landMarkArray.get(0).y;
        yLowest = landMarkArray.get(landMarkArray.size()-1).y;

        //updateExtremePoints(xLowest, xHighest, yLowest, yHighest);
        updateExtremePoints(-5, 5, -5, 5);
    }
*/

    /*public void purgeLandMarkArrayOutliers() {
        int percentile = 50;

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o1.x, o2.x));
        float xLowPercentile = landMarkArray.get(landMarkArray.size() / percentile).x;
        float xHighPercentile = landMarkArray.get(landMarkArray.size() - landMarkArray.size() / percentile).x;
        int xLowIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.x < xLowPercentile - 1)
                xLowIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, xLowIndex).clear();
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.x, o1.x));
        for (Landmark landmark: landMarkArray.subList(0, 9)){
            Log.d("topfive", String.valueOf(landmark.x));
        }
        Log.d("topfive", "-");
        int xHighIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.x > xHighPercentile + 1)
                xHighIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, xHighIndex).clear();
        float xLowest, xHighest;
        xHighest = landMarkArray.get(0).x;
        xLowest = landMarkArray.get(landMarkArray.size()-1).x;

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o1.y, o2.y));
        float yLowPercentile = landMarkArray.get(landMarkArray.size() / percentile).y;
        float yHighPercentile = landMarkArray.get(landMarkArray.size() - landMarkArray.size() / percentile).y;
        int yLowIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.y < yLowPercentile - 1)
                yLowIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, yLowIndex).clear();
        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.y, o1.y));
        int yHighIndex = 0;
        for (Landmark landmark : landMarkArray){
            if (landmark.y > yHighPercentile + 1)
                yHighIndex = landMarkArray.indexOf(landmark);
            else
                break;
        }
        landMarkArray.subList(0, yHighIndex).clear();
        float yLowest, yHighest;
        yHighest = landMarkArray.get(0).y;
        yLowest = landMarkArray.get(landMarkArray.size()-1).y;

        //updateExtremePoints(xLowest, xHighest, yLowest, yHighest);
        updateExtremePoints(-5, 5, -5, 5);
    }*/


    /*public void increaseConfidenceThreshold() {
        this.confidenceThreshold += 0.05;
    }*/


    public void refineGridLandmarks(int limitPerZone){
        for (String key: zonesUpdatedThisIteration){
            GridInfo gridInfo = gridZones.get(key);
            float confSum = 0;
            Collections.sort(gridInfo.landmarks, Collections.reverseOrder());
            if (gridInfo.landmarks.size() > limitPerZone)
                gridInfo.landmarks = (ArrayList<Landmark>) gridInfo.landmarks.subList(0, limitPerZone);
            for (Landmark landmark : gridInfo.landmarks){
                confSum += landmark.con;
                if (landmark.x < gridInfo.lowX)
                    gridInfo.lowX = landmark.x;
                if (landmark.x > gridInfo.highX)
                    gridInfo.highX = landmark.x;
                if (landmark.y < gridInfo.lowY)
                    gridInfo.lowY = landmark.y;
                if (landmark.y < gridInfo.highY)
                    gridInfo.highY = landmark.y;
            }
            gridInfo.confidence = confSum / gridInfo.landmarks.size();
        }
    }

    public String keyFromGridBounds(int xLo, int xHi, int yLo, int yHi){
        StringBuilder sb = new StringBuilder();
        sb.append(xLo).append(xHi).append(yLo).append(yHi);
        return sb.toString();
    }

}







