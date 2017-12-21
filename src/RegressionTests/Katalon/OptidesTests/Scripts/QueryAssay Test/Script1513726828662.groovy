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

WebUI.mouseOver(findTestObject('Page_Start Page Optides/menu_Programs'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_QueryAssays'), 2)

WebUI.click(findTestObject('Page_Start Page Optides/a_QueryAssays'))

WebUI.setText(findTestObject('text_cIDs'), 'CNT0001396\r\nCNT0001356\r\nCNT000137\r\nCNT0001356\r\nCNT0001357\r\nCNT0001358\r\nCNT0001359\r\nCNT0001360\r\nCNT0001361\r\nCNT0001362\r\nCNT0001363\r\nCNT0001364\r\nCNT0001365\r\nCNT0001366\r\nCNT0001367')

WebUI.click(findTestObject('btn_submit1'))

WebUI.delay(3)

WebUI.verifyTextPresent('GSGCLEFWWKCNPNDDKCCRPKLKCSPLGKLCNFSFG', false)

WebUI.verifyTextPresent('OTD-000287', false)

WebUI.verifyTextPresent('4176.82', false)

WebUI.verifyTextPresent('4240.87', false)

WebUI.verifyTextPresent('HT01002B07', false)

WebUI.verifyTextPresent('HT01002C09', false)

WebUI.verifyTextPresent('830.99', false)

WebUI.verifyTextPresent('538.28', false)

WebUI.verifyTextPresent('CHH0000026', false)

WebUI.verifyTextPresent('OTDPDF0254', false)

WebUI.verifyTextPresent('CHH0000023', false)

WebUI.verifyTextPresent('MU00000397', false)

WebUI.closeBrowser()

