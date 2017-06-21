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

SubsetSelectionR <- function(startingID, dataframe, colNo, boundVal){
	returnDataFrame <- dataframe[startingID,]
	subset <- dataframe[dataframe[,colNo] < boundVal, ]
	i = 1

	while(dim(subset)[1] > 1 ){
		cat("At iteration ", i, " the size of filtered subset is ", dim(subset)[1], ".\n")
		i <- i + 1
		returnDataFrame <- rbind(returnDataFrame, subset[1, ])
		subset <- tmp_score(5, 2, 100, dim(subset)[1])
		subset <- subset[subset[,colNo] < boundVal, ]
	}
	cat("The returned dataframe's length is ", dim(returnDataFrame)[1], "\n")
	return(returnDataFrame)
}
ttt <- SubsetSelectionR(3, tmp, 2, 60)


