###
### This Labkey Transformation script will do 3 things upon insertion of new
### peptide sequences into the InSilicoAssay table:
###	1) Make sure there is only 1 HTPlateID listed in all of the incoming files (check first 6 characters of filenames)
###	2) From the filenames, parse out the HTProductionID, Classification, and FileLocation
###	3) Make sure all files exist and are where we expect them to be
###	4) Save the HTProductionID, Classification, Image, and HTPlateID to the HPLC Assay table
###

options(stringsAsFactors = FALSE)

source("${srcDirectory}/Utils.R")

#Parameters for this script 
BASE_URL = "http://optides-prod.fhcrc.org/"
IMAGE_WEBDAV_URL = paste0(BASE_URL, "_webdav/Optides/HTProduction/Assays/%40files/HPLC_Assay_Images/")
HARD_DRIVE_PATH = "G:/labkey/labkey/files/Optides/HTProduction/Assays/@files/HPLC_Assay_Images/"

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, BASE_URL)

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=FALSE)
}else{ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = FALSE, sep = "\t")
}


#################################################################################
## 1) Make sure there is only one HT run specified in the incoming filenames
#################################################################################
HTPlateID <- unique(substr(inputDF[,1],1,6))
if(length(HTPlateID) > 1){
	cat("ERROR: Only one HTPlateID permitted to be inserted at a time.  Your file contains multiple HTPlateIDs: \n")
	cat(HTPlateID, "\n")
	stop("Please modify your input to include only one HTPlateID and try again.")
}

###########################################################################################################
## 2) Parse filenames up into new Dataframe for insertion into assay table
###########################################################################################################

parseFilename <- function(filename){
	HTProductionID <- unlist(strsplit(filename , "[_.]"))[1]
	Classification <- unlist(strsplit(filename , "[_.]"))[2]
	HTPlateID <- substr(HTProductionID, 1, 6)
	Image <- paste0(HARD_DRIVE_PATH, HTPlateID, "/", filename)
	df <- data.frame(HTProductionID, Classification, Image)
	return (df)
}

outputDF <- parseFilename(inputDF[1,])
for(i in 2:length(inputDF[,1])){
	outputDF <- rbind(outputDF, parseFilename(inputDF[i,]))
}

#############################################################################################################
## 3) Make sure all the files exists where we expect them to exist (HTProduction/Assays/Images/(HTPlateID)/ )
#############################################################################################################
fail <- FALSE
failures <- c()
for(i in 1:length(outputDF[,3])){
	if(!file.exists(outputDF[i,3])){
		fail <- TRUE
		failures <- c(failures, outputDF[i,3])
	}
}

if(fail){
	cat("The following files are not found in their expected location (HTProduction/Assays/Images/(HTPlateID)/).  Please place them there and try again.\n")
	for(i in 1:length(failures)){
		cat(failures[i], "\n")
	}
	stop("Please upload the files to the proper directory and try again.")
}

###################################################################
## 4) Insert data to Database
###################################################################
write.table(outputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)