import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import java.sql.Connection as Connection
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.By as By
import org.openqa.selenium.Keys as Keys

'get row'
variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-header > div > div.datatable-row-center.ng-star-inserted datatable-header-cell'))

println(variable.size())

GlobalVariable.FlagFailed = 0

'get data file path'
GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2. Esign.xlsx')

'connect DB eSign'
Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

'get colm excel'
int countColmExcel = findTestData(excelPathIsiSaldo).columnNumbers

int countCheckSaldo

'declare variable array'
ArrayList<String> saldoBefore, saldoAfter

'looping isi saldo'
for (GlobalVariable.NumofColm = 2; GlobalVariable.NumofColm <= countColmExcel; (GlobalVariable.NumofColm)++) {
    if (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 1).length() == 0) {
        break
    } else if (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 1).equalsIgnoreCase('Unexecuted')) {
		'counter check saldo'
        countCheckSaldo = 0

        'call function login admin get saldo'
        saldoBefore = loginAdminGetSaldo(countCheckSaldo, conneSign)

        'counter after check saldo'
        countCheckSaldo = 1

        'call test case login per case'
		WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('sheet') : sheet, ('Path') : excelPathIsiSaldo, ('Email') : 'Email Login', ('Password') : 'Password Login'
			, ('Perusahaan') : 'Perusahaan Login', ('Peran') : 'Peran Login'], FailureHandling.STOP_ON_FAILURE)

		'check if button menu visible atau tidak'
		if(WebUI.verifyElementNotVisible(findTestObject('isiSaldo/menu_isiSaldo'), FailureHandling.OPTIONAL)) {
			'click menu saldo'
			WebUI.click(findTestObject('button_HamburberSideMenu'))
		}
		
//		if(GlobalVariable.NumofColm == 2) {
//			'call function input cancel'
//			inputCancel()
//		}

        'click menu isi saldo'
        WebUI.click(findTestObject('isiSaldo/menu_isiSaldo'))

        'get ddl tenant'
        ArrayList<String> resultTenant = CustomKeywords.'connection.Saldo.getDDLTenant'(conneSign)

        'call function check ddl untuk tenant'
        checkDDL(findTestObject('isiSaldo/input_PilihTenant'), resultTenant)

        'input tenant'
        WebUI.setText(findTestObject('isiSaldo/input_PilihTenant'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                14))

        'enter untuk input tenant'
        WebUI.sendKeys(findTestObject('isiSaldo/input_PilihTenant'), Keys.chord(Keys.ENTER))

        'get ddl vendor'
        ArrayList<String> resultVendor = CustomKeywords.'connection.Saldo.getDDLVendor'(conneSign, findTestData(excelPathIsiSaldo).getValue(
                GlobalVariable.NumofColm, 14))

        'call function check ddl untuk vendor'
        checkDDL(findTestObject('isiSaldo/input_PilihVendor'), resultVendor)

        'input vendor'
        WebUI.setText(findTestObject('isiSaldo/input_PilihVendor'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                15))

        'enter untuk input vendor'
        WebUI.sendKeys(findTestObject('isiSaldo/input_PilihVendor'), Keys.chord(Keys.ENTER))

        'get ddl tipe saldo'
        ArrayList<String> resultTipeSaldo = CustomKeywords.'connection.Saldo.getDDLTipeSaldoActive'(conneSign, findTestData(
                excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 14), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                15))

        'call function check ddl untuk vendor'
        checkDDL(findTestObject('isiSaldo/input_TipeSaldo'), resultTipeSaldo)

        'input tipe saldo'
        WebUI.setText(findTestObject('isiSaldo/input_TipeSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                16))

        'enter untuk input tipe saldo'
        WebUI.sendKeys(findTestObject('isiSaldo/input_TipeSaldo'), Keys.chord(Keys.ENTER))

        'input tambah saldo'
        WebUI.setText(findTestObject('isiSaldo/input_TambahSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                17))
		if (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 17) != '') {
        'tambah saldo before dengan jumlah isi saldo'
        saldoBefore.set(0, (Integer.parseInt(saldoBefore[0]) + Integer.parseInt(findTestData(excelPathIsiSaldo).getValue(
                    GlobalVariable.NumofColm, 17))))

        saldoBefore.set(1, (Integer.parseInt(saldoBefore[1]) + Integer.parseInt(findTestData(excelPathIsiSaldo).getValue(
                    GlobalVariable.NumofColm, 17))))
		}
		
        'input nomor tagihan'
        WebUI.setText(findTestObject('isiSaldo/input_nomorTagihan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                18))

        'input catatan'
        WebUI.setText(findTestObject('isiSaldo/input_Catatan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                19))

        'input tanggal pembelian'
        WebUI.setText(findTestObject('isiSaldo/input_TanggalPembelian'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
                20))

        'click field untuk refresh button lanjut agar bisa di click'
        WebUI.click(findTestObject('isiSaldo/input_Catatan'))

        'declare isMmandatory Complete'
        int isMandatoryComplete = Integer.parseInt(findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 5))

        'check mandatory excel = 0'
        if ((isMandatoryComplete == 0) && !(WebUI.verifyElementHasAttribute(findTestObject('isiSaldo/button_Lanjut'), 'disabled', 
            GlobalVariable.TimeOut, FailureHandling.OPTIONAL))) {
            'click lanjut'
            WebUI.click(findTestObject('isiSaldo/button_Lanjut'))

            'click ya proses'
            WebUI.click(findTestObject('isiSaldo/button_YaProses'))

			if (GlobalVariable.FlagFailed == 0) {
            'write to excel success'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, 'isiSaldo', 0, GlobalVariable.NumofColm - 
                1, GlobalVariable.StatusSuccess)
			}
			
            if (GlobalVariable.checkStoreDB == 'Yes') {
                'delay 5 detik untuk menunggu trx'
				WebUI.delay(5)
				
                'call test case store db'
                WebUI.callTestCase(findTestCase('IsiSaldo/IsiSaldoStoreDB'), [('excelPathIsiSaldo') : 'Saldo/isiSaldo'], 
                    FailureHandling.CONTINUE_ON_FAILURE)
            }
            
            'close browser'
            WebUI.closeBrowser()

            'call function login admin get saldo'
            saldoAfter = loginAdminGetSaldo(countCheckSaldo, conneSign)

            'verify saldoafter tidak sama dengan saldo before'
            checkVerifyEqualOrMatch(WebUI.verifyMatch(saldoAfter.toString(), saldoBefore.toString(), false, FailureHandling.CONTINUE_ON_FAILURE), ' Saldo dimana saldo After = ' + saldoAfter.toString() + ' dan saldo Before adalah ' + saldoBefore.toString() + ' ')
        } else if (isMandatoryComplete > 0) {
            'click batal'
            WebUI.click(findTestObject('isiSaldo/button_Batal'))

            'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedMandatory'
            CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('isiSaldo', GlobalVariable.NumofColm, 
                GlobalVariable.StatusFailed, (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 2) + ';') + 
                GlobalVariable.ReasonFailedMandatory)

            'close browser'
            WebUI.closeBrowser()
        }
    }
}

def checkVerifyEqualOrMatch(Boolean isMatch, String reason) {
    if (isMatch == false) {
        'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedVerifyEqualOrMatch'
        CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'('isiSaldo', GlobalVariable.NumofColm, GlobalVariable.StatusFailed, 
            (findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 2) + ';') + GlobalVariable.ReasonFailedVerifyEqualOrMatch + reason)

        GlobalVariable.FlagFailed = 1
    }
}

