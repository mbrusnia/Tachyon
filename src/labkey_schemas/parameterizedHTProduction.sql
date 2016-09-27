PARAMETERS
(
    htID1 VARCHAR(32),
    htID2 VARCHAR(32),
    htID3 VARCHAR(32)

)
SELECT *
from "/Optides/HTProduction/Assays/".assay.General."HPLC Assays".Data
where HTProductionID in (htID1, htID2, htID3)