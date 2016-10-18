REM This script pulls the IDs and sequence data from the optides-prod Constructs table, makes
REM a FASTA file out of them, then runs blast's makeblastdb program on that fasta file
REM in order to made a blast-able database out of the sequences.

set c2f_R_script="C:/Users/hramos/Documents/HRInternetConsulting/Clients/FHCRC/Tachyon/Tachyon/src/R/Construct2fasta.R"
set fastaOutFile="C:/labkey/tomcat-temp/OptideProdConstruct.fasta"
set R_program="C:/Program Files/R/R-3.2.2/bin/Rscript.exe"
set BLAST_DB_program="C:/Program Files/NCBI/blast-2.5.0+/bin/makeblastdb.exe"
set blastOutFile="C:/labkey/tomcat-temp/temp/OptideProdConstruct.db"


%R_program% %c2f_R_script% %fastaOutFile%

%BLAST_DB_program%  -in %fastaOutFile% -parse_seqids -dbtype prot -title "Optides Constructs BLAST Database" -out %blastOutFile%