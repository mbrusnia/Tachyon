# Copyright [2018] [Mi-Youn Brusniak, Hector Ramos]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
	&& grepl("Elute_CPM", names(inputDF)[3]) && grepl("Elute_mL", names(inputDF)[4])
	&& grepl("Specific_Activity_uCiPerMg", names(inputDF)[5])){
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): ChemProductionID	InputProtein_mg	Input_mL Elute_CPM	Elute_mL Specific_Activity_uCiPerMg")
}

###################################################################
## 2) Do Calculations
###################################################################
# This is for non specific site C14 labelling. More specifically, C14 is labelled not 100% of all Lysine and N-termi site.
# The labeling is calculated based on A280 value upon reported MDT mg weight. For example, if MDT vial has 40mg. Dilute the sample to be ~1mg/mL and calculate A280.
# Based on the calculation ((((1/0.95)/2220000000000)*100*350)/0.000001) = 0.0165955429113324 generates uCi.
# 0.95, 100 are from dilution. 350 is from 3.5 ml protocol. 2220000000000 is conversion between CPM to DPM.

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
