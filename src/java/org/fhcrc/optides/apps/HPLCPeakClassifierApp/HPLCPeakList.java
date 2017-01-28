package org.fhcrc.optides.apps.HPLCPeakClassifierApp;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class HPLCPeakList extends ArrayList<HPLCPeak> {
	protected HPLCPeak majorPeak = null;
	
	//If there are no AU values above this value, we will not count any peaks (in peakPick())
	public static double peakAbsoluteAUThreshold = 20.0;
	
	protected HPLCPeakList peakPick(double sn_ratio, double lowerRT, double upperRT, boolean isARW){
		HPLCPeakList retVal = new HPLCPeakList();
		
		//find max AU value
		double max = this.getMaxAU(lowerRT, upperRT);
		
		//if max au is not greater than peakAbsoluteAUThreshold, then forget it, we wont count any peaks at all
		if(!isARW && max < peakAbsoluteAUThreshold)
			return retVal;
				
		//find the peaks of the ones that pass  the s/n ratio
		HPLCPeak local_max = new HPLCPeak(0,0);
		double threshold = max*sn_ratio;
		if(sn_ratio > 1.0)
			threshold = sn_ratio;
		double prev_au = 0;
		String direction = "down";
		for(int i=0; i < this.size(); i++){
			if(this.get(i).getRt() >= lowerRT 
					&& this.get(i).getRt() <= upperRT 
					&& this.get(i).getAu() >= threshold){
				if(direction.equals("down") && this.get(i).getAu() > prev_au){
					direction = "up";
				}
				if(direction.equals("up")){
					if(this.get(i).getAu() > local_max.getAu()){
						local_max = this.get(i);
					}else {
						retVal.add(local_max);
						local_max = new HPLCPeak(0,0);
						direction = "down";						
					}
				}
				prev_au = this.get(i).getAu();
			}
		}
		return retVal;
	}
	
	protected double getMaxAU(double lowerRT, double upperRT) {
		double max = 0.0;
		for(int i=0; i < this.size(); i++){
			if(this.get(i).getRt() >= lowerRT 
					&& this.get(i).getRt() <= upperRT 
					&& this.get(i).getAu() > max){
				max = this.get(i).getAu();
				majorPeak = this.get(i);
			}
		}
		return max;
	}

	public double getTotalAU(double lowerRT, double upperRT) {
		double total = 0.0;
		for(int i=0; i < this.size(); i++){
			if(this.get(i).getRt() >= lowerRT && this.get(i).getRt() <= upperRT)
				total += this.get(i).getAu();
		}
		return total;
	}

	public double getAverageAU(double lowerRT, double upperRT) {
		int count = 0;
		for(int i=0; i < this.size(); i++){
			if(this.get(i).getRt() >= lowerRT && this.get(i).getRt() <= upperRT)
				count++;
		}
		return getTotalAU(lowerRT, upperRT) / count;
	}
	public HPLCPeak getMajorPeak(double lowerRT, double upperRT){
		if(majorPeak == null)
			getMaxAU(lowerRT, upperRT);
		return majorPeak;
	}
}
