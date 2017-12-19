PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT *
from "/Optides/VIVOAssay/Sample/".assay.General.LSC.Data
where OTDCompoundID in (select OTDProductionID from "/Optides/CompoundsRegistry/Samples".samples.OTDProduction where ParentID in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey))



