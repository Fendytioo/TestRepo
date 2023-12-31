import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import java.sql.Connection as Connection
import internal.GlobalVariable as GlobalVariable

Connection conneSign = CustomKeywords.'connection.ConnectDB.connectDBeSign'()

GlobalVariable.DataFilePath = CustomKeywords.'customizekeyword.WriteExcel.getExcelPath'('\\Excel\\2.1 Esign - API Only.xlsx')

int countColmExcel = findTestData(excelPath).columnNumbers

for (GlobalVariable.NumofColm; GlobalVariable.NumofColm <= countColmExcel; GlobalVariable.NumofColm++) {
	// Create a variable 'status' and store the value of the 'Status' cell in the current column
	def status = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Status'))

	// If status is empty, break the loop
	if (status == '') {
		break
	}

	// If status is 'Unexecuted', set GlobalVariable.FlagFailed to 0 and perform some actions
	if (status == 'Unexecuted') {
		GlobalVariable.FlagFailed = 0

		// Call a custom keyword 'settingBaseUrl' from the 'APIFullService' class with 3 parameters
		CustomKeywords.'connection.APIFullService.settingBaseUrl'(excelPath, GlobalVariable.NumofColm, rowExcel('Use Correct Base Url'))

		// Create a variable 'userCorrectTenantCode' and store the value of the 'Use Correct Tenant Code' cell in the current column
		def userCorrectTenantCode = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Use Correct Tenant Code'))

		// If userCorrectTenantCode is 'Yes', store the value of 'Tenant Login' cell in GlobalVariable.Tenant
		if (userCorrectTenantCode == 'Yes') {
			GlobalVariable.Tenant = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Tenant Login'))
		}
		// If userCorrectTenantCode is 'No', store the value of 'Wrong Tenant Code' cell in GlobalVariable.Tenant
		else if (userCorrectTenantCode == 'No') {
			GlobalVariable.Tenant = findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Wrong Tenant Code'))
		}

		// Send an API request to the Login object in the Postman repository and retrieve the response
		'HIT API Login untuk ambil bearer token'
		respon_login = WS.sendRequest(findTestObject('Postman/Login', [('username') : findTestData(excelPath).getValue(GlobalVariable.NumofColm,
			rowExcel('username')), ('password') : findTestData(excelPath).getValue(GlobalVariable.NumofColm,
			rowExcel('password'))]))

		// If the response status code is 200 and failurehandling.OPTIONAL is true, get the 'access_token' parameter from the response and store it in GlobalVariable.Token
		if (WS.verifyResponseStatusCode(respon_login, 200, FailureHandling.OPTIONAL) == true) {
			GlobalVariable.token = WS.getElementPropertyValue(respon_login, 'access_token')
			
			// Send an API request to the Vendor object in the Postman repository and retrieve the response
			respon = WS.sendRequest(findTestObject('Postman/getStatusEmailServiceTenant', [
				('callerId') : findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('username'))]))
	
			// Create a variable 'elapsedTime' and store the elapsed time of the request in seconds
			def elapsedTime = respon.getElapsedTime() / 1000 + ' seconds'
	
			// Create a variable 'responseBody' and store the response body content
			def responseBody = respon.getResponseBodyContent()
	
			// Call a custom keyword 'process' from the 'BeautifyJson' class with 5 parameters
			CustomKeywords.'customizekeyword.BeautifyJson.process'(responseBody, sheet, rowExcel('Respons') - 1, findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Scenario')))
	
			// Call a custom keyword 'writeToExcel' from the 'WriteExcel' class with 5 parameters
			CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, rowExcel('Process Time') - 1, GlobalVariable.NumofColm - 1, elapsedTime.toString())
	
			// If the response status code is 200 and failurehandling.OPTIONAL is true, get the 'status.code' parameter from the response and store it in 'status_Code'
			if (WS.verifyResponseStatusCode(respon, 200, FailureHandling.OPTIONAL) == true) {
				def status_Code = WS.getElementPropertyValue(respon, 'status.code')
	
				// If status_Code is 0, perform some actions
				if (status_Code == 0) {
					
					// If GlobalVariable.checkStoreDB is 'Yes', perform some actions
					if (GlobalVariable.checkStoreDB == 'Yes') {
						// Declare an ArrayList<String> called 'arrayMatch'
						def arrayMatch = []
	
						// Declare an ArrayList<String> called 'result' and store the result of the 'getVendorofTenant' custom keyword with 1 parameter
						def result = CustomKeywords.'connection.APIFullService.getStatusEmailServiceAPIOnly'(conneSign, GlobalVariable.Tenant)
						
						'deklarasi array index'
						arrayIndex = 0
						
						'verify code di API dengan DB'
						arrayMatch.add(WebUI.verifyMatch(result[arrayIndex++], WS.getElementPropertyValue(respon, 'tenantCode', FailureHandling.OPTIONAL).toString(), false, FailureHandling.CONTINUE_ON_FAILURE))
						
						'verify description di API dengan DB'
						arrayMatch.add(WebUI.verifyMatch(result[arrayIndex++], WS.getElementPropertyValue(respon, 'tenantName', FailureHandling.OPTIONAL).toString(), false, FailureHandling.CONTINUE_ON_FAILURE))
	
						'verify description di API dengan DB'
						arrayMatch.add(WebUI.verifyMatch(result[arrayIndex++], WS.getElementPropertyValue(respon, 'emailService', FailureHandling.OPTIONAL).toString(), false, FailureHandling.CONTINUE_ON_FAILURE))
						
						'jika data db tidak sesuai dengan excel'
						if (arrayMatch.contains(false)) {
							GlobalVariable.FlagFailed = 1
	
							'Write To Excel GlobalVariable.StatusFailed and GlobalVariable.ReasonFailedStoredDB'
							CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm,
								GlobalVariable.StatusFailed, (findTestData(excelPath).getValue(GlobalVariable.NumofColm,
									rowExcel('Reason Failed')) + ';') + GlobalVariable.ReasonFailedStoredDB)
						}
					}
					
					'tulis sukses jika store DB berhasil'
					if (GlobalVariable.FlagFailed == 0) {
						'write to excel success'
						CustomKeywords.'customizekeyword.WriteExcel.writeToExcel'(GlobalVariable.DataFilePath, sheet, 0,
							GlobalVariable.NumofColm - 1, GlobalVariable.StatusSuccess)
					}
				} else {
					getErrorMessageAPI(respon)
				}
			} else {
				getErrorMessageAPI(respon)
			}
		} else {
			getErrorMessageAPI(respon_login)
		}
	}
}

