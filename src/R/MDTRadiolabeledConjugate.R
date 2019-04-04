# Author: Mi-Youn Brusniak
# Deploy using C:\labkey\labkey\files\Optides\@files\MDTRadiolabeledConjugate.R
#
options(stringsAsFactors = FALSE)
library(Rlabkey)

source("${srcDirectory}/Utils.R")

${rLabkeySessionId}
rpPath<- "${runInfo}"


## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath)
## read the input data frame. Is xlsx or csv?
if(tools::file_ext(params$inputPathUploadedFile) == "xlsx"){
	source("${srcDirectory}/xlsxToR.R")
	inputDF <- xlsxToR(params$inputPathUploadedFile, header=TRUE)
}else{
	inputDF<-read.csv(params$inputPathUploadedFile, header=T, sep=",", stringsAsFactors=F)
}
## based on these hard coded column names, get the column indices
INPUT_PROTEIN_MG = which(names(inputDF) == "InputProtein_mg");
ELUTE_CPM = which(names(inputDF) == "Elute_CPM");
ELUTE_ML = which (names(inputDF) == "Elute_mL");
ndata = dim(inputDF)[1]
SPECIFIC_ACTIVITY = vector(mode="numeric", length=ndata)

# The constant is built from experimental protocol like 95% of the volumn, 100x dilution etc.
# It also uses CPM to Ci conversion factor 2220000000000 and unit factor uCi 0.000001
# ((((1/0.95)/2220000000000)*100*1)/0.000001) = 0.0000474158368895
CONVERSION_CONST = 0.0000474158368895
for(i in 1:ndata){
    SPECIFIC_ACTIVITY[i] = round(as.numeric(inputDF[i,ELUTE_CPM])/as.numeric(inputDF[i,INPUT_PROTEIN_MG])*as.numeric(inputDF[i,ELUTE_ML])*100.0*CONVERSION_CONST, digits=2)
    # formt input protein
    inputDF$InputProtein_mg[i] <- round(as.numeric(inputDF[i,INPUT_PROTEIN_MG]), digits=2)
}
inputDF$Specific_Activity_uCiPermg <- SPECIFIC_ACTIVITY

names(inputDF) <- c("ChemProduction ID", "Input Protein (mg)", "Elute CPM", "Elute (mL)", "Comment", "Specific Activity uCi/mg" )
write.table(inputDF,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)
