SELECT const.ID,
const.AlternateName,
const.ParentID,
htp.HTProductID,
htp.ParentID constID,
assay.classification, 
const.AASeq
FROM Construct as const, HTProduction as htp, (select HTProductionID, classification, MaxPeakNR from "/Optides/HTProduction/Assays/".assay.General."HPLC Assays".Data group by HTProductionID, classification, MaxPeakNR) as assay
where const.ID = htp.ParentID and htp.HTProductID = assay.HTProductionID