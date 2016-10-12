# Updating Mouse IDs in the VIVO / WBA area of the optides labkey install

library(Rlabkey)

# enter the whole pathname if you like
outputFileName <- "out.tsv"

##################################################################################
# 1)  Write out contents of the mouseRegistry list as a tsv for upload into a new sampleset
###################################################################################
#switch to prod or stage before running
mReg <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
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

