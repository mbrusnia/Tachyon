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
# 
# 8/12/16 Liquid Scintillation Count LOQ
#
library(ggplot2)
data <- read.csv("/Users/mbrusnia/Documents/ImagingData/LSC_Std_Curve.csv", header=T, sep=",", stringsAsFactors=F)
data$logpCi <- log(data$pCi)
data$logCPM<- log(data$CPM)
ggplot(data, aes(x=logCPM,y=logpCi)) + geom_point(color="red", size=5) + xlab("log(CPM)") + ylab("log(logpCi)")+geom_smooth(method = "lm", se=TRUE, level=0.99)
ggplot(data, aes(x=logCPM,y=logpCi)) + geom_point(color="red", size=5) + xlab("log(CPM)") + ylab("log(logpCi)")+geom_smooth(method = "auto", se=TRUE, level=0.99)
subset<-data[data$pCi>100,]
ggplot(subset, aes(x=logCPM,y=logpCi)) + geom_point(color="red", size=5) + xlab("log(CPM)") + ylab("log(logpCi)")+geom_smooth(method = "lm", se=TRUE, level=0.99)
#
# 9/29/16 Two different LSC counting method comparison and LOD
#
data <- read.csv("/Users/mbrusnia/Documents/ImagingData/LSC_Two_Method_comparison.csv", header=T, sep=",", stringsAsFactors=F)
dim(data)
data <- na.omit(data)
dim(data)
data$log10pCi <- log10(data$pCi)
data$log10CPM <- log10(data$CPM)
model <- lm(log10pCi~log10CPM*Method,data=data)
summary(model)
model
#Call:
#lm(formula = log10pCi ~ log10CPM * Method, data = data)
#
#Coefficients:
#             (Intercept)                  log10CPM           Method52Percent  log10CPM:Method52Percent  
#                 -0.9051                    1.1580                   -0.5751                    0.1050  

# Test H_0: fD2=0 for difference in intercept
# Test H_0: X:fD2=0 for difference in slopes.

# Don't forget this test!
anova(lm(log10pCi~log10CPM,data=data),lm(log10pCi~log10CPM*Method,data=data))
#Analysis of Variance Table
#Model 1: log10pCi ~ log10CPM
#Model 2: log10pCi ~ log10CPM * Method
#  Res.Df    RSS Df Sum of Sq      F  Pr(>F)   
#1     93 18.230                               
#2     91 15.874  2    2.3565 6.7547 0.00184 **
#Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1

library(ggplot2)
ggplot(data=data,aes(x=log10CPM,y=log10pCi,group=Method))+geom_point(aes(colour=Method))+xlim(0,6)+geom_abline(intercept= -0.9051,slope=1.1580)+geom_abline(intercept=(-0.9051-0.5751), slope=(1.1580-0.1050))
