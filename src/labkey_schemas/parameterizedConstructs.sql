PARAMETERS
(
    cID1 VARCHAR(32),
    cID2 VARCHAR(32),
    cID3 VARCHAR(32)

)
SELECT Name, ParentID, AlternateName, AASeq, Vector
from "/Optides/CompoundsRegistry/Samples/".samples.Construct
where Name in (cID1, cID2, cID3)
