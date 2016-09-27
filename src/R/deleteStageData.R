#This script will delete all data from stage, copy all data from prod,
#and insert that data into stage


library(Rlabkey)
options(stringsAsFactors = FALSE)

########################################
## DELETE MAIN 3 SAMPLESETS
########################################

###Delete everything from optide-stage.Homologue
stageHomologue <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Homologue",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sHD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Homologue",
	toDelete=stageHomologue
)

###Delete everything from optide-stage.Variant
stageVariant <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Variant",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sVD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Variant",
	toDelete=stageVariant
)

###Delete everything from optide-stage.Construct
stageConstruct <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sCD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	toDelete=stageConstruct
)

########################################
## DONE DELETING MAIN 3 SAMPLESETS
########################################

########################################
## DELETE HTPRODUCTION AND OTDPRODUCTION
########################################

###Delete everything from optide-stage.HTProduction
stageHtproduction <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sHTPD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	toDelete=stageHtproduction
)

###Delete everything from optide-stage.OTDProduction
stageOtdproduction <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sOTDPD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="OTDProduction",
	toDelete=stageOtdproduction
)


########################################
## DONE DELETING HTProduction and OTDProduction
########################################

########################################
## DELETE SGI_DNA
########################################

stageSGI_DNA <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="SGI_DNA",
	colNameOpt="fieldname",
	showHidden="TRUE"
)
sSDD <- labkey.deleteRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="SGI_DNA",
	toDelete=stageSGI_DNA
)



########################################
## DONE DELETING SGI_DNA
########################################





