#This is a Labkey Tranformation script to be added to the RadioLabeledConjugate assay
#in the folder: /Optides/ChemProduction/Assay/

options(stringsAsFactors = FALSE)
library(Rlabkey)

source("${srcDirectory}/Utils.R")

${rLabkeySessionId}
rpPath<- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath)

BASE_URL<- params$baseUrl

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
	&& grepl("Elute_Peak_Area_mV", names(inputDF)[7])
	&& grepl("CPM", names(inputDF)[8])){
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): ChemProductionID	InputProtein_mg	Input_mL	Wash_mL	Reaction_Peak_Area_mV	Elute_mL	Elute_Peak_Area_mV	CPM")
}

###################################################################
## 2) Do Calculations
###################################################################
#
#First step get conversion factor
standardsList <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/VIVOAssay/Sample",
    schemaName="lists",
    queryName="LSCpCiConversionFactor"
)

#Next, step get AverageMW value from ChemProductionID = MW
chemProd <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="samples",
    queryName="CHEMProduction",
	colNameOpt="fieldname",    
    colSelect=c("CHEMProductionID", "AverageMW")
)
#Then calculate the Specific_Activity_CiPerMol value using the following formula.
# Protocol based constant values mL volumne for CPM meaurement, Dilution volumn
CPM_VOL_ML = 0.01
inputDF$Recovered_Mg = -1.0
inputDF$Recovered_uMol = -1.0
DILUTION = 100
# Note that Calibration will generate pCi from measured CPM. Thus final pCi/pMol should be in inputDF$Specific_Activity_CiPerMol, Thus, converting mw to pMol
for(i in 1:nrow(inputDF)){
	if(is.na(chemProd$AverageMW[chemProd$CHEMProductionID == inputDF$ChemProductionID[i]][1])){
		stop(paste0("The average molecular weight for ", inputDF$ChemProductionID[i], " is not loaded into the CHEMProduction sampleset.  Please fix this and try again."))
	}
	
	avgMW = as.numeric(chemProd$AverageMW[chemProd$CHEMProductionID == inputDF$ChemProductionID[i]][1])
	
	if(avgMW < 0){
		stop(paste0("The loaded (in sample set CHEMProduction) average molecular weight value for ", inputDF$ChemProductionID[i], " is ", avgMW, " which is an invalid value (it's negative).  Please fix this and try again."))
	}
	pCiPerCPM_Calibration_Factor = (as.numeric(inputDF$CPM[i]) - as.numeric(standardsList[standardsList[,"Version"] == params$standardCurve, "YIntercept"]))/standardsList[standardsList[,"Version"] == params$standardCurve, "Slope"]
	inputDF$Recovered_Mg[i] <-round(as.numeric(inputDF$InputProtein_mg[i]) * as.numeric(inputDF$Elute_Peak_Area[i]) * as.numeric(inputDF$Elute_mL[i]) / (as.numeric(inputDF$Reaction_Peak_Area_mV[i])*as.numeric(inputDF$Input_mL[i])), digits=1)
	inputDF$Specific_Activity_CiPerMol[i] <- as.numeric(pCiPerCPM_Calibration_Factor) * (as.numeric(inputDF$Elute_mL[i]) / CPM_VOL_ML) * DILUTION
	inputDF$Specific_Activity_CiPerMol[i] <- round(inputDF$Specific_Activity_CiPerMol[i] / (1.0E12 * (as.numeric(inputDF$Recovered_Mg[i]*1.0E-3) / avgMW)), digits=0)
	inputDF$Recovered_uMol[i] <- prettyNum((inputDF$Recovered_Mg[i] * 1E-3 /avgMW) * 1E6, digits=4)
}

###################################################################
## 3) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

