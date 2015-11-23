//This is a trigger script to be used on the compound sequences datatable.
//It ensures uniqueness of incoming sequences against the database of already
//uploaded sequences.  In order for this script to be triggered upon insertion
//into the "IdentifiedCompounds" table, it must be placed in this directory:
// /LabKey Server/modules/study/queries/study/
// note: the name of this file has to be the same as the labkey "query"/table
// note2: you can see the console output in labkey by going: Admin -> Developer Links -> Server Javascript Console

var console = require("console");
var LABKEY = require("labkey");

var QUERY_NAME = 'IdentifiedCompounds';  //this must be the same as the filename of this script
var COMPOUND_ID_COL_NAME = 'compoundID';
var SEQUENCE_COL_NAME = 'Sequence';

console.log("** evaluating: " + this['javax.script.filename']);

//stores compoundID and Sequence data already in the database
var myResults;  

//to compute runtime
var startTime = new Date().getTime();  

function init(event, errors) {
    console.log("init() called in orgs.js with an event type of " + event);
	
	//fetch all compoundID, sequence data already uploaded to this dataset
	LABKEY.Query.selectRows({
			requiredVersion: 9.1,
			schemaName: 'study',
			queryName: QUERY_NAME,
			columns: COMPOUND_ID_COL_NAME + ', ' + SEQUENCE_COL_NAME,
			filterArray: null,
			success: onSuccess,
			failure: onError
		});
		
		function onSuccess(results) {
			myResults = results
		}	
		function onError(errorInfo) {
			errors[errors.length] = errorInfo.exception;
		}
		
		//TODO VERIFY THAT ALL INCOMING SEQUENCES ARE NOT DUPLICATES AMONGST THEMSELVES
}

function complete(event, errors) {
    console.log("complete() called in orgs.js with an event type of " + event);	
}

//this function is called before a new row is inserted into the database
function beforeInsert(row, errors){
    console.log("beforeInsert() called in orgs.js with a row object of  " + row);
	
	var length = myResults.rows.length;
	
	//iterate through existing sequences to ensure uniqueness of new sequence
	for (var idxRow = 0; idxRow < length; idxRow++) {
		if(row[SEQUENCE_COL_NAME] === myResults.rows[idxRow][SEQUENCE_COL_NAME].value){
			var str = "THIS SEQUENCE HAS ALREADY BEEN UPLOADED INTO THE DATABASE: " + row[SEQUENCE_COL_NAME] + 
					" (" + COMPOUND_ID_COL_NAME + ": " + row[COMPOUND_ID_COL_NAME] + ") -PLEASE REMOVE AND TRY YOUR UPLOAD AGAIN.";
			console.log(str);
			throw new Error(str);
		}
	}
	
	//compute execution time
	var end = new Date().getTime();
	var time = end - startTime;
	console.log('Execution time: ' + time);
}

function beforeUpdate(row, oldRow, errors){
    console.log("beforeUpdate() called in orgs.js with a row object of  " + row + "  and an oldRow of " + oldRow);	
}
function beforeDelete(row, errors){
    console.log("beforeDelete() called in orgs.js with a row object of  " + row);	
}
function afterInsert(row, errors){
    console.log("afterInsert() called in orgs.js with a row object of  " + row);	
}
function afterUpdate(row, oldRow, errors){
    console.log("afterUpdate() called in orgs.js with a row object of  " + row + "  and an oldRow of " + oldRow);	
}
function afterDelete(row, errors){
    console.log("afterDelete() called in orgs.js with a row object of  " + row);	
}
