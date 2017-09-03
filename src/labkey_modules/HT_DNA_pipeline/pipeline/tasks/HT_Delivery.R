###
### This is an R script for a labkey pipeline import feature
###
### HT_Delivery.R - Parse and insert an HT Delivery xlsx file
###


options(stringsAsFactors = FALSE)
library(Rlabkey)

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")
source("C:/labkey/labkey/files/Optides/@files/Utils.R")
pathToInputFile <- "${input.xlsx}"

jobInfoFile <- sub("..", "../", "${pipeline, taskInfo}", perl=TRUE)
jobInfo <- read.table(jobInfoFile,
					  col.names=c("name", "value", "type"),
					  header=FALSE, check.names=FALSE,
					  stringsAsFactors=FALSE, sep="\t", quote="",
					  fill=TRUE, na.strings="")
					  
					  
#Parameters for this script (login script: _netrc)
BASE_URL <- jobInfo$value[ grep("baseUrl", jobInfo$name)]

SAMPLE_SETS_SCHEMA_NAME = "Samples"
HT_DNA_QUERY_NAME = "HT_DNA"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

## read the input
inputDF <- xlsxToR(pathToInputFile, header=FALSE)

##
## Extract only the plate data and its column headers from the file 
##
mynames <- inputDF[1, 1:7]
inputDF <- inputDF[2:(1 + 96),1:7]

## set colnames and rownames
names(inputDF) <- mynames
rownames(inputDF ) <- seq(length=nrow(inputDF )) 

colHeaders <- names(inputDF)
if(!(grepl("Plate", colHeaders[1]) && grepl("Well.*Location", colHeaders[2]) && grepl("Order.*ID", colHeaders[3])
	&& grepl("name", colHeaders[4]) && grepl("Vector", colHeaders[5]) && grepl("Sequence Validation", colHeaders[6])
	&& grepl("DNA.*amount", colHeaders[7]))){
	
	stop("This file does not conform to the expected format.  Please contact the administrator.")
}

inputDF$Vector <- gsub("-GFP", "", inputDF$Vector)
inputDF$Vector[inputDF$Vector == "blank"] = ""

#change HT headers to FHCRC Optides labkey sampleset headers
names(inputDF)[1:7] <- c("VendorPlateID", "WellLocation", "VendorOrderID", "ConstructID", "Vector", "SequenceValidation", "TotalDNA_ng") 

##ensure wellLocations are 3 characters long
well_locations_list <- strsplit(inputDF$WellLocation, "")
for(i in 1:96){
	if(length(well_locations_list[[i]]) == 2){
		inputDF$WellLocation[i] <- paste0(well_locations_list[[i]][1], "0", well_locations_list[[i]][2])
	}
}

#remove units if ng.  if ug, multiply by 1000
inputDF$TotalDNA_ng <- gsub("ng", "", inputDF$TotalDNA_ng)

ug_matches <- grep("ug", inputDF$TotalDNA_ng)
if(length(ug_matches) > 0){
	for(i in 1:length(ug_matches)){
		inputDF$TotalDNA_ng[ug_matches[i]] <- gsub("ug", "", inputDF$TotalDNA_ng[ug_matches[i]])
		inputDF$TotalDNA_ng[ug_matches[i]] <- as.numeric(inputDF$TotalDNA_ng[ug_matches[i]]) * 1000
	}
}


##get corresponding (constructID, Vector) pairs from HT_DNA sampleSet  (i.e. previously ordered (construct,vector))
##makeFilter
filterIDArr = c()
filterVectorArr = c()
for(i in 1:length(inputDF[,1])){
	if(inputDF[i, "ConstructID"] != "empty" && inputDF[i, "ConstructID"] != "Blank" 
		&& inputDF[i, "ConstructID"] != "CNT0001396"  && inputDF[i, "ConstructID"] != "TEST0001396" 
		&& inputDF[i, "ConstructID"] != "Control" && inputDF[i, "ConstructID"] != "blank"){
		filterIDArr <- c(filterIDArr , c(inputDF[i, "ConstructID"]))
		filterVectorArr <- c(filterVectorArr, c(inputDF[i, "Vector"]))
	}
}
filterIDS <- paste(unique(filterIDArr), collapse=";")
filterVectorS <- paste(unique(filterVectorArr), collapse=";")
	
ss <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_DNA_QUERY_NAME,
	colNameOpt="fieldname",
	showHidden=TRUE,
	colSelect=c("RowId", "Name", "DNAID", "VendorOrderID", "ConstructID", "VendorPlateID", "WellLocation", "Concentration_ng_uL", "Volume_uL", "TotalDNA_ng", "AASeq", "Vector"),
	colFilter=makeFilter(c("ConstructID", "IN", filterIDS), c("Vector", "IN", filterVectorS))
)	
  
##make sure all of currently inputed (ConstructID, Vector) pairs were found in the database. if not, throw error
matches <- match(paste0(filterIDArr, "_", filterVectorArr), paste0(ss$ConstructID, "_", ss$Vector))
if(length(matches[is.na(matches)]) > 0){
	nonMatchingRows <- which(is.na(matches))
	cat(length(nonMatchingRows), "incoming (ID, Vector) pairs were not found to be previously existing in our database.  They are the following:\n")
	for(i in 1:length(nonMatchingRows)){
		cat(filterIDArr[nonMatchingRows[i]], " - ", filterVectorArr[nonMatchingRows[i]], "\n")
	}
	stop("No updates were made.  Please correct this problem and try again.")
}

#append data from the input to the existing rows in HT_DNA
for(i in 1:length(ss$ConstructID)){
	thisRow <- inputDF[inputDF[,"ConstructID"] == ss$ConstructID[i], ]
	if(ss$Vector[i] == thisRow[["Vector"]]){
		ss$VendorOrderID[i] <- thisRow[["VendorOrderID"]]
		ss$VendorPlateID[i] <- thisRow[["VendorPlateID"]]
		ss$WellLocation[i] <- thisRow[["WellLocation"]]
		#ss$Concentration_ng_uL[i] <- thisRow[["Concentration_ng_uL"]]
		#ss$Volume_uL[i] <- thisRow[["Volume_uL"]]
		ss$TotalDNA_ng[i] <- thisRow[["TotalDNA_ng"]]
	}
}

#update HT_DNA Database/SampleSet
ssu <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_DNA_QUERY_NAME,
	toUpdate=ss
)

if(!exists("ssu")){
	stop("There was a problem with the database update.  Please contact the administrator.")
}else{
	#completed
	cat(length(ss$DNAID), "RECORDS HAVE BEEN UPDATED IN HT_DNA.\n")
}
