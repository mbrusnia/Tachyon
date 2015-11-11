@AA = split(//, "ACDEFGHIKLMNPQRSTVWY");
$naas = 20;
$aaLength = 20;

print "compoundID\tSequence\n";

$i = 0;
for($a = 0; $a< $naas; $a++){
for($b = 0; $b< $naas; $b++){
for($c = 0; $c< $naas; $c++){
for($d = 0; $d< $naas; $d++){
	$seq = $AA[$a] . $AA[$b] . $AA[$c] . $AA[$d] . "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	print $i++ . "\t" . $seq ."\n";
	if($i > 10000){exit;}
}}}}
