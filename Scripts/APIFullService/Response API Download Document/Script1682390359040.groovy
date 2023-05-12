import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection as Connection
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import internal.GlobalVariable as GlobalVariable

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizeKeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - Full API Services.xlsx')

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathAPIDownload).columnNumbers

'looping API Download Document'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {
        'check if tidak mau menggunakan tenant code yang benar'
        if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 15) == 'No') {
            'set tenant kosong'
            GlobalVariable.Tenant = findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 16)
        } else if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 15) == 'Yes') {
            GlobalVariable.Tenant = findTestData(excelPathSetting).getValue(6, 2)
        }
        
        'check if mau menggunakan api_key yang salah atau benar'
        if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 13) == 'Yes') {
            'get api key dari db'
            GlobalVariable.api_key = CustomKeywords.'connection.DataVerif.getTenantAPIKey'(conneSign, GlobalVariable.Tenant)
        } else if (findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 13) == 'No') {
            'get api key salah dari excel'
            GlobalVariable.api_key = findTestData(excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 14)
        }
        
        'HIT API check Download Document'
        respon = WS.sendRequest(findTestObject('APIFullService/Postman/Download Document', [('callerId') : findTestData(
                        excelPathAPIDownload).getValue(GlobalVariable.NumofColm, 9), ('documentId') : findTestData(excelPathAPIDownload).getValue(
                        GlobalVariable.NumofColm, 11)]))

        'Jika status HIT API 200 OK'
        if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
            code = WS.getElementPropertyValue(respon, 'status.code', FailureHandling.OPTIONAL)

            if (code == 0) {
                'mengambil response'
                base64PDF = WS.getElementPropertyValue(respon, 'pdfBase64', FailureHandling.OPTIONAL)

                'decode Bas64 to File PDF'
                CustomKeywords.'customizeKeyword.ConvertFile.decodeBase64'(base64PDF, findTestData(excelPathAPIDownload).getValue(
                        GlobalVariable.NumofColm, 18))

                'check is file downloaded dan apakah mau di delete'
                if (CustomKeywords.'customizeKeyword.Download.isFileDownloaded'(findTestData(excelPathAPIDownload).getValue(
                        GlobalVariable.NumofColm, 17)) == true) {
                    'write to excel success'
                    CustomKeywords.'customizeKeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, 'API Download Document', 
                        0, GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
                }
            } else {
                'mengambil status code berdasarkan response HIT API'
                message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

                'Write To Excel GlobalVariable.StatusFailed and errormessage'
                CustomKeywords.'customizeKeyword.WriteExcel.writeToExcelStatusReason'('API Download Document', GlobalVariable.NumofColm, 
                    GlobalVariable.StatusFailed, message)
            }
        } else {
            'mengambil status code berdasarkan response HIT API'
            message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

            'Write To Excel GlobalVariable.StatusFailed and errormessage'
            CustomKeywords.'customizeKeyword.WriteExcel.writeToExcelStatusReason'('API Download Document', GlobalVariable.NumofColm, 
                GlobalVariable.StatusFailed, message)
        }
    }
}

