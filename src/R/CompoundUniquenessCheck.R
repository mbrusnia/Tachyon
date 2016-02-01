##
## This script verifies and enforces uniqueness among all uploaded
## peptide sequences into the specified assay
##

options(stringsAsFactors = FALSE)
suppressMessages(require(Rlabkey))
baseUrl<-"http://optides.fhcrc.org:8080/labkey"
source("${srcDirectory}/Utils.R")

## These next four constants point to the data we wish to query from the DB
ASSAY_SCHEMA_NAME = "Samples"
ASSAY_QUERY_NAME = "Data"
ASSAY_SEQUENCE_COL_NAME = "AA_Sequence"
ASSAY_COMPOUND_ID_COL_NAME = "KnottinID"

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

## get all previously uploaded sequences
previousAssayResults <- labkey.selectRows(baseUrl, params$containerPath, ASSAY_SCHEMA_NAME, ASSAY_QUERY_NAME,
    viewName = NULL, colSelect = c(ASSAY_COMPOUND_ID_COL_NAME , ASSAY_SEQUENCE_COL_NAME ), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

## read the input data frame just  to get the column headers.
inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")

##
## check for duplicates in input data. if so, list the row and sequence, then throw error
##
duplicates = duplicated(inputDF[,ASSAY_SEQUENCE_COL_NAME])
if(length(duplicates[duplicates == TRUE]) > 0){
	cat("ERROR: No duplicates allowed. Your input file contains the following duplicated sequences: \n")
	for(i in 1:length(duplicates)){
		if(duplicates[i]){
			cat("row ", i, ": ", inputDF[i,ASSAY_SEQUENCE_COL_NAME], "\n")
		}
	}
	stop("Please remove the duplicates from your input file and try again.")
}

##
## check if the new sequences have previously been loaded into the database. 
## if so, list the rows and sequences of the repeated sequences
##
matches <- match(previousAssayResults[,ASSAY_SEQUENCE_COL_NAME],inputDF[,ASSAY_SEQUENCE_COL_NAME])
matches <- matches[!is.na(matches)]
if(length(matches) > 0){
	cat("ERROR: No duplicates allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(matches)){
		cat("row ", matches[i], ": ", inputDF[matches[i],ASSAY_SEQUENCE_COL_NAME], "\n")
	}
	stop("Please remove the duplicates from your input file and try again.")
}
