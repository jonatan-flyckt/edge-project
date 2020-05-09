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

    int lowest, highest, leftmost, rightmost;

    public ArrayList<Point> tempGetCameraPoints(Context context){
        ArrayList<Point> list = new ArrayList<Point>(){
            {
                add(new Point((int) pxFromDp(context, 20) + (screenWidth / 2), (int) pxFromDp(context, 90)));
                add(new Point((int) pxFromDp(context, 0) + (screenWidth / 2), (int) pxFromDp(context, 60)));
                add(new Point((int) pxFromDp(context, 0) + (screenWidth / 2), (int) pxFromDp(context, 20)));
                add(new Point((int) pxFromDp(context, 50) + (screenWidth / 2), (int) pxFromDp(context, 30)));
                add(new Point((int) pxFromDp(context, 30) + (screenWidth / 2), (int) pxFromDp(context, 60)));
                add(new Point((int) pxFromDp(context, 90) + (screenWidth / 2), (int) pxFromDp(context, 60)));
            }
        };
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
        landmarkCircleSize = pxFromDp(context, 2);
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
        cameraPathPaint.setStrokeWidth(cameraCircleSize / 3);
        cameraPathPaint.setStyle(Paint.Style.STROKE);
        cameraPathPaint.setColor(Color.GREEN);
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.DKGRAY);
    }

    private Path getCameraPath(ArrayList<Point> points){
        path = new Path();
        boolean first = true;
        for (Point point : points){
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
        super.onDraw(canvas);
        canvas.drawPaint(backgroundPaint);


        for (int i = 0; i < 10000; i++){
            canvas.drawCircle(ThreadLocalRandom.current().nextInt(0, getWidth()),
                    ThreadLocalRandom.current().nextInt(0, getHeight()),
                    landmarkCircleSize, landmarkPaint);
        }

        //Draw camera position and path:
        canvas.drawPath(getCameraPath(tempGetCameraPoints(getContext())), cameraPathPaint);
        canvas.drawCircle(tempGetCameraPoints(getContext()).get(tempGetCameraPoints(getContext()).size() - 1).x,
                tempGetCameraPoints(getContext()).get(tempGetCameraPoints(getContext()).size() - 1).y,
                cameraCircleSize, cameraPaint);

    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}