# produce unique sequences for testing

# specify the range of compoundIDs to create (with corresponding sequences)
$LOWER_CID_LIMIT = 19000;
$UPPER_CID_LIMIT = 20470;  #max value can be 20^4

@AA = split(//, "ACDEFGHIKLMNPQRSTVWY");
$naas = 20;
print "Name\tID\tParent ID\tVector\tAASeq\n"; #\tAverageMass\tMonoisotopicMass\tpI\n";

@vectors = ("VCR010", "VCR011", "VCR012", "VCR020", "VCR021", "VCR030", "VCR040", "VCR050", "VCR000", "VCR060");

@parentIDs = ("VAR0000015", "VAR0000019", "VAR0000025", "VAR0000035", "VAR0000045", "VAR0000055", "VAR0000065", "VAR0000075", "Var0000085", "Var0000095");

$i = 0; #$LOWER_CID_LIMIT;
for($a = 0; $a< $naas; $a++){
for($b = 0; $b< $naas; $b++){
for($c = 0; $c< $naas; $c++){
for($d = 0; $d< $naas; $d++){
	if($i > $UPPER_CID_LIMIT){exit;}  #upper limit for compoundID
	if($i >= $LOWER_CID_LIMIT){        #lower limit for compoundID
		$seq = "GS" . $AA[$a] . $AA[$b] . $AA[$c] . $AA[$d] . "AAAAAAAAAAAAAAAAAAAAACCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCAAAAAAA";
		print "CNT" . $i . "\t". "CNT" . $i . "\t" . $parentIDs[int(rand(10))] . "\t" . $vectors[int(rand(10))] . "\t" . $seq . "\n";
	}
	$i++;
}}}}
