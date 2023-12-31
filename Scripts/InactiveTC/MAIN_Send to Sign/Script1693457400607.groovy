import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import org.openqa.selenium.By as By
import java.sql.Connection as Connection
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - Full API Services.xlsx')

'get current date'
def currentDate = new Date().format('yyyy-MM-dd')

'Inisialisasi flag break untuk sequential'
int flagBreak = 0, useBiom = 0, alreadyVerif = 0

'Inisialisasi array untuk Listotp, arraylist arraymatch'
ArrayList listOTP = [], arrayMatch = []

'declare arrayindex'
arrayIndex = 0

'inisialisasi count resend dan saldo terpakai'
int countResend, countSaldoSplitLiveFCused

'declare sheet yang akan digunakan'
sheet = 'Send to Sign'

'looping untuk sending document'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= findTestData(excelPathFESignDocument).columnNumbers; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {
        'Call API Send doc'
        WebUI.callTestCase(findTestCase('APIFullService/Send Document/API Send Document'), 
            [('excelPathAPISendDoc') : excelPathFESignDocument, ('sheet') : sheet], FailureHandling.CONTINUE_ON_FAILURE)
		
        'Jika tidak ada dokumen id di excel'
        if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 6) == '') {
            'loop selanjutnya'
            continue
        }
        
        'Jika document tersebut tidak membutuhkan tanda tangan'
        if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 51) == 'No') {
            continue
        }
        
        'ambil db checking ke UI Beranda'
        ArrayList sendToSign = CustomKeywords.'connection.APIFullService.getDataSendtoSign'(conneSign, findTestData(
                excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 6))

        'Mengambil email berdasarkan documentId'
        ArrayList emailSigner = CustomKeywords.'connection.APIFullService.getEmailLogin'(conneSign, findTestData(
                excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 6)).split(';', -1)
		
		'list data saldo yang perlu diambil'		
		ArrayList saldoList = ['Liveness', 'Face Compare', 'Liveness Face Compare', 'OTP']
		
		'ambil kondisi default face compare'
		String mustFaceCompDB = CustomKeywords.'connection.DataVerif.getMustLivenessFaceCompare'(conneSign, GlobalVariable.Tenant)
		
		'ambil kondisi max liveness harian'
		int maxFaceCompDB = Integer.parseInt(CustomKeywords.'connection.DataVerif.getLimitLivenessDaily'(conneSign))
				
		'ambil nama vendor dari DB'		
		String vendor = CustomKeywords.'connection.DataVerif.getVendorNameForSaldo'(conneSign, findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 11).replace('"',''))
		
		'ambil metode verifikasi dari excel'
		String verifMethod = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('CaraVerifikasi(Biometric/OTP)'))
		
        'Mengambil tenantCode dari excel berdasarkan input body API'
        String tenantCode = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 9).replace('"', '')
		
        'Mengambil aes key based on tenant tersebut'
        String aesKey = CustomKeywords.'connection.APIFullService.getAesKeyBasedOnTenant'(conneSign, tenantCode)

        'jumlah signer yang telah tanda tangan masuk dalam variable dibawah'
        int jumlahSignerTandaTangan = CustomKeywords.'connection.APIFullService.getTotalSigned'(conneSign, findTestData(
                excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 6))

        'saldoUsedDocPertama hanya untuk dokumen pertama'
        int saldoUsedDocPertama = 0

        'looping email signer'
        for (int o = 1; o <= emailSigner.size(); o++) {
			
			'dapatkan count untuk limit harian facecompare akun tersebut'
			int countLivenessFaceComp = CustomKeywords.'connection.DataVerif.getCountFaceCompDaily'(conneSign, emailSigner[o-1])
			
            'saldo Used untuk penggunaan saldo'
            int saldoUsed = 0

            encryptMsg = encryptLink(findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 14), emailSigner[
                (o - 1)], aesKey)

            'membuat link document monitoring'
            linkDocumentMonitoring = ((((((((findTestData(excelPathFESignDocument).getValue(2, 86) + '?msg=') + encryptMsg) + 
            '&isHO=') + findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 83)) + '&isMonitoring=') + 
            findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 84)) + '&tenantCode=') + tenantCode)

            'membuat link kotak masuk'
            linkKotakMasuk = ((((findTestData(excelPathFESignDocument).getValue(2, 85) + '?msg=') + encryptMsg) + '&tenantCode=') + 
            tenantCode)

            'Inisialisasi variable yang dibutuhkan, Mengkosongkan nomor kontrak dan document Template Name'
            String noKontrak = '', saldoSignBefore, saldoSignAfter, otpBefore, otpAfter, documentTemplateName = '', noTelpSigner

            'Inisialisasi variable total document yang akan disign'
            int totalDocSign

            'Memanggil DocumentMonitoring untuk dicheck apakah documentnya sudah masuk'
            WebUI.callTestCase(findTestCase('Document Monitoring/VerifyDocumentMonitoring'), [('excelPathFESignDocument') : excelPathFESignDocument
                    , ('sheet') : sheet, ('linkDocumentMonitoring') : linkDocumentMonitoring, ('nomorKontrak') : noKontrak,
					('CancelDocsSend') : findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Send?'))], FailureHandling.CONTINUE_ON_FAILURE)

			'jika ada proses cancel doc'
			if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Send?')) == 'Yes') {
				
				'lanjutkan loop'
				continue
			}
			
			'panggil fungsi login'
			WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('SheetName') : sheet,
				('Path') : excelPathFESignDocument, ('Email') : 'Email Login', ('Password') : 'Password Login',
				 ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.CONTINUE_ON_FAILURE)

            'mengambil saldo before'
            saldoSignBefore = checkSaldoSign(conneSign, vendor)

            'mengambil saldo before'
            HashMap<String, String> saldoBefore = checkSaldo(saldoList, vendor)

            'tutup browsernya'
            WebUI.closeBrowser()
			
			'ubah flag untuk buka localhost jika syarat if terpenuhi'
			if (!vendor.equalsIgnoreCase('Privy') && mustFaceCompDB == '1') {
				
				'ubah link dari gdk menjadi localhost'
				linkKotakMasuk.replace('http://gdkwebsvr:8080','http://localhost:4600')
			}

            'Open browser dengan embed kotak masuk'
            runWithEmbed(linkKotakMasuk)
			
            'Klik checkbox ttd untuk semua'
            WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/checkboxTtdSemua'))

            'Klik button ttd bulk'
            WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TTDBulk'))

			if (checkPopupWarning() == false) {
				'klik tombol Batal'
				WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_BatalTandaTanganDokumen'))
			}
 
            'Jika running menggunakan embed, maka'
            if (GlobalVariable.RunWithEmbed == 'Yes') {
                'Ganti frame ke default'
                WebUI.switchToDefaultContent(FailureHandling.CONTINUE_ON_FAILURE)

                'click button embed untuk refresh'
                WebUI.click(findTestObject('EmbedView/button_Embed'))

                'swith to iframe'
                WebUI.switchToFrame(findTestObject('EmbedView/iFrameEsign'), GlobalVariable.TimeOut, FailureHandling.CONTINUE_ON_FAILURE)
            } else {
                'Jika running tidak menggunakan embed, maka refresh saja'
                WebUI.refresh()
            }
            
            'Diberikan delay 1 sec dikarenakan agar Lastest dapat diambil'
            WebUI.delay(1)

            'Get row lastest'
            variableLastest = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-content-layout > div > div > div > div.content-wrapper.p-0 > app-dashboard1 > div:nth-child(3) > div > div > div.card-content > div > app-msx-datatable > section > ngx-datatable > div > datatable-footer > div > datatable-pager > ul li'))

            'get row lastest'
            modifyObjectBtnLastest = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/button_Lastest'), 
                'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-footer/div/datatable-pager/ul/li[' + 
                variableLastest.size()) + ']/a', true)

            'jika btn lastest dapat diclick'
            if (WebUI.verifyElementClickable(modifyObjectBtnLastest, FailureHandling.OPTIONAL)) {
                'Klik button Lastest'
                WebUI.click(modifyObjectBtnLastest, FailureHandling.OPTIONAL)
            }
            
            'modify page untuk previous. Ini akan digunakan jika datanya tidak ditemukan'
            modifyObjectBtnPrevious = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/button_Lastest'), 
                'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-footer/div/datatable-pager/ul/li[' + 
                2) + ']/a', true)

            'Jika ingin dilakukannya bulk sign'
            if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 53) == 'Yes') {
                'Ambil data dari excel mengenai total dokumen yang ditandatangani'
                totalDocSign = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 54).toInteger()
            } else if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 53) == 'No') {
                'Total document sign hanya 1 (single)'
                totalDocSign = 1
            }
            
            'Looping berdasarkan page agar bergeser ke page sebelumnya'
            for (int k = 1; k <= (variableLastest.size() - 4); k++) {
                'get row beranda'
                rowBeranda = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-content-layout > div > div > div > div.content-wrapper.p-0 > app-dashboard1 > div:nth-child(3) > div > div > div.card-content > div > app-msx-datatable > section > ngx-datatable > div > datatable-body datatable-row-wrapper'))

                'looping untuk mengambil seluruh row'
                for (int j = rowBeranda.size(); j >= 1; j--) {
                    'deklarasi arrayIndex untuk pemakaian'
                    arrayIndex = 0

                    'modify object text document template name di beranda'
                    modifyObjectTextDocumentTemplateName = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[4]/div/p', true)

                    'modify object text document template tipe di beranda'
                    modifyObjectTextDocumentTemplateTipe = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[3]/div/p', true)

                    'modify object btn TTD Dokumen di beranda'
                    modifyObjectCheckboxTtd = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[1]/div/div/input', true)

                    'modify object lbl tanggal permintaan'
                    modifyObjectTextTglPermintaan = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[6]/div/span', true)

                    'modify object nama pelanggan'
                    modifyObjectLblNamaPelanggan = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[5]/div/p', true)

                    'modify object text no kontrak di beranda'
                    modifyObjectTextRefNumber = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[2]/div/p', true)

                    'modify object test status tanda tangan di beranda'
                    modifyObjectTextStatusTtd = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[9]/div/p', true)

                    'modify object text proses ttd di beranda'
                    modifyObjectTextProsesTtd = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/text_NamaDokumen'), 
                        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[7]/div/p', true)

                    'Jika datanya match dengan db, mengenai referal number'
                    if (WebUI.verifyMatch(WebUI.getText(modifyObjectTextRefNumber), sendToSign[arrayIndex++], false, FailureHandling.OPTIONAL) == 
                    true) {
                        'check Kotak Masuk'
                        checkKotakMasuk(conneSign, emailSigner, sheet, modifyObjectTextRefNumber, modifyObjectTextDocumentTemplateTipe, 
                            modifyObjectTextDocumentTemplateName, modifyObjectTextTglPermintaan, modifyObjectTextStatusTtd, 
                            modifyObjectTextProsesTtd, j)

                        'jika btn lastest dapat diclick'
                        if (WebUI.verifyElementClickable(modifyObjectBtnLastest, FailureHandling.OPTIONAL)) {
                            'Klik button Lastest'
                            WebUI.click(modifyObjectBtnLastest, FailureHandling.OPTIONAL)
                        }
                        
                        'Mengenai tipe dokumen template'
                        checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTextDocumentTemplateTipe), sendToSign[
                                arrayIndex++], false, FailureHandling.OPTIONAL), ' pada tipe document template ')

                        'Mengenai tanggal permintaan'
                        checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTextTglPermintaan), sendToSign[
                                arrayIndex++], false, FailureHandling.OPTIONAL), ' pada tanggal permintaan ')

                        'Input document Template Name dan nomor kontrak dari UI'
                        documentTemplateName = WebUI.getText(modifyObjectTextDocumentTemplateName)

                        noKontrak = WebUI.getText(modifyObjectTextRefNumber)

                        'Klik checkbox tanda tangan'
                        WebUI.click(modifyObjectCheckboxTtd)

                        'Jika total Document Signnya lebih besar dari 1, datanya continue'
                        if (totalDocSign > 1) {
                            continue
                        } else {
                            'Jika total document signnya itu 1, maka perlu break'
                            break
                        }
                    }
                    
                    'Jika bulk sign'
                    if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 53) == 'Yes') {
                        'Jika loopingan sudah cukup untuk total doc sign'
                        if (j == (rowBeranda.size() - totalDocSign)) {
                            break
                        } else {
                            'Jika document Template Namenya masih kosong'
                            if (documentTemplateName == '') {
                                'Input document Template Name dan nomor kontrak dari UI'
                                documentTemplateName = WebUI.getText(modifyObjectTextDocumentTemplateName)

                                noKontrak = WebUI.getText(modifyObjectTextRefNumber)
                            } else {
                                'Input document Template Name dan nomor kontrak dari UI ditambah dengan delimiter ;'
                                documentTemplateName = ((WebUI.getText(modifyObjectTextDocumentTemplateName) + ';') + documentTemplateName)

                                noKontrak = ((WebUI.getText(modifyObjectTextRefNumber) + ';') + noKontrak)
                            }
                            
                            'Klik checkbox tanda tangan'
                            WebUI.click(modifyObjectCheckboxTtd)
                        }
                    }
                }
                
                'Jika masih tidak ditemukan datanya, maka'
                if (documentTemplateName == '') {
                    'Klik previous'
                    WebUI.click(modifyObjectBtnPrevious)
                } else {
                    'Jika sudah ditemukan, maka break'
                    break
                }
                
                'Jika sudah dilooping terakhir mengenai pagenya dan tetap tidak menemukan'
                if ((k == (variableLastest.size() - 4)) && (documentTemplateName == '')) {
                    'Input verifynya false dengan reason'
                    checkVerifyEqualorMatch(false, ' dengan alasan tidak ditemukannya Nomor Kontrak yang diinginkan.')

                    continue
                }
            }
            
            'Klik button Tanda tangan bulk'
            WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TTDBulk'))

            'Split document Template Name berdasarkan delimiter'
            documentTemplateNamePerDoc = documentTemplateName.split(';', -1)

            noKontrakPerDoc = noKontrak.split(';', -1)

            'diberi delay 2 detik untuk muncul popup'
            WebUI.delay(2)

            'check popup'
            if (checkPopup() == true) {
                'break untuk looping selanjutnya'
                flagBreak = 1

                'diberi break dengan alasan sequential signing'
                break
            }

            'Jika total document sign excel tidak sama dengan total document sign paging'
            if (totalDocSign != documentTemplateNamePerDoc.size()) {
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    (((((((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedTotalDocTidakSesuai) + 
                    '<') + documentTemplateNamePerDoc.size()) + '> pada User ') + '<') + (emailSigner[(o - 1)])) + '>')
            }
            
            'Looping berdasarkan total document sign'
            for (int c = 0; c < documentTemplateNamePerDoc.size(); c++) {
                'modify object btn Nama Dokumen '
                modifyObjectbtnNamaDokumen = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/btn_NamaDokumen'), 
                    'xpath', 'equals', ('id("ngb-nav-' + c) + '")', true, FailureHandling.CONTINUE_ON_FAILURE)

                'verify nama dokumen massal dengan nama dokumen di paging'
                if (WebUI.verifyMatch(WebUI.getText(modifyObjectbtnNamaDokumen), documentTemplateNamePerDoc[(documentTemplateNamePerDoc.size() - 
                    (c + 1))], false, FailureHandling.CONTINUE_ON_FAILURE) == false) {
                    'Jika tidak cocok, maka custom keywords jika tidak sama.'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusWarning, ((((((((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                            2) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + ' dimana tidak sesuai di page Bulk Sign antara ') + 
                        '<') + WebUI.getText(modifyObjectbtnNamaDokumen)) + '> dengan ') + '<') + (documentTemplateNamePerDoc[
                        c])) + '>')
                }
            }
            
            'Check konfirmasi tanda tangan'
            checkKonfirmasiTTD()

            'jika page belum pindah'
            if (!(WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_TandaTanganDokumen'), GlobalVariable.TimeOut, 
                FailureHandling.OPTIONAL))) {
                'Jika tidak ada, maka datanya tidak ada, atau save gagal'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + 
                    GlobalVariable.ReasonFailedSaveGagal) + ' dengan alasan page tidak berpindah di Bulk Sign View.')
            } else {
                'Looping berdasarkan document template name per dokumen'
                for (int i = 0; i < documentTemplateNamePerDoc.size(); i++) {
                    'Jika page sudah berpindah maka modify object text document template name di Tanda Tangan Dokumen'
                    modifyObjectlabelnamadokumenafterkonfirmasi = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/lbl_NamaDokumenAfterKonfirmasi'), 
                        'xpath', 'equals', ('//*[@id="pdf-main-container"]/div[1]/ul/li[' + (i + 1)) + ']/label', true)

                    'verify nama dokumen dengan nama dokumen di paging'
                    checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectlabelnamadokumenafterkonfirmasi), 
                            documentTemplateNamePerDoc[(documentTemplateNamePerDoc.size() - (i + 1))], false), ' pada nama dokumen setelah konfirmasi ')
                }
            }
            
            'Scroll ke btn Proses'
            WebUI.scrollToElement(findTestObject('KotakMasuk/Sign/btn_Proses'), GlobalVariable.TimeOut)

            'Klik button proses'
            WebUI.click(findTestObject('KotakMasuk/Sign/btn_Proses'))

            'mereset array index'
            arrayIndex = 0

            'check error log'
            if (checkErrorLog() == true) {
                break
            }
            
            'Jika error log tidak muncul, Jika verifikasi penanda tangan tidak muncul'
            if (!(WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_VerifikasiPenandaTangan'), GlobalVariable.TimeOut, 
                FailureHandling.OPTIONAL))) {
                'Custom keyword mengenai savenya gagal'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + 
                    GlobalVariable.ReasonFailedSaveGagal) + ' pada saat tidak muncul pop up Verifikasi Penanda Tangan')
            } else {
                'Jika verifikasi penanda tangan muncul, Verifikasi antara email yang ada di UI dengan db'
                checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('KotakMasuk/Sign/input_EmailAfterKonfirmasi'), 
                            'value'), emailSigner[(o - 1)], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada email Signer')

                'Get text nomor telepon'
                noTelpSigner = WebUI.getAttribute(findTestObject('KotakMasuk/Sign/input_phoneNoAfterKonfirmasi'), 'value')

                'input text password'
                WebUI.setText(findTestObject('KotakMasuk/Sign/input_KataSandiAfterKonfirmasi'), findTestData(excelPathFESignDocument).getValue(
                        GlobalVariable.NumofColm, 58))

                'klik buka * pada passworod'
                WebUI.click(findTestObject('KotakMasuk/Sign/btn_EyePassword'))

                'verifikasi objek text yang diambil valuenya dengan password'
                checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('KotakMasuk/Sign/input_KataSandiAfterKonfirmasi'), 
                            'value'), findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 58), false), 
                    'pada Kata Sandi Signer')

                'verifikasi objek text yang diambil valuenya dengan nomor telepon'
                checkVerifyEqualorMatch(WebUI.verifyMatch(CustomKeywords.'customizekeyword.ParseText.convertToSHA256'(noTelpSigner), 
                        CustomKeywords.'connection.APIFullService.getHashedNo'(conneSign, emailSigner[(o - 1)]), false), 
                    'pada nomor telepon Signer')

				'jika metode verifikasi tidak muncul'
				if (verifMethod.equalsIgnoreCase('Biometric')) {
					
					'cek apakah button biom tidak muncul'
					if (!WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/btn_verifBiom'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
						
						'cek apakah mau ganti method'
						if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Unavailable')) == 'Yes') {
							
							'panggil fungsi penyelesaian dengan OTP'
							if (verifOTPMethod(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
								
								'jika ada error continue testcase'
								continue
							}
							
							alreadyVerif = 1
						} else {
							
							'tulis excel error dan lanjutkan testcase'
							'jika muncul, tulis error ke excel'
							CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
								GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
									2).replace('-', '') + ';') + 'Verifikasi '+ verifMethod + ' tidak tersedia'))
							
							continue
						}
					}
				} else if (verifMethod.equalsIgnoreCase('OTP')) {
					
					'cek apakah button otp tidak muncul'
					if (!WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/btn_verifOTP'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
						
						'cek apakah mau ganti method'
						if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Unavailable')) == 'Yes') {
							
							'panggil fungsi verif menggunakan biometrik'
							if(verifBiomMethod(maxFaceCompDB, countLivenessFaceComp, conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
								
								'jika ada error break testcase'
								break
							}
							
							alreadyVerif = 1
						} else {
							
							'tulis excel error dan lanjutkan testcase'
							'jika muncul, tulis error ke excel'
							CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
								GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
									2).replace('-', '') + ';') + 'Verifikasi OTP tidak tersedia'))
							
							continue
						}
					}
				}
				
				'jika case privy dan mustliveness aktif serta diatas limit'
				if (vendor.equalsIgnoreCase('Privy') || (mustFaceCompDB == '1' && countLivenessFaceComp >= maxFaceCompDB) && alreadyVerif == 0) {
					
					'pastikan tombol verifikasi biometrik tidak muncul'
					if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/btn_verifBiom'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
						GlobalVariable.FlagFailed = 1
						
						'jika muncul, tulis error ke excel'
						CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
							GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
								2).replace('-', '') + ';') + 'Tombol Liveness muncul saat mustLiveness aktif dan limit sudah terpenuhi'))
					}
					
					'jika tidak sesuai kondisi'
					if (vendor.equalsIgnoreCase('Privy') && verifMethod.equalsIgnoreCase('Biometric')) {
						
						'jika muncul, tulis error ke excel'
						CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
							GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
								2).replace('-', '') + ';') + 'Privy tidak mensupport verifikasi Biometric'))
						
						continue
					}
					
					'panggil fungsi penyelesaian dengan OTP'
					if (verifOTPMethod(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
						
						'cek apakah ingin coba metode lain'
						if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Failed?')) == 'Yes') {
							
							'klik tombol untuk kembali ke laman proses'
							WebUI.click(findTestObject('KotakMasuk/Sign/button_BackOTP'))
							
							inputDataforVerif()
							
							'panggil fungsi verif menggunakan biometrik'
							if(verifBiomMethod(maxFaceCompDB, countLivenessFaceComp, conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
								
								'jika ada error break testcase'
								break
							}
							
						} else {
							
							'jika ada error continue testcase'
							continue
						}
					}
					
				} else if (mustFaceCompDB == '1' && countLivenessFaceComp < maxFaceCompDB && alreadyVerif == 0) {
					
					'pastikan button otp tidak ada'
					checkVerifyEqualorMatch(WebUI.verifyElementNotPresent(findTestObject('KotakMasuk/Sign/btn_verifOTP'),
						GlobalVariable.TimeOut, FailureHandling.OPTIONAL), 'Tombol OTP muncul pada Vendor selain Privy yang mewajibkan FaceCompare')
					
					'panggil fungsi verif menggunakan biometrik'
					if(verifBiomMethod(maxFaceCompDB, countLivenessFaceComp, conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
						
						'cek apakah ingin coba metode lain'
						if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Failed?')) == 'Yes') {
							
							'klik tombol untuk kembali ke laman proses'
							WebUI.click(findTestObject('KotakMasuk/Sign/button_KembaliBiom'))
							
							inputDataforVerif()
							
							'panggil fungsi penyelesaian dengan OTP'
							if (verifOTPMethod(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
								
								'jika ada error continue testcase'
								continue
							}
							
						} else {
							
							'jika ada error break testcase'
							break
						}
					}
				} else if (alreadyVerif == 0) {
					
					'Jika cara verifikasinya menggunakan OTP'
					if (verifMethod == 'OTP') {
						
						'panggil fungsi penyelesaian dengan OTP'
						if (verifOTPMethod(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
							
							'cek apakah ingin coba metode lain'
							if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Failed?')) == 'Yes') {
								
								'klik tombol untuk kembali ke laman proses'
								WebUI.click(findTestObject('KotakMasuk/Sign/button_BackOTP'))
								
								inputDataforVerif()
								
								'panggil fungsi verif menggunakan biometrik'
								if(verifBiomMethod(maxFaceCompDB, countLivenessFaceComp, conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
									
									'jika ada error break testcase'
									break
								}
								
							} else {
								
								'jika ada error continue testcase'
								continue
							}
						}
						
					} else {
						
						'panggil fungsi verif menggunakan biometrik'
						if(verifBiomMethod(maxFaceCompDB, countLivenessFaceComp, conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
							
							'cek apakah ingin coba metode lain'
							if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Force Change Method if other Method Failed?')) == 'Yes') {
								
								'klik tombol untuk kembali ke laman proses'
								WebUI.click(findTestObject('KotakMasuk/Sign/button_KembaliBiom'))
								
								inputDataforVerif()
								
								'panggil fungsi penyelesaian dengan OTP'
								if (verifOTPMethod(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
									
									'jika ada error continue testcase'
									continue
								}
								
							} else {
								
								'jika ada error break testcase'
								break
							}
						}
					}
				}
                
                'Jika label verifikasi mengenai popup berhasil dan meminta masukan ada'
                if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_VerifikasiOTPBerhasildanMasukan'), GlobalVariable.TimeOut, 
                    FailureHandling.CONTINUE_ON_FAILURE)) {
                    'Diberikan delay 4 sec dikarenakan loading'
                    WebUI.delay(4)

                    'Mendapat total success dan failed'
                    String countSuccessSign = WebUI.getText(findTestObject('KotakMasuk/Sign/lbl_success'))

                    String countFailedSign = WebUI.getText(findTestObject('KotakMasuk/Sign/lbl_Failed'))

                    'Menarik value count success ke excel'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 75, GlobalVariable.NumofColm - 
                        1, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 76) + ';') + '<' + countSuccessSign + '>')

                    'Menarik value count failed ke excel'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 76, GlobalVariable.NumofColm - 
                        1, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 77) + ';') + '<' + countFailedSign + '>')

                    'Jika masukan ratingnya tidak kosong'
                    if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 66) != '') {
                        'modify object starmasukan, jika bintang 1 = 2, jika bintang 2 = 4'
                        modifyObjectstarMasukan = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/span_starMasukan'), 
                            'xpath', 'equals', ('//ngb-rating[@id=\'rating\']/span[' + (findTestData(excelPathFESignDocument).getValue(
                                GlobalVariable.NumofColm, 66).toInteger() * 2)) + ']/span', true)

                        'Klik bintangnya bintang berapa'
                        WebUI.click(modifyObjectstarMasukan)
                    }
                    
                    'Jika komentarnya tidak kosoong'
                    if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 67) != '') {
                        'Input komentar di rating'
                        WebUI.setText(findTestObject('KotakMasuk/Sign/input_komentarMasukan'), findTestData(excelPathFESignDocument).getValue(
                                GlobalVariable.NumofColm, 67))
                    }
                    
                    'Scroll ke btn Kirim'
                    WebUI.scrollToElement(findTestObject('KotakMasuk/Sign/btn_Kirim'), GlobalVariable.TimeOut)

                    'klik button Kirim'
                    WebUI.click(findTestObject('KotakMasuk/Sign/btn_Kirim'))

                    if (checkErrorLog() == true) {
                        continue
                    }
                    
                    'Verifikasi label pop up ketika masukan telah selesai dikirim'
                    if (!(WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_popupmasukan'), GlobalVariable.TimeOut))) {
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                            GlobalVariable.StatusFailed, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                                2).replace('-', '') + ';') + GlobalVariable.ReasonFailedFeedbackGagal)
                    } else {
                        'Klik OK'
                        WebUI.click(findTestObject('/KotakMasuk/Sign/button_OK'))
                    }
                    
                    'Jika masukan ratingnya tidak kosong'
                    if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 66) != '') {
                        'StoreDB mengenai masukan'
                        masukanStoreDB(conneSign, emailSigner[(o - 1)], arrayMatch)
                    }
                    
                    if (GlobalVariable.FlagFailed == 0) {
                        'write to excel success'
                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 0, 
                            GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
                    }
                    
                    'Mensplit nomor kontrak yang telah disatukan'
                    noKontrakPerDoc = noKontrak.split(';', -1)

                    'looping untuk mendapatkan total saldo yang digunakan per nomor kontrak'
                    for (i = 0; i < noKontrakPerDoc.size(); i++) {
                        'Mengambil value dari db mengenai tipe pembayran'
                        paymentType = CustomKeywords.'connection.APIFullService.getPaymentType'(conneSign, noKontrakPerDoc[
                            i])
						
						if (i == 0) {
							saldoUsedDocPertama = (saldoUsedDocPertama + CustomKeywords.'connection.APIFullService.getSaldoUsedBasedonPaymentType'(
								conneSign, noKontrakPerDoc[i], emailSigner[(o - 1)]))
						}
						
                        'Jika tipe pembayarannya per sign'
                        if (paymentType == 'Per Sign') {
                            'Saldo usednya akan ditambah dengan value db penggunaan saldo'
                            saldoUsed = (saldoUsed + CustomKeywords.'connection.APIFullService.getSaldoUsedBasedonPaymentType'(
                                conneSign, noKontrakPerDoc[i], emailSigner[(o - 1)]))
                        } else {
                            saldoUsed = (saldoUsed + 1)
                        }
                    }
                    
                    'Jumlah signer tanda tangan akan ditambah dengan total saldo yang telah digunakan'
                    jumlahSignerTandaTangan = (jumlahSignerTandaTangan + saldoUsed)

                    'Looping maksimal 100 detik untuk signing proses. Perlu lama dikarenakan walaupun requestnya done(3), tapi dari VIDAnya tidak secepat itu.'
                    for (int y = 1; y <= 5; y++) {
                        'Kita berikan delay per 20 detik karena proses signingnya masih dalam status In Progress (1), dan ketika selesai, status tanda tangan akan kembali menjadi 0'
                        WebUI.delay(20)

                        'Jika signing process db untuk signing false, maka'
                        if (signingProcessStoreDB(conneSign, emailSigner[(o - 1)], saldoUsedDocPertama) == false) {
                            'Jika looping waktu delaynya yang terakhir, maka'
                            if (y == 5) {
                                'Failed dengan alasan prosesnya belum selesai'
                                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                    GlobalVariable.StatusFailed, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                                        2) + ';') + GlobalVariable.ReasonFailedProcessNotDone)
                            }
                        } else {
                            'Jika hasil store dbnya true, maka'
                            break
                        }
                    }
                    
                    'Browser ditutup'
                    WebUI.closeBrowser()
                } else {
                    'Jika popup berhasilnya tidak ada, maka Savenya gagal'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                            2) + ';') + GlobalVariable.ReasonFailedSaveGagal) + ' dengan alasan tidak muncul page Berhasil mengirimkan permintaan tanda tangan dokumen.')

                    continue
                }
            }
            
            'Memanggil DocumentMonitoring untuk dicheck apakah documentnya sudah masuk'
            WebUI.callTestCase(findTestCase('Document Monitoring/VerifyDocumentMonitoring'), [('excelPathFESignDocument') : excelPathFESignDocument
                    , ('sheet') : sheet, ('linkDocumentMonitoring') : linkDocumentMonitoring, ('nomorKontrak') : noKontrak,
					('CancelDocsSign') : findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Sign?'))], 
                	FailureHandling.CONTINUE_ON_FAILURE)
			
			'jika ada proses cancel doc'
			if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Sign?')) == 'Yes') {
				
				'lanjutkan loop'
				continue
			}

			'panggil fungsi login'
			WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('SheetName') : sheet,
				('Path') : excelPathFESignDocument, ('Email') : 'Email Login', ('Password') : 'Password Login',
				 ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.CONTINUE_ON_FAILURE)

            'Split dokumen template name dan nomor kontrak per dokumen berdasarkan delimiter ;'
            documentTemplateNamePerDoc = documentTemplateName.split(';', -1)

            noKontrakPerDoc = noKontrak.split(';', -1)

			'beri maks 30 sec mengenai perubahan total sign'
			for (int b = 1; b <= 3; b++) {
				'ambil saldo after'
				HashMap<String, String> saldoAfter = checkSaldo(saldoList, vendor)

				'ambil saldo after'
				saldoSignAfter = checkSaldoSign(conneSign, vendor)
				
				'Jika count saldo sign/ttd diatas (after) sama dengan yang dulu/pertama (before) dikurang jumlah dokumen yang ditandatangani'
				if (WebUI.verifyEqual(Integer.parseInt(saldoSignBefore) - saldoUsed, Integer.parseInt(saldoSignAfter),
					FailureHandling.OPTIONAL)) {
					
					'cek apa pernah menggunakan biometrik'
					if (useBiom == 0) {
						
						'Jika count saldo otp after dengan yang before dikurangi 1 ditambah dengan '
						if(WebUI.verifyEqual(Integer.parseInt(saldoBefore.get('OTP')) - (countResend), Integer.parseInt(saldoAfter.get('OTP')), FailureHandling.OPTIONAL)) {
							
							break
						}
	
					} else if (useBiom == 1){
						
						'cek saldo liveness facecompare dipisah atau tidak'
						String isSplitLivenessFc = CustomKeywords.'connection.APIFullService.getSplitLivenessFaceCompareBill'(conneSign)
						
						'jika saldo liveness digabung dengan facecompare'
						if (isSplitLivenessFc == '0') {
							
							'cek apakah saldo liveness facecompare masih sama'
							if(WebUI.verifyEqual(Integer.parseInt(saldoBefore.get('Liveness Face Compare')) - 1, Integer.parseInt(saldoAfter.get('Liveness Face Compare')), FailureHandling.OPTIONAL)) {
								
								break
							}
						}
						else if (isSplitLivenessFc == '1') {
							
							'cek apakah saldo liveness dan facecompare sama'
							if(WebUI.verifyEqual(Integer.parseInt(saldoBefore.get('Liveness')) - (countSaldoSplitLiveFCused), Integer.parseInt(saldoAfter.get('Liveness')), FailureHandling.OPTIONAL) &&
								WebUI.verifyEqual(Integer.parseInt(saldoBefore.get('Face Compare')) - (countSaldoSplitLiveFCused), Integer.parseInt(saldoAfter.get('Face Compare')), FailureHandling.OPTIONAL)) {
								
								break
							}
						}
					}
				}
				
				'Masih sama, dikasi waktu delay 10'
				WebUI.delay(10)

				WebUI.refresh()
			}
            
            'looping berdasarkan total dokumen dari dokumen template code'
            for (int i = 0; i < noKontrakPerDoc.size(); i++) {
                'Input filter di Mutasi Saldo'
                inputFilterTrx(conneSign, currentDate, noKontrakPerDoc[i], documentTemplateNamePerDoc[i])

                'Mengambil value dari db mengenai tipe pembayran'
                paymentType = CustomKeywords.'connection.APIFullService.getPaymentType'(conneSign, noKontrakPerDoc[i])

                'Jika tipe pembayarannya per sign'
                if (paymentType == 'Per Sign') {
                    'Memanggil saldo total yang telah digunakan per dokumen tersebut'
                    saldoUsedperDoc = CustomKeywords.'connection.APIFullService.getTotalSignedUsingRefNumber'(conneSign, 
                        noKontrakPerDoc[i])

                    if (saldoUsedperDoc == '0') {
                        WebUI.delay(10)

                        'Memanggil saldo total yang telah digunakan per dokumen tersebut'
                        saldoUsedperDoc = CustomKeywords.'connection.APIFullService.getTotalSignedUsingRefNumber'(conneSign, 
                            noKontrakPerDoc[i])
                    }
                } else {
                    saldoUsedperDoc = o
                }
                
                'delay dari 10 sampe 60 detik'
                for (int d = 1; d <= 6; d++) {
                    'Jika dokumennya ada, maka'
                    if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_notrxsaldo'), GlobalVariable.TimeOut, 
                        FailureHandling.OPTIONAL)) {
                        'get column di saldo'
                        variableSaldoColumn = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-header > div > div.datatable-row-center.ng-star-inserted datatable-header-cell'))

                        'get row di saldo'
                        variableSaldoRow = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

                        'ambil inquiry di db'
                        ArrayList<String> inquiryDB = CustomKeywords.'connection.APIFullService.gettrxSaldo'(conneSign, 
                            noKontrakPerDoc[i], saldoUsedperDoc.toString())

                        index = 0

                        'check total row dengan yang tertandatangan'
                        checkVerifyEqualorMatch(WebUI.verifyMatch(variableSaldoRow.size().toString(), saldoUsedperDoc.toString(), 
                                false, FailureHandling.CONTINUE_ON_FAILURE), ' pada jumlah tertanda tangan dengan row transaksi ')

                        'looping mengenai rownya'
                        for (int j = 1; j <= variableSaldoRow.size(); j++) {
                            'looping mengenai columnnya'
                            for (int u = 1; u <= variableSaldoColumn.size(); u++) {
                                'modify per row dan column. column menggunakan u dan row menggunakan documenttemplatename'
                                modifyperrowpercolumn = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/lbl_notrxsaldo'), 
                                    'xpath', 'equals', ((('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                                    j) + ']/datatable-body-row/div[2]/datatable-body-cell[') + u) + ']/div', true)

                                'Jika u di lokasi qty atau kolom ke 9'
                                if (u == 9) {
                                    'Jika yang qtynya 1 dan databasenya juga, berhasil'
                                    if ((WebUI.getText(modifyperrowpercolumn) == '1') || ((inquiryDB[(u - 1)]) == '-1')) {
                                        'Jika bukan untuk 2 kolom itu, maka check ke db'
                                        checkVerifyEqualorMatch(WebUI.verifyMatch('-' + WebUI.getText(modifyperrowpercolumn), 
                                                inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), 'pada Kuantitas di Mutasi Saldo dengan nomor kontrak ' + 
                                            (noKontrakPerDoc[i]))
                                    } else {
                                        'Jika bukan -1, atau masih 0. Maka ttdnya dibilang error'
                                        GlobalVariable.FlagFailed = 1

                                        'Jika saldonya belum masuk dengan flag, maka signnya gagal.'
                                        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                            GlobalVariable.StatusFailed, (((((findTestData(excelPathFESignDocument).getValue(
                                                GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedSignGagal) + 
                                            ' terlihat pada Kuantitas di Mutasi Saldo dengan nomor kontrak ') + '<') + (noKontrakPerDoc[
                                            i])) + '>')
                                    }
                                } else if (u == variableSaldoColumn.size()) {
                                    'Jika di kolom ke 10, atau di FE table saldo, check saldo dari table dengan saldo yang sekarang. Takeout dari dev karena no issue dan sudah sepakat'
                                 //   checkVerifyEqualorMatch(WebUI.verifyEqual(Integer.parseInt(WebUI.getText(modifyperrowpercolumn)), 
                                 //           Integer.parseInt(saldoSignBefore) - saldoUsedperDoc, FailureHandling.CONTINUE_ON_FAILURE), 
                                 //       ' pada Saldo di Mutasi Saldo dengan nomor kontrak ' + (noKontrakPerDoc[i]))
                                } else {
                                    'Jika bukan untuk 2 kolom itu, maka check ke db'
                                    checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyperrowpercolumn), inquiryDB[
                                            index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Mutasi Saldo dengan nomor kontrak ' + 
                                        (noKontrakPerDoc[i]))
                                }
                            }
                        }
                        
                        break
                    } else {
                        'jika kesempatan yang terakhir'
                        if (d == 6) {
                            'Jika masih tidak ada'
                            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                GlobalVariable.StatusFailed, (((((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                                    2).replace('-', '') + ';') + GlobalVariable.ReasonFailedNoneUI) + ' dengan nomor kontrak ') + 
                                '<') + (noKontrakPerDoc[i])) + '>')
                        }
                        
                        'delay 10 detik'
                        WebUI.delay(10)

                        'Klik cari'
                        WebUI.click(findTestObject('Saldo/btn_cari'))
                    }
                }
            }
        }
        
        'check flagBreak untuk sequential'
        if (flagBreak == 1) {
            continue
        }
        
        'Jika ingin melakukan stamping'
        if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 90) == 'Yes') {
            'Call API Stamping'
            WebUI.callTestCase(findTestCase('InactiveTC/Flow Stamping'), [('excelPathStamping') : excelPathFESignDocument, ('sheet') : sheet
                    , ('useAPI') : 'v3.1.0', ('linkDocumentMonitoring') : linkDocumentMonitoring,
					('CancelDocsStamp') : findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Stamp?'))], FailureHandling.CONTINUE_ON_FAILURE)
			
			'jika ada proses cancel doc'
			if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Stamp?')) == 'Yes') {
				
				'lanjutkan loop'
				continue
			}
        }
    }
}

