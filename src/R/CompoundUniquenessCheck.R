##
## This script verifies and enforces uniqueness among all uploaded
## peptide sequences into the specified assay
##

options(stringsAsFactors = FALSE)
suppressMessages(require(Rlabkey))
baseUrl<-"http://localhost:8080/labkey"
source("${srcDirectory}/Utils.R")

## These next four constants point to the data we wish to query from the DB
ASSAY_SCHEMA_NAME = "assay.General.Identified Compounds"
ASSAY_QUERY_NAME = "Data"
ASSAY_SEQUENCE_COL_NAME = "Sequence"
ASSAY_COMPOUND_ID_COL_NAME = "compoundID"

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

#check for duplicates in input data. if so, throw error
if(length(inputDF[,ASSAY_SEQUENCE_COL_NAME]) != length(unique(inputDF[,ASSAY_SEQUENCE_COL_NAME]))){
	cat("Error: There are sequences which appear multiple times in your input file.  Each sequence needs to be unique.\n")
	stop()
}

#check if the new sequences have previously been loaded into the database. if so, throw error
for(i in 1:length(inputDF[,ASSAY_SEQUENCE_COL_NAME])){
	if(inputDF[i,ASSAY_SEQUENCE_COL_NAME] %in% previousAssayResults[,ASSAY_SEQUENCE_COL_NAME]){ 
		cat("ERROR: No duplicates allowed. This sequence has previously been uploaded into the repository: ", inputDF[i,ASSAY_SEQUENCE_COL_NAME], "\n")
		stop() 
	}
}

