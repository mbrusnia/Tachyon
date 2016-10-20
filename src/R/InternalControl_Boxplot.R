library(Rlabkey)
data <- labkey.selectRows(
    baseUrl="https://optides-prod.fhcrc.org",
    folderPath="/Optides/HTProduction/Assays",
    schemaName="assay.General.HPLC Assays",
    queryName="Data"
)


InternalControl <- data[grep("D06|D12|H06|H12",data[, "HTProductionID"]),]
InternalControl2 <- InternalControl[-grep("HTP|HT01024H12|HT01034H12|HT01044H12|HT01054H12|HT01064H12|HT01074H12|HT01084H12|HT01094H12|HT01104H12|HT01114H12|HT01124H12|HT01134H12|HT01144H12",InternalControl[,"HTProductionID"]),]

x <- factor(InternalControl2[, "HTPlateID"])
y <- InternalControl2[,"Max Peak NR"]

boxplot(y ~ x, main="Internal Control",col="blue",  
  	xlab="HT Plate ID", ylab="Max Peak NR", las=2)

