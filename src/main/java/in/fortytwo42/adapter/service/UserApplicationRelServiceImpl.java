package  in.fortytwo42.adapter.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.SRAApplicationGatewayRelDaoIntf;
import in.fortytwo42.daos.dao.SRAApplicationSettingDaoIntf;
import in.fortytwo42.daos.dao.UserApplicationRelDaoIntf;
import in.fortytwo42.daos.exception.UserApplicationRelNotFoundException;
import in.fortytwo42.entities.bean.Application;
import in.fortytwo42.entities.bean.SRAApplicationGatewayRel;
import in.fortytwo42.entities.bean.SRAApplicationSetting;
import in.fortytwo42.entities.bean.Service;
import in.fortytwo42.entities.bean.User;
import in.fortytwo42.entities.bean.UserApplicationServiceCompositeKey;
import in.fortytwo42.entities.bean.UserApplicationServiceRel;
import in.fortytwo42.tos.enums.BindingStatus;
import in.fortytwo42.tos.enums.TwoFactorStatus;
import in.fortytwo42.tos.transferobj.ApplicationTO;
import in.fortytwo42.tos.transferobj.ServiceTO;

public class UserApplicationRelServiceImpl implements UserApplicationRelServiceIntf {

	private static Logger logger= LogManager.getLogger(UserApplicationRelServiceImpl.class);

	private UserApplicationRelDaoIntf userApplicationRelDao = DaoFactory.getUserApplicationRel();
	private  ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private SRAApplicationSettingDaoIntf sraApplicationSettingDao = DaoFactory.getSRAApplicationSetting();
    private SRAApplicationGatewayRelDaoIntf sraApplicationGatewayRelDao = DaoFactory.getSRAApplicationGatewayRelDoa();


	private UserApplicationRelServiceImpl() {
		super();
	}

	private static final class InstanceHolder {
		private static final UserApplicationRelServiceImpl INSTANCE = new UserApplicationRelServiceImpl();

		private InstanceHolder() {
			super();
		}
	}

