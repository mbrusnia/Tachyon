package org.fhcrc.optides.apps.MSInfusionDataPlot;

import java.util.ArrayList;

import org.fhcrc.optides.Utils.SortedArrayList;
import org.fhcrc.optides.Utils.Utils;

import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;

/*
 * This class handles the major computation on the mass spec xml data.
 * it implements the Comparable interface, so that it can be sorted
 * on mz
 */
public class MS1Spec implements Comparable<MS1Spec> {
	private int index;
	private double maxIntensityMz;
	private double maxIntensity;
	private Spectrum spec;
	private Number[] mzs;
	private Number[] intensities;

	private boolean peakpicked = false;
	private int peaksPicked = 0;
	private Number[] peakMzs;
	private Number[] peakIntensities;
	
	private boolean monoisotopePeakpicked = false;
	private int monoisotopePeaksPicked = 0;
	private String[] monoisotopeZlabels;
	private Number[] monoisotopePeakMzs;
	private Number[] monoisotopePeakIntensities;
	
	private SortedArrayList<Peak> sortedMonoPeaks;


	//set maxIntensity and maxIntensityMz and filter out intensities from 
	//mz = 824.62.  store raw values for mz and intensities.
	public MS1Spec(int i, Spectrum s) {
		this.index = i;
		this.spec = s;

		BinaryDataArray bda1 = s.getBinaryDataArrayList().getBinaryDataArray().get(0);
		BinaryDataArray bda2 = s.getBinaryDataArrayList().getBinaryDataArray().get(1);
		mzs = bda1.getBinaryDataAsNumberArray();
		intensities = bda2.getBinaryDataAsNumberArray();
		
		this.maxIntensity = 0;
		for(int j=0; j < mzs.length; j++){
			double blah = intensities[j].doubleValue();
			if(blah > this.maxIntensity && 
					Math.abs(mzs[j].doubleValue() - Constants.mzToIgnore) >= Constants.mzToIgnoreTolerance){ //824.62
				this.maxIntensity = intensities[j].doubleValue();
				this.maxIntensityMz = mzs[j].doubleValue();
			}else if(Math.abs(mzs[j].doubleValue() - Constants.mzToIgnore) < Constants.mzToIgnoreTolerance)
				intensities[j] = 0.0;
		}
	}
	
	
	/*
	 * Run through the raw data and pick the tallest points for each peak.  Take 
	 * that mz and intensity as the value for the peak, ignore the rest of the peak
	 */
	public void peakPicking() {
		if(!peakpicked){
			peakpicked = true;
			Number[] mz = getRawMzs();
			Number[] intensity = getRawIntensities();
			
			double threshold = maxIntensity * .05;
			ArrayList<Double> newMzs = new ArrayList<Double>();
			ArrayList<Double> newIntensities = new ArrayList<Double>();
			double mzHighestInt = 0;
			double highestInt = 0;
			boolean on = false;
			for(int i = 0; i < mz.length; i++){
				if(intensity[i].doubleValue() > threshold){
					if(!on){
						on = true;
					}
					if(intensity[i].doubleValue() > highestInt){
						mzHighestInt = mz[i].doubleValue();
						highestInt = intensity[i].doubleValue();
					}
				}else if(on){
					on = false;
					newMzs.add(mzHighestInt);
					newIntensities.add(highestInt);
					mzHighestInt = 0;
					highestInt = 0;
				}
			}
			peakMzs = (Number[]) newMzs.toArray(new Number[newMzs.size()]);
			peakIntensities = (Number[]) newIntensities.toArray(new Number[newIntensities.size()]);
			peaksPicked = newMzs.size();
		}
	}

