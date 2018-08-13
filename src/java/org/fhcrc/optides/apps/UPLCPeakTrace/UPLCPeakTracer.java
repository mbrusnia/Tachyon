package org.fhcrc.optides.apps.UPLCPeakTrace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

public class UPLCPeakTracer {
	private static String nrFilename = "";
	private static String stdFilename = "";
	private static double maxRTFforPeak;
	private static double minRTFforPeak;
	private static int maxMAUForPeak = 500;
	private static String outputdir = "";
	private static final int numbOfPeaks = 0;

	public static void main(String[] args) throws IOException {
		// read file
		String[] curParam = null;
		for (int i = 0; i < args.length; i++) {
			curParam = args[i].split("=");
			if (curParam[0].equals("--STD")) {
				stdFilename = curParam[1];
			} else if (curParam[0].equals("--NR")) {
				nrFilename = curParam[1];
			} else if (curParam[0].equals("--MaxRTForPeak")) {
				maxRTFforPeak = Double.parseDouble(curParam[1]);
			} else if (curParam[0].equals("--MinRTForPeak")) {
				minRTFforPeak = Double.parseDouble(curParam[1]);
			} else if (curParam[0].equals("--MaxMAUForPeak")) {
				maxMAUForPeak = Integer.parseInt(curParam[1]);
			} else if (curParam[0].equals("--outputdir")) {
				outputdir = curParam[1];
			} else {
				System.out.println("invalid input");
			}
		}
		if(nrFilename == "" || stdFilename == "" || outputdir == ""){
			System.out.println("A command line parameter is missing or incorrect.  Please look over your entered parameters.");
			printUsage();
			return;
		}

		// get peaks
		ArrayList<HPLCPeakComparable> HPCLPeakList = acquireData(nrFilename);
		ArrayList<HPLCPeakComparable> fivePeaks = pickPeaks(stdFilename);
		Collections.sort(fivePeaks);
		// draw peeks on the image
		XYLineChart_AWT chart = new XYLineChart_AWT(getFilenameFromFullPath(nrFilename), getFilenameFromFullPath(nrFilename),
				HPCLPeakList, fivePeaks);
		// save image
		int width = 640; /* Width of the image */
		int height = 480; /* Height of the image */
		File XYChart = new File(outputdir + getFilenameFromFullPath(nrFilename).replace(".arw", ".jpg"));
		ChartUtilities.saveChartAsJPEG(XYChart, chart.getChart(), width, height);
	}

	private static String getFilenameFromFullPath(String nrFilename2) {
		return nrFilename2.substring(nrFilename2.lastIndexOf("/") + 1);
	}

	private static void printUsage() {
		System.out.println("USAGE: UPLCPeakClassifier --NR=pathToNRarwFile --STD=pathToStandardArwFile --outdir=pathToOutputDir --MaxRTForPeak=maxRTtoConsider --MinRTForPeak=minRTtoConsider --MaxMAUForPeak=upperYvalueOnChart");
		System.out.println("");
		System.out.println("note: MaxMAUForPeak is defaulted to 500 if not entered.");
		System.out.println("note: if SN parameter is set to greater than 1, then it will be used as an absolute intensity threshold cuttoff for peak finding.");
	}

	// data acquisition
	public static ArrayList<HPLCPeakComparable> acquireData(String filename)
			throws FileNotFoundException {
		ArrayList<HPLCPeakComparable> list = new ArrayList<HPLCPeakComparable>();
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		System.out.println(sc.nextLine());
		System.out.println(sc.nextLine());

		while (sc.hasNextLine()) {
			String dataLine = sc.nextLine();
			String[] splited = dataLine.split("\\s+");
			Double xCoord = Double.parseDouble(splited[0]);
			Double yCoord = Double.parseDouble(splited[1]);
			list.add(new HPLCPeakComparable(xCoord, yCoord));
		}
		return list;
	}

	public static ArrayList<HPLCPeakComparable> pickPeaks(String filename)
			throws FileNotFoundException {
		ArrayList<HPLCPeakComparable> list = acquireData(filename);
		ArrayList<HPLCPeakComparable> returnList = new ArrayList<HPLCPeakComparable>();
		for (int i = 1; i < list.size() - 1; i++) {
			if ((list.get(i).getAu() > (list.get(i - 1).getAu()) && list.get(i)
					.getAu() > list.get(i + 1).getAu())) {
				returnList.add(list.get(i));
			}
		}
		Collections.sort(returnList);
		for (int i = 0; i < returnList.size() - 1; i++) {
			if (returnList.get(i).getRt() - returnList.get(i + 1).getRt() < 0.1) {
				returnList.remove(i + 1);
			}
		}
		return returnList;
	}

}
