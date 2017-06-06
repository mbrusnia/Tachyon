# Updating Mouse IDs in the VIVO / WBA area of the optides labkey install

library(Rlabkey)

# enter the whole pathname if you like
outputFileName <- "out.tsv"

##################################################################################
# 1)  Write out contents of the mouseRegistry list as a tsv for upload into a new sampleset
###################################################################################
#switch to prod or stage before running
mReg <- labkey.selectRows(
	baseUrl="http://optides-prodfhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="lists",
	queryName="MouseRegistry",
	colNameOpt="fieldname"
)

#change the mouseID field from the old format ("muXXX") to the new format ("MUXXXXXXXX")
y <- sub("mu", "", mReg$MouseID)
for(i in 1:length(mReg$MouseID)){
	x <- nchar(y[i])
	for(j in 1:(8-x)){
		y[i] <- paste0("0", y[i])
	}
	y[i] <- paste0("MU", y[i])
}
mReg$MouseID <- y

write.table(mReg, file=outputFileName, sep = "\t", row.names=FALSE, na = "", quote=FALSE)



##################################################################################
# 2)  Copy all WBA batches from Prod to Stage
###################################################################################

#switch to prod or stage before running
wba <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="assay.General.WBA",
	queryName="Data",
	colNameOpt="fieldname"
)
wba <-cbind(wba, labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="assay.General.WBA",
	queryName="Data",
	colNameOpt="fieldname",
	colSelect=c("Run/Name", "Run/Batch", "Run/Batch/Name", "Run/Comments")
))

#change the mouseID field from the old format ("muXXX") to the new format ("MUXXXXXXXX")
#for(i in 1:length(wba$MouseID)){
#	if(grepl("^mu", wba$MouseID[i], perl=TRUE)){
#		wba$MouseID[i] <- sub("mu", "", wba$MouseID[i])
#		x <- nchar(wba$MouseID[i])
#		for(j in 1:(8-x)){
#			wba$MouseID[i] <- paste0("0", wba$MouseID[i])
#		}
#		wba$MouseID[i] <- paste0("MU", wba$MouseID[i])
#	}
#}

#fix the NA's
for(i in 1:length(wba)){
	wba[is.na(wba[, i]) , i] <- ""
}

#insert each batch at a time with it's batch properties
for(i in unique(wba[,"Run/Batch"])){
	curBatch <- wba[wba["Run/Batch"] == i,]
	chemID <-unique(curBatch[,"Run/CHEMID"])
	batchName <-unique(curBatch[,"Run/Batch/Name"])
	runName <-unique(curBatch[,"Run/Name"])
	runComments <-unique(curBatch[,"Run/Comments"])
	studyName <-unique(curBatch[,"Run/Batch/StudyName"])
	acqcompID <-unique(curBatch[,"Run/ACQCOMPID"])
	
	bpl <- list(name=batchName, properties=list(StudyName=studyName))
	rpl <- list(name=runName, comment=runComments, properties=list(StudyName=studyName, AssayID=studyName, CHEMID=chemID, ACQCOMPID=acqcompID))
	
	assayInfo<- labkey.saveBatch(
		baseUrl="http://optides-stage.fhcrc.org",
		folderPath="/Optides/VIVOAssay/Sample",
		"WBA",
		curBatch,
		batchPropertyList=bpl,
		runPropertyList=rpl
	)
	cat("RunName: ", runName, "\tBatchName: ", batchName, "\tchemID: ", chemID, "\tACQCOMPID: ", acqcompID, "\tStudyName: ", studyName, "\n")
}

#not necessary to write tsv file any longer, since this script now writes directly 
#into the staging database
#write.table(wba, file=outputFileName, sep = "\t", row.names=FALSE, na = "", quote=FALSE)

##################################################################################
# 3)  We've inserted a new column in the HTProduction sampleset named parentID.  Here we
#     populate this new column with the necessary value (taken from the other data)
###################################################################################
htp <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colNameOpt="fieldname",
	showHidden=TRUE,
	colSelect=c("RowId", "Name", "ConstructID", "ParentID")
)

htp$ParentID <- gsub("Construct.", "", htp$ConstructID)

