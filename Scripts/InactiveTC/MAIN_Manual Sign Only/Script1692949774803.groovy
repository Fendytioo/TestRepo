import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import org.openqa.selenium.By as By
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import java.sql.Connection as Connection
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get current date'
def currentDate = new Date().format('yyyy-MM-dd')

'declare untuk split array excel dan beberapa variable yang dibutuhkan'
semicolon = ';'

splitIndex = -1

indexForCatatanStamp = 0

int firstRun = 0

'looping berdasarkan jumlah kolom'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= findTestData(excelPathManualSign).columnNumbers; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break
    } else if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase(
        'Unexecuted')) {
        'get tenant dari excel percase'
        GlobalVariable.Tenant = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))

        'get psre dari excel percase'
        GlobalVariable.Psre = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Psre Login'))

        if ((findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm - 1, rowExcel('Email Login')) != findTestData(
            excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Email Login'))) || (firstRun == 0)) {
			'call test case login per case'
			WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('SheetName') : sheet, ('Path') : excelPathManualSign, ('Email') : 'Email Login', ('Password') : 'Password Login'
				, ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.STOP_ON_FAILURE)

            firstRun = 1
        }
        
		if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
			GlobalVariable.FlagFailed = 0
		}

        'Inisialisasi array dan variable'
        ArrayList namaTandaTangan = [], notelpTandaTangan = []

        indexEmail = 0

        'inisiasi index menjadi 9 untuk modify object ketika tidak pada e-meterai'
        index = 9

        'Inisialisasi variable yang dibutuhkan'
        emailPenandaTangan = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Email')).split(
            semicolon, splitIndex)

        editNamaAfterSearch = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Edit Nama After Search')).split(
            semicolon, splitIndex)

        namaPenandaTangan = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Nama')).split(
            semicolon, splitIndex)

        tipeTandaTangan = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$TipeTandaTangan')).split(
            semicolon, splitIndex)

        totalTandaTangan = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('jumlah signer lokasi per signer')).split(
            semicolon, splitIndex)

        pindahkanSignBox = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Pindahkan SignBox')).split(
            semicolon, splitIndex)

        lokasiSignBox = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Lokasi Pemindahan signbox')).split(
            '\\n', splitIndex)

        lockSignBox = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Lock Sign Box')).split(
            semicolon, splitIndex)

        catatanStamping = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Catatan Stamping')).split(
            semicolon, splitIndex)

        'Klik tombol menu manual sign'
        WebUI.click(findTestObject('ManualSign/ManualSign'))

        'Jika kolomnya berada pada kedua'
        if (GlobalVariable.NumofColm == 2) {
            inputCancel(conneSign)
        }
        
        'Pengecekan apakah masuk page manual sign'
        if (WebUI.verifyElementPresent(findTestObject('ManualSign/lbl_ManualSign'), GlobalVariable.TimeOut)) {
            'Input form yang ada pada page'
            inputForm()

            'jika setting menggunakan e-meterai'
            if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Membutuhkan e-Meterai')) == 
            'Yes') {
                'index meningkat karena tambahan 2 kolom ketika menggunakan e-meterai'
                index = 11
            } else {
				'modify menuju index normal'
				index = 9
            }
		
			'modify label daftar penanda tangan dengan naiknya index'
			modifyObjectLblDaftarPenandaTangan = WebUI.modifyObjectProperty(findTestObject('ManualSign/lbl_daftarpenandatangan'),
				'xpath', 'equals', ('//*[@id="msxForm"]/div[' + index) + ']/div[3]/table/tr/td/small', true)

			'modify button tambah penanda tangan dengan naiknya index'
			modifyObjectbuttonTambahPenandaTangan = WebUI.modifyObjectProperty(findTestObject('ManualSign/button_tambahTandaTangan'),
				'xpath', 'equals', ('//*[@id="msxForm"]/div[' + index) + ']/div[2]/a', true)
			
            emailService = CustomKeywords.'connection.DataVerif.getEmailService'(conneSign, GlobalVariable.Tenant)

            'click tambah penanda tangan'
            WebUI.click(modifyObjectbuttonTambahPenandaTangan)

            'diberikan delay 3 detik dengan loading'
            WebUI.delay(1)

            if (checkErrorLog() == true) {
                continue
            }
            
            if (emailService.toString() == '1') {
                if (WebUI.verifyElementNotPresent(findTestObject('ManualSign/input_phonePenandaTangan'), GlobalVariable.TimeOut, 
                    FailureHandling.OPTIONAL)) {
                    'write excel mengenai error log tersebut'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusFailed, (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                            rowExcel('Reason Failed') + ';') + 'Field phone tidak muncul saat setting email service aktif pada tenant ') + 
                        GlobalVariable.Tenant)
                }
            }
            
            'klik button x untuk keluar dari tambahan penanda tangan'
            WebUI.click(findTestObject('ManualSign/button_x'))

            'diberikan delay 3 detik dengan loading'
            WebUI.delay(1)

            'looping berdasarkan email penanda tangan'
            for (int i = 0; i < emailPenandaTangan.size(); i++) {
                if ((emailPenandaTangan[i]) != '') {
                    'klik tambah penanda tangan'
                    WebUI.click(modifyObjectbuttonTambahPenandaTangan)
                } else if ((emailPenandaTangan[i]) == '') {
                    'jika nama dan email penanda tangan kosong'
                    continue
                }
                
                'jika label tambah penanda tangan muncul'
                if (WebUI.verifyElementPresent(findTestObject('ManualSign/lbl_TambahPenandaTangan'), GlobalVariable.TimeOut) == 
                true) {
                    'set text pada email penanda tangan'
                    WebUI.setText(findTestObject('ManualSign/input_emailPenandaTangan'), emailPenandaTangan[i])

                    if (emailService.toString() == '1') {
                        'klik search penanda tangan'
                        WebUI.click(findTestObject('ManualSign/button_searchPenandaTanganWithoutEmailService'))
                    } else {
                        'klik search penanda tangan'
                        WebUI.click(findTestObject('ManualSign/button_searchPenandaTanganWithEmailService'))
                    }
                    
                    'diberikan delay 1 detik dengan loading search'
                    WebUI.delay(1)

                    'jika muncul error log penanda tangan'
                    if (WebUI.verifyElementPresent(findTestObject('ManualSign/errorLog_PenandaTangan'), GlobalVariable.TimeOut, 
                        FailureHandling.OPTIONAL)) {
                        'get text error log'
                        error = WebUI.getText(findTestObject('ManualSign/errorLog_PenandaTangan'))

                        'write excel mengenai error log tersebut'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (((findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('Reason Failed')) + ';') + '<') + error) + '>')

                        'diberikan delay 1 detik agar sudah ter write sebelum button cancel'
                        WebUI.delay(1)

                        'klik button cancel'
                        WebUI.click(findTestObject('ManualSign/button_Cancel'))

                        'delay 1 detik agar cancelnya benar-benar diclick'
                        WebUI.delay(1)

                        continue
                    }
                    
                    'verifikasi signer ketika sudah submit signer'
                    String verifikasiSigner = CustomKeywords.'connection.ManualSign.getVerificationSigner'(conneSign, (emailPenandaTangan[
                        i]).toString().toUpperCase())

                    'check data mengenai email penanda tangan'
                    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_emailPenandaTangan'), 
                                'value'), (emailPenandaTangan[i]).toString().toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE), 
                        ' pada email Penanda Tangan ')

                    'check data nama dari UI dengan db'
                    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_namaPenandaTangan'), 
                                'value'), verifikasiSigner.toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE), ' pada nama Penanda Tangan ')

                    if ((editNamaAfterSearch[i]) == 'Yes') {
                        'set text pada email penanda tangan'
                        WebUI.setText(findTestObject('ManualSign/input_namaPenandaTangan'), namaPenandaTangan[i])
                    }
                }
                
                'diberikan delay 3 detik untuk'
                WebUI.delay(2)

                'check save ada attribute disabled'
                if (WebUI.verifyElementHasAttribute(findTestObject('ManualSign/button_Save'), 'disabled', GlobalVariable.TimeOut, 
                    FailureHandling.OPTIONAL)) {
                    'write to excel save gagal'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusFailed, (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                            rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedMandatory)

                    break
                } else {
                    'klik button save'
                    WebUI.click(findTestObject('ManualSign/button_Save'))
                }
                
                for (int p = 0; p < emailPenandaTangan.size(); p++) {
                    'Inisialisasi array dan index yang dibutuhkan'
                    arrayIndex = 0

                    arrayIndexValue = 0

                    indexObject = 1

                    'gmofidy modifyObjectInformasiPenandaTangan'
                    modifyObjectInformasiPenandaTangan = WebUI.modifyObjectProperty(findTestObject('ManualSign/modifyObject'), 
                        'xpath', 'equals', ((('//*[@id="msxForm"]/div[' + index) + ']/div[3]/table/tr[') + (p + 1)) + ']/td/p', 
                        true)

                    'mengambil informasi signer '
                    valueInformasi = WebUI.getText(modifyObjectInformasiPenandaTangan).replace('\n', ', ')

                    'split informasi signer berdasarkan delimiter'
                    valueInformasi = valueInformasi.split(', ', -1)
                    'query check informasi dari user tersebut'
                    queryCheckInformationUser = CustomKeywords.'connection.ManualSign.getInformationUser'(conneSign, (emailPenandaTangan[
                        indexEmail++]).toString().toUpperCase(), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                            rowExcel('$PSrE')))

                    if (((valueInformasi[2]) == (emailPenandaTangan[(indexEmail - 1)]).toString().toUpperCase())) {
                        'check ui dan query mengenai nama signer'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch(valueInformasi[arrayIndexValue++], queryCheckInformationUser[
                                arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada informasi nama penanda tangan ')

                        'check ui dan query mengenai nomor telepon signer'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch(CustomKeywords.'customizekeyword.ParseText.convertToSHA256'(
                                    valueInformasi[arrayIndexValue++]), queryCheckInformationUser[arrayIndex++], false, 
                                FailureHandling.CONTINUE_ON_FAILURE), ' pada informasi nomor telepon penanda tangan ')

                        'check ui dan query mengenai email signer'
                        checkVerifyEqualOrMatch(WebUI.verifyMatch(valueInformasi[arrayIndexValue++], (queryCheckInformationUser[
                                arrayIndex++]).toString().toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE), ' pada informasi email penanda tangan ')

                        'add nama tanda tangan yang sukses dan nomor telepon'
                        namaTandaTangan.add(valueInformasi[0])

                        notelpTandaTangan.add(valueInformasi[1])

                        break
                    } else {
                        indexEmail--
                    }
                }
            }
            
            'jika button next ada disabled'
            if (WebUI.verifyElementHasAttribute(findTestObject('ManualSign/button_Next'), 'disabled', GlobalVariable.TimeOut, 
                FailureHandling.OPTIONAL)) {
                'write to excel bahwa save gagal'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + 
                    GlobalVariable.ReasonFailedSaveGagal)

                continue
            } else {
                if (WebUI.verifyElementPresent(modifyObjectLblDaftarPenandaTangan, GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
                    'write to excel bahwa save gagal'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusFailed, (((findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                            rowExcel('Reason Failed')) + ';') + '<') + 'Silahkan tambah penanda tangan terlebih dulu!') + 
                        '>')
                } else {
                    WebUI.click(findTestObject('ManualSign/button_Next'))
                }
            }
            
            'check element present pada next tanda tangan'
            if (WebUI.verifyElementPresent(findTestObject('ManualSign/lbl_documentNo'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
                'check ui dan excel pada nomor dokumen'
                checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/lbl_documentNo'), 
                            'value'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Nomor Dokumen')), 
                        false, FailureHandling.CONTINUE_ON_FAILURE), ' pada informasi nomor dokumen ')

                'check ui dan excel pada nama dokumen'
                checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_documentName'), 
                            'value'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Nama Dokumen')), 
                        false, FailureHandling.CONTINUE_ON_FAILURE), ' pada informasi nama dokumen ')

                'Klik button tanda tangan'
                WebUI.click(findTestObject('Object Repository/ManualSign/btn_ttd'))

                'diberikan delay 5 detik untuk loading'
                WebUI.delay(5)

                'Klik set tanda tangan'
                WebUI.click(findTestObject('ManualSign/btn_setTandaTangan'))

                'click button lock signbox'
                WebUI.click(findTestObject('ManualSign/btn_LockSignBox'))

                isLocked = WebUI.getAttribute(findTestObject('ManualSign/btn_LockSignBox'), 'class', FailureHandling.STOP_ON_FAILURE)

                'verify sign box is locked'
                checkVerifyEqualOrMatch(WebUI.verifyMatch(isLocked, 'fa fa-2x fa-lock', false, FailureHandling.CONTINUE_ON_FAILURE), 
                    ' pada sign locked ')

                'Klik button Delete'
                WebUI.click(findTestObject('ManualSign/btn_DeleteSignBox'))

                'verify sign box is deleted'
                checkVerifyEqualOrMatch(WebUI.verifyElementNotPresent(findTestObject('ManualSign/signBox'), GlobalVariable.TimeOut, 
                        FailureHandling.CONTINUE_ON_FAILURE), ' pada deletenya box Sign ')
            } else {
                continue
            }
            
            index = 0

            countValue = 0

            'looping berdasarkan total tanda tangan'
            for (int j = 1; j <= tipeTandaTangan.size(); j++) {
                if ((tipeTandaTangan[(j - 1)]).equalsIgnoreCase('TTD')) {
                    'Klik button tanda tangan'
                    WebUI.click(findTestObject('Object Repository/ManualSign/btn_ttd'))
                } else if ((tipeTandaTangan[(j - 1)]).equalsIgnoreCase('Meterai')) {
                    'Klik button tanda tangan'
                    WebUI.click(findTestObject('Object Repository/ManualSign/btn_meterai'))
                }
                
                WebUI.delay(1)

                'modify label tipe tanda tangan di kotak'
                modifyobjectTTDlblRoleTandaTangan = WebUI.modifyObjectProperty(findTestObject('Object Repository/ManualSign/modifyObject'), 
                    'xpath', 'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-setting-signer/div[2]/div/app-document-anotate/section/section[2]/div/app-bbox[' + 
                    j) + ']/div/div/small', true)

                if ((tipeTandaTangan[(j - 1)]).equalsIgnoreCase('TTD')) {
                    'Verify label tanda tangannya muncul atau tidak'
                    WebUI.verifyElementPresent(findTestObject('TandaTanganDokumen/lbl_TipeTandaTangan'), GlobalVariable.TimeOut, 
                        FailureHandling.CONTINUE_ON_FAILURE)

                    'Klik set tanda tangan'
                    WebUI.click(findTestObject('ManualSign/ddl_TipeTandaTangan'))

                    if (countValue == Integer.parseInt(totalTandaTangan[index])) {
                        index++

                        countValue = 0
                    }
                    
                    'Memilih tipe signer apa berdasarkan excel'
                    WebUI.selectOptionByLabel(findTestObject('ManualSign/ddl_TipeTandaTangan'), ((namaTandaTangan[index]) + 
                        ' - ') + (notelpTandaTangan[index]), false)

                    countValue++

                    'Klik set tanda tangan'
                    WebUI.click(findTestObject('ManualSign/btn_setTandaTangan'))

                    'Verifikasi antara excel dan UI, apakah tipenya sama'
                    WebUI.verifyMatch(namaTandaTangan[index], WebUI.getText(modifyobjectTTDlblRoleTandaTangan), false)
                }
                
                'Verify apakah tanda tangannya ada'
                if (WebUI.verifyElementPresent(modifyobjectTTDlblRoleTandaTangan, GlobalVariable.TimeOut, FailureHandling.CONTINUE_ON_FAILURE)) {
                    'check if signbox mau dipindahkan'
                    if ((pindahkanSignBox[(j - 1)]).equalsIgnoreCase('Yes')) {
                        'memindahkan sign box'
                        CustomKeywords.'customizekeyword.JSExecutor.jsExecutionFunction'('arguments[0].style.transform = arguments[1]', 
                            ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-setting-signer/div[2]/div/app-document-anotate/section/section[2]/div/app-bbox[' + 
                            j) + ']/div', lokasiSignBox[(j - 1)])
                    }
                    
                    'check if signbox mau dilock posisinya'
                    if ((lockSignBox[(j - 1)]).equalsIgnoreCase('Yes')) {
                        'modify obejct lock signbox'
                        modifyobjectLockSignBox = WebUI.modifyObjectProperty(findTestObject('ManualSign/btn_LockSignBox'), 
                            'xpath', 'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-setting-signer/div[2]/div/app-document-anotate/section/section[2]/div/app-bbox[' + 
                            j) + ']/div/button[1]/span', true)

                        'click lock signbox'
                        WebUI.click(modifyobjectLockSignBox)

                        if ((tipeTandaTangan[(j - 1)]).equalsIgnoreCase('Meterai')) {
                            WebUI.verifyElementPresent(findTestObject('ManualSign/lbl_isiCatatanStamping'), GlobalVariable.TimeOut, 
                                FailureHandling.CONTINUE_ON_FAILURE)

                            WebUI.setText(findTestObject('ManualSign/input_isiCatatanStamping'), catatanStamping[indexForCatatanStamp++])

                            WebUI.click(findTestObject('ManualSign/button_batalCatatanStamping'))

                            'click lock signbox'
                            WebUI.click(modifyobjectLockSignBox)

                            WebUI.delay(2)

                            checkVerifyEqualOrMatch(WebUI.verifyMatch('', WebUI.getText(
                                        findTestObject('ManualSign/input_isiCatatanStamping')), false, FailureHandling.CONTINUE_ON_FAILURE), 
                                ' pada catatan meterai ')

                            WebUI.setText(findTestObject('ManualSign/input_isiCatatanStamping'), catatanStamping[(indexForCatatanStamp - 
                                1)])

                            WebUI.click(findTestObject('ManualSign/button_SimpanCatatanStamping'))
                        }
                    }
                }
            }
            
            'click button simpan'
            WebUI.click(findTestObject('Object Repository/ManualSign/btn_proses'))

            WebUI.delay(3)

			'susun urutan tanda tangan'
			sortingSequenceSign()
			
            if (checkErrorLog() == true) {
                continue
            }
            
            if (GlobalVariable.FlagFailed == 0) {
                'write to excel success'
                CustomKeywords.'customizeKeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 0, GlobalVariable.NumofColm - 
                    1, GlobalVariable.StatusSuccess)

                if (GlobalVariable.checkStoreDB == 'Yes') {
                    result = CustomKeywords.'connection.ManualSign.getManualSign'(conneSign, findTestData(excelPathManualSign).getValue(
                            GlobalVariable.NumofColm, rowExcel('$Nomor Dokumen')))

                    index = 0

                    ArrayList arrayMatch = []

                    'verify vendor'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$PSrE')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify ref number'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$Nomor Dokumen')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify document name'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$Nama Dokumen')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify tanggal dokumen'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$Tanggal Dokumen')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify tipe pembayaran'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$Jenis Pembayaran')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    automaticAfterStamping = '0'

                    if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Membutuhkan e-Meterai')) == 
                    'Yes') {
                        tipeDokumenPeruri = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel(
                                'Tipe Dokumen Peruri'))

                        if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Stamp Meterai Otomatis')) == 
                        'Ya') {
                            automaticAfterStamping = '1'
                        }
                    } else {
                        tipeDokumenPeruri = ''
                    }
                    
                    'verify automatic stamping after sign'
                    arrayMatch.add(WebUI.verifyMatch(automaticAfterStamping, result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify tipe dokumen peruri'
                    arrayMatch.add(WebUI.verifyMatch(tipeDokumenPeruri, result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    totalDocument = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Dokumen')).split(
                        '\\n', -1)

                    'verify total dokumen'
                    arrayMatch.add(WebUI.verifyMatch(totalDocument.size().toString(), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify manual upload'
                    arrayMatch.add(WebUI.verifyMatch('1', result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'verify is sequence'
                    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('$isSequence')), result[index++], false, FailureHandling.CONTINUE_ON_FAILURE))

                    'jika data db tidak sesuai dengan excel'
                    if (arrayMatch.contains(false)) {
                        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                                rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB)
                    }
					
					String docId = CustomKeywords.'connection.DataVerif.getDocId'(conneSign, findTestData(excelPathManualSign).getValue(
						GlobalVariable.NumofColm, rowExcel('$Nomor Dokumen')), GlobalVariable.Tenant)

				'Write to excel mengenai Document ID'
				CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, 'Manual Sign', rowExcel('docId') -
					1, GlobalVariable.NumofColm - 1, docId)
                }
            }
        }
    }
}