def checkDDL(TestObject objectDDL, ArrayList<String> listDB) {
    'declare array untuk menampung ddl'
    ArrayList<String> list = []

    'click untuk memunculkan ddl'
    WebUI.click(objectDDL)

    'get id ddl'
    id = WebUI.getAttribute(findTestObject('isiSaldo/ddlClass'), 'id', FailureHandling.CONTINUE_ON_FAILURE)

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector(('#' + id) + '> div > div:nth-child(2) div'))

    'looping untuk get ddl kedalam array'
    for (i = 1; i < variable.size(); i++) {
        'modify object DDL'
        modifyObjectDDL = WebUI.modifyObjectProperty(findTestObject('isiSaldo/modifyObject'), 'xpath', 'equals', ((('//*[@id=\'' + 
            id) + '-') + i) + '\']', true)

        'add ddl ke array'
        list.add(WebUI.getText(modifyObjectDDL))
    }
    
    'verify ddl ui = db'
    checkVerifyEqualOrMatch(listDB.containsAll(list), ' DDL SALDO')

    'verify jumlah ddl ui = db'
    checkVerifyEqualOrMatch(WebUI.verifyEqual(list.size(), listDB.size(), FailureHandling.CONTINUE_ON_FAILURE), ' Jumlah DDL Saldo')
}

