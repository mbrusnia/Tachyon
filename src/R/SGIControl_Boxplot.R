library(Rlabkey)
data <- labkey.selectRows(
    baseUrl="https://optides-stage.fhcrc.org",
    folderPath="/Optides/HTProduction/Assays",
    schemaName="assay.General.HPLC Assays",
    queryName="Data"
)

SGIControl <- data[grep("A01|A07|E01|E07",data[, "HTProduction ID"]),]

boxplot(SGIControl[,"Max Peak NR"]~SGIControl[,"HTPlate ID"], main="SGI Control",col="blue",  
  	xlab="HT Plate ID", ylab="Max Peak NR")

