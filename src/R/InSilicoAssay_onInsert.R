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

#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-prod.fhcrc.org/"
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "ParentID"
ALTERNATE_NAME_COL_NAME = "AlternateName"

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

#calculate molecular weight of a peptide string ("ACPKGGS", for example)
mymw <- function (seq, monoisotopic = FALSE) 
{
    seq <- gsub("[\r\n ]", "", seq)

	## Hydrogen's mass needed for mass calculations
	H_ISO_MASS = 1.00794

    if (monoisotopic == TRUE) {
        weight <- c(A = 71.037114, R = 156.101111, N = 114.042927, 
            D = 115.026943, C = 103.009185, E = 129.042593, Q = 128.058578, 
            G = 57.021464, H = 137.058912, I = 113.084064, L = 113.084064, 
            K = 128.094963, M = 131.040485, F = 147.068414, P = 97.052764, 
            S = 87.032028, T = 101.047679, W = 186.079313, Y = 163.06332, 
            V = 99.068414, U = 150.95363, O = 237.14772, H2O = 18.01056)
    } else {
        weight <- c(A = 71.0779, R = 156.1857, N = 114.1026, 
            D = 115.0874, C = 103.1429, E = 129.114, Q = 128.1292, 
            G = 57.0513, H = 137.1393, I = 113.1576, L = 113.1576, 
            K = 128.1723, M = 131.1961, F = 147.1739, P = 97.1152, 
            S = 87.0773, T = 101.1039, W = 186.2099, Y = 163.1733, 
            V = 99.1311, U = 150.0379, O = 237.3018, H2O = 18.01056)
    }
    sum(weight[c(strsplit(toupper(seq), split = "")[[1]], "H2O")], na.rm = TRUE) - floor(str_count(seq, "C")/2) * 2 * H_ISO_MASS
}

#calculate mass of formula with format: C12H6N3, etc.
calc_formula_monomass <- function(formula){
	weight <- c(H = 1.0078250, O = 15.9949146, C = 12.0000000, N = 14.0030740, P = 30.9737633, S = 31.9720718, F = 18.998403)
	letters <- str_split(formula, "[0-9]+")[[1]]
	counts <- str_split(formula, "[A-Za-z]+")[[1]]
	letters <- letters[!letters == ""]
	counts <- counts[!counts == ""]

	if(length(letters) != length(counts)){
		stop(paste("This chemical formula is not properly constructed: ", formula, ". Each chemical symbol has to be followed by a number (yes, even if it's only 1). Please fix and try again."))
	}

	weight_total = 0
	for(i in 1:length(letters)){
		if (!letters[i] %in% names(weight)){
			stop(paste("This formula: ", formula, " contains the invalid character '", letters[i], ".  Valid characters are: CHONPSF.  Please fix and try again."))
		}
		#cat(letters[i], " ", weight[letters[i]], " ", counts[i], " ", as.numeric(counts[i]), "\t", weight[letters[i]] * as.numeric(counts[i]), "\n")
		weight_total = weight_total + as.numeric(weight[letters[i]]) * as.numeric(counts[i])
	}
	weight_total
}
###############
#END FUNCTIONS
###############

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, BASE_URL)

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
		inputDF$AverageMass[i] <- mymw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE)
		inputDF$MonoisotopicMass[i] <- mymw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE)
		inputDF$ReducedForm_pI[i] <- pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS")
        inputDF$NetChargeAtpH7_4[i]<-round(charge(inputDF[i, SEQUENCE_COL_NAME], pH=7.4, pKscale="Sillero"), digit=2)
		inputDF$HydrophobicityAtpH7_5[i] <- round(hydrophobicity(inputDF[i, SEQUENCE_COL_NAME], scale="Cowan7.5"), digit=2)
	}
}
###################################################################
## 3) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)