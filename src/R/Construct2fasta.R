#This script will read data from optides-prod.fhcrc.org server and write
#it out as a fasta file.  Specifically, it will take data from the Construct
#sampleSet in the CompoundsRegistry folder and write the fasta file to the 
#working directory.


library(Rlabkey)
options(stringsAsFactors = FALSE)

mydata <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	colSelect=c("ID", "ParentID", "AlternateName", "AASeq"),
	colNameOpt="fieldname"
)

filename <-paste0("/Users/mbrusnia/Desktop/OptideConstruct_", Sys.Date(), ".fasta")
sink(filename)
for(i in 1:length(mydata$ID)){
	#cat(paste0(">", mydata$ID[i], ",", mydata$ParentID[i], ",", mydata$AlternateName[i], "\n", mydata$AASeq[i], "\n"))
	cat(paste0(">", mydata$ID[i], "\n", mydata$AASeq[i], "\n"))
}
sink()
