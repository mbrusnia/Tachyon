##----------------------------------------------------------------
# This script queries AA sequences from a given table, calculates 
# monoisotopic mass, average mass, and isoelectric point (pI) for 
# them, then inserts the results into a given assay
##----------------------------------------------------------------

library(Rlabkey)
library(Peptides)



#Parameters for this script
BASE_URL = "http://localhost:8080/labkey"
FOLDER_PATH = "/Optides 01"
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


avg = vector('numeric')
monoisotopic = vector('numeric')
pIs = vector('numeric')

for(i in 1:length(Sequence)){
	#cat(Sequence[i], "   ", mw(mydata$Sequence[i], monoisotopic=TRUE), "\n")
	avg[i] = mw(mydata$Sequence[i], monoisotopic=FALSE)
	monoisotopic[i] = mw(mydata$Sequence[i], monoisotopic=TRUE)
	pIs[i] = pI(mydata$Sequence[i], pKscale="Sillero")
}

mydata$MonoisotopicMass = monoisotopic
mydata$AverageMass = avg
mydata$pI = pIs

#insert results as a batch into our destination assay
labkey.saveBatch(BASE_URL, DESTINATION_FOLDER_PATH, DESTINATION_ASSAY_NAME, mydata,
batchPropertyList=NULL, runPropertyList=NULL)


