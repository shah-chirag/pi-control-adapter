
package in.fortytwo42.adapter.service;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.exception.MapperNotFoundException;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.IdentityProviderDaoIntf;
import in.fortytwo42.entities.bean.IdentityProvider;
import in.fortytwo42.entities.bean.Mapper;
import in.fortytwo42.entities.enums.IdentityProviderType;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.IdentityProviderTO;

public class IdentityProviderServiceImpl implements IdentityProviderServiceIntf {

    private static final String IDETITY_PROVIDER_SERVICE = "<<<<< IdentityProviderServiceImpl";
    private static Logger logger= LogManager.getLogger(IdentityProviderServiceImpl.class);
    private static ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();
    private static final String ACTIVE_DIRECTORY = "Active Directory";
    private IdentityProviderDaoIntf identityProviderDaoIntf = DaoFactory.getIdentityProvideDao();
    private MapperServiceIntf mapperService = ServiceFactory.getMapperService();
    private ADSyncServiceIntf adSyncService = ServiceFactory.getADSyncService();

    private IdentityProviderServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final IdentityProviderServiceImpl INSTANCE = new IdentityProviderServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static IdentityProviderServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public IdentityProviderTO getIdentityProviderByType(IdentityProviderType identityProviderType) {
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " getIdentityProviderByType : start");
        IdentityProviderTO identityProiderTO = identityProviderDaoIntf.getByType(identityProviderType).convertToTO();
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " getIdentityProviderByType : end");
        return identityProiderTO;
    }

    @Override
    public void syncUsers(Session session, IdentityProviderType identityProviderType) throws AuthException {
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " sync : start");
        IdentityProviderTO identityProiderTO = identityProviderDaoIntf.getByType(identityProviderType).convertToTO();
        if (IdentityProviderType.LDAP.equals(identityProviderType)) {
            switch (identityProiderTO.getVendor()) {
                case ACTIVE_DIRECTORY:
                    try {
                        adSyncService.syncADUsers(session, identityProiderTO);
                    }
                    catch (IOException e) {
                        logger.log(Level.FATAL, e.getMessage(), e);
                        throw new AuthException(e, errorConstant.getERROR_CODE_AD_SYNC_FAILED(), e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
        //identityProviderDaoIntf.update(session, identityProider);
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " sync : end");
    }

    @Override
    public IdentityProviderTO createIdentityProvider(Session session, IdentityProviderTO identityProviderTO) throws AuthException {
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " createIdentityProvider : start");
        IdentityProvider identityProvider = convertTOToEntity(session, identityProviderTO);
        identityProviderTO = identityProviderDaoIntf.create(session, identityProvider).convertToTO();
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " createIdentityProvider : end");
        return identityProviderTO;
    }

    private IdentityProvider convertTOToEntity(Session session, IdentityProviderTO identityProviderTO) throws AuthException {
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " convertTOToEntity : start");
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setName(identityProviderTO.getName());
        identityProvider.setAdminDomain(identityProviderTO.getAdminDomain());
        identityProvider.setAdminCredential(identityProviderTO.getAdminCredential());
        identityProvider.setConnectionUrl(identityProviderTO.getConnectionUrl());
        identityProvider.setVendor(identityProviderTO.getVendor());
        if (identityProviderTO.getFilters() != null) {
            identityProvider.setFilters(identityProviderTO.getFilters());
        }
        identityProvider.setType(IdentityProviderType.valueOf(identityProviderTO.getType()));
        identityProvider.setUserObjectClasses(identityProviderTO.getUserObjectClasses());
        identityProvider.setUserDomain(identityProviderTO.getUserDomain());
        identityProvider.setUserAttributes(identityProviderTO.getUserAttributes());
        identityProvider.setAuthenticationType(identityProviderTO.getAuthenticationType());
        try {
            if (identityProviderTO.getUserName() != null) {
                Mapper userName = mapperService.getById(identityProviderTO.getUserName().getId());
                identityProvider.setUserName(userName);
            }
            if (identityProviderTO.getFirstName() != null) {
                Mapper firstName = mapperService.getById(identityProviderTO.getFirstName().getId());
                identityProvider.setFirstName(firstName);
            }
            if (identityProviderTO.getLastName() != null) {
                Mapper lastName = mapperService.getById(identityProviderTO.getLastName().getId());
                identityProvider.setLastName(lastName);
            }
            if (identityProviderTO.getFullName() != null) {
                Mapper fullName = mapperService.getById(identityProviderTO.getFullName().getId());
                identityProvider.setFullName(fullName);
            }
            if (identityProviderTO.getEmail() != null) {
                Mapper email = mapperService.getById(identityProviderTO.getEmail().getId());
                identityProvider.setEmail(email);
            }
            if (identityProviderTO.getLastName() != null) {
                Mapper location = mapperService.getById(identityProviderTO.getLastName().getId());
                identityProvider.setLocation(location);
            }
        }
        catch (MapperNotFoundException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_MAPPER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_MAPPER_NOT_FOUND());
        }
        if (identityProviderTO.getSyncSettings() != null) {

        }
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " convertTOToEntity : end");
        return identityProvider;
    }

    @Override
    public PaginatedTO<IdentityProviderTO> getProviders(int page, int limit, String searchText) {
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " getProviders : start");
        List<IdentityProvider> identityProviders = identityProviderDaoIntf.getPaginatedList(page, limit, searchText);
        Long count = identityProviderDaoIntf.getTotalCount(searchText);
        PaginatedTO<IdentityProviderTO> providers = new PaginatedTO<>();
        providers.setList(new EntityToTOConverter<IdentityProvider, IdentityProviderTO>().convertEntityListToTOList(identityProviders));
        providers.setTotalCount(count);
        logger.log(Level.DEBUG, IDETITY_PROVIDER_SERVICE + " getProviders : end");
        return providers;
    }
}
