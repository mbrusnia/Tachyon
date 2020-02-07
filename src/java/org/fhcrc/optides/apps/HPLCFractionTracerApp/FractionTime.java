package org.fhcrc.optides.apps.HPLCFractionTracerApp;

public class FractionTime {
    private float start_rt;
    private float end_rt;
    public FractionTime(float r1, float r2){
        this.start_rt = r1;
        this.end_rt = r2;
    }
    float getStart_rt(){
        return start_rt;
    }
    float getEnd_rt(){
        return end_rt;
    }
}
