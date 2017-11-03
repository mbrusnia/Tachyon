#This is a Labkey Tranformation script to be added to the LSC assay
#in the folder: /Optides/VIVOAssay/Sample/

options(stringsAsFactors = FALSE)
library(Rlabkey)

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
if(grepl("MouseID", names(inputDF)[1]) && grepl("CompoundID", names(inputDF)[2])
	&& grepl("Tissue", names(inputDF)[3]) && grepl("AcquisitionDate", names(inputDF)[4])
	&& grepl("Tissue_mg", names(inputDF)[5]) && grepl("mg_per_ul", names(inputDF)[6])
	&& grepl("Loaded_Volume_uL", names(inputDF)[7]) && grepl("CPM", names(inputDF)[8])
	&& grepl("Loading_mg", names(inputDF)[9]) && grepl("pCi", names(inputDF)[10])
	&& grepl("pCi_per_uL", names(inputDF)[11]) && grepl("Flag", names(inputDF)[12])	){	
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): MouseID	CompoundID	Tissue	AcquisitionDate	Tissue_mg	mg_per_ul	Loaded_Volume_uL	CPM	Loading_mg	pCi	pCi_per_uL	Flag")
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
inputDF$Loading_mg <- round(as.numeric(inputDF$Loaded_Volume_uL) * as.numeric(inputDF$mg_per_ul), digit=2)
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

