select ID, AASeq
from "/Optides/CompoundsRegistry/Samples/".samples.Construct
where AASeq in(
   select AASeq
   from (
         SELECT AASeq, Vector
		FROM "/Optides/CompoundsRegistry/Samples/".samples.Construct table1
		group by AASeq, Vector
      	having count(*) > 1
		) t1
)
Union All
select ID, AASeq
from "/Optides/CompoundsRegistry/Samples/".samples.Variant
where AASeq in(
   select AASeq
   from "/Optides/CompoundsRegistry/Samples/".samples.Variant
	group by AASeq
	having count(*) > 1 
)
Union All
select ID, AASeq
from "/Optides/CompoundsRegistry/Samples/".samples.Homologue
where AASeq in(
   select AASeq
   from "/Optides/CompoundsRegistry/Samples/".samples.Homologue
	group by AASeq
	having count(*) > 1
)