library(Rlabkey)

# enter the whole pathname if you like
outputFileName <- "out.tsv"

#switch to prod or stage before running
mReg <- labkey.selectRows(
	baseUrl="http://optides-stage.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="lists",
	queryName="MouseRegistry",
	colNameOpt="fieldname"
)

#change the mouseID field from the old format ("muXXX") to the new format ("MUXXXXXXXX")
y <- sub("mu", "", mReg$MouseID)
for(i in 1:length(mReg$MouseID)){
	x <- nchar(y[i])
	for(j in 1:(8-x)){
		y[i] <- paste0("0", y[i])
	}
	y[i] <- paste0("MU", y[i])
}
mReg$MouseID <- y


write.table(mReg, file=outputFileName, sep = "\t", row.names=FALSE, na = "", quote=FALSE)

