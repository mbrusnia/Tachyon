##----------------------------------------------------------------
# This script queries AA sequences from a given table, calculates 
# monoisotopic mass, average mass, and isoelectric point (pI) for 
# them, then inserts the results into a given assay
##----------------------------------------------------------------

library(Rlabkey)
library(Peptides)



#Parameters for this script (login script: _netrc)
BASE_URL = "http://optides-stage.fhcrc.org/"
FOLDER_PATH = "/Optides"
#BASE_URL = "http://localhost:8080/labkey"
#FOLDER_PATH = "/Optides 01"
SCHEMA_NAME = "Samples"
QUERY_NAME  = "IdentifiedCompounds"
COMPOUNDID_COLNAME = "CompoundID"
SEQUENCE_COLNAME = "Sequence"

DESTINATION_ASSAY_NAME = "InSilicoAssay"
DESTINATION_FOLDER_PATH = FOLDER_PATH

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
previousAssayResults <- labkey.selectRows(BASE_URL, FOLDER_PATH, paste("assay.General.", DESTINATION_ASSAY_NAME, sep=""), "Data", viewName = NULL, colSelect = c(COMPOUNDID_COLNAME , SEQUENCE_COLNAME ), maxRows = NULL, rowOffset = NULL, colSort = NULL, colFilter = NULL, showHidden = FALSE)

#calculate desired values
avg = vector('numeric')
monoisotopic = vector('numeric')
pIs = vector('numeric')

for(i in 1:length(mydata$Sequence)){
	#cat(Sequence[i], "   ", mw(mydata$Sequence[i], monoisotopic=TRUE), "\n")
	if(!mydata$Sequence %in% previousAssayResults$Sequence){
		avg[i] = mw(mydata$Sequence[i], monoisotopic=FALSE)
		monoisotopic[i] = mw(mydata$Sequence[i], monoisotopic=TRUE)
		pIs[i] = pI(mydata$Sequence[i], pKscale="Sillero")
	}
}

mydata$MonoisotopicMass = monoisotopic
mydata$AverageMass = avg
mydata$pI = pIs

#insert results as a batch into our destination assay
if(length(monoisotopic) > 0){
	labkey.saveBatch(BASE_URL, DESTINATION_FOLDER_PATH, DESTINATION_ASSAY_NAME, mydata,
	batchPropertyList=NULL, runPropertyList=NULL)
}


