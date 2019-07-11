package org.fhcrc.optides.apps.UPLCPeakTracerApp;

/**
 * Created by mbrusnia on 8/13/18.
 */
import org.fhcrc.optides.apps.HPLCPeakClassifierApp.HPLCPeak;

public class HPLCPeakComparable extends HPLCPeak implements Comparable<HPLCPeakComparable> {
    public HPLCPeakComparable(double rt, double au) {
        super(rt, au);
        // TODO Auto-generated constructor stub
    }
    public HPLCPeakComparable(HPLCPeakComparable p) {
        super(p.getRt(), p.getAu());
        // TODO Auto-generated constructor stub
    }

    public int compareTo(HPLCPeakComparable otherPeak) {
        return this.getRt() < otherPeak.getRt() ? 1:0;
    }

    public String toString() {
        return "[" + getRt() + "," + getAu() + "]";
    }
}