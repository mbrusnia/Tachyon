package org.fhcrc.optides.apps.HPLCPeakClassifierApp;

public class HPLCPeak {
	private double rt;
	private double au;

	public HPLCPeak(double rt, double au) {
		this.setRt(rt);
		this.setAu(au);
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public double getAu() {
		return au;
	}

	public void setAu(double au) {
		this.au = au;
	}

}
