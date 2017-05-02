###
### This is an R script for a labkey pipeline import feature
###
### SGI_Delivery.R - Parse and insert an SGI Delivery xlsx file
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
SGI_DNA_QUERY_NAME = "SGI_DNA"
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
if(grepl("Construct.*ID", colHeaders[1]) && grepl("Construct.*Name", colHeaders[2]) && grepl("Plate.*ID", colHeaders[3])
	&& grepl("Well.*Location", colHeaders[4]) && grepl("Concentration.*ng/uL", colHeaders[5])
	&& grepl("Volume.*uL", colHeaders[6]) && grepl("Total.*DNA.*ng", colHeaders[7])
	&& grepl("Vector", colHeaders[8]) && grepl("Resistance", colHeaders[9])
	&& grepl("Flanking.*Restriction.*Site", colHeaders[10]) && grepl("Sequence.*Verification", colHeaders[11])){	
	1==1
}else{
	stop("This file does not conform to the expected format.  Please contact the administrator.")
}

#change SGI headers to FHCRC Optides labkey sampleset headers
names(inputDF)[1:7] <- c("SGIID", "ConstructID", "SGIPlateID", "WellLocation", "Concentration_ngPeruL", "Volume_uL", "TotalDNA_ng") 

##get corresponding constructIDs from SGI_DNA sampleSet  (i.e. previously ordered constructs)
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
	queryName=SGI_DNA_QUERY_NAME,
	colNameOpt="fieldname",
	showHidden=TRUE,
	colSelect=c("Name", "SGIID", "ConstructID", "SGIPlateID", "WellLocation", "Concentration_ngPeruL", "Volume_uL", "TotalDNA_ng", "AASeq"),
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

#append data from the input to the existing rows in SGI_DNA
for(i in 1:length(ss$ConstructID)){
	thisRow <- inputDF[inputDF[,"ConstructID"] == ss$ConstructID[i], ]
	ss$SGIID[i] <- thisRow[["SGIID"]]
	ss$SGIPlateID[i] <- thisRow[["SGIPlateID"]]
	ss$WellLocation[i] <- thisRow[["WellLocation"]]
	ss$Concentration_ngPeruL[i] <- thisRow[["Concentration_ngPeruL"]]
	ss$Volume_uL[i] <- thisRow[["Volume_uL"]]
	ss$TotalDNA_ng[i] <- thisRow[["TotalDNA_ng"]]
}

#update SGI_DNA Database/SampleSet
ssu <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toUpdate=ss
)

if(!exists("ssu")){
	stop("There was a problem with the database update.  Please contact the administrator.")
}else{
	#completed
	cat(length(ss$SGIID), "RECORDS HAVE BEEN UPDATED IN SGI_DNA.\n")
}
