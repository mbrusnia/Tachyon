package org.fhcrc.optides.apps.MSInfusionDataPlot;

public class Constants {
	
	//expected difference in m/z for each different charge state of an isotope
	public final static double z1 = 1.0;
	public final static double z2 = 0.5;
	public final static double z3 = 0.3333333;
	public final static double z4 = 0.25;
	public final static double z5 = 0.2;
	public final static double z6 = 0.1666667;
	public final static double z7 = 0.142857;
	public final static double z8 = 0.125;
	public final static double z9 = 0.1111111;

	//when matching peaks across spectra, for a ppm of 15, this is the mz tolerance
	public final static double mzTolerance = .01;

	//when we are peak picking a spectrum, we cut out all intensities below
	//peakPickingIntensityCuttoff * highestIntensity of that spectrum
	public final static double peakPickingIntensityCuttoff = .05;
	
	//the program's output of averaged monoisotopic peaks across several spectrum
	//only reports peaks that are finalIntensityCuttoff*HighestIntensityOfAllSpectra
	public final static double finalIntensityCuttoff = .10;
	
	//There is false data around 824.62, so we ignore that part of the 
	//spectra, with a window of mzToIgnoreTolerance
	public final static double mzToIgnore = 824.62;
	public final static double mzToIgnoreTolerance = .25;
}
