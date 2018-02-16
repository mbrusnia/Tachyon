--once you've reconstructed the database from production onto your
--dev server, run this sql script against the new database (usually Optides_Prod)
UPDATE prop.Properties 
SET Value = 'https://optides-dev-lk1.fhcrc.org'
--SET Value = 'https://localhost.fhcrc.org' 
WHERE Name = 'baseServerURL' 
AND (SELECT s.Category FROM prop.PropertySets AS s WHERE s.[Set] = prop.Properties.[Set]) = 'SiteConfig' 
; 

--will inactivate all users except site admins, labkey, and those individually designated 
UPDATE core.Principals 
SET Active = 'FALSE' 
WHERE type = 'u' 
      AND UserId NOT IN (select p.UserId from core.Principals p inner join core.Members m on (p.UserId = m.UserId and m.GroupId=-1)) 
      AND Name NOT LIKE '%@labkey.com' 
      AND Name NOT IN ('mbrusniak@fhcrc.org', 'hramos@fhcrc.org') 
; 

--update the path to R.exe
--UPDATE prop.Properties
--SET Value = 'C:\THE-PATH-OF-YOUR-R-EXECUTABLE\R.exe'
--WHERE Name = 'exePath' AND Value LIKE '%R.exe'
--;


--adds in Hector as a Site Admin
INSERT INTO core.members (UserId,GroupId)
VALUES (1042, -1)
;

--Updates the LabKey Instance Name
UPDATE prop.Properties 
SET Value = 'Optide Dev Server' 
WHERE Name = 'systemShortName' 
AND (SELECT s.Category FROM prop.PropertySets AS s WHERE s.[Set] = prop.Properties.[Set]) = 'LookAndFeel'; 

--Updates the Description of the LabKey Instance
UPDATE prop.Properties 
SET Value = 'Optide Dev Server' 
WHERE Name = 'systemDescription' 
AND (SELECT s.Category FROM prop.PropertySets AS s WHERE s.[Set] = prop.Properties.[Set]) = 'LookAndFeel'; 

--Updates the color theme
UPDATE prop.Properties 
SET Value = 'Leaf' 
WHERE Name = 'themeName' 
AND (SELECT s.Category FROM prop.PropertySets AS s WHERE s.[Set] = prop.Properties.[Set]) = 'LookAndFeel'; 

--Updates the path of where the files are located
UPDATE prop.Properties 
SET Value = 'C:\labkey\labkey\files' 
WHERE Name = 'webRoot' 
AND (SELECT s.Category FROM prop.PropertySets AS s WHERE s.[Set] = prop.Properties.[Set]) = 'LookAndFeel';