	/*
	 * Starting the the peaks picked in peakPicking(), find which ones represent
	 * the monoisotope for a feature, and assign to its mz the intensity of the
	 * most intense peak for that feature.  keep only the monoisotope mz with 
	 * the highest intensity for the feature.
	 */
	public void monoisotopePeakPicking(){
		if(!monoisotopePeakpicked){
			monoisotopePeakpicked = true;
			monoisotopeZlabels = new String[peaksPicked];
			for(int i = 0; i < peaksPicked; i++)
				monoisotopeZlabels[i] = "?";
			
			//first label all the peaks with their calculated z value
			double a;
			double b;
			double c;
			for(int i = 0; i < peaksPicked - 1; i++){
				a = peakMzs[i].doubleValue();
				b = peakMzs[i+1].doubleValue();
				c = b-a;
				//System.out.println(peakMzs[i] + "\t" + c + "\t" + peakIntensities[i] + "\t" + peakIntensities[i+1]);
				// .01 wiggle room (15 ppm)
				if(Math.abs(c - Constants.z1)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "1";
				}else if(Math.abs(c - Constants.z2)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "2";
				}else if(Math.abs(c - Constants.z3)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "3";
				}else if(Math.abs(c - Constants.z4)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "4";
				}else if(Math.abs(c - Constants.z5)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "5";
				}else if(Math.abs(c - Constants.z6)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "6";
				}else if(Math.abs(c - Constants.z7)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "7";
				}else if(Math.abs(c - Constants.z8)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "8";
				}else if(Math.abs(c - Constants.z9)  <= Constants.mzTolerance){
					monoisotopeZlabels[i] = "9";
				}else if(i > 0){
					//now take a look backwards (we might be at the last peak of a feature)
					a = peakMzs[i-1].doubleValue();
					b = peakMzs[i].doubleValue();
					c = b-a;
					//System.out.println(peakMzs[i] + "\t" + c + "\t" + peakIntensities[i] + "\t" + peakIntensities[i+1]);
					// .01 wiggle room (15 ppm)
					if(Math.abs(c - Constants.z1)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "1";
					}else if(Math.abs(c - Constants.z2)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "2";
					}else if(Math.abs(c - Constants.z3)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "3";
					}else if(Math.abs(c - Constants.z4)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "4";
					}else if(Math.abs(c - Constants.z5)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "5";
					}else if(Math.abs(c - Constants.z6)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "6";
					}else if(Math.abs(c - Constants.z7)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "7";
					}else if(Math.abs(c - Constants.z8)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "8";
					}else if(Math.abs(c - Constants.z9)  <= Constants.mzTolerance){
						monoisotopeZlabels[i] = "9";
					}
				}
			}
		}
		
		//now go through and label the monoisotopic peaks with the intensity of the highest isotope's intensity
		double mz = 0;
		double curInt = 0;
		String z = "";
		
		ArrayList<Double> mpmz = new ArrayList<Double>();
		ArrayList<Double> mpi = new ArrayList<Double>();
		ArrayList<String> mpz = new ArrayList<String>();

		Peak curPeak = new Peak(Integer.parseInt(getScanNumber()), 
				peakMzs[0].doubleValue(), peakIntensities[0].doubleValue(), monoisotopeZlabels[0]);
		sortedMonoPeaks = new SortedArrayList<Peak>();
		for(int i = 1; i < peaksPicked; i++){
			mz = peakMzs[i].doubleValue();
			z = monoisotopeZlabels[i];
			curInt = peakIntensities[i].doubleValue();
			
			if(!curPeak.z.equals(z) || mz - peakMzs[i-1].doubleValue() > 0.51){
				if(curPeak.intensity > Constants.peakPickingIntensityCuttoff * maxIntensity )
					sortedMonoPeaks.insertSorted(curPeak, "asc");
				curPeak = new Peak(Integer.parseInt(getScanNumber()), mz, curInt, z);
				if(i == peaksPicked - 1 && curPeak.intensity > Constants.peakPickingIntensityCuttoff * maxIntensity)
					sortedMonoPeaks.insertSorted(curPeak, "asc");
			}else{
				if(curInt > curPeak.intensity)
					curPeak.intensity = curInt;
			}
		}
		monoisotopePeakIntensities = new Number[sortedMonoPeaks.size()];
		monoisotopePeakMzs = new Number[sortedMonoPeaks.size()];
		monoisotopeZlabels = new String[sortedMonoPeaks.size()];
		monoisotopePeaksPicked =  sortedMonoPeaks.size();
		for(int i = 0; i < sortedMonoPeaks.size(); i++){
			monoisotopePeakIntensities[i] = sortedMonoPeaks.get(i).intensity;
			monoisotopePeakMzs[i] = sortedMonoPeaks.get(i).mz;
			monoisotopeZlabels[i] = sortedMonoPeaks.get(i).z;
		}
	}
	
	
	public void dumpMonoPeaks(SortedArrayList<Peak> monoPeaks) {
		for(int i = 0; i < monoisotopePeaksPicked; i++){
			monoPeaks.insertSorted(new Peak(Integer.parseInt(getScanNumber()), monoisotopePeakMzs[i].doubleValue(), 
					monoisotopePeakIntensities[i].doubleValue(), monoisotopeZlabels[i]), "asc");
		}
	}
	
	public Number[] getRawMzs(){
		return mzs;
	}
	
	public Number[] getRawIntensities(){
		return intensities;
	}
	
	public boolean isPeakPicked(){
		return peakpicked;
	}
	
	public boolean isMonoisotopicPeakPicked(){
		return monoisotopePeakpicked;
	}
	
	public int getMonoisotopicPeaksPicked() {
		return monoisotopePeaksPicked;
	}
	
	public int getPeaksPicked(){
		return peaksPicked;
	}
	public Number[] getPeakMzs(){
		return peakMzs;
	}
	public Number[] getPeakIntensities(){
		return peakIntensities;
	}
	public Object getMsLevel() {
		return spec.getCvParam().get(1).getValue();
	}

	public Spectrum getSpectrum() {
		return spec;
	}


	@Override
	public int compareTo(MS1Spec cs) {
		if(this.maxIntensity == cs.getMaxIntensity())
			return 0;
		else
			return this.maxIntensity < cs.getMaxIntensity() ? 1 : -1;

	}

	public int getIndex() {
		return index;
	}
	public String getScanNumber(){
		return spec.getId().substring(spec.getId().lastIndexOf("=") + 1, spec.getId().length());
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getMaxIntensityMz() {
		return maxIntensityMz;
	}

	public void setMaxIntensityMz(double mz) {
		this.maxIntensityMz = mz;
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public void setMaxIntensity(double intensity) {
		this.maxIntensity = intensity;
	}

	public Number[] getMonoisotopePeakMzs() {
		return monoisotopePeakMzs;
	}

	public Number[] getMonoisotopePeakIntensities() {
		return monoisotopePeakIntensities;
	}
	public String[] getMonoisotopeZlabels() {
		return monoisotopeZlabels;
	}
}
