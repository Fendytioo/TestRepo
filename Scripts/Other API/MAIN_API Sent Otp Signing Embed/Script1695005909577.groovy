import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import java.sql.Connection as Connection
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import org.openqa.selenium.By as By

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(API_Excel_Path).columnNumbers

for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break
    } else if (findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
		
		GlobalVariable.FlagFailed = 0
		
		'setting menggunakan base url yang benar atau salah'
		CustomKeywords.'connection.APIFullService.settingBaseUrl'(API_Excel_Path, GlobalVariable.NumofColm, rowExcel('Use Correct Base Url'))

		'Mengambil aes key based on tenant tersebut'
		String aesKey = CustomKeywords.'connection.APIFullService.getAesKeyBasedOnTenant'(conneSign, findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('tenantCode')).replace('"',''))
		
		if (aesKey != null) {
			'enkripsi msg'
			encryptMsg = CustomKeywords.'customizekeyword.ParseText.parseEncrypt'(findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('msg')), aesKey)
		}
		else {
			encryptMsg = ''
		}
		
		println aesKey
		println encryptMsg

		'HIT API'
        respon = WS.sendRequest(findTestObject('Postman/Sent Otp Signing Embed', [('msg') : '"' + encryptMsg + '"', 
			('tenantCode') : findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('tenantCode')), 
			('phoneNo') : findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('phoneNo')),
			('vendorCode') : findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('vendorCode')), 
			('callerId') : findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('callerId'))]))

		'ambil lama waktu yang diperlukan hingga request menerima balikan'
		def elapsedTime = (respon.getElapsedTime()) / 1000 + ' second'
		
		'ambil body dari hasil respons'
		responseBody = respon.getResponseBodyContent()
		
		'panggil keyword untuk proses beautify dari respon json yang didapat'
		CustomKeywords.'customizekeyword.BeautifyJson.process'(responseBody, sheet, rowExcel('Respons') - 1,
			findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('Scenario')))
		
		'write to excel response elapsed time'
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Process Time') - 1, GlobalVariable.NumofColm -
			1, elapsedTime.toString())
		
		'Jika status HIT API 200 OK'
		if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
			'get Status Code'
			status_Code = WS.getElementPropertyValue(respon, 'status.code')

			'Jika status codenya 0'
			if (status_Code == 0) {	
				message = WS.getElementPropertyValue(respon, 'otpByEmail', FailureHandling.OPTIONAL)
				
				'write otpby email'
				CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('otpByEmail') - 1,
					GlobalVariable.NumofColm - 1, message)
				if (GlobalVariable.FlagFailed == 0) {
					'write to excel success'
					CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Status') - 1,
						GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
				}
			} else {
				getErrorMessageAPI(respon)
				}
			} else {
				getErrorMessageAPI(respon)
		}
    }
}

def getErrorMessageAPI(def respon) {
    'mengambil status code berdasarkan response HIT API'
    message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

    'Write To Excel GlobalVariable.StatusFailed and errormessage'
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
        ((findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace('-','') + ';') + 
        ('<' + message)) + '>')

    GlobalVariable.FlagFailed = 1
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def encryptLink(Connection conneSign, String documentId, String emailSigner, String aesKey) {
    officeCode = CustomKeywords.'connection.DataVerif.getOfficeCode'(conneSign, documentId)

    'pembuatan message yang akan dienkrip'
    msg = (((((('{"tenantCode":"' + findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))) + 
    '","officeCode":"') + officeCode) + '","email":"') + emailSigner) + '"}')

    'enkripsi msg'
    encryptMsg = CustomKeywords.'customizekeyword.ParseText.parseEncrypt'(msg, aesKey)

    println(msg)

    return encryptMsg
}