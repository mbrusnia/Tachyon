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

WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.mouseOver(findTestObject('LSC_objects/Page_Start Page Optides/a_VIVOAssay'))

WebUI.waitForElementVisible(findTestObject('LSC_objects/Page_Start Page Optides/a_Sample'), 0)

WebUI.click(findTestObject('LSC_objects/Page_Start Page Optides/a_Sample'))

WebUI.click(findTestObject('LSC_objects/Page_Assay Dashboard OptidesVIVOAss/a_LSC'))

WebUI.click(findTestObject('LSC_objects/Page_LSC Runs OptidesVIVOAssaySampl/span_Import Data'))

WebUI.selectOptionByLabel(findTestObject('Page_Data Import Batch Properties O (1)/select_studyDescription'), 'LSC0001 Tumor Homing of OTD257', 
    false)

WebUI.selectOptionByLabel(findTestObject('Page_Data Import Batch Properties O (1)/select_standardCurve'), 'STD_2016_0928', 
    false)

WebUI.click(findTestObject('LSC_objects/Page_Data Import Batch Properties O/span_Next'))

WebUI.setText(findTestObject('LSC_objects/Page_Data Import Run Properties and/input_name'), 'TEST0001')

WebUI.setText(findTestObject('LSC_objects/Page_Data Import Run Properties and/textarea_TextAreaDataCollector'), 'MouseID\tOTDCompoundID\tCHEMCompoundID\tReagentID\tTissue\tAcquisitionDate\tTissue_mg\tmg_per_ul\tLoaded_Volume_uL\tCPM\tLoaded_mg\tpCi\tpCi_per_uL\tFlag\r\nMU00000100\tOTD169\t\t\tKidney Cortex\t2016.11.2\t50\t0.16667\t16\t340\t\t\t\t\\n')

WebUI.click(findTestObject('LSC_objects/Page_Data Import Run Properties and/span_Save and Finish'))

WebUI.click(findTestObject('LSC_objects/Page_LSC Runs OptidesVIVOAssaySampl (1)/a_TEST0001'))

WebUI.waitForPageLoad(15)

WebUI.verifyTextPresent('148.54', false)

WebUI.verifyTextPresent('9.28', false)

WebUI.verifyTextPresent('2.67', false)

WebUI.verifyTextPresent('340.0', false)

WebUI.verifyTextPresent('0.16667', false)

WebUI.click(findTestObject('LSC_objects/Page_LSC Batches OptidesVIVOAssaySa/a_view runs'))

WebUI.click(findTestObject('LSC_objects/Page_LSC Runs OptidesVIVOAssaySampl (2)/input_.select'))

WebUI.click(findTestObject('LSC_objects/Page_LSC Runs OptidesVIVOAssaySampl (2)/span_Delete'))

WebUI.verifyTextPresent('TEST0001', false)

WebUI.click(findTestObject('LSC_objects/Page_Confirm Deletion OptidesVIVOAs/span_Confirm Delete'))

WebUI.closeBrowser()

