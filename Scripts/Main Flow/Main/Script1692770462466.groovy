import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import org.openqa.selenium.By as By
import java.sql.Connection as Connection

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

sheet = 'Main'

'looping untuk sending document'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= findTestData(excelPathMain).columnNumbers; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {
		
		resetValue()
		
		GlobalVariable.Tenant = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Tenant'))
		
		ArrayList signerInput = []
		
        if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 'API Send Document External') {
			WebUI.callTestCase(findTestCase('Main Flow/API Send Document External'), [('excelPathAPISendDoc') : excelPathMain
                    , ('sheet') : sheet], FailureHandling.CONTINUE_ON_FAILURE)
			signerInput = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('$signerType (Send External)')).split(';', -1)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 'API Send Document Normal') {
            WebUI.callTestCase(findTestCase('Main Flow/API Send Document Normal'), [('API_Excel_Path') : excelPathMain, ('sheet') : sheet], 
                FailureHandling.CONTINUE_ON_FAILURE)
			signerInput = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('$signerType (Send Normal)')).split(';', -1)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 'Manual Sign') {
            WebUI.callTestCase(findTestCase('Main Flow/Manual Sign'), [('excelPathManualSigntoSign') : excelPathMain, ('sheet') : sheet], 
                FailureHandling.CONTINUE_ON_FAILURE)
			signerInput = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('jumlah signer lokasi per signer (Send Manual)')).split(';', -1)
        }
		
		if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).length() > 0) {
			WebUI.callTestCase(findTestCase('Main Flow/KotakMasuk'), [('excelPathFESignDocument') : excelPathMain, ('sheet') : sheet]
				, FailureHandling.STOP_ON_FAILURE)
			
			if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Need Sign for this document? ')) == 'Yes') {
				
				ArrayList opsiSigning = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Sign Document per Signer')).split(';', -1)
				
				documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid'))
				
				'Mengambil email signer berdasarkan documentId'
				ArrayList emailSigner = CustomKeywords.'connection.SendSign.getEmailLogin'(conneSign, documentId).split(';', -1)
			
				if (WebUI.verifyNotEqual(signerInput.size(), emailSigner.size(), FailureHandling.OPTIONAL)) {
					'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
					CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
						(findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 2) + ';') + ' total signer pada Send Document dengan signer yang terdaftar tidak sesuai ')
				}
				
				for (int i = 0; i <= emailSigner.size(); i++) {
					if (opsiSigning[i] == 'API Sign Document External') {
						indexReadDataExcelAPIExternal = inisializeArray(isUsedAPIExternal, indexReadDataExcelAPIExternal)
					
						WebUI.callTestCase(findTestCase('Main Flow/API Sign Document External'), [('excelPathAPISignDocument') : excelPathMain
							, ('sheet') : sheet, ('indexUsed') : indexReadDataExcelAPIExternal], FailureHandling.CONTINUE_ON_FAILURE)
						
						isUsedAPIExternal = true
					} else if (opsiSigning[i] == 'API Sign Document Normal') {
						indexReadDataExcelAPINormal = inisializeArray(isUsedAPINormal, indexReadDataExcelAPINormal)
					
						WebUI.callTestCase(findTestCase('Main Flow/API Sign Document Normal'), [('API_Excel_Path') : excelPathMain
							, ('sheet') : sheet, ('indexUsed') : indexReadDataExcelAPINormal], FailureHandling.CONTINUE_ON_FAILURE)
						
						isUsedAPINormal = true
					}
				}
			}
		}
    }
}

def inisializeArray(boolean isUsed, int indexReadDataExcel) {
	if (isUsed == false) {
		return indexReadDataExcel
	} else {
		return indexReadDataExcel + 1
	}
}

def rowExcel(String cellValue) {
	return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, 'Main', cellValue)
}

def resetValue() {
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('documentid') - 1, GlobalVariable.NumofColm -
		1, '')
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('trxNo') - 1, GlobalVariable.NumofColm -
		1, '')
	
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('trxNos') - 1, GlobalVariable.NumofColm -
		1, '')
	
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Success') - 1, GlobalVariable.NumofColm -
		1, '')
	
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Failed') - 1, GlobalVariable.NumofColm -
		1, '')
}
