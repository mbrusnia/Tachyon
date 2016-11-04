options(stringsAsFactors = FALSE)

baseUrl<-"http://optides-prod.fhcrc.org/"
${rLabkeySessionId}
rpPath<- "${runInfo}"

getRunPropsList<- function(rpPath, baseUrl) {
	rpIn<- read.table(rpPath,  col.names=c("name", "val1", "val2", "val3"),          #########
		header=FALSE, check.names=FALSE,                                             ##  1  ##  
		stringsAsFactors=FALSE, sep="\t", quote="", fill=TRUE, na.strings="");       ######### 

	## pull out the run properties

	params<- list(inputPathUploadedFile = rpIn$val1[rpIn$name=="runDataUploadedFile"],
		inputPathValidated = rpIn$val1[rpIn$name=="runDataFile"],
		
		##a little strange.  AssayRunTSVData is the one we need to output to
		outputPath = rpIn$val3[rpIn$name=="runDataFile"],
	
		containerPath = rpIn$val1[rpIn$name=="containerPath"], 
		runPropsOutputPath = rpIn$val1[rpIn$name=="transformedRunPropertiesFile"],
		sampleSetId = as.integer(rpIn$val1[rpIn$name=="sampleSet"]),
		probeSourceId = as.integer(rpIn$val1[rpIn$name=="probeSource"]),
		calibrationFactor = as.numeric(rpIn$val1[rpIn$name=="CalibrationFactor"]),
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
	return (params)

}

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

## read the input data frame. Is xlsx or tsv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "csv"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = ",", check.names=FALSE)
}else if (tools::file_ext(params$inputPathUploadedFile) == "tsv" || tools::file_ext(params$inputPathUploadedFile) == "tmp"){ 
	inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t", check.names=FALSE)
}

###################################################################
## 1) Format column headers
###################################################################
if(grepl("MouseID", names(inputDF)[1]) && grepl("CompoundID", names(inputDF)[2])
	&& grepl("Tissue", names(inputDF)[3]) && grepl("Date", names(inputDF)[4])
	&& grepl("Tissue_mg", names(inputDF)[5]) && grepl("mg_per_ul", names(inputDF)[6])
	&& grepl("Loaded_Volume_uL", names(inputDF)[7]) && grepl("CPM", names(inputDF)[8])
	&& grepl("Loaded_mg", names(inputDF)[9]) && grepl("pCi", names(inputDF)[10])
	&& grepl("pCi_per_uL", names(inputDF)[11])	){	
	1==1
}else{
	stop("This file does not conform to the expected format.  These are the expected column headers (in this order): MouseID	CompoundID	Tissue	Date	Tissue_mg	mg_per_ul	Loaded_Volume_uL	CPM	Loaded_mg	pCi	pCi_per_uL")
}

#names(inputDF) = c("PlateID", "WellID", "Specimen", "Sample", "M3_Percent_Parent", "M3_Median_FITC_H", "P1_percent_All", "R2_percent_Parent", "Run_Time", "R2_Abs_Count")
inputDF$pCi <- params$calibrationFactor * as.numeric(inputDF$CPM)
inputDF$Loaded_mg <- as.numeric(inputDF$Loaded_Volume_uL) / as.numeric(inputDF$mg_per_ul)
inputDF$pCi_per_uL <- as.numeric(inputDF$pCi) / as.numeric(inputDF$Loaded_Volume_uL)


###################################################################
## 2) Insert data to Database
###################################################################

write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