	public static UserApplicationRelServiceImpl getInstance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public UserApplicationServiceRel getUserApplicationRel(User user, Application application, Service service) {
		UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
		userApplicationCompositeKey.setApplication(application);
		userApplicationCompositeKey.setService(service);
		userApplicationCompositeKey.setUser(user);
		try {
			return userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey);
		} catch (UserApplicationRelNotFoundException e) {}
		return null;
	}



	@Override
	public UserApplicationServiceRel getUserApplicationRel(User user, Application application, Service service, Session session) {
		UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
		userApplicationCompositeKey.setApplication(application);
		userApplicationCompositeKey.setService(service);
		userApplicationCompositeKey.setUser(user);
		try {
			return userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey, session);
		} catch (UserApplicationRelNotFoundException e) {}
		return null;
	}

	@Override
	public UserApplicationServiceRel getUserApplicationRelForAccountId(User user, Application application, Service service) {
		UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
		userApplicationCompositeKey.setApplication(application);
		userApplicationCompositeKey.setService(service);
		userApplicationCompositeKey.setUser(user);
		try {
			return userApplicationRelDao.getUserApplicationForAccountId(userApplicationCompositeKey);
		} catch (UserApplicationRelNotFoundException e) {}
		return null;
	}

	@Override
	public void validateUserApplicationRel(UserApplicationServiceRel userApplicationRel) throws AuthException {
		if (userApplicationRel == null) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
		} else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_BLOCKED(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_BLOCKED());
		} else if (userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN) {
			throw new AuthException(null, errorConstant.getERROR_CODE_BLOCKED_FOR_RESET_PIN(), errorConstant.getERROR_MESSAGE_BLOCKED_FOR_RESET_PIN());
		} else if (userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED) {
			throw new AuthException(null, errorConstant.getERROR_CODE_RESET_PIN_COMPLETED(), errorConstant.getERROR_MESSAGE_RESET_PIN_COMPLETED());
		} else if (userApplicationRel.getBindingStatus() != BindingStatus.ACTIVE) {
			throw new AuthException(null, errorConstant.getERROR_CODE_USER_SERVICE_BINDING_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_SERVICE_BINDING_NOT_FOUND());
		}
	}
	
	@Override
	public List<UserApplicationServiceRel> getUserApplicationRel(Long applicationId, Long userId){
		return userApplicationRelDao.getUserApplicationRels(applicationId, userId);
	}

	@Override
	public void bulkUpdateUserApplicationRel(Session session, List<UserApplicationServiceRel> userApplicationRels){
		 userApplicationRelDao.bulkUpdate(session, userApplicationRels);
	}
	
	@Override
	public UserApplicationServiceRel updateUserApplicationRel(Session session, UserApplicationServiceRel userApplicationRel, BindingStatus bindingStatus) {
		userApplicationRel.setBindingStatus(bindingStatus);
		return userApplicationRelDao.update(session, userApplicationRel);
	}
	
	@Override
	public List<UserApplicationServiceRel> getUserApplicationRels(Long applicationId , Long userId){
        return userApplicationRelDao.getUserApplicationRels(applicationId, userId);
	}
	
	@Override
    public UserApplicationServiceRel getUserApplicationRel(UserApplicationServiceCompositeKey userApplicationCompositeKey) {
    	try {
        	return userApplicationRelDao.getUserApplicationForId(userApplicationCompositeKey);
		} catch (UserApplicationRelNotFoundException e) {
			logger.log(Level.INFO, e);
			return null;
		}
    }
	
	@Override
	public List<UserApplicationServiceRel> getUserApplicationRel(Long userId){
		return getUserApplicationServiceRels(userId);
	}
	
	public List<UserApplicationServiceRel> getUserApplicationServiceRels(Long userId){
	    List<Object[]> objects = userApplicationRelDao.getUserApplicationRels(userId);
	    List<UserApplicationServiceRel> userApplicationRels = new ArrayList<>();
	    for (Object[] object : objects) {
	    	long applicationId = -1;
			long serviceId = -1;
	    	try {	    		
	    		applicationId = ((BigDecimal) (object[0])).longValue();
	    	}
	    	catch(ClassCastException e) {
	    		applicationId = ((BigInteger) (object[0])).longValue();
	    	}
			if (applicationId >= 0) {
				try {
					serviceId = ((BigDecimal) (object[1])).longValue();
				}
				catch(ClassCastException e) {
					serviceId = ((BigInteger) (object[1])).longValue();
				}
				Application application = null;
				Service service = null;
				User user = null;
				try {
					application = DaoFactory.getApplicationDao().getApplicationById(applicationId);
					service = DaoFactory.getServiceDao().getById(serviceId);
					user = DaoFactory.getUserDao().getActiveById(userId);
					UserApplicationServiceCompositeKey id = new UserApplicationServiceCompositeKey();
					id.setApplication(application);
					id.setService(service);
					id.setUser(user);
					UserApplicationServiceRel rel = new UserApplicationServiceRel();
					rel.setId(id);
					rel.setTwoFactorStatus(TwoFactorStatus.valueOf((String) object[3]));
					rel.setBindingStatus(BindingStatus.valueOf((String) object[4]));
					userApplicationRels.add(rel);
				} catch (Exception e) {
					logger.log(Level.DEBUG, e.getMessage(), e);
				}
			}
		}
	    return userApplicationRels;
    }



	@Override
	public List<UserApplicationServiceRel> getUserApplicationRel(Long userId, Session session){
		return getUserApplicationServiceRels(userId, session);
	}

	public List<UserApplicationServiceRel> getUserApplicationServiceRels(Long userId, Session session){
		List<Object[]> objects = userApplicationRelDao.getUserApplicationRels(userId, session);
		List<UserApplicationServiceRel> userApplicationRels = new ArrayList<>();
		for (Object[] object : objects) {
			long applicationId = -1;
			long serviceId = -1;
			try {
				applicationId = ((BigDecimal) (object[0])).longValue();
			}
			catch(ClassCastException e) {
				applicationId = ((BigInteger) (object[0])).longValue();
			}
			if (applicationId >= 0) {
				try {
					serviceId = ((BigDecimal) (object[1])).longValue();
				}
				catch(ClassCastException e) {
					serviceId = ((BigInteger) (object[1])).longValue();
				}
				Application application = null;
				Service service = null;
				User user = null;
				try {
					application = DaoFactory.getApplicationDao().getApplicationById(applicationId, session);
					service = DaoFactory.getServiceDao().getById(serviceId, session);
					user = DaoFactory.getUserDao().getActiveById(userId, session);
					UserApplicationServiceCompositeKey id = new UserApplicationServiceCompositeKey();
					id.setApplication(application);
					id.setService(service);
					id.setUser(user);
					UserApplicationServiceRel rel = new UserApplicationServiceRel();
					rel.setId(id);
					rel.setTwoFactorStatus(TwoFactorStatus.valueOf((String) object[3]));
					rel.setBindingStatus(BindingStatus.valueOf((String) object[4]));
					userApplicationRels.add(rel);
				} catch (Exception e) {
					logger.log(Level.DEBUG, e.getMessage(), e);
				}
			}
		}
		return userApplicationRels;
	}
	
	@Override
	public List<UserApplicationServiceRel> getBlockedApplicationRelsForUser(Long userId,Long applicationId) {
		return userApplicationRelDao.getBlockedApplicationRelsForUser(userId, applicationId);
	}

	@Override
	public List<ApplicationTO> getApplicationRelsForUser(Long userId) {
		List<UserApplicationServiceRel> adUserApplicationRels = userApplicationRelDao.getApplicationRelsForUser(userId);
		List<ApplicationTO> applicationTOs = new ArrayList<>();
		if (adUserApplicationRels != null && !adUserApplicationRels.isEmpty()) {
			for (UserApplicationServiceRel adUserApplicationRel : adUserApplicationRels) {
				Application application = adUserApplicationRel.getId().getApplication();
				Service service = adUserApplicationRel.getId().getService();
				ApplicationTO applicationTO = new ApplicationTO();
				applicationTO.setApplicationId(application.getApplicationId());
				int index = applicationTOs.indexOf(applicationTO);
				if (index >= 0) {
					applicationTO = applicationTOs.get(index);
					ServiceTO serviceTO = service.convertToTO();
					applicationTO.getServices().add(serviceTO);
				} else {
					applicationTO.setId(application.getId());
					applicationTO.setEnterpriseId(application.getEnterprise().getEnterpriseId());
					applicationTO.setApplicationName(application.getApplicationName());
					ServiceTO serviceTO = service.convertToTO();
					List<ServiceTO> services = new ArrayList<>();
					services.add(serviceTO);
					applicationTO.setServices(services);
					applicationTOs.add(applicationTO);
				}
			}
		}
		return applicationTOs;
	}
	
    @Override
    public List<ApplicationTO> getTunnelingApplicationRelsForUser(Long userId) {
        List<UserApplicationServiceRel> adUserApplicationRels = userApplicationRelDao.getTunnelingApplicationRelsForUser(userId);
        List<ApplicationTO> applicationTOs = new ArrayList<>();
        if (adUserApplicationRels != null && !adUserApplicationRels.isEmpty()) {
            for (UserApplicationServiceRel adUserApplicationRel : adUserApplicationRels) {
                Application application = adUserApplicationRel.getId().getApplication();
                Service service = adUserApplicationRel.getId().getService();
                ApplicationTO applicationTO = new ApplicationTO();
                applicationTO.setApplicationId(application.getApplicationId());
                int index = applicationTOs.indexOf(applicationTO);
                if (index >= 0) {
                    applicationTO = applicationTOs.get(index);
                    ServiceTO serviceTO = service.convertToTO();
                    applicationTO.getServices().add(serviceTO);
                } else {
                    applicationTO.setId(application.getId());
                    applicationTO.setEnterpriseId(application.getEnterprise().getEnterpriseId());
                    applicationTO.setApplicationName(application.getApplicationName());
                    ServiceTO serviceTO = service.convertToTO();
                    List<ServiceTO> services = new ArrayList<>();
                    services.add(serviceTO);
                    applicationTO.setServices(services);
                    try {
                        System.out.println("CONSUMER App id " + application.getApplicationId());
                        SRAApplicationSetting sraApplicationSetting = sraApplicationSettingDao.getSettingsByApplicationId(application);
                        applicationTO.setUrl(sraApplicationSetting.getUrl());
                        applicationTO.setExternalAddress(sraApplicationSetting.getExternalAddress());
                        applicationTO.setExternalPort(sraApplicationSetting.getExternalPort());
                        applicationTO.setInternalAddress(sraApplicationSetting.getInternalAddress());
                        applicationTO.setInternalPort(sraApplicationSetting.getInternalPort());
                        applicationTO.setProtocol(sraApplicationSetting.getProtocol());
                        SRAApplicationGatewayRel sraApplicationGatewayRel = sraApplicationGatewayRelDao.getSRAApplicationGatewayRel(application);
                        applicationTO.setGatewayName(sraApplicationGatewayRel.getSraGatewaySetting().getName());
                        applicationTO.setPortForwardingFacade(sraApplicationSetting.getPortForwardingFacade());
                        if (sraApplicationSetting.getPortForwardingFacadeLocalPort() != null) {
                            applicationTO.setDefaultLocalPort(sraApplicationSetting.getPortForwardingFacadeLocalPort());
                        }
                    }
                    catch (Exception e) {
                        logger.log(Level.ERROR, e.getMessage(), e);   
                    }
                    applicationTOs.add(applicationTO);
                }
            }
        }
        return applicationTOs;
    }

