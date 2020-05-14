package com.google.ar.core.examples.java.sharedcamera;

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


public class PlotView extends View {
    Paint cameraPaint, cameraPathPaint, landmarkPaint, backgroundPaint;
    int screenWidth, screenHeight;
    float landmarkCircleSize, cameraCircleSize;
    Path path;

    float plotScalingFactor, pointScalingFactor;
    int numberOfPlotPoints;

    public LandmarksHelper landmarksHelper;


    public float setPlotScalingFactors(){
        float xDist = landmarksHelper.highX - landmarksHelper.lowX;
        float yDist = landmarksHelper.highY - landmarksHelper.lowY;
        float xScale = getWidth() / xDist;
        float yScale = getHeight() / yDist;
        return Math.max(xScale, yScale);
    }

    public float setPointScalingFactor(){
        return (float)Math.pow(numberOfPlotPoints, 0.1);
    }

    public void updatePlotSettings(){
        plotScalingFactor = setPlotScalingFactors();
        pointScalingFactor = setPointScalingFactor();
    }

    public Point scaledPointFromLandmark(LandmarksHelper.Landmark landmark){
        float newX = (landmark.x - landmarksHelper.lowX) * plotScalingFactor;
        float newY = (landmark.y - landmarksHelper.lowY) * plotScalingFactor;
        return new Point((int)newX, (int)newY);
    }

    public PlotView(Context context) {
        super(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        screenWidth = getWidth();
        screenHeight = getHeight();

        landmarkCircleSize = pxFromDp(context, 4);
        cameraCircleSize = pxFromDp(context, 5);

        landmarkPaint = new Paint();
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setAlpha(25);
        cameraPaint = new Paint();
        cameraPaint.setStrokeWidth(cameraCircleSize);
        cameraPaint.setStyle(Paint.Style.FILL);
        cameraPaint.setColor(Color.GREEN);
        cameraPathPaint = new Paint();
        cameraPathPaint.setStyle(Paint.Style.STROKE);
        cameraPathPaint.setAntiAlias(true);
        cameraPathPaint.setStrokeCap(Paint.Cap.ROUND);
        cameraPathPaint.setColor(Color.GREEN);
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.TRANSPARENT);
    }

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

    @Override
    protected void onDraw(Canvas canvas) {

        updatePlotSettings();

        super.onDraw(canvas);
        canvas.drawPaint(backgroundPaint);

        int pointSum = 0;
        for (String key: landmarksHelper.gridZones.keySet()){
            GridInfo gridInfo = landmarksHelper.gridZones.get(key);
            try {
                CopyOnWriteArrayList<LandmarksHelper.Landmark> landmarkArrayList = new CopyOnWriteArrayList<LandmarksHelper.Landmark>(gridInfo.landmarks);
                for (LandmarksHelper.Landmark landmark : landmarkArrayList) {
                    Point point = scaledPointFromLandmark(landmark);
                    RadialGradient gradient = new RadialGradient((float) point.x, (float) point.y, landmarkCircleSize / pointScalingFactor,
                            Color.RED, Color.TRANSPARENT, Shader.TileMode.CLAMP);
                    landmarkPaint.setShader(gradient);
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
        ArrayList<LandmarksHelper.Landmark> cameraLandmarkPath = landmarksHelper.getCameraLandMarkArray();
        canvas.drawPath(getCameraPath(cameraLandmarkPath), cameraPathPaint);
        Point cameraPoint = scaledPointFromLandmark(cameraLandmarkPath.get(cameraLandmarkPath.size() - 1));
        canvas.drawCircle(cameraPoint.x, cameraPoint.y,cameraCircleSize /pointScalingFactor, cameraPaint);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}