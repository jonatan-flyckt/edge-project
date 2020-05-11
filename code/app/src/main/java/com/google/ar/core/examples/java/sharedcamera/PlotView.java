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


public class PlotView extends View {
    Paint cameraPaint, cameraPathPaint, landmarkPaint, backgroundPaint;
    int screenWidth, screenHeight;
    float landmarkCircleSize, cameraCircleSize;
    Path path;

    //float lowX, highX, lowY, highY;
    //float origoX, origoY;
    float plotScalingFactor, pointScalingFactor;
    int numberOfPlotPoints;

    public LandmarksHelper landmarksHelper;

    boolean isFirstTime = true;

    /*public void updateExtremeLandmarks(ArrayList<LandmarksHelper.Landmark> landmarks, ArrayList<LandmarksHelper.Landmark> cameraLandmarks){
        if (isFirstTime) {
            isFirstTime = false;
            lowX = landmarks.get(0).x;
            highX = landmarks.get(0).x;
            lowY = landmarks.get(0).y;
            highY = landmarks.get(0).y;
        }

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
    }*/

    public float setPlotScalingFactors(){
        float xDist = landmarksHelper.highX - landmarksHelper.lowX;
        float yDist = landmarksHelper.highY - landmarksHelper.lowY;
        float xScale = getWidth() / xDist;
        float yScale = getHeight() / yDist;
        Log.d("EH low X", String.valueOf(landmarksHelper.lowX));
        Log.d("EH high X", String.valueOf(landmarksHelper.highX));
        Log.d("EH low Y", String.valueOf(landmarksHelper.lowY));
        Log.d("EH high Y", String.valueOf(landmarksHelper.highY));
        return Math.max(xScale, yScale);
    }

    public float setPointScalingFactor(){
        return (float)Math.pow(numberOfPlotPoints, 0.1);
    }

    public void updatePlotSettings(){
        //updateExtremeLandmarks(landmarksHelper.getLandMarkArray(), landmarksHelper.getCameraLandMarkArray());
        plotScalingFactor = setPlotScalingFactors();
        pointScalingFactor = setPointScalingFactor();
        //updateOrigo();
    }

    public Point scaledPointFromLandmark(LandmarksHelper.Landmark landmark){
        float newX = (landmark.x - landmarksHelper.lowX) * plotScalingFactor;
        float newY = (landmark.y - landmarksHelper.lowY) * plotScalingFactor;
        return new Point((int)newX, (int)newY);
    }

    public ArrayList<LandmarksHelper.Landmark> tempGetCameraPoints(Context context){
        ArrayList<LandmarksHelper.Landmark> list = new ArrayList<LandmarksHelper.Landmark>(){
            {
                add(new LandmarksHelper.Landmark(-0.5f, 0.5f, 1f));
                add(new LandmarksHelper.Landmark(-0.3f, 0.3f, 1f));
                add(new LandmarksHelper.Landmark(0.1f, 0.5f, 1f));
                add(new LandmarksHelper.Landmark(0.1f, 0.8f, 1f));
                add(new LandmarksHelper.Landmark(0.5f, 0.2f, 1f));
                add(new LandmarksHelper.Landmark(0.8f, -0.2f, 1f));
            }
        };
        return list;
    }

    //private int amount = 10;
    //private float scaling = 1.0f;
    /*public ArrayList<LandmarksHelper.Landmark> tempGetLandmarks(Context context){
        ArrayList<LandmarksHelper.Landmark> list = new ArrayList<LandmarksHelper.Landmark>();
        for (int i = 0; i < amount; i++){
            list.add(new LandmarksHelper.Landmark((float)(-scaling + Math.random() * (scaling*2)),
                    (float)(-scaling + Math.random() * (scaling*2)), 1f));
        }
        amount += 2;
        scaling *= 1.01;
        numberOfPlotPoints = list.size();
        return list;
    }*/

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

        //landmarkCircleSize = pxFromDp(context, 3);
        landmarkCircleSize = pxFromDp(context, 4);
        cameraCircleSize = pxFromDp(context, 5);

        landmarkPaint = new Paint();
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setAlpha(20);
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

        /*if (landmarksHelper.isBeingCleaned) {
            return;
        }*/
        updatePlotSettings();

        super.onDraw(canvas);
        canvas.drawPaint(backgroundPaint);



        //Draw all landmark points:
        /*for (LandmarksHelper.Landmark landmark: tempGetLandmarks(getContext())){
            Point point = scaledPointFromLandmark(landmark);
            RadialGradient gradient = new RadialGradient((float)point.x, (float)point.y, landmarkCircleSize / pointScalingFactor,
                    Color.RED, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            landmarkPaint.setShader(gradient);
            canvas.drawCircle(point.x, point.y, landmarkCircleSize / pointScalingFactor, landmarkPaint);
        }*/

        ArrayList<LandmarksHelper.Landmark> landmarkCopy = landmarksHelper.getLandMarkArray();
        numberOfPlotPoints = landmarkCopy.size();
        Log.d("EH", String.valueOf(landmarkCopy.size()));
        for (LandmarksHelper.Landmark landmark: landmarkCopy){
            Point point = scaledPointFromLandmark(landmark);
            RadialGradient gradient = new RadialGradient((float)point.x, (float)point.y, landmarkCircleSize / pointScalingFactor,
                    Color.RED, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            landmarkPaint.setShader(gradient);
            canvas.drawCircle(point.x, point.y, landmarkCircleSize / pointScalingFactor, landmarkPaint);
        }

        Log.d("EH", Float.toString(landmarkCircleSize));
        Log.d("EH", Float.toString(plotScalingFactor));

        //Draw camera path and current position
        cameraPathPaint.setStrokeWidth((cameraCircleSize / 3) / pointScalingFactor);
        CornerPathEffect corEffect = new CornerPathEffect((int)(getHeight() * 0.04 / pointScalingFactor));
        cameraPathPaint.setPathEffect(corEffect);
        ArrayList<LandmarksHelper.Landmark> cameraLandmarkPath = landmarksHelper.getCameraLandMarkArray();
        Log.d("EH camerapath", String.valueOf(cameraLandmarkPath.size()));
        canvas.drawPath(getCameraPath(cameraLandmarkPath), cameraPathPaint);
        Point cameraPoint = scaledPointFromLandmark(cameraLandmarkPath.get(cameraLandmarkPath.size() - 1));
        canvas.drawCircle(cameraPoint.x, cameraPoint.y,cameraCircleSize /pointScalingFactor, cameraPaint);
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}