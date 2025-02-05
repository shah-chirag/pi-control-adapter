package in.fortytwo42.adapter.service;


import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.tos.transferobj.SRAGatewaySettingTO;

public interface SRAGatewaySettingServiceIntf {

	 SRAGatewaySettingTO createSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException;
	
	 SRAGatewaySettingTO editSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException;
	 
	 SRAGatewaySettingTO deleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO, String actor) throws AuthException;
	 
	 SRAGatewaySettingTO getSRAGatewaySetting(SRAGatewaySettingTO sraGatewaySettingTO) throws NotFoundException;
	 
	 SRAGatewaySettingTO getSRAGatewaySettingById(Long sraGatewaySettingId) throws NotFoundException;
	 
	 PaginatedTO<SRAGatewaySettingTO> getAllSRAGatewaySetting(Integer page, Integer limit, String searchText) throws NotFoundException;
	 
	 SRAGatewaySettingTO approveAddSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO);
	 
	 SRAGatewaySettingTO approveUpdateSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO)throws AuthException;
	 
	 void approveDeleteSRAGatewaySetting(Session session, SRAGatewaySettingTO sraGatewaySettingTO)throws AuthException;
	 
	 SRAGatewaySettingTO getSRAGatewaySettingByName(String gatewaySettingsName) throws NotFoundException;
}

