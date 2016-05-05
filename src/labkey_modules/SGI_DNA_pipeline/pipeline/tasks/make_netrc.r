#######################################################################################
##
## Make the _netrc file we need in order to connect to the database through rlabkey
##
#######################################################################################
f = file(description="_netrc", open="w")
cat(file=f, sep="", "machine optides-stage.fhcrc.org", "\n")
cat(file=f, sep="", "login brusniak.computelifesci@gmail.com", "\n")
cat(file=f, sep="", "password Kn0ttin10K", "\n")
flush(con=f)
close(con=f)
######################################
## end
######################################
