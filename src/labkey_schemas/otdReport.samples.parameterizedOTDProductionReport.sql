PARAMETERS
(
    otdIDsLookupKey VARCHAR(32)

)
SELECT *
FROM "/Optides/OTDProduction/Assays/".samples.OTDProductionReport
WHERE Name in (select OTDID from lists.otdQueryLookupTable where LookupKey=otdIDsLookupKey)

/**** In order for this to work propertly, the following code must be added to the "XML Metadata" tab of the query definition UI in Labkey:
<tables xmlns="http://labkey.org/data/xml">
   <table tableName="parameterizedOTDProductionReport" tableDbType="NOT_IN_DB">
      <columns>
         <column columnName="RowId">
           <isHidden>true</isHidden>
         </column>
         <column columnName="SourceProtocolApplication">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Protocol">
           <isHidden>true</isHidden>
         </column>
         <column columnName="SampleSet">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Folder">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Run">
           <isHidden>true</isHidden>
         </column>
         <column columnName="LSID">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Created">
           <isHidden>true</isHidden>
         </column>
         <column columnName="CreatedBy">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Modified">
           <isHidden>true</isHidden>
         </column>
         <column columnName="ModifiedBy">
           <isHidden>true</isHidden>
         </column>
         <column columnName="Flag">
           <isHidden>true</isHidden>
         </column>
     </columns>
   </table>
</tables>
**************************/