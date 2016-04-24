###
### This R Transformation script for labkey 
###
pathToInputFile <- "/Users/mbrusnia/Documents/LabKey/LIMSData/SGI_Delivery_Form.xlsx"
srcDirectory <- "/Users/mbrusnia/IntelliJProjects/Tachyon/src/R"
source("/Users/mbrusnia/IntelliJProjects/Tachyon/src/R/xlsxToR.R")
#source("HRInternetConsulting/Clients/FHCRC/Tachyon/src/R/xlsxToR.R")
#pathToInputFile <- "C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/Project13 - SGI_DNA_pipeline/SGI_Delivery_Form.xlsx"
options(stringsAsFactors = FALSE)
library(Rlabkey)
<<<<<<< HEAD

=======
pathToInputFile <- "/Users/mbrusnia/Documents/LabKey/LIMSData/SGI_Delivery_Form.xlsx"
srcDirectory <- "/Users/mbrusnia/IntelliJProjects/Tachyon/src/R"
source("/Users/mbrusnia/IntelliJProjects/Tachyon/src/R/xlsxToR.R")
>>>>>>> e8f32fdddac9c384c33591d6436760599ee6961d
#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"

SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"

SAMPLE_SETS_SCHEMA_NAME = "Samples"
SGI_DNA_QUERY_NAME = "SGI_DNA"
HTP_SPECIMEN_QUERY_NAME = "HTP_SPECIMEN"
SAMPLE_SETS_FOLDER_PATH = "Optides/CompoundsRegistry/Samples"



## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(pathToInputFile) == "xlsx"){
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
filterArr = c()
for(i in 1:length(inputDF[,1])){
	if(is.na(inputDF[i, "Comment"]) || (inputDF[i, "Construct ID"] != "Blank" && inputDF[i, "Comment"] != "Leave well blank" && inputDF[i, "Comment"] != "Control")){
		filterArr <- c(filterArr, c(inputDF[i, "Construct ID"]))
	}
}
filterArr <- filterArr[2:length(filterArr)]
filterS <- paste(filterArr, collapse=";")
	
ss <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	viewName="",
	containerFilter=NULL,
	colNameOpt="fieldname",
	colSelect=c("Name", "ConstructID", "Comments", "DNAConcentration", "DNASeq", "PlateID", "WellID"),
	colFilter=makeFilter(c("ConstructID", "IN", filterS))
)

##make sure all of currently inputed Construct ID's were found in the database. if not, throw error
matches <- match(filterArr, ss$ConstructID)
if(length(matches[is.na(matches)]) > 0){
	nonMatchingRows <- which(is.na(matches))
	cat(length(nonMatchingRows), "incoming IDs were not found to be previously existing in our database.  They are the following:\n")
	for(i in 1:length(nonMatchingRows)){
		cat(filterArr[nonMatchingRows[i]], "\n")
	}
	stop("No updates were made.  Please correct this problem and try again.")
}

#append data from the input to the existing rows in SGI_DNA
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

#update SGI_DNA Database/SampleSet
ssu <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=SGI_DNA_QUERY_NAME,
	toUpdate=ss
)

#
#now prepare data for insertion into HTP_Specimen
#

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

##find the latest HTPPlateID (we will begin with that + 1 for our new HTPPlate)
ssHTP <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HTP_SPECIMEN_QUERY_NAME,
	colNameOpt="fieldname",
	colSelect=c("Name", "HTPPlateID")
)
newHTPPlateID <- 0
if(length(ssHTP$HTPPlateID) == 0){
	newHTPPlateID <- 20
}else{
	newHTPPlateID <- max(as.numeric(substr(ssHTP$HTPPlateID, 4, nchar(ssHTP$HTPPlateID)))) + 1
}
if(newHTPPlateID < 20){ newHTPPlateID <- 20}
if(nchar(newHTPPlateID) == 2){
	newHTPPlateID = paste0("00", newHTPPlateID)
}else if(nchar(newHTPPlateID) == 3){
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

##insert data into sampleset database
ssHTP_insert <- labkey.insertRows(
	baseUrl=BASE_URL,
	folderPath=SAMPLE_SETS_FOLDER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName=HTP_SPECIMEN_QUERY_NAME,
	toInsert=htpSpecimenToInsert
)
#completed
cat(length(ss$DNASeq), "RECORDS HAVE BEEN UPDATED IN SGI_DNA.\n", length(htpSpecimenToInsert$Name), "ROWS HAVE BEEN INSERTED INTO HTP_SPECIMEN.\n")
