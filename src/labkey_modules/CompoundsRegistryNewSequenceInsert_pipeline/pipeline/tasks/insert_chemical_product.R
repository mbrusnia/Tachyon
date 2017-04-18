###
### This Labkey Pipeline script will do 3 things upon uploading of new
### Sequences to be inserted into the Homologue SampleSet:
###	1) auto ID generation and 
### 2) Reaction keyed property calculation
### 3) updates Compound Registry -> ChemProduction table
###


options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

source("C:/labkey/labkey/files/Optides/@files/xlsxToR.R")
source("C:/labkey/labkey/files/Optides/@files/Utils.R")

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
CONTAINER_PATH <- jobInfo$value[ grep("containerPath", jobInfo$name)]
SAMPLE_SETS_SCHEMA_NAME <- "samples"
SAMPLESET_NAME <- "CHEMProduction"
ID_COL_NAME <- "CHEMProductionID"
CONJUGATION_METHOD_COL_NAME = "Conjugation Method"
OTDPRODUCTIONID_COL_NAME = "OTDProduction ID"
AVG_MOL_WEIGHT_COL_NAME = "Average MW"

OTDPRODUCTION_SAMPESET_NAME = "OTDProduction"
SEQUENCE_COL_NAME <- "AASeq"
VECTOR_COL_NAME <- "Vector"

#######################################################################################
##
## Make the _netrc file we need in order to connect to the database through rlabkey
##
#######################################################################################
machineName <- machineNameFromBaseURL(BASE_URL)
login <- "brusniak.computelifesci@gmail.com"
password <- "Kn0ttin10K"

write_NetRC_file(machineName, login, password)

######################################
## end
######################################



######################################
## 1) Calculate new IDs
######################################
## get all previously uploaded IDs
previousChemProductionContents <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
		SAMPLE_SETS_SCHEMA_NAME, SAMPLESET_NAME, colSelect =ID_COL_NAME, colNameOpt="fieldname")

nextIDnum = 0

#get next ID Number
if(length(previousChemProductionContents[,ID_COL_NAME]) > 0){
	nextIDnum = max(as.numeric(substring(previousChemProductionContents[, ID_COL_NAME], 4, 10))) + 1
}


##
## Create new IDs
##
inputDF[, ID_COL_NAME] <- ""

#cat("Inserting ", length(inputDF[,ID_COL_NAME]), " new sequences.  New IDs begin with ", paste0("CNT", str_pad(newID, 7, "0", side="left")), " and end with ", paste0("CNT", str_pad(newID + length(inputDF[,ID_COL_NAME]) - 1, 7, "0", side="left")), "\n")

for(i in 1:length(inputDF[,ID_COL_NAME])){
	if(inputDF[i, CONJUGATION_METHOD_COL_NAME] == "C14 reductive amination"){
		inputDF[i, ID_COL_NAME] <- paste0("CHH", str_pad(nextIDnum, 7, "0", side="left"))
	}else{
		inputDF[i, ID_COL_NAME] <- paste0("CHC", str_pad(nextIDnum, 7, "0", side="left"))
	}
	nextIDnum = nextIDnum + 1
}

##############################################
## 2) Calculate Molecular Weights
################################################
for(i in 1:length(inputDF[,ID_COL_NAME])){
	if(inputDF[i, CONJUGATION_METHOD_COL_NAME] == "C14 reductive amination"){
		#get sequence in order to calculate weight
		otdProdID = inputDF[i, OTDPRODUCTIONID_COL_NAME]
		if(is.null(otdProdID) || is.na(otdProdID) || otdProdID == ""){
			stop(paste0("Row ", i, " is C14 reductive animation, yet has no given OTDProductionID.  Please add the OTDProductionID and try again."))
		}
		#get ConstructID
		constructIDs <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
			SAMPLE_SETS_SCHEMA_NAME, OTDPRODUCTION_SAMPESET_NAME, colSelect = c("OTDProductionID", "ParentID"), 
			colFilter=makeFilter(c("OTDProductionID", "EQUAL", otdProdID)), colNameOpt="fieldname")
		if(length(constructIDs) < 1){
			stop(paste0("The OTDProductionID: ", otdProdID, " is not found in the OTDProduction Sampleset!  Please correct this issue and try again."))
		}
		constructID = gsub("Construct.", "", constructIDs$ParentID[1])

		#get sequence
		sequence <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
			SAMPLE_SETS_SCHEMA_NAME, "Construct", colSelect = c("ID", "AASeq"), 
			colFilter=makeFilter(c("ID", "EQUAL", constructID)), colNameOpt="fieldname")$AASeq[1]
#inputDF$sequence[i] = sequence
		#calculate Molecular Weight
		inputDF[i, AVG_MOL_WEIGHT_COL_NAME] = DSBMWCalc(sequence) + (str_count(sequence, "K")+2) * calc_formula_mass("C1H2")
	
	#else, the value given in the input file is fine
	}else{
		if(inputDF[i, AVG_MOL_WEIGHT_COL_NAME] == "" || is.null(inputDF[i, AVG_MOL_WEIGHT_COL_NAME]) || is.na(inputDF[i, AVG_MOL_WEIGHT_COL_NAME])){
			stop(paste0("Row ", i, " is not C14 reductive animation, yet has no given Average Molecular Weight.  Please add the Average Moleculare Weight and try again."))
		}
	}
}



#mydata <- labkey.selectRows(
#    baseUrl="https://optides-prod.fhcrc.org",
#    folderPath="/Optides/CompoundsRegistry/Samples",
#    schemaName="samples",
#    queryName="CHEMProduction",
#    colSelect = c("CHEMProductionID",  "ConjugationMethod", "OTDProductionID",   "AverageMW"),
#    colNameOpt="fieldname"
#)
##
##insert into DB
##
##ssi <- labkey.insertRows(
##	baseUrl=BASE_URL,
##	folderPath=CONTAINER_PATH,
##	schemaName=SAMPLE_SETS_SCHEMA_NAME,
##	queryName="CHEMProduction",
##	toInsert=inputDF
##)

if(!exists("ssi")){
	stop("The insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat(length(inputDF$AASeq), " RECORDS HAVE BEEN INSERTED.\n")
}