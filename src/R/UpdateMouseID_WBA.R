library(Rlabkey)

# enter the whole pathname if you like
outputFileName <- "WBA_out.tsv"

#switch to prod or stage before running
wba <- labkey.selectRows(
	baseUrl="http://optides-prod.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="assay.General.WBA",
	queryName="Data",
	colNameOpt="fieldname"
)

#change the mouseID field from the old format ("muXXX") to the new format ("MUXXXXXXXX")
for(i in 1:length(wba$MouseID)){
	if(grepl("^mu", wba$MouseID[i], perl=TRUE)){
		wba$MouseID[i] <- sub("mu", "", wba$MouseID[i])
		x <- nchar(y[i])
		for(j in 1:(8-x)){
			wba$MouseID[i] <- paste0("0", wba$MouseID[i])
		}
		wba$MouseID[i] <- paste0("MU", wba$MouseID[i])
	}
}

write.table(wba, file=outputFileName, sep = "\t", row.names=FALSE, na = "", quote=FALSE)

