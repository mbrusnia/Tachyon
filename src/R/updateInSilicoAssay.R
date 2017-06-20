##
## This script pulls all CONSTRUCT_IDs that are currently NOT in InSilicoAssay
## and calculates their AverageMass, MonoisotopicMass, and pI for each sequence
##
## This script is called every Saturday at 1am by windows task scheduler
##
options(stringsAsFactors = FALSE)
library(Rlabkey)
library(stringr)
library(Peptides)

source("C:/labkey/labkey/files/Optides/@files/Utils.R")

BASE_URL = "https://optides-prod.fhcrc.org"

COMPOUND_ID_COL_NAME = "ID"
SEQUENCE_COL_NAME = "AASeq"

CONTAINER_PATH = "/Optides/CompoundsRegistry/Samples"
SAMPLE_SETS_SCHEMA_NAME = "samples"

cat("\n-------- ", date(), " --------\n")
cat("updating InSilicoAssay\n")
cat("Updating server: ", BASE_URL, "\n")


###########################################################################
##  create object "inputDF" which contains only the constructs which need to
##  have their values calculated and be inserted into InsilicoAssay
###########################################################################
constructs <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/CompoundsRegistry/Samples",
    schemaName="samples",
    queryName="Construct",
	colNameOpt="fieldname", 
	colFilter=makeFilter(c("OTDProductionID", "NOT_MISSING", "")),  
    colSelect=c("ID", "ParentID", "AlternateName", "AASeq", "Vector")
)

alreadyInsertedConstructs <- labkey.selectRows(
    baseUrl=BASE_URL,
    folderPath="/Optides/InSilicoAssay/MolecularProperties",
    schemaName="assay.General.InSilicoAssay",
    queryName="Data",
	colNameOpt="fieldname", 
	colFilter=makeFilter(c("OTDProductionID", "NOT_MISSING", "")),  
    colSelect="ID"
)

#pull out the constructs that are in Construct but not in InSilicoAssay and 
#put them in the object named "inputDF"
for(i in 1:length(constructs$ID)){
	if(!constructs$ID[i] %in% alreadyInsertedConstructs$ID){
		if(!exists("inputDF")){
			inputDF <- constructs[i,]
		}else{
			inputDF <- rbind(inputDF, constructs[i,])
		}
	}
}
if(!exists("inputDF")){
	cat("No new constructs found.  Exiting.\n")
	stop("No new constructs found.\n")
}
#####################################################################################################
## calculate average mass, monoisotopic mass, pI, netcharge at pH=7.4 and hydrophobicity at pH=7.5
#####################################################################################################
if(!"AverageMass" %in% names(inputDF)){
	inputDF <- cbind(inputDF, AverageMass = vector(length=length(inputDF[,COMPOUND_ID_COL_NAME])))
	inputDF$AverageMass[] <- NA
}
if(!"MonoisotopicMass" %in% names(inputDF)){
	inputDF <- cbind(inputDF, MonoisotopicMass = vector(length=length(inputDF[,COMPOUND_ID_COL_NAME])))
	inputDF$MonoisotopicMass[] <- NA
}
if(!"ReducedForm_pI" %in% names(inputDF)){
	inputDF <- cbind(inputDF, ReducedForm_pI = vector(length=length(inputDF[,COMPOUND_ID_COL_NAME])))
	inputDF$ReducedForm_pI[] <- NA
}
if(!"NetChargeAtpH7_4" %in% names(inputDF)){
	inputDF <- cbind(inputDF, NetChargeAtpH7_4 = vector(length=length(inputDF[,COMPOUND_ID_COL_NAME])))
	inputDF$NetChargeAtpH7_4[] <- NA
}
if(!"HydrophobicityAtpH7_5" %in% names(inputDF)){
	inputDF <- cbind(inputDF, HydrophobicityAtpH7_5 = vector(length=length(inputDF[,COMPOUND_ID_COL_NAME])))
	inputDF$HydrophobicityAtpH7_5[] <- NA
}
for (i in 1:length(inputDF[,SEQUENCE_COL_NAME])){
	#if it's a chemical formula...
	if(str_detect(inputDF[i,SEQUENCE_COL_NAME], "[1-9]+")){
		inputDF$AverageMass[i] <- round(calc_formula_mass(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE), digit=2)
		inputDF$MonoisotopicMass[i] <- round(calc_formula_mass(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE), digit=2)
	}else{
		#if it's a peptide sequence...
		inputDF$AverageMass[i] <- round(DSBMWCalc(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE), digit=2)
		inputDF$MonoisotopicMass[i] <- round(DSBMWCalc(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE), digit=2)
		inputDF$ReducedForm_pI[i] <- round(pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS"), digit=2)
        inputDF$NetChargeAtpH7_4[i]<-round(charge(inputDF[i, SEQUENCE_COL_NAME], pH=7.4, pKscale="Sillero"), digit=2)
		inputDF$HydrophobicityAtpH7_5[i] <- round(hydrophobicity(inputDF[i, SEQUENCE_COL_NAME], scale="Cowan7.5"), digit=2)
	}
}
inputDF[is.na(inputDF$AlternateName), "AlternateName"] = ""
inputDF[is.na(inputDF$Vector), "Vector"] = ""

###################################################################
## Insert data to Database
###################################################################
bpl <- list(name=paste("Automated_InSilicoAssayInsert_", format(Sys.Date(), "%m_%d_%Y")))
rpl <- list(name=paste("Automated_InSilicoAssayInsert_", format(Sys.Date(), "%m_%d_%Y")))

assayInfo<- labkey.saveBatch(	baseUrl=BASE_URL,	
	folderPath="/Optides/InSilicoAssay/MolecularProperties/",	
	assayName="InSilicoAssay", 
	resultDataFrame=inputDF,
	batchPropertyList=bpl,
	runPropertyList=rpl
)


if(!exists("assayInfo")){
	stop("There was a problem with the insertion.  Please contact administrator.")
}else{
	cat(length(inputDF$ID), " new constructs inserted into InSilicoAssay.  These are their IDs:\n")
	for(i in 1:length(inputDF$ID)){
		if(i %% 4 == 1){ cat("\n")}
		cat(inputDF$ID[i], "\t")
	}
	cat("\n")
}

