PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)
)

SELECT ID, ParentID, AlternateName, AASeq, Vector, AverageMass, MonoisotopicMass, ReducedForm_pI, NetChargeAtpH7_4
FROM "/Optides/InSilicoAssay/MolecularProperties/".assay.General.InSilicoAssay.Data
WHERE ID in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey)