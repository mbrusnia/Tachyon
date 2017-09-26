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


RunConfiguration.setExecutionSettingFile('C:\\Users\\Hector\\AppData\\Local\\Temp\\Katalon\\Test Cases\\HPLC Image Assay Pipeline\\20170925_144455\\execution.properties')

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

WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HPLC_Assay_Images'))

WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'), 0)

WebUI.doubleClick(findTestObject('Page_Assay Dashboard HTProduction/file_HT0102'))

WebUI.waitForElementPresent(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'), 0)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A01_Perfect_etc'))

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A02_Complex_etc'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A03_Simple_etc'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A04_Perfect_523.58_6.53.jpg'), FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Assay Dashboard HTProduction/file_HT01021A05'), FailureHandling.STOP_ON_FAILURE)

''', 'Test Cases/HPLC Image Assay Pipeline', new TestCaseBinding('Test Cases/HPLC Image Assay Pipeline', [:]), FailureHandling.STOP_ON_FAILURE )
    
} catch (Exception e) {
    TestCaseMain.logError(e, 'Test Cases/HPLC Image Assay Pipeline')
}
