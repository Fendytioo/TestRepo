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
import org.apache.commons.lang3.time.StopWatch as StopWatch
import org.apache.commons.lang3.time.DurationFormatUtils as DurationFormatUtils

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign Main.xlsx')

sheet = 'Main'

'get current date'
def currentDate = new Date().format('yyyy-MM-dd')

'looping untuk menjalankan Main'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= findTestData(excelPathMain).columnNumbers; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
        break //  'Input enter'
        //WebUI.sendKeys(findTestObject('Saldo/input_tipedokumen'), Keys.chord(Keys.ENTER))
        //  'Input documentTemplateName'
    } //   WebUI.setText(findTestObject('Saldo/input_namadokumen'), documentTemplateName)
    else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
        StopWatch watch = StopWatch.createStarted()

        'reset value di excel mengenai output previous run. Jika menggunakan opsi Sign Only, maka document id tidak akan didelete'
        resetValue()

        inisializeValue()

        'setting menggunakan base url yang benar atau salah'
        CustomKeywords.'connection.APIFullService.settingMustUseWAFirst'(conneSign, findTestData(excelPathMain).getValue(
                GlobalVariable.NumofColm, rowExcel('Must Wa')))

        'setting menggunakan base url yang benar atau salah'
        CustomKeywords.'connection.APIFullService.settingUseWAMessage'(conneSign, findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 
                rowExcel('Use WA Message')))

        String vendor

        String signType

        String minuses = '-'

        String cancelDocsValue = ''

        int flagBreak = 0

        HashMap<String, String> resultSaldoBefore = new HashMap<String, String>()

        HashMap<String, String> resultSaldoAfter = new HashMap<String, String>()

        'pasang gv tenant agar tidak berubah'
        GlobalVariable.Tenant = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Tenant'))

        'inisialisasi signerInput dan email signer sebagai array list'
        ArrayList emailSignerPerDoc

        ArrayList signers = []

        ArrayList documentId = []

        ArrayList signersPerDoc = []

        LinkedHashMap emailSigner = new LinkedHashMap()

        LinkedHashMap signerInput = new LinkedHashMap()

        if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 'API Send Document External') {
            documentTemplateCode = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('$documentTemplateCode (Send External)')).split(
                ';', -1)

            for (loopingGetSaldoBefore = 0; loopingGetSaldoBefore < documentTemplateCode.size(); loopingGetSaldoBefore++) {
                logicVendor = CustomKeywords.'connection.SendSign.getProyectionOfVendorForSend'(conneSign, (documentTemplateCode[
                    loopingGetSaldoBefore]).replace('"', ''), GlobalVariable.Tenant)

                GlobalVariable.saldo = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain
                        , ('sheet') : sheet, ('vendor') : logicVendor, ('usageSaldo') : 'Sign'], FailureHandling.CONTINUE_ON_FAILURE)

                resultSaldoBefore.putAll(GlobalVariable.saldo)
            }
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'API Send Document Normal') {
            documentTemplateCode = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('$documentTemplateCode (Send Normal)')).split(
                ';', -1)

            for (loopingGetSaldoBefore = 0; loopingGetSaldoBefore < documentTemplateCode.size(); loopingGetSaldoBefore++) {
                logicVendor = CustomKeywords.'connection.SendSign.getProyectionOfVendorForSend'(conneSign, (documentTemplateCode[
                    loopingGetSaldoBefore]).replace('"', ''), GlobalVariable.Tenant)

                GlobalVariable.saldo = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain
                        , ('sheet') : sheet, ('vendor') : logicVendor, ('usageSaldo') : 'Sign'], FailureHandling.CONTINUE_ON_FAILURE)

                resultSaldoBefore.putAll(GlobalVariable.saldo)
            }
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'Manual Sign') {
            logicVendor = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('$PSrE (Send Manual)'))

            GlobalVariable.saldo = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain, ('sheet') : sheet
                    , ('vendor') : logicVendor, ('usageSaldo') : 'Sign'], FailureHandling.CONTINUE_ON_FAILURE)

            resultSaldoBefore.putAll(GlobalVariable.saldo)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'Sign Only') {
            logicVendor = CustomKeywords.'connection.SendSign.getVendorNameUsingDocId'(conneSign, findTestData(excelPathMain).getValue(
                    GlobalVariable.NumofColm, rowExcel('documentid')))

            GlobalVariable.saldo = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain, ('sheet') : sheet
                    , ('vendor') : logicVendor, ('usageSaldo') : 'Sign'], FailureHandling.CONTINUE_ON_FAILURE)

            resultSaldoBefore.putAll(GlobalVariable.saldo)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'Stamp Only') {
            GlobalVariable.saldo = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain, ('sheet') : sheet
                    , ('vendor') : '', ('usageSaldo') : 'Stamp'], FailureHandling.CONTINUE_ON_FAILURE)

            resultSaldoBefore.putAll(GlobalVariable.saldo)
        }
        
        'Pemilihan opsi send document. Jika send document API Send External'
        if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 'API Send Document External') {
            'call test case send doc external'
            WebUI.callTestCase(findTestCase('Main Flow - Copy/API Send Document External'), [('excelPathAPISendDoc') : excelPathMain
                    , ('sheet') : sheet], FailureHandling.CONTINUE_ON_FAILURE)

            documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).split(', ', 
                -1)

            signerInput = checkingDocAndEmailFromInput(documentId, '$email (Send External)', signerInput)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'API Send Document Normal') {
            'jika send document menggunakan api send normal, maka call test case send doc normal'
            WebUI.callTestCase(findTestCase('Main Flow - Copy/API Send Document Normal'), [('API_Excel_Path') : excelPathMain
                    , ('sheet') : sheet], FailureHandling.CONTINUE_ON_FAILURE)

            documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).split(', ', 
                -1)

            signerInput = checkingDocAndEmailFromInput(documentId, '$email (Send Normal)', signerInput)
        } else if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
        'Manual Sign') {
            'jika send document menggunakan manual sign, maka call test case manual sign'
            WebUI.callTestCase(findTestCase('Main Flow - Copy/Manual Sign'), [('excelPathManualSigntoSign') : excelPathMain
                    , ('sheet') : sheet], FailureHandling.CONTINUE_ON_FAILURE)

            documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).split(', ', 
                -1)

            signerInput = checkingDocAndEmailFromInput(documentId, 'jumlah signer lokasi per signer (Send Manual)', signerInput)
        }
        
        'jika documentid nya tidak kosong'
        if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).length() > 0) {
            if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) == 
            'Cancel Only') {
                cancelDocsValue = 'Yes'
            } else {
                cancelDocsValue = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Send?'))
            }
            
            'Memanggil DocumentMonitoring untuk dicheck apakah documentnya sudah masuk'
            WebUI.callTestCase(findTestCase('Main Flow - Copy/VerifyDocumentMonitoring'), [('excelPathFESignDocument') : excelPathMain
                    , ('sheet') : sheet, ('CancelDocsSend') : cancelDocsValue], FailureHandling.CONTINUE_ON_FAILURE)

            'jika ada proses cancel doc'
            if (cancelDocsValue == 'Yes') {
                'lanjutkan loop'
                continue
            }
            
            'jika opsi tanda tangannya bukan stamp only'
            if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 
            'Stamp Only') {
                'jika memerlukan tanda tangan pada dokumen ini'
                if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Need Sign for this document?')) == 
                'Yes') {
                    'setting must liveness dimatikan/diaktifkan'
                    if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Setting Must LivenessFaceCompare')).length() > 
                    0) {
                        'update setting vendor otp ke table di DB'
                        CustomKeywords.'connection.UpdateData.settingLivenessFaceCompare'(conneSign, findTestData(excelPathMain).getValue(
                                GlobalVariable.NumofColm, rowExcel('Setting Must LivenessFaceCompare')))
                    }
                    
                    'ambil opsi signing'
                    ArrayList opsiSigning = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Sign Document per Signer')).split(
                        ';', -1)

                    'jika opsi tanda tangannya bukan sign only'
                    if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 
                    'Sign Only') {
                        for (i = (documentId.size() - 1); i >= 0; i--) {
                            emailSignerPerDoc = CustomKeywords.'connection.SendSign.getEmailLogin'(conneSign, documentId[
                                i]).split(';', -1)

                            emailSignerPerDoc = emailSignerPerDoc.collect({ 
                                    it.trim()
                                })

                            emailSigner.put(documentId[i], emailSignerPerDoc)
                        }
                        
                        'pengecekan jika total signer dari inputan dan dari db tidak sama'
                        if (WebUI.verifyNotEqual(signerInput.size(), emailSigner.size(), FailureHandling.OPTIONAL)) {
                            'Write To Excel GlobalVariable.StatusFailed and total signernya tidak sesuai'
                            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                GlobalVariable.StatusFailed, (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 
                                    rowExcel('Reason Failed')) + ';') + ' total signer pada Send Document dengan signer yang terdaftar tidak sesuai ')
                        }
                    } else {
                        documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).split(
                            ', ', -1)

                        for (i = (documentId.size() - 1); i >= 0; i--) {
                            documentId = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid')).split(
                                ', ', -1)

                            for (i = (documentId.size() - 1); i >= 0; i--) {
                                emailSignerPerDoc = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel(
                                        'email Signer (Sign Only)')).split(';', -1)

                                emailSignerPerDoc = emailSignerPerDoc.collect({ 
                                        it.trim()
                                    })

                                emailSigner.put(documentId[i], emailSignerPerDoc)
                            }
                        }
                    }
                    
                    cancelDocsValue = ''

                    'looping berdasarkan email yang akan menandatangani'
                    for (int i = 0; i < emailSigner.keySet().size(); i++) {
                        if (flagBreak == 1) {
                            break
                        }
                        
                        for (y = 0; y < emailSigner.get(emailSigner.keySet()[i]).size(); y++) {
                            if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Sign?')) == 
                            'Yes') {
                                'integrasikan cancel docs jika signer sudah sesuai'
                                if ((Integer.parseInt(findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel(
                                            'Cancel Docs after how many Signer?'))) - 1) == y) {
                                    'ubah value cancel docs menjadi yes'
                                    cancelDocsValue = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel(
                                            'Cancel Docs after Sign?'))
                                }
                            }
                            
                            ifSignerAuto = CustomKeywords.'connection.APIFullService.getIfSignerAutosign'(conneSign, emailSigner.keySet()[
                                i], emailSigner.get(emailSigner.keySet()[i])[y])

                            if (ifSignerAuto == 'Autosign') {
                                continue
                            }
                            
                            GlobalVariable.storeVar = [:]

                            GlobalVariable.storeVar.putAt(emailSigner.keySet()[i], emailSigner.get(emailSigner.keySet()[
                                    i])[y])

                            'reset value GV'
                            GlobalVariable.eSignData.putAt('VerifikasiOTP', 0)

                            GlobalVariable.eSignData.putAt('VerifikasiBiometric', 0)

                            GlobalVariable.eSignData.putAt('VerifikasiSign', 0)

                            'jika opsi tanda tangannya bukan sign only'
                            if ((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 
                            'Sign Only') && (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 
                            'Stamp Only')) {
                                'call test case kotak masuk dan verify document monitoring. Document monitoring terdapat didalam kotak masuk.'
                                WebUI.callTestCase(findTestCase('Main Flow - Copy/KotakMasuk'), [('excelPathFESignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('checkBeforeSigning') : 'Yes'], FailureHandling.CONTINUE_ON_FAILURE)
                            }
                            
                            'jika opsi signing untuk signer adalah api sign document external'
                            if ((opsiSigning[GlobalVariable.opsiSigning]) == 'API Sign Document External') {
                                'setting index untuk penggunaan data. Cara bacanya adalah apakah opsi tersebut telah digunakan. Jika sudah digunakan, maka + 1, jika tidak, maka 0.'
                                indexReadDataExcelAPIExternal = inisializeArray(isUsedAPIExternal, indexReadDataExcelAPIExternal)

                                'diberikan kepada gv indexused'
                                GlobalVariable.indexUsed = indexReadDataExcelAPIExternal

                                'call test case api sign document external'
                                WebUI.callTestCase(findTestCase('Main Flow - Copy/API Sign Document External'), [('excelPathAPISignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('CancelDocsSign') : cancelDocsValue], FailureHandling.CONTINUE_ON_FAILURE)

                                'set boolean is used api external menjadi true'
                                isUsedAPIExternal = true
                            } else if ((opsiSigning[GlobalVariable.opsiSigning]) == 'API Sign Document Normal') {
                                'jika opsi signing untuk signer adalah api sign document normal setting index untuk penggunaan data. Cara bacanya adalah apakah opsi tersebut telah digunakan. Jika sudah digunakan, maka + 1, jika tidak, maka 0.'
                                indexReadDataExcelAPINormal = inisializeArray(isUsedAPINormal, indexReadDataExcelAPINormal)

                                'diberikan kepada gv indexused'
                                GlobalVariable.indexUsed = indexReadDataExcelAPINormal

                                'call test case api sign document normal'
                                WebUI.callTestCase(findTestCase('Main Flow/API Sign Document Normal'), [('API_Excel_Path') : excelPathMain
                                        , ('sheet') : sheet, ('CancelDocsSign') : cancelDocsValue], FailureHandling.CONTINUE_ON_FAILURE)

                                'set boolean menjadi true'
                                isUsedAPINormal = true
                            } else if ((opsiSigning[GlobalVariable.opsiSigning]) == 'Webview Sign') {
                                'jika opsi signing untuk signer adalah webview sign setting index untuk penggunaan data. Cara bacanya adalah apakah opsi tersebut telah digunakan. Jika sudah digunakan, maka + 1, jika tidak, maka 0.'
                                indexReadDataExcelWebview = inisializeArray(isUsedUI, indexReadDataExcelUI)

                                'diberikan kepada gv indexused'
                                GlobalVariable.indexUsed = indexReadDataExcelWebview

                                'call test case webview embed sign'
                                WebUI.callTestCase(findTestCase('Main Flow/Webview Embed Sign'), [('excelPathFESignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('indexUsed') : indexReadDataExcelWebview, ('opsiSigning') : opsiSigning[
                                        i], ('CancelDocsSign') : cancelDocsValue], FailureHandling.CONTINUE_ON_FAILURE)

                                'set boolean true'
                                isUsedUI = true
                            } else if ((opsiSigning[GlobalVariable.opsiSigning]) == 'Embed Sign') {
                                'jika opsi signing untuk signer adalah embed sign setting index untuk penggunaan data. Cara bacanya adalah apakah opsi tersebut telah digunakan. Jika sudah digunakan, maka + 1, jika tidak, maka 0.'
                                indexReadDataExcelEmbed = inisializeArray(isUsedUI, indexReadDataExcelUI)

                                'diberikan kepada gv indexused'
                                GlobalVariable.indexUsed = indexReadDataExcelEmbed

                                'call test case webview embed sign'
                                WebUI.callTestCase(findTestCase('Main Flow/Webview Embed Sign'), [('excelPathFESignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('indexUsed') : indexReadDataExcelEmbed, ('opsiSigning') : opsiSigning[
                                        i], ('CancelDocsSign') : cancelDocsValue], FailureHandling.CONTINUE_ON_FAILURE)

                                'set boolean true'
                                isUsedUI = true
                            } else if ((opsiSigning[GlobalVariable.opsiSigning]) == 'Sign Via Inbox') {
                                'jika opsi signing untuk signer adalah sign via inbox setting index untuk penggunaan data. Cara bacanya adalah apakah opsi tersebut telah digunakan. Jika sudah digunakan, maka + 1, jika tidak, maka 0.'
                                indexReadDataExcelInboxSigner = inisializeArray(isUsedUI, indexReadDataExcelUI)

                                'diberikan kepada gv indexused'
                                GlobalVariable.indexUsed = indexReadDataExcelInboxSigner

                                'call test case signer login sign'
                                WebUI.callTestCase(findTestCase('Main Flow - Copy/SignerLogin Sign'), [('excelPathFESignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('opsiSigning') : opsiSigning[i], ('CancelDocsSign') : cancelDocsValue], 
                                    FailureHandling.CONTINUE_ON_FAILURE)

                                'set boolean true'
                                isUsedUI = true
                            }
                            
                            (GlobalVariable.opsiSigning)++

                            'total saldo otp'
                            GlobalVariable.eSignData.putAt('CountVerifikasiOTP', GlobalVariable.eSignData.getAt('CountVerifikasiOTP') + 
                                GlobalVariable.eSignData.getAt('VerifikasiOTP'))

                            'total saldo biometric'
                            GlobalVariable.eSignData.putAt('CountVerifikasiBiometric', GlobalVariable.eSignData.getAt('CountVerifikasiBiometric') + 
                                GlobalVariable.eSignData.getAt('VerifikasiBiometric'))

                            'total saldo sign'
                            GlobalVariable.eSignData.putAt('CountVerifikasiSign', GlobalVariable.eSignData.getAt('CountVerifikasiSign') + 
                                GlobalVariable.eSignData.getAt('VerifikasiSign'))

                            if (cancelDocsValue != '') {
                                'Memanggil DocumentMonitoring untuk dicheck apakah documentnya sudah masuk'
                                WebUI.callTestCase(findTestCase('Main Flow - Copy/VerifyDocumentMonitoring'), [('excelPathFESignDocument') : excelPathMain
                                        , ('sheet') : sheet, ('CancelDocsSign') : cancelDocsValue, ('vendor') : vendor], 
                                    FailureHandling.CONTINUE_ON_FAILURE)

                                flagBreak = 1

                                break
                            }
                        }
                    }
                }
            }
            
            'ambil nama vendor dari DB'
            vendor = CustomKeywords.'connection.DataVerif.getVendorNameForSaldo'(conneSign, GlobalVariable.eSignData.getAt(
                    'NoKontrakProcessed').split(';', -1)[0])

            if (flagBreak != 1) {
                'jika set stamping'
                if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Do Stamp for this document?')) == 
                'Yes') {
                    'call test case stamping'
                    WebUI.callTestCase(findTestCase('Main Flow - Copy/Stamping'), [('excelPathStamping') : excelPathMain
                            , ('sheet') : sheet, ('linkDocumentMonitoring') : '', ('CancelDocsStamp') : findTestData(excelPathMain).getValue(
                                GlobalVariable.NumofColm, rowExcel('Cancel Docs after Stamp?'))], FailureHandling.CONTINUE_ON_FAILURE)

                    'jika ada proses cancel doc'
                    if (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Cancel Docs after Stamp?')) == 
                    'Yes') {
                        'lanjutkan loop'
                        continue
                    }
                }
                
                'Memanggil DocumentMonitoring untuk dicheck apakah documentnya sudah masuk'
                WebUI.callTestCase(findTestCase('Main Flow - Copy/VerifyDocumentMonitoring'), [('excelPathFESignDocument') : excelPathMain
                        , ('sheet') : sheet, ('nomorKontrak') : GlobalVariable.eSignData.getAt('NoKontrakProcessed'), ('vendor') : vendor], 
                    FailureHandling.CONTINUE_ON_FAILURE)
            }
            
            resultSaldoAfter = WebUI.callTestCase(findTestCase('Main Flow/getSaldo'), [('excel') : excelPathMain, ('sheet') : sheet
                    , ('vendor') : vendor, ('usageSaldo') : 'Sign'], FailureHandling.CONTINUE_ON_FAILURE)

            WebUI.comment(GlobalVariable.eSignData.toString())

            'Jika count saldo sign/ttd diatas (after) sama dengan yang dulu/pertama (before) dikurang jumlah dokumen yang ditandatangani'
            if (WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get(vendor)) - GlobalVariable.eSignData.getAt('CountVerifikasiSign'), 
                Integer.parseInt(resultSaldoAfter.get(vendor)), FailureHandling.OPTIONAL)) {
                flagBreak = 0

                'cek apa pernah menggunakan biometrik'

                'Jika count saldo otp after dengan yang before dikurangi 1 ditambah dengan '
                WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('OTP')) - GlobalVariable.eSignData.getAt('CountVerifikasiOTP'), 
                    Integer.parseInt(resultSaldoAfter.get('OTP')), FailureHandling.CONTINUE_ON_FAILURE)

                if (GlobalVariable.eSignData.getAt('CountVerifikasiBiometric') > 0) {
                    'cek saldo liveness facecompare dipisah atau tidak'
                    String isSplitLivenessFc = CustomKeywords.'connection.APIFullService.getSplitLivenessFaceCompareBill'(
                        conneSign)

                    'jika saldo liveness digabung dengan facecompare'
                    if (isSplitLivenessFc == '0') {
                        'cek apakah saldo liveness facecompare masih sama'
                        if (WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('Liveness Face Compare')) - GlobalVariable.eSignData.getAt(
                                'CountVerifikasiBiometric'), Integer.parseInt(resultSaldoAfter.get('Liveness Face Compare')), 
                            FailureHandling.CONTINUE_ON_FAILURE)) {
                        }
                    } else if (isSplitLivenessFc == '1') {
                        'cek apakah saldo liveness dan facecompare sama'
                        if (WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('Liveness')) - GlobalVariable.eSignData.getAt(
                                'CountVerifikasiBiometric'), Integer.parseInt(resultSaldoAfter.get('Liveness')), FailureHandling.CONTINUE_ON_FAILURE) && 
                        WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('Face Compare')) - GlobalVariable.eSignData.getAt(
                                'CountVerifikasiBiometric'), Integer.parseInt(resultSaldoAfter.get('Face Compare')), FailureHandling.CONTINUE_ON_FAILURE)) {
                        }
                    }
                }
                
                WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('SMS Notif')) - GlobalVariable.eSignData.getAt(
                        'CountVerifikasiSMS'), Integer.parseInt(resultSaldoAfter.get('SMS Notif')), FailureHandling.CONTINUE_ON_FAILURE)

                WebUI.verifyEqual(Integer.parseInt(resultSaldoBefore.get('WhatsApp Message')) - GlobalVariable.eSignData.getAt(
                        'CountVerifikasiWA'), Integer.parseInt(resultSaldoAfter.get('WhatsApp Message')), FailureHandling.CONTINUE_ON_FAILURE)
            } else {
                'Write To Excel GlobalVariable.StatusFailed and total signernya tidak sesuai'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + 
                    ' Pemotongan saldo After seluruh proses signing tidak sesuai ')
            }
            
            if (GlobalVariable.eSignData.getAt('VerifikasiMeterai') > 0) {
                'Jika count saldo sign/ttd diatas (after) sama dengan yang dulu/pertama (before) dikurang jumlah dokumen yang ditandatangani'
                if (WebUI.verifyNotEqual(Integer.parseInt(resultSaldoBefore.get('Meterai')) - GlobalVariable.eSignData.getAt(
                        'VerifikasiMeterai'), Integer.parseInt(resultSaldoAfter.get('Meterai')), FailureHandling.OPTIONAL)) {
                    'Write To Excel GlobalVariable.StatusFailed and total signernya tidak sesuai'
                    CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                        GlobalVariable.StatusFailed, (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel(
                                'Reason Failed')) + ';') + ' Pemotongan saldo After seluruh proses Meterai tidak sesuai ')
                }
            }
            
            'loopig berdasarkan total dokumen dari dokumen template code'
            if (GlobalVariable.eSignData.getAt('VerifikasiSign') > 0) {
                'loopig berdasarkan total dokumen dari dokumen template code'
                for (i = 0; i < GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1).size(); i++) {
                    'ambil nama vendor dari DB'
                    vendor = CustomKeywords.'connection.DataVerif.getVendorNameForSaldo'(conneSign, GlobalVariable.eSignData.getAt(
                            'NoKontrakProcessed').split(';', -1)[i])

                    if (vendor == 'DIGISIGN') {
                        signType = 'Document'
                    } else {
                        signType = 'Sign'
                    }
                    
                    'Input filter di Mutasi Saldo'
                    inputFilterTrx(conneSign, currentDate, GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', 
                            -1)[i], signType)

                    'Mengambil value dari db mengenai tipe pembayran'
                    paymentType = CustomKeywords.'connection.APIFullService.getPaymentType'(conneSign, GlobalVariable.eSignData.getAt(
                            'NoKontrakProcessed').split(';', -1)[i])

                    'Jika tipe pembayarannya per sign'
                    if (paymentType == 'Per Sign') {
                        'Memanggil saldo total yang telah digunakan per dokumen tersebut'
                        saldoUsedperDoc = CustomKeywords.'connection.APIFullService.getTotalSignedUsingRefNumber'(conneSign, 
                            GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i])

                        if (saldoUsedperDoc == '0') {
                            WebUI.delay(10)

                            'Memanggil saldo total yang telah digunakan per dokumen tersebut'
                            saldoUsedperDoc = CustomKeywords.'connection.APIFullService.getTotalSignedUsingRefNumber'(conneSign, 
                                GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i])
                        }
                    } else {
                        saldoUsedperDoc = 1
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
                            ArrayList inquiryDB = CustomKeywords.'connection.APIFullService.gettrxSaldo'(conneSign, GlobalVariable.eSignData.getAt(
                                    'NoKontrakProcessed').split(';', -1)[i], GlobalVariable.eSignData.getAt('CountVerifikasiSign').toString(), 
                                'Use ' + signType)

                            index = 0

                            'looping mengenai rownya'
                            for (int j = 1; j <= variableSaldoRow.size(); j++) {
                                'looping mengenai columnnya'
                                for (int u = 1; u <= variableSaldoColumn.size(); u++) {
                                    'modify per row dan column. column menggunakan u dan row menggunakan documenttemplatename'
                                    modifyperrowpercolumn = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/lbl_notrxsaldo'), 
                                        'xpath', 'equals', ((('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[') + u) + ']/div', true)

                                    WebUI.scrollToElement(modifyperrowpercolumn, GlobalVariable.TimeOut)

                                    'Jika u di lokasi qty atau kolom ke 9'
                                    if (u == 9) {
                                        'Jika yang qtynya 1 dan databasenya juga, berhasil'
                                        minuses = '-'

                                        if ((inquiryDB[index]).toString() == '0') {
                                            minuses = ''
                                        }
                                        
                                        'Jika bukan untuk 2 kolom itu, maka check ke db'
                                        checkVerifyEqualorMatch(WebUI.verifyMatch(minuses + WebUI.getText(modifyperrowpercolumn), 
                                                inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), 'pada Kuantitas di Mutasi Saldo dengan nomor kontrak ' + 
                                            (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i]))
                                    } else if (u == variableSaldoColumn.size()) {
                                        'Jika di kolom ke 10, atau di FE table saldo, check saldo dari table dengan saldo yang sekarang. Takeout dari dev karena no issue dan sudah sepakat'
                                    } else {
                                        'Jika bukan untuk 2 kolom itu, maka check ke db'
                                        checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyperrowpercolumn), 
                                                inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Mutasi Saldo dengan nomor kontrak ' + 
                                            (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i]))
                                    }
                                }
                            }
                            
                            break
                        } else {
                            'jika kesempatan yang terakhir'
                            if (d == 6) {
                                'Jika masih tidak ada'
                                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                    GlobalVariable.StatusFailed, (((((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 
                                        rowExcel('Reason Failed')).replace('-', '') + ';') + GlobalVariable.ReasonFailedNoneUI) + 
                                    ' dengan nomor kontrak ') + '<') + (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(
                                        ';', -1)[i])) + '>')
                            }
                            
                            'delay 10 detik'
                            WebUI.delay(10)

                            'Klik cari'
                            WebUI.click(findTestObject('Saldo/btn_cari'))
                        }
                    }
                }
            }
            
            GlobalVariable.eSignData.putAt('NoKontrakProcessed', CustomKeywords.'connection.APIFullService.getRefNumber'(
                    conneSign, findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('documentid'))))

            'loopig berdasarkan total dokumen dari dokumen template code'
            if (GlobalVariable.eSignData.getAt('VerifikasiMeterai') > 0) {
                for (i = 0; i < GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1).size(); i++) {
                    'klik ddl untuk tenant memilih mengenai Vida'
                    WebUI.selectOptionByLabel(findTestObject('Saldo/ddl_Vendor'), 'ESIGN/ADINS', false)

                    signType = 'Stamp Duty'

                    'Input filter di Mutasi Saldo'
                    inputFilterTrx(conneSign, currentDate, GlobalVariable.eSignData.getAt('NoKontrakProcessed'), signType)

                    'mengambil value db proses ttd'
                    prosesMaterai = CustomKeywords.'connection.Meterai.getProsesMaterai'(conneSign, GlobalVariable.eSignData.getAt(
                            'NoKontrakProcessed'))

                    if (prosesMaterai == 63) {
                        'ambil inquiry di db'
                        inquiryDB = CustomKeywords.'connection.APIFullService.gettrxSaldoForMeteraiPrivy'(conneSign, GlobalVariable.eSignData.getAt(
                                'NoKontrakProcessed'))
                    } else {
                        'ambil inquiry di db'
                        inquiryDB = CustomKeywords.'connection.APIFullService.gettrxSaldoForMeterai'(conneSign, GlobalVariable.eSignData.getAt(
                                'NoKontrakProcessed'))
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

                            index = 0

                            'looping mengenai rownya'
                            for (int j = 1; j <= variableSaldoRow.size(); j++) {
                                'looping mengenai columnnya'
                                for (int u = 1; u <= variableSaldoColumn.size(); u++) {
                                    'modify per row dan column. column menggunakan u dan row menggunakan documenttemplatename'
                                    modifyperrowpercolumn = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/lbl_notrxsaldo'), 
                                        'xpath', 'equals', ((('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                                        j) + ']/datatable-body-row/div[2]/datatable-body-cell[') + u) + ']/div', true)

                                    WebUI.scrollToElement(modifyperrowpercolumn, GlobalVariable.TimeOut)

                                    'Jika u di lokasi qty atau kolom ke 9'
                                    if (u == 9) {
                                        minuses = '-'

                                        if ((inquiryDB[index]).toString() == '0') {
                                            minuses = ''
                                        }
                                        
                                        'Jika yang qtynya 1 dan databasenya juga, berhasil'

                                        'Jika bukan untuk 2 kolom itu, maka check ke db'
                                        checkVerifyEqualorMatch(WebUI.verifyMatch(minuses + WebUI.getText(modifyperrowpercolumn), 
                                                inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), 'pada Kuantitas di Mutasi Saldo dengan nomor kontrak ' + 
                                            (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i]))
                                    } else if (u == variableSaldoColumn.size()) {
                                        'Jika di kolom ke 10, atau di FE table saldo, check saldo dari table dengan saldo yang sekarang. Takeout dari dev karena no issue dan sudah sepakat'
                                    } else {
                                        'Jika bukan untuk 2 kolom itu, maka check ke db'
                                        checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyperrowpercolumn), 
                                                inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Mutasi Saldo dengan nomor kontrak ' + 
                                            (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(';', -1)[i]))
                                    }
                                }
                            }
                            
                            break
                        } else {
                            'jika kesempatan yang terakhir'
                            if (d == 6) {
                                'Jika masih tidak ada'
                                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, 
                                    GlobalVariable.StatusFailed, (((((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, 
                                        rowExcel('Reason Failed')).replace('-', '') + ';') + GlobalVariable.ReasonFailedNoneUI) + 
                                    ' dengan nomor kontrak ') + '<') + (GlobalVariable.eSignData.getAt('NoKontrakProcessed').split(
                                        ';', -1)[i])) + '>')
                            }
                            
                            'delay 10 detik'
                            WebUI.delay(10)

                            'Klik cari'
                            WebUI.click(findTestObject('Saldo/btn_cari'))
                        }
                    }
                }
            }
            
            ArrayList signTypes = GlobalVariable.eSignData.getAt('allSignType').toString().split(';', -1)

            ArrayList trxNo = (GlobalVariable.eSignData.getAt('allTrxNo').toString()[(0..-2)]).split(';', -1)

            WebUI.comment(GlobalVariable.eSignData.toString())

            if (signTypes.size() != trxNo.size()) {
                'Jika equalnya salah maka langsung berikan reason bahwa reasonnya failed'
                CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
                    ((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
                        '-', '') + ';') + 'Perbedaan total Transaksi dan tipe saldo yaitu ') + trxNo.toString())
            }
            
            if (GlobalVariable.eSignData.getAt('allTrxNo').toString().length() > 0) {
                'klik ddl untuk tenant memilih mengenai Vida'
                WebUI.selectOptionByLabel(findTestObject('Saldo/ddl_Vendor'), 'ESIGN/ADINS', false)

                for (looping = (signTypes.size() - 1); looping >= 0; looping--) {
                    'Input filter di Mutasi Saldo'
                    inputFilterTrx(conneSign, currentDate, '', signTypes[looping])

                    inquiryDB = CustomKeywords.'connection.DataVerif.getDetailTrx'(conneSign, trxNo[looping])

                    boolean isChecked

                    'Get row lastest'
                    variableLastest = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-dashboard1 > div:nth-child(3) > div > div > div.card-content > div > app-msx-datatable > section > ngx-datatable > div > datatable-footer > div > datatable-pager > ul li'))

                    'get row lastest'
                    modifyObjectBtnLastest = WebUI.modifyObjectProperty(findTestObject('Object Repository/KotakMasuk/Sign/btn_Lastest'), 
                        'xpath', 'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-dashboard1/div[3]/div/div/div[2]/div/app-msx-datatable/section/ngx-datatable/div/datatable-footer/div/datatable-pager/ul/li[' + 
                        variableLastest.size()) + ']/a/i', true)

                    'jika btn lastest dapat diclick'
                    if (WebUI.verifyElementPresent(modifyObjectBtnLastest, GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
                        WebUI.focus(modifyObjectBtnLastest)

                        'Klik button Lastest'
                        WebUI.click(modifyObjectBtnLastest, FailureHandling.CONTINUE_ON_FAILURE)
                    }
                    
                    'get column di saldo'
                    variableSaldoColumn = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-header > div > div.datatable-row-center.ng-star-inserted datatable-header-cell'))

                    'get row di saldo'
                    variableSaldoRow = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

                    index = 0
					
					int u
                    'looping mengenai rownya'
                    for (j = variableSaldoRow.size(); j >= 0; j--) {

                        if (j == 0) {
                            if (isChecked == false) {
                                WebUI.click(findTestObject('KotakMasuk/Sign/btn_balmut_Previous'))

                                'get column di saldo'
                                variableSaldoColumn = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-header > div > div.datatable-row-center.ng-star-inserted datatable-header-cell'))

                                'get row di saldo'
                                variableSaldoRow = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

                                j = (variableSaldoRow.size() + 1)

                                isChecked = true

                                continue
                            }
                        }
						
						if (isChecked == true) {
							break
						}
                        
                        'looping mengenai columnnya'
                        for (u = 1; u <= variableSaldoColumn.size(); u++) {
                            'modify per row dan column. column menggunakan u dan row menggunakan documenttemplatename'
                            modifyperrowpercolumn = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/lbl_notrxsaldo'), 
                                'xpath', 'equals', ((('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
                                j) + ']/datatable-body-row/div[2]/datatable-body-cell[') + u) + ']/div', true)

                            WebUI.scrollToElement(modifyperrowpercolumn, GlobalVariable.TimeOut)

                            if (u == 1) {
                                if (WebUI.getText(modifyperrowpercolumn) != (trxNo[looping])) {
                                    isChecked = false

                                    break
                                }
                            }
                            
                            'Jika u di lokasi qty atau kolom ke 9'
                            if (u == 9) {
                                minuses = '-'

                                if ((inquiryDB[index]) == '0') {
                                    minuses = ''
                                }
                                
                                'Jika bukan untuk 2 kolom itu, maka check ke db'
                                checkVerifyEqualorMatch(WebUI.verifyMatch(minuses + WebUI.getText(modifyperrowpercolumn), 
                                        inquiryDB[index++], false, FailureHandling.CONTINUE_ON_FAILURE), 'pada Kuantitas di Mutasi Saldo dengan nomor transaksi ' + 
                                    (trxNo[looping]))
								
									isChecked = true
								
                            } else if (u == variableSaldoColumn.size()) {
                                'Jika di kolom ke 10, atau di FE table saldo, check saldo dari table dengan saldo yang sekarang. Takeout dari dev karena no issue dan sudah sepakat'
                            } else {
                                'Jika bukan untuk 2 kolom itu, maka check ke db'
                                checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getText(modifyperrowpercolumn), inquiryDB[
                                        index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Mutasi Saldo dengan nomor transaksi ' + 
                                    (trxNo[looping]))
                            }
                        }
                    }
                    
                    break
                }
            }
        }
        
        watch.stop()

        String formattedElapsedTime = DurationFormatUtils.formatDurationHMS(watch.getTime())

        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Elapsed Time') - 
            1, GlobalVariable.NumofColm - 1, 'Elapsed Time: ' + formattedElapsedTime)
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
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def resetValue() {
    if (((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 'Sign Only') && 
    (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 'Stamp Only')) && 
    (findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Option for Send Document :')) != 'Cancel Only')) {
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('documentid') - 
            1, GlobalVariable.NumofColm - 1, '')
    }
    
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('trxNo') - 1, 
        GlobalVariable.NumofColm - 1, '')

    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Elapsed Time') - 
        1, GlobalVariable.NumofColm - 1, '')

    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('PsRE Document') - 
        1, GlobalVariable.NumofColm - 1, '')

    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('trxNos') - 1, 
        GlobalVariable.NumofColm - 1, '')

    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Success') - 
        1, GlobalVariable.NumofColm - 1, '')

    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Failed') - 
        1, GlobalVariable.NumofColm - 1, '')
}

