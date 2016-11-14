PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT *
FROM "/Optides/FreezerPro/".samples.FreezerProVialSummary
WHERE constructID in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey)