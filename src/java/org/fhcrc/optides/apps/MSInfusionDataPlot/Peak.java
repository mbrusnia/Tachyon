package org.fhcrc.optides.apps.MSInfusionDataPlot;


public class Peak implements Comparable<Peak> {
	public int scanNum;
	public String z;
	public double mz;
	public double intensity;
	public Peak(int newScanNo, double newMz, double newIntensity, String zLabel) {
		scanNum = newScanNo;
		mz = newMz;
		intensity = newIntensity;
		z = zLabel;
		
	}
	@Override
	public int compareTo(Peak o) {
		if(this.mz == o.mz)
			return 0;
		else
			return this.mz < o.mz ? 1 : -1;
	}
}