def inputCancel(Connection conneSign) {
    inputForm()

    'get data tipe-tipe pembayaran secara asc'
    ArrayList vendorOfTenantDB = CustomKeywords.'connection.ManualSign.getVendorofTenant'(conneSign, GlobalVariable.Tenant)

    'check ddl tipe pembayaran'
    checkDDL(findTestObject('ManualSign/input_psre'), vendorOfTenantDB, ' pada DDL psre ')

    'get data tipe-tipe pembayaran secara asc'
    ArrayList jenisPembayaranByVendorDB = CustomKeywords.'connection.ManualSign.getPaymentTypeBasedOnTenantAndVendor'(conneSign, 
        GlobalVariable.Tenant, findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$PSrE')))

    'check ddl tipe pembayaran'
    checkDDL(findTestObject('ManualSign/input_jenisPembayaran'), jenisPembayaranByVendorDB, ' pada DDL jenis pembayaran ')

    'Klik button cancel'
    WebUI.click(findTestObject('ManualSign/button_batal'))

    'verify field kode template dokumen kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_psre'), 'value', FailureHandling.OPTIONAL), 
            '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field input psre tidak kosong ')

    'verify field Nama template dokumen kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_referenceNo'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field Nomor Kontrak tidak kosong ')

    'verify field Deskripsi template dokumen kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_documentName'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field Nama Dokumen tidak kosong ')

    'verify field tipe pembayaran kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_documentDate'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field Tanggal Dokumen tidak kosong ')

    'verify field status kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_jenisPembayaran'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field jenis Pembayaran tidak kosong ')

    'verify field status kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_isSequence'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field Urutan Tanda Tangan tidak kosong ')

    if (WebUI.verifyElementNotChecked(findTestObject('ManualSign/input_isE-Meterai'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
        'Input AKtif pada input Status'
        WebUI.click(findTestObject('ManualSign/btn_E-Meterai'))
    }
    
    'verify field status kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_tipeDokumenPeruri'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field jenis Pembayaran tidak kosong ')

    'verify field status kosong'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ManualSign/input_isAutomatedStamp'), 'value', 
                FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' Field jenis Pembayaran tidak kosong ')
}

def checkVerifyEqualOrMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            ((findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + 
            reason)

        GlobalVariable.FlagFailed = 1
    }
}

def checkDDL(TestObject objectDDL, ArrayList listDB, String reason) {
    'declare array untuk menampung ddl'
    ArrayList list = []

    'click untuk memunculkan ddl'
    WebUI.click(objectDDL)

    'get id ddl'
    id = WebUI.getAttribute(findTestObject('ManualSign/ddlClass'), 'id', FailureHandling.CONTINUE_ON_FAILURE)

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector(('#' + id) + '> div > div:nth-child(2) div'))

    'looping untuk get ddl kedalam array'
    for (i = 1; i < variable.size(); i++) {
        'modify object DDL'
        modifyObjectDDL = WebUI.modifyObjectProperty(findTestObject('ManualSign/modifyObject'), 'xpath', 'equals', ((('//*[@id=\'' + 
            id) + '-') + i) + '\']', true)

        'add ddl ke array'
        list.add(WebUI.getText(modifyObjectDDL))
    }
    
    'verify ddl ui = db'
    checkVerifyEqualOrMatch(listDB.containsAll(list), reason)

    println(listDB)

    println(list)

    'verify jumlah ddl ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyEqual(list.size(), listDB.size(), FailureHandling.CONTINUE_ON_FAILURE), ' Jumlah ' + 
        reason)

    'Input enter untuk tutup ddl'
    WebUI.sendKeys(objectDDL, Keys.chord(Keys.ENTER))
}

