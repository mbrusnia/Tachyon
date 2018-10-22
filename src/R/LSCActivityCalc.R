#This is a Labkey Tranformation script to be added to the LSC assay
#in the folder: /Optides/VIVOAssay/Sample/

options(stringsAsFactors = FALSE)
library(Rlabkey)
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

source("${srcDirectory}/Utils.R")

# Constants
LOD <- 30.0
LOQ <- 340.0

${rLabkeySessionId}
rpPath<- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath)

BASE_URL <- params$baseUrl

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
if(grepl("MouseID", names(inputDF)[1]) && grepl("OTDCompoundID", names(inputDF)[2])
	&& grepl("CHEMCompoundID", names(inputDF)[3]) && grepl("ReagentID", names(inputDF)[4])
	&& grepl("Tissue", names(inputDF)[5]) && grepl("AcquisitionDate", names(inputDF)[6])
	&& grepl("Tissue_mg", names(inputDF)[7]) && grepl("mg_per_ul", names(inputDF)[8])
	&& grepl("Loaded_Volume_uL", names(inputDF)[9]) && grepl("CPM", names(inputDF)[10])
	&& grepl("Loaded_mg", names(inputDF)[11]) && grepl("pCi", names(inputDF)[12])
	&& grepl("pCi_per_uL", names(inputDF)[13]) && grepl("Flag", names(inputDF)[14])){	
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): MouseID	OTDCompoundID	CHEMCompoundID	ReagentID	Tissue	AcquisitionDate	Tissue_mg	mg_per_ul	Loaded_Volume_uL	CPM	Loading_mg	pCi	pCi_per_uL	Flag")
}

#
#First step get AverageMW value from ChemProductionID = MW
standardsList <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/VIVOAssay/Sample",
    schemaName="lists",
    queryName="LSCpCiConversionFactor"
)

inputDF$pCi <- round((as.numeric(inputDF$CPM) - as.numeric(standardsList[standardsList[,"Version"] == params$standardCurve, "YIntercept"]))/as.numeric(standardsList[standardsList[,"Version"] == params$standardCurve, "Slope"]), digit=2)
inputDF$Loaded_mg <- round(as.numeric(inputDF$Loaded_Volume_uL) * as.numeric(inputDF$mg_per_ul), digit=2)
inputDF$pCi_per_uL <- round(as.numeric(inputDF$pCi) / as.numeric(inputDF$Loaded_Volume_uL), digit=2)
for(i in 1:length(inputDF$CPM)){
	if(as.numeric(inputDF$CPM[i]) <= LOD){
   		inputDF$Flag[i] <- "lower than LOD"
	} else if (as.numeric(inputDF$CPM[i]) <= LOQ){
   		inputDF$Flag[i] <- "lower than LOQ"
	}
}

###################################################################
## 2) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

