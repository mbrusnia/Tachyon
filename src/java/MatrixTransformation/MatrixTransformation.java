package MatrixTransformation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * 7/18/17
 * @author Hector Ramos
 * 
 * Purpose: 
 * 		1) Transform a square matrix to RowID; ColumnID; matrix-value format
 * 		2) Filter output by matrix-value, if given at prompt by user
 * 		3) print histogram with default nbin=100
 * 
 * ***Since matrix are symmetrical, all the output and histogram should use only upper triangle matrix.***
 * 
 * params: --input=project35_example_matrix.csv --histogram --nbin=100 --cut_off=50.0
 * --greaterthanequalto --lessthanequaltto --output=transformed.csv
 * 
 */
public class MatrixTransformation {

	public static void main(String[] args) {
		String inputFile = null;
		String outputFile = null;
		String histogramFilename = null;
		int nbin = 100;
		boolean doHistogram = false;
		Double cutOff = Double.MIN_VALUE;
		boolean greaterThanEqualTo = false;
		boolean lessThanEqualTo = false;
		ArrayList<Double> allValues = null;
		
		String[] curParam = null;
		for(int i = 0; i < args.length; i++){
			curParam = args[i].split("=");
			if(curParam[0].equals("--input"))
				inputFile = curParam[1];
			else if(curParam[0].equals("--output"))
				outputFile = curParam[1];
			else if(curParam[0].equals("--greaterthanequalto"))
				greaterThanEqualTo = true;
			else if(curParam[0].equals("--lessthanequalto"))
				lessThanEqualTo = true;
			else if(curParam[0].equals("--cut_off"))
				cutOff = Double.parseDouble(curParam[1]);
			else if(curParam[0].equals("--histogram"))
				doHistogram = true;
			else if(curParam[0].equals("--nbin"))
				nbin = Integer.parseInt(curParam[1]);
			else{
				System.out.println("Unrecognized command line parameter: " + curParam[0]);
				printUsage();
				return;
			}	
		}

		if(inputFile == null || outputFile == null){
			System.out.println("ERROR: you must specify --input and --output values!");
			printUsage();
			return;
		}
		
		if(cutOff > Double.MIN_VALUE && (!(greaterThanEqualTo || lessThanEqualTo) || (greaterThanEqualTo && lessThanEqualTo))){
			System.out.println("ERROR: When a --cut_off value is specified, this must be accompanied with either --greaterthanorequalto OR --lessthanorequalto BUT NOT BOTH.  Please try again.");
			printUsage();
			return;
		}
		if((greaterThanEqualTo || lessThanEqualTo) && cutOff.equals(Double.MIN_VALUE)){
			System.out.println("ERROR: When either --greaterthanorequalto or --lessthanorequalto are specified, you must provide a --cut_off value.  Please try again.");
			printUsage();
			return;
		}
		
		//set histogram filename
		if(doHistogram){
			histogramFilename = outputFile.substring(0, outputFile.lastIndexOf(".")) + ".jpg";
			allValues = new ArrayList<Double>();
			System.out.println("Info: drawing histogram with " + nbin + " bins to " + histogramFilename);
		}
		
		
		//prepare the reading and writing
		FileReader fileReader;
		BufferedReader inputBufferedReader = null;
		File fout1;
		FileOutputStream fos1;
		BufferedWriter outputFileWriter;
		try {
			fileReader = new FileReader(inputFile);
			inputBufferedReader = new BufferedReader(fileReader);
			fout1 = new File(outputFile);
			fos1 = new FileOutputStream(fout1);
			outputFileWriter = new BufferedWriter(new OutputStreamWriter(fos1));
			int row = 0, col = 0;
			String[] colHeaders = null;
			String line;
			while ((line = inputBufferedReader.readLine()) != null) {
				//parse out the headers... this is uglier than I wish it had to be
				if(row == 0){
					colHeaders = line.split("\",");
					if(colHeaders[0].startsWith(","))
						colHeaders[0] = colHeaders[0].substring(1);
					for(; col < colHeaders.length; col++)
						colHeaders[col] = colHeaders[col].replaceAll("\"", "");
				}else{
					//print: RowID; ColumnID; matrix value
					String[] tmp = line.split("\",");
					String rowID = tmp[0].replaceAll("\"", "");
					String[] strValues = tmp[1].split(",");
					if(row != colHeaders.length && strValues.length != colHeaders.length){
						System.out.println("ERROR: row " + row + 1 + " has " + strValues.length + 1 + " columns.  There are " + colHeaders.length + 1 + " column headers, and that is what is expected of each row.\n");
						inputBufferedReader.close();
						outputFileWriter.close();
						return;
					}
					//(row, row) is always blank (""), so start at cell to the right (row + 1)
					for(col = row; col < colHeaders.length; col++){
						Double val = Double.parseDouble(strValues[col]);
						if(!greaterThanEqualTo && !lessThanEqualTo)
							outputFileWriter.write(rowID + "; " + colHeaders[col] + "; " + strValues[col] + "\n");
						else if(greaterThanEqualTo && val >= cutOff)
							outputFileWriter.write(rowID + "; " + colHeaders[col] + "; " + strValues[col] + "\n");
						else if(lessThanEqualTo && val <= cutOff)
							outputFileWriter.write(rowID + "; " + colHeaders[col] + "; " + strValues[col] + "\n");
						
						if(doHistogram)
							allValues.add(val);
					}
				}
				row++;
			}
			inputBufferedReader.close();
			outputFileWriter.close();
			
			//draw histogram jpg
			if(doHistogram){
				//transform the ArrayList<Double> to double[]
				double[] doubleVals = new double[allValues.size()];
				for (int i = 0; i < doubleVals.length; i++) {
					doubleVals[i] = allValues.get(i);
				 }

				HistogramDataset dataset = new HistogramDataset();
			    dataset.setType(HistogramType.FREQUENCY);
			    dataset.addSeries("Histogram",doubleVals,nbin);
			    String plotTitle = "Histogram (nbin=" + nbin + ")"; 
			    String xaxis = "Value";
			    String yaxis = "Absolute Count"; 
			    PlotOrientation orientation = PlotOrientation.VERTICAL; 
			    boolean show = false; 
			    boolean toolTips = false;
			    boolean urls = false; 
			    JFreeChart chart = ChartFactory.createHistogram( plotTitle, xaxis, yaxis, 
			    		dataset, orientation, show, toolTips, urls);
			    int width = 1000;
			    int height = 600; 
			    ChartUtilities.saveChartAsPNG(new File(histogramFilename), chart, width, height);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void printUsage() {
		System.out.print("");
		System.out.println("USAGE: MatrixTransformation --input=myMatrixFile.csv --output=outputFile.csv --histogram --nbin=100 --cut_off=50.0 --greaterthanequalto --lessthanequaltto");
		System.out.print("");
	}

}
