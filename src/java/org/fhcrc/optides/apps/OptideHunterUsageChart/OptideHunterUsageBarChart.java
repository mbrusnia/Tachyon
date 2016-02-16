package org.fhcrc.optides.apps.OptideHunterUsageChart;

/*
 * This program parses a flask.log file to produce a jfreechart barchart
 * of usage statistics over the last several months (#months specified 
 * by the user) by the events of interest.  
 */

import java.io.*;
import java.math.RoundingMode;
import java.util.*;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.ChartFactory;
import org.jfree.data.category.DefaultCategoryDataset;

public class OptideHunterUsageBarChart {
	
	//height and width of the chart
	private static int chartHeight = 540;
	private static int chartWidth = 1000;
	
	/*
	 * events we look for in the log file
	 */
	private static String[][] eventsOfInterest= {
			{"view_request_gblocks start", "Gblocks"},
			{"view_request_agilent_hplcworklist start", "Agilent HPLCworklist"},
	        {"view_request_masscalc start", "MassCalc"},
	        {"view_request_proteinquantcalc start", "Protein QuantCalc"},
	        {"view_request_blastotdsearch start", "BlastOTDsearch"}};
	
	//main app
	public static void main(String[] args) {
		if(args.length !=2){
			System.out.println("USAGE: java OptideHunterUsageBarChart monthsBack flaskLogFilename");
			return;
		}
		int monthsBack = Integer.parseInt(args[0]);
		String filename = args[1];
		Calendar cal = Calendar.getInstance();
		DecimalFormat mFormat= new DecimalFormat("00");
		mFormat.setRoundingMode(RoundingMode.DOWN);
		String outputFilename = "OptideHunterUsageBarChart-" + mFormat.format(cal.get(Calendar.YEAR)) + "-" +  mFormat.format(Double.valueOf(cal.get(Calendar.MONTH) + 1)) + "-" +  mFormat.format(cal.get(Calendar.DAY_OF_MONTH))  + ".jpg";
		String result = filename.substring(0, filename.lastIndexOf("/") + 1) + outputFilename;

		// This will create the dataset 
        DefaultCategoryDataset dataset = getDataset(monthsBack, filename);
        // based on the dataset we create the chart
        String chartTitle = "Optide-Hunter Usage Statistics - " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
        JFreeChart chart = ChartFactory.createBarChart(
        		chartTitle, 
        		"Month", "Frequency", 
        		dataset,PlotOrientation.VERTICAL, 
        		true, true, false);
		
		try {
			ChartUtilities.saveChartAsJPEG(new File(result), chart, chartWidth, chartHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Create the chart
	 */
	
	
	private static DefaultCategoryDataset getDataset(int monthsBack, String filename) { 
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		 
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Map<String, Map<String, Integer>> map = new LinkedHashMap<String, Map<String, Integer>>();

		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			for(String line; (line = br.readLine()) != null; ) {
				if(line.contains(eventsOfInterest[0][0]) ||
						line.contains(eventsOfInterest[1][0]) ||
						line.contains(eventsOfInterest[2][0]) ||
						line.contains(eventsOfInterest[3][0]) ||
						line.contains(eventsOfInterest[4][0])){
					String[] a = line.split(" ");
					a[3] += " start";
					Date accessDate = null;
					try {
						accessDate =  df.parse(parseDate(a[0]));
						Calendar calendar=Calendar.getInstance();
						calendar.setTime(accessDate);
						calendar.set(Calendar.HOUR_OF_DAY, 2);
						accessDate = calendar.getTime();
					} catch (ParseException e) {
						e.printStackTrace();
					}
					Calendar cal = Calendar.getInstance();  //Get current date/month i.e 27 Feb, 2012
					cal.add(Calendar.MONTH, - monthsBack);   //Go to date, backmonths months ago 27 July, 2011
					cal.set(Calendar.DAY_OF_MONTH, 1); //set date, to make it 1 July, 2011
					cal.set(Calendar.HOUR_OF_DAY, 0); //set hour of day, to make it 1 July, 2011
					
					if(accessDate.after(cal.getTime())){
						String dateStr = df.format(accessDate);
						if(!map.containsKey(dateStr)){
							map.put(dateStr, new HashMap<String, Integer>());
						}
						if(!map.get(dateStr).containsKey(a[3]))
							map.get(dateStr).put(a[3],  1);
						else
							map.get(dateStr).put(a[3], map.get(df.format(accessDate)).get(a[3]) +1);
					}
				}
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * now store the results of all our parsing into our dataset object
		 */
        int j = 0;
		Iterator it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Map<String, Integer> innerMap = (Map<String, Integer>) pair.getValue();
	        
	        for(int i = 0; i < 5; i++){
	        	Calendar cal = Calendar.getInstance(); 
	        	try {
					cal.setTime(df.parse(pair.getKey().toString()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
	        	String xDate = getMonthFromInt(cal.get(Calendar.MONTH)) + " - " + cal.get(Calendar.YEAR);
	        	if(innerMap.containsKey(eventsOfInterest[i][0])){
				        dataset.addValue(Integer.parseInt(innerMap.get(eventsOfInterest[i][0]).toString()), eventsOfInterest[i][1], xDate);
		    	}else{
		    		dataset.addValue(0, eventsOfInterest[i][1], xDate);
		    	}
	        	j++;		    		
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }  
		
		return dataset; 
	} 
	
	
	/* s comes in as format YYYY-MM-DD, we need to reformat 
	 * 		it for SimpleDateFormat.parse; also, since we are interested
	 * 		in monthly, we change the date to only 1
	 */
	private static String parseDate(String s) {
		String[] b = s.split("-");
		b[2] = "01";
		return b[1] +"/"+ b[2] + "/"+ b[0];
	}
	
	private static String getMonthFromInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getShortMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }

}
