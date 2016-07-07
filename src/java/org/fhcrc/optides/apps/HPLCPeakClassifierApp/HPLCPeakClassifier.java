package org.fhcrc.optides.apps.HPLCPeakClassifierApp;

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
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class HPLCPeakClassifier {
	//Signal-to-noise ratio for cuttoff in peak picking
	private double sn_ratio;
	
	//parameter for labeling output jpg - number of peaks to use for file classification
	private int classification = 0;
	
	//paths to inputs and outputs:
	private String blankRCsvFilepath = "";
	private String blankNRCsvFilepath = "";
	private String nrCsvFilepath = "";
	private String rCsvFilepath = "";
	private String sampleInfoXmlFile = "";
	private String outDir = "";
	
	//xml document for Sample Info
	private Document sampleInfoXmlDoc = null;
	
	//Sample Info
	private String sampleName = null;
	
	//peak lists stored in a HashMap (key = filename)
	HashMap<String, ArrayList<HPLCPeak>> peakLists = null;	
	
	//peak picked lists
	ArrayList<HPLCPeak> rpp = null;
	ArrayList<HPLCPeak> nrpp = null;
	
	
	public HPLCPeakClassifier(double sn_ratio, int classification, String blankRCsv, String blankNRCsv, String nrCsv, String rCsv, String sampleInfoXmlFile, String outDir) throws ParserConfigurationException, SAXException, IOException {
		this.sn_ratio = sn_ratio;
		this.classification = classification;
		this.blankRCsvFilepath = blankRCsv;
		this.blankNRCsvFilepath = blankNRCsv;
		this.nrCsvFilepath = nrCsv;
		this.rCsvFilepath = rCsv;
		this.sampleInfoXmlFile = sampleInfoXmlFile;
		this.outDir = outDir;
		
		//initiate peakLists HashMap:
		peakLists = new HashMap<String, ArrayList<HPLCPeak>>();
		peakLists.put(blankRCsvFilepath, new ArrayList<HPLCPeak>());
		peakLists.put(blankNRCsvFilepath, new ArrayList<HPLCPeak>());
		peakLists.put(nrCsvFilepath, new ArrayList<HPLCPeak>());
		peakLists.put(rCsvFilepath, new ArrayList<HPLCPeak>());
		
		//parse the Sample Info XML
		File inputFile = new File(sampleInfoXmlFile);
		DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		sampleInfoXmlDoc = builder.parse(inputFile);
	}

	public static void main(String[] args) {
		String blankRCsv = "";
		String blankNRCsv = "";
		String nrCsv = "";
		String rCsv = "";
		String sampleInfoXmlFile = "";
		String outDir = "";
		double sn_ratio = .10; 
		int classification = 0; 
		
		//get input params
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--BLANK_R"))
				blankRCsv = curParam[1];
			else if(curParam[0].equals("--BLANK_NR"))
				blankNRCsv = curParam[1];
			else if(curParam[0].equals("--NR"))
				nrCsv = curParam[1];
			else if(curParam[0].equals("--R"))
				rCsv = curParam[1];
			else if(curParam[0].equals("--sampleInfo"))
				sampleInfoXmlFile = curParam[1];
			else if(curParam[0].equals("--SN"))
				sn_ratio = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--Classification"))
				classification = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--outdir"))
				outDir = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		/*System.out.println("USAGE: HPLCPeakClassifier --NR=pathToNRcsvFile --R=pathToRcsvFile "
				+ "--BLANK_NR=pathToBlankNRCsvFile --BLANK_R=pathToBlankRCsvFile "
				+ "--sampleInfo=pathToSampleInfoXmlFile --outdir=pathToOutputDir "
				+ "--SN=sn_ratio_decimal --Classification=NumOfPeaksForClassification");*/
		
		if(blankRCsv == "" || blankNRCsv == "" || nrCsv == "" || rCsv == "" || 
				sampleInfoXmlFile == "" || outDir == "" || classification == 0){
			System.out.println("A command line parameter is missing or incorrect.  Please look over your entered parameters: ");

			System.out.println("--NR: " + nrCsv);
			System.out.println("--R: " + rCsv);
			System.out.println("--BLANK_NR: " + blankNRCsv);
			System.out.println("--BLANK_R: " + blankRCsv);
			System.out.println("--sampleInfo: " + sampleInfoXmlFile);
			System.out.println("--outdir: " + outDir);
			System.out.println("--SN: " + sn_ratio + "   (default: 0.10)"); 
			System.out.println("--Classification: " + classification); 
			System.out.println(""); 
			printUsage();
			return;
		}
		if(blankRCsv == blankNRCsv){
			System.out.println("BLANK_R and BLANK_NR files cannot be set to the same value!");
			return;
		}
		
		try {
			HPLCPeakClassifier hpc = new HPLCPeakClassifier(sn_ratio, classification, blankRCsv, blankNRCsv, nrCsv, rCsv, sampleInfoXmlFile, outDir);
			String sampleName = hpc.getSampleName();
			hpc.loadLCAUdata();
			hpc.initialRtAlignmentCheck();
			hpc.trimHPLCPeakListsByRT(1.0, 14.0);
			hpc.subtractBackgroundAUsfromR();
			//hpc.printPeaks("R");
			hpc.subtractBackgroundAUsfromNR();
			
			hpc.peakPickingR(sn_ratio, 2.0, 14.0);
			hpc.peakPickingNR(sn_ratio, 2.0, 14.0);
			
			int peaks = hpc.getMaxNumOfPeaksPicked();
			String str = sampleName.split("\\_")[0];
			String chartName = str;
			String fileName = str;
			if(peaks == 1){
				chartName += " [Perfect]";
				fileName += "_Perfect";
			}else if (peaks > 1 && peaks <= classification){
				chartName += " [Simple]";
				fileName += "_Simple";
			}else if(peaks > classification){
					chartName += " [Complex]";
					fileName += "_Complex";
			}
			fileName += ".jpg";
			System.out.println("max peaks found: " + peaks);
			System.out.println("Chart Title: " + chartName);
			System.out.println("Filename: " + fileName);
			
			hpc.drawHPLCsAsJPG(chartName, outDir + fileName.replace(" ",  ""), 800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * after peak picking, this func. tells us what the max num of peaks between r vs. nr were
	 */
	private int getMaxNumOfPeaksPicked() {
		int peaks = rpp.size() / 3;
		if(nrpp.size() / 3 > peaks)
			peaks = nrpp.size();
		return peaks;
	}

	public void peakPickingR(double sn_ratio, double lowerRT, double upperRT) {
		ArrayList<HPLCPeak> pl = peakLists.get(rCsvFilepath);
		rpp = new ArrayList<HPLCPeak>();
		
		peakPicking(pl, rpp, sn_ratio, lowerRT, upperRT);
	}
	
	public void peakPickingNR(double sn_ratio, double lowerRT, double upperRT) {
		ArrayList<HPLCPeak> pl = peakLists.get(nrCsvFilepath);
		nrpp = new ArrayList<HPLCPeak>();
		
		peakPicking(pl, nrpp, sn_ratio, lowerRT, upperRT);
	}
	
	private void peakPicking(ArrayList<HPLCPeak> from, ArrayList<HPLCPeak> to, 
			double sn_ratio, double lowerRT, double upperRT){
		//find max AU value
		double max = 0;
		for(int i=0; i < from.size(); i++){
			if(from.get(i).getRt() >= lowerRT && from.get(i).getRt() <= upperRT 
					&& from.get(i).getAu() > max)
				max = from.get(i).getAu();
		}
		
		//find the peaks of the ones that pass  the s/n ratio
		HPLCPeak local_max = new HPLCPeak(0,0);
		double prev_au = 0;
		String direction = "down";
		for(int i=0; i < from.size(); i++){
			if(from.get(i).getRt() >= lowerRT 
					&& from.get(i).getRt() <= upperRT 
					&& from.get(i).getAu() >= max*sn_ratio){
				if(direction.equals("down") && from.get(i).getAu() > prev_au +.1){
					direction = "up";
				}
				if(direction.equals("up")){
					if(from.get(i).getAu() > local_max.getAu()){
						local_max = from.get(i);
					}else {
						to.add(new HPLCPeak(local_max.getRt() - .001, 0));
						to.add(local_max);
						to.add(new HPLCPeak(local_max.getRt() + .001, 0));
						local_max = new HPLCPeak(0,0);
						direction = "down";						
					}
				}
				prev_au = from.get(i).getAu();
			}
		}
	}

	/*
	 * using JFreeChart, draw the jpg of the LC runs
	 */
	private void drawHPLCsAsJPG(String chartTitle, String outputFilename, int width, int height) {
		XYDataset ds = createDataset(peakLists.get(rCsvFilepath), peakLists.get(nrCsvFilepath));
		//ds = createDataset(rpp, nrpp);
		
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);  
		renderer1.setBaseItemLabelsVisible(true);

		ValueAxis domain1 = new NumberAxis("Retention Time (min)");
		//domain1.setRange(lowerMz, higherMz);
		domain1.setRange(0, 15);
		ValueAxis range1 = new NumberAxis("mAU (214nm wavelength)");
		range1.setRange(0, 500);
		//range1.setRange(0, 100);

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
	 * create the dataset for jFreeChart to draw the line chart
	 */
	private XYDataset createDataset(ArrayList<HPLCPeak> rHplcPeaks, ArrayList<HPLCPeak> nrHplcPeaks) {
		XYSeries series1 = new XYSeries("R");
		XYSeries series2 = new XYSeries("NR");
		for(int i=0; i < rHplcPeaks.size(); i++)
			series1.add(rHplcPeaks.get(i).getRt(), rHplcPeaks.get(i).getAu());
		
		for(int i=0; i < nrHplcPeaks.size(); i++)
			series2.add(nrHplcPeaks.get(i).getRt(), nrHplcPeaks.get(i).getAu());

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
		return dataset;
	}

	private void printPeaks(String which) {
		ArrayList<HPLCPeak> peaks = null;
		switch(which){
		case "R":
			peaks = peakLists.get(rCsvFilepath);
			break;
		case "BLANK_R":
			peaks = peakLists.get(blankRCsvFilepath);
			break;
		case "BLANK_NR":
			peaks = peakLists.get(blankNRCsvFilepath);
			break;
		case "NR":
			peaks = peakLists.get(nrCsvFilepath);
			break;
		}
		for(int i=0; i < peaks.size(); i++)
			System.out.println(peaks.get(i).getRt() + "\t" + peaks.get(i).getAu());
		
	}

	private void subtractBackgroundAUsfromR() {
		subtractAUs(peakLists.get(rCsvFilepath), peakLists.get(blankRCsvFilepath)); 
	}
	private void subtractBackgroundAUsfromNR() {
		subtractAUs(peakLists.get(nrCsvFilepath), peakLists.get(blankNRCsvFilepath)); 
	}

	private void subtractAUs(ArrayList<HPLCPeak> peaks1, ArrayList<HPLCPeak> peaks2) {
		assert(peaks1.size() == peaks2.size());

		HPLCPeak p1 = null;
		HPLCPeak p2 = null;
		for(int i=0; i < peaks1.size(); i++){
			p1 = peaks1.get(i);
			p2 = peaks2.get(i);
			p1.setAu(p1.getAu() - p2.getAu());
		}
	}

	private void trimHPLCPeakListsByRT(double lowerRtLimit, double upperRTLimit) {
		ArrayList<HPLCPeak> newBlankRPeakList = new ArrayList<HPLCPeak>();
		ArrayList<HPLCPeak> newBlankNRPeakList = new ArrayList<HPLCPeak>();
		ArrayList<HPLCPeak> newNRPeakList = new ArrayList<HPLCPeak>();
		ArrayList<HPLCPeak> newRPeakList = new ArrayList<HPLCPeak>();
		
		ArrayList<HPLCPeak> refPeakList = peakLists.get(blankRCsvFilepath);
		for(int i=0; i < refPeakList.size(); i++){
			if(refPeakList.get(i).getRt() >= lowerRtLimit 
					&& refPeakList.get(i).getRt() <= upperRTLimit){
				newRPeakList.add(peakLists.get(rCsvFilepath).get(i));
				newNRPeakList.add(peakLists.get(nrCsvFilepath).get(i));
				newBlankRPeakList.add(peakLists.get(blankRCsvFilepath).get(i));
				newBlankNRPeakList.add(peakLists.get(blankNRCsvFilepath).get(i));
			}
		}
		peakLists.put(rCsvFilepath, newRPeakList);
		peakLists.put(nrCsvFilepath, newNRPeakList);
		peakLists.put(blankRCsvFilepath, newBlankRPeakList);
		peakLists.put(blankNRCsvFilepath, newBlankNRPeakList);
	}

	private void initialRtAlignmentCheck() throws Exception {
		HPLCPeak blankRHplcInitialPeak = peakLists.get(blankRCsvFilepath).get(0);
		HPLCPeak blankNRHplcInitialPeak = peakLists.get(blankNRCsvFilepath).get(0);
		HPLCPeak rHplcInitialPeak = peakLists.get(rCsvFilepath).get(0);
		HPLCPeak nrHplcInitialPeak = peakLists.get(nrCsvFilepath).get(0);

		double one = Math.abs(blankRHplcInitialPeak.getRt() - blankNRHplcInitialPeak.getRt());
		double two = Math.abs(blankRHplcInitialPeak.getRt() - rHplcInitialPeak.getRt());
		double three = Math.abs(blankRHplcInitialPeak.getRt() - nrHplcInitialPeak.getRt());
		double four = Math.abs(blankNRHplcInitialPeak.getRt() - rHplcInitialPeak.getRt());
		double five = Math.abs(blankNRHplcInitialPeak.getRt() - nrHplcInitialPeak.getRt());
		double six = Math.abs(rHplcInitialPeak.getRt() - nrHplcInitialPeak.getRt());
		
		assert(	one < .01 && two < .01 &&	three < .01 &&	four < .01 &&	five < .01 &&	six < .01);
	}

	protected void loadLCAUdata() throws IOException {
		for (HashMap.Entry<String, ArrayList<HPLCPeak>> entry : peakLists.entrySet()) {
			 
			// FileReader reads text files in the default encoding.
	        FileReader fileReader = new FileReader(entry.getKey());
	
	        // Always wrap FileReader in BufferedReader.
	        BufferedReader bufferedReader = new BufferedReader(fileReader);
	
	        String line = null;
	        String[] rt_au = null;
	        while((line = bufferedReader.readLine()) != null) {
	        	if(line.contains(",")){
	        		rt_au = line.split("\\w?,\\w?");
		        	entry.getValue().add(new HPLCPeak(Double.parseDouble(rt_au[0]), Double.parseDouble(rt_au[1])));
	        	}
	        }   
	
	        // Always close files.
	        bufferedReader.close(); 
		}
	}

	private String getSampleName() throws Exception {
		NodeList nl = null;
		if(sampleName == null){
			nl = sampleInfoXmlDoc.getElementsByTagName("Name");
			if(nl.getLength() != 1)
				throw new Exception("More or less than one Sample Name was given in the Sample Info Xml file.  Please correct this and try again.");
			sampleName = nl.item(0).getTextContent();
		}
		return sampleName;
	}

	private static void printUsage() {
		System.out.println("USAGE: HPLCPeakClassifier --NR=pathToNRcsvFile --R=pathToRcsvFile --BLANK_NR=pathToBlankNRCsvFile --BLANK_R=pathToBlankRCsvFile --sampleInfo=pathToSampleInfoXmlFile --outdir=pathToOutputDir --SN=sn_ratio_decimal --Classification=NumOfPeaksForClassification");
	}

}
