###
### This is an R script for a labkey pipeline import feature
###
### HT_PlateGenerator.R
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
	cat(file=f, sep="", "machine optides-prod.fhcrc.org", "\n")
	cat(file=f, sep="", "login brusniak.computelifesci@gmail.com", "\n")
	cat(file=f, sep="", "password Kn0ttin10K", "\n")
	flush(con=f)
	close(con=f)
}else{
	txtFile <- readLines(filename)
	counter <- 0
	for(i in 1:length(txtFile)){
		if(txtFile[i] == "machine optides-prod.fhcrc.org"){
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
		write("\nmachine optides-prod.fhcrc.org",file=filename,append=TRUE)
		write("login brusniak.computelifesci@gmail.com",file=filename,append=TRUE)
		write("password Kn0ttin10K",file=filename,append=TRUE)
	}

}
######################################
## end
######################################

pathToInputFile <- "${input.xlsx}"

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-prod.fhcrc.org/"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
HT_PRODUCTION_QUERY_NAME = "HTProduction"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

## read the input
inputDF <- xlsxToR(pathToInputFile, header=FALSE)

##
## Extract only the plate data and its column headers from the file 
##
mynames <- inputDF[17, 1:11]
inputDF <- inputDF[18:(18 - 1 + 96),1:11]
names(inputDF) <- mynames

colHeaders <- names(inputDF)
if(grepl("Construct.*ID", colHeaders[1]) && grepl("Construct.*Name", colHeaders[2]) && grepl("Plate.*ID", colHeaders[3])
	&& grepl("Well.*Location", colHeaders[4]) && grepl("Concentration.*ng/uL", colHeaders[5])
	&& grepl("Volume.*uL", colHeaders[6]) && grepl("Total.*DNA.*ng", colHeaders[7])
	&& grepl("Vector", colHeaders[8]) && grepl("Resistance", colHeaders[9])
	&& grepl("Flanking.*Restriction.*Site", colHeaders[10]) && grepl("Sequence.*Verification", colHeaders[11])){	
	1==1
}else{
	stop("This file does not conform to the expected format.  Please contact the administrator.")
}

#change SGI headers to FHCRC Optides labkey sampleset headers
names(inputDF)[1:7] <- c("SGIID", "ConstructID", "SGIPlateID", "WellLocation", "Concentration_ngPeruL", "Volume_uL", "TotalDNA_ng") 


#
#now prepare data for insertion into HTP_Specimen
#

##find the latest HTPPlateID (we will begin with that + 1 for our new HTPPlate)
ssHTP <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_PRODUCTION_QUERY_NAME,
	colNameOpt="fieldname",
	colSelect=c("HTQuadPlateID")
)
newHTPlateID <- 100
if(length(ssHTP$HTQuadPlateID) > 0){
	newHTPlateID <- max(as.numeric(substr(ssHTP$HTQuadPlateID, 3, 6))) + 1
}

if(nchar(newHTPlateID) == 3){
	newHTPlateID = paste0("0", newHTPlateID)
}
newHTPlateID <- paste0("HT", newHTPlateID)

htProductsToInsert <- data.frame(cbind(HTProductID = newHTPlateID, HTQuadPlateID = newHTPlateID, 
	ConstructID = inputDF[, "ConstructID"], WellLocation = inputDF[, "WellLocation"], SGIID = inputDF[, "SGIID"], SGIPlateID = inputDF[, "SGIPlateID"]))

#now calculate quadrant and update/complete Specimen value
for(i in 1:length(htProductsToInsert$HTProductID)){
	wid <- htProductsToInsert$WellLocation[i]
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
	htProductsToInsert$HTQuadPlateID[i] <- paste0(htProductsToInsert$HTQuadPlateID[i], quadrant)
	htProductsToInsert$HTProductID[i] <- paste0(htProductsToInsert$HTProductID[i], quadrant, htProductsToInsert$WellLocation[i])

	if(htProductsToInsert$ConstructID[i] == "Blank"){
		htProductsToInsert$ConstructID[i] = "${blanks-replacement}"
	}
}

##insert data into HTP_Specimen sampleset database
ssHTP_insert <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HT_PRODUCTION_QUERY_NAME,
	toInsert=htProductsToInsert
)

#completed
cat(length(htProductsToInsert$HTProductID), "ROWS HAVE BEEN INSERTED INTO HTProduction.\n")

