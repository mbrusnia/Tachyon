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

'use global script for log in with credentials and BASE_URL set there'
WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Files'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_test_GenScript_HT_DNA_DeliveryFile.xlsx'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Import Data'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/radioBtn_Upload_HT_Delivery'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/button_Import'))

WebUI.delay(3)

if (WebUI.verifyElementVisible(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Name'), FailureHandling.OPTIONAL)) {
    WebUI.setText(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Name'), 'TEST_HT_DNA_Delivery 01')
}

WebUI.click(findTestObject('Page_Upload_HT_Delivery_Info/a_submitButton'))

WebUI.delay(2)

'Get current page\'s URL'
url = WebUI.getUrl()

if (!(url.endsWith('pipeline-status/Optides/CompoundsRegistry/Samples/showList.view?'))) {
    WebUI.navigateToUrl(url + 'pageId=Pipeline')
}

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')

'Check every 10 seconds for a status update'
while ((pipelineStatus == 'ht_delivery RUNNING') || (pipelineStatus == 'ht_delivery WAITING')) {
    WebUI.delay(10)

    pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')
}

assert pipelineStatus == 'COMPLETE'

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))

WebUI.closeBrowser()

