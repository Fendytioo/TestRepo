import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection as Connection
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - Full API Services.xlsx')

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathAPICheckRegistrasi).columnNumbers

'looping API Registrasi'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {
		
		'setting menggunakan base url yang benar atau salah'
		CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPathAPICheckRegistrasi, GlobalVariable.NumofColm, 16)
		
        'check if mau menggunakan api_key yang salah atau benar'
        if (findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 14) == 'Yes') {
            'get api key dari db'
            GlobalVariable.api_key = CustomKeywords.'connection.APIFullService.getTenantAPIKey'(conneSign, GlobalVariable.Tenant)
        } else if (findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 14) == 'No') {
            'get api key salah dari excel'
            GlobalVariable.api_key = findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 15)
        }
        
        'HIT API check registrasi'
        respon = WS.sendRequest(findTestObject('APIFullService/Postman/Check Registration', [('callerId') : findTestData(
                        excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 9), ('dataType') : findTestData(
                        excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 11), ('dataValue') : findTestData(
                        excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 12)]))

        'Jika status HIT API 200 OK'
        if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
            code = WS.getElementPropertyValue(respon, 'status.code', FailureHandling.OPTIONAL)

            if (code == 0) {
                'mengambil response'
                vendor = WS.getElementPropertyValue(respon, 'registrationData.vendor', FailureHandling.OPTIONAL)

                vendoractive = WS.getElementPropertyValue(respon, 'registrationData.registrationStatus', FailureHandling.OPTIONAL)

                if (GlobalVariable.checkStoreDB == 'Yes') {
                    arrayIndex = 0

                    'get data from db'
                    ArrayList<String> result = CustomKeywords.'connection.APIFullService.getAPICheckRegisterStoreDB'(conneSign, 
                        findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 12).replace('"', ''))

					println(result)
					
                    'declare arraylist arraymatch'
                    ArrayList<String> arrayMatch = []

                    for (index = 0; index < vendor.size(); index++) {
						
						arrayIndex = result.indexOf(vendor[index])
						
						vendorDB = result[arrayIndex++]
						
						vendorStatusDB = result[arrayIndex++]
						
                        if ((vendor[index]) == vendorDB) {
                            'verify vendor'
                            arrayMatch.add(WebUI.verifyMatch(vendorDB.toUpperCase(), (vendor[index]).toUpperCase(), 
                                    false, FailureHandling.CONTINUE_ON_FAILURE))

                            'verify vendor status'
                            arrayMatch.add(WebUI.verifyMatch(vendorStatusDB.toUpperCase(), (vendoractive[index]).toUpperCase(), 
                                    false, FailureHandling.CONTINUE_ON_FAILURE))
                        }
                    }
                    
                    'jika data db tidak sesuai dengan excel'
                    if (arrayMatch.contains(false)) {
                        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('API Check Registrasi', GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPathAPICheckRegistrasi).getValue(GlobalVariable.NumofColm, 
                                2) + ';') + GlobalVariable.ReasonFailedStoredDB)
                    } else {
                        'write to excel success'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, 'API Check Registrasi', 
                            0, GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
                    }
                }
            } else {
                'mengambil status code berdasarkan response HIT API'
                message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

                'Write To Excel GlobalVariable.StatusFailed and errormessage'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('API Check Registrasi', GlobalVariable.NumofColm, 
                    GlobalVariable.StatusFailed, message)
            }
        } else {
            'mengambil status code berdasarkan response HIT API'
            message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

            'Write To Excel GlobalVariable.StatusFailed and errormessage'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('API Check Registrasi', GlobalVariable.NumofColm, 
                GlobalVariable.StatusFailed, message)
        }
    }
}

