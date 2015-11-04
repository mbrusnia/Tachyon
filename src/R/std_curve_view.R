# This code draws the sample's standardization line at the top,
# then (optionally) uncomment the last line to draw the data table underneath 


# get colunm indices based on their column names
CONC_DPM_COL_INDEX = which(grepl ("std_{1,2}activity_{1,2}dpm_{1,2}", names(labkey.data), perl=TRUE))
INTENSITY_BKG_COL_INDEX = which(grepl ("intensity_{1,2}area_{1,2}ql_{1,2}mm2_{1,2}", names(labkey.data), perl=TRUE))
AREA_COL_INDEX = which(grepl ("area_{1,2}mm2_{1,2}", names(labkey.data), perl=TRUE))
GROUP_COL_INDEX = which(names(labkey.data) == "grp")

#Standardization line
Y <- log10(labkey.data[labkey.data[,GROUP_COL_INDEX]=="std", INTENSITY_BKG_COL_INDEX])
X <- log2(labkey.data[labkey.data[,GROUP_COL_INDEX]=="std", CONC_DPM_COL_INDEX])
fit<-summary(lm(Y~X))

# draw image
png(filename="${imgout:labkeyl_png}")
plot(Y~X, main="Standard Curve Using log10-log2", xlab="Log2(Standard Conc[DPM])", ylab="Log10(Intensity-Bkg)")
abline(fit, col="blue")
legend("topleft", cex=0.9, legend=paste("Rsq=", round(fit$r.squared, digits=2), "slope=", round(fit$coefficients[2], digits=3), sep=" "))
        dev.off()


#display the Table
#write.table(labkey.data, file = "${tsvout:tsvfile}", sep = "\t", qmethod = "double", col.names=NA)
