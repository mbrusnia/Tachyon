PARAMETERS
(
    constructIDsLookupKey VARCHAR(32)

)
SELECT Name, CHEMProductionID, IntermediateProduct, DrugReagentID, LinkerReagentID, LinkerProperty,	 OTDProductionID,	
VariantID, ConjugationMethod, Amount_mg, AverageMW,	Comments, ReceivedBy, PickupBy
from "/Optides/CompoundsRegistry/Samples/".samples.CHEMProduction
where OTDProductionID in (select OTDProductionID from "/Optides/CompoundsRegistry/Samples".samples.OTDProduction where replace(ParentID, 'Construct.', '') in (select ConstructID from lists.constructQueryLookupTable where LookupKey=constructIDsLookupKey))