' penggunaan ini hanya untuk Masukan Store Db'
if (arrayMatch.contains(false)) {
    'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
        ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + GlobalVariable.ReasonFailedStoredDB) + 
        ' untuk Masukan Store DB')
}

def encryptLink(String officeCode, String emailSigner, String aesKey) {
    'pembuatan message yang akan dienkrip'
    msg = (((('{"officeCode" : ' + officeCode) + ', "email" : "') + emailSigner) + '"}')

    'enkripsi msg'
    encryptMsg = CustomKeywords.'customizekeyword.ParseText.parseEncrypt'(msg, aesKey)

    return encryptMsg
}

def checkVerifyEqualorMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write to excel status failed and ReasonFailedVerifyEqualorMatch'
        GlobalVariable.FlagFailed = 1

        'Jika equalnya salah maka langsung berikan reason bahwa reasonnya failed'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + 
            reason)

        return false
    }
    
    return true
}

def verifOTPMethod(Connection conneSign, ArrayList emailSigner, ArrayList listOTP, int o, String noTelpSigner, ArrayList otpAfter) {
	
	'Klik verifikasi by OTP'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_verifOTP'))

	'Memindahkan variable ke findTestObject'
	modifyObjectlabelRequestOTP = findTestObject('KotakMasuk/Sign/lbl_RequestOTP')

	'Jika button menyetujuinya yes'
	if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Menyetujui(Yes/No)')) == 'Yes') {
		'Klik button menyetujui untuk menandatangani'
		WebUI.click(findTestObject('KotakMasuk/Sign/btn_MenyetujuiMenandatangani'))
	}
	
	'Jika btn lanjut setelah konfirmasi untuk mengarah ke otp dapat diklik'
	if (WebUI.verifyElementClickable(findTestObject('KotakMasuk/Sign/btn_LanjutAfterKonfirmasi'), FailureHandling.OPTIONAL)) {
		'Klik lanjut after konfirmasi'
		WebUI.click(findTestObject('KotakMasuk/Sign/btn_LanjutAfterKonfirmasi'), FailureHandling.OPTIONAL)
	} else {
		'Jika btn lanjut setelah konfirmasi untuk mengarah ke otp tidak dapat diklik'

		'Failed alasan save gagal tidak bisa diklik.'
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
			GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
				2).replace('-', '') + ';') + GlobalVariable.ReasonFailedSaveGagal) + ' dengan alasan tidak bisa lanjut proses OTP')

		'kembali ke loop atas'
		return false
	}
	
	'check error log'
	if (checkErrorLog() == true) {
		return false
	}
	
	'Jika tidak muncul untuk element selanjutnya'
	if (!(WebUI.verifyElementPresent(modifyObjectlabelRequestOTP, GlobalVariable.TimeOut))) {
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
			GlobalVariable.StatusFailed, ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
				2).replace('-', '') + ';') + GlobalVariable.ReasonFailedSaveGagal) + ' dengan alasan tidak muncul page input OTP')
	} else {
		
		if (verifOTPMethodDetail(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
			
			return false
		}
	}
	
	'check error log'
	if (checkErrorLog() == true) {
		return false
	}
	
	'check pop up'
	if (checkPopup() == true) {
		return false
	}
}

