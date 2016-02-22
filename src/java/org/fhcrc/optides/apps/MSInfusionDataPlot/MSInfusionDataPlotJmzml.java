/**
 * 
 */
package org.fhcrc.optides.apps.MSInfusionDataPlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.expasy.mzjava.core.ms.peaklist.PeakList.Precision;
import org.expasy.mzjava.core.ms.spectrum.MsnSpectrum;
import org.expasy.mzjava.external.io.ms.spectrum.mzml.EbiMzmlReader;
import org.expasy.mzjava.external.io.ms.spectrum.mzml.MzmlConsistencyCheck;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.ChromatogramList;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.SpectrumList;
import uk.ac.ebi.jmzml.xml.Constants;
import uk.ac.ebi.jmzml.xml.io.MzMLObjectIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;



/**
 * @author hramos
 *
 */
public class MSInfusionDataPlotJmzml {

	private String mzmlFilename;
	private int thresholdIntensity;
	EbiMzmlReader mzmlReader;
	
	private double ms2CuttoffThreshold = .10;
	
	public MSInfusionDataPlotJmzml(String filename, int threshold) {
		mzmlFilename = filename;
		thresholdIntensity = threshold;
		
		//create a new unmarshaller object 
		//you can use a URL or a File to initialize the unmarshaller 
		File xmlFile = new File(mzmlFilename); 
		//Corra
		MzMLUnmarshaller unmarshaller = new MzMLUnmarshaller(xmlFile);
		/* unmarshaller.unmarshall();
		mzmlReader = new EbiMzmlReader(unmarshaller, Precision.DOUBLE);
		Set<MzmlConsistencyCheck> css = new HashSet<MzmlConsistencyCheck>();
		css.add(MzmlConsistencyCheck.TOTAL_ION_CURRENT);
		mzmlReader.turnTolerant(css);
		// hasNext() returns true if there is more spectrum to read
		while (mzmlReader.hasNext()) {

		    // next() returns the next spectrum or throws an IOException is something went wrong
		    MsnSpectrum spectrum;
			try {
				spectrum = mzmlReader.next();
			    // do some stuff with your spectrum
			    System.out.println(spectrum.size());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {
			mzmlReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		MzML completeMzML = unmarshaller.unmarshall();
		
		//retrieve the cvList element of the mzML file, given its XPath 
		//CVList cvList = unmarshaller.unmarshalFromXpath("/cvList", CVList.class); 
		//the object is now fully populated with the data from the XML file 
		//System.out.println("Number of defined CVs in this mzML: " + cvList.getCount()); 
		//retrieve the fileDescription element 
		/*ChromatogramList cl = completeMzML.getRun().getChromatogramList();//unmarshaller.unmarshalFromXpath("/mzML/run/chromatogramList", ChromatogramList.class); 
		Chromatogram c = cl.getChromatogram().get(0); 
		BinaryDataArrayList bdal = c.getBinaryDataArrayList();
		BinaryDataArray bda = bdal.getBinaryDataArray().get(0);
		BinaryDataArray bda2 = bdal.getBinaryDataArray().get(1);
		Number[] time = bda.getBinaryDataAsNumberArray();
		Number[] intensity = bda2.getBinaryDataAsNumberArray();
		*/
		SpectrumList sl = completeMzML.getRun().getSpectrumList();
		List<Spectrum> spec = sl.getSpectrum();
		SortedArrayList<MS1Scan> sortedMS1s = new SortedArrayList<MS1Scan>();
		double maxIntensity = 0;
		double intensity = 0;
		//get maxIntensity
		for(int i =0; i < sl.getCount(); i++){
			Spectrum s = spec.get(i);

			//if MS1
			if(s.getCvParam().get(1).getValue().equals("1")){
				intensity = Double.parseDouble(s.getCvParam().get(6).getValue());
				if(intensity>maxIntensity)
					maxIntensity = intensity;
				//MS1Scan scan = new MS1Scan(i, time[i], intensity[i]);
				//sortedMS1s.insertSorted(scan);
				//System.out.println(maxIntensity);
			}
		}
		
		//now, for those MS1s that lie within the threshold cutoff, 
		//get the ms1s and average them together, with a 10% cuttoff threshold
		for(int i =0; i < sl.getCount(); i++){
			Spectrum s = spec.get(i);

			//if MS1
			if(s.getCvParam().get(1).getValue().equals("1")){
				intensity = Double.parseDouble(s.getCvParam().get(6).getValue());
				if(intensity>maxIntensity * ((double)thresholdIntensity/100)){
					//MS1Scan scan = new MS1Scan(i, time[i], intensity[i]);
					//sortedMS1s.insertSorted(scan);
					System.out.println(intensity);
				}
			}
		}
		//print
		/*double maxIntensity = sortedMS1s.get(0).getIntensity();
		double lowTime = 1000;
		double highTime = 0;
		int i;
		for(i = 0; i < sortedMS1s.size(); i++){
			MS1Scan scan = sortedMS1s.get(i);
			if(scan.getIntensity() < maxIntensity * ((double)thresholdIntensity/100))
				break;

			//System.out.println(i + " - " + scan.getIndex() + " - " + scan.getTime() + " - " + scan.getIntensity());
			if(scan.getTime() < lowTime)
				lowTime = scan.getTime();
			if(scan.getTime() > highTime)
				highTime = scan.getTime();
			
		}
		System.out.println("number of spectra passing threshold: " + i);
		System.out.println("MS1 filtered range: " + lowTime + " - " + highTime);
		*/
		//now get the ms1s and average them together, with a 10% cuttoff threshold
		/*SpectrumList sl1 = completeMzML.getRun().getSpectrumList();
		List<Spectrum> spec1 = sl1.getSpectrum();
		ArrayList<SortedArrayList<MS2Scan>> sortedMS2s  = new ArrayList<SortedArrayList<MS2Scan>>();
		for(int j=0; j <  i ; j++){
			Spectrum ms2 = spec1.get(sortedMS1s.get(j).getIndex());
			BinaryDataArray mz = ms2.getBinaryDataArrayList().getBinaryDataArray().get(0);
			BinaryDataArray intensityMs2 = ms2.getBinaryDataArrayList().getBinaryDataArray().get(1);
			Number[] mzVals = mz.getBinaryDataAsNumberArray();
			Number[] intensityVals = intensityMs2.getBinaryDataAsNumberArray();
			SortedArrayList<MS2Scan> ms2SAR = new SortedArrayList<MS2Scan>();
			for(int k=0; k < mzVals.length; k++){
				double mzVal = mzVals[k].doubleValue();
				double intensityVal = intensityVals[k].doubleValue();
				if(intensityVal > 10000){
					MS2Scan scan = new MS2Scan(k, mzVal, intensityVal);
					ms2SAR.insertSorted(scan);
				}
			}
			sortedMS2s.add(j, ms2SAR);
		}
		for(int j =0; j < i; j++){
			System.out.println(sortedMS1s.get(j).getIndex()+1 + " - " + sortedMS2s.get(j).get(0).getMz() + " - " + sortedMS2s.get(j).get(0).getIntensity());
			drawSpectrumAsJPG(sortedMS2s.get(j), .05, mzmlFilename.substring(0, mzmlFilename.lastIndexOf("/")+1) + "spec-" + j + ".jpg");
		}
		*/
		//System.out.println("Number of defined Spectrums in this mzML: " + spec1.size());
		
		//supported XPath for indexed and non-indexed mzML 
		//System.out.println("Supported XPath:" + Constants.XML_INDEXED_XPATHS);
		
		//number of spectrum elements in the XML file 
		//System.out.println("Number of spectrum elements: " + unmarshaller.getObjectCountForXpath("/run/spectrumList/spectrum"));
		
		//retrieve the cvList element of the mzML file, given its XPath 
		//SpectrumList spectrumList = unmarshaller.unmarshalFromXpath("/run/spectrumList", SpectrumList.class); 
				//the object is now fully populated with the data from the XML file 
				
				//dealing with element collections 
				/*MzMLObjectIterator<Spectrum> spectrumIterator = unmarshaller.unmarshalCollectionFromXpath("/run/spectrumList/spectrum", Spectrum.class); 
				while (spectrumIterator.hasNext()){ 
					//read next spectrum from XML file 
					Spectrum spectrum = spectrumIterator.next(); 
					//use it 
					//System.out.println("Spectrum ID: " + spectrum.getId()); 
				}*/
	}

