package com.EdgeSLAM;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class PlotHelper extends View {
    Paint cameraPaint, cameraPathPaint, landmarkPaint, backgroundPaint;
    float landmarkCircleSize, cameraCircleSize;
    Path path;
    float plotScalingFactor, pointScalingFactor;
    int numberOfPlotPoints;
    int landmarkBaselineAlpha = 25;
    public LandmarksHelper landmarksHelper;

    //Calculate what scale the plot should have based on extreme points and the screen resolution
    public float setPlotScalingFactors(){
        float xDist = Math.max(landmarksHelper.highX, landmarksHelper.camHighX) - Math.min(landmarksHelper.lowX, landmarksHelper.camLowX);
        float yDist = Math.max(landmarksHelper.highY, landmarksHelper.camHighY) - Math.min(landmarksHelper.lowY, landmarksHelper.camLowY);
        float xScale = getWidth() / xDist;
        float yScale = getHeight() / yDist;
        return Math.min(xScale, yScale);
    }

    //Base the point size on the total amount of scanned points
    public float setPointScalingFactor(){
        return (float)Math.pow(numberOfPlotPoints, 0.15);
    }

    //Update zoom and graphic sizes
    public void updatePlotSettings(){
        plotScalingFactor = setPlotScalingFactors();
        pointScalingFactor = setPointScalingFactor();
    }

    //Scales a point position based on screen size and the extreme points in x and y directions
    public Point scaledPointFromLandmark(LandmarksHelper.Landmark landmark){
        float newX = (landmark.x - landmarksHelper.lowX) * plotScalingFactor;
        float newY = (landmark.y - landmarksHelper.lowY) * plotScalingFactor;
        return new Point((int)newX, (int)newY);
    }

    //Create paints and initiate the correct display metric values
    public PlotHelper(Context context) {
        super(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        landmarkCircleSize = pxFromDp(context, 6);
        cameraCircleSize = pxFromDp(context, 20);

        landmarkPaint = new Paint();
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setAlpha(landmarkBaselineAlpha);
        cameraPaint = new Paint();
        cameraPaint.setStrokeWidth(cameraCircleSize);
        cameraPaint.setStyle(Paint.Style.FILL);
        cameraPaint.setColor(Color.CYAN);
        cameraPathPaint = new Paint();
        cameraPathPaint.setStyle(Paint.Style.STROKE);
        cameraPathPaint.setAntiAlias(true);
        cameraPathPaint.setStrokeCap(Paint.Cap.ROUND);
        cameraPathPaint.setColor(Color.CYAN);
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.TRANSPARENT);
    }

    //Calculates the alpha with which to plot a point based on its height position
    int getZAlpha(float z){
        if (z > landmarksHelper.averageCameraZ)
            return landmarkBaselineAlpha;
        float distance = Math.abs(z - landmarksHelper.averageCameraZ);
        if (distance > landmarksHelper.distanceFromCameraToFloor){
            return 0;
        }
        else{
            return (int)(landmarkBaselineAlpha * (1 - Math.pow(distance / landmarksHelper.distanceFromCameraToFloor, 2)));
        }
    }

    //Calculates the colour with which to plot a point based on its confidence
    String colorFromConfidence(float confidence){
        String[] colors = {"#ff0000", "#ff5100", "#ff7700", "#ff9900", "#ffc400", "#ffe600", "#eaff00", "#b3ff00", "#80ff00", "#00ff00"};
        int colorPos = (int) (Math.pow(normalizedConfidence(confidence), 1.5) * 10);
        if (confidence == 10)
            return colors[9];
        return colors[colorPos];
    }

    //Normalizes confidence so that the interval is always 0-1
    float normalizedConfidence(float confidence){
        float lowerBound = landmarksHelper.CONF_THRESHOLD;
        float upperBound = 1;
        return (confidence - lowerBound) / (upperBound - lowerBound);
    }

    //Return the path of the camera
    private Path getCameraPath(ArrayList<LandmarksHelper.Landmark> landmarks){
        path = new Path();
        boolean first = true;
        for (LandmarksHelper.Landmark landmark : landmarks){
            Point point = scaledPointFromLandmark(landmark);
            if (first){
                first = false;
                path.moveTo(point.x, point.y);
            }
            else
                path.lineTo(point.x, point.y);
        }
        return path;
    }

    //Redraws the entire canvas
    @Override
    protected void onDraw(Canvas canvas) {
        //Check if we need to zoom out or re-scale the point sizes
        updatePlotSettings();
        canvas.drawPaint(backgroundPaint);

        //Plot all the landmarks with accurate alpha and colour
        int pointSum = 0;
        for (String key: landmarksHelper.gridZones.keySet()){
            GridInfo gridInfo = landmarksHelper.gridZones.get(key);
            try {
                CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarkArrayList = new CopyOnWriteArrayList<LandmarksHelper.Landmark>(gridInfo.landmarks);
                for (LandmarksHelper.Landmark landmark : landmarkArrayList) {
                    Point point = scaledPointFromLandmark(landmark);
                    RadialGradient gradient = new RadialGradient((float) point.x, (float) point.y, landmarkCircleSize / pointScalingFactor,
                            Color.parseColor(colorFromConfidence(landmark.con)), Color.TRANSPARENT, Shader.TileMode.CLAMP);
                    landmarkPaint.setShader(gradient);
                    landmarkPaint.setAlpha(getZAlpha(landmark.z));
                    canvas.drawCircle(point.x, point.y, landmarkCircleSize / pointScalingFactor, landmarkPaint);
                    pointSum++;
                }
            }
            catch (Exception e){
                continue;
            }
        }
        numberOfPlotPoints = pointSum;

        //Draw camera path and current position
        cameraPathPaint.setStrokeWidth((cameraCircleSize / 3) / pointScalingFactor);
        CornerPathEffect corEffect = new CornerPathEffect((int)(getHeight() * 0.04 / pointScalingFactor));
        cameraPathPaint.setPathEffect(corEffect);
        cameraPathPaint.setAlpha(50);
        try {
            cameraLandmarkPath = landmarksHelper.getCameraLandMarkArray();
            cameraPoint = scaledPointFromLandmark(cameraLandmarkPath.get(cameraLandmarkPath.size() - 1));
            canvas.drawPath(getCameraPath(cameraLandmarkPath), cameraPathPaint);
            canvas.drawCircle(cameraPoint.x, cameraPoint.y,cameraCircleSize /pointScalingFactor, cameraPaint);
        } catch (Exception e){
            canvas.drawPath(getCameraPath(cameraLandmarkPath), cameraPathPaint);
            canvas.drawCircle(cameraPoint.x, cameraPoint.y,cameraCircleSize /pointScalingFactor, cameraPaint);
        }
    }
    Point cameraPoint;
    ArrayList<LandmarksHelper.Landmark> cameraLandmarkPath;

    //Calculate pixel size from screen resolution
    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}