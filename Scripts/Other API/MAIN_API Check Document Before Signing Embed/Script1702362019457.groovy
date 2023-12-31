import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import internal.GlobalVariable as GlobalVariable
import java.nio.charset.StandardCharsets as StandardCharsets
import java.sql.Connection as Connection
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - API Only.xlsx')

'Connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPath).columnNumbers

'looping API Confirm OTP'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break
    } else if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
        'inisialisasi arrayList'
        ArrayList documentId = [], listDocId = []

        'setting menggunakan base url yang benar atau salah'
        CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPath, GlobalVariable.NumofColm, rowExcel('Use Correct Base Url'))

        'check if tidak mau menggunakan tenant code yang benar'
        if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('use Correct Tenant Code')) == 'No') {
            'set tenant kosong'
            GlobalVariable.Tenant = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Wrong tenant Code'))
        } else if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('use Correct Tenant Code')) == 'Yes') {
            'get tenant per case dari colm excel'
            GlobalVariable.Tenant = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
        }
        
        'get aesKet Tenant'
        aesKey = CustomKeywords.'connection.APIFullService.getAesKeyBasedOnTenant'(conneSign, GlobalVariable.Tenant)

        def currentDate = new Date()

        def dateFormat = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')

        def timestamp = dateFormat.format(currentDate)

        if (aesKey.toString() != 'null') {
            'Mengambil document id dari excel dan displit'
            documentId = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Document ID')).split(';', 
                -1)

            'get office code dari db'
            officeCode = CustomKeywords.'connection.DataVerif.getOfficeCode'(conneSign, documentId[0])

            'pembuatan message yang akan dienkrip'
            msg = (((((('{\'officeCode\':\'' + officeCode) + '\',\'email\':\'') + findTestData(excelPath).getValue(GlobalVariable.NumofColm, 
                rowExcel('email'))) + '\',\'timestamp\':\'') + timestamp) + '\'}')

            if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct Msg')) == 'No') {
                'officecode + email + time stamp tanpa encrypt'
                endcodedMsg = msg
            } else if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct Msg')) == 'Yes') {
                'encrypt and decode officecode + email + time stamp'
                endcodedMsg = encryptEncodeValue(msg, aesKey)
            }
            
            for (int q = 0; q < documentId.size(); q++) {
                if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct DocumentID')) == 'No') {
                    encryptDocID = documentId
                } else if (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct DocumentID')) == 
                'Yes') {
                    encryptDocID = encryptEncodeValue((documentId[q]).toString(), aesKey)
                }
                
                listDocId.add(('"' + encryptDocID) + '"')
            }
        } else {
            endcodedMsg = ''
        }
        
        'ubah menjadi string'
        String listDoc = listDocId.toString().replace('[', '').replace(']', '')

        'HIT API'
        respon = WS.sendRequest(findTestObject('Postman/Check Document Before Signing Embed', [('docId') : parseCodeOnly(listDoc), ('msg') : parseCodeOnly(endcodedMsg)]))

        'ambil lama waktu yang diperlukan hingga request menerima balikan'
        def elapsedTime = (respon.getElapsedTime() / 1000) + ' second'

        'ambil body dari hasil respons'
        responseBody = respon.getResponseBodyContent()

        'panggil keyword untuk proses beautify dari respon json yang didapat'
        CustomKeywords.'customizekeyword.BeautifyJson.process'(responseBody, sheet, rowExcel('Respons') - 1, findTestData(
                excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Scenario')))

        'write to excel response elapsed time'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Process Time') - 
            1, GlobalVariable.NumofColm - 1, elapsedTime.toString())

        if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
            'get Status Code'
            status_Code = WS.getElementPropertyValue(respon, 'status.code')

            'Jika status codenya 0'
            if (status_Code == 0) {
                'get  doc id'
                docId = WS.getElementPropertyValue(respon, 'listCheckDocumentBeforeSigning.documentId', FailureHandling.OPTIONAL)

                'get  signing Process'
                signingProcess = WS.getElementPropertyValue(respon, 'listCheckDocumentBeforeSigning.signingProcess', FailureHandling.OPTIONAL)

                if (GlobalVariable.checkStoreDB == 'Yes') {
                    'declare arraylist arraymatch'
                    ArrayList arrayMatch = []

                    for (index = 0; index < docId.size(); index++) {
						
						println(signingProcess)
						
						'decrypt docid'
						decryptedDocId =  CustomKeywords.'customizekeyword.ParseText.parseDecrypt'(docId[index], aesKey)
						
                        'get data from DB'
                        ArrayList resultDB = CustomKeywords.'connection.APIFullService.getDocSignSequence'(conneSign, decryptedDocId, 
							findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('email')).replace(
                                '"', ''))
						
                        arrayIndex = 0

                        'verify doc ID'
                        arrayMatch.add(WebUI.verifyMatch(decryptedDocId, resultDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify status'
                        arrayMatch.add(WebUI.verifyMatch(signingProcess[index], resultDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))
                    }
                    
                    'jika data db tidak sesuai dengan excel'
                    if (arrayMatch.contains(false)) {
                        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel(
                                    'Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB)
                    } else {
                        'write to excel success'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 0, 
                            GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
                    }
                }
            } else {
                getErrorMessageAPI(respon)
            }
        } else {
            'call function get error message API'
            getErrorMessageAPI(respon)
        }
    }
}

def getErrorMessageAPI(def respon) {
    'mengambil status code berdasarkan response HIT API'
    message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

    'Write To Excel GlobalVariable.StatusFailed and errormessage'
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
        ('<' + message) + '>')
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def encryptEncodeValue(String value, String aesKey) {
    'enkripsi msg'
    encryptMsg = CustomKeywords.'customizekeyword.ParseText.parseEncrypt'(value, aesKey)

    println(encryptMsg)

    try {
        return URLEncoder.encode(encryptMsg, StandardCharsets.UTF_8.toString())
    }
    catch (UnsupportedEncodingException ex) {
        throw new RuntimeException(ex.getCause())
    } 
}

def parseCodeOnly(String url) {
	
		'ambil data sesudah "code="'
		Pattern pattern = Pattern.compile("([^&]+)")
		
		'ambil matcher dengan URL'
		Matcher matcher = pattern.matcher(url)
		
		'cek apakah apttern nya sesuai'
		if (matcher.find()) {
			'ubah jadi string'
			String code = matcher.group(1)
			
			'decode semua ascii pada url'
			code = URLDecoder.decode(code, "UTF-8")
			
			return code
		} else {
			
			return ''
		}
	}