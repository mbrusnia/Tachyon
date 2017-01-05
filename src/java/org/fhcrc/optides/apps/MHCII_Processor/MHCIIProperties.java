package org.fhcrc.optides.apps.MHCII_Processor;

public class MHCIIProperties {

	protected String allele; //  -- please make this as a list so new allele could be added. If input has unknown allele, must thrown an exception with the unknown input allele.
	protected Integer pos;          
	protected String peptide;       
	protected String core; 
	protected Double log50k; 
	protected Double affinity_nM;
	protected String bindLevel;
	protected Double randomPercent;    
	protected String identity;
	protected Double alleleWeightToUse;
	protected Double bindLevelWeightToUse;
	protected Double score;
	
	
	public MHCIIProperties(String allele, Integer pos, String peptide, String core, Double log50k, 
			Double affinity_nM, String bindLevel, Double randomPercent, String identity, Double alleleWeightToUse, Double bindLevelWeightToUse) {
		this.allele = allele;
		this.pos = pos;
		this.peptide = peptide;
		this.core = core;
		this.log50k = log50k;
		this.affinity_nM = affinity_nM;
		this.bindLevel = bindLevel;
		this.randomPercent = randomPercent;
		this.identity = identity;
		this.alleleWeightToUse = alleleWeightToUse;
		this.bindLevelWeightToUse = bindLevelWeightToUse;
		
		//calculate score
		this.score = affinity_nM * alleleWeightToUse * bindLevelWeightToUse;
	}
	
	public String toString(){
		return allele + "\t" + pos.toString() + "\t" + peptide + "\t" + core + "\t" + log50k.toString() + "\t" + affinity_nM.toString() + "\t" + bindLevel + "\t" + randomPercent.toString() + "\t" + identity;
	}
}
