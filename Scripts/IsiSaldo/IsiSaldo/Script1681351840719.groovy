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
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.By as By
import org.openqa.selenium.Keys as Keys

GlobalVariable.FlagFailed = 0

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizeKeyword.writeExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.connectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathIsiSaldo).getColumnNumbers()

'looping isi saldo'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {

        'call test case login admin esign'
        WebUI.callTestCase(findTestCase('Login/Login_AdminEsign'), [:], FailureHandling.STOP_ON_FAILURE)

        'click menu isi saldo'
        WebUI.click(findTestObject('isiSaldo/menu_isiSaldo'))

        'get ddl tenant'
        ArrayList<String> resultTenant = CustomKeywords.'connection.dataVerif.getDDLTenant'(conneSign)

		'call function check ddl untuk tenant'
		checkDDL(findTestObject('isiSaldo/input_PilihTenant'), resultTenant)

        'input tenant'
        WebUI.setText(findTestObject('isiSaldo/input_PilihTenant'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                9))

        'enter untuk input tenant'
        WebUI.sendKeys(findTestObject('isiSaldo/input_PilihTenant'), Keys.chord(Keys.ENTER))

        'get ddl vendor'
        ArrayList<String> resultVendor = CustomKeywords.'connection.dataVerif.getDDLVendor'(conneSign, findTestData(excelPathIsiSaldo).getValue(
                GlobalVariable.NumofColm, 9))

		'call function check ddl untuk vendor'
		checkDDL(findTestObject('isiSaldo/input_PilihVendor'), resultVendor)
		
        'input vendor'
        WebUI.setText(findTestObject('isiSaldo/input_PilihVendor'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                10))

        'enter untuk input vendor'
        WebUI.sendKeys(findTestObject('isiSaldo/input_PilihVendor'), Keys.chord(Keys.ENTER))

        'get ddl tipe saldo'
        ArrayList<String> resultTipeSaldo = CustomKeywords.'connection.dataVerif.getDDLTipeSaldo'(conneSign, findTestData(
                excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 9), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                10))

		'call function check ddl untuk vendor'
		checkDDL(findTestObject('isiSaldo/input_TipeSaldo'), resultTipeSaldo)

        'input tipe saldo'
        WebUI.setText(findTestObject('isiSaldo/input_TipeSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                11))

        'enter untuk input tipe saldo'
        WebUI.sendKeys(findTestObject('isiSaldo/input_TipeSaldo'), Keys.chord(Keys.ENTER))

        'input tambah saldo'
        WebUI.setText(findTestObject('isiSaldo/input_TambahSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                12))

        'input nomor tagihan'
        WebUI.setText(findTestObject('isiSaldo/input_nomorTagihan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                13))

        'input catatan'
        WebUI.setText(findTestObject('isiSaldo/input_Catatan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                14))

        'input tanggal pembelian'
        WebUI.setText(findTestObject('isiSaldo/input_TanggalPembelian'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                15))

        'click field untuk refresh button lanjut agar bisa di click'
        WebUI.click(findTestObject('isiSaldo/input_Catatan'))

        'declare isMmandatory Complete'
        int isMandatoryComplete = Integer.parseInt(findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 4))

        'check mandatory excel = 0'
        if ((isMandatoryComplete == 0) && !(WebUI.verifyElementHasAttribute(findTestObject('isiSaldo/button_Lanjut'), 'disabled', GlobalVariable.TimeOut, FailureHandling.OPTIONAL))) {
            'click lanjut'
            WebUI.click(findTestObject('isiSaldo/button_Lanjut'))

            'click ya proses'
            WebUI.click(findTestObject('isiSaldo/button_YaProses'))

            'write to excel success'
            CustomKeywords.'customizeKeyword.writeExcel.writeToExcel'(GlobalVariable.DataFilePath, 'isiSaldo', 0, GlobalVariable.NumofColm - 
                1, GlobalVariable.StatusSuccess)

            if (GlobalVariable.checkStoreDB == 'Yes') {
                'call test case store db'
                WebUI.callTestCase(findTestCase('IsiSaldo/IsiSaldoStoreDB'), [('excelPathIsiSaldo') : 'Saldo/isiSaldo'], 
                    FailureHandling.STOP_ON_FAILURE)
            }
        } else if (isMandatoryComplete > 0) {
            'click batal'
            WebUI.click(findTestObject('isiSaldo/button_Batal'))

            'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedMandatory'
            CustomKeywords.'customizeKeyword.writeExcel.writeToExcelStatusReason'('isiSaldo', GlobalVariable.NumofColm, 
                GlobalVariable.StatusFailed, (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 2) + ';') + 
                GlobalVariable.ReasonFailedMandatory)
        }
        
        'close browser'
        WebUI.closeBrowser()
    }
}

def checkVerifyEqualOrMatch(Boolean isMatch) {
    if ((isMatch == false) && (GlobalVariable.FlagFailed == 0)) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizeKeyword.writeExcel.writeToExcelStatusReason'('isiSaldo', GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch)

        GlobalVariable.FlagFailed = 1
    }
}

def checkDDL(TestObject objectDDL, ArrayList<String> listDB) {
	
	'declare array untuk menampung ddl'
	ArrayList<String> list = new ArrayList<String>()
	
	'click untuk memunculkan ddl'
	WebUI.click(objectDDL)

	'get id ddl'
	id = WebUI.getAttribute(findTestObject('isiSaldo/ddlClass'), 'id', FailureHandling.CONTINUE_ON_FAILURE)

	'get row'
	variable = DriverFactory.getWebDriver().findElements(By.cssSelector(('#' + id) + '> div > div:nth-child(2) div'))

	'looping untuk get ddl kedalam array'
	for (i = 1; i < variable.size(); i++) {
		'modify object DDL'
		modifyObjectDDL = WebUI.modifyObjectProperty(findTestObject('isiSaldo/modifyObject'), 'xpath', 'equals', ((('//*[@id=\'' +
			id) + '-') + i) + '\']', true)

		'add ddl ke array'
		list.add(WebUI.getText(modifyObjectDDL))
	}
	
	'verify ddl ui = db'
	checkVerifyEqualOrMatch(listDB.containsAll(list))

	'verify jumlah ddl ui = db'
	checkVerifyEqualOrMatch(WebUI.verifyEqual(list.size(), listDB.size(), FailureHandling.CONTINUE_ON_FAILURE))
}