#fix NAs
htp$ConstructID[is.na(htp$Construct)] <- ""
htp$ParentID[is.na(htp$ParentID)] <- ""

htpUpdate <- labkey.updateRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	toUpdate=htp
)

###########################################
######update HTProduction SGIIDs#####
###########################################
source("C:/Users/Hector/Documents/HRInternetConsulting/Clients/FHCRC/Tachyon/src/R/xlsxToR.R")

###
### which machine to run this on?
###
HOST_NAME = "http://optides-stage.fhcrc.org"

###
### In which folder are the files found?
###
xlsxFilesDirectory <- "C:/Users/Hector/Documents/HRInternetConsulting/Clients/FHCRC/Project13 - SGI_DNA_pipeline/"


htp <- labkey.selectRows(
	baseUrl=HOST_NAME,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colNameOpt="fieldname",
	showHidden=TRUE,
	colSort="+SGIID"
)
htp <- cbind(htp, labkey.selectRows(
	baseUrl=HOST_NAME,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="HTProduction",
	colNameOpt="fieldname",
	colSort="+SGIID",
	colSelect="Flag/Comment"
))
htp$Flag <- htp[,"Flag/Comment"]
htp$Flag[is.na(htp$Flag)] <- ""

for(i in 1:nrow(htp)){ if(!is.na(htp$SGIID[i])){break}}
sgiPlateIdsNeeded <- unique(htp$SGIPlateID[1:(i-1)])

dirFiles <- dir(xlsxFilesDirectory)
curFile  <- ""

for(i in 1:length(sgiPlateIdsNeeded)){
	curHTP <- htp[htp$SGIPlateID == sgiPlateIdsNeeded[i],]
	
	if(nrow(curHTP) == 96){
		##find the correct filename
		for(j in 1:length(dirFiles)){
			if(grepl(sgiPlateIdsNeeded[i], dirFiles[j])){
				curFile <- dirFiles[j]
				break
			}
		}

		## read the input
		inputDF <- xlsxToR(paste0(xlsxFilesDirectory, curFile), header=FALSE)
	
		##
		## Extract only the plate data and its column headers from the file 
		##
		mynames <- inputDF[17, 1:11]
		inputDF <- inputDF[18:(18 - 1 + 96),1:11]
		names(inputDF) <- mynames
	
		colHeaders <- names(inputDF)
		if(grepl("Construct.*ID", colHeaders[1]) && grepl("Construct.*Name", colHeaders[2]) && grepl("Plate.*ID", colHeaders[3])
			&& grepl("Well.*Location", colHeaders[4]) && grepl("Concentration.*ng/uL", colHeaders[5])
			&& grepl("Volume.*uL", colHeaders[6]) && grepl("Total.*DNA.*ng", colHeaders[7])
			&& grepl("Vector", colHeaders[8]) && grepl("Resistance", colHeaders[9])
			&& grepl("Flanking.*Restriction.*Site", colHeaders[10]) && grepl("Sequence.*Verification", colHeaders[11])){	
			1==1
		}else{
			stop("This file does not conform to the expected format.  Please contact the administrator.")
		}

		#change SGI headers to FHCRC Optides labkey sampleset headers
		names(inputDF)[1:7] <- c("SGIID", "ConstructID", "SGIPlateID", "WellLocation", "Concentration_ngPeruL", "Volume_uL", "TotalDNA_ng") 
	
		for(j in 1:nrow(curHTP)){
			curHTP$SGIID[j] <- inputDF$SGIID[inputDF$ConstructID == curHTP$ParentID[j]][1]
		}

		curHTP$ParentID[is.na(curHTP$ParentID)] <- ""
		curHTP$ConstructID[is.na(curHTP$ConstructID)] <- ""
		curHTP$SGIID[is.na(curHTP$SGIID)] <- ""

		htpU <- labkey.updateRows( 
		   baseUrl=HOST_NAME, 
		   folderPath="/Optides/CompoundsRegistry/Samples", 
		   schemaName="samples", 
 		   queryName="HTProduction", 
 		   toUpdate=curHTP
		)
	}
}

