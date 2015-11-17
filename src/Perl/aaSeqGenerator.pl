# produce unique sequences for testing

# specify the range of compoundIDs to create (with corresponding sequences)
$UPPER_CID_LIMIT = 15999;  #max value can be 20^4
$LOWER_CID_LIMIT = 14990;

@AA = split(//, "ACDEFGHIKLMNPQRSTVWY");
$naas = 20;
print "compoundID\tSequence\n";

$i = 0;
for($a = 0; $a< $naas; $a++){
for($b = 0; $b< $naas; $b++){
for($c = 0; $c< $naas; $c++){
for($d = 0; $d< $naas; $d++){
	if($i > $UPPER_CID_LIMIT){exit;}  #upper limit for compoundID
	if($i >= $LOWER_CID_LIMIT){        #lower limit for compoundID
		$seq = $AA[$a] . $AA[$b] . $AA[$c] . $AA[$d] . "AAAAAAAAAAAAAAAAAAAAAAAAAAACCCAAAAAAAAAAAAAAAAAAAAA";
		print $i . "\t" . $seq ."\n";
	}
	$i++;
}}}}
