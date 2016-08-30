SELECT
SUBSTRING(sd.GlobalUniqueId, 0, charindex('#', sd.GlobalUniqueId)) compoundID,
otdp.ParentId ConstructID, 
count(*) numberOfVials,
sum(PrimaryVolume) "Total Amount (Mg)",
max(otdp.AAAnalysis_mg_ml_) "AAAnalysis (mg/ml)"
FROM "/FreezerProDemo".study.SpecimenDetail as sd
left outer join "/Optides/CompoundsRegistry/Samples/".samples.OTDProduction as otdp
on SUBSTRING(sd.GlobalUniqueId, 0, charindex('#', sd.GlobalUniqueId)) = otdp.OTDProductionID
group by SUBSTRING(sd.GlobalUniqueId, 0, charindex('#', sd.GlobalUniqueId)), otdp.ParentId