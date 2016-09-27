PARAMETERS
(
    cID1 VARCHAR(32),
    cID2 VARCHAR(32),
    cID3 VARCHAR(32)

)
SELECT HTProductID, Replace(ConstructID, 'Construct.', '') AS ConstructID
from "/Optides/CompoundsRegistry/Samples/".samples.HTProduction
where Replace(ConstructID, 'Construct.', '') in (cID1, cID2, cID3)