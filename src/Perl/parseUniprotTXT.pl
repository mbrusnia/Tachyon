use LWP::Simple;

open( my $fh, '<', "C:/Users/Hector/Documents/HRInternetConsulting/Clients/FHCRC/Project25 - Research/Project25_ProteinList.txt" ) or die "Can't open $filename: $!";
my $i = 0;
    while ( my $protID = <$fh>) {
		chomp $protID;
		my $link = "http://www.uniprot.org/uniprot/" . $protID . ".txt";
		#print $link . "\n";
		$content = get($link);
			die "Couldn't get it!" unless defined $content;
		
		my $id;
		my $chain = "false";
		my $chainId;
		my $signalPep = "false";
		my $isSeq = "false";
		my $seq;
		my $organism;
		@a = split(/\n/, $content);
		for ($j=0; $j < scalar(@a); $j++){
			my $ln = $a[$j];
			#<feature type="signal peptide"> 
			#<feature type="chain"> with id starting with “PRO”,
			if ( $ln =~ /^OS   (\w+ \w+).*$/) {
				$organism = $1;
			}elsif ( $ln =~ /^FT   SIGNAL/) {
				$signalPep = "true";
			}elsif ( $ln =~ /^FT   CHAIN/) {
				$j++;
				if($a[$j] =~ /FTId=PRO/){
					$chain = "true";
				}
			}elsif($ln =~ /SQ   SEQUENCE/){
				$isSeq = "true";
			}elsif($isSeq eq "true"){
				if($ln =~/^\/\//){
					$isSeq = "false";
				}else{
					$seq .=  join('', split(/\W+/, $ln))
				}
			}
		}
		
		if($chain eq "true" or $signalPep eq "true"){
			#print "---YES-------\n" . "http://www.uniprot.org/uniprot/" . $protID . ".txt\n";
			print ">" . $protID . ":";
			if($signalPep eq "true"){
				print "signal peptide";
			}
			if($signalPep eq "true" and $chain eq "true"){
				print ",";
			}
			if($chain eq "true"){
				print "PRO";
			}
			print "," . $organism;
			print "\n" . $seq . "\n";
		} 
		#else{
		#	print "---NO--------\n" . "http://www.uniprot.org/uniprot/" . $protID . ".txt\n\n";
		#}
    }
close $fh;
