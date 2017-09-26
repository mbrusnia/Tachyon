##
## This script updated the molecular weights in /CompoundsRegistry/Samples CHEMProduction
## and OTDProduction/Assays/ OTDProductionReport
##
## extension 6/6/17 - m/z validation using monoisotopic mass from InSilicoAssay/MolecularProperties
## extension 8/9/17 - in OTDProductioncopy OTDProductionID to OTDNovocyteID
## extension 9/19/17 - update ChemProductionReport Mol Yield %
##

options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)

source("C:/labkey/labkey/files/Optides/@files/Utils.R")

##put all output into this log file:
con <- file("C:/Users/tomcat/Programs/OptideCronJobs.log", open="a")
sink(con, append=TRUE)
sink(con, append=TRUE, type="message")


BASE_URL = "https://optides-prod.fhcrc.org"

cat("\n-------- ", date(), " --------\n")
cat("updateMWs of CHEMProduction and OTDProductionReport\n")
cat("Also populating ChemProductionReport table 'Mol Yield (%)' value\n")
cat("Updating server: ", BASE_URL, "\n")


DeltaC14 = 2.0

#############################################
## update CHEMProduction MWs
#############################################
CONTAINER_PATH = "/Optides/CompoundsRegistry/Samples"
SAMPLE_SETS_SCHEMA_NAME = "samples"
cat("#########   update CHEMProduction MWs  ########\n")
chemProd <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath=CONTAINER_PATH,
    schemaName=SAMPLE_SETS_SCHEMA_NAME,
    queryName="CHEMProduction",
	colNameOpt="fieldname",  
    colSelect=c("RowId", "CHEMProductionID", "OTDProductionID", "VariantID", "DrugReagentID", "LinkerReagentID", "AverageMW", "ConjugationMethod")
)

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
cat("#################  update OTDProductionReport MWs  #########\n")
CONTAINER_PATH = "/Optides/OTDProduction/Assays"
SAMPLE_SETS_SCHEMA_NAME = "samples"

otdProdReport <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="OTDProductionReport",
	colNameOpt="fieldname", 
	showHidden=TRUE,
	colFilter=makeFilter(c("OTDProductionID", "NOT_MISSING", "")),  
	colSelect=c("RowId", "OTDProductionID", "MonoisotopicMass", "ObservedMz", "MSValidated", "ReporterMedian")
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
	otdProdReport$ReporterMedian[i] = labkey.selectRows(BASE_URL, "/Optides/OTDProduction/Assays", 
		"assay.General.Novocyte_TransductionReport", "Data", colSelect = c("Sample", "ReporterMedian"), colNameOpt="fieldname",
		colFilter=makeFilter(c("Sample", "EQUAL", otdProdReport$OTDProductionID[i])))$ReporterMedian[1]

}

#set NAs to ""s
otdProdReport[is.na(otdProdReport$MonoisotopicMass), "MonoisotopicMass"] = ""
otdProdReport[is.na(otdProdReport$ObservedMz), "ObservedMz"] = ""
otdProdReport[is.na(otdProdReport$MSValidated), "MSValidated"] = ""
otdProdReport[is.na(otdProdReport$ReporterMedian), "ReporterMedian"] = ""

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


#############################################
## update ChemProductionReport Mol Yield %
#############################################
cat("#########   update ChemProductionReport Mol Yield %  ########\n")
CONTAINER_PATH = "/Optides/ChemProduction/Assay"
SAMPLE_SETS_SCHEMA_NAME = "samples"

chemProdReport <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="ChemProductionReport",
	colNameOpt="fieldname", 
	showHidden=TRUE,
	colFilter=makeFilter(c("ChemProductionID", "NOT_MISSING", "")),
	colSelect=c("RowId", "ChemProductionID", "StartingAmount_mg", "ProductionAmount_mg", "Percent_Yield")
)

#we already have the necessary records from CHEMProduction in chemProduction (from above)