def inputDataforVerif() {
	
	'Scroll ke btn Proses'
	WebUI.scrollToElement(findTestObject('KotakMasuk/Sign/btn_Proses'), GlobalVariable.TimeOut)

	'Klik button proses'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_Proses'))
	
	'input text password'
	WebUI.setText(findTestObject('KotakMasuk/Sign/input_KataSandiAfterKonfirmasi'), findTestData(excelPathFESignDocument).getValue(
			GlobalVariable.NumofColm, rowExcel('PasswordOTP')))

	'klik buka * pada password'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_EyePassword'))
}

def verifOTPMethodDetail(Connection conneSign, ArrayList emailSigner, ArrayList listOTP, int o, String noTelpSigner, ArrayList otpAfter) {
	
	'ubah pemakaian biom menjadi false'
	useBiom = 0
	
	'Verifikasi antara no telp yang dinput dengan yang sebelumnya'
	checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('KotakMasuk/Sign/lbl_phoneNo'),
				'value'), noTelpSigner, false), '')

	'OTP yang pertama dimasukkan kedalam 1 var'
	OTP = CustomKeywords.'connection.DataVerif.getOTPAktivasi'(conneSign, emailSigner[(o - 1)])

	'clear arraylist sebelumnya'
	listOTP.clear()

	'add otp ke list'
	listOTP.add(OTP)

	'bikin flag untuk dilakukan OTP by db'
	if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Correct OTP (Yes/No)')) == 'Yes') {
		'value OTP dari db'
		WebUI.setText(findTestObject('KotakMasuk/Sign/input_OTP'), OTP)
	} else {
		'value OTP dari excel'
		WebUI.setText(findTestObject('KotakMasuk/Sign/input_OTP'), findTestData(excelPathFESignDocument).getValue(
				GlobalVariable.NumofColm, rowExcel('Manual OTP')))
	}
	
	'klik verifikasi OTP'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_ProsesOTP'))

	'Kasih delay 1 detik karena proses OTP akan trigger popup, namun loading. Tidak instan'
	WebUI.delay(1)

	'check pop up'
	if (checkPopup() == true) {
		return false
	}
	
	'Resend OTP'
	if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Resend OTP (Yes/No)')) == 'Yes') {
		'Ambil data dari excel mengenai countResend'
		countResend = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('CountResendOTP')).toInteger()

		'Looping dari 1 hingga total count resend OTP'
		for (int w = 1; w <= countResend; w++) {
			'berikan waktu delay'
			WebUI.delay(115)

			'Klik resend otp'
			WebUI.click(findTestObject('KotakMasuk/Sign/btn_ResendOTP'))

			'Memberikan delay 3 karena OTP after terlalu cepat'
			WebUI.delay(3)

			'OTP yang kedua'
			otpAfter = CustomKeywords.'connection.DataVerif.getOTPAktivasi'(conneSign, emailSigner[(o -
				1)])

			'add otp ke list'
			listOTP.add(otpAfter)

			'dicheck OTP pertama dan kedua dan seterusnya'
			if (WebUI.verifyMatch(listOTP[(w - 1)], listOTP[w], false, FailureHandling.CONTINUE_ON_FAILURE)) {
				CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
					GlobalVariable.StatusFailed, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
						2).replace('-', '') + ';') + GlobalVariable.ReasonFailedOTPError)
			}
			
			'Jika looping telah diterakhir, baru set text'
			if (w == countResend) {
				'value OTP dari db'
				WebUI.setText(findTestObject('KotakMasuk/Sign/input_OTP'), otpAfter, FailureHandling.CONTINUE_ON_FAILURE)

				'klik verifikasi OTP'
				WebUI.click(findTestObject('KotakMasuk/Sign/btn_ProsesOTP'))
			}
		}
	} else {
		'tidak ada resend, namun menggunakan send otp satu kali'
		countResend = 1
	}
}

