##
## This utility function gets the run properties for an R transformation script
##

getRunPropsList<- function(rpPath) 
{
	rpIn<- read.table(rpPath,  col.names=c("name", "val1", "val2", "val3"), 
		header=FALSE, check.names=FALSE,   
		stringsAsFactors=FALSE, sep="\t", quote="", fill=TRUE, na.strings="")

	## pull out the run properties
	
	params<- list(inputPathUploadedFile = rpIn$val1[rpIn$name=="runDataUploadedFile"],
		inputPathValidated = rpIn$val1[rpIn$name=="runDataFile"],
		
		##a little strange.  AssayRunTSVData is the one we need to output to
		outputPath = rpIn$val3[rpIn$name=="runDataFile"],
	    protocolId = rpIn$val1[rpIn$name=="protocolId"],
	    assayName = rpIn$val1[rpIn$name=="assayName"],
		containerPath = rpIn$val1[rpIn$name=="containerPath"], 
		baseUrl = rpIn$val1[rpIn$name=="baseUrl"],
		runPropsOutputPath = rpIn$val1[rpIn$name=="transformedRunPropertiesFile"],
		standardCurve = rpIn$val1[rpIn$name=="StandardCurve"],
		errorsFile = rpIn$val1[rpIn$name=="errorsFile"])
		
	params$baseUrl = gsub("https:", "http:", params$baseUrl)
	
	return (params)
}

machineNameFromBaseURL <- function(baseUrl){
	a = strsplit(baseUrl, "/")[[1]]
	gsub("www.", "", a[3])
	
}

write_NetRC_file <- function(machineName, login, password){
	filename <- paste0(Sys.getenv()["HOME"], .Platform$file.sep, "_netrc")
	if(!file.exists(filename)){
		f = file(description=filename, open="w")
		cat(file=f, sep="", "machine ", machineName, "\n")
		cat(file=f, sep="", "login ", login, "\n")
		cat(file=f, sep="", "password ", password, "\n")
		flush(con=f)
		close(con=f)
	}else{
		txtFile <- readLines(filename)
		counter <- 0
		for(i in 1:length(txtFile)){
			if(txtFile[i] == paste0("machine ", machineName)){
				counter <- counter + 1
			}
			if(txtFile[i] == paste0("login ", login)){
				counter <- counter + 1
			}
			if(txtFile[i] == paste0("password ", password)){
				counter <- counter + 1
			}
		}
		if(counter != 3){
			write(paste0("\nmachine ", machineName),file=filename,append=TRUE)
			write(paste0("login ", login),file=filename,append=TRUE)
			write(paste0("password ", password),file=filename,append=TRUE)
		}

	}
}

#calculate molecular weight of a peptide string ("ACPKGGS", for example)
DSBMWCalc <-function(seq, monoisotopic = FALSE){
	seq <- gsub("[\r\n ]", "", seq)

	## Hydrogen's mass needed for mass calculations
	H_ISO_MASS = 1.00794

    if (monoisotopic == TRUE) {
        weight <- c(A = 71.037114, R = 156.101111, N = 114.042927, 
            D = 115.026943, C = 103.009185, E = 129.042593, Q = 128.058578, 
            G = 57.021464, H = 137.058912, I = 113.084064, L = 113.084064, 
            K = 128.094963, M = 131.040485, F = 147.068414, P = 97.052764, 
            S = 87.032028, T = 101.047679, W = 186.079313, Y = 163.06332, 
            V = 99.068414, U = 150.95363, O = 237.14772, H2O = 18.01056)
    } else {
        weight <- c(A = 71.0779, R = 156.1857, N = 114.1026, 
            D = 115.0874, C = 103.1429, E = 129.114, Q = 128.1292, 
            G = 57.0513, H = 137.1393, I = 113.1576, L = 113.1576, 
            K = 128.1723, M = 131.1961, F = 147.1739, P = 97.1152, 
            S = 87.0773, T = 101.1039, W = 186.2099, Y = 163.1733, 
            V = 99.1311, U = 150.0379, O = 237.3018, H2O = 18.01056)
    }
    sum(weight[c(strsplit(toupper(seq), split = "")[[1]], "H2O")], na.rm = TRUE) - floor(str_count(seq, "C")/2) * 2 * H_ISO_MASS
}

#calculate mass of formula with format: C12H6N3, etc.
calc_formula_mass <- function(formula){
	weights <- c(H = 1.0078250, O = 15.9949146, C = 12.0000000, N = 14.0030740, P = 30.9737633, S = 31.9720718, F = 18.998403)
	letters <- str_split(formula, "[0-9]+")[[1]]
	counts <- str_split(formula, "[A-Za-z]+")[[1]]
	letters <- letters[!letters == ""]
	counts <- counts[!counts == ""]

	if(length(letters) != length(counts)){
		stop(paste("This chemical formula is not properly constructed: ", formula, ". Each chemical symbol has to be followed by a number (yes, even if it's only 1). Please fix and try again."))
	}

	weight_total = 0
	for(i in 1:length(letters)){
		if (!letters[i] %in% names(weights)){
			stop(paste("This formula: ", formula, " contains the invalid character '", letters[i], ".  Valid characters are: CHONPSF.  Please fix and try again."))
		}
		#cat(letters[i], " ", weight[letters[i]], " ", counts[i], " ", as.numeric(counts[i]), "\t", weight[letters[i]] * as.numeric(counts[i]), "\n")
		weight_total = weight_total + as.numeric(weights[letters[i]]) * as.numeric(counts[i])
	}
	weight_total
}
