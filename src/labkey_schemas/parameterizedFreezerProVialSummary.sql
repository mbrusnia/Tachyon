PARAMETERS
(
    cID1 VARCHAR(32),
    cID2 VARCHAR(32),
    cID3 VARCHAR(32)
)

SELECT *
FROM "/Optides/FreezerPro/".samples.FreezerProVialSummary
WHERE constructID in (cID1, cID2, cID3)