def checkErrorLog() {
    'Jika error lognya muncul'
    if (WebUI.verifyElementPresent(findTestObject('ManualSign/errorLog'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
        'ambil teks errormessage'
        errormessage = WebUI.getAttribute(findTestObject('ManualSign/errorLog'), 'aria-label', FailureHandling.CONTINUE_ON_FAILURE)

        'jika error message null, masuk untuk tulis error non-sistem'
        if (errormessage != null) {
            if (!(errormessage.contains('Permintaan tanda tangan berhasil dibuat.'))) {
                'Tulis di excel itu adalah error'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    (((findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
                        '-', '') + ';') + '<') + errormessage) + '>')
				
				return true
            }
        } else {
            'Tulis di excel itu adalah error'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
                    '-', '') + ';') + 'Error tidak berhasil ditangkap')
			
			return true
        }
    }
    
    return false
}

def inputForm() {
    'Input teks di nama template dokumen'
    WebUI.setText(findTestObject('ManualSign/input_psre'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$PSrE')))

    'Klik enter'
    WebUI.sendKeys(findTestObject('ManualSign/input_psre'), Keys.chord(Keys.ENTER))

    'Input teks kode template dokumen'
    WebUI.setText(findTestObject('ManualSign/input_referenceNo'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$Nomor Dokumen')))

    'Input AKtif pada input Status'
    WebUI.setText(findTestObject('ManualSign/input_documentName'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$Nama Dokumen')))

    'Input AKtif pada input Status'
    WebUI.setText(findTestObject('ManualSign/input_documentDate'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$Tanggal Dokumen')))

    'Input AKtif pada input Status'
    WebUI.setText(findTestObject('ManualSign/input_jenisPembayaran'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$Jenis Pembayaran')))

    'Klik enter'
    WebUI.sendKeys(findTestObject('ManualSign/input_jenisPembayaran'), Keys.chord(Keys.ENTER))

    'Input AKtif pada input Status'
    WebUI.setText(findTestObject('ManualSign/input_isSequence'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
            rowExcel('$isSequence')))

    'Klik enter'
    WebUI.sendKeys(findTestObject('ManualSign/input_isSequence'), Keys.chord(Keys.ENTER))

    'Code untuk mengambil file berdasarkan direktori masing-masing sekaligus ambil value dari excel'
    String userDir = System.getProperty('user.dir')

    String filePath = userDir + findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Dokumen'))

    'Upload file berdasarkan filePath yang telah dirancang'
    WebUI.uploadFile(findTestObject('ManualSign/input_documentExample'), filePath, FailureHandling.CONTINUE_ON_FAILURE)

    if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Membutuhkan e-Meterai')) == 'Yes') {
        if (WebUI.verifyElementNotChecked(findTestObject('ManualSign/input_isE-Meterai'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
            'Input AKtif pada input Status'
            WebUI.click(findTestObject('ManualSign/btn_E-Meterai'))
        }
        
        'Input Aktif pada input Status'
        WebUI.setText(findTestObject('ManualSign/input_tipeDokumenPeruri'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                rowExcel('Tipe Dokumen Peruri')))

        'Klik enter'
        WebUI.sendKeys(findTestObject('ManualSign/input_tipeDokumenPeruri'), Keys.chord(Keys.ENTER))

        'Input AKtif pada input Status'
        WebUI.setText(findTestObject('ManualSign/input_isAutomatedStamp'), findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, 
                rowExcel('$Stamp Meterai Otomatis')))

        'Klik enter'
        WebUI.sendKeys(findTestObject('ManualSign/input_isAutomatedStamp'), Keys.chord(Keys.ENTER))
    } else if (findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$Membutuhkan e-Meterai')) == 
    'No') {
        if (WebUI.verifyElementChecked(findTestObject('ManualSign/input_isE-Meterai'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
            'Input AKtif pada input Status'
            WebUI.click(findTestObject('ManualSign/btn_E-Meterai'))
        }
    }
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def sortingSequenceSign() {
	'check if Sequential signing iya'
	if(findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('$isSequence')).equalsIgnoreCase('Ya')) {
		'get urutan seq sign dari excel'
		ArrayList<String> seqSignRole = findTestData(excelPathManualSign).getValue(GlobalVariable.NumofColm, rowExcel('Urutan Signing')).toString().toUpperCase().split(';',-1)
		
		'count box'
		variable = DriverFactory.webDriver.findElements(By.cssSelector('#cdk-drop-list-0 div'))
		
		'looping seq sign'
		for (int seq = 1; seq <= variable.size(); seq++) {
			'modify label tipe tanda tangan di kotak'
			modifyObject = WebUI.modifyObjectProperty(findTestObject('TandaTanganDokumen/modifyObject'),
				'xpath', 'equals', '//*[@id="cdk-drop-list-0"]/div['+ seq +']', true)
			
			'get text pisah nomor dan psre'
			signer = WebUI.getText(modifyObject).split(' ',-1)
			
			index = seqSignRole.indexOf(signer[1]) + 1

			if (seq != index) {
				'modify label tipe tanda tangan di kotak'
				modifyObjectNew = WebUI.modifyObjectProperty(findTestObject('TandaTanganDokumen/modifyObject'),
					'xpath', 'equals', '//*[@id="cdk-drop-list-0"]/div['+ index +']', true)
				
				'pindahin ke urutan sesuai excel'
				WebUI.dragAndDropToObject(modifyObject, modifyObjectNew)
				
				'untuk proses pemindahan'
				WebUI.delay(2)
				
				seq--
			}
		}
		
		'click button simpan'
		WebUI.click(findTestObject('Object Repository/ManualSign/btn_simpan'))
		
		'delay untuk loading simpan'
		WebUI.delay(3)
	}
}
