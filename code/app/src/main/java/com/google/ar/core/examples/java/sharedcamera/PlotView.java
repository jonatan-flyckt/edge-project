package com.google.ar.core.examples.java.sharedcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PlotView extends View {
    Paint cameraPaint, cameraPathPaint, landmarkPaint, backgroundPaint;
    int screenWidth, screenHeight;
    float landmarkCircleSize, cameraCircleSize;
    Path path;

    float lowX, highX, lowY, highY;
    float origoX, origoY;
    float plotScalingFactor;

    ArrayList<Point> cameraPath = tempGetCameraPoints(getContext());

    public void updateExtremePoints(ArrayList<Point> points, ArrayList<Point> cameraPoints){
        lowX = points.get(0).x;
        highX = points.get(0).x;
        lowY = points.get(0).y;
        highY = points.get(0).y;
        for (Point point: points){
            if (point.x > highX)
                highX = point.x;
            if (point.x < lowX)
                lowX = point.x;
            if (point.y > highY)
                highY = point.y;
            if (point.y < lowY)
                lowY = point.y;
        }
        for (Point point: cameraPoints){
            if (point.x > highX)
                highX = point.x;
            if (point.x < lowX)
                lowX = point.x;
            if (point.y > highY)
                highY = point.y;
            if (point.y < lowY)
                lowY = point.y;
        }
    }

    public float setPlotScalingFactors(){
        float xDist = highX - lowX;
        float yDist = highY - lowY;
        origoX = 0 - lowX;
        origoY = 0 - lowY;
        float xScale = screenWidth / xDist;
        float yScale = screenHeight / yDist;
        return Math.max(xScale, yScale);
    }

    public ArrayList<Point> tempGetCameraPoints(Context context){
        ArrayList<Point> list = new ArrayList<Point>(){
            {
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 20) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 90)))));
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 0) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 60)))));
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 0) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 20)))));
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 50) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 30)))));
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 30) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 60)))));
                add(new Point((int) (plotScalingFactor *(pxFromDp(context, 90) + (screenWidth / 2))),
                        (int) (plotScalingFactor *(pxFromDp(context, 60)))));
            }
        };
        return list;
    }

    private int amount = 10;
    private float scaling = 1.0f;
    public ArrayList<Point> tempGetLandmarkPoints(Context context){
        ArrayList<Point> list = new ArrayList<Point>();
        for (int i = 0; i < amount; i++){
            list.add(new Point(ThreadLocalRandom.current().nextInt(0, (int)(screenWidth * scaling)),
                    ThreadLocalRandom.current().nextInt(0, (int)(screenHeight * scaling))));
        }
        amount += 50;
        scaling *= 1.01;
        return list;
    }

    public PlotView(Context context) {
        super(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        landmarkCircleSize = pxFromDp(context, 3);
        cameraCircleSize = pxFromDp(context, 5);
        landmarkPaint = new Paint();
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setAlpha(30);
        cameraPaint = new Paint();
        cameraPaint.setStrokeWidth(cameraCircleSize);
        cameraPaint.setStyle(Paint.Style.FILL);
        cameraPaint.setColor(Color.GREEN);
        cameraPathPaint = new Paint();
        cameraPathPaint.setStyle(Paint.Style.STROKE);
        cameraPathPaint.setColor(Color.GREEN);
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.BLACK);
    }

    private Path getCameraPath(ArrayList<Point> points){
        path = new Path();
        boolean first = true;
        for (Point point : points){
            if (first){
                first = false;
                path.moveTo(origoX + point.x, origoY + point.y);
            }
            else
                path.lineTo(origoX + point.x, origoY + point.y);
        }
        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        updateExtremePoints(tempGetLandmarkPoints(getContext()), cameraPath);
        plotScalingFactor = setPlotScalingFactors();

        super.onDraw(canvas);
        canvas.drawPaint(backgroundPaint);

        //Draw all landmark points:
        for (Point point: tempGetLandmarkPoints(getContext())){
            canvas.drawCircle(origoX + point.x * plotScalingFactor, origoY + point.y * plotScalingFactor, landmarkCircleSize * plotScalingFactor, landmarkPaint);
        }


        cameraPathPaint.setStrokeWidth((cameraCircleSize / 3)*plotScalingFactor);
        //Draw camera position and path:
        canvas.drawPath(getCameraPath(cameraPath), cameraPathPaint);
        canvas.drawCircle(origoX + cameraPath.get(cameraPath.size() - 1).x * plotScalingFactor,
                (origoY + cameraPath.get(cameraPath.size() - 1).y) * plotScalingFactor,
                cameraCircleSize * plotScalingFactor, cameraPaint);

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}