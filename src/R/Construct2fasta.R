#This script will read data from optides-prod.fhcrc.org server and write
#it out as a fasta file.  Specifically, it will take data from the Construct
#sampleSet in the CompoundsRegistry folder and write the fasta file to the 
#working directory.


library(Rlabkey)
options(stringsAsFactors = FALSE)

BASE_URL = "http://optides-prod.fhcrc.org"

const <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="ConstructHTIDHTAssay",
	colNameOpt="fieldname"
)
AllConst <- labkey.selectRows(
	baseUrl=BASE_URL,
	folderPath="/Optides/CompoundsRegistry/Samples",
	schemaName="samples",
	queryName="Construct",
	colNameOpt="fieldname"
)

args = commandArgs(trailingOnly=TRUE)
filename <-paste0("/Users/mbrusnia/Desktop/OptideConstruct_", Sys.Date(), ".fasta")
if(length(args) == 1){
	filename <- args[1]
}

sink(filename)
# Make sure control was inserted only once.
CNT0001396 <- 0
CNT0001465 <- 0
currSeq <- ""
for(i in 1:nrow(const)){
    if(const$ID[i] == "CNT0001396"){
      if(CNT0001396 == 0){
		cat(paste0(">", const$ID[i], " ", const$ParentID[i], " "))
		if(!is.na(const$AlternateName[i])){
			cat(paste0(const$AlternateName[i], " "))
		}
		cat(paste0(const$HTProductID[i], " ", const$classification[i], "\n", const$AASeq[i], "\n"))
		#cat(paste0(">", const$ID[i], "\n", const$AASeq[i], "\n"))
		CNT0001396 <- 1
      }
    }
    else if(const$ID[i] == "CNT0001465"){
      if(CNT0001465 == 0){
		cat(paste0(">", const$ID[i], " ", const$ParentID[i], " "))
		if(!is.na(const$AlternateName[i])){
			cat(paste0(const$AlternateName[i], " "))
		}
		cat(paste0(const$HTProductID[i], " ", const$classification[i], "\n", const$AASeq[i], "\n"))
		#cat(paste0(">", const$ID[i], "\n", const$AASeq[i], "\n"))
		CNT0001465 <- 1
      }
    }
    else{  # Remove duplicate sequences due to two different vector
    	if(i == 1){
    		currSeq <- const$ID[i]
			cat(paste0(">", const$ID[i], " ", const$ParentID[i], " "))
			if(!is.na(const$AlternateName[i])){
				cat(paste0(const$AlternateName[i], " "))
			}
			cat(paste0(const$HTProductID[i], " ", const$classification[i], "\n", const$AASeq[i], "\n"))
    	}
    	else{
    		if(currSeq != const$ID[i]){
    			currSeq = const$ID[i]
				cat(paste0(">", const$ID[i], " ", const$ParentID[i], " "))
				if(!is.na(const$AlternateName[i])){
					cat(paste0(const$AlternateName[i], " "))
				}
				cat(paste0(const$HTProductID[i], " ", const$classification[i], "\n", const$AASeq[i], "\n"))
			}
		}
    }
}

# Append Construct that is not in HT Assay
NoHTConst <- AllConst[-match(const$ID, AllConst$ID),]
SortedSeq <- NoHTConst[order(NoHTConst$AASeq),]
currSeq <- ""
for(i in 1:nrow(SortedSeq)){
    if(i == 1){
    	currSeq <- SortedSeq$AASeq[i]
		cat(paste0(">", SortedSeq$ID[i], " ", SortedSeq$ParentID[i], " "))
		if(!is.na(SortedSeq$AlternateName[i])){
			cat(paste0(SortedSeq$AlternateName[i], " "))
		}
		cat(paste0("\n", SortedSeq$AASeq[i], "\n"))
    }
    else{
    	if(currSeq != SortedSeq$AASeq[i]){
    		currSeq = SortedSeq$AASeq[i]
			cat(paste0(">", SortedSeq$ID[i], " ", SortedSeq$ParentID[i], " "))
			if(!is.na(SortedSeq$AlternateName[i])){
				cat(paste0(SortedSeq$AlternateName[i], " "))
			}
			cat(paste0("\n", SortedSeq$AASeq[i], "\n"))
		}
	}
}

sink()
