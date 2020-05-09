package com.google.ar.core.examples.java.sharedcamera;

import android.os.Handler;
import android.util.Log;

import com.github.mikephil.charting.charts.ScatterChart;

import java.util.concurrent.TimeUnit;

public abstract class PlottingManager implements Runnable{
//public class PlottingManager{
    LandmarksHelper landmarksHelper;
    ScatterChart chart;

    public PlottingManager(LandmarksHelper _landmarksHelper, ScatterChart _chart){
        landmarksHelper = _landmarksHelper;
        chart = _chart;
    }





    public void plotPoints(){
        Log.d("EH", "looping with 1 second sleep.");
    }
}
