package com.google.ar.core.examples.java.sharedcamera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;

    public static ArrayList<Landmark> cameraLandMarkArray = new ArrayList<Landmark>(1000);

    GridZones gridZones = new GridZones();

    float confThreshold = (float) 0.5;

    float cleanCameraThreshold = (float) 1;

    ArrayList<String> zonesUpdatedThisIteration = new ArrayList<String>();

    float positionMultiplier = 4;

    float averageCameraZ = 0;

    int limitPerZone = 200;

    public LandmarksHelper() {
        lowX = -6;
        lowY = -6;
        highX = 6;
        highY = 6;
        lowZ = 0;
        highZ = 0;
        camLowX = 0;
        camHighX = 0;
        camLowY = 0;
        cameraLandMarkArray.add(new Landmark(0, 0, 0, 1));
    }

    private int cleanCount = 0;

    float lowX, highX, lowY, highY, lowZ, highZ, camLowX, camHighX, camLowY, camHighY;

    public static class Landmark {
        public final float x;
        public final float y;
        public final float z;
        public final float con;

        public Landmark(float x, float y, float z, float con) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.con = con;
        }
    }

    public ArrayList<Landmark> getCameraLandMarkArray(){
        return new ArrayList<Landmark>(cameraLandMarkArray);
    }

    public void cleanCameraLandmarkArray(){
        Iterator<Landmark> iterator = cameraLandMarkArray.iterator();
        Log.d("cameralandmark before clean", String.valueOf(cameraLandMarkArray.size()));
        float previousX = 0;
        float previousY = 0;
        while (iterator.hasNext()){
            Landmark currentLandmark = iterator.next();
            if ( Math.abs(currentLandmark.x - previousX) < cleanCameraThreshold && Math.abs(currentLandmark.y - previousY) < cleanCameraThreshold)
                iterator.remove();
            else {
                previousX = currentLandmark.x;
                previousY = currentLandmark.y;
            }
        }
        Log.d("cameralandmark after clean", String.valueOf(cameraLandMarkArray.size()));
        Log.d("cameralandmark", "------------------");
    }

    public void addCameraLandMark(float x, float y, float z){
        cameraLandMarkArray.add(new Landmark(x * positionMultiplier, y * positionMultiplier, z * positionMultiplier,  1));
        if (x > camHighX)
            camHighX = x;
        if (x < camLowX)
            camLowX = x;
        if (y > camHighY)
            camHighY = y;
        if (y < camLowY)
            camLowY = y;
        updateAverageCameraZ();
    }

    public void updateAverageCameraZ(){
        float zSum = 0;
        for (Landmark landmark: cameraLandMarkArray){
            zSum += landmark.z;
        }
        averageCameraZ = zSum / cameraLandMarkArray.size();
    }

    public void addLandmarks(float[] pointBuffer) {

        for (int i = 0; i < pointBuffer.length; i = i + BYTES_PER_FLOAT) {

            float fx = pointBuffer[i + 0] * positionMultiplier;
            float fz = pointBuffer[i + 1] * positionMultiplier;
            float fy = pointBuffer[i + 2] * positionMultiplier;
            float fcon = pointBuffer[i + 3];

            if (fx < highX+1 && fx > lowX-1 && fy < highY+1 && fy > lowY-1 && fcon > confThreshold) {
                Landmark newLandmark = new Landmark(fx, fy, fz, fcon);
                String key = keyFromGridBounds((int) newLandmark.x, (int) newLandmark.x + 1, (int) newLandmark.y, (int) newLandmark.y + 1);
                if (gridZones.get(key) == null){
                    gridZones.put(key, new GridInfo(limitPerZone, fx, fy));
                }
                GridInfo gridInfo = gridZones.get(key);
                gridInfo.landmarks.add(newLandmark);
                gridZones.put(key, gridInfo);
                if (!zonesUpdatedThisIteration.contains(key))
                    zonesUpdatedThisIteration.add(key);
            }
        }

        cleanCount++;

        if (cleanCount > 20) {
            cleanCount = 0;
            cleanLandmarks();
            zonesUpdatedThisIteration.clear();
        }
    }

    public void cleanLandmarks() {
        refineGridLandmarks();
        removeGridsWithFewPoints(30);
        cleanCameraLandmarkArray();
        estimateFloorPosition();
        updateExtremePoints();
        Log.d("floor avgCameraZ", String.valueOf(averageCameraZ));
        Log.d("floor floorPos", String.valueOf(floorPosition));
        Log.d("floor camFloorDist", String.valueOf(distanceFromCameraToFloor));
        Log.d("floor", "-----------");
    }


    float floorPosition = 0;
    float distanceFromCameraToFloor = 0;

    public void estimateFloorPosition(){
        HashMap<String, Integer> zPosMap = new HashMap<String, Integer>();
        for (GridInfo gridInfo: gridZones.values()){
            for (Landmark landmark: gridInfo.landmarks){
                if (landmark.z < averageCameraZ){
                    String key = String.valueOf((int)landmark.z);
                    if (zPosMap.get(key) == null){
                        zPosMap.put(key, 0);
                    }
                    Integer count = zPosMap.get(key);
                    zPosMap.put(key, count+1);
                }
            }
        }
        int largestCount = 0;
        String currentEstimatedFloorKey = "0";
        for (Map.Entry<String, Integer> entry : zPosMap.entrySet()) {
            if (entry.getValue() > largestCount){
                currentEstimatedFloorKey = entry.getKey();
                largestCount = entry.getValue();
            }
            Log.d("largestCount", String.valueOf(largestCount));
        }
        floorPosition = Float.parseFloat(currentEstimatedFloorKey);
        distanceFromCameraToFloor = Math.abs(averageCameraZ - floorPosition);
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
            if (gridInfo.lowZ < lowZ)
                lowZ = gridInfo.lowZ;
            if (gridInfo.highZ > highZ)
                highZ = gridInfo.highZ;
        }
    }

    public void refineGridLandmarks(){
        for (String key: zonesUpdatedThisIteration){
            GridInfo gridInfo = gridZones.get(key);
            float confSum = 0;
            Collections.sort(gridInfo.landmarks, (o1, o2) -> Float.compare(o2.con, o1.con));
            if (gridInfo.landmarks.size() > gridInfo.maxNumberOfPoints)
                gridInfo.landmarks = new CopyOnWriteArrayList<>(gridInfo.landmarks.subList(0, gridInfo.maxNumberOfPoints));
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
                if (landmark.z < gridInfo.lowZ)
                    gridInfo.lowZ = landmark.z;
                if (landmark.z > gridInfo.highZ)
                    gridInfo.highZ = landmark.z;
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