for(i in 1:length(chemProdReport$ChemProductionID)){
	#get MW_input -depending on what fields are set, we get this value from different places:
	curChemProductionRow <- chemProd[chemProd$CHEMProductionID == chemProdReport$ChemProductionID[i],]
	MW_output = curChemProductionRow$AverageMW
	MW_input = 0.0

	#find or calculate MW_input:
	if(!is.na(curChemProductionRow$OTDProductionID) && curChemProductionRow$OTDProductionID != ""){
		# 1) if OTDProductionID is set:
		# get the constructID
		ConstructIDs <- labkey.selectRows(
			baseUrl=BASE_URL,
			folderPath="/Optides/CompoundsRegistry/Samples",
			schemaName=SAMPLE_SETS_SCHEMA_NAME,
			queryName="OTDProduction",
			colNameOpt="fieldname",
			showHidden=TRUE,
			colFilter=makeFilter(c("OTDProductionID", "EQUALS", curChemProductionRow$OTDProductionID)),
			colSelect=c("ParentID"))
		#if no constructID found for the OTDProductionID, skip this iteration
		if(length(ConstructIDs[,1]) == 0){
			cat(curChemProductionRow$CHEMProductionID, ": ODTProductionID ", curChemProductionRow$OTDProductionID, " has no associated ParentID (ConstructID).  Skipping this calculation.\n")
		}else{
			ConstructID <- ConstructIDs[1, 1]
			#lookup weight from InSilicoAssay
			MW_inputs <- labkey.selectRows(
				baseUrl=BASE_URL,
				folderPath="/Optides/InSilicoAssay/MolecularProperties",
				schemaName="assay.General.InSilicoAssay",
				queryName="Data",
				colNameOpt="fieldname",
				showHidden=TRUE,
				colFilter=makeFilter(c("ID", "EQUALS", ConstructID)),
				colSelect=c("AverageMass"))
			if(length(MW_inputs[,1]) == 0){
				cat(curChemProductionRow$CHEMProductionID, ": ConstructID ", ConstructID, ", which mapped from ODTProductionID ", curChemProductionRow$OTDProductionID, " has no associated AverageMass value in InSilicoAssay.  Skipping this calculation.\n")
			}else{
				MW_input <- MW_inputs[1, 1]
			}
		}
	}else if(!is.na(curChemProductionRow$VariantID) && curChemProductionRow$VariantID != ""){
		# 2) if VariantID is set:
		# get the sequence
		variantSequences <- labkey.selectRows(
			baseUrl=BASE_URL,
			folderPath="/Optides/CompoundsRegistry/Samples",
			schemaName=SAMPLE_SETS_SCHEMA_NAME,
			queryName="Variant",
			colNameOpt="fieldname",
			showHidden=TRUE,
			colFilter=makeFilter(c("ID", "EQUALS", curChemProductionRow$VariantID)),
			colSelect=c("AASeq"))
		if(length(variantSequences[,1]) == 0){
			cat(curChemProductionRow$CHEMProductionID, ": VariantID ", curChemProductionRow$VariantID, " has no associated sequence value in the Variant SampleSet.  Skipping this calculation.\n")
		}else{
			variantSequence <- variantSequences[1, 1]
			#calculate mass from sequence
			MW_input = DSBMWCalc(variantSequence)
		}
	}else if(!is.na(curChemProductionRow$DrugReagentID) && curChemProductionRow$DrugReagentID != ""){
		# 3) DrugReagent is set. get weight from CompoundsRegistry Reagents table AverageMW.
		MW_inputs  <- labkey.selectRows(
			baseUrl=BASE_URL,
			folderPath="Optides/CompoundsRegistry/Samples",
			schemaName=SAMPLE_SETS_SCHEMA_NAME,
			queryName="Reagents",
			colNameOpt="fieldname",
			showHidden=TRUE,
			colFilter=makeFilter(c("ReagentID", "EQUALS", curChemProductionRow$DrugReagentID)),
			colSelect=c("AverageMass"))
		if(length(MW_inputs[,1]) == 0){
			cat(curChemProductionRow$CHEMProductionID, ": DrugReagentID ", curChemProductionRow$VariantID, " has no associated AverageMass value in the Reagents SampleSet.  Skipping this calculation.\n")
		}
		MW_input <- MW_inputs[1, 1]
	}
	
	if(MW_input == 0.0){
		#no condition was met, something is wrong
		chemProdReport$Percent_Yield[i] = NA
	}else{
		MW_input<- as.numeric(MW_input)
		MW_output <- as.numeric(MW_output)
		chemProdReport$ProductionAmount_mg[i] <- as.numeric(chemProdReport$ProductionAmount_mg[i])
		chemProdReport$StartingAmount_mg[i] <- as.numeric(chemProdReport$StartingAmount_mg[i])
		chemProdReport$Percent_Yield[i] = round(((chemProdReport$ProductionAmount_mg[i] * 0.001/MW_output)/(chemProdReport$StartingAmount_mg[i] *0.001/MW_input))*100.0, digit=2)
	}
}

#report problems calculating Mol Yield (%)
for(i in 1:length(chemProdReport$ChemProductionID)){
	if(is.na(chemProdReport$Percent_Yield[i])){
		cat("Something went wrong with the Mol Yield (%) calculation for ", chemProdReport$ChemProductionID[i], "\n") 
	}
}

##
##insert into DB
##
ssi3 <- labkey.updateRows(
	baseUrl=BASE_URL,
	folderPath=CONTAINER_PATH,
	schemaName=SAMPLE_SETS_SCHEMA_NAME,
	queryName="ChemProductionReport",
	toUpdate=chemProdReport
)

if(!exists("ssi3")){
	stop("In ChemProductionReport, the insertion into the database failed.  Please contact the administrator.")
}else{
	#completed
	cat("In ChemProductionReport, ", length(chemProdReport$ChemProductionID), " RECORDS HAVE BEEN UPDATED.  These are their ChemProductionID's:\n")
	for(i in 1:length(chemProdReport$ChemProductionID)){
		if(!is.na(chemProdReport$Percent_Yield[i])){
			cat(chemProdReport$ChemProductionID[i], "\n")
		}else
			cat(chemProdReport$ChemProductionID[i], "'s Mol Yield (%) could not be updated properly.", "\n")
	}
}

