package connection

import java.sql.Connection
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement
import com.kms.katalon.core.annotation.Keyword
import internal.GlobalVariable

public class PengaturanDokumen {

	String data
	int columnCount, i
	Statement stm
	ResultSetMetaData metadata
	ResultSet resultSet
	ArrayList<String> listdata = []

	@Keyword
	getDataDocTemplate(Connection conn, String docTempCode) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("SELECT doc_template_code, doc_template_name, doc_template_description, CASE WHEN is_active = '1' THEN 'Active' ELSE 'Inactive' END FROM esign.ms_doc_template where doc_template_code = '" +  docTempCode  + "'")

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
	dataDocTemplateStoreDB(Connection conn, String docTempCode) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("SELECT doc_template_code, doc_template_name, doc_template_description, ml.description, CASE WHEN mdt.is_active = '1' THEN 'Active' ELSE 'Inactive' END FROM esign.ms_doc_template mdt JOIN ms_lov ml ON ml.id_lov = mdt.lov_payment_sign_type where doc_template_code = '" +  docTempCode  + "'")

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
	getLovTipePembayaran(Connection conn) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select description from ms_lov where lov_group = 'PAYMENT_SIGN_TYPE' order by description asc")

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
	getTotalDokumenTemplatKode(Connection conn) {
		stm = conn.createStatement()

		resultSet = stm.executeQuery("select count(*) from ms_doc_template where is_active = '1' and id_ms_tenant = 1")

		metadata = resultSet.metaData

		columnCount = metadata.getColumnCount()

		while (resultSet.next()) {
			data = resultSet.getObject(1)
		}
		data}}
