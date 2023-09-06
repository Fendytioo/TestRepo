import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import org.openqa.selenium.WebDriver as WebDriver
import org.openqa.selenium.By as By
import java.time.LocalDate as LocalDate
import org.openqa.selenium.JavascriptExecutor as JavascriptExecutor

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'connect dengan db'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathMessageDeliveryReport).columnNumbers

'get dates'
currentDate = LocalDate.now()

firstDateOfMonth = currentDate.withDayOfMonth(1)

int firstRun = 0

'looping saldo'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
	if (findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Status')).length() == 0) {
		break
	} else if (findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Status')).equalsIgnoreCase('Unexecuted')) {
		'set penanda error menjadi 0'
		GlobalVariable.FlagFailed = 0

		'get tenant dari excel percase'
		GlobalVariable.Tenant = findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
		
		'get psre dari excel percase'
		GlobalVariable.Psre = findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Psre Login'))
		
		if (findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm - 1, rowExcel('Email Login')) != 
			findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Email Login')) || firstRun == 0) {
			'call test case login per case'
			WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('SheetName') : sheet, ('Path') : excelPathMessageDeliveryReport, ('Email') : 'Email Login', ('Password') : 'Password Login'
				, ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.CONTINUE_ON_FAILURE)
			
			firstRun = 1
		}
		
		'ambil index tab yang sedang dibuka di chrome'
		int currentTab = WebUI.getWindowIndex()

		'setting zoom menuju 80 persen'
		zoomSetting(80)

		'ganti fokus robot ke tab baru'
		WebUI.switchToWindowIndex(currentTab)
		
		'click menu MessageDeliveryReport'
		WebUI.click(findTestObject('MessageDeliveryReport/menu_MessageDeliveryReport'))
		
		'click menu MessageDeliveryReport'
		WebUI.click(findTestObject('MessageDeliveryReport/menu_MessageDeliveryReport'))
		
		'ambil index tab yang sedang dibuka di chrome'
		currentTab = WebUI.getWindowIndex()

		'setting zoom menuju 80 persen'
		zoomSetting(100)

		'ganti fokus robot ke tab baru'
		WebUI.switchToWindowIndex(currentTab)
		
		'click ddl bahasa'
		WebUI.click(findTestObject('Login/button_bahasa'))
		
		'click english'
		WebUI.click(findTestObject('Login/button_English'))

		if (GlobalVariable.NumofColm == 2) {
			'call function check paging'
			checkPaging(currentDate, firstDateOfMonth, conneSign)
			
			'get ddl tipe Saldo'
			ArrayList<String> resultVendor = CustomKeywords.'connection.messageDeliveryReport.getDDLVendor'(conneSign, GlobalVariable.Tenant)
			
			checkDDL(findTestObject('MessageDeliveryReport/input_vendor'), resultVendor, 'DDL Vendor')
			
			'get ddl tipe Saldo'
			ArrayList<String> resultMessageMedia = CustomKeywords.'connection.messageDeliveryReport.getDDLMessageMedia'(conneSign)
			
			checkDDL(findTestObject('MessageDeliveryReport/input_messageMedia'), resultMessageMedia, 'DDL Message Media')
			}
			
			inputMessageDeliveryReport()
		
			'Input enter'
			WebUI.click(findTestObject('MessageDeliveryReport/button_search'))
			
			if (checkErrorLog() == true) {
				continue
			}
			
		'jika hasil pencarian tidak memberikan hasil'
		if(WebUI.verifyElementPresent(findTestObject('Object Repository/MessageDeliveryReport/labebl_TableVendor'),
			GlobalVariable.TimeOut, FailureHandling.OPTIONAL)){
		
		'Jika bukan di page 1, verifikasi menggunakan button Lastest. Get row lastest'
		getRow = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-list-message-delivery-report > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller > datatable-row-wrapper datatable-body-row'))
		println getRow
		
		ArrayList<String> result = CustomKeywords.'connection.Saldo.getTrxSaldo'(conneSign, findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Tanggal Transaksi Dari')), 
			findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Nomor Kontrak')), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Tipe Transaksi')), 
			findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Nama Dokumen')))
		}
		arrayIndex = 0
		
		'verify trx no ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableTrxNo')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Trx No')
		
		'verify tgl trx ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableTglTrx')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Tgl Trx')
		
		'verify tipe trx ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableTipeTrx')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Tipe Trx')
		
		'verify trx oleh ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableTrxOleh')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Trx Oleh')
		
		'verify no kontrak ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableNoKontrak')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' No Kontrak')
		
		'verify tipe dok ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableTipeDok')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Tipe Dok')
		
		'verify nama dok ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableNamaDok')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Nama Dok')
		
		'verify notes ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableNotes')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Note')
		
		'verify qty ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/label_TableQty')), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' Qty')
		
		'verify Total Data ui = db'
		checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('Object Repository/Saldo/Label_TotalSaldo')), (result.size()/9).toString() + ' total', false, FailureHandling.CONTINUE_ON_FAILURE), ' Total Data Tidak Match')
		
		if (findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Download File')) == 'Yes'){
			
			'klik pada tombol unduh excel'
			WebUI.click(findTestObject('Object Repository/Saldo/button_UnduhExcel'))
			
			WebUI.delay(10)
			
			'pengecekan file yang sudah didownload'
			boolean isDownloaded = CustomKeywords.'customizekeyword.Download.isFileDownloaded'(findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Delete Downloaded File ?')))
			
			println(isDownloaded)
		
			'jika file tidak terdeteksi telah terdownload'
			checkVerifyEqualOrMatch(WebUI.verifyEqual(isDownloaded, true, FailureHandling.CONTINUE_ON_FAILURE), GlobalVariable.ReasonFailedDownload)	
		}
	}
}

'tutup browser'
WebUI.closeBrowser()

def inputMessageDeliveryReport() {
   'input vendor'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_vendor'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Vendor')))

	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_vendor'), Keys.chord(Keys.ENTER))

	'Input tipe message media'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_messageMedia'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Message Media')))

	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_messageMedia'), Keys.chord(Keys.ENTER))

	'Input report time start'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_reportTimeStart'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Report Time Start')))

	'Input date sekarang'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_reportTimeEnd'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Report Time End')))

	'Input tipe dokumen'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_deliveryStatus'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Status Delivery')))

	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_deliveryStatus'), Keys.chord(Keys.ENTER))

	'Input referal number'
	WebUI.setText(findTestObject('MessageDeliveryReport/input_recipient'), findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Recipient')))
}

def checkPaging(LocalDate currentDate, LocalDate firstDateOfMonth, Connection conneSign) {
	inputMessageDeliveryReport()

    'Klik set ulang'
    WebUI.click(findTestObject('MessageDeliveryReport/button_SetUlang'))

	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_vendor'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - vendor')
	
	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_messageMedia'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - message Media')

	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_reportTimeStart'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - report time start')
	
	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_reportTimeEnd'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - report time end')
	
	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_deliveryStatus'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - delivery status')
	
	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/input_recipient'), 'value', FailureHandling.CONTINUE_ON_FAILURE),
	'', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - recipient')
	
	'click ddl vendor'
	WebUI.click(findTestObject('MessageDeliveryReport/input_vendor'))

	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getText(findTestObject('MessageDeliveryReport/selected_DDL')), 'All', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - DDL tipe saldo')

	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_vendor'), Keys.chord(Keys.ENTER))
	
	'click ddl tipe transaksi'
	WebUI.click(findTestObject('MessageDeliveryReport/input_messageMedia'))

	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getText(findTestObject('MessageDeliveryReport/selected_DDL')), 'All', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - DDL tipe transaksi')
	
	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_messageMedia'), Keys.chord(Keys.ENTER))
	
	'click ddl tipe dokumen'
	WebUI.click(findTestObject('MessageDeliveryReport/input_deliveryStatus'))

	'verify field ke reset'
	checkVerifyPaging(WebUI.verifyMatch(WebUI.getText(findTestObject('MessageDeliveryReport/selected_DDLDeliveryStatus')), 'All', false, FailureHandling.CONTINUE_ON_FAILURE), ' field search form tidak kereset - DDL tipe dokumen')

	'Input enter'
	WebUI.sendKeys(findTestObject('MessageDeliveryReport/input_deliveryStatus'), Keys.chord(Keys.ENTER))

	'Input enter'
	WebUI.click(findTestObject('MessageDeliveryReport/button_search'))

	'ambil total trx berdasarkan filter yang telah disiapkan pada ui'
	totalTrxUI = WebUI.getText(findTestObject('MessageDeliveryReport/label_TotalMessageDeliveryReport')).split(' ', -1)

	'ambil total trx berdasarkan filter yang telah disiapkan pada db'
	totalTrxDB = CustomKeywords.'connection.messageDeliveryReport.getTotalMessageDeliveryReport'(conneSign, GlobalVariable.Tenant)
	
	'verify total Saldo'
	checkVerifyPaging(WebUI.verifyMatch(totalTrxUI[0], totalTrxDB, false, FailureHandling.CONTINUE_ON_FAILURE), ' total transaksi ui dan db tidak match')
	
	if (Integer.parseInt(totalTrxUI[0]) > 10) {
		'click next page'
		WebUI.click(findTestObject('MessageDeliveryReport/button_NextPage'))
	
		'verify paging di page 2'
		checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/paging_Page'), 'ng-reflect-page', FailureHandling.CONTINUE_ON_FAILURE),
				'2', false, FailureHandling.CONTINUE_ON_FAILURE), ' button page selanjutnya tidak berfungsi')
	
		'click prev page'
		WebUI.click(findTestObject('MessageDeliveryReport/button_PrevPage'))
	
		'verify paging di page 1'
		checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/paging_Page'), 'ng-reflect-page', FailureHandling.CONTINUE_ON_FAILURE),
				'1', false, FailureHandling.CONTINUE_ON_FAILURE), ' button page sebelumnya tidak berfungsi')
	
		'click last page'
		WebUI.click(findTestObject('MessageDeliveryReport/button_LastPage'))
	
		'verify paging di last page'
		checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/paging_Page'), 'ng-reflect-page', FailureHandling.CONTINUE_ON_FAILURE),
				WebUI.getAttribute(findTestObject('MessageDeliveryReport/page_Active'), 'aria-label', FailureHandling.CONTINUE_ON_FAILURE).replace(
					'page ', ''), false, FailureHandling.CONTINUE_ON_FAILURE), ' button page terakhir tidak berfungsi')
	
		'click first page'
		WebUI.click(findTestObject('MessageDeliveryReport/button_FirstPage'))
	
		'verify paging di page 1'
		checkVerifyPaging(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('MessageDeliveryReport/paging_Page'), 'ng-reflect-page', FailureHandling.CONTINUE_ON_FAILURE),
				'1', false, FailureHandling.CONTINUE_ON_FAILURE), ' button page pertama tidak berfungsi')
	}
}

