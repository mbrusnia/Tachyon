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
import java.text.NumberFormat as NumberFormat
import java.util.Locale as Locale

'use global script for log in with credentials and BASE_URL set there'
WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

numOfConstructs = WebUI.getText(findTestObject('Page_Assay Dashboard OptidesCompoun/text_NumberOfConstructs'))

WebUI.mouseOver(findTestObject('Page_Start Page Optides/menu_InSilicoAssay'))

WebUI.waitForElementPresent(findTestObject('Page_Start Page Optides/menu_InSilicoAssay_MolecularProperties'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/menu_InSilicoAssay_MolecularProperties'))

WebUI.click(findTestObject('Page_InSilicoAssay Assay Dashboard/a_InSilicoAssay'))

WebUI.click(findTestObject('Page_InSilicoAssay Assay Dashboard/input_Toggle'))

WebUI.click(findTestObject('Page_InSilicoAssay Assay Dashboard/a_ViewResults'))

String formatted = NumberFormat.getNumberInstance(Locale.US).format(Integer.valueOf(numOfConstructs))

System.out.println(formatted)

WebUI.verifyTextPresent(formatted, false)

WebUI.closeBrowser()

