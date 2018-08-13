package org.fhcrc.optides.apps.UPLCPeakTracerApp;

import org.fhcrc.optides.apps.HPLCPeakClassifierApp.HPLCPeak;

public class HPLCPeakComparable extends HPLCPeak implements Comparable<HPLCPeakComparable> {
	public HPLCPeakComparable(double rt, double au) {
		super(rt, au);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int compareTo(HPLCPeakComparable otherPeak) {
		if(this.getAu()*1000 - otherPeak.getAu()*1000 > 0) {
			return -1;
		} else if(this.getAu()*1000 - otherPeak.getAu()*1000 < 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public String toString() {
		return "[" + getRt() + "," + getAu() + "]";
	}
}
