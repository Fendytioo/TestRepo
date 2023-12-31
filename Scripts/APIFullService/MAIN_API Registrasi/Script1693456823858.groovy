import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection as Connection
import org.openqa.selenium.By as By
import org.openqa.selenium.Keys as Keys
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - Full API Services.xlsx')

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathAPIRegistrasi).columnNumbers

String selfPhoto, idPhoto

int countCheckSaldo

'looping API Registrasi'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break
    } else if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
        ArrayList<String> saldoBefore = [], saldoAfter = []
		
		GlobalVariable.FlagFailed = 0

		'get Psre per case'
		GlobalVariable.Psre = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Psre Login'))
		
        'setting menggunakan base url yang benar atau salah'
        CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPathAPIRegistrasi, GlobalVariable.NumofColm, rowExcel('Use Correct Base Url'))

        'check ada value maka setting need password for signing'
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Flag Need Password')).length() > 0) {
            'setting need password for signing'
            CustomKeywords.'connection.APIFullService.settingFlagNeedPassword'(conneSign, findTestData(excelPathAPIRegistrasi).getValue(
                    GlobalVariable.NumofColm, rowExcel('Setting Flag Need Password')))
        }
        
        'check ada value maka setting email service tenant'
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Service')).length() > 0) {
            'setting email service tenant'
            CustomKeywords.'connection.APIFullService.settingEmailServiceTenant'(conneSign, findTestData(excelPathAPIRegistrasi).getValue(
                    GlobalVariable.NumofColm, rowExcel('Setting Email Service')))
        }
		
		'check ada value maka Setting Email Certif Notif'
		if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Certif Notif')).length() > 0) {
			'Setting Email Certif Notif'
			CustomKeywords.'connection.Registrasi.settingSendCertNotifbySMS'(conneSign, findTestData(excelPathAPIRegistrasi).getValue(
					GlobalVariable.NumofColm, rowExcel('Setting Email Certif Notif')))
		}
        
        'check ada value maka setting register as dukcapil check'
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting as Dukcapil Check')).length() > 0) {
            'setting register as dukcapil check'
            CustomKeywords.'connection.APIFullService.settingRegisterasDukcapilCheck'(conneSign, findTestData(excelPathAPIRegistrasi).getValue(
                    GlobalVariable.NumofColm, rowExcel('Setting as Dukcapil Check')))
        }
        
        'check if tidak mau menggunakan tenant code yang benar'
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('use Correct Tenant Code')) == 'No') {
            'set tenant kosong'
            GlobalVariable.Tenant = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Wrong tenant Code'))
        } else if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('use Correct Tenant Code')) == 'Yes') {
            GlobalVariable.Tenant = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
        }
        
        'check if mau menggunakan api_key yang salah atau benar'
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('use Correct API Key')) == 'Yes') {
            'get api key dari db'
            GlobalVariable.api_key = CustomKeywords.'connection.APIFullService.getTenantAPIKey'(conneSign, GlobalVariable.Tenant)
        } else if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('use Correct API Key')) == 'No') {
            'get api key salah dari excel'
            GlobalVariable.api_key = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Wrong API Key'))
        }
        
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('enter Correct base64 SelfPhoto')) == 'Yes') {
            selfPhoto = CustomKeywords.'customizekeyword.ConvertFile.base64File'(findTestData(excelPathAPIRegistrasi).getValue(
                    GlobalVariable.NumofColm, rowExcel('selfPhoto')))
        } else if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('enter Correct base64 SelfPhoto')) == 'No') {
            selfPhoto = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('selfPhoto'))
        }
        
        if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('enter Correct base64 IdPhoto')) == 'Yes') {
            idPhoto = CustomKeywords.'customizekeyword.ConvertFile.base64File'(findTestData(excelPathAPIRegistrasi).getValue(
                    GlobalVariable.NumofColm, rowExcel('idPhoto')))
        } else if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('enter Correct base64 IdPhoto')) == 'No') {
            idPhoto = findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('idPhoto'))
        }
        
        if (GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI') {
            countCheckSaldo = 0

            saldoBefore = loginAdminGetSaldo(countCheckSaldo, conneSign)

            countCheckSaldo = 1

            println(saldoBefore)
        }
        
        'HIT API'
        respon = WS.sendRequest(findTestObject('APIFullService/Postman/Register', [('callerId') : findTestData(excelPathAPIRegistrasi).getValue(
                        GlobalVariable.NumofColm, rowExcel('callerId')), ('psreCode') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                        rowExcel('psreInput')), ('nama') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                        rowExcel('nama')), ('email') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('email')), ('tmpLahir') : findTestData(
                        excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('tmpLahir')), ('tglLahir') : findTestData(excelPathAPIRegistrasi).getValue(
                        GlobalVariable.NumofColm, rowExcel('tglLahir')), ('jenisKelamin') : findTestData(excelPathAPIRegistrasi).getValue(
                        GlobalVariable.NumofColm, rowExcel('jenisKelamin')), ('tlp') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                        rowExcel('tlp')), ('idKtp') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('idKtp')), ('alamat') : findTestData(
                        excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('alamat')), ('kecamatan') : findTestData(excelPathAPIRegistrasi).getValue(
                        GlobalVariable.NumofColm, rowExcel('kecamatan')), ('kelurahan') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                        rowExcel('kelurahan')), ('kota') : findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('kota')), ('provinsi') : findTestData(
                        excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('provinsi')), ('kodePos') : findTestData(excelPathAPIRegistrasi).getValue(
                        GlobalVariable.NumofColm, rowExcel('kodePos')), ('selfPhoto') : selfPhoto, ('idPhoto') : idPhoto, ('password') : findTestData(
                        excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('password'))]))

        'Jika status HIT API 200 OK'
        if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
            'get status code'
            code = WS.getElementPropertyValue(respon, 'status.code', FailureHandling.OPTIONAL)

            'declare arraylist arraymatch'
            arrayMatch = []

            if (code == 0) {
                'mengambil response'
                trxNo = WS.getElementPropertyValue(respon, 'trxNo', FailureHandling.OPTIONAL)

                email = WS.getElementPropertyValue(respon, 'email', FailureHandling.OPTIONAL)

				psreCode = WS.getElementPropertyValue(respon, 'psreCode', FailureHandling.OPTIONAL)
				
                println(trxNo)

                if (GlobalVariable.checkStoreDB == 'Yes') {
					
					'get psre Registered'
					String resultVendorRegistered = CustomKeywords.'connection.APIFullService.getRegisteredVendor'(conneSign,
						findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('email')))
					
					ArrayList<String> resultTrx = CustomKeywords.'connection.APIFullService.getAPIRegisterTrx'(conneSign,
						trxNo[0], trxNo[1])
					
					'reset index kembali 0 untuk array selanjutnya'
					arrayIndex = 0

					'verify trx Verification qty = -1'
					arrayMatch.add(WebUI.verifyMatch(resultTrx[arrayIndex++], '-1', false, FailureHandling.CONTINUE_ON_FAILURE))

                    if (GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI') {
						
						'verify trx PNBP qty / text verification = -1'
						arrayMatch.add(WebUI.verifyMatch(resultTrx[arrayIndex++], '-1', false, FailureHandling.CONTINUE_ON_FAILURE))
						
                        arrayIndex = 0

                        'get data from db'
                        ArrayList<String> result = CustomKeywords.'connection.APIFullService.checkAPIRegisterActive'(conneSign, 
                            findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('email')), 
                            findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('tlp')))

                        ArrayList<String> resultDataUser = CustomKeywords.'connection.Registrasi.buatUndanganStoreDB'(conneSign, 
                            findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('email')),
							findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('tlp')))
						
                        println(resultDataUser)

						if (GlobalVariable.Psre == 'VIDA') {
							'verify is_active'
							arrayMatch.add(WebUI.verifyMatch((result[arrayIndex++]), '1', false, FailureHandling.CONTINUE_ON_FAILURE))							
						} else if (GlobalVariable.Psre == 'DIGI') {
							'verify is_active'
							arrayMatch.add(WebUI.verifyMatch((result[arrayIndex++]), '0', false, FailureHandling.CONTINUE_ON_FAILURE))							
						}

                        'verify is_registered'
                        arrayMatch.add(WebUI.verifyMatch((result[arrayIndex++]), '1', false, FailureHandling.CONTINUE_ON_FAILURE))

                        'reset index kembali 0 untuk array selanjutnya'
                        arrayIndex = 0

                        'verify full name'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('nama')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify tempat lahir'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('tmpLahir')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'parse Date from MM/dd/yyyy > yyyy-MM-dd'
                        sDate = CustomKeywords.'customizekeyword.ParseDate.parseDateFormat'(resultDataUser[arrayIndex++], 
                            'MM/dd/yyyy', 'yyyy-MM-dd')

                        'verify tanggal lahir'
                        arrayMatch.add(WebUI.verifyMatch(sDate.toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('tglLahir')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify jenis kelamin'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('jenisKelamin')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

						if(findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('email')) != '""') {
							'verify email'
							arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
									GlobalVariable.NumofColm, rowExcel('email')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))							
						} else {
							arrayIndex++
						}

                        'verify provinsi'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('provinsi')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify kota'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('kota')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify kecamatan'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('kecamatan')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify kelurahan'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('kelurahan')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))

                        'verify kode pos'
                        arrayMatch.add(WebUI.verifyMatch((resultDataUser[arrayIndex++]).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
                                    GlobalVariable.NumofColm, rowExcel('kodePos')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))
                    } else if (GlobalVariable.Psre == 'PRIVY') {
                        'looping untuk delay 100detik menunggu proses request status'
                        for (delay = 1; delay <= 5; delay++) {
                            resultDataUser = CustomKeywords.'connection.APIFullService.getAPIRegisterPrivyStoreDB'(conneSign, 
                                trxNo[0])

                            'reset arraymatch'
                            arrayMatch = []

                            arrayIndex = 0

                            'verify request status = 1'
                            arrayMatch.add(WebUI.verifyMatch(resultDataUser[arrayIndex++], '0', false, FailureHandling.OPTIONAL))

                            'verify is external = 1'
                            arrayMatch.add(WebUI.verifyMatch(resultDataUser[arrayIndex++], '1', false, FailureHandling.OPTIONAL))

                            if (arrayMatch.contains(false)) {
                                'jika sudah delay ke 5 maka dianggap failed'
                                if (delay == 5) {
                                    'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, 
                                        GlobalVariable.NumofColm, GlobalVariable.StatusFailed, ((findTestData(excelPathAPIRegistrasi).getValue(
                                            GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB) + 
                                        ' Karena Job Tidak Jalan')
                                }
                                
                                'delay 20detik'
                                WebUI.delay(20)
                            } else {
                                break
                            }
                        }
                    }
					
					if (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('psreInput')) == '""') {

						String resultDefaultVvendor = CustomKeywords.'connection.APIFullService.getDefaultVendor'(conneSign,
							GlobalVariable.Tenant)
						
						'verify psre default =  respon'
						arrayMatch.add(WebUI.verifyMatch(resultDefaultVvendor.toUpperCase(), psreCode.toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))
					} else {
						if(GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI' || GlobalVariable.Psre == 'TKNAJ') {
							'verify psre registered db = excel'
							arrayMatch.add(WebUI.verifyMatch((resultVendorRegistered).toUpperCase(), findTestData(excelPathAPIRegistrasi).getValue(
									GlobalVariable.NumofColm, rowExcel('psreInput')).toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))
							
							'verify psre registered db =  respon'
							arrayMatch.add(WebUI.verifyMatch(resultVendorRegistered.toUpperCase(), psreCode.toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))
						}
						
						'verify psre input =  respon'
						arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('psreInput')).toUpperCase(),
							 psreCode.toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE))
					}
                    
                    'jika data db tidak sesuai dengan excel'
                    if (arrayMatch.contains(false)) {
                        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                                rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB)
						
						GlobalVariable.FlagFailed = 1
                    } else {
                        'write to excel success'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 
                            0, GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
                    }
                    
                    if (GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI') {
                        'kurang saldo before dengan proses verifikasi'
                        saldoBefore.set(0, (Integer.parseInt(saldoBefore[0]) - 1).toString())

                        'kurang saldo before dengan proses PNBP'
                        saldoBefore.set(1, (Integer.parseInt(saldoBefore[1]) - 1).toString())

                        saldoAfter = loginAdminGetSaldo(countCheckSaldo, conneSign)

                        println(saldoAfter)

                        'verify saldo before dan after'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch(saldoBefore.toString(), saldoAfter.toString(), false, 
                                FailureHandling.CONTINUE_ON_FAILURE), ' Saldo Gagal Potong')
						
						if ((((findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Certif Notif')) == '0' || 
								findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Certif Notif')) == 'null') && 
								(findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Service')) == '1')) || (
								(findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Setting Email Service')) == '0'))) &&
								findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Email')).toUpperCase().contains('OUTLOOK.COM') &&
								GlobalVariable.Psre == 'VIDA') {
							'call keyword get email'
							String emailCert = CustomKeywords.'customizekeyword.GetEmail.getEmailContent'(findTestData(excelPathAPIRegistrasi).getValue(
												GlobalVariable.NumofColm, rowExcel('email')).replace('"',''), findTestData(excelPathAPIRegistrasi).getValue(
												GlobalVariable.NumofColm, rowExcel('Password')).replace('"',''), 'Certif')
							
							'verify email cert'
							checkVerifyEqualOrMatch(WebUI.verifyMatch(emailCert, 'Penerbitan Sertifikat Elektronik',
									false, FailureHandling.CONTINUE_ON_FAILURE), ' email cert tidak terkirim')
						}
                    }
                }
            } else {
                'mengambil status code berdasarkan response HIT API'
                message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

                trxNo = WS.getElementPropertyValue(respon, 'trxNo', FailureHandling.OPTIONAL)

                'Write To Excel GlobalVariable.StatusFailed and errormessage'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                    GlobalVariable.StatusFailed, ('<' + message) + '>')

                GlobalVariable.FlagFailed = 1

                if ((GlobalVariable.checkStoreDB == 'Yes') && (trxNo != null)) {
                    'get trx dari db'
                    ArrayList<String> resultTrx = CustomKeywords.'connection.APIFullService.getAPIRegisterTrx'(conneSign, 
                        trxNo[0], trxNo[1])

                    arrayIndex = 0

                    'verify saldo Verification'
                    checkVerifyEqualOrMatch(WebUI.verifyMatch(resultTrx[arrayIndex++], '-1', false, FailureHandling.CONTINUE_ON_FAILURE), 
                        ' Gagal Verifikasi Saldo Terpotong')

                    if (GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI') {
						
						if(message.equalsIgnoreCase('Verifikasi Gagal. Nama, Tanggal Lahir, atau Foto Diri tidak sesuai. Harap cek kembali Nama dan Tanggal Lahir Anda serta mengambil ulang Foto Diri.')) {
							'verify saldo VIDA PNBP / DIGI TEXT VERIFICATION'
							checkVerifyEqualOrMatch(WebUI.verifyMatch((resultTrx[arrayIndex++]).toString(), '-1', false, FailureHandling.CONTINUE_ON_FAILURE), 
									' Gagal Verifikasi Saldo Terpotong - VIDA / DIGI')														
						} else {
							'verify saldo VIDA PNBP / DIGI TEXT VERIFICATION'
							checkVerifyEqualOrMatch(WebUI.verifyMatch((resultTrx[arrayIndex++]).toString(), 'null', false, FailureHandling.CONTINUE_ON_FAILURE),
									' Gagal Verifikasi Saldo Terpotong - VIDA / DIGI')
						}

                        'kurang saldo before dengan proses verifikasi'
                        saldoBefore.set(0, (Integer.parseInt(saldoBefore[0]) - 1).toString())

						if(message.contains('Verifikasi Gagal. Nama, Tanggal Lahir, atau Foto Diri tidak sesuai.')) {
							'kurang saldo before dengan proses PNBP'
							saldoBefore.set(1, (Integer.parseInt(saldoBefore[1]) - 1).toString())							
						}

                        saldoAfter = loginAdminGetSaldo(countCheckSaldo, conneSign)

                        'verify saldo before dan after'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch(saldoBefore.toString(), saldoAfter.toString(), false, 
                                FailureHandling.CONTINUE_ON_FAILURE), ' Saldo Gagal Potong')
                    } else if (GlobalVariable.Psre == 'PRIVY') {
                        'verify saldo privy PNBP'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch((resultTrx[arrayIndex++]).toString(), 'null', false, FailureHandling.CONTINUE_ON_FAILURE), 
                            ' Gagal Verifikasi Saldo Terpotong - Privy')
                    }
                    
                    'jika data db tidak sesuai dengan excel'
                    if (arrayMatch.contains(false)) {
                        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
                                rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB)

                        GlobalVariable.FlagFailed = 1
                    }
                }
            }
        } else {
            'mengambil status code berdasarkan response HIT API'
            message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

            'Write To Excel GlobalVariable.StatusFailed and errormessage'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                GlobalVariable.StatusFailed, ('<' + message) + '>')

            GlobalVariable.FlagFailed = 1
        }
    }
}

