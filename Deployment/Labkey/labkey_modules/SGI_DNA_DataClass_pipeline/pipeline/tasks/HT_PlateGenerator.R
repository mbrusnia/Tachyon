###
### This is an R script for a labkey pipeline import feature
###


options(stringsAsFactors = FALSE)
library(Rlabkey)

#script required for reading xlsx files:
source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")

#######################################################################################
##
## Make the _netrc file we need in order to connect to the database through rlabkey
##
#######################################################################################
filename <- paste0(Sys.getenv()["HOME"], .Platform$file.sep, "_netrc")
if(!file.exists(filename)){
	f = file(description=filename, open="w")
	cat(file=f, sep="", "machine optides-stage.fhcrc.org", "\n")
	cat(file=f, sep="", "login brusniak.computelifesci@gmail.com", "\n")
	cat(file=f, sep="", "password Kn0ttin10K", "\n")
	flush(con=f)
	close(con=f)
}else{
	txtFile <- readLines(filename)
	counter <- 0
	for(i in 1:length(txtFile)){
		if(txtFile[i] == "machine optides-stage.fhcrc.org"){
			counter <- counter + 1
		}
		if(txtFile[i] == "login brusniak.computelifesci@gmail.com"){
			counter <- counter + 1
		}
		if(txtFile[i] == "password Kn0ttin10K"){
			counter <- counter + 1
		}
	}
	if(counter != 3){
		write("\nmachine optides-stage.fhcrc.org",file=filename,append=TRUE)
		write("login brusniak.computelifesci@gmail.com",file=filename,append=TRUE)
		write("password Kn0ttin10K",file=filename,append=TRUE)
	}

}
######################################
## end
######################################

pathToInputFile <- "${input.xlsx}"

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"

SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"

DATA_CLASSES_SCHEMA_NAME = "exp.data"
HTP_SPECIMEN_QUERY_NAME = "HTP_Specimen"
DATA_CLASSES_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

## read the input
inputDF <- xlsxToR(pathToInputFile, header=TRUE)

colHeaders <- names(inputDF)
if(colHeaders[1] != "Plate ID" || colHeaders[2] != "Construct ID" ||
	colHeaders[3] != "Well ID" || colHeaders[4] != "DNA Concentration" ||
	colHeaders[5] != "AASeq" || colHeaders[6] != "DNASeq" ||
	colHeaders[7] != "Comment"){
	
	stop("The input file format detected is incompatible with this operation.  Please contact the administrator.")
}

#re-format the PlateID and WellID fields
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

#
#now prepare data for insertion into HTP_Specimen
#

##find the latest HTPPlateID (we will begin with that + 1 for our new HTPPlate)
ssHTP <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=DATA_CLASSES_FOLDER_PATH,
	schemaName=DATA_CLASSES_SCHEMA_NAME,
	queryName=HTP_SPECIMEN_QUERY_NAME,
	colNameOpt="fieldname",
	colSelect=c("HTPPlateID")
)
newHTPPlateID <- 100
if(length(ssHTP$HTPPlateID) > 0){
	newHTPPlateID <- max(as.numeric(substr(ssHTP$HTPPlateID, 4, nchar(ssHTP$HTPPlateID)))) + 1
}

if(nchar(newHTPPlateID) == 3){
	newHTPPlateID = paste0("0", newHTPPlateID)
}
newHTPPlateID <- paste0("HTP", newHTPPlateID)

htpSpecimenToInsert <- data.frame(cbind("Specimen" = newHTPPlateID, "HTPPlateID" = newHTPPlateID, "CNT" = inputDF[, "Construct ID"], "PlateID" = inputDF[, "Plate ID"], "WellID" = inputDF[, "Well ID"]))

#now calculate quadrant and update/complete Specimen value
for(i in 1:length(htpSpecimenToInsert$Specimen)){
	wid <- htpSpecimenToInsert$WellID[i]
	letter <- substr(wid, 1,1)
	num    <- as.numeric(substr(wid, 2,3))

	quadrant <- 0
	if(letter == "A" || letter == "B" || letter == "C" || letter == "D"){
		if(num < 7){
			quadrant = 1
		}else{
			quadrant = 2
		}
	}else{
		if(num < 7){
			quadrant = 3
		}else{
			quadrant = 4
		}
	}
	htpSpecimenToInsert$Specimen[i] <- paste0(htpSpecimenToInsert$Specimen[i], quadrant, htpSpecimenToInsert$WellID[i])
}
htpSpecimenToInsert <- cbind("Name" = htpSpecimenToInsert$Specimen, htpSpecimenToInsert)

##insert data into HTP_Specimen sampleset database
ssHTP_insert <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=DATA_CLASSES_FOLDER_PATH,
	schemaName=DATA_CLASSES_SCHEMA_NAME,
	queryName=HTP_SPECIMEN_QUERY_NAME,
	toInsert=htpSpecimenToInsert
)
#completed
cat(length(htpSpecimenToInsert$Name), "ROWS HAVE BEEN INSERTED INTO HTP_SPECIMEN.\n")

