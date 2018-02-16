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


RunConfiguration.setExecutionSettingFile('C:\\Users\\Hector\\AppData\\Local\\Temp\\Katalon\\Test Cases\\HT_DNA HTProduction Pipeline Test\\20180215_152028\\execution.properties')

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
import org.openqa.selenium.Keys as Keys

'use global script for log in with credentials and BASE_URL set there'
not_run: WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

not_run: WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

not_run: WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Files'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_test_GenScript_HT_DNA_DeliveryFile.xlsx'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Import Data'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/radioBtn_GenerateHTplates'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/button_Import'))

not_run: WebUI.setText(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Name'), 'TEST_HT_DNA_HTProduction 01')

not_run: WebUI.setText(findTestObject('Page_Generate HT Plate from HT Delivery File/input_toReplaceBlanks'), 'CNT0001396')

not_run: WebUI.click(findTestObject('Page_Generate HT Plate from HT Delivery File/btn_GenerateHTPlate'))

not_run: WebUI.delay(2)

'Get current page\\'s URL'
not_run: url = WebUI.getUrl()

not_run: if (!(url.contains('pipeline-status'))) {
    WebUI.navigateToUrl(url + 'pageId=Pipeline')
}

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

not_run: pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 
    'text')

'Check every 10 seconds for a status update'
not_run: while ((pipelineStatus == 'ht_plate_generator RUNNING') || (pipelineStatus == 'ht_plate_generator WAITING')) {
    WebUI.delay(10)

    pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')
}

not_run: assert pipelineStatus == 'COMPLETE'

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

not_run: WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))

not_run: WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

not_run: WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Files'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_test_GenScript_HT_DNA_DeliveryFile.xlsx'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/span_Import Data'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/radioBtn_GenerateHTplates'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/button_Import'))

WebUI.setText(findTestObject('Page_Generate HT Plate from HT Delivery File/input_toReplaceBlanks'), 'CNT0001396')

WebUI.click(findTestObject('Page_Generate HT Plate from HT Delivery File/chkBox_reproductionPlate'))

WebUI.delay(2)

WebUI.selectOptionByValue(findTestObject('Page_Generate HT Plate from HT Delivery File/select_reproductionPlateID'), 'HT0106', 
    false)

WebUI.click(findTestObject('Page_Generate HT Plate from HT Delivery File/btn_GenerateHTPlate'))

WebUI.delay(2)

'Get current page\\'s URL'
not_run: url = WebUI.getUrl()

not_run: WebUI.navigateToUrl(url + 'pageId=Pipeline')

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')

'Check every 10 seconds for a status update'
while ((pipelineStatus == 'ht_plate_generator RUNNING') || (pipelineStatus == 'ht_plate_generator WAITING')) {
    WebUI.delay(10)

    pipelineStatus = WebUI.getAttribute(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_PipelineStatus'), 'text')
}

assert pipelineStatus == 'COMPLETE'

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))

WebUI.closeBrowser()

''', 'Test Cases/HT_DNA HTProduction Pipeline Test', new TestCaseBinding('Test Cases/HT_DNA HTProduction Pipeline Test', [:]), FailureHandling.STOP_ON_FAILURE )
    
} catch (Exception e) {
    TestCaseMain.logError(e, 'Test Cases/HT_DNA HTProduction Pipeline Test')
}