def verifBiomMethod(int maxFaceCompDB, int countLivenessFaceComp, Connection conneSign, ArrayList emailSigner, ArrayList listOTP, int o, String noTelpSigner, ArrayList otpAfter) {
	useBiom = 1
	
	'Klik biometric object'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_verifBiom'))
	
	'button menyetujui'
	if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Menyetujui(Yes/No)')) == 'Yes') {
		'Klik button menyetujui untuk menandatangani'
		WebUI.click(findTestObject('KotakMasuk/Sign/btn_MenyetujuiMenandatangani'))
	}
	
	'Klik lanjut after konfirmasi'
	WebUI.click(findTestObject('KotakMasuk/Sign/btn_LanjutAfterKonfirmasi'), FailureHandling.OPTIONAL)
	
	'jika localhost aktif'
	if (isLocalhost == 1) {
	
		'tap allow camera'
		MobileBuiltInKeywords.tapAndHoldAtPosition(895, 1364, 3)
	}
	
	'looping hingga count sampai batas maksimal harian'
	while(countLivenessFaceComp != (maxFaceCompDB + 1)) {
		
		'klik untuk ambil foto'
		WebUI.click(findTestObject('KotakMasuk/Sign/btn_ProsesBiom'))
		
		'jika error muncul'
		if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_popup'), 60, FailureHandling.OPTIONAL)) {
			
			'ambil message error'
			String messageError = WebUI.getText(findTestObject('KotakMasuk/Sign/lbl_popup'))
			
			if(messageError.equalsIgnoreCase('Percobaan verifikasi wajah sudah melewati batas harian')) {
				
				countSaldoSplitLiveFCused++
				
				'klik tombol OK'
				WebUI.click(findTestObject('Object Repository/KotakMasuk/Sign/button_OK'))
				
				'klik tombol lanjut dengan OTP'
				WebUI.click(findTestObject('Object Repository/KotakMasuk/Sign/btn_LanjutdenganOTP'))
				
				'panggil fungsi verifOTP'
				if (verifOTPMethodDetail(conneSign, emailSigner, listOTP, o, noTelpSigner, otpAfter) == false) {
					
					return false
				}
			} else if (messageError.equalsIgnoreCase('Verifikasi user gagal. Foto Diri tidak sesuai.') ||
				messageError.equalsIgnoreCase('Lebih dari satu wajah terdeteksi. Pastikan hanya satu wajah yang terlihat')) {
				
				countSaldoSplitLiveFCused++
			}
			
			'ambil message error'
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
				GlobalVariable.StatusFailed, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm,
					2).replace('-', '') + ';') + '<' + messageError + '>')
			
			'klik pada tombol OK'
			WebUI.click(findTestObject('KotakMasuk/Sign/button_OK'))
			
			GlobalVariable.FlagFailed = 1
			
			'ambil terbaru count dari DB'
			countLivenessFaceComp = CustomKeywords.'connection.DataVerif.getCountFaceCompDaily'(conneSign, emailSigner[o-1])
			
		} else {
			
			break
		}
	}
}

