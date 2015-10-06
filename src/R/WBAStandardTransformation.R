options(stringsAsFactors = FALSE)
library(Rlabkey)

GROUP_COL_INDEX = 1;
CONC_DPM_COL_INDEX = 10;
INTENSITY_BKG_COL_INDEX = 7;
AREA_COL_INDEX = 5;

baseUrl<-"http://localhost:8080/labkey"
${rLabkeySessionId}
rpPath<- "${runInfo}"

getRunPropsList<- function(rpPath, baseUrl) 
{
	rpIn<- read.table(rpPath,  col.names=c("name", "val1", "val2", "val3"),              #########
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
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
	return (params)

}

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)

#setwd("C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/transformationScript01")
data <- read.csv(params$inputPathUploadedFile, header=T, sep=",", stringsAsFactors=F)
## read the input data frame just  to get the column headers.

Y <- log10(data[data[,GROUP_COL_INDEX]=="std", INTENSITY_BKG_COL_INDEX])
X <- log2(data[data[,GROUP_COL_INDEX]=="std", CONC_DPM_COL_INDEX])
fit<-summary(lm(Y~X))

intercept <- fit$coefficients[1]
slope <- fit$coefficients[2]
FittedDPM <- 2^((log10(data[,INTENSITY_BKG_COL_INDEX])-intercept)/slope)
FittedDPMOverArea <- FittedDPM/data[,AREA_COL_INDEX] 

result <- cbind(data, FittedDPM, FittedDPMOverArea)
names(result) <- c("Grp", "Grp Name", "Name", "Type", "Area [mm2]", "Intensity [QL]", "Intensity-Bkg [QL]", "Intensity/Area [QL/mm2]", "Intensity/Area-Bkg [QL/mm2]", "Std. Activity [DPM]", "Recalc. Activity [DPM]", "Fitted [DPM]", "Norm [DPM/mm2]")
write.table(result,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)

 
