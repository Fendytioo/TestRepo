import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import java.sql.Connection as Connection
import internal.GlobalVariable as GlobalVariable

'declare invitation link inquiry'
String invitationLinkInquiry = ''

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'get colm excel'
int countColmExcel = findTestData(excelPathGenerateLink).columnNumbers

for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break
    } else if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase(
        'Unexecuted')) {
	
		'get psre from excel percase'
		GlobalVariable.Psre = findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Psre Login'))
		
		'get Tenant from excel percase'
		GlobalVariable.Tenant = findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
	
        'setting menggunakan base url yang benar atau salah'
        CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPathGenerateLink, GlobalVariable.NumofColm, rowExcel(
                'Use Correct base Url'))
		
        'check ada value maka setting email service tenant'
        if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Service')).length() > 
        0) {
            'setting email service tenant'
            CustomKeywords.'connection.Registrasi.settingEmailServiceTenant'(conneSign, findTestData(excelPathGenerateLink).getValue(
                    GlobalVariable.NumofColm, rowExcel('Setting Email Service')))
        }
        
        'check ada value maka setting allow regenerate link'
        if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Setting Allow Regenarate Link')).length() > 
        0) {
            'setting allow regenerate link'
            CustomKeywords.'connection.APIFullService.settingAllowRegenerateLink'(conneSign, findTestData(excelPathGenerateLink).getValue(
                    GlobalVariable.NumofColm, rowExcel('Setting Allow Regenarate Link')))
        }
		
		'check if tidak mau menggunakan tenant code yang benar'
		if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct Tenant Code')) == 'No') {
			'set tenant kosong'
			GlobalVariable.Tenant = findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Wrong Tenant Code'))
		} else if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct Tenant Code')) == 'Yes') {
			GlobalVariable.Tenant = findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
		}
		
		'check if mau menggunakan api_key yang salah atau benar'
		if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct API Key')) == 'Yes') {
			'get api key dari db'
			GlobalVariable.api_key = CustomKeywords.'connection.APIFullService.getTenantAPIKey'(conneSign, GlobalVariable.Tenant)
		} else if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct API Key')) == 'No') {
			'get api key salah dari excel'
			GlobalVariable.api_key = findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Wrong API Key'))
		}
        
        'Pembuatan pengisian variable di sendRequest per column berdasarkan data excel.'
        ArrayList listInvitation = []

        'Declare variable untuk sendRequest'
        (listInvitation[0]) = (((((((((((((((((((((((((('{"email" :' + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('email'))) + ',"nama" :') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('nama'))) + ',"tlp": ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel(
                'tlp'))) + ',"jenisKelamin" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('jenisKelamin'))) + ',"tmpLahir" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('tmpLahir'))) + ',"tglLahir" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('tglLahir'))) + ',"idKtp" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('idKtp'))) + ', "provinsi" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('provinsi'))) + ', "kota" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('kota'))) + ', "kecamatan" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('kecamatan'))) + ',"kelurahan": ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('kelurahan'))) + ',"kodePos" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('kodePos'))) + ',"alamat" : ') + findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
            rowExcel('alamat'))) + '}  ')

        'HIT API'
        respon = WS.sendRequest(findTestObject('Postman/Gen Invitation Link', [('callerId') : findTestData(excelPathGenerateLink).getValue(
                        GlobalVariable.NumofColm, rowExcel('callerId')), ('tenantCode') : findTestData(excelPathGenerateLink).getValue(
                        GlobalVariable.NumofColm, rowExcel('$tenantCode')), ('users') : listInvitation[0]]))

        'Jika status HIT API 200 OK'
        if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
            'mengambil status code berdasarkan response HIT API'
            status_Code = WS.getElementPropertyValue(respon, 'status.code', FailureHandling.OPTIONAL)

            'jika status codenya 0'
            if (status_Code == 0) {
                'Mengambil links berdasarkan response HIT API'
                links = WS.getElementPropertyValue(respon, 'links', FailureHandling.OPTIONAL)

                'check ada value maka setting Link Is Active'
                if (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Setting Allow Regenarate Link')) == 
                '0') {
                    'setting Link Is Active'
                    CustomKeywords.'connection.APIFullService.settingLinkIsActive'(conneSign, findTestData(excelPathGenerateLink).getValue(
                            GlobalVariable.NumofColm, rowExcel('Setting Allow Regenarate Link')), findTestData(excelPathGenerateLink).getValue(
                            GlobalVariable.NumofColm, rowExcel('email')).replace('"', ''))

                    'HIT API'
                    respon = WS.sendRequest(findTestObject('Postman/Gen Invitation Link', [('callerId') : findTestData(excelPathGenerateLink).getValue(
                                    GlobalVariable.NumofColm, rowExcel('callerId')), ('tenantCode') : findTestData(excelPathGenerateLink).getValue(
                                    GlobalVariable.NumofColm, rowExcel('$tenantCode')), ('users') : listInvitation[0]]))

                    'Jika status HIT API 200 OK'
                    if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
                        'get status code'
                        code = WS.getElementPropertyValue(respon, 'status.code', FailureHandling.OPTIONAL)

                        if (code == 0) {
                            'Mengambil links berdasarkan response HIT API'
                            links = WS.getElementPropertyValue(respon, 'links', FailureHandling.OPTIONAL)

                            'write to excel failed'
                            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                GlobalVariable.StatusFailed, (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, 
                                    rowExcel('Reason Failed')).replace('-', '') + ';') + ' Link tergenerate walupun sudah tidak active')
                        } else {
                            'call function get API error message'
                            getAPIErrorMessage(respon)
                        }
                    } else {
                        'call function get API error message'
                        getAPIErrorMessage(respon)
                    }
                    
                    continue
                }
                
            	'write to excel success'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 0, GlobalVariable.NumofColm - 
                    1, GlobalVariable.StatusSuccess)

                if ((GlobalVariable.checkStoreDB == 'Yes') && (GlobalVariable.FlagFailed == 0)) {
                    'call test case ResponseAPIStoreDB'
                    WebUI.callTestCase(findTestCase('Generate Invitation Link/ResponseAPIStoreDB'), [('excelPathGenerateLink') : 'Registrasi/Generate_Inv_Link'], 
                        FailureHandling.CONTINUE_ON_FAILURE)
                }
            } else {
                'call function get API error message'
                getAPIErrorMessage(respon)
            }
        } else {
            'write to excel status failed dan reason : '
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                (findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
                    '-', '') + ';') + GlobalVariable.ReasonFailedHitAPI)
        }
    }
}

def getAPIErrorMessage(def respon) {
    'jika status codenya bukan 0, yang berarti antara salah verifikasi data dan error'
    messageFailed = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

    'write to excel status failed dan reason : '
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
        (((findTestData(excelPathGenerateLink).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace('-', 
            '') + ';') + '<') + messageFailed) + '>')
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}