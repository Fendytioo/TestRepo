import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection as Connection
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable

import org.openqa.selenium.By
import org.openqa.selenium.Keys as Keys

'get current date'
def currentDate = new Date().format('yyyy-MM-dd')

'click menu ErrorReport'
WebUI.click(findTestObject('ErrorReport/menu_ErrorReport'))

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'call function check paging'
checkPaging()

ArrayList<String> namasigner = findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 34).split(';', -1)

'click button cari'
WebUI.click(findTestObject('ErrorReport/button_Cari'))

'get total data error dari DB'
String resultTotalData = CustomKeywords.'connection.ErrorReport.getTotalDataError'(conneSign, WebUI.getAttribute(findTestObject(
            'ErrorReport/input_TanggalDari'), 'value'))

'get error detail dari DB'
String resultErrorDetail = CustomKeywords.'connection.ErrorReport.getErrorReportDetail'(conneSign, namasigner.last().toString().replace(
        '"', ''))

'get status activation'
ArrayList<String> resultStatusAktivasi = CustomKeywords.'connection.ErrorReport.getStatusActivation'(conneSign)

'get total data'
totalData = WebUI.getText(findTestObject('ErrorReport/label_TotalData')).split(' ')

'verify total data UI dan DB'
checkVerifyEqualOrMatch(WebUI.verifyMatch((totalData[0]).replace(',', ''), resultTotalData, false, FailureHandling.CONTINUE_ON_FAILURE), ' Total Data')

'select modul'
WebUI.setText(findTestObject('ErrorReport/select_Modul'), 'send document error history')

'send keys enter'
WebUI.sendKeys(findTestObject('ErrorReport/select_Modul'), Keys.chord(Keys.ENTER))

'input nama'
WebUI.setText(findTestObject('ErrorReport/input_Nama'), namasigner.last().toString().replace('"', ''))

'input no kontrak'
WebUI.setText(findTestObject('ErrorReport/input_NoKontrak'), findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 
        11).replace('"', ''))

'input lini bisnis'
WebUI.setText(findTestObject('ErrorReport/input_LiniBisnis'), findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 
        18).replace('"', ''))

'input cabang'
WebUI.setText(findTestObject('ErrorReport/input_Cabang'), findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 
        14).replace('"', ''))

'input Wilayah'
WebUI.setText(findTestObject('ErrorReport/input_Wilayah'), findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 
        16).replace('"', ''))

'input tanggal dari'
WebUI.setText(findTestObject('ErrorReport/input_TanggalDari'), currentDate)

'input tanggal ke'
WebUI.setText(findTestObject('ErrorReport/input_TanggalKe'), currentDate)

'click button cari'
WebUI.click(findTestObject('ErrorReport/button_Cari'))

//'verify match tipe error'
//checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_Tipe')), GlobalVariable.ErrorType, false, FailureHandling.OPTIONAL), ' Tipe Error')

'click button view'
WebUI.click(findTestObject('ErrorReport/button_View'))

'verify match error detail'
checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_ErrorDetail')), resultErrorDetail, 
        false, FailureHandling.OPTIONAL), ' Error Detail')

'click button X'
WebUI.click(findTestObject('ErrorReport/button_X'))

'click button status aktivasi'
WebUI.click(findTestObject('ErrorReport/button_StatusAktivasi'))

index = 0

'verify data UI dan DB'
checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_SignerType'), FailureHandling.CONTINUE_ON_FAILURE), 
        'Customer', false, FailureHandling.CONTINUE_ON_FAILURE), ' Signer Type')

