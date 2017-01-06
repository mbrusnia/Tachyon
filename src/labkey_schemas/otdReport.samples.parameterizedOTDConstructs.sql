PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT Name, ParentID, AlternateName, AASeq, Vector
from "/Optides/CompoundsRegistry/Samples/".samples.Construct
where Name in (select replace(ParentID, 'Construct.', '') as pIDS from "/Optides/CompoundsRegistry/Samples/".samples.OTDProduction where OTDProductionID in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey))