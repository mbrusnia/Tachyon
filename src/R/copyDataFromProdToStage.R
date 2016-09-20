#this script copies all data from prod to stage.  it assumes the DB schemas are the same

###insert everything from optide-prod.Homologue
prodHomologue <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Homologue",
	colNameOpt="fieldname"
)
pHI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Homologue",
	toInsert=prodHomologue
)

###insert everything from optide-prod.Variant
prodVariant <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Variant",
	colNameOpt="fieldname"
)
pVI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Variant",
	toInsert=prodVariant
)

###insert everything from optide-prod.Construct
prodConstruct <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	colNameOpt="fieldname"
)
pCI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	toInsert=prodConstruct
)
###insert everything from optide-prod.HTProduction
prodHTProduction <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colNameOpt="fieldname"
)
prodHTProduction$Flag = ""
pHTPI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	toInsert=prodHTProduction
)


###insert everything from optide-prod.OTDProduction
prodOTDProduction <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	colNameOpt="fieldname"
)

for(i in 1:length(prodOTDProduction$OTDProductionID)){
	if(is.na(prodOTDProduction$AAAnalysis_mg_ml_[i])){
		prodOTDProduction$AAAnalysis_mg_ml_[i] <- ""
		prodOTDProduction$AAAReportDate[i] <- as.Date("2000-01-01")
	}
}
prodOTDProduction$Flag = ""
pOTDPI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	toInsert=prodOTDProduction
)

###insert everything from optide-prod.SGI_DNA
prodSD <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="SGI_DNA",
	colNameOpt="fieldname"
)
prodSD$Flag = ""
pSDI <- labkey.insertRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="SGI_DNA",
	toInsert=prodSD
)
