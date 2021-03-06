import com.kms.katalon.core.main.TestCaseMain
import com.kms.katalon.core.logging.KeywordLogger
import groovy.lang.MissingPropertyException
import com.kms.katalon.core.testcase.TestCaseBinding
import com.kms.katalon.core.driver.internal.DriverCleanerCollector
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.contribution.WebUiDriverCleaner
import com.kms.katalon.core.mobile.contribution.MobileDriverCleaner


DriverCleanerCollector.getInstance().addDriverCleaner(new com.kms.katalon.core.webui.contribution.WebUiDriverCleaner())
DriverCleanerCollector.getInstance().addDriverCleaner(new com.kms.katalon.core.mobile.contribution.MobileDriverCleaner())


RunConfiguration.setExecutionSettingFile('C:\\Users\\Hector\\AppData\\Local\\Temp\\Katalon\\Test Cases\\HPLC Image Assay Pipeline\\20180327_142318\\execution.properties')

TestCaseMain.beforeStart()
try {
    
        TestCaseMain.runTestCaseRawScript(
'''import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
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
not_run: WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.mouseOver(findTestObject('Page_Start Page Optides/menu_HTProduction'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Assays'), 0)

not_run: WebUI.click(findTestObject('Page_Start Page Optides/a_Assays'))

not_run: WebUI.refresh()

not_run: WebUI.delay(3, FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HPLC_Assay_Images'))

not_run: WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'), 0)

not_run: WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'))

not_run: WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'), 0)

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A02_Complex_etc'), FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A03_Simple_etc'), FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A04_Perfect_523.58_6.53.jpg'), FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A05'), FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Import Data'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/radioBtn_Update HPLC Assay'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/button_Import'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Upload_HT_Delivery_Info/a_submitButton'), 0)

not_run: if (WebUI.verifyElementVisible(findTestObject('input_ProtocolName'), FailureHandling.CONTINUE_ON_FAILURE)) {
    WebUI.setText(findTestObject('input_ProtocolName'), 'TEST -Assay Image Import01')
}

not_run: WebUI.click(findTestObject('Page_Upload_HT_Delivery_Info/a_submitButton'))

not_run: WebUI.delay(2)

'Get current page\\'s URL'
not_run: url = WebUI.getUrl()

not_run: WebUI.navigateToUrl(url + 'pageId=Pipeline')

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

not_run: pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 
    'text')

'Check every 10 seconds for a status update'
not_run: while ((pipelineStatus == 'insert_jpegs RUNNING') || (pipelineStatus == 'insert_jpegs WAITING')) {
    WebUI.delay(10)

    pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')
}

not_run: assert pipelineStatus == 'COMPLETE'

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/link_Assays'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/link_HPLC Assays'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/chkbx_HPLC_HT0102'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/btn_Delete'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/btn_ConfirmDelete'))

WebUI.closeBrowser()

''', 'Test Cases/HPLC Image Assay Pipeline', new TestCaseBinding('Test Cases/HPLC Image Assay Pipeline', [:]), FailureHandling.STOP_ON_FAILURE )
    
} catch (Exception e) {
    TestCaseMain.logError(e, 'Test Cases/HPLC Image Assay Pipeline')
}
