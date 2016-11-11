PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT *
from "/Optides/HTProduction/Assays/".assay.General.Novocyte.Data
where HTProductionID in (select HTProductID from "/Optides/CompoundsRegistry/Samples".samples.HTProduction where ParentID in (select ConstructID from lists.constructTmpLookupTable where LookupKey=constructIDsLookupKey))