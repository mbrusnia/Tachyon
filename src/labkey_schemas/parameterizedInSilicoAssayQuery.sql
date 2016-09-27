PARAMETERS
(
    cID1 VARCHAR(32),
    cID2 VARCHAR(32),
    cID3 VARCHAR(32)
)

SELECT ID, ParentID, AlternateName, AASeq, Vector, AverageMass, MonoisotopicMass, ReducedForm_pI, NetChargeAtpH7_4
FROM "/Optides/InSilicoAssay/MolecularProperties/".assay.General.InSilicoAssay.Data
WHERE ID in (cID1, cID2, cID3)