def checkKonfirmasiTTD() {
    'Klik tanda tangan'
    WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TtdSemuaTandaTanganDokumen'))

    'Klik tidak untuk konfirmasi ttd'
    WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TidakTandaTanganDokumen'))

    'Klik tanda tangan'
    WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TtdSemuaTandaTanganDokumen'))

    'Klik ya untuk konfirmasi ttd'
    WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_YaTandaTanganDokumen'))
}

def masukanStoreDB(Connection conneSign, String emailSigner, ArrayList<String> arrayMatch) {
    'deklarasi arrayIndex untuk penggunakan selanjutnya'
    arrayIndex = 0

    'MasukanDB mengambil value dari hasil query'
    masukanDB = CustomKeywords.'connection.APIFullService.getFeedbackStoreDB'(conneSign, emailSigner)

    'verify rating'
    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 66), masukanDB[
            arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))

    'verify komentar'
    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 67), masukanDB[
            arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))
}

def signingProcessStoreDB(Connection conneSign, String emailSigner, int jumlahSignerTandaTangan) {
    'deklarasi arrayIndex untuk penggunakan selanjutnya'
    arrayIndex = 0

    'SigningDB mengambil value dari hasil query'
    signingDB = CustomKeywords.'connection.SendSign.getSigningStatusProcess'(conneSign, findTestData(excelPathFESignDocument).getValue(
            GlobalVariable.NumofColm, 6), emailSigner)

    'looping berdasarkan size dari signingDB'
    for (int t = 1; t <= signingDB.size(); t++) {
        ArrayList<String> arrayMatch = new ArrayList<String>()

        'verify request status. 3 berarti done request. Terpaksa hardcode karena tidak ada masternya untuk 3.'
        arrayMatch.add(WebUI.verifyMatch('3', signingDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))

        'verify sign date. Jika ada, maka teksnya Sudah TTD. Sign Date sudah dijoin ke email masing-masing, sehingga pengecekan apakah sudah sign atau belum ditandai disini'
        arrayMatch.add(WebUI.verifyMatch('Sudah TTD', signingDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))

        'verify total signed. Total signed harusnya seusai dengan variable jumlah signed'
        arrayMatch.add(WebUI.verifyEqual(jumlahSignerTandaTangan, Integer.parseInt(signingDB[arrayIndex++]), FailureHandling.CONTINUE_ON_FAILURE))

        'Jika arraymatchnya ada false'
        if (arrayMatch.contains(false)) {
            'mengembalikan false'
            return false
            
            'dibreak ke looping code'
            break
        } else {
            'jika semuanya true'

            'mengembalikan true'
            return true
            
            'dibreak ke looping code'
            break
        }
    }
}

def inputFilterTrx(Connection conneSign, String currentDate, String noKontrak, String documentTemplateName) {
    documentType = CustomKeywords.'connection.APIFullService.getDocumentType'(conneSign, noKontrak)

    'input filter dari saldo'
    WebUI.setText(findTestObject('Saldo/input_tipesaldo'), findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
            69))

    'Input enter'
    WebUI.sendKeys(findTestObject('Saldo/input_tipesaldo'), Keys.chord(Keys.ENTER))

    'Input tipe transaksi'
    WebUI.setText(findTestObject('Saldo/input_tipetransaksi'), findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
            70))

    'Input enter'
    WebUI.sendKeys(findTestObject('Saldo/input_tipetransaksi'), Keys.chord(Keys.ENTER))

    'Input date sekarang'
    WebUI.setText(findTestObject('Saldo/input_fromdate'), currentDate)

    'Input tipe dokumen'
    WebUI.setText(findTestObject('Saldo/input_tipedokumen'), documentType)

    'Input enter'
    WebUI.sendKeys(findTestObject('Saldo/input_tipedokumen'), Keys.chord(Keys.ENTER))

    'Input referal number'
    WebUI.setText(findTestObject('Saldo/input_refnumber'), noKontrak)

    'Input documentTemplateName'
    WebUI.setText(findTestObject('Saldo/input_namadokumen'), documentTemplateName)

    'Input date sekarang'
    WebUI.setText(findTestObject('Saldo/input_todate'), currentDate)

    'Klik cari'
    WebUI.click(findTestObject('Saldo/btn_cari'))
}

