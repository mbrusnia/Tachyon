# produce unique sequences for testing

# specify the range of compoundIDs to create (with corresponding sequences)
$LOWER_CID_LIMIT = 100000;
$UPPER_CID_LIMIT = 100099;  #max value can be 20^4

@AA = split(//, "ACDEFGHIKLMNPQRSTVWY");
$naas = 20;
print "ID\tParent ID\tAASeq\n"; #\tAverageMass\tMonoisotopicMass\tpI\n";

$i = 0; #$LOWER_CID_LIMIT;
for($a = 0; $a< $naas; $a++){
for($b = 0; $b< $naas; $b++){
for($c = 0; $c< $naas; $c++){
for($d = 0; $d< $naas; $d++){
	if($i > $UPPER_CID_LIMIT){exit;}  #upper limit for compoundID
	if($i >= $LOWER_CID_LIMIT){        #lower limit for compoundID
		$seq = $AA[$a] . $AA[$b] . $AA[$c] . $AA[$d] . "AAAAAAAAAAAAAAAAAAAAAAAAAAACCCAAAAAAAAAAAAAAAAAAAAA";
		print $i . "\tVAR0000001\t" . $seq . "\n";
	}
	$i++;
}}}}
