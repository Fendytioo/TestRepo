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
import java.util.concurrent.ConcurrentHashMap.KeySetView as KeySetView
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.configuration.RunConfiguration as RunConfiguration
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

if (flowGetSignLink == '' || flowGetSignLink == null) {
	
	if (WebUI.verifyElementPresent(findTestObject('Object Repository/APIFullService/Send to Sign/button_TTDBulk'), GlobalVariable.TimeOut)) {
		WebUI.focus(findTestObject('Object Repository/APIFullService/Send to Sign/button_TTDBulk'))
		
		'Klik button ttd bulk'
		WebUI.click(findTestObject('Object Repository/APIFullService/Send to Sign/button_TTDBulk'))
		
		'Klik ya untuk konfirmasi ttd'
		WebUI.click(findTestObject('KotakMasuk/Sign/btn_YaKonfirmasiTTD'))
	}
	'change frame ke digisign'
	WebUI.switchToFrame(findTestObject('Signing-DIGISIGN/iFrame'), GlobalVariable.TimeOut)
}

'jika bagian text docId menjadi Bulk Sign, masuk ke single sign doc, lainnya akan masuk bulk sign'
if(WebUI.getText(findTestObject('Signing-DIGISIGN/text_documentId')) != 'Bulk Sign') {
	
	'jika document id pada page signing digisign sama dengan documentId yang diberikan'
	if (WebUI.getText(findTestObject('Signing-DIGISIGN/text_documentId')).contains(documentId[0])) {
		'checknya true'
		checkVerifyEqualorMatch(true, '')
	} else {
		'checknya false dan alasannya adalah document id tidak sesuai'
		checkVerifyEqualorMatch(false, 'document id tidak sesuai dengan yang diinput dimana document id yang muncul adalah ' +
			WebUI.getText(findTestObject('Signing-DIGISIGN/text_documentId')))
	}
	
	'Jika button signnya ada'
	if (WebUI.verifyElementPresent(findTestObject('Signing-DIGISIGN/button_sign'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
		'beri delay 3 detik'
		WebUI.delay(3)
	
		'ambil value total click sign location berdasarkan jumlah signer pada nomor kontrak tersebut'
		totalClickedSignLocation = CustomKeywords.'connection.APIFullService.getCountTtdLocation'(conneSign, nomorKontrak, emailSigner.toUpperCase())

		'looping sign location hingga total click sign locationnya habis'
		for (loopingSignLocation = 0; loopingSignLocation < totalClickedSignLocation; loopingSignLocation++) {
			'modify button sign dimana per button sign akan di click'
			buttonSign = WebUI.modifyObjectProperty(findTestObject('Signing-DIGISIGN/button_sign'), 'xpath', 'equals', ('//*[@id = \'sgnClick-' +
				loopingSignLocation) + '\']', true)
	
			'scroll menuju button sign tersebut'
			WebUI.scrollToElement(buttonSign, GlobalVariable.TimeOut, FailureHandling.OPTIONAL)
			
			'pencet arrow up agar bisa diclick'
			WebUI.sendKeys(buttonSign, Keys.chord(Keys.PAGE_UP))
			
			for (p = 1; p <= 5; p++) {
				'jika button tersebut bisible'
				if (WebUI.verifyElementVisible(buttonSign, FailureHandling.OPTIONAL)) {
					'click button sign'
					WebUI.click(buttonSign, FailureHandling.OPTIONAL)
					break
				}
			}
		}
	}
	
	'scroll menuju proses ttd'
	WebUI.scrollToElement(findTestObject('Signing-DIGISIGN/button_prosesTtd'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)
	
	'click proses ttd'
	WebUI.click(findTestObject('Signing-DIGISIGN/button_prosesTtd'))
} else {
	'ambil dokumen id by size'
	for (int i = 1; i <= documentId.size(); i++) {
		'modifikasi ambil object docID'
		modifyObjectdocId = WebUI.modifyObjectProperty(findTestObject('Object Repository/Signing-DIGISIGN/docIdBulkSign'), 'xpath', 'equals',
			("//div[@id='pdf-main-container']/div/ul/li["+ i +"]/label"), true)
		
		'pastikan sesuai dengan inputan yang ada di excel'
		checkVerifyEqualorMatch(WebUI.verifyMatch(documentId[i-1], WebUI.getText(modifyObjectdocId).replace('DOC_','').replace('.pdf',''),
			false, FailureHandling.CONTINUE_ON_FAILURE), 'Document id ke - '+ i +' tidak sesuai')
	}
	
	'klik pada proses bulksign'
	WebUI.click(findTestObject('Object Repository/Signing-DIGISIGN/button_ProsesBulk'))
}

'beri delay 2 detik untuk loading proses ttd'
WebUI.delay(2)

'jika popup gagalnya muncul'
if (WebUI.verifyElementVisible(findTestObject('Signing-DIGISIGN/text_popupGagal'), FailureHandling.OPTIONAL)) {
	
	'reason failed sesuai dengan text pop up gagal'
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
		((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
			'-', '') + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + WebUI.getText(findTestObject('Signing-DIGISIGN/text_popupGagal')))
	
	'click button close popup gagal'
    WebUI.click(findTestObject('Signing-DIGISIGN/button_closePopupGagal')) 
	//WebUI.setText(findTestObject('Signing-DIGISIGN/input_Otp'), 'otp')
    //WebUI.setText(findTestObject('Signing-DIGISIGN/input_Password'), 'password')
} else {
	'jika popup success muncul'
    if (WebUI.verifyElementVisible(findTestObject('Signing-DIGISIGN/text_popupSuccess'), FailureHandling.OPTIONAL)) {
        'check email signer'
		checkVerifyEqualorMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('Signing-DIGISIGN/text_email'), 'value').toUpperCase(), 
                emailSigner.toUpperCase(), false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Email Signer')

		'check nomor telepon signer'
        checkVerifyEqualorMatch(WebUI.verifyMatch(CustomKeywords.'customizekeyword.ParseText.convertToSHA256'(WebUI.getAttribute(
                        findTestObject('Signing-DIGISIGN/text_noHp'), 'value')), CustomKeywords.'connection.APIFullService.getHashedNo'(
                    conneSign, emailSigner.toUpperCase()), false, FailureHandling.CONTINUE_ON_FAILURE), ' pada Nomor telepon Signer')

		if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Correct OTP (Yes/No)')) == 'Yes') {
		'click request otp untuk digisign'
        WebUI.click(findTestObject('Signing-DIGISIGN/button_requestOtp'))
		} else {
			WebUI.setText(findTestObject('Signing-DIGISIGN/input_Otp'), findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Manual OTP')))
		}
		
		'delay 60 detik untuk input otp dari sms dan password'
        WebUI.delay(30)

		'jika menyetujui yes'
        if (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Menyetujui(Yes/No)')).split(';', -1)[GlobalVariable.indexUsed] == 'Yes') {
            'click button menyetujui'
			WebUI.click(findTestObject('Signing-DIGISIGN/button_menyetujui'))
        }
        
		'jika setuju tidak ada disabled'
        if (!(WebUI.verifyElementHasAttribute(findTestObject('Sigining-DIGISIGN/button_setuju'), 'disabled', GlobalVariable.TimeOut, 
            FailureHandling.OPTIONAL))) {
			'click button setuju'
            WebUI.click(findTestObject('Signing-DIGISIGN/button_setuju'))
        }
        
		'berikan delay 10 detik untuk loading proses tanda tangan'
        WebUI.delay(10)

		'jika pop up after proses visible'
        if (WebUI.verifyElementVisible(findTestObject('Signing-DIGISIGN/text_selesai'), FailureHandling.OPTIONAL)) {
			'ambil value success dan failed'
			String success =  WebUI.getText(findTestObject('Signing-DIGISIGN/text_success'))
		
			String failed = WebUI.getText(findTestObject('Signing-DIGISIGN/text_failed'))
				
			'klik button selesai'
			WebUI.click(findTestObject('Signing-DIGISIGN/button_Selesai'))

            inputMasukanAndWriteResultSign(success, failed)

			'beri delay 2 detik'
            WebUI.delay(2)

            'Jika masukan ratingnya tidak kosong'
            if ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('$Rating')).split(';', 
                -1)[GlobalVariable.indexUsed]) != '') {
                'StoreDB mengenai masukan'
                masukanStoreDB(conneSign, emailSigner)
            }
        } else {

			'reason failed sesuai dengan text pop up gagal'
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
				((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
					'-', '') + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + WebUI.getText(findTestObject('Signing-DIGISIGN/text_popupGagalOtp')))
			
			'click button close popup gagal'
			WebUI.click(findTestObject('Signing-DIGISIGN/button_closePopupGagal'))
		
		}
    }
}

