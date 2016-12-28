###
### This Labkey Pipeline script will do 3 things upon uploading of new
### Sequences to be inserted into the Homologue SampleSet:
###	1) Make sure all sequences are unique.  
###	2) If there are duplicates, report them all at once
### 3) Assign new IDs to incoming sequences, incrementally from previously existing max ID
###


options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")

jobInfoFile <- sub("..", "../", "${pipeline, taskInfo}", perl=TRUE)
jobInfo <- read.table(jobInfoFile,
                      col.names=c("name", "value", "type"),
                      header=FALSE, check.names=FALSE,
                      stringsAsFactors=FALSE, sep="\t", quote="",
                      fill=TRUE, na.strings="")


inputFile <- "${input.xlsx}"
inputDF <- xlsxToR(inputFile, header=TRUE)

##
##Parameters for this script (login script: _netrc)
##
BASE_URL <- jobInfo$value[ grep("baseUrl", jobInfo$name)]
BASE_URL <- gsub("https", "http", BASE_URL)
CONTAINER_PATH <- jobInfo$value[ grep("containerPath", jobInfo$name)]
SAMPLE_SETS_SCHEMA_NAME <- "samples"
SAMPLESET_NAME <- "Homologue"
ID_COL_NAME <- "ID"
SEQUENCE_COL_NAME <- "AASeq"

#######################################################################################
##
## Make the _netrc file we need in order to connect to the database through rlabkey
##
#######################################################################################
filename <- paste0(Sys.getenv()["HOME"], .Platform$file.sep, "_netrc")
machineName <- "optides-stage.fhcrc.org"
login <- "brusniak.computelifesci@gmail.com"
password <- "Kn0ttin10K"
if(!file.exists(filename)){
	f = file(description=filename, open="w")
	cat(file=f, sep="", "machine ", machineName, "\n")
	cat(file=f, sep="", "login ", login, "\n")
	cat(file=f, sep="", "password ", password, "\n")
	flush(con=f)
	close(con=f)
}else{
	txtFile <- readLines(filename)
	counter <- 0
	for(i in 1:length(txtFile)){
		if(txtFile[i] == paste0("machine ", machineName)){
			counter <- counter + 1
		}
		if(txtFile[i] == paste0("login ", login)){
			counter <- counter + 1
		}
		if(txtFile[i] == paste0("password ", password)){
			counter <- counter + 1
		}
	}
	if(counter != 3){
		write(paste0("\nmachine ", machineName),file=filename,append=TRUE)
		write(paste0("login ", login),file=filename,append=TRUE)
		write(paste0("password ", password),file=filename,append=TRUE)
	}

}
######################################
## end
######################################


## get all previously uploaded sequences
previousHomologueSequenceContents <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
		SAMPLE_SETS_SCHEMA_NAME, SAMPLESET_NAME, colSelect =c(ID_COL_NAME, SEQUENCE_COL_NAME), colNameOpt="fieldname")

#find duplicate AA Sequences and report
matches <- match(inputDF[,SEQUENCE_COL_NAME], previousHomologueSequenceContents[,SEQUENCE_COL_NAME])
rowsWithDuplicates <- which(!is.na(matches))
if(length(rowsWithDuplicates) > 0){
	cat("ERROR: No duplicate AA sequences allowed allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(rowsWithDuplicates)){
		cat("Row ", rowsWithDuplicates[i] + 1, " - Matching ID: ", previousHomologueSequenceContents[matches[rowsWithDuplicates[i]], ID_COL_NAME],  " - Sequence: ", inputDF[rowsWithDuplicates[i],SEQUENCE_COL_NAME], "\n")
	}
	stop("Please remove the duplicate AA Sequences from your input file and try again.")
}

##
## Create new IDs
##
inputDF[, ID_COL_NAME] <- ""

#get highest ID
newID <- max(as.numeric(substr(previousHomologueSequenceContents[, ID_COL_NAME], 4, 10))) + 1
cat("Inserting ", length(inputDF[,ID_COL_NAME]), " new sequences.  New IDs begin with ", paste0("HMG", str_pad(newID, 7, "0", side="left")), " and end with ", paste0("HMG", str_pad(newID + length(inputDF[,ID_COL_NAME]) - 1, 7, "0", side="left")), "\n")

for(i in 1:length(inputDF[,ID_COL_NAME])){
	inputDF[i, ID_COL_NAME] <- paste0("HMG", str_pad(newID, 7, "0", side="left"))
	newID = newID + 1
}


##
##insert into DB
##
ssi <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SAMPLESET_NAME,
	toInsert=inputDF
)

if(!exists("ssi")){
	stop("The insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat(length(inputDF$AASeq), " RECORDS HAVE BEEN INSERTED.\n")
}