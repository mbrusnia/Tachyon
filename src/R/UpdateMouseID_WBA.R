library(Rlabkey)

# enter the whole pathname if you like
outputFileName <- "WBA_out.tsv"

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
for(i in 1:length(wba$MouseID)){
	if(grepl("^mu", wba$MouseID[i], perl=TRUE)){
		wba$MouseID[i] <- sub("mu", "", wba$MouseID[i])
		x <- nchar(wba$MouseID[i])
		for(j in 1:(8-x)){
			wba$MouseID[i] <- paste0("0", wba$MouseID[i])
		}
		wba$MouseID[i] <- paste0("MU", wba$MouseID[i])
	}
}

#fix the NA's
wba[is.na(wba$Fitted_DPM_), "Fitted_DPM_"] <- ""
wba[is.na(wba$Norm_DPM_mm2_), "Norm_DPM_mm2_"] <- ""
wba[is.na(wba$Intensity_Area_Bkg_QL_mm2_), "Intensity_Area_Bkg_QL_mm2_"] <- ""
wba[is.na(wba[, "Run/ACQCOMPID"]), "Run/ACQCOMPID"] <- ""


#insert each batch at a time with it's batch properties
for(i in unique(wba[,"Run/Batch"])){
	curBatch <- wba[wba["Run/Batch"] == i,]
	chemID <-unique(curBatch[,"Run/CHEMID"])
	batchName <-unique(curBatch[,"Run/Batch/Name"])
	runName <-unique(curBatch[,"Run/Name"])
	runComments <-unique(curBatch[,"Run/Comments"])
	studyName <-unique(curBatch[,"Run/Batch/StudyName"])
	acqcompID <-unique(curBatch[,"Run/ACQCOMPID"])
	
	bpl <- list(name=batchName, properties=list(StudyName=studyName, CHEMID=chemID, ACQCOMPID=acqcompID))
	rpl <- list(name=runName, properties=list(StudyName=studyName, Comments=runComments, AssayID=studyName, CHEMID=chemID, ACQCOMPID=acqcompID))
	
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

