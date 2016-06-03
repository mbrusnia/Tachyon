###
### This Labkey Transformation script will do the following upon insertion of new
### peptide sequences into the Novocyte Assay table:
###	1) Combine (concatenate) the "Specimen" with the "Sample" column values
###		to produce the "HTProductionID" column
### 2) write to a table so that it can be imported through labkey into an assay table
###	
options(stringsAsFactors = FALSE)

source("${srcDirectory}/Utils.R")

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-prod.fhcrc.org/"

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, BASE_URL)

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "csv"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = ",", check.names=FALSE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "tsv"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t", check.names=FALSE)
}



###################################################################
## 1) Format column headers
###################################################################
if(grepl("Plate ID", names(inputDF)[1]) && grepl("Well ID", names(inputDF)[2])
	&& grepl("Specimen", names(inputDF)[3]) && grepl("Sample", names(inputDF)[4])
	&& grepl("Parent", names(inputDF)[5]) && grepl("Median", names(inputDF)[6])
	&& grepl("P1", names(inputDF)[7]) && grepl("R2", names(inputDF)[8])
	&& grepl("Run Time", names(inputDF)[9]) && grepl("Abs. Count", names(inputDF)[10])){	
	1==1
}else{
	stop("This file does not conform to the expected format.  Please contact the administrator.")
}

names(inputDF) = c("PlateID", "WellID", "Specimen", "Sample", "M3_Percent_Parent", "M3_Median_FITC_H", "P1_percent_All", "R2_percent_Parent", "Run_Time", "R2_Abs_Count")

##
## 1) a. Create HTProductionID and append to the data
##

HTProductionID = paste0(inputDF$Specimen, inputDF$Sample)

inputDF <- cbind(inputDF, HTProductionID)


###################################################################
## 2) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)