//	public void creatApplicationRelsForUser(List<UserApplicationStagingRel> userApplicationStagingRels) {
//		for (UserApplicationStagingRel userApplicationStagingRel : userApplicationStagingRels) {
//			UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
//			userApplicationCompositeKey.setAdUser(userApplicationStagingRel.getUser());
//			userApplicationCompositeKey.setApplication(userApplicationStagingRel.getApplication());
//			userApplicationCompositeKey.setService(userApplicationStagingRel.getService());
//			UserApplicationServiceRel userApplicationRel = null;
//			try {
//				userApplicationRel = userApplicationRelDaoIntf.getUserApplicationForId(userApplicationCompositeKey);
//				userApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
//				userApplicationRel.setActive(true);
//				userApplicationRelDaoIntf.update(userApplicationRel);
//			} catch (UserApplicationRelNotFoundException e) {
//			}
//			if (userApplicationRel == null) {
//				userApplicationRel = new UserApplicationServiceRel();
//				userApplicationRel.setId(userApplicationCompositeKey);
//				userApplicationRel.setActive(true);
//				userApplicationRel.setBindingStatus(BindingStatus.ACTIVE);
//				userApplicationRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
//				userApplicationRelDaoIntf.create(userApplicationRel);
//			}
//		}
//	}

