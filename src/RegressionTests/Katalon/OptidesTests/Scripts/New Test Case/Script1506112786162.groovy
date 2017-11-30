import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory as CheckpointFactory
import com.kms.katalon.core.exception.StepFailedException as StepFailedException
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

WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_CHEMProduction'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/div_Received By'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Filter'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/a_All'))

WebUI.click(findTestObject('Page_CHEMProduction SampleSet/a_TESTTEST'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