public loginAdminGetSaldo(int countCheckSaldo, Connection conneSign) {
    ArrayList<String> saldo = []
	
	'panggil fungsi login'
	WebUI.callTestCase(findTestCase('Login/Login_perCase'), [('sheet') : 'isiSaldo',
		('Path') : excelPathIsiSaldo], FailureHandling.CONTINUE_ON_FAILURE)

    'click ddl bahasa'
    WebUI.click(findTestObject('isiSaldo/SaldoAdmin/button_bahasa'))

    'click english'
    WebUI.click(findTestObject('isiSaldo/SaldoAdmin/button_English'))

    'select vendor'
    WebUI.selectOptionByLabel(findTestObject('isiSaldo/SaldoAdmin/select_Vendor'), '(?i)' + findTestData(excelPathIsiSaldo).getValue(
            GlobalVariable.NumofColm, 15), true)

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > div > div > div div'))

    for (index = 2; index <= variable.size(); index++) {
        'modify object box info'
        modifyObjectBoxInfo = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
            ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + index) + ']/div/div/div/div/div[1]/h3', 
            true)

        'check if box info = tipe saldo di excel'
        if (WebUI.getText(modifyObjectBoxInfo).equalsIgnoreCase(findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 16))) {
            'modify object qty'
            modifyObjectQty = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
                ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/div/div/div/div[' + index) + ']/div/div/div/div/div[2]/h3', 
                true)

            'get qty saldo before'
            saldo.add(WebUI.getText(modifyObjectQty).replace(',', ''))

            break
        }
    }
	
    'input tipe saldo'
    WebUI.setText(findTestObject('isiSaldo/SaldoAdmin/input_TipeSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm, 
            16))

    'enter untuk input tipe saldo'
    WebUI.sendKeys(findTestObject('isiSaldo/SaldoAdmin/input_TipeSaldo'), Keys.chord(Keys.ENTER))

    'input tipe transaksi'
    WebUI.setText(findTestObject('isiSaldo/SaldoAdmin/input_TipeTransaksi'), 'Topup ' + findTestData(excelPathIsiSaldo).getValue(
            GlobalVariable.NumofColm, 16))

    'enter untuk input tipe saldo'
    WebUI.sendKeys(findTestObject('isiSaldo/SaldoAdmin/input_TipeTransaksi'), Keys.chord(Keys.ENTER))

    'click button cari'
    WebUI.click(findTestObject('isiSaldo/SaldoAdmin/button_Cari'))

    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-footer > div > datatable-pager > ul li'))

    'modify object button last page'
    modifyObjectButtonLastPage = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 
        'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-footer/div/datatable-pager/ul/li[' + 
        variable.size()) + ']', true)

    if (WebUI.getAttribute(modifyObjectButtonLastPage, 'class', FailureHandling.OPTIONAL) != 'disabled') {
        'click button last page'
        WebUI.click(findTestObject('isiSaldo/SaldoAdmin/button_LastPage'))
    }
    
    'get row'
    variable = DriverFactory.webDriver.findElements(By.cssSelector('body > app-root > app-full-layout > div > div.main-panel > div > div.content-wrapper > app-balance > app-msx-paging > app-msx-datatable > section > ngx-datatable > div > datatable-body > datatable-selection > datatable-scroller datatable-row-wrapper'))

    'modify object balance'
    modifyObjectBalance = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
        ('/html/body/app-root/app-full-layout/div/div[2]/ div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
        variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[10]/div', true)

    'get trx saldo'
    saldo.add(WebUI.getText(modifyObjectBalance).replace(',', ''))

    if (countCheckSaldo == 1) {
        'modify object no transaksi'
        modifyObjectNoTransaksi = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 
            'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[1]/div', true)

        'modify object tanggal transaksi'
        modifyObjectTanggalTransaksi = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 
            'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[2]/div', true)

        'modify object tipe transaksi'
        modifyObjectTipeTransaksi = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 
            'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[3]/div', true)

        'modify object user'
        modifyObjectUser = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
            ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[4]/div', true)

        'modify object no kontrak'
        modifyObjectNoKontrak = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 
            'equals', ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[5]/div', true)

        'modify object Catatan'
        modifyObjectCatatan = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
            ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[8]/div', true)

        'modify object qty'
        modifyObjectQty = WebUI.modifyObjectProperty(findTestObject('isiSaldo/SaldoAdmin/modifyObject'), 'xpath', 'equals', 
            ('/html/body/app-root/app-full-layout/div/div[2]/div/div[2]/app-balance/app-msx-paging/app-msx-datatable/section/ngx-datatable/div/datatable-body/datatable-selection/datatable-scroller/datatable-row-wrapper[' + 
            variable.size()) + ']/datatable-body-row/div[2]/datatable-body-cell[9]/div', true)

        'get trx dari db'
        ArrayList<String> result = CustomKeywords.'connection.Saldo.getIsiSaldoTrx'(conneSign, findTestData(excelPathIsiSaldo).getValue(
                GlobalVariable.NumofColm, 18))

        arrayIndex = 0

        'verify no trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectNoTransaksi), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' no Trx')

        'verify tgl trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTanggalTransaksi), (result[arrayIndex++]).replace(
                    '.0', ''), false, FailureHandling.CONTINUE_ON_FAILURE), ' Tanggal Trx')

        'verify tipe trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectTipeTransaksi), result[arrayIndex++], false, 
                FailureHandling.CONTINUE_ON_FAILURE), ' Tipe Trx')

        'verify user trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectUser), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' user Trx')

        'verify no kontrak ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectNoKontrak), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' no Kontrak')

        'verify note trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectCatatan), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' note Trx')

        'verify qty trx ui = db'
        checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(modifyObjectQty), result[arrayIndex++], false, FailureHandling.CONTINUE_ON_FAILURE), ' qty Trx')
    }
    
    'close browser'
    WebUI.closeBrowser()

    return saldo
}