/*
 *  This function is used to get the error message from an API response and write it to the Excel file.
 *
 *  @param respon The API response
 */
def getErrorMessageAPI(respon) {
	// Get the 'status.message' parameter from the response and store it in 'message'
	message = WS.getElementPropertyValue(respon, 'status.message', FailureHandling.OPTIONAL)

	// Call a custom keyword 'writeToExcelStatusReason' from the 'WriteExcel' class with 4 parameters
	CustomKeywords.'customizekeyword.WriteExcel.writeToExcelStatusReason'(sheet, GlobalVariable.NumofColm, GlobalVariable.StatusFailed, findTestData(excelPath).getValue(GlobalVariable.NumofColm, rowExcel('Reason Failed')) + ';' + message)

	// Set GlobalVariable.FlagFailed to 1
	GlobalVariable.FlagFailed = 1
}

/*
 *  This function is used to get the row number of a cell in the Excel file.
 *
 *  @param cellValue The value of the cell
 *  @return The row number of the cell
 */
def rowExcel(cellValue) {
	// Call a custom keyword 'getExcelRow' from the 'WriteExcel' class with 3 parameters
	CustomKeywords.'customizekeyword.WriteExcel.getExcelRow'(GlobalVariable.DataFilePath, sheet, cellValue)
}
