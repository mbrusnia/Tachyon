###
### This Labkey Transformation script will do 3 things upon insertion of new
### peptide sequences into the InSilicoAssay table:
###	1) Will make sure than none of the incoming sequences are already in the InSilicoAssay Table (identify duplicates)
###	2) Will calculate AverageMass, MonoisotopicMass, and pI for each sequence
###	3) Will save all the data as follows:
###		a) ID, ParentID, Sequence, AverageMass, MonoMass, and pI in the Assay
###
### 3/29/2016 update: Step #1 has been removed, as it has shown to expose buggy behavior in the labkey framework

options(stringsAsFactors = FALSE)
options(digits=10)
suppressWarnings(suppressMessages(require(Peptides)))
suppressWarnings(suppressMessages(require(stringr)))

source("${srcDirectory}/Utils.R")

########################################
# FUNCTIONS
########################################
#return those elements in x that are not in y
mysetdiff<-function (x, y, multiple=FALSE) {
    x <- as.vector(x)
    y <- as.vector(y)
    if (length(x) || length(y)) {
        if (!multiple) {
             unique( x[match(x, y, 0L) == 0L])  
              }else  x[match(x, y, 0L) == 0L] 
        } else x
}

###############
#END FUNCTIONS
###############


${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath)

#Parameters for this script (login script: _netrc)
BASE_URL = params$baseUrl
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"
ALTERNATE_NAME_COL_NAME = "AlternateName"

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else{ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")
}

#change the column name of parent.ID and alternate.name
names(inputDF)[grepl("[Pp]arent.ID", names(inputDF))] <- PARENT_ID_COL_NAME
names(inputDF)[grepl("[Aa]lternate.[Nn]ame", names(inputDF))] <- ALTERNATE_NAME_COL_NAME

##############################################################
## 1) Check for duplicates in input data. if so, list the row and sequence, then throw error
## MYB No sequence duplicate check due to vector differences
##############################################################
#duplicates = duplicated(inputDF[,SEQUENCE_COL_NAME])
#duplicates_vec = duplicate(inputDF[,
#if(length(duplicates[duplicates == TRUE]) > 0){
#	cat("ERROR: No duplicates allowed. Your input file contains the following duplicate sequences: \n")
#	for(i in 1:length(duplicates)){
#		if(duplicates[i]){
#			cat("ID: ", inputDF[i,COMPOUND_ID_COL_NAME], ": ", inputDF[i,SEQUENCE_COL_NAME], "\n")
#		}
#	}
#	stop("Please remove the duplicates from your input list or file and try again.")
#}


##
## check if the new sequences have previously been loaded into the database. 
##
##  removed.

#####################################################################################################
## 2) calculate average mass, monoisotopic mass, pI, netcharge at pH=7.4 and hydrophobicity at pH=7.5
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
		inputDF$MonoisotopicMass[i] <- calc_formula_monomass(inputDF[i, SEQUENCE_COL_NAME])
	}else{
		#if it's a peptide sequence...
		inputDF$AverageMass[i] <- DSBMWCalc(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE)
		inputDF$MonoisotopicMass[i] <- DSBMWCalc(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE)
		inputDF$ReducedForm_pI[i] <- pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS")
        inputDF$NetChargeAtpH7_4[i]<-round(charge(inputDF[i, SEQUENCE_COL_NAME], pH=7.4, pKscale="Sillero"), digit=2)
		inputDF$HydrophobicityAtpH7_5[i] <- round(hydrophobicity(inputDF[i, SEQUENCE_COL_NAME], scale="Cowan7.5"), digit=2)
	}
}
###################################################################
## 3) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)