def inputCancel() {
	'input tenant'
	WebUI.setText(findTestObject('isiSaldo/input_PilihTenant'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			14))

	'enter untuk input tenant'
	WebUI.sendKeys(findTestObject('isiSaldo/input_PilihTenant'), Keys.chord(Keys.ENTER))
	
	'input vendor'
	WebUI.setText(findTestObject('isiSaldo/input_PilihVendor'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			15))

	'enter untuk input vendor'
	WebUI.sendKeys(findTestObject('isiSaldo/input_PilihVendor'), Keys.chord(Keys.ENTER))
	
	'input tipe saldo'
	WebUI.setText(findTestObject('isiSaldo/input_TipeSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			16))

	'enter untuk input tipe saldo'
	WebUI.sendKeys(findTestObject('isiSaldo/input_TipeSaldo'), Keys.chord(Keys.ENTER))

	'input tambah saldo'
	WebUI.setText(findTestObject('isiSaldo/input_TambahSaldo'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			17))
	
	'input nomor tagihan'
	WebUI.setText(findTestObject('isiSaldo/input_nomorTagihan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			18))

	'input catatan'
	WebUI.setText(findTestObject('isiSaldo/input_Catatan'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			19))

	'input tanggal pembelian'
	WebUI.setText(findTestObject('isiSaldo/input_TanggalPembelian'), findTestData(excelPathIsiSaldo).getValue(GlobalVariable.NumofColm,
			20))

	'click field untuk refresh button lanjut agar bisa di click'
	WebUI.click(findTestObject('isiSaldo/input_Catatan'))
	
	'click batal'
	WebUI.click(findTestObject('isiSaldo/button_Batal'))
	
	'click tenant ddl'
	WebUI.click(findTestObject('isiSaldo/input_PilihTenant'))
	
	'verify ddl tenant kereset'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('isiSaldo/selected_DDL')), 'Pilih Tenant', false, FailureHandling.CONTINUE_ON_FAILURE), ' ddl tenant tidak tereset')
	
	'enter untuk input tenant'
	WebUI.sendKeys(findTestObject('isiSaldo/input_PilihTenant'), Keys.chord(Keys.ENTER))
	
	'click vendor ddl'
	WebUI.click(findTestObject('isiSaldo/input_PilihVendor'))
	
	'verify ddl vendor kereset'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('isiSaldo/selected_DDL')), 'Pilih Vendor', false, FailureHandling.CONTINUE_ON_FAILURE), ' ddl vendor tidak tereset')
	
	'enter untuk input vendor'
	WebUI.sendKeys(findTestObject('isiSaldo/input_PilihVendor'), Keys.chord(Keys.ENTER))
	
	'click tipe saldo ddl'
	WebUI.click(findTestObject('isiSaldo/input_TipeSaldo'))
	
	'verify ddl tipe saldo kereset'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getText(findTestObject('isiSaldo/selected_DDL')), 'Pilih Tipe Saldo', false, FailureHandling.CONTINUE_ON_FAILURE), ' ddl tipe saldo tidak tereset')
	
	'enter untuk input tipe saldo'
	WebUI.sendKeys(findTestObject('isiSaldo/input_TipeSaldo'), Keys.chord(Keys.ENTER))
	
	'verify field tambah saldo kosong'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('isiSaldo/input_TambahSaldo'), 'value', FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' field tambah saldo')
	
	'verify field no tagihan kosong'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('isiSaldo/input_nomorTagihan'), 'value', FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' field nomor tagihan')
	
	'verify field Catatan kosong'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('isiSaldo/input_Catatan'), 'value', FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' field catatan')
	
	'verify field tanggal pemeblian kosong'
	checkVerifyEqualOrMatch(WebUI.verifyMatch(WebUI.getAttribute(findTestObject('isiSaldo/input_TanggalPembelian'), 'value', FailureHandling.OPTIONAL), '', false, FailureHandling.CONTINUE_ON_FAILURE), ' field tanggal pembelian')
}