	private void drawSpectrumAsJPG(SortedArrayList<MS2Scan> spec, double intensityThreshold, String outputFilename) {
		// Create a single plot containing both the scatter and line
		XYPlot plot = new XYPlot();

		/* SETUP SCATTER */

		// Create the scatter data, renderer, and axis
		XYDataset collection1 = getScatterPlotData(spec, intensityThreshold);
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(false, true);   // Shapes only
		ValueAxis domain1 = new NumberAxis("m/z");
		ValueAxis range1 = new NumberAxis("Intensity");

		// Set the scatter data, renderer, and axis into plot
		plot.setDataset(0, collection1);
		plot.setRenderer(0, renderer1);
		plot.setDomainAxis(0, domain1);
		plot.setRangeAxis(0, range1);

		// Map the scatter to the first Domain and first Range
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);

		/* SETUP LINE 

		// Create the line data, renderer, and axis
		XYDataset collection2 = getLinePlotData();
		XYItemRenderer renderer2 = new XYLineAndShapeRenderer(true, false);   // Lines only
		ValueAxis domain2 = new NumberAxis("Domain2");
		ValueAxis range2 = new NumberAxis("Range2");

		// Set the line data, renderer, and axis into plot
		plot.setDataset(1, collection2);
		plot.setRenderer(1, renderer2);
		plot.setDomainAxis(1, domain2);
		plot.setRangeAxis(1, range2);

		// Map the line to the second Domain and second Range
		plot.mapDatasetToDomainAxis(1, 1);
		plot.mapDatasetToRangeAxis(1, 1);
*/
		// Create the chart with the plot and a legend
		JFreeChart chart = new JFreeChart("Multi Dataset Chart", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		

		int chartWidth = 560;
		int chartHeight = 300;
		
		try {
			ChartUtilities.saveChartAsJPEG(new File(outputFilename), chart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private XYDataset getScatterPlotData(SortedArrayList<MS2Scan> spec, double intensityThreshold) {
		final XYSeries series1 = new XYSeries("First");
        for(int i = 0; i < spec.size(); i++){
        	if(spec.get(i).getIntensity() < spec.get(0).getIntensity() * intensityThreshold)
        		break;
        	
        	series1.add(spec.get(i).getMz(), spec.get(i).getIntensity());
        }

        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
                
        return dataset;
	}

	/**
	 * @param args = filename, 
	 */
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("USAGE: MSInfusionDataPlotJmzml pathToMzmlFile thresholdIntensity");
			
		} 
		MSInfusionDataPlotJmzml msip = new MSInfusionDataPlotJmzml(args[0], Integer.parseInt(args[1]));

	}
	
	private class SortedArrayList<T> extends ArrayList<T> {

	    @SuppressWarnings("unchecked")
	    public void insertSorted(T value) {
	        add(value);
	        Comparable<T> cmp = (Comparable<T>) value;
	        for (int i = size()-1; i > 0 && cmp.compareTo(get(i-1)) < 0; i--)
	            Collections.swap(this, i, i-1);
	    }
	}
	@SuppressWarnings("rawtypes")
	private class MS1Scan implements Comparable<MS1Scan>{
		private int index;
		private double time;
		private double intensity;


		public MS1Scan(int i, Number number, Number number2) {
			this.index = i;
			this.time = number.doubleValue();
			this.intensity = number2.doubleValue();
		}

		@Override
		public int compareTo(MS1Scan cs) {
			if(this.intensity == cs.getIntensity())
				return 0;
			else
				return this.intensity < cs.getIntensity() ? 1 : -1;

		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public double getTime() {
			return time;
		}

		public void setTime(double time) {
			this.time = time;
		}

		public double getIntensity() {
			return intensity;
		}

		public void setIntensity(double intensity) {
			this.intensity = intensity;
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private class MS2Scan implements Comparable<MS2Scan>{
		private int index;
		private double mz;
		private double intensity;


		public MS2Scan(int i, Number number, Number number2) {
			this.index = i;
			this.mz = number.doubleValue();
			this.intensity = number2.doubleValue();
		}

		@Override
		public int compareTo(MS2Scan cs) {
			if(this.intensity == cs.getIntensity())
				return 0;
			else
				return this.intensity < cs.getIntensity() ? 1 : -1;

		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public double getMz() {
			return mz;
		}

		public void setMz(double mz) {
			this.mz = mz;
		}

		public double getIntensity() {
			return intensity;
		}

		public void setIntensity(double intensity) {
			this.intensity = intensity;
		}
		
	}

}
