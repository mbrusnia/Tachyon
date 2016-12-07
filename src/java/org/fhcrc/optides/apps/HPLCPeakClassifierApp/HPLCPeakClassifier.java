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
import java.nio.charset.Charset;

import javax.xml.parsers.*;
import java.io.*;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;


public class HPLCPeakClassifier {
	//Signal-to-noise ratio for cutoff in peak picking
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
	private boolean isPartialPeak = false;

	//max and min RTs to use in all calculations
	private double maxRTForPeak = 11.0; 
	private static double minRTForPeak = 2.0; 
	
	//when checking for r and nr peak overlap, this is the RT tolerance for the window
	private static double peakOverlapTolerance = .1;
	
	//xml document for Sample Info
	private Document sampleInfoXmlDoc = null;
	
	//Sample Info
	private String sampleName = null;
	
	//peak lists stored in a HashMap (key = fullpathFfilename)
	private HashMap<String, HPLCPeakList> peakLists = null;	
	
	//peak picked lists
	private HPLCPeakList rpp = null;
	private HPLCPeakList nrpp = null;
	
	//path to Logging output file
	private static String loggingFilepath = "C:/Program Files/OptidesSoftware/Optide-Hunter.log";
	
	
	public HPLCPeakClassifier(double sn_ratio, int classification, String blankRCsv, String blankNRCsv, String nrCsv, String rCsv, String sampleInfoXmlFile, double maxRTForPeak, String outDir) throws ParserConfigurationException, SAXException, IOException {
		this.sn_ratio = sn_ratio;
		this.classification = classification;
		this.blankRCsvFilepath = blankRCsv;
		this.blankNRCsvFilepath = blankNRCsv;
		this.nrCsvFilepath = nrCsv;
		this.rCsvFilepath = rCsv;
		this.sampleInfoXmlFile = sampleInfoXmlFile;
		this.maxRTForPeak = maxRTForPeak;
		this.outDir = outDir;
		
		//initiate peakLists HashMap:
		peakLists = new HashMap<String, HPLCPeakList>();
		peakLists.put(blankRCsvFilepath, new HPLCPeakList());
		peakLists.put(blankNRCsvFilepath, new HPLCPeakList());
		peakLists.put(nrCsvFilepath, new HPLCPeakList());
		peakLists.put(rCsvFilepath, new HPLCPeakList());
		
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
		double maxRTForPeak = 11.0;
		int chartDefaultYmax = 500;
		
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
			else if(curParam[0].equals("--MaxRTForPeak"))
				maxRTForPeak = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--MinRTForPeak"))
				minRTForPeak = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--Classification"))
				classification = Integer.parseInt(curParam[1]);
			else if(curParam[0].equals("--MaxMAUForPeak"))
				chartDefaultYmax = Integer.parseInt(curParam[1]);			
			else if(curParam[0].equals("--outdir"))
				outDir = curParam[1];
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}
		}
		
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
			System.out.println("--MaxRTForPeak: " + maxRTForPeak);
			System.out.println("--MaxMAUForPeak: " + chartDefaultYmax);
			System.out.println(""); 
			printUsage();
			return;
		}
		if(blankRCsv == blankNRCsv){
			System.out.println("BLANK_R and BLANK_NR files cannot be set to the same value!");
			return;
		}
		
		try {
			HPLCPeakClassifier hpc = new HPLCPeakClassifier(sn_ratio, classification, blankRCsv, blankNRCsv, nrCsv, rCsv, sampleInfoXmlFile, maxRTForPeak, outDir);
			String sampleName = hpc.getSampleName();
			hpc.loadLCAUdata();
			hpc.initialRtAlignmentCheck();
			hpc.trimHPLCPeakListsByRT(1.0, 14.0);
			hpc.subtractBackgroundAUsfromR();
			//hpc.printPeaks("R");
			hpc.subtractBackgroundAUsfromNR();
			
			hpc.peakPickingR(sn_ratio, minRTForPeak, maxRTForPeak);
			hpc.peakPickingNR(sn_ratio, minRTForPeak, maxRTForPeak);
			
			int nrpeaks = hpc.getNRPeakPicked().size();
			int rpeaks = hpc.getRPeakPicked().size();

			String str = sampleName.split("\\_")[0];
			String chartName = str;
			String fileName = str;
			String classificationOutput = "";
			int peaks = hpc.getNumberOfPeaksToReport();
			boolean isPartialPeak = hpc.isPartialPeak();
			
			if(nrpeaks == 0){
				if(nrpeaks == 0)
					classificationOutput += "NoNR";
				if(nrpeaks == 0 && rpeaks == 0)
					classificationOutput += "-";
				if(rpeaks == 0)
					classificationOutput += "NoR";
			}else{				
				if(peaks == 1){
					if(isPartialPeak){
						classificationOutput = "Perfect-PR";
					}
					else classificationOutput = "Perfect";
				}else if (peaks > 1 && peaks <= classification){
					classificationOutput = "Simple";
				}else if(peaks > classification){
					classificationOutput = "Complex";
				}
			}
			fileName += "_" + classificationOutput;
			DecimalFormat df = new DecimalFormat("#.00");
			df.setRoundingMode(RoundingMode.HALF_UP);
			if(nrpeaks == 0){
				fileName += "_0.00";
			}else{
				fileName += "_" + df.format(hpc.getPeakList(nrCsv).getMaxAU(minRTForPeak, maxRTForPeak));
			}
			fileName +=  ".jpg";
			chartName += " [" + classificationOutput + "]";
			System.out.println("max peaks found: " + peaks);
			System.out.println("Chart Title: " + chartName);
			System.out.println("Filename: " + fileName);
			
			hpc.drawHPLCsAsJPG(chartName, outDir + fileName.replace(" ",  ""), 800, 600, chartDefaultYmax);
		
			//append stats to log file
			/*
			 * 
	 Date BLANK_R BLANK_NR R NR ClassificationOutput R_Max NR_Max R_Total NR_Total BLANK_R_Max 
	 								BLANK_NR_Max BLANK_R_Total BLANK_R_Total
	
			 */
			StringBuilder log_str = new StringBuilder();
			File f = new File(HPLCPeakClassifier.loggingFilepath);
			PrintWriter logfile = null;
			if(!f.exists()){
				Files.write(Paths.get(HPLCPeakClassifier.loggingFilepath), "Date\tBLANK_R\tBLANK_NR\tR\tNR\tClassificationCount\tClassificationOutput\tR_Max\tNR_Max\tR_Total\tNR_Total\tBLANK_R_Max\tBLANK_NR_Max\tBLANK_R_Total\tBLANK_R_Total\n".getBytes(), StandardOpenOption.CREATE_NEW);
			}

			//append
			log_str.append(new Date() + "\t");
			log_str.append(blankRCsv + "\t");
			log_str.append(blankNRCsv + "\t");
			log_str.append(rCsv + "\t");
			log_str.append(nrCsv + "\t");
			log_str.append(classification + "\t");
			log_str.append(classificationOutput + "\t");
			log_str.append(df.format(hpc.getPeakList(rCsv).getMaxAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(nrCsv).getMaxAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(rCsv).getTotalAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(nrCsv).getTotalAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(blankRCsv).getMaxAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(blankNRCsv).getMaxAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(blankRCsv).getTotalAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append(df.format(hpc.getPeakList(blankNRCsv).getTotalAU(minRTForPeak, maxRTForPeak)) + "\t");
			log_str.append("\n");
			
			Files.write(Paths.get(HPLCPeakClassifier.loggingFilepath), log_str.toString().getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private HPLCPeakList getPeakList(String key) {
		return peakLists.get(key);
	}

	
	/*
	 * after peak picking, this function tells us what the  num of peaks between r vs. nr to report
	 * we want to report the most possible, after subtracting overlapping nr and r peaks
	 */
	private int getNumberOfPeaksToReport() {
		int peaks = nrpp.size();
		if(rpp.size() > peaks){
			peaks = rpp.size();
			
			//if a reduced peak overlaps with the nr major peak, subtract it from the count
			HPLCPeak nrMajorPeak = nrpp.getMajorPeak(minRTForPeak, maxRTForPeak);
			if(nrMajorPeak == null)
				return peaks;
			for(int i = 0; i < rpp.size(); i++){
				if(Math.abs(nrMajorPeak.getRt() - rpp.get(i).getRt()) < peakOverlapTolerance){
					peaks--;
					isPartialPeak = true;
					break;
				}
			}
		}
		return peaks;
	}
	public boolean isPartialPeak(){
		return isPartialPeak;
	}
	public void peakPickingR(double sn_ratio, double lowerRT, double upperRT) {
		HPLCPeakList pl = peakLists.get(rCsvFilepath);
		rpp = pl.peakPick(sn_ratio, lowerRT, upperRT);
	}
	
	public void peakPickingNR(double sn_ratio, double lowerRT, double upperRT) {
		HPLCPeakList pl = peakLists.get(nrCsvFilepath);
		nrpp = pl.peakPick(sn_ratio, lowerRT, upperRT);
	}
	
	
	/*
	 * using JFreeChart, draw the jpg of the LC runs
	 */
	private void drawHPLCsAsJPG(String chartTitle, String outputFilename, int width, int height, int defaultYmax) {
		XYDataset ds = createDataset(peakLists.get(rCsvFilepath), peakLists.get(nrCsvFilepath));
		//ds = createDataset(rpp, nrpp);
		
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);  
		renderer1.setBaseItemLabelsVisible(true);

		ValueAxis domain1 = new NumberAxis("Retention Time (min)");
		//domain1.setRange(lowerMz, higherMz);
		domain1.setRange(0, 15);
		ValueAxis range1 = new NumberAxis("mAU (214nm wavelength)");
		int yMax = defaultYmax;
		int yNRpp = maxAUvalue(rpp, nrpp);
		if(yNRpp > yMax){
			yMax = yNRpp + 20;
		}
		range1.setRange(0, yMax);
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

	private int maxAUvalue(HPLCPeakList l1, HPLCPeakList l2) {
		double max = 0;
		for(int i = 0; i < l1.size(); i++){
			if(l1.get(i).getAu() > max)
				max = l1.get(i).getAu();
		}
		for(int i = 0; i < l2.size(); i++){
			if(l2.get(i).getAu() > max)
				max = l2.get(i).getAu();
		}
		return (int) max;
	}

	/*
	 * create the dataset for jFreeChart to draw the line chart
	 */
	private XYDataset createDataset(HPLCPeakList rHplcPeaks, HPLCPeakList nrHplcPeaks) {
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
		HPLCPeakList peaks = null;
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

	private void subtractAUs(HPLCPeakList peaks1, HPLCPeakList peaks2) {
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
		HPLCPeakList newBlankRPeakList = new HPLCPeakList();
		HPLCPeakList newBlankNRPeakList = new HPLCPeakList();
		HPLCPeakList newNRPeakList = new HPLCPeakList();
		HPLCPeakList newRPeakList = new HPLCPeakList();
		
		HPLCPeakList refPeakList = peakLists.get(blankRCsvFilepath);
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
		for (HashMap.Entry<String, HPLCPeakList> entry : peakLists.entrySet()) {
			 /*default encoding **/
			// FileReader reads text files in the default encoding.
	        FileReader fileReader = new FileReader(entry.getKey());
	
	        // Always wrap FileReader in BufferedReader.
	        BufferedReader bufferedReader = new BufferedReader(fileReader);

			
			/* UTF-16 encoding
			File f = new File(entry.getKey());
	        FileInputStream stream = new FileInputStream(f);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-16")));
			*/
			
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
		System.out.println("USAGE: HPLCPeakClassifier --NR=pathToNRcsvFile --R=pathToRcsvFile --BLANK_NR=pathToBlankNRCsvFile --BLANK_R=pathToBlankRCsvFile --sampleInfo=pathToSampleInfoXmlFile --outdir=pathToOutputDir --SN=sn_ratio_decimal --Classification=NumOfPeaksForClassification --MaxRTForPeak=maxRTtoConsider --MaxMAUForPeak=upperYvalueOnChart");
		System.out.println("");
		System.out.println("note: MaxMAUForPeak is defaulted to 500 if not entered.");
		System.out.println("note: if SN parameter is set to greater than 1, then it will be used as an absolute intensity threshold cuttoff for peak finding.");
	}

	private HPLCPeakList getNRPeakPicked(){
		return nrpp;
	}

	private HPLCPeakList getRPeakPicked(){
		return rpp;
	}
}
