##
## This utility function gets the run properties for an R transformation script
##

getRunPropsList<- function(rpPath) 
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
		baseUrl = rpIn$val1[rpIn$name=="baseUrl"],
		runPropsOutputPath = rpIn$val1[rpIn$name=="transformedRunPropertiesFile"],
		standardCurve = rpIn$val1[rpIn$name=="StandardCurve"],
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
		
	params$baseUrl = gsub("https:", "http:", params$baseUrl)
	
	return (params)
}

machineNameFromBaseURL <- function(baseUrl){
	a = strsplit(baseUrl, "/")[[1]]
	gsub("www.", "", a[3])
	
}

write_NetRC_file <- function(machineName, login, password){
	filename <- paste0(Sys.getenv()["HOME"], .Platform$file.sep, "_netrc")
	if(!file.exists(filename)){
		f = file(description=filename, open="w")
		cat(file=f, sep="", "machine ", machineName, "\n")
		cat(file=f, sep="", "login ", login, "\n")
		cat(file=f, sep="", "password ", password, "\n")
		flush(con=f)
		close(con=f)
	}else{
		txtFile <- readLines(filename)
		counter <- 0
		for(i in 1:length(txtFile)){
			if(txtFile[i] == paste0("machine ", machineName)){
				counter <- counter + 1
			}
			if(txtFile[i] == paste0("login ", login)){
				counter <- counter + 1
			}
			if(txtFile[i] == paste0("password ", password)){
				counter <- counter + 1
			}
		}
		if(counter != 3){
			write(paste0("\nmachine ", machineName),file=filename,append=TRUE)
			write(paste0("login ", login),file=filename,append=TRUE)
			write(paste0("password ", password),file=filename,append=TRUE)
		}

	}
}
