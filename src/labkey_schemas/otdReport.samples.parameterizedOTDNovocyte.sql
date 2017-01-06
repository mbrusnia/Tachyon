PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT Sample, Specimen, M3_Percent_Parent as "M3 % Parent", M3_Median_FITC_H as "M3 Median FITC-H", P1_percent_All as "P1 % All", R2_percent_Parent as "R2 % Parent", Run_Time, R2_Abs_Count as "R2 Abs Count"
FROM "/Optides/OTDProduction/Assays/".assay.General.Novocyte_TransductionReport.Data
WHERE Sample in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)