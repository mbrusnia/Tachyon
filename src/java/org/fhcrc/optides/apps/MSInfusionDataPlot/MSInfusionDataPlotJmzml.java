/**
 * 
 */
package org.fhcrc.optides.apps.MSInfusionDataPlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fhcrc.optides.Utils.SortedArrayList;
import org.fhcrc.optides.Utils.Utils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.SpectrumList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;



/**
 * @author hramos
 *
 */
public class MSInfusionDataPlotJmzml {

	private String mzmlFilename;
	private double maxMS1Intensity;
	private double thresholdIntensity;
	private MzML completeMzML;
	private SortedArrayList<MS1Spec> sortedMS1s;
	private int ms1sAboveThresholdCount = 0;
	
	private SortedArrayList<Peak> monoPeaks =  new SortedArrayList<Peak>();
	
	private Number[] finalMzs;
	private Number[] finalIntensities;
	private String[] zLabels;

	/**
	 * @param args = filename, intensityThresholdPercentage
	 */
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("USAGE: MSInfusionDataPlotJmzml pathToMzmlFile thresholdIntensity");
			return;
		} 
		MSInfusionDataPlotJmzml msip = new MSInfusionDataPlotJmzml(args[0], Integer.parseInt(args[1]));
		msip.pickMonoisotopicPeaks();
		msip.averageOutResults();
		
		String chartTitle = msip.mzmlFilename.substring(msip.mzmlFilename.lastIndexOf("/") +1, msip.mzmlFilename.lastIndexOf("."));
		chartTitle = chartTitle + ": Monoisotopic Peaks Averaged Out for All Spectra Over " + args[1] + " Percent of the Highest Recorded Intensity";
		msip.saveResultsAsJPG(chartTitle);
	}

	/*
	 * Constructor - set filename, read and parse xml, find max intensity among
	 * all the spectra, sort the spectra by max intensity
	 */
	public MSInfusionDataPlotJmzml(String filename, int thresholdInt) {
		mzmlFilename = filename;
		
		//create a new unmarshaller object 
		//you can use a URL or a File to initialize the unmarshaller 
		File xmlFile = new File(mzmlFilename); 

		MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(xmlFile);
		completeMzML = unmarshaller.unmarshall();

		SpectrumList sl = completeMzML.getRun().getSpectrumList();
		List<Spectrum> spec = sl.getSpectrum();
		sortedMS1s = new SortedArrayList<MS1Spec>();
		for(int i =0; i < sl.getCount(); i++){
			Spectrum s = spec.get(i);

			//if MS1
			if(s.getCvParam().get(1).getValue().equals("1")){
				sortedMS1s.insertSorted(new MS1Spec(i, s), "desc");
			}
		}
		maxMS1Intensity = sortedMS1s.get(0).getMaxIntensity();
		thresholdIntensity = maxMS1Intensity * ((double)thresholdInt/100);
	}
	
	
	public void pickMonoisotopicPeaks(){
		//now, for those MS1s that lie within the threshold cutoff, 
		//get the ms1s and average them together, with a 10% cuttoff threshold
		for(int i =0; i < sortedMS1s.size(); i++){
			MS1Spec ms1 = sortedMS1s.get(i);

			//if MS1 and passes maxIntensity threshold
			if(ms1.getMaxIntensity() > this.thresholdIntensity){
				//saveResultsAsJPG(ms1);
				ms1.peakPicking();
				//saveResultsAsJPG(ms1);
				ms1.monoisotopePeakPicking();
				//saveResultsAsJPG(ms1);
				ms1.dumpMonoPeaks(monoPeaks);
				ms1sAboveThresholdCount++;
			}
		}
	}
	
	/*
	 * go through all found monoPeaks from all spectra and add them together and average them out and
	 * filter based on if the average passes the maxIntensity*.10 threshold
	 */
	public void averageOutResults(){
		SortedArrayList<Peak> list = new SortedArrayList<Peak>();
		Peak curPeak = monoPeaks.get(0);
		//TODO
		for(int i = 1; i<monoPeaks.size(); i++){
			Peak mp = monoPeaks.get(i);

			//if it could be considered the same peak, add the intensity
			if(Math.abs(curPeak.mz - mp.mz) <= Constants.mzTolerance && curPeak.z.equals(mp.z)||
					(Math.abs(curPeak.mz - mp.mz - Constants.z2) <= Constants.mzTolerance && curPeak.z.equals("2") && mp.z.equals("2")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z3) <= Constants.mzTolerance && curPeak.z.equals("3") && mp.z.equals("3")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z4) <= Constants.mzTolerance && curPeak.z.equals("4") && mp.z.equals("4")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z5) <= Constants.mzTolerance && curPeak.z.equals("5") && mp.z.equals("5")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z6) <= Constants.mzTolerance && curPeak.z.equals("6") && mp.z.equals("6")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z7) <= Constants.mzTolerance && curPeak.z.equals("7") && mp.z.equals("7")) ||
					(Math.abs(curPeak.mz - mp.mz - Constants.z8) <= Constants.mzTolerance && curPeak.z.equals("8") && mp.z.equals("8"))){
				curPeak.intensity += mp.intensity;
			}else{  //do we keep the peak?
				curPeak.intensity = curPeak.intensity / ms1sAboveThresholdCount;
				if(curPeak.intensity > maxMS1Intensity * Constants.finalIntensityCuttoff)
					list.insertSorted(curPeak, "asc");
				curPeak = new Peak(mp.scanNum, mp.mz, mp.intensity, mp.z);
			}
		}
		System.out.println("initial monopeaks size: " + monoPeaks.size() + "\tfinal monoPeak list size: " + list.size());
		finalMzs = new Number[list.size()];
		finalIntensities = new Number[list.size()];
		zLabels = new String[list.size()];
		for(int i = 0; i < list.size(); i++){
			Peak p = list.get(i);
			finalMzs[i] = p.mz;
			finalIntensities[i] = p.intensity;
			zLabels[i] = p.z;
			//System.out.println(list.get(i).mz + "\t" + 
			//		list.get(i).intensity + "\t" + list.get(i).z + "\t" + list.get(i).scanNum);
		}
	}
	
	private void saveResultsAsJPG(String chartTitle) {
		double maxInt = 0;
		for(int i = 0; i < finalIntensities.length; i++)
			if(finalIntensities[i].doubleValue() > maxInt)
				maxInt = finalIntensities[i].doubleValue();
		drawSpectrumAsJPG(chartTitle, finalMzs, finalIntensities, zLabels, 300.0, 1800.0, 1000, 420, 
				maxInt*Constants.finalIntensityCuttoff, maxInt, 
				mzmlFilename.substring(0, mzmlFilename.lastIndexOf(".")) + ".jpg");
	}
	

	//used mainly for debugging
	private void saveResultsAsJPG(MS1Spec ms) {
		double maxInt = 0;
		Number[] intensities = null;
		Number[] mzs = null;
		String[] zLabels = null;
		String suffix = "a";
		String typeOfChart = "Raw";
		
		if(ms.isMonoisotopicPeakPicked()){
			mzs = ms.getMonoisotopePeakMzs();
			intensities = ms.getMonoisotopePeakIntensities();
			zLabels = ms.getMonoisotopeZlabels();
			suffix = "c";
			typeOfChart = "Monoisotopic Peaks";
		}else if(ms.isPeakPicked()){
			mzs = ms.getPeakMzs();
			intensities = ms.getPeakIntensities();
			zLabels = new String[mzs.length];
			suffix = "b";
			typeOfChart = "Picked Peaks";
		}else{
			mzs = ms.getRawMzs();
			intensities = ms.getRawIntensities();
			zLabels = new String[mzs.length];
		}
		for(int i = 0; i < intensities.length; i++)
			if(intensities[i].doubleValue() > maxInt)
				maxInt = intensities[i].doubleValue();
		drawSpectrumAsJPG("Spec " + ms.getScanNumber() + " - " + typeOfChart, mzs, intensities, zLabels, 300.0, 1800.0, 1000, 420, maxInt * Constants.finalIntensityCuttoff, maxInt, 
				mzmlFilename.substring(0, mzmlFilename.lastIndexOf("/")+1) + "scan" + ms.getScanNumber() + suffix + ".jpg");
	}
	
	
	private void drawSpectrumAsJPG(String chartTitle, Number[] mzs, Number[] intensities, String[] zLabels, double lowerMz, double higherMz, int width, int height, 
								double intensityThreshold, double maxIntensity, String outputFilename) {
		LabeledXYDataset ds = new LabeledXYDataset();
        for(int i = 0; i < mzs.length; i++){
        	if(mzs[i].doubleValue() > higherMz)
        		break;
        	if(mzs[i].doubleValue() > lowerMz && intensities[i].doubleValue() > intensityThreshold){
    			ds.add(mzs[i].doubleValue() - 0.0001, 0.0, "");
        		ds.add(mzs[i].doubleValue(), intensities[i].doubleValue(), Double.toString(Utils.round(mzs[i].doubleValue(), 4)) + " (" + zLabels[i] + ")");
    			ds.add(mzs[i].doubleValue() + 0.0001, 0.0, "");
        	}
        }
        
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);  
		renderer1.setBaseItemLabelGenerator(new LabelGenerator());
	    renderer1.setBaseItemLabelsVisible(true);
	    
		
		ValueAxis domain1 = new NumberAxis("m/z");
		domain1.setRange(lowerMz, higherMz);
		ValueAxis range1 = new NumberAxis("Intensity");
		range1.setRange(0, maxIntensity*1.061);

		XYPlot plot = new XYPlot();
		// Set the scatter data, renderer, and axis into plot
		plot.setDataset(0, ds);
		plot.setRenderer(0, renderer1);
		plot.setDomainAxis(0, domain1);
		plot.setRangeAxis(0, range1);

		// Map the scatter to the first Domain and first Range
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);

		// Create the chart with the plot and a legend
		JFreeChart chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
			
		try {
			ChartUtilities.saveChartAsJPEG(new File(outputFilename), chart, width, height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Necessary for adding labels to the chart.
	 */
	private static class LabeledXYDataset extends AbstractXYDataset {
        private List<Number> x = new ArrayList<Number>();
        private List<Number> y = new ArrayList<Number>();
        private List<String> label = new ArrayList<String>();


		public void add(double x, double y, String label){
            this.x.add(x);
            this.y.add(y);
            this.label.add(label);
        }

        public String getLabel(int series, int item) {
            return label.get(item);
        }

        @Override
        public int getSeriesCount() {
            return 1;
        }

        @Override
        public Comparable getSeriesKey(int series) {
            return "Intensity";
        }

        @Override
        public int getItemCount(int series) {
            return label.size();
        }

        @Override
        public Number getX(int series, int item) {
            return x.get(item);
        }

        @Override
        public Number getY(int series, int item) {
            return y.get(item);
        }
    }

	/*
	 * Necessary for adding labels to the chart.
	 */
    private static class LabelGenerator implements XYItemLabelGenerator {
	    @Override
	    public String generateLabel(XYDataset dataset, int series, int item) {
	            LabeledXYDataset labelSource = (LabeledXYDataset) dataset;
	            return labelSource.getLabel(series, item);
	    }
    }
}
