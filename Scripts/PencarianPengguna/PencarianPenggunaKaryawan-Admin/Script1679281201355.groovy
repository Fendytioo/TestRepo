import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import java.sql.Connection as Connection
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.callTestCase(findTestCase('Login/Login_Admin'), [:], FailureHandling.STOP_ON_FAILURE)

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizeKeyword.writeExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'call function check paging'
checkPaging()

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.connectDB.connectDBeSign'()

if (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 7).equalsIgnoreCase('Email')) {
    'set text search box dengan email'
    WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_Email'), findTestData(excelPathPencarianPengguna).getValue(
            GlobalVariable.NumofColm, 11))
} else if (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 7).equalsIgnoreCase('Phone')) {
    'set text search box dengan Phone'
    WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_Email'), findTestData(excelPathPencarianPengguna).getValue(
            GlobalVariable.NumofColm, 10))
} else if (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 7).equalsIgnoreCase('Id no')) {
    'set text search box dengan NIK'
    WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_Email'), findTestData(excelPathPencarianPengguna).getValue(
            GlobalVariable.NumofColm, 9))
}

'click button cari'
WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_Cari'))

'check if edit / reset OTP / Resend Link'
if (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 6).equalsIgnoreCase('Edit')) {
	
	'click button reset OTP'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_View'))
	
	'get data view dari DB'
	ArrayList<String> resultData = CustomKeywords.'connection.dataVerif.getPencarianPengguna'(conneSign, findTestData(excelPathPencarianPengguna).getValue(
			GlobalVariable.NumofColm, 11).toUpperCase())
	
	index = 0
	
	'verify nama'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/View/input_Nama'), 'value', FailureHandling.OPTIONAL), resultData[index++], false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify Email'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/View/input_Email'), 'value', FailureHandling.OPTIONAL), resultData[index++], false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'parse Date from yyyy-MM-dd > dd-MMM-yyyy'
	sDate = CustomKeywords.'customizeKeyword.parseDate.parseDateFormat'(resultData[index++], 'yyyy-MM-dd', 'dd-MMM-yyyy')
	
	'verify tanggal lahir'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/View/input_TanggalLahir'), 'value', FailureHandling.OPTIONAL), sDate, false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify Status AutoSign'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/View/input_StatusAutoSign'), 'value', FailureHandling.OPTIONAL), resultData[index++], false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify Status'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/View/input_Status'), 'value', FailureHandling.OPTIONAL), resultData[index++], false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'click button kembali'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/View/button_Kembali'))
	
} else if (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 6).equalsIgnoreCase('Reset OTP')) {
    'click button reset OTP'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_ResetOTP'))

    'click button Ya Kirim OTP'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_YaKirimOTP'))

    if (WebUI.verifyElementPresent(findTestObject('PencarianPenggunaAdmin/Karyawan/MessagePopUp'), GlobalVariable.TimeOut, 
        FailureHandling.OPTIONAL)) {
        'click button OK'
        WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_OK'))

        'write to excel success'
        CustomKeywords.'customizeKeyword.writeExcel.writeToExcel'(GlobalVariable.DataFilePath, 'PencarianPengguna', 0, GlobalVariable.NumofColm - 
            1, GlobalVariable.StatusSuccess)

        'get data reset request OTP dari DB'
        String resultResetOTP = CustomKeywords.'connection.dataVerif.getResetOTP'(conneSign, findTestData(excelPathPencarianPengguna).getValue(
                GlobalVariable.NumofColm, 11).toUpperCase())

        'verify OTP reset menjadi 0'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(resultResetOTP, '0', false, FailureHandling.CONTINUE_ON_FAILURE))
    } else {
        'write to excel status failed dan reason'
        CustomKeywords.'customizeKeyword.writeExcel.writeToExcelStatusReason'('PencarianPengguna', GlobalVariable.NumofColm, 
            GlobalVariable.StatusFailed, ((findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 2).replace(
                '-', '') + ';') + GlobalVariable.ReasonFailedResend) + ' OTP')
    }
}

def checkPaging() {
    'click menu pencarian pengguna'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/menu_PencarianPengguna'))

    'click menu pelanggan'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/menu_Karyawan'))

    'input email'
    WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_Email'), 'AAA@EMAIL.COM')

	'input nama lengkap'
	WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_NamaLengkap'), 'AAAAAAAAAA')
	
	'input tanggal aktivasi dari'
	WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_TanggalAktivasiDari'), '2023-01-01')
	
	'input tanggal aktivasi sampai'
	WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/input_TanggalAktivasiSampai'), '2023-01-01')
	
	'input status aktivasi'
	WebUI.setText(findTestObject('PencarianPenggunaAdmin/Karyawan/Select_Status'), 'Active')
	
	'send keys enter'
	WebUI.sendKeys(findTestObject('PencarianPenggunaAdmin/Karyawan/Select_Status'), Keys.chord(Keys.ENTER))
	
    'click button reset'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_Reset'))
	
    'verify input email'
    checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/input_Email'), 
    		'value', FailureHandling.CONTINUE_ON_FAILURE), '', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify input nama lengkap'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/input_NamaLengkap'),
			'value', FailureHandling.CONTINUE_ON_FAILURE), '', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify tanggal dari'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/input_TanggalAktivasiDari'),
			'value', FailureHandling.CONTINUE_ON_FAILURE), '', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify tanggal sampai'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/input_TanggalAktivasiSampai'),
			'value', FailureHandling.CONTINUE_ON_FAILURE), '', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'verify status'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/Select_Status'),
			'value', FailureHandling.CONTINUE_ON_FAILURE), '', false, FailureHandling.CONTINUE_ON_FAILURE))
	
    'click button cari'
    WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_Cari'))
	
	'click page 2'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/page_2'))
	
	'verify page 2 active'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/label_Page'), 'ng-reflect-page'), '2', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'click page 1'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/page_1'))
	
	'verify page 1 active'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/label_Page'), 'ng-reflect-page'), '1', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'click next page'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_NextPage'))
	
	'verify page 2 active'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/label_Page'), 'ng-reflect-page'), '2', false, FailureHandling.CONTINUE_ON_FAILURE))
	
	'click previous page'
	WebUI.click(findTestObject('PencarianPenggunaAdmin/Karyawan/button_PreviousPage'))
	
	'verify page 1 active'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('PencarianPenggunaAdmin/Karyawan/label_Page'), 'ng-reflect-page'), '1', false, FailureHandling.CONTINUE_ON_FAILURE))

}

def checkVerifyEqualOrMatch(Boolean isMatch) {
    if ((isMatch == false) && (GlobalVariable.FlagFailed == 0)) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizeKeyword.writeExcel.writeToExcelStatusReason'('PencarianPengguna', GlobalVariable.NumofColm, 
            GlobalVariable.StatusFailed, (findTestData(excelPathPencarianPengguna).getValue(GlobalVariable.NumofColm, 2) + ';') + 
            GlobalVariable.ReasonFailedVerifyEqualOrMatch)

        GlobalVariable.FlagFailed = 1
    }
}
