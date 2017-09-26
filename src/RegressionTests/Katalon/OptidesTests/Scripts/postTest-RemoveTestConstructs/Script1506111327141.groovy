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

'use global script for log in with credentials and BASE_URL set there'
WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_HT_DNA'))

WebUI.click(findTestObject('Page_Sample Set HT_DNA OptidesCo/div_ConstructID'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TEST')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Construct'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_ID'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TEST')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Construct'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_ID'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TEST')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Variant'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/a_Sample Sets'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Homologue'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/div_AASeq'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Filter...'))

WebUI.setText(findTestObject('Page_Sample Set Construct OptidesCo/input_value_1'), 'TTEESSTT')

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/button_OK'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/input_.toggle'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Delete'))

WebUI.click(findTestObject('Page_Confirm Deletion OptidesCompou/span_Confirm Delete'))

WebUI.closeBrowser()

