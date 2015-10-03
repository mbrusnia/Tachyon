data <- read.csv("/Users/mbrusnia/Documents/ImagingData/DPM_WBA_Calibration_Group1.csv", header=T, sep=",", stringsAsFactors=F)
Y <- log10(data[,1])
X <- log2(data[,2])
fit<-summary(lm(Y~X))
plot(Y~X, main="standard curve using log10-log2", xlab="Log2(Standard Conc[DPM])", ylab="Log2(Intensity-Bkg)")
abline(fit, col="blue")
legend("topleft", cex=0.9, legend=paste("Rsq=", round(fit$r.squared, digits=2), "slop=", round(fit$coefficients[2], digits=3), sep=" "))
data <- read.csv("/Users/mbrusnia/Documents/ImagingData/StandardCurve.csv", header=T, sep=",", stringsAsFactors=F)
std <- data[grep("std",data$Grp),]
std.X.DPM <- log2(std$StdActivity_DPM)
std.Y.IntMinusBkg <- log10(std$IntensityBkg_QL)
fit<-summary(lm(std.Y.IntMinusBkg~std.X.DPM ))
plot(std.Y.IntMinusBkg~std.X.DPM , main="standard curve using log10-log2", xlab="Log2(Standard Conc[DPM])", ylab="Log10(Intensity-Bkg)")
abline(fit, col="blue")
legend("topleft", cex=0.9, legend=paste("Rsq=", round(fit$r.squared, digits=2), "slop=", round(fit$coefficients[2], digits=3), sep=" "))
intercept <- fit$coefficients[1]
slop <- fit$coefficients[2]
FittedDPM <- 2^((log10(data$IntensityBkg_QL)-intercept)/slop)
FittedDPMOverArea <- FittedDPM/data$Area_mm_sq 
result <- cbind(data[,1:12], FittedDPM, FittedDPMOverArea)
write.table(result,file="/Users/mbrusnia/Documents/ImagingData/SCNH.csv", col.names = TRUE, sep=",",row.names=F, quote=F)
temp <- cbind(data$IntensityBkg_QL, data$DPM, FittedDPM)
Log2_FittedDPM <- (log10(data$IntensityBkg_QL)-intercept)/slop
temp <- cbind(data$IntensityBkg_QL, data$Log2DPM, Log2_FittedDPM)
temp <- cbind(data$Log2DPM, 2^(data$Log2DPM), data$DPM)