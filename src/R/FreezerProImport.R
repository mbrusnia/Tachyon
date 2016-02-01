# Author(s): Mi-Youn Brusniak
# Description: Transformation code from FreezerPro export file to Optide LIMS file
#			   This code will be updated to use LIMS table input
library(reshape)

data <- read.csv("/Users/mbrusnia/Documents/OTDReports/FreezerPro.csv", header=T, sep=",", stringsAsFactors=F)
data <- data[-grep("mbrusnia", data$Owner),] # remove vogus sample that I have created to build freezerpro. It should be removed after we are confident using Freezerpro customization.
data = transform(data, BARCODE = colsplit(BARCODE, split = "#", names = c('OTD', 'vial')))
FreezerProLIMS <- data.frame(table(data$BARCODE$OTD))

colnames(FreezerProLIMS) <- c("CompoundID", "NumberOfVials")
write.table(FreezerProLIMS, file = "/Users/mbrusnia/Desktop/FreezerPro_20160125.tsv",sep = "\t",row.names = F,col.names = T,na="N/A",quote = FALSE)