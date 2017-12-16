library(Rlabkey) 
library(dplyr)
library(ggplot2) 

labkey.data <- labkey.selectRows(
    baseUrl="https://optides-dev-lk1.fhcrc.org", 
    folderPath="/Optides/HTProduction/Assays", 
    schemaName="assay.General.HPLC Assays", 
    queryName="Data", 
    colSelect=c("Run/Batch/HTPlateID", "HTProductionID", "MaxPeakNR"),
    colFilter=makeFilter(c("Run/Batch/HTPlateID", "EQUAL", labkey.url.params['htplateID'])),
    colNameOpt="fieldname"
)
filterVal <- 0
if(!(is.null(labkey.url.params$filterVal) || is.na(labkey.url.params$filterVal) || labkey.url.params$filterVal == "")){
   labkey.data <- labkey.data[labkey.data$MaxPeakNR > as.numeric(labkey.url.params$filterVal),]
   filterVal <- as.numeric(labkey.url.params$filterVal)
}

substrRight <- function(x, n){
  substr(x, nchar(x)-n+1, nchar(x))
}

labkey.data$Well <- substrRight(labkey.data[,"HTProductionID"],3)

maxNRval <- max(as.numeric(labkey.data$MaxPeakNR),na.rm=TRUE)
platemap <- mutate(labkey.data,
                   Row=as.numeric(match(toupper(substr(Well, 1, 1)), LETTERS)),
                   Column=as.numeric(substr(Well, 2, 5)))

platemap$Percent_of_MaxPeakNR <- 15 *platemap$MaxPeakNR / maxNRval

png(filename="${imgout:labkeyl_png}",width=700,height=700,units="px")
   ggplot(data=platemap, aes(x=Column, y=Row)) +
    geom_point(data=expand.grid(seq(1, 12), seq(1, 8)), aes(x=Var1, y=Var2),
               color="grey90", fill="white", shape=21, size=15) +
    geom_point(size=platemap$Percent_of_MaxPeakNR, color="blue") +
    coord_fixed(ratio=(13/12)/(9/8), xlim=c(0.5, 12.5), ylim=c(0.5, 8.5)) +
    scale_y_reverse(breaks=seq(1, 8), labels=LETTERS[1:8]) +
    scale_x_continuous(breaks=seq(1, 12)) +
    labs(title=paste0(labkey.url.params['htplateID'],  " Plate Layout. MaxPeakNR=", maxNRval, ". Cutoff threshold: ", filterVal))
        dev.off()