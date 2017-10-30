import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory as CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as MobileBuiltInKeywords
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testcase.TestCaseFactory as TestCaseFactory
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository as ObjectRepository
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WSBuiltInKeywords
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUiBuiltInKeywords
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

'use global script for log in with credentials and BASE_URL set there'
WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.delay(2)

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Admin'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Manage_Views'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Add_Report'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_SampleComparison'))

WebUI.delay(2)

WebUI.setText(findTestObject('Page_Assay Dashboard OptidesCompoun/input_ReportName'), 'testAssayReport-')

WebUI.setText(findTestObject('Page_Assay Dashboard OptidesCompoun/textarea_JSON'), '{ "selectedColumns": [ {"name": "Name", "schemaName": "samples", "queryName": "Construct", "type":"VARCHAR"},{ "folderPath":"/Optides/InSilicoAssay/MolecularProperties", "schemaName": "assay.general.InSilicoAssay", "queryName": "Data", "name": "NetChargeAtpH7_4", "type": "DOUBLE", "sampleIdColumnName": "ID" },{ "folderPath":"/Optides/InSilicoAssay/MolecularProperties", "schemaName": "assay.general.InSilicoAssay", "queryName": "Data", "name": "ReducedForm_pI", "type": "DOUBLE", "sampleIdColumnName": "ID" } ]}')

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/btn_Submit'))

WebUI.verifyTextPresent('testAssayReport-', false)

WebUI.verifyTextPresent('Name', false)

WebUI.verifyTextPresent('NetChargeAtpH7_4', false)

WebUI.verifyTextPresent('ReducedForm_pI', false)

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Admin'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Manage_Views'))

WebUI.setText(findTestObject('Page_Assay Dashboard OptidesCompoun/input_Filter'), 'testAssayReport-')

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_EditPencil'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/btn_Delete'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/btn_Yes'))

