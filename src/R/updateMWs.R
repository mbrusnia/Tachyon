options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

source("C:/labkey/labkey/files/Optides/@files/Utils.R")

BASE_URL = "https://optides-stage.fhcrc.org"
CONTAINER_PATH = "/Optides/CompoundsRegistry/Samples"
SAMPLE_SETS_SCHEMA_NAME = "samples"

chemProd <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath=CONTAINER_PATH,
    schemaName=SAMPLE_SETS_SCHEMA_NAME,
    queryName="CHEMProduction",
	colNameOpt="fieldname", 
	colFilter=makeFilter(c("OTDProductionID", "NOT_MISSING", "")),  
    colSelect=c("RowId", "CHEMProductionID", "OTDProductionID", "AverageMW", "ConjugationMethod")
)

DeltaC14 = 2.0
for(i in 1:length(chemProd$OTDProductionID)){
	if(chemProd$ConjugationMethod[i] == "C14 reductive amination"){
		#get sequence in order to calculate weight
		otdProdID = chemProd$OTDProductionID[i]
		if(is.null(otdProdID) || is.na(otdProdID) || otdProdID == ""){
			stop(paste0("Row ", i, " is C14 reductive animation, yet has no given OTDProductionID.  Please add the OTDProductionID and try again."))
		}
		#get ConstructID
		constructIDs <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
			SAMPLE_SETS_SCHEMA_NAME, "OTDProduction", colSelect = c("OTDProductionID", "ParentID"), 
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
		chemProd$NewAverageMW[i] = DSBMWCalc(sequence) + (str_count(sequence, "K")+1) * 2.0 * (calc_formula_mass("C1H2")+ DeltaC14)
	
	#else, the value given in the input file is fine
	}
}

##
##insert into DB
##
ssi <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="CHEMProduction",
	toUpdate=chemProd
)

if(!exists("ssi")){
	stop("The insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat(length(chemProd$CHEMProductionID), " RECORDS HAVE BEEN UPDATED.  These are their CHEMProductionID's:\n")
	for(i in 1:length(chemProd$CHEMProductionID)){
		cat(chemProd$CHEMProductionID[i], " ")
	}
	cat("\n")
}

