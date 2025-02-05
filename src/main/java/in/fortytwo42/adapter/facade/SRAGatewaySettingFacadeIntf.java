package in.fortytwo42.adapter.facade;


import java.io.InputStream;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CSVUploadTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.enterprise.extension.core.DecryptionDataV2;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;

public interface SRAGatewaySettingFacadeIntf {

	 SRAGatewaySettingTO addSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,
	                                          String role, boolean saveRequest)throws AuthException;

	public CSVUploadTO uploadSRAGatewaySettings(InputStream inputStream, String role, String username,Long id, String fileName) throws AuthException;
	
	 SRAGatewaySettingTO updateSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,String role, boolean saveRequest) throws AuthException;
	 
	 SRAGatewaySettingTO deleteSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO, String actor,Long id,String role, boolean saveRequest) throws AuthException;
	 
	 PaginatedTO<SRAGatewaySettingTO> getAllSRAGatewaySetting(Integer page, String searchText) throws AuthException;

    DecryptionDataV2 getDecryptionKey(String signTransactionId, String clientIp,String applicationId, String type, String host, Integer port) throws AuthException;

	public String readSampleCsvFile(String fileName);

	public String downloadUpdateSRAGatewaySettingStatus(String fileName, String role) throws AuthException;
}
