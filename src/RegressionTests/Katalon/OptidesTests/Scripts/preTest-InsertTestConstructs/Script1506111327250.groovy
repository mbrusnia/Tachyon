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
not_run: WebUI.callTestCase(findTestCase('OpenAndLogIn'), [:], FailureHandling.STOP_ON_FAILURE)

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.delay(2)

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Pipeline'))

WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/a_All'))

if (!WebUI.verifyTextPresent('No data to show.', false)) {
    WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/input_Toggle'))

    WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_Delete'))

    WebUI.click(findTestObject('Page_Data Pipeline - CompoundsRegistry/btn_ConfirmDelete'))
}

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.waitForElementVisible(findTestObject('Page_Start Page Optides/a_Samples_CompReg'), 0)

WebUI.click(findTestObject('Page_Start Page Optides/a_Samples_CompReg'))

WebUI.click(findTestObject('Page_Assay Dashboard OptidesCompoun/a_Construct'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Insert'))

WebUI.click(findTestObject('Page_Sample Set Construct OptidesCo/span_Import Bulk Data'))

WebUI.click(findTestObject('Page_Import Sample Set OptidesCompo/input_insertUpdateChoice'))

WebUI.setText(findTestObject('Page_Import Sample Set OptidesCompo/textarea_data'), 'Name\tID\tParent ID\tAlternate Name\tAASeq\tVector\t\t\t\nTEST0000013\tTEST0000013\tVAR0000016\tOTD-000054\tGSSEKDCIKHLQRCRENKDCCSKKCSRRGTNPEKRCR\tVCR020\t\t\t\nTEST0000026\tTEST0000026\tVAR0000046\tOTD-000071\tGSGDCLPHLKRCKADNDCCGKKCKRRGTNAEKRCR\tVCR020\t\t\t\nTEST0000037\tTEST0000037\tVAR0000028\tOTD-000087\tGSGDCLPHLKRCKENNDCCSKKCKRRGANPEKRCR\tVCR020\t\t\t\nTEST0001397\tTEST0001397\tVAR0001397\tOTD-000035\tGSMCMPCFTTDHQMARRCDDCCGGRGRGRCYGPQCLCR\tVCR020\t\t\t\nTEST0001463\tTEST0001463\tVAR0001462\tCTX_WT, OTD-000194\tGSMCMPCFTTDHQMARKCDDCCGGKGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005460\tTEST0005460\tVAR0005368\tOTD-000313\tGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005476\tTEST0005476\tVAR0005386\tTat_TB1G2\tGSGRKKRRQRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005477\tTEST0005477\tVAR0005387\tCysTat_TB1G2\tGSCYRKKRRQRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005478\tTEST0005478\tVAR0005388\tS19-Tat_TB1G2\tGSPFVIGAGVLGALGTGIGGIGRKKRRQRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005479\tTEST0005479\tVAR0005389\tPas-Tat_TB1G2\tGSFFLIPKGGRKKRRQRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005480\tTEST0005480\tVAR0005390\tR8_TB1G2\tGSRRRRRRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005481\tTEST0005481\tVAR0005391\tPas-R8_TB1G2\tGSFFLIPKGRRRRRRRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005482\tTEST0005482\tVAR0005392\tpAntp_TB1G2\tGSRQIKIWFQNRRMKWKKGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005483\tTEST0005483\tVAR0005393\tPas-pAntp_TB1G2\tGSFFLIPKGRQIKIWFQNRRMKWKKGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005484\tTEST0005484\tVAR0005394\tPas-FHV_TB1G2\tGSFFLIPKGRRRRNRTRRNRRRVRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005485\tTEST0005485\tVAR0005395\tMCa(1-9)_TB1G2\tGSGDALPHLKLGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005486\tTEST0005486\tVAR0005396\tImp(1-9)_TB1G2\tGSGDALPHLKRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005487\tTEST0005487\tVAR0005397\tHad(1-11)_TB1G2\tGSSEKDAIKHLQRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005488\tTEST0005488\tVAR0005398\tHad(3-11)_TB1G2\tGSKDAIKHLQRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005489\tTEST0005489\tVAR0005399\tybbR_TB1G2\tGSVLDSLEFIASKLGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005490\tTEST0005490\tVAR0005400\tQ15(F2R4)_TB1G2\tGSPDEYIERAKECCKKFFRRRRDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005491\tTEST0005491\tVAR0005401\tCTX_TEAD_EYE\tGSMCMPCDTTDHLMALFCDGCCGNSGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005492\tTEST0005492\tVAR0005402\tB55_TB1G2\tGSKAVLGATKIDLPVDINDPYDLGLLLRHLRHHSNLLANIGDPAVREQVLSAMQEEEGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005493\tTEST0005493\tVAR0005403\tazu_TB1G2\tGSLSTAADMQGVVTDGMASGLDKDYLKPDDGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005494\tTEST0005494\tVAR0005404\tIMT-P8_TB1G2\tGSRRWRRWNRFNRRRCRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005495\tTEST0005495\tVAR0005405\tBR2_TB1G2\tGSRAGLQFPVGRLLRRLLRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005496\tTEST0005496\tVAR0005406\tOMOTAG1_TB1G2\tGSKRAHHNALERKRRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005497\tTEST0005497\tVAR0005407\tOMOTAG2_TB1G2\tRRMKANARERNRMGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005498\tTEST0005498\tVAR0005408\tpVEC_TB1G2\tGSLLIILRRRIRKQAHAHSKGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005499\tTEST0005499\tVAR0005409\tSynB3_TB1G2\tGSRRLSYSRRRFGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005500\tTEST0005500\tVAR0005410\tDPV1047_TB1G2\tGSVKRGLKLRHVRPRVTRMDVGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005501\tTEST0005501\tVAR0005411\tC105Y_TB1G2\tGSCSIPPEVKFNKPFVYLIGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005502\tTEST0005502\tVAR0005412\tTransportan_TB1G2\tGSGWTLNSAGYLLGKINLKALAALAKKILGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005503\tTEST0005503\tVAR0005413\tMTS_TB1G2\tGSKGEGAAVLLPVLLAAPGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005504\tTEST0005504\tVAR0005414\tCtx-GS-TB1G2\tGSMCMPCFTTDHQMARKCDDCCGGKGRGKCYGPQCLCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005505\tTEST0005505\tVAR0005415\tCtx-DkTx-TB1G2\tGSMCMPCFTTDHQMARKCDDCCGGKGRGKCYGPQCLCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005506\tTEST0005506\tVAR0005416\tCtx-hIgG3-TB1G2\tGSMCMPCFTTDHQMARKCDDCCGGKGRGKCYGPQCLCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005507\tTEST0005507\tVAR0005417\tMCa-GS-TB1G2\tGSGDCLPHLKLCKENKDCCSKKCKRRGTNIEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005508\tTEST0005508\tVAR0005418\tMCa-DkTx-TB1G2\tGSGDCLPHLKLCKENKDCCSKKCKRRGTNIEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005509\tTEST0005509\tVAR0005419\tMCa-hIgG3-TB1G2\tGSGDCLPHLKLCKENKDCCSKKCKRRGTNIEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005510\tTEST0005510\tVAR0005420\tImp-GS-TB1G2\tGSGDCLPHLKRCKADNDCCGKKCKRRGTNAEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005511\tTEST0005511\tVAR0005421\tImp-DkTx-TB1G2\tGSGDCLPHLKRCKADNDCCGKKCKRRGTNAEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005512\tTEST0005512\tVAR0005422\tImp-hIgG3-TB1G2\tGSGDCLPHLKRCKADNDCCGKKCKRRGTNAEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005513\tTEST0005513\tVAR0005423\tHem-GS-TB1G2\tGSGDCLPHLKLCKADKDCCSKKCKRRGTNPEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005514\tTEST0005514\tVAR0005424\tHem-DkTx-TB1G2\tGSGDCLPHLKLCKADKDCCSKKCKRRGTNPEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005515\tTEST0005515\tVAR0005425\tHem-hIgG3-TB1G2\tGSGDCLPHLKLCKADKDCCSKKCKRRGTNPEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005516\tTEST0005516\tVAR0005426\tOpi1-GS-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGTNPEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005517\tTEST0005517\tVAR0005427\tOpi1-DkTx-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGTNPEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005518\tTEST0005518\tVAR0005428\tOpi1-hIgG3-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGTNPEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005519\tTEST0005519\tVAR0005429\tOpi2-GS-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGANPEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005520\tTEST0005520\tVAR0005430\tOpi2-DkTx-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGANPEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005521\tTEST0005521\tVAR0005431\tOpi2-hIgG3-TB1G2\tGSGDCLPHLKRCKENNDCCSKKCKRRGANPEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005522\tTEST0005522\tVAR0005432\tHad-GS-TB1G2\tGSSEKDCIKHLQRCRENKDCCSKKCSRRGTNPEKRCRGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005523\tTEST0005523\tVAR0005433\tHad-DkTx-TB1G2\tGSSEKDCIKHLQRCRENKDCCSKKCSRRGTNPEKRCRKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005524\tTEST0005524\tVAR0005434\tHad-hIgG3-TB1G2\tGSSEKDCIKHLQRCRENKDCCSKKCSRRGTNPEKRCREPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005525\tTEST0005525\tVAR0005435\tMK(62-104)-GS-TB1G2\tGSCKYKFENWGACDGGTGTKVRQGTLKKARYNAQCQETIRVTKPCGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005526\tTEST0005526\tVAR0005436\tMK(62-104)-DkTx-TB1G2\tGSCKYKFENWGACDGGTGTKVRQGTLKKARYNAQCQETIRVTKPCKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005527\tTEST0005527\tVAR0005437\tMK(62-104)-hIgG3-TB1G2\tGSCKYKFENWGACDGGTGTKVRQGTLKKARYNAQCQETIRVTKPCEPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005528\tTEST0005528\tVAR0005438\tMCOTI-GS-TB1G2\tGSSGSDGGVCPKILKKCRRDSDCPGACICRGNGYCGGGGSGGGSGGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005529\tTEST0005529\tVAR0005439\tMCOTI-DkTx-TB1G2\tGSSGSDGGVCPKILKKCRRDSDCPGACICRGNGYCGKKYKPYVPVTTNPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005530\tTEST0005530\tVAR0005440\tMCOTI-hIgG3-TB1G2\tGSSGSDGGVCPKILKKCRRDSDCPGACICRGNGYCGEPKSSDKTHTPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005531\tTEST0005531\tVAR0005441\tCTXgraft01\tGSMCMPCFSDDLFMRARCAKCCGGAGRGRCYGPQCLCR\tVCR020\t\t\t\nTEST0005532\tTEST0005532\tVAR0005442\tCTXgraft02\tGSMCMPCFSSDLFMRAKCQKCCGGAGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005533\tTEST0005533\tVAR0005443\tCTXgraft03\tGSMCMPCFLFDSQMARRCDDCCGGRGRGECRGPQCLCR\tVCR020\t\t\t\nTEST0005534\tTEST0005534\tVAR0005444\tCTXgraft04\tGSMCMPCFLFDSQMARKCDDCCGGKGRGKCDGPQCLCR\tVCR020\t\t\t\nTEST0005535\tTEST0005535\tVAR0005445\tCTXgraft05\tGSMCMPCFTTDHQMARRCDDCCLFDGFGRCYGPQCLCV\tVCR020\t\t\t\nTEST0005536\tTEST0005536\tVAR0005446\tCTXgraft06\tGSMCMPCFTTDHQMARKCDDCCLFIGWGKCYGPQCLCK\tVCR020\t\t\t\nTEST0005537\tTEST0005537\tVAR0005447\tCTXgraft07\tGSMCEPCTTLFTDEANSCDACCGGRGRGRCYGPQCLCR\tVCR020\t\t\t\nTEST0005538\tTEST0005538\tVAR0005448\tCTXgraft08\tGSMCTPCWTLFQSIADLCDACCGGKGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005539\tTEST0005539\tVAR0005449\tCTXgraft09\tGSMCMPCFTTDHQMARRCDDCCGGALFGRCYGPQCLCI\tVCR020\t\t\t\nTEST0005540\tTEST0005540\tVAR0005450\tCTXgraft10\tGSMCMPCFTTDHQMARKCDDCCGGALFGKCYGPQCLCI\tVCR020\t\t\t\nTEST0005541\tTEST0005541\tVAR0005451\tCTXgraft11\tGSMCMPCFTTDLTMQLFCEACCGGSGRGRCYGPQCLCR\tVCR020\t\t\t\nTEST0005542\tTEST0005542\tVAR0005452\tCTXgraft12\tGSMCMPCFTTDLTMQLFCEACCGGSGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005543\tTEST0005543\tVAR0005453\tCTXgraft13\tGSMCVPCYTMLQDVAYLCWWCCGGKGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005544\tTEST0005544\tVAR0005454\tCTXgraft14\tGSMCYPCFTTDHTMAMLCWQCCGGEGRGKCYGPQCLCR\tVCR020\t\t\t\nTEST0005545\tTEST0005545\tVAR0005455\tCTXgraft15\tGSMCSPCFTTDHTMAMLCQQCCGGYGRGRCYGPQCLCR\tVCR020\t\t\t\nTEST0005546\tTEST0005546\tVAR0005456\tMCOTIgraft01\tGSSGNLGGVCPKILKKCRNETDCPGACICFENGYCG\tVCR020\t\t\t\nTEST0005547\tTEST0005547\tVAR0005457\tMCOTIgraft02\tGSSGSDGGVCPKILKKCRRDSDCPGACICRLFGYCG\tVCR020\t\t\t\nTEST0005548\tTEST0005548\tVAR0005458\tMCOTIgraft03\tGSSGLFGGVCPKILKKCRTENDCPGACQCRGNGYCG\tVCR020\t\t\t\nTEST0005549\tTEST0005549\tVAR0005459\tMCOTIgraft04\tGSSGTDLFWCPKILKKCRRDSDCPGACICRGNGYCG\tVCR020\t\t\t\nTEST0005550\tTEST0005550\tVAR0005460\tMCOTIgraft05\tGSSGSDGGVCPLFIQKCRRDSDCPGACICRGNGYCG\tVCR020\t\t\t\nTEST0005551\tTEST0005551\tVAR0005461\tMCOTIgraft06\tGSSGSDGGVCPKILKECLFNSDCPGACICRGNGYCG\tVCR020\t\t\t\nTEST0005552\tTEST0005552\tVAR0005462\tMCOTIgraft07\tGSSGSDGGVCPMLFSRCRRDSDCPGACICRGNGFCG\tVCR020\t\t\t\nTEST0005553\tTEST0005553\tVAR0005463\tMCOTI\tSGSDGGVCPKILKKCRRDSDCPGACICRGNGYCG\tVCR020\t\t\t\nTEST0005554\tTEST0005554\tVAR0005464\tOpi2\tGDCLPHLKLCKENKDCCSKKCKRRGTNIEKRCR\tVCR020\t\t\t\nTEST0005555\tTEST0005555\tVAR0005465\tHem\tGDCLPHLKLCKADKDCCSKKCKRRGTNPEKRCR\tVCR020\t\t\t\nTEST0005556\tTEST0005556\tVAR0005466\tMca\tGDCLPHLKRCKENNDCCSKKCKRRGTNPEKRCR\tVCR020\t\t\t\nTEST0005557\tTEST0005557\tVAR0005467\tMK\tCKYKFENWGACDGGTGTKVRQGTLKKARYNAQCQETIRVTKPC\tVCR020\t\t\t\nTEST0005645\tTEST0005645\tVAR0005468\thLF_TB1G2\tGSKCFQWQRNMRKVRGPPVSCIKRGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\nTEST0005647\tTEST0005647\tVAR0005469\tPFVYLI_TB1G2\tGSPFVYLIGGSPDEYIERAKECCKKQDIQCCLRIFDESKDPNVMLICLFCW\tVCR020\t\t\t\n')

WebUI.click(findTestObject('Page_Import Sample Set OptidesCompo/span_Submit'))

WebUI.mouseOver(findTestObject('Page_Start Page Optides/a_CompoundsRegistry'))

WebUI.closeBrowser()
