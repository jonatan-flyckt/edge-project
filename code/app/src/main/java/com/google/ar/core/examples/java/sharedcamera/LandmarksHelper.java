package com.google.ar.core.examples.java.sharedcamera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;

    public static ArrayList<Landmark> cameraLandMarkArray = new ArrayList<>(1000);

    GridZones gridZones = new GridZones();

    ArrayList<String> zonesUpdatedThisIteration = new ArrayList<String>();

    public LandmarksHelper() {
        lowX = 0;
        lowY = 0;
        highX = 0;
        highY = 0;
        camLowX = 0;
        camHighX = 0;
        camLowY = 0;
        camHighY = 0;
        cameraLandMarkArray.add(new Landmark(0, 0, 1));
    }

    private int cleanCount = 0;

    float lowX, highX, lowY, highY, camLowX, camHighX, camLowY, camHighY;

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

    public ArrayList<Landmark> getCameraLandMarkArray(){
        return new ArrayList<Landmark>(cameraLandMarkArray);
    }

    public void addCameraLandMark(float x, float y){
        cameraLandMarkArray.add(new Landmark(x, y, 1));
        if (x > camHighX)
            camHighX = x;
        if (x < camLowX)
            camLowX = x;
        if (y > camHighY)
            camHighY = y;
        if (y < camLowY)
            camLowY = y;
    }

    public void addLandmarks(float[] pointBuffer) {

        for (int i = 0; i < pointBuffer.length; i = i + BYTES_PER_FLOAT) {

            float fx = pointBuffer[i + 0];
            float fy = pointBuffer[i + 2];
            float fcon = pointBuffer[i + 3];

            if (fx < highX+1 && fx > lowX-1 && fy < highY+1 && fy > lowY-1 && fcon > 0.3) {
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
        }

        cleanCount++;

        if (cleanCount > 50) {
            cleanCount = 0;
            cleanLandmarks();
            zonesUpdatedThisIteration.clear();
        }
    }

    public void cleanLandmarks() {
        int limitPerZone = 1500;
        //clearPointsInCloseProximity(0.02f, limitPerZone);
        refineGridLandmarks(limitPerZone);
        removeGridsWithFewPoints(50);
        updateExtremePoints();
    }

    void clearPointsInCloseProximity(float meters, int limitPerZone){
        for (String key: zonesUpdatedThisIteration) {
            GridInfo gridInfo = gridZones.get(key);
            Collections.sort(gridInfo.landmarks, (o1, o2) -> Float.compare(o2.con, o1.con));
            int j = 0;
            for (int i = limitPerZone; i > 0; i--){
                while(j < gridInfo.landmarks.size()) {
                    float x = gridInfo.landmarks.get(j).x;
                    float y = gridInfo.landmarks.get(j).y;
                    float con = gridInfo.landmarks.get(j).con;

                    Iterator<Landmark> iterator = gridInfo.landmarks.iterator();
                    int k = 0;
                    while (iterator.hasNext()){
                        Landmark currentLandmark = iterator.next();
                        if (k != j &&
                        currentLandmark.x < x - meters && currentLandmark.x > x + meters &&
                        currentLandmark.y < y - meters && currentLandmark.y > y + meters &&
                        currentLandmark.con <= con)
                            iterator.remove();
                        k++;
                    }
                    j++;
                    break;
                }
            }
        }
    }

    void removeGridsWithFewPoints(int threshold){
        ArrayList<String> keysToBeRemoved = new ArrayList<String>();
        for (Map.Entry<String, GridInfo> entry : gridZones.entrySet()){
            if (entry.getValue().landmarks.size() < threshold){
                keysToBeRemoved.add(entry.getKey());
            }
        }
        for (String key: keysToBeRemoved){
            gridZones.remove(key);
        }
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

    public void refineGridLandmarks(int limitPerZone){

        for (String key: zonesUpdatedThisIteration){
            GridInfo gridInfo = gridZones.get(key);
            float confSum = 0;
            Collections.sort(gridInfo.landmarks, (o1, o2) -> Float.compare(o2.con, o1.con));
            if (gridInfo.landmarks.size() > limitPerZone)
                gridInfo.landmarks = new CopyOnWriteArrayList<>(gridInfo.landmarks.subList(0, limitPerZone));
            for (Landmark landmark : gridInfo.landmarks){
                confSum += landmark.con;
                if (landmark.x < gridInfo.lowX)
                    gridInfo.lowX = landmark.x;
                if (landmark.x > gridInfo.highX)
                    gridInfo.highX = landmark.x;
                if (landmark.y < gridInfo.lowY)
                    gridInfo.lowY = landmark.y;
                if (landmark.y > gridInfo.highY)
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







