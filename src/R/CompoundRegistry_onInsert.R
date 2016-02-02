###
### This Labkey Transformation script will do 4 things upon insertion of new
### peptide sequences into any of the 3 compound Registry Assay tables:
### 	1) Will verify the ParentID's of all incoming sequences (that they are valid)
###	2) Will make sure than non of the incoming sequences are already in the database
###	3) Will calculate AverageMass, MonoisotopicMass, and pI for each sequence
###	4) Will save all the data as follows:
###		a) ID, ParentID, Sequence, AverageMass, MonoMass, and pI in the Assay
###		b) Everything BUT those 3 calculated values in the Sample Set
###
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
PARENT_ID_COL_NAME = "Parent ID"
SAMPLES_SCHEMA = "Samples"
COMPOUND_TABLE = "Construct"
COMPOUND_FOLDER = "/Optides/CompoundsRegistry/Samples"

## Hydrogen's mass needed for mass calculations
H_ISO_MASS = 1.0078250

#
# FUNCTIONS
#
uniquenessCheck <- function(arg1, arg2){
	matches <- match(arg1, arg2)
	matches <- matches[!is.na(matches)]
	if(length(matches) > 0){
		cat("ERROR: No duplicates allowed. The following sequences have previously been uploaded into the repository: \n")
		for(i in 1:length(matches)){
			cat("ID: ", inputDF[matches[i],COMPOUND_ID_COL_NAME], ": ", inputDF[matches[i],SEQUENCE_COL_NAME], "\n")
		}
		return(FALSE)
	}
	return(TRUE)
}
mysetdiff<-function (x, y, multiple=FALSE) {
    x <- as.vector(x)
    y <- as.vector(y)
    if (length(x) || length(y)) {
        if (!multiple) {
             unique( x[match(x, y, 0L) == 0L])  
              }else  x[match(x, y, 0L) == 0L] 
        } else x
}
# END FUNCTIONS

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

## get all previously uploaded sequences
parentSampleSetIDs <- labkey.selectRows(baseUrl, "experiment/Optides/CompoundsRegistry/Samples/", "Samples", "Variant",
    viewName = NULL, colSelect = COMPOUND_ID_COL_NAME, maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

## read the input data frame just to get the column headers.
inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")



##
## 1) Verify ParentIDs (that they exists in the Sample Set)
##
#NA check.  prompt user if there are missting parent IDs in his input
if(length(inputDF[,is.na(inputDF[,"Parent.ID"])]) > 0){
	stop("There are sequences in your input that do not have a Parent ID.  Please provide a Parent ID for all sequences and then try again.")
}

a <- mysetdiff(inputDF[,"Parent.ID"], parentSampleSetIDs[,COMPOUND_ID_COL_NAME], multiple=TRUE)
if(length(a) > 0){
	cat("ERROR: Some of the ParentIDs were not found in the ", COMPOUND_TABLE, " Table. The following ParentID were not found: \n")
	for(i in 1:length(a)){
		cat(i, ": ", a[i], "\n")
	}
	stop("Please correct these ParentIDs and try again.")
}


##
## 2) Check for duplicates in input data. if so, list the row and sequence, then throw error
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

## get all previously uploaded sequences
previousSampleSetContents <- labkey.selectRows(baseUrl, "experiment/Optides/CompoundsRegistry/Samples/", "Samples", "Construct",
    viewName = NULL, colSelect = c(COMPOUND_ID_COL_NAME, PARENT_ID_COL_NAME, SEQUENCE_COL_NAME), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

if(!uniquenessCheck(previousSampleSetContents[,SEQUENCE_COL_NAME],inputDF[,SEQUENCE_COL_NAME])){
	stop("Please remove the duplicates from your input file and try again.")
}


##
## 3) calculate average mass, monoisotopic mass, and pI
##
toinsert <- inputDF
for (i in 1:length(toinsert[,SEQUENCE_COL_NAME])){
	Cs <- floor(str_count(inputDF[i, SEQUENCE_COL_NAME], "C")/2) * 2
	toinsert$AverageMass[i] <- mw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE) - Cs * H_ISO_MASS
	toinsert$MonoisotopicMass[i] <- mw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE) - Cs * H_ISO_MASS
	toinsert$pI[i] <- pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS")
}

##
## 4) Insert data to Database
##
#insert input into compound registry sample set (error will be thrown in ID already exists)
labkey.insertRows(baseUrl, COMPOUND_FOLDER, SAMPLES_SCHEMA, COMPOUND_TABLE, inputDF)

#insert input into assay
write.table(toinsert,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)


