PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT Sample, percent_GFP as "% GFP", Median_GFP as "Median GFP", FSC_over_SSC as "FSC/SSC", percent_Viable as "% Viable", Abs_Count as "Absolute Count"
FROM "/Optides/OTDProduction/Assays/".assay.General.Novocyte_TransductionReport.Data
WHERE Sample in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)