##
## This script updated the molecular weights in /CompoundsRegistry/Samples CHEMProduction
## and OTDProduction/Assays/ OTDProductionReport
##
## extension 6/6/17 - m/z validation using monoisotopic mass from InSilicoAssay/MolecularProperties
##
options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

source("C:/labkey/labkey/files/Optides/@files/Utils.R")

BASE_URL = "https://optides-prod.fhcrc.org"

CONTAINER_PATH = "/Optides/CompoundsRegistry/Samples"
SAMPLE_SETS_SCHEMA_NAME = "samples"

cat("\n-------- ", date(), " --------\n")
cat("updateMWs of CHEMProduction and OTDProductionReport\n")
cat("Updating server: ", BASE_URL, "\n")

#############################################
## update CHEMProduction MWs
#############################################
chemProd <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath=CONTAINER_PATH,
    schemaName=SAMPLE_SETS_SCHEMA_NAME,
    queryName="CHEMProduction",
	colNameOpt="fieldname",  
    colSelect=c("RowId", "CHEMProductionID", "OTDProductionID", "VariantID", "DrugReagentID", "LinkerReagentID", "AverageMW", "ConjugationMethod")
)

DeltaC14 = 2.0
for(i in 1:length(chemProd$CHEMProductionID)){
	#if drug reagent or linker reagents were used, we leave user-entered MW values in place
	if(!is.na(chemProd$DrugReagentID[i]) || !is.na(chemProd$LinkerReagentID[i])){
		next
	}
	
	#we recalculate mass only for the C14 reductive amination entries that either have an otdID or variantID
	if(!is.na(chemProd$ConjugationMethod[i]) && chemProd$ConjugationMethod[i] == "C14 reductive amination"
		&& (!is.na(chemProd$OTDProductionID[i]) || !is.na(chemProd$VariantID[i]))){
		
		#get the sequence via construct, if OTDid is set, or variant if VariantID is set
		if(!is.na(chemProd$OTDProductionID[i]) && is.na(chemProd$VariantID[i])){
			#get sequence in order to calculate weight
			otdProdID = chemProd$OTDProductionID[i]
			if(is.null(otdProdID) || is.na(otdProdID) || otdProdID == ""){
				stop(paste0("Row ", i, " is C14 reductive animation, yet has no given OTDProductionID.  Please add the OTDProductionID and try again."))
			}
			#get ConstructID
			constructIDs <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
				SAMPLE_SETS_SCHEMA_NAME, "OTDProduction", colSelect = c("OTDProductionID", "ParentID"), 
				colFilter=makeFilter(c("OTDProductionID", "EQUAL", otdProdID)), colNameOpt="fieldname")
			if(length(constructIDs$OTDProductionID) < 1){
				stop(paste0("The OTDProductionID: ", otdProdID, " is not found in the OTDProduction Sampleset!  Please correct this issue and try again."))
			}
			constructID = gsub("Construct.", "", constructIDs$ParentID[1])

			#get sequence
			sequence <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
				SAMPLE_SETS_SCHEMA_NAME, "Construct", colSelect = c("ID", "AASeq"), 
				colFilter=makeFilter(c("ID", "EQUAL", constructID)), colNameOpt="fieldname")$AASeq[1]
		}else if(is.na(chemProd$OTDProductionID[i]) && !is.na(chemProd$VariantID[i])){
			#get sequence in order to calculate weight
			variantID = chemProd$VariantID[i]

			#get sequence
			sequence <- labkey.selectRows(BASE_URL, CONTAINER_PATH, 
				SAMPLE_SETS_SCHEMA_NAME, "Variant", colSelect = c("ID", "AASeq"), 
				colFilter=makeFilter(c("ID", "EQUAL", variantID)), colNameOpt="fieldname")$AASeq[1]
		}

		#calculate Molecular Weight
		chemProd$AverageMW[i] = round(DSBMWCalc(sequence) + (str_count(sequence, "K")+1) * 2.0 * (calc_formula_mass("C1H2")+ DeltaC14), digit=2)
	}
}
##set NAs to ""  (required for R -> labkey insertions)
chemProd$OTDProductionID[is.na(chemProd$OTDProductionID)] = ""
chemProd$VariantID[is.na(chemProd$VariantID)] = ""
chemProd$DrugReagentID[is.na(chemProd$DrugReagentID)] = ""
chemProd$LinkerReagentID[is.na(chemProd$LinkerReagentID)] = ""
chemProd$AverageMW[is.na(chemProd$AverageMW)] = ""
chemProd$ConjugationMethod[is.na(chemProd$ConjugationMethod)] = ""

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
	stop("In CHEMProduction, the insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat("In CHEMProduction, ", length(chemProd$CHEMProductionID), " RECORDS HAVE BEEN UPDATED.  These are their CHEMProductionID's:\n")
	for(i in 1:length(chemProd$CHEMProductionID)){
		cat(chemProd$CHEMProductionID[i], "\n")
	}
}



