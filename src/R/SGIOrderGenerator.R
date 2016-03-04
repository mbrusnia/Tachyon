###
### This R Transformation script for labkey 
###

options(stringsAsFactors = FALSE)
suppressWarnings(suppressMessages(require(Rlabkey)))

source("${srcDirectory}/Utils.R")

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
CONSTRUCT_QUERY_NAME = "Construct"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

#a hash to look up vector ids to vector names
Vector_hash <- list(VCR012 ="JMO84")  #this list will grow


${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, BASE_URL)

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else{ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")
}


##
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
previousConstructSequenceContents <- labkey.selectRows(BASE_URL, SAMPLE_SETS_FOLDER_PATH, SAMPLE_SETS_SCHEMA_NAME, CONSTRUCT_QUERY_NAME,
    viewName = NULL, colSelect = c(COMPOUND_ID_COL_NAME, SEQUENCE_COL_NAME), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

#find duplicates
matches <- match(inputDF[,SEQUENCE_COL_NAME], previousConstructSequenceContents[,SEQUENCE_COL_NAME])
rowsWithDuplicates <- which(!is.na(matches))
if(length(rowsWithDuplicates) > 0){
	cat("ERROR: No duplicate sequences allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(rowsWithDuplicates)){
		cat("Row ", rowsWithDuplicates[i] + 2, " - ID: ", inputDF[rowsWithDuplicates[i],COMPOUND_ID_COL_NAME], " - AASeq: ", inputDF[rowsWithDuplicates[i],SEQUENCE_COL_NAME], "\n")
	}
	stop("Please remove the duplicate sequences from your input file and try again.")
}

##prepare the table for insertion into the assay
names(inputDF) <- c("Name", "Sample Set", "Flag", "ConstructID", "ID", "Parent ID", "Alternate Name", "AASeq", "Vector")
inputDF <- inputDF[, c(1,4, 5, 8, 9)]

for(i in 1:length(inputDF[,1])){
	inputDF[i, "Vector"] <- Vector_hash[[inputDF[i, "Vector"]]]
}

## Insert data to Database
write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)