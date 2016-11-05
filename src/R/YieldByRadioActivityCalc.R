#This is a Labkey Tranformation script to be added to the RadioLabeledConjugate assay
#in the folder: /Optides/ChemProduction/Assay/

options(stringsAsFactors = FALSE)
library(Rlabkey)

BASE_URL<-"http://optides-prod.fhcrc.org/"
${rLabkeySessionId}
rpPath<- "${runInfo}"

getRunPropsList<- function(rpPath, BASE_URL) {
	rpIn<- read.table(rpPath,  col.names=c("name", "val1", "val2", "val3"), 
		header=FALSE, check.names=FALSE,     
		stringsAsFactors=FALSE, sep="\t", quote="", fill=TRUE, na.strings=""); 

	## pull out the run properties

	params<- list(inputPathUploadedFile = rpIn$val1[rpIn$name=="runDataUploadedFile"],
		inputPathValidated = rpIn$val1[rpIn$name=="runDataFile"],
		
		##a little strange.  AssayRunTSVData is the one we need to output to
		outputPath = rpIn$val3[rpIn$name=="runDataFile"],
	
		containerPath = rpIn$val1[rpIn$name=="containerPath"], 
		runPropsOutputPath = rpIn$val1[rpIn$name=="transformedRunPropertiesFile"],
		sampleSetId = as.integer(rpIn$val1[rpIn$name=="sampleSet"]),
		probeSourceId = as.integer(rpIn$val1[rpIn$name=="probeSource"]),
		calibrationFactor = as.numeric(rpIn$val1[rpIn$name=="CalibrationFactor"]),
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
	return (params)

}

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, BASE_URL)

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "csv"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = ",", check.names=FALSE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "tsv" || tools::file_ext(params$inputPathUploadedFile) == "tmp"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t", check.names=FALSE)
}

###################################################################
## 1) Format column headers
###################################################################
if(grepl("ChemProductionID", names(inputDF)[1]) && grepl("InputProtein_mg", names(inputDF)[2])
	&& grepl("Input_mL", names(inputDF)[3]) && grepl("Wash_mL", names(inputDF)[4])
	&& grepl("Reaction_Peak_Area_mV", names(inputDF)[5]) && grepl("Elute_mL", names(inputDF)[6])
	&& grepl("Elute_Peak_Area_mV", names(inputDF)[7]) && grepl("CPM_Vol_mL", names(inputDF)[8])
	&& grepl("Yield_Vol_mL", names(inputDF)[9]) && grepl("Dilution", names(inputDF)[10])
	&& grepl("CPM", names(inputDF)[11]) && grepl("CiPerCPM_Calibration_Factor", names(inputDF)[12])
	&& grepl("Specific_Activity_CiPerMol", names(inputDF)[13])	){	
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): ChemProductionID	InputProtein_mg	Input_mL	Wash_mL	Reaction_Peak_Area_mV	Elute_mL	Elute_Peak_Area_mV	CPM_Vol_mL	Yield_Vol_mL	Dilution	CPM	CiPerCPM_Calibration_Factor	Specific_Activity_CiPerMol")
}

###################################################################
## 2) Do Calculations
###################################################################
#First step get AverageMW value from ChemProductionID = MW
chemProd <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="samples",
    queryName="CHEMProduction",
	colNameOpt="fieldname",    
    colSelect=c("CHEMProductionID", "AverageMW")
)
inputDF$Recovered_Mg = ""
#Then calculate the Specific_Activity_CiPerMol value using the following formula.
for(i in 1:nrow(inputDF)){
	avgMW = as.numeric(chemProd$AverageMW[chemProd$CHEMProductionID == inputDF$ChemProductionID[i]][1])

	#simplified equations:
	#Recovered_Mg = InputProtein_mg * Elute_Peak_Area  * Elute_mL / (Reaction_Peak_Area_mV * Input_mL)
	#Specific_Activity_CiPerMol = CPM * CiPerCPM_Calibration_Factor * Yield_Vol_mL * Dilution * MW/ (CPM_Vol_mL * Recovered_Mg)

	inputDF$Recovered_Mg[i] <- as.numeric(inputDF$InputProtein_mg[i]) * as.numeric(inputDF$Elute_Peak_Area[i]) * as.numeric(inputDF$Elute_mL[i]) / (as.numeric(inputDF$Reaction_Peak_Area_mV[i]) / as.numeric(inputDF$Input_mL[i]))
	
	inputDF$Specific_Activity_CiPerMol[i] <- as.numeric(inputDF$CPM[i]) * as.numeric(inputDF$CiPerCPM_Calibration_Factor[i]) * as.numeric(inputDF$Yield_Vol_mL[i])  * as.numeric(inputDF$Dilution[i]) * avgMW
	inputDF$Specific_Activity_CiPerMol[i] <- inputDF$Specific_Activity_CiPerMol[i] / (as.numeric(inputDF$Recovered_Mg[i]) * as.numeric(inputDF$CPM_Vol_mL[i]))
}

###################################################################
## 3) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