def inisializeValue() {
    isUsedAPIExternal = false

    isUsedAPINormal = false

    isUsedUI = false

    indexReadDataExcelAPIExternal = 0

    indexReadDataExcelAPINormal = 0

    indexReadDataExcelInboxSigner = 0

    indexReadDataExcelEmbed = 0

    indexReadDataExcelWebview = 0

    indexReadDataExcelUI = 0

    GlobalVariable.opsiSigning = 0

    GlobalVariable.eSignData.putAt('CountVerifikasiOTP', 0)

    GlobalVariable.eSignData.putAt('CountVerifikasiBiometric', 0)

    GlobalVariable.eSignData.putAt('CountVerifikasiSign', 0)

    GlobalVariable.eSignData.putAt('CountVerifikasiWA', 0)

    GlobalVariable.eSignData.putAt('CountVerifikasiSMS', 0)

    GlobalVariable.eSignData.putAt('allTrxNo', '')

    GlobalVariable.eSignData.putAt('allSignType', '')

    GlobalVariable.eSignData.putAt('emailUsageSign', '')
}

def checkingDocAndEmailFromInput(ArrayList documentId, String rowEmail, LinkedHashMap signerInput) {
    for (loopingDocument = (documentId.size() - 1); loopingDocument >= 0; loopingDocument--) {
        signers = findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel(rowEmail)).split('\n', -1)

        signersPerDoc = (signers[loopingDocument]).split(';', -1)

        signersPerDoc = (signers[loopingDocument]).split(';', -1).collect({ 
                it.trim()
            })

        signerInput.put(documentId[loopingDocument], signersPerDoc)
    }
    
    return signerInput
}

