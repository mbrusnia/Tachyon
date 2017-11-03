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


RunConfiguration.setExecutionSettingFile('C:\\Users\\Hector\\AppData\\Local\\Temp\\Katalon\\Test Cases\\updateMWs Test\\20171030_170653\\execution.properties')

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
import groovy.time.TimeCategory as TimeCategory

'use global script for log in with credentials and BASE_URL set there'
not_run: WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

today = null

'TODAY\\'S DATE.  used to check "modified" columns in the various tables with MWs.\\t'
use(TimeCategory, { 
        today = new Date()
    })

dateStr = today.format('yyyy-MM-dd')

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_CHEMProduction'))

'Check CHEMProduction table to see if weights were updated today.'
WebUI.verifyTextPresent(dateStr, false)

WebUI.mouseOver(findTestObject('Page_Assay Dashboard OptidesCompoun/menu_OTDProduction'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/menu_OTDProduction_Assays'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/menu_OTDProduction_Assays'))

WebUI.click(findTestObject('Page_OTDProduction Assay Dashboard/a_OTDProductionReport'))

'Check CHEMProduction table to see if weights were updated today.'
WebUI.verifyTextPresent(dateStr, false)

WebUI.mouseOver(findTestObject('Page_Start Page Optides/menu_CHEMProduction'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/menu_CHEMProduction_Assays'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/menu_CHEMProduction_Assays'))

WebUI.click(findTestObject('a_ChemProductionReport'))

'Check CHEMProduction table to see if weights were updated today.'
WebUI.verifyTextPresent(dateStr, false)

''', 'Test Cases/updateMWs Test', new TestCaseBinding('Test Cases/updateMWs Test', [:]), FailureHandling.STOP_ON_FAILURE )
    
} catch (Exception e) {
    TestCaseMain.logError(e, 'Test Cases/updateMWs Test')
}
