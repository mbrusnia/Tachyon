#This script takes a tsv spreadsheet in as input.  One of the column headers must be HTProductionID.
#Based on this HTProductionID, look up all the associated info in 1) Construct sampleset, and 
#2) molecularProperties assay.


library(Rlabkey)
options(stringsAsFactors = FALSE)

inputFile <- "C:/PATH/TO/YOUR/FILE/HTProduction_data.tsv"

inputDF <- read.table(inputFile, sep="\t", header=TRUE, check.names=FALSE)

HTPROD_COL  <- which(names(inputDF) == "HTProduction ID" || names(inputDF) == "HTProductionID")

#get data from HTProduction (HTProductID to ConstructID mapping)
htProdData <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colSelect=c("HTProductID", "ConstructID"),
	colNameOpt="fieldname",
	colFilter=makeFilter(c("HTProductID", "IN", paste(inputDF[,HTPROD_COL], collapse=";")))
)

#make sure we have a 1 to 1 lookup
if(length(inputDF[,HTPROD_COL]) != length(htProdData$HTProductID)){
	stop("Not all of your entered HTProductionID's were found in the HTProduction Table.  Please address this error and try again.")
}

#remove prefixing "Construct." if it is present
htProdData$ConstructID <- gsub("Construct.", "", htProdData$ConstructID)

#get data from Construct 
constructData <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	colSelect=c("ID", "ParentID", "AlternateName", "Vector", "AASeq"),
	colNameOpt="fieldname",
	colFilter=makeFilter(c("ID", "IN", paste(htProdData$ConstructID, collapse=";")))
)

#make sure we have a 1 to 1 lookup
if(length(htProdData$HTProductID) != length(constructData$ID)){
	stop("Not all of your entered HTProductionID's were mapped to a corresponding construct in the Construct Table.  Please address this error and try again.")
}


#get data from Molecular Properties 
molecularPropertiesData <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/InSilicoAssay/MolecularProperties",
	schemaName="assay.general.InSilicoAssay",
	queryName="Data",
	colSelect=c("ID", "AverageMass", "MonoisotopicMass", "pI"),
	colNameOpt="fieldname",
	colFilter=makeFilter(c("ID", "IN", paste(htProdData$ConstructID, collapse=";")))
)

#make sure we have a 1 to 1 lookup
if(length(constructData$ID) != length(molecularPropertiesData$ID)){
	stop("Not all of your entered HTProductionID's were mapped to a corresponding ID in the MolecularProperties Table.  Please address this error and try again.")
}


results <-data.frame()
for(i in 1:length(inputDF[,HTPROD_COL])){
	curRow <- data.frame(inputDF[i,], check.names=FALSE)
	curConstID <- htProdData$ConstructID[htProdData$HTProductID == inputDF[i, HTPROD_COL]]
	curRow <-cbind(curRow, constructData[constructData$ID == curConstID,])
	curRow <-cbind(curRow, molecularPropertiesData[molecularPropertiesData$ID == curConstID, c("AverageMass", "MonoisotopicMass", "pI")])

	results <-rbind(results, curRow)
}
outfile <- gsub("\\.(\\w{3})", "_out.\\1", inputFile)
write.table(results, file=outfile, sep = "\t", row.names=FALSE, na = "")



