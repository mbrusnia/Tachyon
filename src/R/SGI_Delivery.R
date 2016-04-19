###
### This R Transformation script for labkey 
###

options(stringsAsFactors = FALSE)
library(Rlabkey)
pathToInputFile <- "C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/Project13 - SGI_DNA_pipeline/SGI_Delivery_Form.xlsx"

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"

SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
SGI_DNA_QUERY_NAME = "SGI_DNA"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"


## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(pathToInputFile) == "xlsx"){
	source("./xlsxToR.R")
	inputDF <- xlsxToR(pathToInputFile, header=TRUE)
}else{ 
	inputDF<-read.table(file=pathToInputFile, header = TRUE, sep = "\t")
}

colHeaders <- names(inputDF)
if(colHeaders[1] != "Plate ID" || colHeaders[2] != "Construct ID" ||
	colHeaders[3] != "Well ID" || colHeaders[4] != "DNA Concentration" ||
	colHeaders[5] != "AASeq" || colHeaders[6] != "DNASeq" ||
	colHeaders[7] != "Comment"){
	
	stop("The input file format detected is incompatible with this operation.  Please contact the administrator.")
}

##get corresponding constructIDs from Constructs sampleSet
##makeFilter
filterStr = ""
for(i in 1:length(inputDF[,1])){
	if(is.na(inputDF[i, "Comment"]) || (inputDF[i, "Construct ID"] != "Blank" && inputDF[i, "Comment"] != "Leave well blank" && inputDF[i, "Comment"] != "Control")){
		filterStr <- c(filterStr, c(inputDF[i, "Construct ID"]))
	}
}
filterStr <- filterStr[2:length(filterStr)]
filterStr <- paste(filterStr, collapse=";")
	
ss <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	viewName="",
	containerFilter=NULL,
	colNameOpt="fieldname",
	colSelect=c("Name", "ConstructID", "Comments", "DNAConcentration", "DNASeq", "PlateID", "WellID"),
	colFilter=makeFilter(c("ConstructID", "IN", filterStr))
)

#append data
for(i in 1:length(ss$ConstructID)){
	thisRow <- inputDF[inputDF[,"Construct ID"] == ss$ConstructID[i], ]
	ss$DNASeq[i] <- thisRow[["DNASeq"]]
	ss$PlateID[i] <- thisRow[["Plate ID"]]
	ss$WellID[i] <- thisRow[["Well ID"]]
	ss$DNAConcentration[i] <- thisRow[["DNA Concentration"]]
	if(is.na(thisRow[["Comment"]])){
		ss$Comments[i] <- ""
	}else{
		ss$Comments[i] <- thisRow[["Comment"]]
	}
}


#update DB
ssu <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toUpdate=ss
)

#completed
cat(length(ss$DNASeq), " RECORDS HAVE BEEN UPDATED.")