def checkSaldoSign(Connection conneSign, String vendor) {
    String totalSaldo
	
    'klik ddl untuk tenant memilih mengenai Vida'
    WebUI.selectOptionByLabel(findTestObject('Saldo/ddl_Vendor'), vendor, false)

    'get total div di Saldo'
    variableDivSaldo = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > div > div > div div'))

    'looping berdasarkan total div yang ada di saldo'
    for (int c = 1; c <= variableDivSaldo.size(); c++) {
        'modify object mengenai find tipe saldo'
        modifyObjectFindSaldoSign = WebUI.modifyObjectProperty(findTestObject('Saldo/lbl_saldo'), 'xpath', 'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + 
            (c + 1)) + ']/div/div/div/div/div[1]', true)

        'verifikasi label saldonya '
        if (WebUI.verifyElementText(modifyObjectFindSaldoSign, findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                72), FailureHandling.OPTIONAL)) {
            'modify object mengenai ambil total jumlah saldo'
            modifyObjecttotalSaldoSign = WebUI.modifyObjectProperty(findTestObject('Saldo/lbl_countsaldo'), 'xpath', 'equals', 
                ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + (c + 1)) + ']/div/div/div/div/div[2]', 
                true)

            'mengambil total saldo yang pertama'
            totalSaldo = WebUI.getText(modifyObjecttotalSaldoSign)

            break
        }
    }
    
    'return total saldo awal'
    return totalSaldo
    
    'tutup browsernya'
    WebUI.closeBrowser()
}

def checkSaldo(ArrayList rowName, String vendor) {
	HashMap<String, String> result = new HashMap<>()
	
	for (int b = 0; b < rowName.size(); b++) {
		
		'deklarasi totalSaldo'
		String totalSaldo = ''
		
		'cek apakah elemen menu ditutup'
		if (WebUI.verifyElementVisible(findTestObject('button_HamburberSideMenu'), FailureHandling.OPTIONAL)) {
			
			'klik pada button hamburber'
			WebUI.click(findTestObject('button_HamburberSideMenu'))
		}
	
		'klik button saldo'
		WebUI.click(findTestObject('isiSaldo/SaldoAdmin/menu_Saldo'))
		
		'cek apakah tombol x terlihat'
		if (WebUI.verifyElementVisible(findTestObject('buttonX_sideMenu'), FailureHandling.OPTIONAL)) {
			
			'klik pada button X'
			WebUI.click(findTestObject('buttonX_sideMenu'))
		}
	
		'cek apakah vendor merupakan privy'
		if (vendor.equalsIgnoreCase('Privy') && rowName[b].equals('OTP')) {
		
			'klik ddl untuk tenant memilih mengenai privy'
			WebUI.selectOptionByLabel(findTestObject('Saldo/ddl_Vendor'), vendor.toUpperCase(), false)
		} else {
			
			'klik ddl untuk tenant memilih mengenai VIDA'
			WebUI.selectOptionByLabel(findTestObject('Saldo/ddl_Vendor'), 'ESIGN/ADINS', false)
		}
	
		'get total div di Saldo'
		variableDivSaldo = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > div > div > div > div'))
		
		'looping berdasarkan total div yang ada di saldo'
		for (int c = 2; c <= variableDivSaldo.size(); c++) {
			
			'modify object mengenai find tipe saldo'
			modifyObjectFindSaldoSign = WebUI.modifyObjectProperty(findTestObject('Saldo/lbl_saldo'), 'xpath', 'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' +
				(c)) + ']/div/div/div/div/div[1]', true)
	
			'verifikasi label saldonya '
			if (WebUI.verifyElementText(modifyObjectFindSaldoSign, rowName[b], FailureHandling.OPTIONAL)) {
				'modify object mengenai ambil total jumlah saldo'
				modifyObjecttotalSaldoSign = WebUI.modifyObjectProperty(findTestObject('Saldo/lbl_countsaldo'), 'xpath', 'equals',
					('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + (c)) + ']/div/div/div/div/div[2]',
					true)
	
				'mengambil total saldo yang dipilih'
				totalSaldo = WebUI.getText(modifyObjecttotalSaldoSign)
				
				result.put(rowName[b], totalSaldo)
			}
		}
	}
	
	'return total saldo awal'
	return result
}

def checkPopup() {
    'Jika popup muncul'
    if (WebUI.verifyElementNotPresent(findTestObject('KotakMasuk/Sign/lbl_popup'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
    } else {
        'label popup diambil'
        lblpopup = WebUI.getText(findTestObject('KotakMasuk/Sign/lbl_popup'), FailureHandling.CONTINUE_ON_FAILURE)

        if (!(lblpopup.contains('Kode OTP salah'))) {
            'Tulis di excel sebagai failed dan error.'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                (((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + 
                '<') + lblpopup) + '>')

            return true
        }
        
        'Klik OK untuk popupnya'
        WebUI.click(findTestObject('KotakMasuk/Sign/errorLog_OK'))
    }
}

def checkPopupWarning() {
	'Jika popup muncul'
	if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/lbl_popup'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
		'label popup diambil'
		lblpopup = WebUI.getText(findTestObject('KotakMasuk/Sign/lbl_popup'), FailureHandling.CONTINUE_ON_FAILURE)

			'Tulis di excel sebagai failed dan error.'
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusWarning,
				(((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') +
				'<') + lblpopup) + '>')
			
		'Klik OK untuk popupnya'
		WebUI.click(findTestObject('KotakMasuk/Sign/errorLog_OK'))
		
		return true
	}
	
	return false
}

def checkErrorLog() {
    'Jika error lognya muncul'
    if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/errorLog'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
        'ambil teks errormessage'
        errormessage = WebUI.getAttribute(findTestObject('KotakMasuk/Sign/errorLog'), 'aria-label', FailureHandling.CONTINUE_ON_FAILURE)

        if (!(errormessage.contains('Verifikasi OTP berhasil')) && !(errormessage.contains('feedback'))) {
            'Tulis di excel itu adalah error'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                (((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + 
                '<') + errormessage) + '>')

            return true
        }
    }
    
    return false
}

def runWithEmbed(String linkUrl) {
    'check if ingin menggunakan embed atau tidak'
    if (GlobalVariable.RunWithEmbed == 'Yes') {
        'navigate url ke daftar akun'
        WebUI.openBrowser(GlobalVariable.embedUrl)

        'Diberikan delay 3 sec'
        WebUI.delay(3)

        'Maximize windows'
        WebUI.maximizeWindow()

        'Set text link Url'
        WebUI.setText(findTestObject('EmbedView/inputLinkEmbed'), linkUrl)

        'click button embed'
        WebUI.click(findTestObject('EmbedView/button_Embed'))

        if (GlobalVariable.RunWithEmbed == 'Yes') {
            'swith to iframe'
            WebUI.switchToFrame(findTestObject('EmbedView/iFrameEsign'), GlobalVariable.TimeOut, FailureHandling.CONTINUE_ON_FAILURE)
        }
    } else if (GlobalVariable.RunWithEmbed == 'No') {
        'navigate url ke daftar akun'
        WebUI.openBrowser(linkUrl)

        'Maximize Windows'
        WebUI.maximizeWindow()
    }
}