'verify data UI dan DB'
checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_SignerName'), FailureHandling.CONTINUE_ON_FAILURE), 
        resultStatusAktivasi[index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Signer Nama')

'verify total data UI dan DB'
checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_SignerNIK'), FailureHandling.CONTINUE_ON_FAILURE), 
        resultStatusAktivasi[index++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Signer NIK')

'declare status'
String status

String isregis = resultStatusAktivasi[index++]

String isaktif = resultStatusAktivasi[index++]

'cek if regis = 0 && aktif = 0 maka status belum registrasi'
if ((isregis == '0') && (isaktif == '0')) {
    status = 'Belum Registrasi' //cek if regis = 1 && aktif = 0 maka status belum aktivasi
    //cek if regis = 1 && aktif = 1 maka status sudah aktivasi
} else if ((isregis == '1') && (isaktif == '0')) {
    status = 'Belum Aktivasi'
} else if ((isregis == '1') && (isaktif == '1')) {
    status = 'Sudah Aktivasi'
}

'verify total data UI dan DB'
checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('ErrorReport/label_StatusAktivasi'), FailureHandling.CONTINUE_ON_FAILURE), 
        status, false, FailureHandling.CONTINUE_ON_FAILURE), ' Total Data')

'click button X'
WebUI.click(findTestObject('ErrorReport/button_X'))

def checkVerifyEqualOrMatch(Boolean isMatch, String reason) {
    if ((isMatch == false) && (GlobalVariable.FlagFailed == 0)) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('API Send Document', GlobalVariable.NumofColm, 
            GlobalVariable.StatusFailed, (findTestData(API_Excel_Path).getValue(GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch + reason)

        GlobalVariable.FlagFailed = 1
    }
}

def checkVerifyPaging(Boolean isMatch, String reason) {
	if (isMatch == false) {
		'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('API Send Document', GlobalVariable.NumofColm,
			GlobalVariable.StatusFailed, (findTestData(excelPathBuatUndangan).getValue(GlobalVariable.NumofColm, 2) +
			';') + GlobalVariable.ReasonFailedPaging + reason)

		GlobalVariable.FlagFailed = 1
	}
}

