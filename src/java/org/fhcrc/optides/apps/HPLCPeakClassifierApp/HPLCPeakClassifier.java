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


public class HPLCPeakClassifier {
	//paths to inputs and outputs:
	private String blankCsvFilepath = "";
	private String nrCsvFilepath = "";
	private String rCsvFilepath = "";
	private String sampleInfoXmlFile = "";
	private String outDir = "";
	
	//xml document for Sample Info
	private Document sampleInfoXmlDoc = null;
	
	//Sample Info
	private String sampleName = null;
	
	//peak lists
	ArrayList<HPLCPeak> blankHplcPeaks = null;
	ArrayList<HPLCPeak> nrHplcPeaks = null;
	ArrayList<HPLCPeak> rHplcPeaks = null;

	
	
	public HPLCPeakClassifier(String blankCsv, String nrCsv, String rCsv, String sampleInfoXmlFile, String outDir) throws ParserConfigurationException, SAXException, IOException {
		this.blankCsvFilepath = blankCsv;
		this.nrCsvFilepath = nrCsv;
		this.rCsvFilepath = rCsv;
		this.sampleInfoXmlFile = sampleInfoXmlFile;
		this.outDir = outDir;
		
		//parse the Sample Info XML
		File inputFile = new File(sampleInfoXmlFile);
		DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		sampleInfoXmlDoc = builder.parse(inputFile);
	}

	public static void main(String[] args) {
		String blankCsv = "";
		String nrCsv = "";
		String rCsv = "";
		String sampleInfoXmlFile = "";
		String outDir = "";
		
		if(args.length != 5){
			printUsage();
			return;
		}else{
			//get input params
			String[] curParam = null;
			for(int i = 0; i < args.length; i++){
				curParam = args[i].split("=");
				if(curParam[0].equals("--BLANK"))
					blankCsv = curParam[1];
				else if(curParam[0].equals("--NR"))
					nrCsv = curParam[1];
				else if(curParam[0].equals("--R"))
					rCsv = curParam[1];
				else if(curParam[0].equals("--sampleInfo"))
					sampleInfoXmlFile = curParam[1];
				else if(curParam[0].equals("--outdir"))
					outDir = curParam[1];
				else{
					System.out.println("Unrecognized command line parameter: " + curParam[0]);
					printUsage();
					return;
				}
			}
		}
		
		try {
			HPLCPeakClassifier hpc = new HPLCPeakClassifier(blankCsv, nrCsv, rCsv, sampleInfoXmlFile, outDir);
			String sampleName = hpc.getSampleName();
			hpc.loadBlankLCAUdata();
			hpc.loadNrLCAUdata();
			hpc.loadRLCAUdata();
			hpc.initialRtAlignmentCheck();
			hpc.trimHPLCPeakListsByRT(1.0, 14.0);
			hpc.subtractBackgroundAUsfromR();
			//hpc.printPeaks("R");
			hpc.subtractBackgroundAUsfromNR();
			String chartName = sampleName.split("\\_")[0];
			hpc.drawHPLCsAsJPG(chartName, outDir + chartName.replace(" ",  "") + "s_LCPlot.jpg", 700, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * using JFreeChart, draw the jpg of the LC runs
	 */
	private void drawHPLCsAsJPG(String chartTitle, String outputFilename, int width, int height) {
		XYDataset ds = createDataset();
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);  
		renderer1.setBaseItemLabelsVisible(true);

		ValueAxis domain1 = new NumberAxis("Retention Time (min)");
		//domain1.setRange(lowerMz, higherMz);
		ValueAxis range1 = new NumberAxis("mAU (214nm wavelength)");
		range1.setRange(-20, 500);

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
	private XYDataset createDataset() {
		XYSeries series1 = new XYSeries("R");
		XYSeries series2 = new XYSeries("NR");
		for(int i=0; i < rHplcPeaks.size(); i++){
			series1.add(rHplcPeaks.get(i).getRt(), rHplcPeaks.get(i).getAu());
			series2.add(nrHplcPeaks.get(i).getRt(), nrHplcPeaks.get(i).getAu());
		}

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
		return dataset;
	}

	private void printPeaks(String which) {
		ArrayList<HPLCPeak> peaks = null;
		switch(which){
		case "R":
			peaks = rHplcPeaks;
			break;
		case "BLANK":
			peaks = blankHplcPeaks;
			break;
		case "NR":
			peaks = nrHplcPeaks;
			break;
		}
		for(int i=0; i < peaks.size(); i++)
			System.out.println(peaks.get(i).getRt() + "\t" + peaks.get(i).getAu());
		
	}

	private void subtractBackgroundAUsfromR() {
		subtractAUs(rHplcPeaks, blankHplcPeaks); 
	}
	private void subtractBackgroundAUsfromNR() {
		subtractAUs(nrHplcPeaks, blankHplcPeaks); 
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
		ArrayList<HPLCPeak> newBlankPeakList = new ArrayList<HPLCPeak>();
		ArrayList<HPLCPeak> newNRPeakList = new ArrayList<HPLCPeak>();
		ArrayList<HPLCPeak> newRPeakList = new ArrayList<HPLCPeak>();
		for(int i=0; i < rHplcPeaks.size(); i++){
			if(rHplcPeaks.get(i).getRt() >= lowerRtLimit 
					&& rHplcPeaks.get(i).getRt() <= upperRTLimit){
				newRPeakList.add(rHplcPeaks.get(i));
				newNRPeakList.add(nrHplcPeaks.get(i));
				newBlankPeakList.add(blankHplcPeaks.get(i));
			}
		}
		blankHplcPeaks = newBlankPeakList;
		nrHplcPeaks = newNRPeakList;
		rHplcPeaks = newRPeakList;
	}

	private void initialRtAlignmentCheck() throws Exception {
		assert(	Math.abs(blankHplcPeaks.get(0).getRt() - nrHplcPeaks.get(0).getRt()) < .01 &&
				Math.abs(blankHplcPeaks.get(0).getRt() - rHplcPeaks.get(0).getRt()) < .01 &&
				Math.abs(nrHplcPeaks.get(0).getRt() - rHplcPeaks.get(0).getRt()) < .01);
	}

	private void loadBlankLCAUdata() throws IOException {
		blankHplcPeaks = new ArrayList<HPLCPeak>();
		loadLCAUdata(blankHplcPeaks, blankCsvFilepath);
	}

	private void loadNrLCAUdata() throws IOException {
		nrHplcPeaks = new ArrayList<HPLCPeak>();
		loadLCAUdata(nrHplcPeaks, nrCsvFilepath);
	}

	private void loadRLCAUdata() throws IOException {
		rHplcPeaks = new ArrayList<HPLCPeak>();
		loadLCAUdata(rHplcPeaks, rCsvFilepath);
	}

	private void loadLCAUdata(ArrayList<HPLCPeak> hplcp, String filepath) throws IOException {
		 // FileReader reads text files in the default encoding.
        FileReader fileReader = new FileReader(filepath);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = null;
        String[] rt_au = null;
        while((line = bufferedReader.readLine()) != null) {
        	rt_au = line.split("\\w?,\\w?");
        	hplcp.add(new HPLCPeak(Double.parseDouble(rt_au[0]), Double.parseDouble(rt_au[1])));
        }   

        // Always close files.
        bufferedReader.close(); 
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
		System.out.println("USAGE: HPLCPeakClassifier --BLANK=pathToBlankCsvFile --NR=pathToNRcsvFile --R=pathToRcsvFile --sampleInfo=pathToSampleInfoXmlFile --outdir=pathToOutputDir");
	}

}
