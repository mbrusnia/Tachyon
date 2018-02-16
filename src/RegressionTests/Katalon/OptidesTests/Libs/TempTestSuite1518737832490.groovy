import com.kms.katalon.core.logging.KeywordLogger
import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.reporting.ReportUtil
import com.kms.katalon.core.main.TestCaseMain
import com.kms.katalon.core.testdata.TestDataColumn
import groovy.lang.MissingPropertyException
import com.kms.katalon.core.testcase.TestCaseBinding
import com.kms.katalon.core.driver.internal.DriverCleanerCollector
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.configuration.RunConfiguration
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData



def static runTestCase_0() {
    TestCaseMain.runTestCase('Test Cases/postTest-RemoveTestConstructs', new TestCaseBinding('Test Cases/postTest-RemoveTestConstructs',  null), FailureHandling.STOP_ON_FAILURE)
    
}


Map<String, String> suiteProperties = new HashMap<String, String>();


suiteProperties.put('id', 'Test Suites/OptidesLabkeyTests')

suiteProperties.put('name', 'OptidesLabkeyTests')

suiteProperties.put('description', '')
 

DriverCleanerCollector.getInstance().addDriverCleaner(new com.kms.katalon.core.webui.contribution.WebUiDriverCleaner())
DriverCleanerCollector.getInstance().addDriverCleaner(new com.kms.katalon.core.mobile.contribution.MobileDriverCleaner())



RunConfiguration.setExecutionSettingFile("C:\\Users\\Hector\\Documents\\HRInternetConsulting\\Clients\\FHCRC\\Tachyon\\src\\RegressionTests\\Katalon\\OptidesTests\\Reports\\OptidesLabkeyTests\\20180215_153712\\execution.properties")

TestCaseMain.beforeStart()

KeywordLogger.getInstance().startSuite('OptidesLabkeyTests', suiteProperties)

(0..0).each {
    "runTestCase_${it}"()
}

DriverCleanerCollector.getInstance().cleanDrivers()

KeywordLogger.getInstance().endSuite('OptidesLabkeyTests', null)
