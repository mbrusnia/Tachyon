PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT Name, OTDReportID, OTDProductionID, HPLC_Classification, High_Resolution_MS, pdf_report, Culture_Scale, Final_VCD_Cells_mL, Final_Viability_Percent, Fusion_Yield_mg, Total_Yield_mg, Batch_Date, Scientist_initial, Comments
from "/Optides/OTDProduction/Assays/".samples.PDF_OTDReport_till_2017_May
where OTDProductionID in (select OTDProductionID from "/Optides/CompoundsRegistry/Samples".samples.OTDProduction where replace(ParentID, 'Construct.', '') in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey))
