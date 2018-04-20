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

WebUI.mouseOver(findTestObject('Page_Start Page Optides/menu_HTProduction'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Assays'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Assays'))

WebUI.refresh()

WebUI.delay(3, FailureHandling.STOP_ON_FAILURE)

WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HPLC_Assay_Images'))

WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'), 0)

WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'))

WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'), 0)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A02_Complex_etc'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A03_Simple_etc'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A04_Perfect_523.58_6.53.jpg'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A05'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Import Data'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/radioBtn_Update HPLC Assay'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/button_Import'))

WebUI.waitForElementVisible(findTestObject('Page_Upload_HT_Delivery_Info/a_submitButton'), 0)

if(WebUI.verifyElementVisible(findTestObject('input_ProtocolName'), FailureHandling.OPTIONAL)){
	WebUI.setText(findTestObject('input_ProtocolName'), 'TEST -Assay Image Import01')
}
WebUI.click(findTestObject('Page_Upload_HT_Delivery_Info/a_submitButton'))

WebUI.delay(2)

'Get current page\'s URL'
url = WebUI.getUrl()

WebUI.navigateToUrl(url + 'pageId=Pipeline')

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')

'Check every 10 seconds for a status update'
while ((pipelineStatus == 'insert_jpegs RUNNING') || (pipelineStatus == 'insert_jpegs WAITING')) {
    WebUI.delay(10)

    pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')
}

assert pipelineStatus == 'COMPLETE'

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/link_Assays'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/link_HPLC Assays'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/chkbx_HPLC_HT0102'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/btn_Delete'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/btn_ConfirmDelete'))

WebUI.closeBrowser()