@Override
	public UserApplicationServiceRel getUserApplicationForId(UserApplicationServiceCompositeKey adUserApplicationCompositeKey) throws UserApplicationRelNotFoundException{
		return userApplicationRelDao.getUserApplicationForId(adUserApplicationCompositeKey);
	}
	
	@Override
	public List<UserApplicationServiceRel> getApplicationRelsForUserId(Long userId){
		return userApplicationRelDao.getApplicationRelsForUser(userId);
	}
	
	@Override
	public boolean isApplicationUserBindingPresent(Long applicationId, Long userId) {
		return userApplicationRelDao.isApplicationUserBindingPresent(applicationId, userId);
	}

	@Override
	public UserApplicationServiceRel createUserApplicationRel(Session session, User user, Application application, Service service, BindingStatus bindingStatus) {
		UserApplicationServiceCompositeKey userApplicationCompositeKey = new UserApplicationServiceCompositeKey();
		userApplicationCompositeKey.setApplication(application);
		userApplicationCompositeKey.setUser(user);
		userApplicationCompositeKey.setService(service);
		UserApplicationServiceRel userApplicationRel = new UserApplicationServiceRel();
		userApplicationRel.setId(userApplicationCompositeKey);
		userApplicationRel.setBindingStatus(bindingStatus);
		userApplicationRel.setTwoFactorStatus(TwoFactorStatus.ENABLED);
		userApplicationRelDao.create(session, userApplicationRel);
		return userApplicationRel;
	}
	
	@Override
	public List<UserApplicationServiceRel> getApplicationRels(Long userId){
	    return userApplicationRelDao.getApplicationRelsForUser(userId);
	}
	
	@Override
	public UserApplicationServiceRel createUserApplicationRel(Session session, UserApplicationServiceRel userApplicationRel) {
	    return userApplicationRelDao.create(session, userApplicationRel);
	}
	
	@Override
	public Long getUserAndApplicationRelCount(Long applicationId, Long userId) {
	    return userApplicationRelDao.getUserAndApplicationRelCount(applicationId, userId);
	}
	@Override
	public Long getUserAndApplicationRelCount(Long applicationId, Long userId, Session session) {
		return userApplicationRelDao.getUserAndApplicationRelCount(applicationId, userId, session);
	}

	@Override
	public boolean isServiceBindingPresent(UserApplicationServiceRel userApplicationRel) {
		return userApplicationRel != null && (userApplicationRel.getBindingStatus() == BindingStatus.ACTIVE
				|| userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED
				|| userApplicationRel.getBindingStatus() == BindingStatus.RESET_PIN_COMPLETED
				|| userApplicationRel.getBindingStatus() == BindingStatus.BLOCKED_FOR_RESET_PIN);
	}
	
	@Override
	public Long getUserServiceRelCount(Long serviceId, Long userId, Session session) {
        return userApplicationRelDao.getUserServiceRelCount(serviceId, userId, session);
    }
	
}