def checkPaging() {
    'click button cari'
    WebUI.click(findTestObject('ErrorReport/button_Cari'))

    'click button set ulang'
    WebUI.click(findTestObject('Object Repository/ErrorReport/button_Reset'))

    'get defaultTanggalDari'
    String defaultTanggalDari = WebUI.getAttribute(findTestObject('ErrorReport/input_TanggalDari'), 'value')

    'get defaultTanggalKe'
    String defaultTanggalKe = WebUI.getAttribute(findTestObject('ErrorReport/input_TanggalKe'), 'value')

    'select modul'
    WebUI.setText(findTestObject('ErrorReport/select_Modul'), 'generate invitation link error history')

    'send keys enter'
    WebUI.sendKeys(findTestObject('ErrorReport/select_Modul'), Keys.chord(Keys.ENTER))

    'input no kontrak'
    WebUI.setText(findTestObject('ErrorReport/input_NoKontrak'), '0000')

    'input nama konsumen'
    WebUI.setText(findTestObject('ErrorReport/input_Nama'), 'abcd')

    'input lini bisnis'
    WebUI.setText(findTestObject('ErrorReport/input_LiniBisnis'), 'abcd')

    'input cabang'
    WebUI.setText(findTestObject('ErrorReport/input_Cabang'), 'abcd')

    'input wilayah'
    WebUI.setText(findTestObject('ErrorReport/input_Wilayah'), 'abcd')

    'input Tanggal dari'
    WebUI.setText(findTestObject('ErrorReport/input_TanggalDari'), '2012-12-12')

    'input Tanggal Ke'
    WebUI.setText(findTestObject('ErrorReport/input_TanggalKe'), '2020-12-12')

    'input Tanggal Ke'
    WebUI.setText(findTestObject('ErrorReport/select_Tipe'), 'REJECT')

    'send keys enter'
    WebUI.sendKeys(findTestObject('ErrorReport/select_Tipe'), Keys.chord(Keys.ENTER))

    'click button set ulang'
    WebUI.click(findTestObject('Object Repository/ErrorReport/button_Reset'))

    'click button cari'
    WebUI.click(findTestObject('ErrorReport/button_Cari'))

    'verif select modul'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/select_Modul'), 'value'), '', 
            false, FailureHandling.CONTINUE_ON_FAILURE), ' modul')

    'verif no kontrak'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_NoKontrak'), 'value'), 
            '', false, FailureHandling.CONTINUE_ON_FAILURE), ' no kontrak')

    'verif nama konsumen'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_Nama'), 'value'), '', 
            false, FailureHandling.CONTINUE_ON_FAILURE), ' nama konsumen')

    'verif lini bisnis'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_LiniBisnis'), 'value'), 
            '', false, FailureHandling.CONTINUE_ON_FAILURE), ' lini bisnis')

    'verif cabang'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_Cabang'), 'value'), '', 
            false, FailureHandling.CONTINUE_ON_FAILURE), ' cabang')

    'verif wilayah'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_Wilayah'), 'value'), 
            '', false, FailureHandling.CONTINUE_ON_FAILURE), ' wilayah')

    'verif tanggal dari'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_TanggalDari'), 'value'), 
            defaultTanggalDari, false, FailureHandling.CONTINUE_ON_FAILURE), ' tanggal dari')

    'verif tanggal ke'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/input_TanggalKe'), 'value'), 
            defaultTanggalKe, false, FailureHandling.CONTINUE_ON_FAILURE), ' tanggal ke')

    'verif select tipe'
    checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/select_Tipe'), 'value'), '', 
            false, FailureHandling.CONTINUE_ON_FAILURE), ' tipe')

    'click page 2'
    WebUI.click(findTestObject('ErrorReport/page_2'))

    'verify page 2 active'
    checkVerifyPaging(WebUI.getAttribute(findTestObject('ErrorReport/page_2'), 'class', FailureHandling.CONTINUE_ON_FAILURE).contains('active'), ' page 2')

    'click page 1'
    WebUI.click(findTestObject('ErrorReport/page_1'))

    'verify page 1 active'
    checkVerifyPaging(WebUI.getAttribute(findTestObject('ErrorReport/page_1'), 'class', FailureHandling.CONTINUE_ON_FAILURE).contains('active'), ' page 1')

    'click next page'
    WebUI.click(findTestObject('ErrorReport/button_NextPage'))

    'verify page 2 active'
    checkVerifyPaging(WebUI.getAttribute(findTestObject('ErrorReport/page_2'), 'class', FailureHandling.CONTINUE_ON_FAILURE).contains('active'), ' next page')

    'click previous page'
    WebUI.click(findTestObject('ErrorReport/button_PreviousPage'))

    'verify page 1 active'
    checkVerifyPaging(WebUI.getAttribute(findTestObject('ErrorReport/page_1'), 'class', FailureHandling.CONTINUE_ON_FAILURE).contains('active'), ' previous page')
	
	'get total page'
	variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-error-report > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-footer > div > datatable-pager > ul li'))
	
	'click max page'
	WebUI.click(findTestObject('ErrorReport/button_MaxPage'))

	'get total data'
	lastPage = Double.parseDouble(WebUI.getText(findTestObject('ErrorReport/label_TotalData')).split(' ',-1)[0])/10
	
	'jika hasil perhitungan last page memiliki desimal'
	if (lastPage.toString().contains('.0')) {
		'tidak ada round up'
		additionalRoundUp = 0
	} else {
		'round up dengan tambahan 0.5'
		additionalRoundUp = 0.5
	}
	
	'verify paging di page terakhir'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('ErrorReport/paging_Page'), 'aria-label',
				FailureHandling.CONTINUE_ON_FAILURE), 'page ' + Math.round(lastPage+additionalRoundUp).toString(), false, FailureHandling.CONTINUE_ON_FAILURE), 'last page')

	'click min page'
	WebUI.click(findTestObject('ErrorReport/button_MinPage'))

	 'verify page 1 active'
    checkVerifyPaging(WebUI.getAttribute(findTestObject('ErrorReport/page_1'), 'class', FailureHandling.CONTINUE_ON_FAILURE).contains('active'), ' first page')
}

