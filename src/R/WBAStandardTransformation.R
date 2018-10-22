# Copyright [2018] [Mi-Youn Brusniak, Hector Ramos]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
options(stringsAsFactors = FALSE)
library(Rlabkey)

source("${srcDirectory}/Utils.R")

${rLabkeySessionId}
rpPath<- "${runInfo}"


## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath)

## read the input data frame
data <- read.csv(params$inputPathUploadedFile, header=T, sep=",", stringsAsFactors=F)

## based on these hard coded column names, get the column indices
GROUP_COL_INDEX = which(names(data) == "Grp");
CONC_DPM_COL_INDEX = which(names(data) == "Std..Activity..DPM.");
INTENSITY_BKG_COL_INDEX = which(names(data) == "Intensity.Area.Bkg..QL.mm2.")
AREA_COL_INDEX = which(names(data) == "Area..mm2.");
GRP_NAME_COL_INDEX = which(names(data) == "Grp.Name");

Y <- log10(data[data[,GROUP_COL_INDEX]=="std", INTENSITY_BKG_COL_INDEX])
X <- log2(data[data[,GROUP_COL_INDEX]=="std", CONC_DPM_COL_INDEX])
fit<-summary(lm(Y~X))

intercept <- fit$coefficients[1]
slope <- fit$coefficients[2]
FittedDPM <- 2^((log10(data[,INTENSITY_BKG_COL_INDEX])-intercept)/slope)
FittedDPMOverArea <- FittedDPM/data[,AREA_COL_INDEX] 
MouseID <- unlist(lapply(strsplit(as.character(data[,GRP_NAME_COL_INDEX]), ".", fixed=TRUE), "[", 1))

result <- cbind(data, FittedDPM, FittedDPMOverArea, MouseID)
names(result) <- c("Grp", "Grp Name", "Name", "Type", "Area [mm2]", "Intensity [QL]", "Intensity-Bkg [QL]", "Intensity/Area [QL/mm2]", "Intensity/Area-Bkg [QL/mm2]", "Std. Activity [DPM]", "Recalc. Activity [DPM]", "Fitted [DPM]", "Norm [DPM/mm2]", "Mouse ID")
write.table(result,file=params$outputPath, col.names = TRUE, sep="\t",na="", row.names=F, quote=F)