def inputFilterTrx(Connection conneSign, String currentDate, String noKontrak, String signType) {
    String documentType = ''

    if ((signType == 'Sign') || (signType == 'Document')) {
        documentType = CustomKeywords.'connection.APIFullService.getDocumentType'(conneSign, noKontrak)
    }
    
    'input filter dari saldo'
    WebUI.setText(findTestObject('Saldo/input_tipesaldo'), signType)

    if (signType == 'Stamp Duty') {
        WebUI.sendKeys(findTestObject('Saldo/input_tipesaldo'), Keys.chord(Keys.ARROW_DOWN))
    }
    
    'Input enter'
    WebUI.sendKeys(findTestObject('Saldo/input_tipesaldo'), Keys.chord(Keys.ENTER))

    'Input tipe transaksi'
    WebUI.setText(findTestObject('Saldo/input_tipetransaksi'), 'Use ' + signType)

    'Input enter'
    WebUI.sendKeys(findTestObject('Saldo/input_tipetransaksi'), Keys.chord(Keys.ENTER))

    'Input date sekarang'
    WebUI.setText(findTestObject('Saldo/input_fromdate'), currentDate)

    'Input referal number'
    WebUI.setText(findTestObject('Saldo/input_refnumber'), noKontrak)

    'Input date sekarang'
    WebUI.setText(findTestObject('Saldo/input_todate'), currentDate)

    'Klik cari'
    WebUI.click(findTestObject('Saldo/btn_cari'))
}

def checkVerifyEqualorMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write to excel status failed and ReasonFailedVerifyEqualorMatch'
        GlobalVariable.FlagFailed = 1

        'Jika equalnya salah maka langsung berikan reason bahwa reasonnya failed'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            ((findTestData(excelPathMain).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace('-', '') + 
            ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + reason)
    }
}
