###
### This is an R script for a labkey pipeline import feature
###
### SGIOrderGenerator.R
###

options(stringsAsFactors = FALSE)
library(Rlabkey)

pathToInputFile <- "${input.xlsx}"

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-prod.fhcrc.org/"

SEQUENCE_COL_NAME = "AASeq"
CONSTRUCT_ID_COL_NAME = "ConstructID"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
SGI_DNA_QUERY_NAME = "SGI_DNA"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

#######################################################################################
##
## Make the _netrc file we need in order to connect to the database through rlabkey
##
#######################################################################################
filename <- paste0(Sys.getenv()["HOME"], .Platform$file.sep, "_netrc")
machineName <- "optides-prod.fhcrc.org"
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
previousSGI_DNASequenceContents <- labkey.selectRows(BASE_URL, SAMPLE_SETS_FOLDER_PATH, 
		SAMPLE_SETS_SCHEMA_NAME, SGI_DNA_QUERY_NAME, colSelect =c(CONSTRUCT_ID_COL_NAME, SEQUENCE_COL_NAME), colNameOpt="fieldname")

#find duplicate ConstructIDs
matches <- match(inputDF[,"ID"], previousSGI_DNASequenceContents[,CONSTRUCT_ID_COL_NAME])
rowsWithDuplicates <- which(!is.na(matches))
if(length(rowsWithDuplicates) > 0){
	cat("ERROR: No duplicate constructIDs allowed. The following sequences have previously been uploaded into the repository: \n")
	for(i in 1:length(rowsWithDuplicates)){
		cat("Row ", rowsWithDuplicates[i] + 2, " - ID: ", inputDF[rowsWithDuplicates[i],"ID"], "\n")
	}
	stop("Please remove the duplicate Construct IDs from your input file and try again.")
}

##prepare the table for insertion into the assay
names(inputDF) <- c("Name", "Sample Set", "Flag", "ConstructID", "ID", "Parent ID", "Alternate Name", "AASeq", "Vector")
inputDF <- inputDF[, c(1,4, 8, 9)]

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
}


#insert into DB
ssi <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toInsert=inputDF
)

if(!exists("ssi")){
	stop("The insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat(length(inputDF$AASeq), " RECORDS HAVE BEEN INSERTED.\n")
}
