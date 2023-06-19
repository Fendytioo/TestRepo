package connection

import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement
import com.kms.katalon.core.annotation.Keyword
import internal.GlobalVariable

public class SendSign {

	String data
	int columnCount, i
	Statement stm
	ResultSetMetaData metadata
	ResultSet resultSet
	ArrayList<String> listdata = []

	@Keyword
	settingEmailServiceVendorRegisteredUser(Connection conn, String value, String email) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("UPDATE ms_vendor_registered_user SET email_service = "+ value +" WHERE signer_registered_email = '"+ email +"'")
	}

	@Keyword
	getSigningStatusProcess(Connection conn, String documentid, String emailsigner) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select tdsp.request_status, CASE WHEN tdds.sign_date is not null THEN 'Sudah TTD' end , tdd.total_signed from tr_document_d_sign tdds join tr_document_d tdd on tdds.id_document_d = tdd.id_document_d join am_msuser amm on tdds.id_ms_user = amm.id_ms_user join tr_document_signing_request tdsp on amm.id_ms_user = tdsp.id_ms_user where tdd.document_id = '"+documentid+"' and amm.login_id = '"+emailsigner+"' ORDER BY tdsp.dtm_crt desc limit 1 ")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getDataSendtoSign(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select tdh.ref_number, msl.description, TO_char(tdd.request_date, 'DD-Mon-YYYY HH24:MI') from tr_document_d tdd join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h join ms_lov msl on tdh.lov_doc_type = msl.id_lov where tdd.document_id = '" + documentid + "'")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getEmailLogin(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("SELECT STRING_AGG(distinct au.login_id, ';') AS login FROM tr_document_h AS tdh JOIN tr_document_d AS tdd ON tdh.id_document_h = tdd.id_document_h JOIN tr_document_d_sign AS tdds ON tdd.id_document_d = tdds.id_document_d JOIN am_msuser AS au ON au.id_ms_user = tdds.id_ms_user WHERE tdd.document_id = '"+documentid+"'")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data
	}

	@Keyword
	getProsesTtdProgress(Connection conn, String refNumber) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("SELECT COUNT(tdsr.id_ms_user) FROM tr_document_h tdh JOIN tr_document_signing_request tdsr ON tdh.id_document_h = tdsr.id_document_h WHERE tdh.ref_number = '"+refNumber+"'")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		Integer.parseInt(data)
	}

	@Keyword
	getTotalSigned(Connection conn, String documentId) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select total_signed from tr_document_d where document_id = '" + documentId + "'")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		Integer.parseInt(data)
	}

	@Keyword
	getPaymentType(Connection conn, String refnumber){
		String data

		stm = conn.createStatement()

		resultSet = stm.executeQuery("select msl.description from tr_document_d tdd join ms_lov msl on tdd.lov_payment_sign_type = msl.id_lov join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h where tdh.ref_number = '"+refnumber+"'")
		metadata = resultSet.getMetaData()

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data
	}

	@Keyword
	getSaldoUsedBasedonPaymentType(Connection conn, String refnumber, String emailSigner){
		String data

		stm = conn.createStatement()

		resultSet = stm.executeQuery("select CASE WHEN msl_tdd.description = 'Per Document' then '1' WHEN msl_tdd.description = 'Per Sign' then count(tdds.sign_location) end from tr_document_d_sign tdds join tr_document_d tdd on tdds.id_document_d = tdd.id_document_d join ms_lov msl on tdds.lov_signer_type = msl.id_lov join ms_lov msl_tdd on tdd.lov_payment_sign_type = msl_tdd.id_lov join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h join am_msuser amm on tdds.id_ms_user = amm.id_ms_user where tdh.ref_number = '"+refnumber+"' and amm.login_id = '"+emailSigner+"' GROUP BY msl.description, msl_tdd.description")
		metadata = resultSet.getMetaData()

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		Integer.parseInt(data)
	}

	@Keyword
	getTotalSignedUsingRefNumber(Connection conn, String refnumber) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select tdd.total_signed from tr_document_d tdd join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h where tdh.ref_number=  '"+refnumber+"'")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		Integer.parseInt(data)
	}

	@Keyword
	getDocumentType(Connection conn, String refnumber){
		String data

		stm = conn.createStatement()

		resultSet = stm.executeQuery("select msl.description from tr_document_h tdh join ms_lov msl on tdh.lov_doc_type = msl.id_lov where tdh.ref_number = '"+refnumber+"'")
		metadata = resultSet.getMetaData()

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data
	}

	@Keyword
	getTenant(Connection conn, String user) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("Select mt.tenant_code from am_msuser amu JOIN ms_office mo ON mo.id_ms_office = amu.id_ms_office JOIN ms_tenant mt ON mt.id_ms_tenant = mo.id_ms_tenant where login_id = '" +  user  + "' AND mt.is_active = '1'")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data
	}

	@Keyword
	getSendDoc(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select mst.tenant_code, tdh.ref_number, tdd.document_id,mdt.doc_template_code, mso.office_code,mso.office_name, msr.region_code, msr.region_name,mbl.business_line_code, mbl.business_line_name, tdd.is_sequence,msv.vendor_code, tdh.result_url,tdh.url_upload, tdh.total_document from tr_document_d as tdd join tr_document_h as tdh on tdd.id_document_h = tdh.id_document_h join tr_document_d_sign as tdds on tdd.id_document_d = tdds.id_document_d join am_msuser as amm on tdds.id_ms_user = amm.id_ms_user join ms_tenant as mst on tdd.id_ms_tenant = mst.id_ms_tenant join ms_lov as lov on tdds.lov_signer_type = lov.id_lov join ms_doc_template as mdt on tdd.id_ms_doc_template = mdt.id_doc_template join ms_office as mso on tdh.id_ms_office = mso.id_ms_office join ms_region as msr on mso.id_ms_region = msr.id_ms_region join ms_business_line as mbl on tdh.id_ms_business_line = mbl.id_ms_business_line join ms_vendor as msv on tdd.id_ms_vendor = msv.id_ms_vendor where tdd.document_id = '" + documentid + "' GROUP BY mst.tenant_code, tdh.ref_number, tdd.document_id,mdt.doc_template_code, mso.office_code,mso.office_name, msr.region_code, msr.region_name,mbl.business_line_code, mbl.business_line_name, mso.office_code, tdd.is_sequence,msv.vendor_code,tdh.result_url,tdh.url_upload,tdh.total_document")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getSendDocForEmailAndSignerType(Connection conn, String documentid,String emailSigner) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select DISTINCT amm.login_id as email, lov.code as code from tr_document_d as tdd join tr_document_h as tdh on tdd.id_document_h = tdh.id_document_h join tr_document_d_sign as tdds on tdd.id_document_d = tdds.id_document_d join am_msuser as amm on tdds.id_ms_user = amm.id_ms_user join ms_tenant as mst on tdd.id_ms_tenant = mst.id_ms_tenant join ms_lov as lov on tdds.lov_signer_type = lov.id_lov where tdd.document_id = '"+documentid+"' and amm.login_id = '"+emailSigner+"'")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getKotakMasukSendDoc(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select tdh.ref_number, msl.description as doctype,case when mdt.doc_template_name is null then tdd.document_name else mdt.doc_template_name end, TO_CHAR(tdd.dtm_crt, 'DD-Mon-YYYY HH24:MI') as timee, CASE WHEN msl_sign.description = 'Need Sign' THEN 'Belum TTD' END as description , case when tdd.completed_date is null then '-' else TO_CHAR(tdd.completed_date, 'DD-Mon-YYYY HH24:MI') end from tr_document_d tdd join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h join ms_lov msl on tdh.lov_doc_type = msl.id_lov left join ms_doc_template as mdt on tdd.id_ms_doc_template = mdt.id_doc_template join tr_document_d_sign as tdds on tdd.id_document_d = tdds.id_document_d join ms_lov ms on tdds.lov_signer_type = ms.id_lov join am_msuser amm on tdds.id_ms_user = amm.id_ms_user join ms_lov msl_sign on tdd.lov_sign_status = msl_sign.id_lov where document_id = '" + documentid + "' ORDER BY tdds.id_document_d_sign asc limit 1")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getSignStatus(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select msl.description from tr_document_d tdd join ms_lov msl on tdd.lov_sign_status = msl.id_lov where document_id = '" + documentid + "'")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data
	}

	@Keyword
	getTotalStampingandTotalMaterai(Connection conn, String refNumber) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select tdd.total_stamping, tdd.total_materai from tr_document_d tdd join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h where tdh.ref_number = '"+refNumber+"'")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getSignerKotakMasukSendDoc(Connection conn, String documentid, String emailSigner) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select ms.description as signertype,amm.full_name as name, amm.login_id as email,CASE WHEN amm.is_active = '1' THEN 'Sudah Aktivasi' END as aktivasi, CASE WHEN tdds.sign_date is not null THEN 'Signed' ELSE msl.description END as status, CASE WHEN tdds.sign_date IS null THEN '-' else to_char(tdds.sign_date, 'DD-Mon-YYYY HH24:MI') END sign_date from tr_document_d tdd join tr_document_h tdh on tdd.id_document_h = tdh.id_document_h join ms_lov msl on tdd.lov_sign_status = msl.id_lov left join ms_doc_template as mdt on tdd.id_ms_doc_template = mdt.id_doc_template join tr_document_d_sign as tdds on tdd.id_document_d = tdds.id_document_d join ms_lov ms on tdds.lov_signer_type = ms.id_lov join am_msuser amm on tdds.id_ms_user = amm.id_ms_user where document_id = '" + documentid + "' and amm.login_id = '"+emailSigner+"' ORDER BY tdds.id_document_d_sign asc limit 1")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getEmailsSign(Connection conn, String documentid) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("SELECT au.login_id FROM tr_document_h AS tdh JOIN tr_document_d AS tdd ON tdh.id_document_h = tdd.id_document_h JOIN tr_document_d_sign AS tdds ON tdd.id_document_d = tdds.id_document_d JOIN am_msuser AS au ON au.id_ms_user = tdds.id_ms_user WHERE tdd.document_id = '" + documentid + "' ")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}

	@Keyword
	getbulkSign(Connection conn, String documentids) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select STRING_AGG(amm.login_id,';'),msv.vendor_code, tdd.total_signed from tr_document_d_sign as tdds join tr_document_d as tdd on tdds.id_document_d = tdd.id_document_d join am_msuser as amm on tdds.id_ms_user = amm.id_ms_user join ms_vendor as msv on tdd.id_ms_vendor = msv.id_ms_vendor join tr_document_h as tdh on tdh.id_document_h = tdd.id_document_h where tdd.document_id = '" + documentids + "' GROUP BY msv.vendor_code, tdd.total_signed")
		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			for (i = 1 ; i <= columnCount ; i++) {
				data = resultSet.getObject(i)
				listdata.add(data)
			}
		}
		listdata
	}
}