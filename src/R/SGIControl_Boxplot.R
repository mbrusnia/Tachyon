library(Rlabkey)
data <- labkey.selectRows(
    baseUrl="https://optides-stage.fhcrc.org",
    folderPath="/Optides/HTProduction/Assays",
    schemaName="assay.General.HPLC Assays",
    queryName="Data"
)

SGIControl <- data[grepl("A01|A07|E01|E07",data$HTProductionID),]
SGIControl <- SGIControl[!grep("HTP",SGIControl$HTProductionID),]

y <- SGIControl[,"Max Peak NR"]
x <- factor(SGIControl[,"HTPlateID"])

boxplot(y ~ x, main="SGI Control",col="blue",  
  	xlab="HT Plate ID", ylab="Max Peak NR", las=2)

