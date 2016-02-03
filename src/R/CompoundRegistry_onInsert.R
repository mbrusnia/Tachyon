###
### This Labkey Transformation script will do 4 things upon insertion of new
### peptide sequences into any of the 3 compound Registry Assay tables:
### 	1) Will verify the ParentID's of all incoming sequences (that they are valid)
###	2) Will make sure than non of the incoming sequences are already in the database
###	3) Will calculate AverageMass, MonoisotopicMass, and pI for each sequence
###	4) Will save all the data as follows:
###		a) ID, ParentID, Sequence, AverageMass, MonoMass, and pI in the Assay
###		b) Everything BUT those 3 calculated values in the Sample Set
###
options(stringsAsFactors = FALSE)
options(digits=10)
suppressWarnings(suppressMessages(require(Rlabkey)))
suppressWarnings(suppressMessages(require(Peptides)))
suppressWarnings(suppressMessages(require(stringr)))

baseUrl<-"http://optides-stage.fhcrc.org/"
source("${srcDirectory}/Utils.R")

## These next four constants point to the data we wish to query from the DB
ASSAY_SCHEMA_NAME = "assay.General.InSilicoAssay"
ASSAY_QUERY_NAME = "Data"
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "Parent ID"
SAMPLES_SCHEMA = "Samples"
COMPOUND_TABLE = "Construct"
COMPOUND_FOLDER = "/Optides/CompoundsRegistry/Samples"

## Hydrogen's mass needed for mass calculations
H_ISO_MASS = 1.0078250

########################################
# FUNCTIONS
########################################
uniquenessCheck <- function(arg1, arg2){
	matches <- match(arg1, arg2)
	matches <- matches[!is.na(matches)]
	if(length(matches) > 0){
		cat("ERROR: No duplicates allowed. The following sequences have previously been uploaded into the repository: \n")
		for(i in 1:length(matches)){
			cat("ID: ", inputDF[matches[i],COMPOUND_ID_COL_NAME], ": ", inputDF[matches[i],SEQUENCE_COL_NAME], "\n")
		}
		return(FALSE)
	}
	return(TRUE)
}
mysetdiff<-function (x, y, multiple=FALSE) {
    x <- as.vector(x)
    y <- as.vector(y)
    if (length(x) || length(y)) {
        if (!multiple) {
             unique( x[match(x, y, 0L) == 0L])  
              }else  x[match(x, y, 0L) == 0L] 
        } else x
}
mymw <- function (seq, monoisotopic = FALSE) 
{
    seq <- gsub("[\r\n ]", "", seq)
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
    sum(weight[c(strsplit(toupper(seq), split = "")[[1]], "H2O")], 
        na.rm = TRUE)
}
#calculate mass of formula with format: C12H6N3, etc.
calc_formula_monomass <- function(formula){
	weight <- c(H = 1.0078250, O = 15.9949146, C = 12.0000000, N = 14.0030740, P = 30.9737633, S = 31.9720718, F = 18.998403)
	letters <- str_split(formula, "[0-9]+")[[1]]
	counts <- str_split(formula, "[A-Za-z]+")[[1]]
	letters <- letters[!letters == ""]
	counts <- counts[!counts == ""]

	if(length(letters) != length(counts)){
		stop(paste("This chemical formula is not properly constructed: ", formula, ". Please fix and try again."))
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
# END FUNCTIONS

${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

## get all previously uploaded sequences
parentSampleSetIDs <- labkey.selectRows(baseUrl, "experiment/Optides/CompoundsRegistry/Samples/", "Samples", "Variant",
    viewName = NULL, colSelect = COMPOUND_ID_COL_NAME, maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

## read the input data frame just to get the column headers.
inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t", check.names=FALSE)



#########################################################
## 1) Verify ParentIDs (that they exists in the Sample Set)
#########################################################
#NA check.  prompt user if there are missting parent IDs in his input
if(length(inputDF[,is.na(inputDF[,PARENT_ID_COL_NAME])]) > 0){
	stop("There are sequences in your input that do not have a Parent ID.  Please provide a Parent ID for all sequences and then try again.")
}

a <- mysetdiff(inputDF[,PARENT_ID_COL_NAME], parentSampleSetIDs[,COMPOUND_ID_COL_NAME], multiple=TRUE)
if(length(a) > 0){
	cat("ERROR: Some of the ParentIDs were not found in the ", COMPOUND_TABLE, " Table. The following ParentID were not found: \n")
	for(i in 1:length(a)){
		cat(i, ": ", a[i], "\n")
	}
	stop("Please correct these Parent IDs and try again.")
}


##############################################################
## 2) Check for duplicates in input data. if so, list the row and sequence, then throw error
##############################################################
duplicates = duplicated(inputDF[,SEQUENCE_COL_NAME])
if(length(duplicates[duplicates == TRUE]) > 0){
	cat("ERROR: No duplicates allowed. Your input file contains the following duplicate sequences: \n")
	for(i in 1:length(duplicates)){
		if(duplicates[i]){
			cat("ID: ", inputDF[i,COMPOUND_ID_COL_NAME], ": ", inputDF[i,SEQUENCE_COL_NAME], "\n")
		}
	}
	stop("Please remove the duplicates from your input file and try again.")
}


##
## check if the new sequences have previously been loaded into the database. 
## if so, list the rows and sequences of the repeated sequences
##

## get all previously uploaded sequences
previousSampleSetContents <- labkey.selectRows(baseUrl, "experiment/Optides/CompoundsRegistry/Samples/", "Samples", "Construct",
    viewName = NULL, colSelect = c(COMPOUND_ID_COL_NAME, PARENT_ID_COL_NAME, SEQUENCE_COL_NAME), maxRows = NULL,
    rowOffset = NULL, colSort = NULL,	colFilter=NULL, showHidden = FALSE, colNameOpt="caption",
    containerFilter=NULL)

if(!uniquenessCheck(previousSampleSetContents[,SEQUENCE_COL_NAME],inputDF[,SEQUENCE_COL_NAME])){
	stop("Please remove the duplicates from your input file and try again.")
}


############################################################
## 3) calculate average mass, monoisotopic mass, and pI
############################################################
toinsert <- inputDF
for (i in 1:length(toinsert[,SEQUENCE_COL_NAME])){
	#if it's a chemical formula...
	if(str_detect(toinsert[i,SEQUENCE_COL_NAME], "[1-9]+")){
		toinsert$MonoisotopicMass[i] <- calc_formula_monomass(inputDF[i, SEQUENCE_COL_NAME])
	}else{
		#if it's a peptide sequence...
		Cs <- floor(str_count(inputDF[i, SEQUENCE_COL_NAME], "C")/2) * 2
		toinsert$AverageMass[i] <- mymw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=FALSE) - Cs * H_ISO_MASS
		toinsert$MonoisotopicMass[i] <- mymw(inputDF[i, SEQUENCE_COL_NAME], monoisotopic=TRUE) - Cs * H_ISO_MASS
		toinsert$pI[i] <- pI(inputDF[i, SEQUENCE_COL_NAME], pKscale="EMBOSS")
	}
}

###########################################################
## 4) Insert data to Database
###########################################################
#insert input into compound registry sample set (error will be thrown in ID already exists)
labkey.insertRows(baseUrl, COMPOUND_FOLDER, SAMPLES_SCHEMA, COMPOUND_TABLE, inputDF)

#insert input into assay
write.table(toinsert,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)



