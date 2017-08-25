###
### This is an R script for a labkey pipeline import feature
###
### SGIOrderGenerator.R
###

options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

pathToInputFile <- "${input.xlsx}"

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")
source("C:/labkey/labkey/files/Optides/@files/Utils.R")

jobInfoFile <- sub("..", "../", "${pipeline, taskInfo}", perl=TRUE)
jobInfo <- read.table(jobInfoFile,
                      col.names=c("name", "value", "type"),
                      header=FALSE, check.names=FALSE,
                      stringsAsFactors=FALSE, sep="\t", quote="",
                      fill=TRUE, na.strings="")
					  
#Parameters for this script (login script: _netrc)
BASE_URL <- jobInfo$value[ grep("baseUrl", jobInfo$name)]

CONSTRUCT_ID_COL_NAME = "ConstructID"
SEQUENCE_COL_NAME = "AASeq"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
HT_DNA_QUERY_NAME = "HT_DNA"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"


#a hash to look up vector ids to vector names  #this list will grow
#Vector_hash <- list(VCR010="RKS017", VCR011="RSK056", VCR012="RKS017", VCR020 ="JMO084", VCR21="JMO084", VCR30="JMO300", VCR040="MDT208", VCR050="Elafin", VCR000="Unavailable")
#Vector_hash <- list(VCR010="VCR010", VCR011="VCR011", VCR012="VCR012", VCR020 ="VCR020", VCR21="VCR21", VCR30="VCR30", VCR040="VCR040", VCR050="VCR050", VCR000="Unavailable")


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

## Get the Vendor Name
vendorName <- "${vendorName}"

## Get the Linker sequence and remove it from all the sequences
linker <- "${linkerSequence}"
for(i in 1:length(inputDF[,SEQUENCE_COL_NAME])){
	inputDF[i, SEQUENCE_COL_NAME] = sub(paste0("^", linker), "", inputDF[i, SEQUENCE_COL_NAME])
}


##
## check if the new sequences have previously been loaded into the database. 
## if so, list the rows and sequences of the repeated sequences
##

## get all previously uploaded sequences
previousHT_DNAID_ConstructIDs <- labkey.selectRows(BASE_URL, SAMPLE_SETS_FOLDER_PATH, 
		SAMPLE_SETS_SCHEMA_NAME, HT_DNA_QUERY_NAME, colSelect =c(CONSTRUCT_ID_COL_NAME, "DNAID", "Vector"), colNameOpt="fieldname")

#find duplicate (ConstructID, Vector) pairs
matches <- match(paste0(inputDF[,"ID"], "_", inputDF[,"Vector"]), paste0(previousHT_DNAID_ConstructIDs[,CONSTRUCT_ID_COL_NAME], "_", previousHT_DNAID_ConstructIDs[,"Vector"]))
rowsWithDuplicateConstructIDsAndVectors <- which(!is.na(matches))
if(length(rowsWithDuplicateConstructIDsAndVectors) > 0){
	cat("ERROR: No duplicate (constructID, Vector) pairs allowed. The following entries have previously been uploaded into the repository: \n")
	for(i in 1:length(rowsWithDuplicateConstructIDsAndVectors)){
		cat("Row ", rowsWithDuplicateConstructIDsAndVectors[i] + 2, " - ID: ", inputDF[rowsWithDuplicateConstructIDsAndVectors[i],"ID"], " - Vector: ", inputDF[rowsWithDuplicateConstructIDsAndVectors[i],"Vector"], "\n")
	}
	stop("Please remove the duplicate (ConstructID, Vector) pairs from your input file and try again.")
}

##prepare the table for insertion into the assay
names(inputDF) <- c("Name", "Sample Set", "Flag", "ConstructID", "ID", "Parent ID", "Alternate Name", "AASeq", "Vector")
inputDF <- inputDF[, c(1,4, 8, 9)]

inputDF$Vendor <- vendorName

#increment DNAIDs
max_dnaID <-  max(as.numeric(substring(previousHT_DNAID_ConstructIDs[, "DNAID"], 4, 10)))

inputDF$DNAID = ""

for(i in 1:length(inputDF[,1])){
	#no blank allowed as well as no unknown
	if(is.na(inputDF[i, "Vector"])){
		cat("There is a blank value entered for a Vector value on row ", i, ".  This is not allowed.  Please fix this and try again.")
		stop()
	}
#	if(is.null(Vector_hash[[inputDF[i, "Vector"]]])){
#		stop(paste("An invalid Vector has been specified in your input.  This Vector value is invalid: ", inputDF[i, "Vector"]))
#	}
	inputDF[i, "Vector"] <- inputDF[i, "Vector"]
	inputDF$DNAID[i] <- paste0("DNA", str_pad(max_dnaID + i, 7, "0", side="left"))
	inputDF$Name[i] = inputDF$DNAID[i]
}

#insert into DB
ssi <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_DNA_QUERY_NAME,
	toInsert=inputDF
)

if(!exists("ssi")){
	stop("The insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat(length(inputDF$AASeq), " RECORDS HAVE BEEN INSERTED.\n")
}
