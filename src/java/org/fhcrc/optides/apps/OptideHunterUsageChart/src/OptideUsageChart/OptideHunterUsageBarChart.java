package OptideUsageChart;

/*
 * This program parses a flask.log file to produce a jfreechart barchart
 * of usage statistics over the last several months (#months specified 
 * by the user) by the events of interest.  
 */

import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.ChartFactory;
import org.jfree.data.category.DefaultCategoryDataset;

public class OptideHunterUsageBarChart extends JFrame{
	
	//height and width of the chart
	private int chartHeight = 540;
	private int chartWidth = 1000;
	
	/*
	 * events we look for in the log file
	 */
	private String[][] eventsOfInterest= {
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
		
		OptideHunterUsageBarChart bc = new OptideHunterUsageBarChart("Optide-Hunter Usage Statistics", Integer.parseInt(args[0]), args[1]);
		bc.pack();
		bc.setVisible(true);
	}
	
	/*
	 * Create the chart
	 */
	public OptideHunterUsageBarChart(String chartTitle, int monthsBack, String filename) {
        super(chartTitle);
        // This will create the dataset 
        DefaultCategoryDataset dataset = getDataset(monthsBack, filename);
        // based on the dataset we create the chart
        JFreeChart chart = ChartFactory.createBarChart(
        		chartTitle, 
        		"Month", "Frequency", 
        		dataset,PlotOrientation.VERTICAL, 
        		true, true, false);
        // we put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(chartWidth, chartHeight));
        // add it to our application
        setContentPane(chartPanel);
	  } 
	
	private DefaultCategoryDataset getDataset(int monthsBack, String filename) { 
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
						accessDate =  df.parse(this.parseDate(a[0]));
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
	        	if(innerMap.containsKey(eventsOfInterest[i][0])){
				        dataset.addValue(Integer.parseInt(innerMap.get(eventsOfInterest[i][0]).toString()), eventsOfInterest[i][1], getMonthFromInt(cal.get(Calendar.MONTH)));
				        //System.out.println(innerMap.get(eventsOfInterest[i][0]));
		    	}else{
		    		dataset.addValue(0, eventsOfInterest[i][1], getMonthFromInt(cal.get(Calendar.MONTH)));
		    	}
	        	j++;		    		
	        }
	        it.remove(); // avoids a ConcurrentModificationException
	    }  
		
		return dataset; 
	} 
	
	/*
	 * Parse the flask.log file provided
	 */
	public String[][] getStats(int monthsBack, String filename){
		String[][] returnVal = new String[eventsOfInterest.length * (monthsBack + 1)][3];
		
		return returnVal;
	}

	/* s comes in as format YYYY-MM-DD, we need to reformat 
	 * 		it for SimpleDateFormat.parse; also, since we are interested
	 * 		in monthly, we change the date to only 1
	 */
	private String parseDate(String s) {
		String[] b = s.split("-");
		b[2] = "01";
		return b[1] +"/"+ b[2] + "/"+ b[0];
	}
	
	private String getMonthFromInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }

}
