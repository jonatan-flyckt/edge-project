package com.EdgeSLAM;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class LandmarksHelper {

    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;

    final float CONF_THRESHOLD = (float) 0.5;

    float CLEAN_CAMERA_THRESHOLD = (float) 1;

    final int FRAMES_UNTIL_CLEAN = 20;

    final int POINTS_IN_GRIDINFO = 300;

    final int GRIDINFO_POINT_THRESHOLD = 40;

    // Sets the size of the grid, 1 = 1 gridinfo/m^2, 2 = 4 gridinfo/m^2 etc.
    final int GRID_SIZE = 4;

    // By default its 1m away from current visible area
    final int NEW_POINT_INCLUSION_DIST = (int)(1.5 * GRID_SIZE);

    // Viewport square in m^2, 3 * GRID_SIZE = 3m^2
    final int INITIAL_VIEW_SIZE = 3 * GRID_SIZE;

    public static ArrayList<Landmark> cameraLandMarkArray = new ArrayList<Landmark>(1000);
    GridZones gridZones = new GridZones();
    ArrayList<String> zonesUpdatedThisIteration = new ArrayList<String>();

    float averageCameraZ = 0;
    float floorPosition = 0;
    float distanceFromCameraToFloor = 0;
    private int cleanCount = 0;
    float lowX, highX, lowY, highY, camLowX, camHighX, camLowY, camHighY;

    public LandmarksHelper() {
        lowX = -INITIAL_VIEW_SIZE;
        lowY = -INITIAL_VIEW_SIZE;
        highX = INITIAL_VIEW_SIZE;
        highY = INITIAL_VIEW_SIZE;
        camLowX = 0;
        camHighX = 0;
        camLowY = 0;
        cameraLandMarkArray.add(new Landmark(0, 0, 0, 1));
    }

    //Class used to represent a landmark's position and strength of confidence
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

    //Removes camera landmarks that are too close to previous position
    public void cleanCameraLandmarkArray(){
        Iterator<Landmark> iterator = cameraLandMarkArray.iterator();
        float previousX = 0;
        float previousY = 0;
        while (iterator.hasNext()){
            Landmark currentLandmark = iterator.next();
            if ( Math.abs(currentLandmark.x - previousX) < CLEAN_CAMERA_THRESHOLD && Math.abs(currentLandmark.y - previousY) < CLEAN_CAMERA_THRESHOLD)
                iterator.remove();
            else {
                previousX = currentLandmark.x;
                previousY = currentLandmark.y;
            }
        }
    }

    //Adds camera landmark and updates average camera position
    public void addCameraLandMark(float x, float y, float z){
        cameraLandMarkArray.add(new Landmark(x * GRID_SIZE, y * GRID_SIZE, z * GRID_SIZE, 1));
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

    //Calculates the average height that the user has held the phone when scanning
    public void updateAverageCameraZ(){
        float zSum = 0;
        for (Landmark landmark: cameraLandMarkArray){
            zSum += landmark.z;
        }
        averageCameraZ = zSum / cameraLandMarkArray.size();
    }

    //Add landmarks if they have high enough confidence, and if they are not outliers
    //Clean the landmarks if we have added landmarks 20 times
    public void addLandmarks(float[] pointBuffer) {

        for (int i = 0; i < pointBuffer.length; i = i + BYTES_PER_FLOAT) {
            float fx = pointBuffer[i + 0] * GRID_SIZE;
            float fz = pointBuffer[i + 1] * GRID_SIZE;
            float fy = pointBuffer[i + 2] * GRID_SIZE;
            float fcon = pointBuffer[i + 3];

            if (fx < highX+ NEW_POINT_INCLUSION_DIST && fx > lowX- NEW_POINT_INCLUSION_DIST && fy < highY+ NEW_POINT_INCLUSION_DIST && fy > lowY- NEW_POINT_INCLUSION_DIST && fcon > CONF_THRESHOLD) {
                Landmark newLandmark = new Landmark(fx, fy, fz, fcon);
                String key = keyFromGridBounds((int) newLandmark.x, (int) newLandmark.x + 1, (int) newLandmark.y, (int) newLandmark.y + 1);
                if (gridZones.get(key) == null){
                    gridZones.put(key, new GridInfo(POINTS_IN_GRIDINFO, fx, fy));
                }
                GridInfo gridInfo = gridZones.get(key);
                gridInfo.landmarks.add(newLandmark);
                gridZones.put(key, gridInfo);
                if (!zonesUpdatedThisIteration.contains(key))
                    zonesUpdatedThisIteration.add(key);
            }
        }

        cleanCount++;
        if (cleanCount > FRAMES_UNTIL_CLEAN) {
            cleanCount = 0;
            cleanLandmarks();
            zonesUpdatedThisIteration.clear();
        }
    }

    //Performs pruning of landmarks with several steps
    public void cleanLandmarks() {
        refineGridLandmarks();
        removeGridsWithFewPoints(GRIDINFO_POINT_THRESHOLD);
        cleanCameraLandmarkArray();
        estimateFloorPosition();
        updateExtremePoints();
    }

    //Estimate the position of the floor on the Z axis by finding the largest distribution of landmarks
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
        }
        floorPosition = Float.parseFloat(currentEstimatedFloorKey);
        distanceFromCameraToFloor = Math.abs(averageCameraZ - floorPosition);
    }

    //Exclude grids with fewer than 40 landmarks
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

    //Calculates new extreme points for the entire plot
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

    //Prunes a grid from points by excluding points with low confidence
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
            }
            gridInfo.confidence = confSum / gridInfo.landmarks.size();
        }
    }

    //HashMap key value for grids based on bounds of grid
    public String keyFromGridBounds(int xLo, int xHi, int yLo, int yHi){
        StringBuilder sb = new StringBuilder();
        sb.append(xLo).append(xHi).append(yLo).append(yHi);
        return sb.toString();
    }

    //Resets all values when the user presses STOP
    public void resetSLAM() {
        zonesUpdatedThisIteration.clear();
        gridZones.clear();
        cameraLandMarkArray.clear();
        lowX = -INITIAL_VIEW_SIZE;
        lowY = -INITIAL_VIEW_SIZE;
        highX = INITIAL_VIEW_SIZE;
        highY = INITIAL_VIEW_SIZE;
        camLowX = 0;
        camHighX = 0;
        camLowY = 0;
        camHighY = 0;
        cameraLandMarkArray.add(new Landmark(0, 0, 0, 1));
    }

}







