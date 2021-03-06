#this script copies all data from prod to stage.  it assumes the DB schemas are the same
library(Rlabkey)

BASE_URL_FROM = "http://optides-prod.fhcrc.org"
BASE_URL_TO = "http://optides-stage.fhcrc.org"

sampleSetCopyData <- function(FOLDER_PATH, SAMPLESET, BASE_URL_FROM, BASE_URL_TO){
	thisSampleSet <- labkey.selectRows( 
	    baseUrl=BASE_URL_FROM, 
	    folderPath=FOLDER_PATH, 
	    schemaName="samples", 
	    queryName=SAMPLESET, 
	    colNameOpt="fieldname" 
	) 
	thisSampleSet <- cbind(thisSampleSet, labkey.selectRows( 
	    baseUrl=BASE_URL_FROM, 
	    folderPath=FOLDER_PATH, 
	    schemaName="samples", 
	    queryName=SAMPLESET, 
	    colNameOpt="fieldname", 
	    colSelect="Flag/Comment" 
	)) 
	thisSampleSet$Flag <- thisSampleSet[,"Flag/Comment"]
 
	thisSampleSetB <- thisSampleSet[!is.na(thisSampleSet$Flag),]
	thisSampleSet <- thisSampleSet[is.na(thisSampleSet$Flag),]

	pCI <- labkey.insertRows( 
	   baseUrl=BASE_URL_TO, 
	   folderPath=FOLDER_PATH, 
	   schemaName="samples", 
 	   queryName=SAMPLESET, 
 	   toInsert=thisSampleSet
	)
	for (index in 1:nrow(thisSampleSetB)) {
		pCI <- labkey.insertRows( 
		   baseUrl=BASE_URL_TO, 
		   folderPath=FOLDER_PATH, 
		   schemaName="samples", 
 		   queryName=SAMPLESET, 
 		   toInsert=thisSampleSetB[index,]
		)
	}
}


###insert everything from optide-prod.Homologue, Variant, and Construct
sampleSetCopyData("/Optides/CompoundsRegistry/Samples", "Homologue", BASE_URL_FROM, BASE_URL_TO)
sampleSetCopyData("/Optides/CompoundsRegistry/Samples", "Variant", BASE_URL_FROM, BASE_URL_TO)
sampleSetCopyData("/Optides/CompoundsRegistry/Samples", "Construct", BASE_URL_FROM, BASE_URL_TO)

###insert everything from optide-prod.HTProduction
sampleSetCopyData("/Optides/CompoundsRegistry/Samples", "HTProduction", BASE_URL_FROM, BASE_URL_TO)

###insert everything from optide-prod.SGI_DNA
sampleSetCopyData("/Optides/CompoundsRegistry/Samples", "SGI_DNA", BASE_URL_FROM, BASE_URL_TO)

###insert everything from optide-prod.OTDProduction
prodOTDProduction <- labkey.selectRows(
	baseUrl=BASE_URL_FROM,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	colNameOpt="fieldname"
)
prodOTDProduction <- cbind(prodOTDProduction , labkey.selectRows( 
	    baseUrl=BASE_URL_FROM, 
	    folderPath="/Optides/CompoundsRegistry/Samples", 
	    schemaName="samples", 
	    queryName="OTDProduction", 
	    colNameOpt="fieldname", 
	    colSelect="Flag/Comment" 
	))

for(i in 1:length(prodOTDProduction$OTDProductionID)){
	if(is.na(prodOTDProduction$AAAnalysis_mg_ml_[i])){
		prodOTDProduction$AAAnalysis_mg_ml_[i] <- ""
		prodOTDProduction$AAAReportDate[i] <- as.Date("2000-01-01")
	}
}
prodOTDProduction$Flag = <- prodOTDProduction[,"Flag/Comment"]
prodOTDProductionB <- prodOTDProduction[!is.na(prodOTDProduction$Flag),]
prodOTDProduction<- prodOTDProduction[is.na(prodOTDProduction$Flag),]

pOTDPI <- labkey.insertRows(
	baseUrl=BASE_URL_TO,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	toInsert=prodOTDProduction
)
for (index in 1:nrow(prodOTDProductionB)) {
		pCI <- labkey.insertRows( 
		   baseUrl=BASE_URL_TO, 
		   folderPath="/Optides/CompoundsRegistry/Samples", 
		   schemaName="samples", 
 		   queryName="OTDProduction", 
 		   toInsert=prodOTDProductionB[index,]
		)
}


