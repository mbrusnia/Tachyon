PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT Name, OTDProductionID, purificationMethod, totalAmount_mg, cultureScale_L, comments, notebook, reviewed, gel, HPLC, finalViability_Percent, finalViableCellCount_Cells_per_mL
FROM "/Optides/OTDProduction/Assays/".samples.OTDProductionReport
WHERE Name in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)