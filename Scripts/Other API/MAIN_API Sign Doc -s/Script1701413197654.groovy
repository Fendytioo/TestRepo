import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - API Only.xlsx')

'Pembuatan pengisian variable di sendRequest per jumlah documentId.'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= findTestData(excelPath).columnNumbers; (GlobalVariable.NumofColm)++) {
	if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
		break
	} else if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
	
		'setting menggunakan base url yang benar atau salah'
		CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPath, GlobalVariable.NumofColm, rowExcel('Use Correct Base Url'))

		'inisialisasi arrayList'
		ArrayList documentId = [], list = [], listDocId = [], emailsString = []
	
		'Mengambil document id dari excel dan displit'
		documentId = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('$documentId')).split(';', -1)
		
		'Mengambil document id dari excel dan displit'
		emailsString = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('$email')).split(';', -1)
	
		for (int q = 0; q < documentId.size(); q++) {
			list.add('"' + documentId.get(q) + '"');
			
			if (q == 0) {
				listDocId.add(list.get(q));
			} else {
				listDocId.set(0, listDocId.get(0) + "," + list.get(q));
			}
		}
		
		'ubah menjadi string'
		String listDoc = listDocId.toString().replace('[','').replace(']','')
		
		'buat list sementara untuk ubah format email'
		List<String> formattedEmails = new ArrayList<>();
		
		'buat jadi list multi email untuk hit API'
		for (int q = 0; q < emailsString.size(); q++) {
			formattedEmails.add('"' + emailsString[q] + '"');
		}
		
		'join kembali hasilnya untuk dikirim ke param API'
		String listEmail = String.join(",", formattedEmails)
		
		'HIT API Login untuk ambil bearer token'
        respon_login = WS.sendRequest(findTestObject('Postman/Login', [('username') : findTestData(excelPath).getValue(GlobalVariable.NumofColm, 
                        rowExcel('username')), ('password') : findTestData(excelPath).getValue(GlobalVariable.NumofColm, 
                        rowExcel('password'))]))

        'Jika status HIT API Login 200 OK'
        if (WS.verifyResponseStatusCode(respon_login, 200, FailureHandling.OPTIONAL) == true) {
            'Parsing token menjadi GlobalVariable'
            GlobalVariable.token = WS.getElementPropertyValue(respon_login, 'access_token')
	
			'HIT API Sign Document'
			respon_signdoc = WS.sendRequest(findTestObject('Postman/Sign Document -s', [
				('callerId') : ('"' + findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('callerId'))) + '"',
				('email') : listEmail,
				('documentId') : listDoc,
				('msg') : ('"' + findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('msg'))) + '"']))
	
			'ambil lama waktu yang diperlukan hingga request menerima balikan'
			def elapsedTime = (respon_signdoc.getElapsedTime() / 1000) + ' second'

			'ambil body dari hasil respons'
			responseBody = respon_signdoc.getResponseBodyContent()

			'panggil keyword untuk proses beautify dari respon json yang didapat'
			CustomKeywords.'customizekeyword.BeautifyJson.process'(responseBody, sheet, rowExcel('Respons') - 1, findTestData(
					excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Scenario')))

			'write to excel response elapsed time'
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Process Time') -
				1, GlobalVariable.NumofColm - 1, elapsedTime.toString())
			
			'Jika status HIT API 200 OK'
			if (WS.verifyResponseStatusCode(respon_signdoc, 200, FailureHandling.OPTIONAL) == true) {
				'get Status Code'
				status_Code = WS.getElementPropertyValue(respon_signdoc, 'status.code')
	
				'Jika status codenya 0'
				if (status_Code == 0) {
					'get vendor Code'
					GlobalVariable.Response = WS.getElementPropertyValue(respon_signdoc, 'vendorCode')
	
					'write to excel success'
					CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet,
						0, GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
				} else {
					getErrorMessageAPI(respon_signdoc)
				}
			} else {
				getErrorMessageAPI(respon_signdoc)
			}
		} else {
			getErrorMessageAPI(respon_login)
		}
	}
}

def rowExcel(String cellValue) {
	return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def getErrorMessageAPI(def respon) {
	'mengambil status code berdasarkan response HIT API'
	message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

	'Write To Excel GlobalVariable.StatusFailed and errormessage'
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
		((findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + ('<' + message.toString())) +
		'>')

	GlobalVariable.FlagFailed = 1
}