def rowExcel(String cellValue) {
    return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def checkVerifyEqualorMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write to excel status failed and ReasonFailedVerifyEqualorMatch'
        GlobalVariable.FlagFailed = 1

        'Jika equalnya salah maka langsung berikan reason bahwa reasonnya failed'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
                '-', '') + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch) + reason)
    }
}

def inputMasukanAndWriteResultSign(String success, String failed) {
    'Menarik value count success ke excel'
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Success') - 
        1, GlobalVariable.NumofColm - 1, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel(
                'Result Count Success')) + ';') + success)

    'Menarik value count failed ke excel'
    CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Result Count Failed') - 
        1, GlobalVariable.NumofColm - 1, (findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel(
                'Result Count Failed')) + ';') + failed)

    'Jika masukan ratingnya tidak kosong'
    if ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('$Rating')).split(';', -1)[GlobalVariable.indexUsed]) != 
    '') {
        'modify object starmasukan, jika bintang 1 = 2, jika bintang 2 = 4'
        modifyObjectstarMasukan = WebUI.modifyObjectProperty(findTestObject('KotakMasuk/Sign/span_starMasukan'), 'xpath', 
            'equals', ('//*[@id=\'rating\']/span[' + ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, 
                rowExcel('$Rating')).split(';', -1)[GlobalVariable.indexUsed]).toInteger() * 2)) + ']/span', true)

        'Klik bintangnya bintang berapa'
        WebUI.click(modifyObjectstarMasukan)
    }
    
    'Jika komentarnya tidak kosoong'
    if ((findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('comment')).split(';', -1)[GlobalVariable.indexUsed]) != 
    '') {
        'Input komentar di rating'
        WebUI.setText(findTestObject('KotakMasuk/Sign/input_komentarMasukan'), findTestData(excelPathFESignDocument).getValue(
                GlobalVariable.NumofColm, rowExcel('comment')).split(';', -1)[GlobalVariable.indexUsed])
    }
    
    'Scroll ke btn Kirim'
    WebUI.scrollToElement(findTestObject('KotakMasuk/Sign/btn_Kirim'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)

    'klik button Kirim'
    WebUI.click(findTestObject('KotakMasuk/Sign/btn_Kirim'))
}

def masukanStoreDB(Connection conneSign, String emailSigner) {
    ArrayList arrayMatch = []

    'deklarasi arrayIndex untuk penggunakan selanjutnya'
    arrayIndex = 0

    'MasukanDB mengambil value dari hasil query'
    masukanDB = CustomKeywords.'connection.APIFullService.getFeedbackStoreDB'(conneSign, emailSigner)

    'verify rating'
    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('$Rating')).split(
                ';', -1)[GlobalVariable.indexUsed], masukanDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))

    'verify komentar'
    arrayMatch.add(WebUI.verifyMatch(findTestData(excelPathFESignDocument).getValue(GlobalVariable.NumofColm, rowExcel('comment')).split(
                ';', -1)[GlobalVariable.indexUsed], masukanDB[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE))
}

