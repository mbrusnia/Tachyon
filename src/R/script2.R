options(stringAsFactors = FALSE)
require(Rlabkey)
baseUrl<-"http://localhost:8080/labkey/"
source("${srcDirectory}/Utils.R")


## These next four constants point to the data we wish to query from the DB
ASSAY_SCHEMA_NAME = "assay.General.InSilicoAssay"
ASSAY_QUERY_NAME = "Data"
SEQUENCE_COL_NAME = "AASeq"
COMPOUND_ID_COL_NAME = "ID"
PARENT_ID_COL_NAME = "Parent ID"
SAMPLES_SCHEMA = "Samples"
COMPOUND_TABLE = "Construct"
COMPOUND_FOLDER = "Optides/CompoundsRegistry/Samples"


${rLabkeySessionId}

rpPath <- "${runInfo}"

## read the file paths etc out of the runProperties.tsv file
params <- getRunPropsList(rpPath, baseUrl)


## read the input data frame just to get the column headers.
inputDF<-read.table(file=params$inputPathUploadedFile, header = TRUE, sep = "\t")
str(inputDF)
names(inputDF)[grepl("[Pp]arent.ID", names(inputDF))] <- PARENT_ID_COL_NAME
str(inputDF)
labkey.insertRows(baseUrl, COMPOUND_FOLDER, SAMPLES_SCHEMA, COMPOUND_TABLE, inputDF)
stop("halt!")