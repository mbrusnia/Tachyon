options(stringsAsFactors = FALSE)
suppressWarnings(suppressMessages(require(Rlabkey)))
suppressWarnings(suppressMessages(require(Peptides)))
suppressWarnings(suppressMessages(require(stringr)))

baseUrl<-"http://optides-stage.fhcrc.org/"
source("${srcDirectory}/Utils.R")

## These next four constants point to the data we wish to query from the DB
ASSAY_SCHEMA_NAME = "assay.General.InSilicoAssay"
ASSAY_QUERY_NAME = "Data"
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
SAMPLES_SCHEMA = "Samples"
COMPOUND_TABLE = "Construct"
COMPOUND_FOLDER = "/Optides/CompoundsRegistry/Samples"

H_ISO_MASS = 1.0078250

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

## get all previously uploaded sequences
previousAssayResults <- labkey.selectRows(baseUrl, "experiment/Optides/CompoundsRegistry/Samples/", "Samples", "Construct",
    viewName = NULL, colSelect = c(COMPOUND_ID_COL_NAME, SEQUENCE_COL_NAME), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

## read the input data frame just to get the column headers.
inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")

##
## check for duplicates in input data. if so, list the row and sequence, then throw error
##
duplicates = duplicated(inputDF[,SEQUENCE_COL_NAME])
if(length(duplicates[duplicates == TRUE]) > 0){
	cat("ERROR: No duplicates allowed. Your input file contains the following duplicate sequences: \n")
	for(i in 1:length(duplicates)){
		if(duplicates[i]){
			cat("ID: ", inputDF[i,COMPOUND_ID_COL_NAME], ": ", inputDF[i,SEQUENCE_COL_NAME], "\n")
		}
	}
	stop("Please remove the duplicates from your input file and try again.")
}


##
## check if the new sequences have previously been loaded into the database. 
## if so, list the rows and sequences of the repeated sequences
##
matches <- match(previousAssayResults[,SEQUENCE_COL_NAME],inputDF[,SEQUENCE_COL_NAME])
matches <- matches[!is.na(matches)]
if(length(matches) > 0){
	cat("ERROR: No duplicates allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(matches)){
		cat("ID: ", inputDF[matches[i],COMPOUND_ID_COL_NAME], ": ", inputDF[matches[i],SEQUENCE_COL_NAME], "\n")
	}
	stop("Please remove the duplicates from your input file and try again.")
}


##
##calculate average mass, monoisotopic mass, and pI
##
toinsert <- inputDF
for (i in 1:length(toinsert[,SEQUENCE_COL_NAME])){
	Cs <- floor(str_count(inputDF[i, SEQUENCE_COL_NAME], "C")/2) * 2
	toinsert$AverageMass[i] <- mw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE) - Cs * H_ISO_MASS
	toinsert$MonoisotopicMass[i] <- mw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE) - Cs * H_ISO_MASS
	toinsert$pI[i] <- pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS")
}


#insert input into compound registry sample set
labkey.insertRows(baseUrl, COMPOUND_FOLDER, SAMPLES_SCHEMA, COMPOUND_TABLE, inputDF)

#insert input into assay
write.table(toinsert,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

#stop("Please remove the duplicates from your input file and try again.")

