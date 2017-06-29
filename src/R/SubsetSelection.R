##
## This function creates and returns a subset of inputDF (dataframe) where inputDF[, colNo] < boundval
## 
## Step 1 :Add startingID row to the returnDataFrame
## Step 2:  Get  the dataframe and get subset_1 that has dataframe[, colNo] < boundVal.
## Step 3: if subset_i size is > 0 add subset_i[1, ] to the returnDataFrame, else stop 
## Step 3.5: call score(subset_i[1,], subset_i), return dataframe of the same size of subset_i but now contains new values in subset_i.  (in my testing , make sure the first number is the max number in the column)
## Step 4: iteration of step 2 for i+1
## The routine should print out each iteration subset_size and final returnDataFrame size.
##
library(Biostrings)
library(pryr)
data(BLOSUM50)


##startingID is a string like "tr|R7RPX4|R7RPX4_9CLOT"
SimpleSubsetSelectionR <- function(startingID, fastaAAStringSet, colNo, boundVal){
	returnDataFrame <- fastaAAStringSet[substring(names(fastaAAStringSet), 1, nchar(startingID)) == startingID]
	alm <- pairwiseAlignment(fastaAAStringSet, returnDataFrame, substitutionMatrix="PAM30", gapOpening=-10, gapExtension=-0.2)
	new_score <- as.matrix(score(alm))
    	#normalize the score values from 0 to 100
	new_score = (new_score + abs(min(new_score)))/(max(new_score) + abs(min(new_score)))
	return(fastaAAStringSet[new_score > boundVal])
}

SubsetSelectionR <- function(startingID, fastaAAStringSet, colNo, boundVal){
	returnDataFrame <- fastaAAStringSet[substring(names(fastaAAStringSet), 1, nchar(startingID)) == startingID]
	#pairwise score against fasta file #and 
	alm <- pairwiseAlignment(fastaAAStringSet, returnDataFrame[1], substitutionMatrix="PAM30", gapOpening=-10, gapExtension=-0.2)
      new_score <- as.matrix(score(alm))
	#normalize the score values from 0 to 100
	new_score = (new_score + abs(min(new_score)))/(max(new_score) + abs(min(new_score)))
	subset <- fastaAAStringSet[new_score < boundVal]
	i = 1

	while(length(subset) > 0 ){
		cat("At inner iteration", i, "the size of filtered subset is", length(subset), "\n")
		i <- i + 1
		returnDataFrame = append(returnDataFrame , subset[1])
		alm <- pairwiseAlignment(subset, subset[1], substitutionMatrix="PAM30", gapOpening=-10, gapExtension=-0.2)
        	new_score <- as.matrix(score(alm))
		#normalize the score values from 0 to 100
		new_score = (new_score + abs(min(new_score)))/(max(new_score) + abs(min(new_score)))
		subset <- subset[new_score < boundVal]
		#subset1 <- subset[new_score < boundVal]
		#gc(subset)
		#subset = subset1
		#gc(subset1)
	}
	cat("The returned dataframe's length is", length(returnDataFrame), "\n")
	return(returnDataFrame)
}

# step1: Read initial score file
initialScoreFile <- "C:\\Users\\Hector\\Documents\\HRInternetConsulting\\Clients\\FHCRC\\Project25 - Research\\Project25_SubsetSelection_RunExample\\Project25_SubsetSelection_RunExample\\Input_initialScore.txt"
initialScores <- read.table(initialScoreFile, header=TRUE, sep="\t")

# step2: Read fasta file to make dataframe.
fastaFile <- "C:\\Users\\Hector\\Documents\\HRInternetConsulting\\Clients\\FHCRC\\Project25 - Research\\Project25_SubsetSelection_RunExample\\Project25_SubsetSelection_RunExample\\input.fasta"
fasta <- readAAStringSet(fastaFile) 

# step3: loop over each sequences that is above of InitialScore_Cutoff, call SubsetSelectionR with SetBoundVal
InitialScore_Cutoff = -20

##for our test data, this threshold gives us an initial dataset of 36 data points
#InitialScore_Cutoff = 50
SetBoundVal = 0.1

initialScoreSubset <- initialScores[initialScores$InitialScore > InitialScore_Cutoff,]
upperVal <- length(initialScoreSubset$InitialScore)
start.time <- Sys.time()

#we're running this script with both functions, first with SimpleSubsetSelectionR, then with SubsetSelectionR
for(j in 1:2){
	curFun <- SubsetSelectionR
	if(j == 2)
		curFun <- SimpleSubsetSelectionR
	for(i in 1:upperVal){
		cat("Memory used:", mem_used(), "\n")
		end.time <- Sys.time()
		time.taken <- end.time - start.time
		cat("Time taken:", time.taken, "\n")
		cat("OUTER LOOP ITERATION", i, "of", upperVal,"\n")
		# step4: startingID needs to be found from dataframe using proteinID (ex. tr|R7RPX4|R7RPX4_9CLOT)
		if(i == 1)
			returnDF  = curFun(as.character(initialScores[i, "fullname"]), fasta, 3, SetBoundVal)
		else
			returnDF = append(returnDF, curFun(as.character(initialScores[i, "fullname"]), fasta, 3, SetBoundVal))
	}
	#
	# step5: output is returnDataFrame is written in fasta file format.  first make unique
	#
	#remove duplicates
	returnDF = returnDF[!duplicated(returnDF), ]
	length(returnDF)

	#write fasta output:
	if(j==1){
		outFastaFile <- "C:\\Users\\Hector\\Documents\\HRInternetConsulting\\Clients\\FHCRC\\Project25 - Research\\Project25_SubsetSelection_RunExample\\Project25_SubsetSelection_RunExample\\simpleSubsetSelectionOut.fasta"
	}else{
		outFastaFile <- "C:\\Users\\Hector\\Documents\\HRInternetConsulting\\Clients\\FHCRC\\Project25 - Research\\Project25_SubsetSelection_RunExample\\Project25_SubsetSelection_RunExample\\subsetSelectionOut.fasta"
	}

	sink(outFastaFile)
	for(i in 1:length(returnDF)){
		cat(paste0(">", names(returnDF[i]), "\n"))
		cat(paste0(as.character(returnDF[i]), "\n"))
	}
	sink()
}
end.time <- Sys.time()
time.taken <- end.time - start.time
cat("Time taken:", time.taken, "minutes.\n")
cat("Final Memory used:", mem_used(), "\n")


