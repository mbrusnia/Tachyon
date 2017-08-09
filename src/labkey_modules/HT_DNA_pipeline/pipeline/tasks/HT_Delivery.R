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
#inputDF <- xlsxToR(file.choose(), header=FALSE)

##
## Extract only the plate data and its column headers from the file 
##
mynames <- inputDF[17, 1:11]
inputDF <- inputDF[18:(18 - 1 + 96),1:11]
names(inputDF) <- mynames


colHeaders <- names(inputDF)
if(!(grepl("Construct.*ID", colHeaders[1]) && grepl("Construct.*Name", colHeaders[2]) && grepl("Plate.*ID", colHeaders[3])
	&& grepl("Well.*Location", colHeaders[4]) && grepl("Concentration.*ng/uL", colHeaders[5])
	&& grepl("Volume.*uL", colHeaders[6]) && grepl("Total.*DNA.*ng", colHeaders[7])
	&& grepl("Vector", colHeaders[8]) && grepl("Resistance", colHeaders[9])
	&& grepl("Flanking.*Restriction.*Site", colHeaders[10]) && grepl("Sequence.*Verification", colHeaders[11]))){
	
	stop("This file does not conform to the expected format.  Please contact the administrator.")
}

#change HT headers to FHCRC Optides labkey sampleset headers
names(inputDF)[1:7] <- c("VendorOrderID", "ConstructID", "VendorPlateID", "WellLocation", "Concentration_ng_uL", "Volume_uL", "TotalDNA_ng") 

##get corresponding constructIDs from HT_DNA sampleSet  (i.e. previously ordered constructs)
##makeFilter
filterArr = c()
for(i in 1:length(inputDF[,1])){
	if(inputDF[i, "ConstructID"] != "Blank" && inputDF[i, "ConstructID"] != "CNT0001396"  && inputDF[i, "ConstructID"] != "TEST0001396" && inputDF[i, "ConstructID"] != "Control"){
		filterArr <- c(filterArr, c(inputDF[i, "ConstructID"]))
	}
}
filterS <- paste(filterArr, collapse=";")
	
ss <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_DNA_QUERY_NAME,
	colNameOpt="fieldname",
	showHidden=TRUE,
	colSelect=c("Name", "DNAID", "VendorOrderID", "ConstructID", "VendorPlateID", "WellLocation", "Concentration_ng_uL", "Volume_uL", "TotalDNA_ng", "AASeq"),
	colFilter=makeFilter(c("ConstructID", "IN", filterS))
)	

##make sure all of currently inputed Construct ID's were found in the database. if not, throw error
matches <- match(filterArr, ss$ConstructID)
if(length(matches[is.na(matches)]) > 0){
	nonMatchingRows <- which(is.na(matches))
	cat(length(nonMatchingRows), "incoming IDs were not found to be previously existing in our database.  They are the following:\n")
	for(i in 1:length(nonMatchingRows)){
		cat(filterArr[nonMatchingRows[i]], "\n")
	}
	stop("No updates were made.  Please correct this problem and try again.")
}

#append data from the input to the existing rows in HT_DNA
for(i in 1:length(ss$ConstructID)){
	thisRow <- inputDF[inputDF[,"ConstructID"] == ss$ConstructID[i], ]
	ss$VendorOrderID[i] <- thisRow[["VendorOrderID"]]
	ss$VendorPlateID[i] <- thisRow[["VendorPlateID"]]
	ss$WellLocation[i] <- thisRow[["WellLocation"]]
	ss$Concentration_ng_uL[i] <- thisRow[["Concentration_ng_uL"]]
	ss$Volume_uL[i] <- thisRow[["Volume_uL"]]
	ss$TotalDNA_ng[i] <- thisRow[["TotalDNA_ng"]]
}

#update SGI_DNA Database/SampleSet
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
