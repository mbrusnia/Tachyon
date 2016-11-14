PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT Name, ParentID, AlternateName, AASeq, Vector
from "/Optides/CompoundsRegistry/Samples/".samples.Construct
where Name in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey)