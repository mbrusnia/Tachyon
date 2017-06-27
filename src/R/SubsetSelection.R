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


##temporary score function.  replace this with optides real score function
tmp_score <- function(cols, colNo, maxVal, size){
	tmp <- data.frame(replicate(cols,sample(0:maxVal,size,rep=TRUE)))
	tmp[1, colNo] = maxVal
	return(tmp)
}
tmp <- tmp_score(5, 2, 100, 700000)
# step1: Read initial score file
# step2: Read fasta file to make dataframe.
InitialScore_Cutoff = -20
# step3: loop over each sequences that is above of InitialScore_Cutoff, call SubsetSelectionR with SetBoundVal
# step4: startingID needs to be found from dataframe using proteinID (ex. tr|R7RPX4|R7RPX4_9CLOT)
# step5: output is returnDataFrame is written in fasta file format.
SetBoundVal = 0.9

SimpleSubsetSelectionR <- function(startingID, dataframe, colNo, boundVal){
	returnDataFrame <- dataframe[startingID,]
	alm <- pairwiseAlignment(dataframe[startingID,2], dataframe$sequence, substitutionMatrix="PAM30", gapOpening=-10, gapExtension=-0.2)
	score <- as.matrix(score(alm))
    returnDataFrame <- dataframe[which(score[score > boundVal]), ]
	return(returnDataFrame)
}

SubsetSelectionR <- function(startingID, dataframe, colNo, boundVal){
	returnDataFrame <- dataframe[startingID,]
	subset <- dataframe[dataframe[,colNo] < boundVal, ]
	i = 1

	while(dim(subset)[1] > 1 ){
		cat("At iteration ", i, " the size of filtered subset is ", dim(subset)[1], ".\n")
		i <- i + 1
		returnDataFrame <- rbind(returnDataFrame, subset[1, ])
		alm <- pairwiseAlignment(dataframe[startingID,2], dataframe$sequence, substitutionMatrix="PAM30", gapOpening=-10, gapExtension=-0.2)
        score <- as.matrix(score(alm))
		subset <- dataframe[which(score[score < boundVal]), ]
	}
	cat("The returned dataframe's length is ", dim(returnDataFrame)[1], "\n")
	return(returnDataFrame)
}
ttt <- SubsetSelectionR(3, tmp, 2, 60)