def checkKotakMasuk(Connection conneSign, ArrayList<String> emailSigner, String sheet, TestObject modifyObjectTextRefNumber, TestObject modifyObjectTextDocumentTemplateTipe, TestObject modifyObjectTextDocumentTemplateName, TestObject modifyObjectTextTglPermintaan, TestObject modifyObjectTextStatusTtd, TestObject modifyObjectTextProsesTtd, int row) {
    'declare arraylist arraymatch'
    ArrayList<String> arrayMatch = []

    'declare arrayIndexnya 0'
    arrayIndexKotakMasuk = 0

    'Declare is Download document, is delete download document, dan is view documument berdasarkan inputan excel'
    String isDownloadDocument = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 79)

    String isDeleteDownloadedDocument = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 80)

    String isViewDocument = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 81)

    'mengambil document dari excel yang telah diberikan.'
    docId = findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 6)

    'get data kotak masuk send document secara asc, dimana customer no 1'
    ArrayList<String> result = CustomKeywords.'connection.APIFullService.getKotakMasukSendDoc'(conneSign, docId)

    'Mengambil label nomor kontrak untuk View Document'
    labelRefNum = WebUI.getText(modifyObjectTextRefNumber)

    'verifikasi ref number dengan database'
    arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextRefNumber), result[arrayIndexKotakMasuk++], false, FailureHandling.CONTINUE_ON_FAILURE))

    'verifikasi doctype dengan database'
    arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextDocumentTemplateTipe), result[arrayIndexKotakMasuk++], 
            false, FailureHandling.CONTINUE_ON_FAILURE))

    'verifikasi document template name dengan database'
    arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextDocumentTemplateName), result[arrayIndexKotakMasuk++], 
            false, FailureHandling.CONTINUE_ON_FAILURE))

    'verifikasi tanggal permintaan dengan database'
    arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextTglPermintaan), result[arrayIndexKotakMasuk++], false, 
            FailureHandling.CONTINUE_ON_FAILURE))

    'verifikasi status ttd'
    arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextStatusTtd), result[arrayIndexKotakMasuk++], false, FailureHandling.CONTINUE_ON_FAILURE))

    'Mengambil text mengenai proses tanda tangan dan displit menjadi 2, yang pertama menjadi jumlah signer yang sudah tanda tangan. Yang kedua menjadi total signer'
    ArrayList<String> prosesTtd = WebUI.getText(modifyObjectTextProsesTtd).split(' / ', -1)

    jumlahSignerTelahTandaTangan = CustomKeywords.'connection.APIFullService.getProsesTtdProgress'(conneSign, labelRefNum)

    'Verif hasil split, dimana proses awal hingga akhir. Awal dibandingkan dengan jumlahsignertandatangan, sedangkan akhir dibandingkan dengan total signer dari email'
    arrayMatch.add(WebUI.verifyEqual(prosesTtd[0], jumlahSignerTelahTandaTangan, FailureHandling.CONTINUE_ON_FAILURE))

    arrayMatch.add(WebUI.verifyEqual(prosesTtd[1], emailSigner.size(), FailureHandling.CONTINUE_ON_FAILURE))

    'Jika error lognya muncul'
    if (WebUI.verifyElementPresent(findTestObject('KotakMasuk/Sign/errorLog'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
        'Tulis di excel itu adalah error'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            (((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2) + ';') + '<') + WebUI.getAttribute(
                findTestObject('KotakMasuk/Sign/errorLog'), 'aria-label')) + '>')
    }
    
    'modify object button Signer berdasarkan row yang dipilih'
    modifyObjectBtnSigner = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/button_DownloadDocument'), 
        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        row) + ']/datatable-body-row/div[2]/datatable-body-cell[10]/div/a[4]/em', true)

    'modify object button Download Doc'
    modifyObjectBtnDownloadDoc = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/button_DownloadDocument'), 
        'xpath', 'equals', ('//datatable-row-wrapper[' + row) + ']/datatable-body-row/div[2]/datatable-body-cell[10]/div/a[3]/em', 
        true)

    'modify object button View Document'
    modifyObjectBtnViewDoc = WebUI.modifyObjectProperty(findTestObject('Object Repository/APIFullService/Send to Sign/button_DownloadDocument'), 
        'xpath', 'equals', ('/html/body/app-root/app-content-layout/div/div/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        row) + ']/datatable-body-row/div[2]/datatable-body-cell[10]/div/a[2]/em', true)

    'declare arrayIndexnya 0'
    arrayIndexKotakMasuk = 0

    'Klik button Signer'
    WebUI.click(modifyObjectBtnSigner)

    'Diberikan delay 1 sec untuk mengambil variable row dan column popup'
    WebUI.delay(1)

    'get row popup'
    variableRowPopup = DriverFactory.webDriver.findElements(By.cssSelector('body > ngb-modal-window > div > div > app-signer > div.modal-body > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

    'get column popup'
    variableColPopup = DriverFactory.webDriver.findElements(By.cssSelector('body > ngb-modal-window > div > div > app-signer > div.modal-body > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-body-cell'))

    'loop untuk row popup'
    for (int i = 1; i <= variableRowPopup.size(); i++) {
        emailSignerBasedOnSequence = CustomKeywords.'connection.APIFullService.getEmailBasedOnSequence'(conneSign, docId).split(
            ';', -1)

        'get data kotak masuk send document secara asc, dimana customer no 1'
        ArrayList<String> resultSigner = CustomKeywords.'connection.APIFullService.getSignerKotakMasukSendDoc'(conneSign, 
            docId, emailSignerBasedOnSequence[(i - 1)])

        'declare arrayIndexnya 0'
        arrayIndexKotakMasuk = 0

        'loop untuk column popup'
        for (int m = 1; m <= (variableColPopup.size() / variableRowPopup.size()); m++) {
            'modify object text nama, email, signer Type, sudah aktivasi Untuk yang terakhir belum bisa, dikarenakan masih gak ada data (-) Dikarenakan modifynya bukan p di lastnya, melainkan span'
            modifyObjectTextPopup = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/text_tipepopup'), 'xpath', 'equals', 
                ((('/html/body/ngb-modal-window/div/div/app-signer/div[2]/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                i) + ']/datatable-body-row/div[2]/datatable-body-cell[') + m) + ']/div', true)

            'signer nama,email,signerType,sudahAktivasi popup'
            arrayMatch.add(WebUI.verifyMatch(WebUI.getText(modifyObjectTextPopup), resultSigner[arrayIndexKotakMasuk++], 
                    false, FailureHandling.CONTINUE_ON_FAILURE))
        }
    }
    
    'declare arrayIndexnya 0'
    arrayIndexKotakMasuk = 0

    'Klik x terlebih dahulu pada popup'
    WebUI.click(findTestObject('Object Repository/KotakMasuk/btn_X'))

    'Diberikan delay 1 sec'
    WebUI.delay(1)

    'Jika document ingin didownload, maka'
    if (isDownloadDocument == 'Yes') {
        'setting untuk membuat lokasi default folder download'
        HashMap<String, ArrayList> chromePrefs = new HashMap<String, ArrayList>()

        chromePrefs.put('download.default_directory', System.getProperty('user.dir') + '\\Download')

        RunConfiguration.setWebDriverPreferencesProperty('prefs', chromePrefs)

        'Klik download file'
        WebUI.click(modifyObjectBtnDownloadDoc)

        'Kasih waktu 3 detik untuk proses download'
        WebUI.delay(3)

        'Check apakah sudah terddownload menggunakan custom keyword'
        CustomKeywords.'customizekeyword.Download.isFileDownloaded'(isDeleteDownloadedDocument)
    }
    
    'Jika is View Document yes, maka '
    if (isViewDocument == 'Yes') {
        'Klik View Document'
        WebUI.click(modifyObjectBtnViewDoc)

        'Pemberian waktu 3 detik karena loading terus menerus'
        WebUI.delay(3)

        'modify object header label nomor kontrak'
        modifyObjectTextTitelViewDokumen = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/text_tipepopup'), 'xpath', 
            'equals', '/html/body/app-root/app-content-layout/div/div/div/div[2]/app-view-document-inquiry/div[1]/div[2]/div', 
            true)

        'verifikasi label dokumen'
        if (WebUI.verifyElementPresent(modifyObjectTextTitelViewDokumen, GlobalVariable.TimeOut, FailureHandling.CONTINUE_ON_FAILURE)) {
            'Mengambil label pada view Dokoumen'
            labelViewDoc = WebUI.getText(modifyObjectTextTitelViewDokumen)

            'Jika pada label terdapat teks No Kontrak'
            if (labelViewDoc.contains('No Kontrak')) {
                'Direplace dengan kosong agar mendapatkan nomor kontrak'
                labelViewDoc = labelViewDoc.replace('No Kontrak ', '')
            }
            
            'Diverifikasi dengan UI didepan'
            arrayMatch.add(WebUI.verifyMatch(labelRefNum, labelViewDoc, false, FailureHandling.CONTINUE_ON_FAILURE))

            'Klik kembali'
            WebUI.click(findTestObject('Object Repository/KotakMasuk/btn_backViewDokumen'))

            if (WebUI.verifyElementPresent(modifyObjectTextTitelViewDokumen, GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
                'Klik kembali'
                WebUI.click(findTestObject('Object Repository/KotakMasuk/btn_backViewDokumen'))
            }
        } else {
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedProcessNotDone) + 
                ' untuk proses View dokumen tanda tangan. ')
        }
    }
    
    'penggunaan ini hanya untuk Masukan Store Db'
    if (arrayMatch.contains(false)) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 2).replace('-', '') + ';') + GlobalVariable.ReasonFailedStoredDB) + 
            ' pada Kotak Masuk')
    }
}

def rowExcel(String cellValue) {
	return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}