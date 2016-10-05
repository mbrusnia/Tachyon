###
### This is an R script for a labkey pipeline import feature
###
### SGI_Delivery.R - Parse and insert an SGI Delivery xlsx file
###


options(stringsAsFactors = FALSE)
library(Rlabkey)

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")
pathToInputFile <- "${input.xlsx}"

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"

SCHEMA_NAME = "exp.data"
QUERY_NAME = "Construct"
DATA_CLASS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"

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


## read the input
inputDF <- xlsxToR(pathToInputFile, header=TRUE)

## add the DataInputs/Variant column:
inputDF <- data.frame(inputDF, "DataInputs/Variant"="test", check.names=FALSE)
inputDF["DataInputs/Variant"] <- inputDF["Parent ID"]

varData <- labkey.selectRows(
    baseUrl="http://optides-stage.fhcrc.org",
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="exp.data",
	colNameOpt="fieldname",
	colSelect=c("RowId", "Name"),
	showHidden=TRUE,
    queryName="Variant"
)
vecData <- labkey.selectRows(
    baseUrl="http://optides-stage.fhcrc.org",
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="exp.data",
	colNameOpt="fieldname",
	colSelect=c("RowId", "Name"),
	showHidden=TRUE,
    queryName="Vector"
)

#replace alternate keys by lookup
for(i in 1:length(inputDF$ID)){
	inputDF[i, "Parent ID"] = varData$RowId[varData$Name == inputDF[i, "Parent ID"]]
	inputDF$Vector[i] = vecData$RowId[vecData$Name == inputDF$Vector[i]]
}

ir <- labkey.insertRows(
    baseUrl="http://optides-stage.fhcrc.org",
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="exp.data",
	queryName="Construct",
	toInsert=inputDF
)

if(ir){