#############################################
## update OTDProductionReport MWs
#############################################
CONTAINER_PATH = "/Optides/OTDProduction/Assays"
SAMPLE_SETS_SCHEMA_NAME = "samples"
DeltaC14 = 2.0

otdProdReport <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="OTDProductionReport",
	colNameOpt="fieldname", 
	colFilter=makeFilter(c("OTDProductionID", "NOT_MISSING", "")),  
	colSelect=c("RowId", "OTDProductionID", "MonoisotopicMass", "ObservedMz", "MSValidated")
)

for(i in 1:length(otdProdReport$OTDProductionID)){
	#get sequence in order to calculate weight
	otdProdID = otdProdReport$OTDProductionID[i]
		
	#get ConstructID
	constructIDs <- labkey.selectRows(BASE_URL, "/Optides/CompoundsRegistry/Samples", 
		SAMPLE_SETS_SCHEMA_NAME, "OTDProduction", colSelect = c("OTDProductionID", "ParentID"), 
		colFilter=makeFilter(c("OTDProductionID", "EQUAL", otdProdID)), colNameOpt="fieldname")
	if(length(constructIDs$OTDProductionID) < 1){
		cat(paste0("The OTDProductionID: ", otdProdID, " is not found in the OTDProduction Sampleset!  Please correct this issue.\n"))
		next
	}
	constructID = gsub("Construct.", "", constructIDs$ParentID[1])

	#get sequence
	sequence <- labkey.selectRows(BASE_URL, "/Optides/CompoundsRegistry/Samples", 
		SAMPLE_SETS_SCHEMA_NAME, "Construct", colSelect = c("ID", "AASeq"), 
		colFilter=makeFilter(c("ID", "EQUAL", constructID)), colNameOpt="fieldname")$AASeq[1]
	
	#calculate Molecular Weight
	otdProdReport$MonoisotopicMass[i] = round(DSBMWCalc(sequence, monoisotopic=TRUE), digit=2)
	
	#MSValidation
	H = calc_formula_mass("H1", monoisotopic=TRUE)
	otdProdReport$MSValidated[i] = "FALSE"
	for(q in 1:6){
		if(is.na(otdProdReport$ObservedMz[i]) || otdProdReport$ObservedMz[i] == ""){
			otdProdReport$ObservedMz[i] = ""
			otdProdReport$MSValidated[i] = ""
		}else if(abs((otdProdReport$MonoisotopicMass[i] + q*H)/q - as.numeric(otdProdReport$ObservedMz[i])) < 2.0)
			otdProdReport$MSValidated[i] = "TRUE"
	}
	#If validation fails, print the q, mz, and diff value of each q:
	if(otdProdReport$MSValidated[i] == "FALSE"){
		cat(otdProdID, " with ObservedMz ", otdProdReport$ObservedMz[i], " failed MS/MZ validation.  Here are the q, m/z, and diff. values:\n")
		for(q in 1:6){
			cat(q, ": ", sequence, ": ", (otdProdReport$MonoisotopicMass[i] + q*H)/q, ": ", abs((otdProdReport$MonoisotopicMass[i] + q*H)/q - as.numeric(otdProdReport$ObservedMz[i])), "\n")
		}
	}
}

#set NAs to ""s
otdProdReport[is.na(otdProdReport$MonoisotopicMass), "MonoisotopicMass"] = ""
otdProdReport[is.na(otdProdReport$ObservedMz), "ObservedMz"] = ""
otdProdReport[is.na(otdProdReport$MSValidated), "MSValidated"] = ""

##
##insert into DB
##
ssi2 <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="OTDProductionReport",
	toUpdate=otdProdReport
)

if(!exists("ssi2")){
	stop("In OTDProductionReport, the insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat("In OTDProductionReport, ", length(otdProdReport$OTDProductionID), " RECORDS HAVE BEEN UPDATED.  These are their OTDProductionID's:\n")
	for(i in 1:length(otdProdReport$OTDProductionID)){
		if(!is.na(otdProdReport$MonoisotopicMass[i])){
			cat(otdProdReport$OTDProductionID[i], "\n")
		}else
			cat(otdProdReport$OTDProductionID[i], "'s MonoisotopicMass could not be updated properly.", "\n")
	}

}

