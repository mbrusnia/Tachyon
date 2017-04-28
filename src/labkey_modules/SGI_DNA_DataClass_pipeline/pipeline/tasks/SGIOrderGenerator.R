###
### This is an R script for a labkey pipeline import feature
###

options(stringsAsFactors = FALSE)
library(Rlabkey)

pathToInputFile <- "${input.xlsx}"

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"

SEQUENCE_COL_NAME = "AASeq"
CONSTRUCT_ID_COL_NAME = "ConstructID"

DATA_CLASSES_SCHEMA_NAME = "exp.data"
SGI_DNA_QUERY_NAME = "SGI_DNA"
DATA_CLASSES_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

#a hash to look up vector ids to vector names  #this list will grow
Vector_hash <- list(VCR010="RKS017", VCR011="RSK056", VCR012="RKS017", VCR020 ="JMO084", VCR21="JMO084", VCR30="JMO300", VCR040="MDT208", VCR050="Elafin", VCR000="Unavailable")  


## read the input
inputDF <- xlsxToR(pathToInputFile, header=TRUE)

colHeaders <- names(inputDF)
if(colHeaders[1] != "Name" || colHeaders[2] != "Sample Set" ||
	colHeaders[3] != "Flag" || colHeaders[4] != "ID" ||
	colHeaders[5] != "Parent ID" || colHeaders[6] != "Parent ID" ||
	colHeaders[7] != "Alternate Name" || colHeaders[8] != "AASeq" ||
	colHeaders[9] != "Vector"){
	
	stop("The input file format detected is incompatible with this operation.  Please contact the administrator.")
}

## Get the Linker sequence and remove it from all the sequences
linker <- inputDF[1,SEQUENCE_COL_NAME]
inputDF <- inputDF[2:length(inputDF[,SEQUENCE_COL_NAME]),]
for(i in 1:length(inputDF[,SEQUENCE_COL_NAME])){
	inputDF[i, SEQUENCE_COL_NAME] = sub(linker, "", inputDF[i, SEQUENCE_COL_NAME])
}

##
## check if the new sequences have previously been loaded into the database. 
## if so, list the rows and sequences of the repeated sequences
##

## get all previously uploaded sequences
previousSGI_DNASequenceContents <- labkey.selectRows(BASE_URL, DATA_CLASSES_FOLDER_PATH, DATA_CLASSES_SCHEMA_NAME, SGI_DNA_QUERY_NAME,
    viewName = NULL, colSelect = c(CONSTRUCT_ID_COL_NAME, SEQUENCE_COL_NAME), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,colFilter=NULL, showHidden = FALSE, colNameOpt="fieldname",
    containerFilter=NULL)

#find duplicates
matches <- match(inputDF[,SEQUENCE_COL_NAME], previousSGI_DNASequenceContents[,SEQUENCE_COL_NAME])
rowsWithDuplicates <- which(!is.na(matches))
if(length(rowsWithDuplicates) > 0){
	cat("ERROR: No duplicate sequences allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(rowsWithDuplicates)){
		cat("Row ", rowsWithDuplicates[i] + 2, " - ID: ", inputDF[rowsWithDuplicates[i],CONSTRUCT_ID_COL_NAME], " - AASeq: ", inputDF[rowsWithDuplicates[i],SEQUENCE_COL_NAME], "\n")
	}
	stop("Please remove the duplicate sequences from your input file and try again.")
}

##prepare the table for insertion into the assay
names(inputDF) <- c("Name", "Sample Set", "Flag", "ConstructID", "ID", "Parent ID", "Alternate Name", "AASeq", "Vector")
inputDF <- inputDF[, c(1,4, 5, 8, 9)]

for(i in 1:length(inputDF[,1])){
	#no blank allowed as well as no unknown
	if(is.na(inputDF[i, "Vector"])){
		cat("There is a blank value entered for a Vector value on row ", i, ".  This is not allowed.  Please fix this and try again.")
		stop()
	}
	if(is.null(Vector_hash[[inputDF[i, "Vector"]]])){
		stop(paste("An invalid Vector has been specified in your input.  This Vector value is invalid: ", inputDF[i, "Vector"]))
	}
	inputDF[i, "Vector"] <- Vector_hash[[inputDF[i, "Vector"]]]
}


#insert into DB
ssi <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=DATA_CLASSES_FOLDER_PATH,
	schemaName=DATA_CLASSES_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toInsert=inputDF
)

#completed
cat(length(inputDF$AASeq), " RECORDS HAVE BEEN inserted.\n")
