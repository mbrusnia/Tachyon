##
## This utility function gets the run properties for an R transformation script
##

getRunPropsList<- function(rpPath, baseUrl) 
{
	rpIn<- read.table(rpPath,  col.names=c("name", "val1", "val2", "val3"), 
		header=FALSE, check.names=FALSE,   
		stringsAsFactors=FALSE, sep="\t", quote="", fill=TRUE, na.strings="")

	## pull out the run properties

	params<- list(inputPathUploadedFile = rpIn$val1[rpIn$name=="runDataUploadedFile"],
		inputPathValidated = rpIn$val1[rpIn$name=="runDataFile"],
		
		##a little strange.  AssayRunTSVData is the one we need to output to
		outputPath = rpIn$val3[rpIn$name=="runDataFile"],
	    protocolId = rpIn$val1[rpIn$name=="protocolId"],
	    assayName = rpIn$val1[rpIn$name=="assayName"],
		containerPath = rpIn$val1[rpIn$name=="containerPath"], 
		runPropsOutputPath = rpIn$val1[rpIn$name=="transformedRunPropertiesFile"],
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
	return (params)
}

machineNameFromBaseURL <- function(baseUrl){
	a = strsplit(baseUrl, "/")[[1]]
	gsub("www.", "", a[3])
	
}
