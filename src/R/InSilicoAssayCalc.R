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
SEQUENCE_COLNAME = "AASeq"

DESTINATION_ASSAY_NAME = "InSilicoAssay"
DESTINATION_FOLDER_PATH = "Optides/InSilicoAssay/MolecularProperties"

H_ISO_MASS = 1.0078250

#query sequence data
mydata <- labkey.selectRows(
	baseUrl= BASE_URL,
	folderPath= FOLDER_PATH,
	schemaName= SCHEMA_NAME,
	queryName=QUERY_NAME,
	viewName=NULL,
	colFilter=NULL,
	colSelect = c(COMPOUNDID_COLNAME , SEQUENCE_COLNAME ),
	containerFilter=NULL
)

#remove compounds for which we have already done the calculation
previousAssayResults <- labkey.selectRows(BASE_URL, DESTINATION_FOLDER_PATH, paste("assay.General.", DESTINATION_ASSAY_NAME, sep=""), "Data", viewName = NULL, colSelect = c(COMPOUNDID_COLNAME , SEQUENCE_COLNAME ), maxRows = NULL, rowOffset = NULL, colSort = NULL, colFilter = NULL, showHidden = FALSE)

#calculate desired values
toinsert = data.frame()
toinsert$ID = vector('character')
toinsert$AASeq = vector('character')
toinsert$AverageMass = vector('numeric')
toinsert$MonoisotopicMass = vector('numeric')
toinsert$pI = vector('numeric')

for(i in 1:length(mydata$AASeq)){
	if(length(previousAssayResults$AASeq) == 0 || !mydata$AASeq[i] %in% previousAssayResults$AASeq){
		
		col1 = mydata$ID[i]
		col2 = mydata$AASeq[i]
		Cs = floor(str_count(mydata$AASeq[i], "C")/2) * 2
		col3 = mw(mydata$AASeq[i], monoisotopic=FALSE) - Cs * H_ISO_MASS
		col4 = mw(mydata$AASeq[i], monoisotopic=TRUE) - Cs * H_ISO_MASS
		col5 = pI(mydata$AASeq[i], pKscale="EMBOSS")
		toinsert = rbind(toinsert, data.frame(ID=col1, AASeq=col2, AverageMass=col3, MonoisotopicMass=col4, pI=col5))
	}
}


#insert results as a batch into our destination assay
if(length(toinsert$MonoisotopicMass) > 0){
	bpl <- list(name=paste("Batch ", as.character(format(Sys.time(), "%Y%M%d"))))
	rpl <- list(name=paste("Assay Run ", as.character(format(Sys.time(), "%Y%M%d"))))
	labkey.saveBatch(BASE_URL, DESTINATION_FOLDER_PATH, DESTINATION_ASSAY_NAME, toinsert,
		batchPropertyList=bpl, runPropertyList=rpl)
}
