package com.google.ar.core.examples.java.sharedcamera;

import java.util.ArrayList;

public class LandmarksHelper {

    // 1000 is 3 decimals, 100 is 2 etc.
    private static final int FLOAT_TO_INT_DECIMALS = 1000;

    public ArrayList<Landmark> landMarkArray = new ArrayList<>();

    public float confidenceThreshold = (float) 0.4;

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

    // To round the float to 3 decimals, we convert to int.
    public void addLandmark(float fx, float fy, float fz, float fcon) {
        int ix = (int) fx * FLOAT_TO_INT_DECIMALS;
        int iy = (int) fy * FLOAT_TO_INT_DECIMALS;
        int iz = (int) fz * FLOAT_TO_INT_DECIMALS;
        int icon = (int) fcon * FLOAT_TO_INT_DECIMALS;

        Landmark newLandmark = new Landmark(ix, iy, iz, icon);
        if (!landMarkArray.contains(newLandmark)) {
            landMarkArray.add(newLandmark);
        }
    }

    public void increaseConfidenceThreshold() {
        this.confidenceThreshold += 0.05;
    }

    public void cleanLandmarkArray() {
        for (Landmark landmark : landMarkArray) {
            if (landmark.con < this.confidenceThreshold) {
                landMarkArray.remove(landmark);
            }
        }
    }


}
