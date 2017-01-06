PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT *
FROM "/Optides/FreezerPro/".samples.FreezerProVialSummary
WHERE compoundID in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)