package org.fhcrc.optides.apps.HPLCFractionTracerApp;

import org.fhcrc.optides.apps.HPLCPeakClassifierApp.HPLCPeak;

public class HPLCFractionPeakComparable extends HPLCPeak implements Comparable<HPLCFractionPeakComparable>  {
    public HPLCFractionPeakComparable(double rt, double au) {
        super(rt, au);
        // TODO Auto-generated constructor stub
    }
    public HPLCFractionPeakComparable(HPLCFractionPeakComparable p) {
        super(p.getRt(), p.getAu());
        // TODO Auto-generated constructor stub
    }

    public int compareTo(HPLCFractionPeakComparable otherPeak) {
        return this.getRt() < otherPeak.getRt() ? 1:0;
    }

    public String toString() {
        return "[" + getRt() + "," + getAu() + "]";
    }
}