def checkVerifyPaging(Boolean isMatch, String reason) {
	if (isMatch == false) {
		'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
			(findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedPaging + reason)

		GlobalVariable.FlagFailed = 1
	}
}

def checkVerifyEqualOrMatch(Boolean isMatch, String reason) {
	if (isMatch == false) {
		'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
		CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed,
			(findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch + reason)

		GlobalVariable.FlagFailed = 1
	}
}

def checkDDL(TestObject objectDDL, ArrayList<String> listDB, String reason) {
	'declare array untuk menampung ddl'
	ArrayList<String> list = []

	'click untuk memunculkan ddl'
	WebUI.click(objectDDL)

	'get id ddl'
	id = WebUI.getAttribute(findTestObject('MessageDeliveryReport/ddlClass'), 'id', FailureHandling.CONTINUE_ON_FAILURE)

	'get row'
	variable = DriverFactory.webDriver.findElements(By.cssSelector(('#' + id) + '> div > div:nth-child(2) div'))

	'looping untuk get ddl kedalam array'
	for (i = 1; i < variable.size(); i++) {
		'modify object DDL'
		modifyObjectDDL = WebUI.modifyObjectProperty(findTestObject('MessageDeliveryReport/modifyObject'), 'xpath', 'equals', ((('//*[@id=\'' +
			id) + '-') + i) + '\']', true)

		'add ddl ke array'
		list.add(WebUI.getText(modifyObjectDDL))
	}
	
	'verify ddl ui = db'
	checkVerifyEqualOrMatch(listDB.containsAll(list), reason)

	'verify jumlah ddl ui = db'
	checkVerifyEqualOrMatch(WebUI.verifyEqual(list.size(), listDB.size(), FailureHandling.CONTINUE_ON_FAILURE), ' Jumlah ' + reason)
	
	'Input enter untuk tutup ddl'
	WebUI.sendKeys(objectDDL, Keys.chord(Keys.ENTER))
}

def rowExcel(String cellValue) {
	return CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}

def zoomSetting(int percentage) {
	Float percentageZoom = percentage / 100

	WebDriver driver = DriverFactory.webDriver

	'buka tab baru'
		((driver) as JavascriptExecutor).executeScript('window.open();')

	'ambil index tab yang sedang dibuka di chrome'
	int currentTab = WebUI.getWindowIndex()

	'ganti fokus robot ke tab baru'
	WebUI.switchToWindowIndex(currentTab + 1)

	driver.get('chrome://settings/')

		((driver) as JavascriptExecutor).executeScript(('chrome.settingsPrivate.setDefaultZoom(' + percentageZoom.toString()) +
		');')

	'close tab baru'
		((driver) as JavascriptExecutor).executeScript('window.close();')
}

def checkErrorLog() {
	'Jika error lognya muncul'
	if (WebUI.verifyElementPresent(findTestObject('ManualSign/errorLog'), GlobalVariable.TimeOut, FailureHandling.OPTIONAL)) {
		'ambil teks errormessage'
		errormessage = WebUI.getAttribute(findTestObject('ManualSign/errorLog'), 'aria-label', FailureHandling.CONTINUE_ON_FAILURE)

		'jika error message null, masuk untuk tulis error non-sistem'
		if (errormessage != null) {
			if (!(errormessage.contains('Verifikasi OTP berhasil')) && !(errormessage.contains('feedback'))) {
				'Tulis di excel itu adalah error'
				CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
					GlobalVariable.StatusFailed, (((findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')).replace(
						'-', '') + ';') + '<') + errormessage) + '>')
				
				return true
			}
		} else {
			'Tulis di excel itu adalah error'
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
				GlobalVariable.StatusFailed, (((findTestData(excelPathMessageDeliveryReport).getValue(GlobalVariable.NumofColm, 2).replace(
					'-', '') + ';')) + 'Error tidak berhasil ditangkap'))
		}
	}
	return false
}