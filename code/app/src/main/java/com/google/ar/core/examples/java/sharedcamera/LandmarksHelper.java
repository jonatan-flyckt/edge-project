package com.google.ar.core.examples.java.sharedcamera;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;

    public static ArrayList<Landmark> landMarkArray = new ArrayList<>(10000);

//    public ArrayList<Landmark> landMarkCacheArray = new ArrayList<>(1000);

    public float confidenceThreshold = (float) 0.4;

    public LandmarksHelper() {
        landMarkArray.add(new Landmark(0, 0, 1));
    }

    private int cleanCount = 0;

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

    public void addLandmarks(float[] pointBuffer) {

//        Log.d("Point buffer", String.valueOf(pointBuffer.length));

        for (int i = 0; i < pointBuffer.length; i = i + BYTES_PER_FLOAT) {

            float fx = pointBuffer[i + 0];
            float fy = pointBuffer[i + 1];
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
    

    public void cleanLandmarkArray() {

        Collections.sort(landMarkArray, (o1, o2) -> Float.compare(o2.con, o1.con));

        if (landMarkArray.size() > 10000) {
            landMarkArray.subList(10000, landMarkArray.size()-1).clear();
        }

    }

    public void increaseConfidenceThreshold() {
        this.confidenceThreshold += 0.05;
    }
}
