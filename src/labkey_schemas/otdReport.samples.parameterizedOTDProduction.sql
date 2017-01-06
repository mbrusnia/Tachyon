PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT Name, replace(ParentID, 'Construct.', '') as ParentID, OTDProductionID, AAAnalysis_mg_ml_, AAAReportDate
from "/Optides/CompoundsRegistry/Samples/".samples.OTDProduction
where Name in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)