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


RunConfiguration.setExecutionSettingFile('C:\\Users\\Hector\\AppData\\Local\\Temp\\Katalon\\Test Cases\\postTest-RemoveTestConstructs\\20170930_145818\\execution.properties')

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

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_HT_DNA'))

not_run: WebUI.click(findTestObject('Page_Sample Set HT_DNA OptidesCo/div_ConstructID'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

not_run: WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TST')

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

not_run: WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

not_run: WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Construct'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_ID'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

not_run: WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TST')

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

not_run: WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

not_run: WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

not_run: WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

not_run: WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Construct'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

not_run: WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

not_run: WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

not_run: WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Variant'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

not_run: WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

not_run: WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

not_run: WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Homologue'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_CHEMProduction'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/div_Received By'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/a_All'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/a_TESTTEST'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.closeBrowser()

''', 'Test Cases/postTest-RemoveTestConstructs', new TestCaseBinding('Test Cases/postTest-RemoveTestConstructs', [:]), FailureHandling.STOP_ON_FAILURE )
    
} catch (Exception e) {
    TestCaseMain.logError(e, 'Test Cases/postTest-RemoveTestConstructs')
}