def loginAdminGetSaldo(int countCheckSaldo, Connection conneSign) {
    ArrayList<String> saldo = []
	
	'call test case login per case'
	WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('sheet') : sheet, ('Path') : excelPathAPIRegistrasi, ('Email') : 'Email Login', ('Password') : 'Password Login'
		, ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.STOP_ON_FAILURE)

    'check if button menu visible atau tidak'
    if (WebUI.verifyElementNotVisible(findTestObject('RegisterEsign/checkSaldo/menu_Saldo'), FailureHandling.OPTIONAL)) {
        'click menu saldo'
        WebUI.click(findTestObject('button_HamburberSideMenu'))
    }
    
    'click menu saldo'
    WebUI.click(findTestObject('RegisterEsign/checkSaldo/menu_Saldo'))

    'click ddl bahasa'
    WebUI.click(findTestObject('RegisterEsign/checkSaldo/button_bahasa'))

    'click english'
    WebUI.click(findTestObject('RegisterEsign/checkSaldo/button_English'))

    'select vendor'
    WebUI.selectOptionByValue(findTestObject('RegisterEsign/checkSaldo/select_Vendor'), '(?i)' + GlobalVariable.Psre, true)

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > div > div > div div'))

    for (index = 2; index <= variable.size(); index++) {
        'modify object box info'
        modifyObjectBoxInfo = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
            'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + index) + 
            ']/div/div/div/div/div[1]/h3', true)

        'check if box info = tipe saldo di excel'
        if (WebUI.getText(modifyObjectBoxInfo).equalsIgnoreCase('Verification') || (WebUI.getText(modifyObjectBoxInfo).equalsIgnoreCase(
            'PNBP') && (GlobalVariable.Psre == 'VIDA')) || (WebUI.getText(modifyObjectBoxInfo).equalsIgnoreCase(
            'Text Verification') && (GlobalVariable.Psre == 'DIGI'))) {
            'modify object qty'
            modifyObjectQty = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
                'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + index) + 
                ']/div/div/div/div/div[2]/h3', true)

            'get qty saldo before'
            saldo.add(WebUI.getText(modifyObjectQty).replace(',', ''))

            'if saldo sudah terisi 2 verification dan pnbp'
            if ((saldo.size() == 2) && (GlobalVariable.Psre == 'VIDA' || GlobalVariable.Psre == 'DIGI')) {
                break
            } else if ((saldo.size() == 1) && (GlobalVariable.Psre == 'PRIVY')) {
                break
            }
            
            continue
        }
    }
    
    if (countCheckSaldo == 1) {
        'call function input filter saldo'
        inputFilterSaldo('Verification', conneSign)

        if ((GlobalVariable.FlagFailed == 0) && (GlobalVariable.Psre == 'VIDA')) {
            'call function input filter saldo'
            inputFilterSaldo('PNBP', conneSign)
        }
		
		if ((GlobalVariable.FlagFailed == 0) && (GlobalVariable.Psre == 'DIGI')) {
			'call function input filter saldo'
			inputFilterSaldo('Text Verification', conneSign)
		}
    }
    
    return saldo
}

def checkVerifyEqualOrMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
            GlobalVariable.StatusFailed, ((findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + 
            ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + reason)

        GlobalVariable.FlagFailed = 1
    }
}

def inputFilterSaldo(String tipeSaldo, Connection conneSign) {
    'get current date'
    currentDate = new Date().format('yyyy-MM-dd')

    'input tipe saldo'
    WebUI.setText(findTestObject('RegisterEsign/checkSaldo/input_TipeSaldo'), tipeSaldo)

    'enter untuk input tipe saldo'
    WebUI.sendKeys(findTestObject('RegisterEsign/checkSaldo/input_TipeSaldo'), Keys.chord(Keys.ENTER))

    'input tanggal Transaksi'
    WebUI.setText(findTestObject('RegisterEsign/checkSaldo/input_TanggalTransaksi'), currentDate)

    'click button cari'
    WebUI.click(findTestObject('RegisterEsign/checkSaldo/button_Cari'))

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-footer > div > datatable-pager > ul li'))

    'modify object button last page'
    modifyObjectButtonLastPage = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-footer/div/datatable-pager/ul/li[' + 
        variable.size()) + ']', true)

    if (WebUI.getAttribute(modifyObjectButtonLastPage, 'class', FailureHandling.OPTIONAL) != 'disabled') {
        'click button last page'
        WebUI.click(findTestObject('RegisterEsign/checkSaldo/button_LastPage'))
    }
    
    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

    'modify object no transaksi'
    modifyObjectNoTransaksi = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[1]/div', true)

    'modify object tanggal transaksi'
    modifyObjectTanggalTransaksi = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[2]/div', true)

    'modify object tipe transaksi'
    modifyObjectTipeTransaksi = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[3]/div', true)

    'modify object user'
    modifyObjectUser = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 'equals', 
        ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[4]/div', true)

    'modify object no kontrak'
    modifyObjectNoKontrak = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[5]/div', true)

    'modify object Catatan'
    modifyObjectCatatan = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 'equals', 
        ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[8]/div', true)

    'modify object qty'
    modifyObjectQty = WebUI.modifyObjectProperty(findTestObject('RegisterEsign/checkSaldo/modifyObject'), 'xpath', 'equals', 
        ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[9]/div', true)

    'get trx dari db'
    ArrayList<String> result = CustomKeywords.'connection.DataVerif.getSaldoTrx'(conneSign, findTestData(excelPathAPIRegistrasi).getValue(
            GlobalVariable.NumofColm, rowExcel('email')), findTestData(excelPathAPIRegistrasi).getValue(GlobalVariable.NumofColm, 
            rowExcel('tlp')), 'Use ' + tipeSaldo)

    arrayIndex = 0

    'verify no trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectNoTransaksi), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), 
        ' No Trx ' + tipeSaldo)

    'verify tgl trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTanggalTransaksi), (result[arrayIndex++]).replace(
                '.0', ''), false, FailureHandling.CONTINUE_ON_FAILURE), ' Tanggal Trx ' + tipeSaldo)

    'verify tipe trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTipeTransaksi), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), 
        ' Tipe Trx ' + tipeSaldo)

    'verify user trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectUser), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), 
        ' User ' + tipeSaldo)

    'verify note trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectCatatan).replace('\u00A0', ' '), result[arrayIndex++].replace('\u00A0', ' '), false, FailureHandling.CONTINUE_ON_FAILURE), 
        ' Notes ' + tipeSaldo)

    'verify qty trx ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectQty), (result[arrayIndex++]).replace('-', ''), false, 
            FailureHandling.CONTINUE_ON_FAILURE), ' Qty Trx ' + tipeSaldo)
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

