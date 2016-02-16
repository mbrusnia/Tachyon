##----------------------------------------------------------------
# This script queries AA sequences from a given table, calculates 
# monoisotopic mass, average mass, and isoelectric point (pI) for 
# them, then inserts the results into a given assay
##----------------------------------------------------------------

library(Rlabkey)
library(Peptides)
library(stringr)


#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"
FOLDER_PATH = "Optides/CompoundsRegistry/Samples"
SCHEMA_NAME = "Samples"
QUERY_NAME  = "Construct"
COMPOUNDID_COLNAME = "ID"
PARENTID_COLNAME = "Parent ID"
SEQUENCE_COLNAME = "AASeq"

DESTINATION_ASSAY_NAME = "InSilicoAssay"
DESTINATION_FOLDER_PATH = "Optides/InSilicoAssay/MolecularProperties"

H_ISO_MASS = 1.0078250

#############
## FUNCTIONS
#############
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
###############
#END FUNCTIONS
###############

#query sequence data
mydata <- labkey.selectRows(
	baseUrl= BASE_URL,
	folderPath= FOLDER_PATH,
	schemaName= SCHEMA_NAME,
	queryName=QUERY_NAME,
	viewName=NULL,
	colFilter=NULL,
	colSelect = c(COMPOUNDID_COLNAME , PARENTID_COLNAME, SEQUENCE_COLNAME ),
	containerFilter=NULL
)

#remove compounds for which we have already done the calculation
previousAssayResults <- labkey.selectRows(BASE_URL, DESTINATION_FOLDER_PATH, paste("assay.General.", DESTINATION_ASSAY_NAME, sep=""), "Data", viewName = NULL, colSelect = c(COMPOUNDID_COLNAME , PARENTID_COLNAME, SEQUENCE_COLNAME ), maxRows = NULL, rowOffset = NULL, colSort = NULL, colFilter = NULL, showHidden = FALSE)

#calculate desired values
toinsert = data.frame(stringsAsFactors=FALSE)
toinsert$ID = vector('character') 
toinsert[,PARENTID_COLNAME] = vector('character')
toinsert$AASeq = vector('character')
toinsert$AverageMass = vector('numeric')
toinsert$MonoisotopicMass = vector('numeric')
toinsert$pI = vector('numeric')

for(i in 1:length(mydata$AASeq)){
	if(length(previousAssayResults$AASeq) == 0 || !mydata$AASeq[i] %in% previousAssayResults$AASeq){
		col1 = mydata$ID[i]
		col2 = mydata$AASeq[i]
		col3 = mymw(mydata$AASeq[i], monoisotopic=FALSE)
		col4 = mymw(mydata$AASeq[i], monoisotopic=TRUE)
		col5 = pI(mydata$AASeq[i], pKscale="EMBOSS")
		col6 = mydata[i,PARENTID_COLNAME]
		cat(col6, "\n")
		toinsert = rbind(toinsert, data.frame(ID=col1, AASeq=col2, AverageMass=col3, MonoisotopicMass=col4, pI=col5, "Parent ID"=as.character(col6)))
	}
}
names(toinsert)[6] = "Parent ID"

#insert results as a batch into our destination assay
if(length(toinsert$MonoisotopicMass) > 0){
	bpl <- list(name=paste("Batch ", as.character(format(Sys.time(), "%Y%M%d"))))
	rpl <- list(name=paste("Assay Run ", as.character(format(Sys.time(), "%Y%M%d"))))
	labkey.saveBatch(BASE_URL, DESTINATION_FOLDER_PATH, DESTINATION_ASSAY_NAME, toinsert,
		batchPropertyList=bpl, runPropertyList=rpl)
}
