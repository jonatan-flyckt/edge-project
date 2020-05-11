package com.google.ar.core.examples.java.sharedcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;


public class PlotView extends View {
    Paint cameraPaint, cameraPathPaint, landmarkPaint, backgroundPaint;
    int screenWidth, screenHeight;
    float landmarkCircleSize, cameraCircleSize;
    Path path;

    float lowX, highX, lowY, highY;
    float origoX, origoY;
    float plotScalingFactor, pointScalingFactor;
    int numberOfPlotPoints;

    ArrayList<LandmarksHelper.Landmark> cameraPath = tempGetCameraPoints(getContext());

    public void updateExtremeLandmarks(ArrayList<LandmarksHelper.Landmark> landmarks, ArrayList<LandmarksHelper.Landmark> cameraLandmarks){
        lowX = landmarks.get(0).x;
        highX = landmarks.get(0).x;
        lowY = landmarks.get(0).y;
        highY = landmarks.get(0).y;
        for (LandmarksHelper.Landmark landmark: landmarks){
            if (landmark.x > highX)
                highX = landmark.x;
            if (landmark.x < lowX)
                lowX = landmark.x;
            if (landmark.y > highY)
                highY = landmark.y;
            if (landmark.y < lowY)
                lowY = landmark.y;
        }
        for (LandmarksHelper.Landmark landmark: cameraLandmarks){
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

    public float setPlotScalingFactors(){
        float xDist = highX - lowX;
        float yDist = highY - lowY;
        float xScale = getWidth() / xDist;
        float yScale = getHeight() / yDist;
        return Math.max(xScale, yScale);
    }

    public float setPointScalingFactor(){
        return (float)Math.pow(numberOfPlotPoints, 0.1);
    }

    public void updateOrigo(){
        origoX = lowX;
        origoY = lowY;
        Log.d("EH", Float.toString(origoX));
        Log.d("EH", Float.toString(origoX));
    }

    public void updatePlotSettings(){
        updateExtremeLandmarks(tempGetLandmarks(getContext()), cameraPath);
        plotScalingFactor = setPlotScalingFactors();
        pointScalingFactor = setPointScalingFactor();
        updateOrigo();
    }

    public Point scaledPointFromLandmark(LandmarksHelper.Landmark landmark){
        float newX = (landmark.x - origoX) * plotScalingFactor;
        float newY = (landmark.y - origoY) * plotScalingFactor;
        return new Point((int)newX, (int)newY);
    }

    public ArrayList<LandmarksHelper.Landmark> tempGetCameraPoints(Context context){
        ArrayList<LandmarksHelper.Landmark> list = new ArrayList<LandmarksHelper.Landmark>(){
            {
                add(new LandmarksHelper.Landmark(-0.5f, 0.5f, 0f, 1f));
                add(new LandmarksHelper.Landmark(-0.3f, 0.3f, 0f, 1f));
                add(new LandmarksHelper.Landmark(0.1f, 0.5f, 0f, 1f));
                add(new LandmarksHelper.Landmark(0.1f, 0.8f, 0f, 1f));
                add(new LandmarksHelper.Landmark(0.5f, 0.2f, 0f, 1f));
                add(new LandmarksHelper.Landmark(0.8f, -0.2f, 0f, 1f));
            }
        };
        return list;
    }

    private int amount = 10;
    private float scaling = 1.0f;
    public ArrayList<LandmarksHelper.Landmark> tempGetLandmarks(Context context){
        ArrayList<LandmarksHelper.Landmark> list = new ArrayList<LandmarksHelper.Landmark>();
        for (int i = 0; i < amount; i++){
            list.add(new LandmarksHelper.Landmark((float)(-scaling + Math.random() * (scaling*2)),
                    (float)(-scaling + Math.random() * (scaling*2)), 0f, 1f));
        }
        amount += 50;
        scaling *= 1.1;
        numberOfPlotPoints = list.size();
        return list;
    }

    public PlotView(Context context) {
        super(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        //screenWidth = displayMetrics.widthPixels;
        //screenHeight = displayMetrics.heightPixels;
        screenWidth = getWidth();
        screenHeight = getHeight();

        landmarkCircleSize = pxFromDp(context, 3);
        cameraCircleSize = pxFromDp(context, 5);

        landmarkPaint = new Paint();
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setAlpha(60);
        cameraPaint = new Paint();
        cameraPaint.setStrokeWidth(cameraCircleSize);
        cameraPaint.setStyle(Paint.Style.FILL);
        cameraPaint.setColor(Color.GREEN);
        cameraPathPaint = new Paint();
        cameraPathPaint.setStyle(Paint.Style.STROKE);
        cameraPathPaint.setColor(Color.GREEN);
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
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

        //Draw all landmark points:
        for (LandmarksHelper.Landmark landmark: tempGetLandmarks(getContext())){
            Point point = scaledPointFromLandmark(landmark);
            canvas.drawCircle(point.x, point.y, landmarkCircleSize / pointScalingFactor, landmarkPaint);
        }
        Log.d("EH", Float.toString(landmarkCircleSize));
        Log.d("EH", Float.toString(plotScalingFactor));

        //Draw camera path and current position
        cameraPathPaint.setStrokeWidth((cameraCircleSize / 3) / pointScalingFactor);
        canvas.drawPath(getCameraPath(cameraPath), cameraPathPaint);
        Point cameraPoint = scaledPointFromLandmark(cameraPath.get(cameraPath.size() - 1));
        canvas.drawCircle(cameraPoint.x, cameraPoint.y,cameraCircleSize /pointScalingFactor, cameraPaint);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}