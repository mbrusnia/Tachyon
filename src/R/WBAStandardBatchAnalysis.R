library(ggplot2)
library(Rlabkey)

options(stringsAsFactors = FALSE)

## make sure your _netrc file points to optides-prod
assaydata <- labkey.selectRows(
	baseUrl="https://optides-prod.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="assay.General.WBA",
	queryName="Data",
	colSelect=c("RowId", "Std_Activity_DPM_", "Intensity_Area_Bkg_QL_mm2_", "Run/Batch/StudyName", "Run/Batch"),
	colNameOpt="fieldname",
	showHidden=TRUE,
	colFilter=makeFilter(c("Type", "EQUALS", "Standard"))
)

# look up batch names from their IDs
batchdata <- labkey.selectRows(
	baseUrl="https://optides-prod.fhcrc.org",
	folderPath="/Optides/VIVOAssay/Sample",
	schemaName="assay.General.WBA",
	queryName="Batches",
	colSelect=c("RowId", "Name"),
	showHidden=TRUE,
	colNameOpt="fieldname"
)
batchnames <- c()
for(i in 1:length(assaydata$RowId)){
	batchnames <- c(batchnames, batchdata[batchdata$RowId==assaydata[i, "Run/Batch"], "Name"])
}
assaydata <-cbind(assaydata, BatchName=batchnames)

#display counts per batch name
#for(i in 1:length(batchdata$Name)){
#	cat(paste0(batchdata$RowId[i], " - ", batchdata$Name[i], "\t- ", length(assaydata[assaydata[,"Run/Batch"]==batchdata$RowId[i], "Run/Batch"]), "\n"))
#}

assaydata <-cbind(assaydata, StandardAmounts="aaa")
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "1"] <- paste0("A-1-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "1"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "3"] <- paste0("B-3-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "3"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "7"] <- paste0("C-7-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "7"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "13"] <- paste0("D-13-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "13"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "26"] <- paste0("E-26-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "26"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "75"] <- paste0("F-75-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "75"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "96"] <- paste0("G-96-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "96"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "214"] <- paste0("H-214-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "214"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "390"] <- paste0("I-390-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "390"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "817"] <- paste0("J-817-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "817"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "1580"] <- paste0("K-1580-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "1580"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "3920"] <- paste0("L-3920-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "3920"]))
assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "6610"] <- paste0("M-6610-",length(assaydata$StandardAmounts[assaydata$Std_Activity_DPM_ == "6610"]))

ggplot(assaydata, aes(StandardAmounts, Intensity_Area_Bkg_QL_mm2_)) + geom_boxplot(fill="blue") +  ggtitle("Luminescence per Standard Across Batches") + theme(axis.text.x=element_text(angle=90, vjust=0.4,hjust=1, size=14)) + labs(y="Intensity_Area_Bkg_QL_mm2_")
# Remove 2016-02-05 batch 2
assaydata_subset <- assaydata[-grep("2016-02-05 batch 2", assaydata$BatchName),]
assaydata_subset$LogIntAreaBkg <- log10(assaydata_subset$Intensity_Area_Bkg_QL_mm2_)
ggplot(assaydata_subset, aes(StandardAmounts, LogIntAreaBkg)) + geom_boxplot(fill="blue") +  ggtitle("Luminescence per Standard Across Batches") + theme(axis.text.x=element_text(angle=90, vjust=0.4,hjust=1, size=14)) + labs(y="log10(Intensity_Area_Bkg_QL_mm2_)") 
fit <- lm(assaydata_subset$LogIntAreaBkg~assaydata_subset$StandardAmounts)
summary(fit)
sd(assaydata_subset[grep("F-75-12", assaydata_subset$StandardAmounts),]$Intensity_Area_Bkg_QL_mm2_)/mean(assaydata_subset[grep("F-75-12", assaydata_subset$StandardAmounts),]$Intensity_Area_Bkg_QL_mm2_)
sd(assaydata_subset[grep("G-96-13", assaydata_subset$StandardAmounts),]$Intensity_Area_Bkg_QL_mm2_)/mean(assaydata_subset[grep("G-96-13", assaydata_subset$StandardAmounts),]$Intensity_Area_Bkg_QL_mm2_)

min(assaydata_subset[grep("F-75-12", assaydata_subset$StandardAmounts),]$Intensity_Area_Bkg_QL_mm2_)