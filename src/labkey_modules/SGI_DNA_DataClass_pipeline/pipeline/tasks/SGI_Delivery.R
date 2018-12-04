###
### This is an R script for a labkey pipeline import feature
###


options(stringsAsFactors = FALSE)
library(Rlabkey)

source("${srcDirectory}/xlsxToR.R")
source("${srcDirectory}/Utils.R")
pathToInputFile <- "${input.xlsx}"

#Parameters for this script (login script: _netrc)
BASE_URL = "https://optides-stage.fhcrc.org/"

DATA_CLASS_SCHEMA_NAME = "exp.data"
SGI_DNA_QUERY_NAME = "SGI_DNA"
DATA_CLASS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

## read the input
inputDF <- xlsxToR(pathToInputFile, header=TRUE)

#skip 18 rows
#inputDF <- inputDF[18:, ]

colHeaders <- names(inputDF)
#if(!("Construct ID" %in% colHeaders && "Vector" %in% colHeaders && "SGI ID" %in% colHeaders &&
#	"Plate ID" %in% colHeaders && "Well Location" %in% colHeaders &&
#	"Concentration (ng/uL)" %in% colHeaders && "Volume (uL)" %in% colHeaders &&
#	"Total DNA (ng)" %in% colHeaders && "AA Seq" %in% colHeaders && "DNA Seq" %in% colHeaders && 
#	"Comment" %in% colHeaders)){
	
#	stop("The input file format detected is incompatible with this operation.  Please contact the administrator.")
#}

if(colHeaders[1] != "Plate ID" || colHeaders[2] != "Construct ID" ||
	colHeaders[3] != "Well ID" || colHeaders[4] != "DNA Concentration" ||
	colHeaders[5] != "AASeq" || colHeaders[6] != "DNASeq" ||
	colHeaders[7] != "Comment"){
	
	stop("The input file format detected is incompatible with this operation.  Please contact the administrator.")
}

#re-format the PlateID and WellLocation fields
inputDF[, "Plate ID"] <- sub("Plate_", "", inputDF[, "Plate ID"])
for( i in 1:length(inputDF[, "Plate ID"])){
	if(nchar(inputDF[i, "Well ID"]) == 2){
		inputDF[i, "Well ID"] = paste0(substr(inputDF[i, "Well ID"], 1, 1), "0", substr(inputDF[i, "Well ID"], 2, 2))
	}
	if(nchar(inputDF[i, "Plate ID"]) == 1){
		inputDF[i, "Plate ID"] = paste0("000", inputDF[i, "Plate ID"])
	}else if(nchar(inputDF[i, "Plate ID"]) == 2){
		inputDF[i, "Plate ID"] = paste0("00", inputDF[i, "Plate ID"])
	}else if(nchar(inputDF[i, "Plate ID"]) == 3){
		inputDF[i, "Plate ID"] = paste0("0", inputDF[i, "Plate ID"])
	}
}


##get corresponding constructIDs from SGI_DNA sampleSet  (i.e. previously ordered constructs)
##makeFilter
filterArr = c()
for(i in 1:length(inputDF[,1])){
	if(is.na(inputDF[i, "Comment"]) || (inputDF[i, "Construct ID"] != "Blank" && inputDF[i, "Comment"] != "Leave well blank" && inputDF[i, "Comment"] != "Control")){
		filterArr <- c(filterArr, c(inputDF[i, "Construct ID"]))
	}
}
filterS <- paste(filterArr, collapse=";")
	
ss <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=DATA_CLASS_FOLDER_PATH,
	schemaName=DATA_CLASS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	showHidden=TRUE,
	colNameOpt="fieldname",
	colSelect=c("RowId", "Name", "ConstructID", "Comments", "DNAConcentration", "DNASeq", "PlateID", "WellID"),
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
	thisRow <- inputDF[inputDF[,"Construct ID"] == ss$ConstructID[i], ]
	ss$DNASeq[i] <- thisRow[["DNASeq"]]
	ss$PlateID[i] <- thisRow[["Plate ID"]]
	ss$WellID[i] <- thisRow[["Well ID"]]
	ss$DNAConcentration[i] <- thisRow[["DNA Concentration"]]
	if(is.na(thisRow[["Comment"]])){
		ss$Comments[i] <- ""
	}else{
		ss$Comments[i] <- thisRow[["Comment"]]
	}
}

#update SGI_DNA Database/SampleSet
ssu <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=DATA_CLASS_FOLDER_PATH,
	schemaName=DATA_CLASS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toUpdate=ss
)

#completed
cat(length(ss$DNASeq), "RECORDS HAVE BEEN UPDATED IN SGI_DNA.\n")
