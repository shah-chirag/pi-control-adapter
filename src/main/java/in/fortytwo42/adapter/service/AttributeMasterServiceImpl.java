
package in.fortytwo42.adapter.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import in.fortytwo42.adapter.transferobj.AttributeMetadataWTO;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.StringUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeMetadataTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.RequestDaoIntf;
import in.fortytwo42.daos.exception.AttributeNotFoundException;
import in.fortytwo42.entities.bean.Request;
import in.fortytwo42.entities.enums.ApprovalStatus;
import in.fortytwo42.entities.enums.RequestSubType;
import in.fortytwo42.entities.enums.RequestType;

public class AttributeMasterServiceImpl implements AttributeMasterServiceIntf {

    //TODO: Service to Service
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private RequestDaoIntf requestDao = DaoFactory.getRequestDao();
    private AttributeStoreDaoIntf attributeStoreDaoIntf = DaoFactory.getAttributeStoreDao();

    //private List<AttributeMetadataTO> attributeMetadataTos = new ArrayList<>();

    private static Logger logger= LogManager.getLogger(AttributeMasterServiceImpl.class);
    private static String ATTRIBUTE_METADATA_CACHE = "attributeMetadataCache";
    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    private final CacheConfiguration<String, String> attributeMetadataCacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100000))
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.ATTRIBUTE_METADATA_CACHE_TIMEOUT_IN_SECONDS)!=null?Config.getInstance().getProperty(Constant.ATTRIBUTE_METADATA_CACHE_TIMEOUT_IN_SECONDS):"1800"))))
            .build();
    private final Cache<String, String> attributeMetadataStore = cacheManager.createCache("attributeMetadataCache", attributeMetadataCacheConfiguration);

    private AttributeMasterServiceImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final AttributeMasterServiceImpl INSTANCE = new AttributeMasterServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static AttributeMasterServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public AttributeMetadataTO createAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO, String actor) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(attributeMetadataTO));
        request.setRequestorComments(attributeMetadataTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_MASTER_ADDITION);
        request.setRequestSubType(RequestSubType.ATTRIBUTE_MASTER_ADDITION);
        try {
            request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        attributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        attributeMetadataTO.setId(createdRequest.getId());
        return attributeMetadataTO;
    }

    @Override
    public AttributeMetadataTO editAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO, String actor) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(attributeMetadataTO));
        request.setRequestorComments(attributeMetadataTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_MASTER_UPDATION);
//        request.setRequestSubType(RequestType.ATTRIBUTE_MASTER_UPDATION);
        try {
            request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        attributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        attributeMetadataTO.setId(createdRequest.getId());
        return attributeMetadataTO;
    }

    @Override
    public AttributeMetadataTO deleteAttributeMetaDataRequest(Session session, AttributeMetadataTO attributeMetadataTO, String actor) throws AuthException {
        Request request = new Request();
        request.setRequestJSON(new Gson().toJson(attributeMetadataTO));
        request.setRequestorComments(attributeMetadataTO.getComments());
        request.setRequestType(RequestType.ATTRIBUTE_MASTER_DELETION);
//        request.setRequestSubType(RequestType.ATTRIBUTE_MASTER_DELETION);
        try {
            request.setRequestor(attributeStoreDaoIntf.getUserByAttributeValue(actor));
        }
        catch (AttributeNotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_USER_NOT_FOUND(), errorConstant.getERROR_MESSAGE_USER_NOT_FOUND());
        }
        request.setApprovalStatus(ApprovalStatus.CHECKER_APPROVAL_PENDING);
        Request createdRequest = requestDao.create(session, request);
        attributeMetadataTO.setStatus(Constant.SUCCESS_STATUS);
        attributeMetadataTO.setId(createdRequest.getId());
        return attributeMetadataTO;
    }

    @Override
    public AttributeMetadataTO approveCreateAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO) throws AuthException {
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 != null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_MASTER_ADDITION_ALREADY_PRESENT());
        }
        logger.log(Level.DEBUG," addAttributeMetaData : start sourceId" +attributeMetadataTO.getAttributeVerifiers().get(0).getSourceId());
        return iamExtensionService.createAttributeMetadata(attributeMetadataTO);
    }

    @Override
    public AttributeMetadataTO approveEditAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO) throws AuthException {
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
        }
        validUpdationAttributeMetaData(attributeMetadataTO, attributeMetadataTO2);
        return iamExtensionService.editAttributeMetadata(attributeMetadataTO.getAttributeName(), attributeMetadataTO);
    }

    @Override
    public AttributeMetadataTO approveDeleteAddAttributeMetaDataRequest(AttributeMetadataTO attributeMetadataTO) throws AuthException {
        AttributeMetadataTO attributeMetadataTO2 = iamExtensionService.getAttributeMetadataForAttributeName(attributeMetadataTO.getAttributeName());
        if (attributeMetadataTO2 == null) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_META_DATA_NOT_FOUND(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_META_DATA_NOT_FOUND());
        }
        return iamExtensionService.deleteAttributeMetadata(attributeMetadataTO);
    }

    @Override
    public void validUpdationAttributeMetaData(AttributeMetadataTO attributeMetadataTO, AttributeMetadataTO attributeMetadataTO2) throws AuthException {
        logger.log(Level.DEBUG,"attributeMetadataTO : "+new Gson().toJson(attributeMetadataTO));
        boolean attributeName = attributeMetadataTO.getAttributeName().equals(attributeMetadataTO2.getAttributeName());
        boolean attributeType = attributeMetadataTO.getAttributeType().equals(attributeMetadataTO2.getAttributeType());
        boolean attributeStoreSecurityPolicy = attributeMetadataTO.getAttributeStoreSecurityPolicy().equals(attributeMetadataTO2.getAttributeStoreSecurityPolicy());
        boolean attributeValueModel = attributeMetadataTO.getAttributeValueModel().equals(attributeMetadataTO2.getAttributeValueModel());
        boolean isUnique= attributeMetadataTO.getIsUnique().equals(attributeMetadataTO2.getIsUnique());
        boolean attributeSettings = attributeMetadataTO.getAttributeSettings().keySet().size() == attributeMetadataTO2.getAttributeSettings().keySet().size();
        logger.log(Level.DEBUG,"attributeSettings 1 : "+attributeSettings);
        if(attributeSettings) {

            for (String key : attributeMetadataTO.getAttributeSettings().keySet()) {
                logger.log(Level.DEBUG,"key : "+key+" type : "+attributeMetadataTO.getAttributeSettings().get(key).getClass());
                if(attributeMetadataTO.getAttributeSettings().get(key).getClass().equals(String.class) ) {
                    attributeSettings = attributeMetadataTO.getAttributeSettings().get(key).equals(attributeMetadataTO2.getAttributeSettings().get(key));
                }
                if(attributeMetadataTO.getAttributeSettings().get(key).getClass().equals(Integer.class) ) {
                    logger.log(Level.DEBUG, "int request key : "+attributeMetadataTO.getAttributeSettings().get(key));
                    logger.log(Level.DEBUG, "int db key : "+attributeMetadataTO2.getAttributeSettings().get(key));
                    if(attributeMetadataTO.getAttributeSettings().get(key).getClass().equals(Integer.class)) {
                       Object value= attributeMetadataTO2.getAttributeSettings().get(key);
                       Object newValue = attributeMetadataTO.getAttributeSettings().get(key);
                       if(value==null && newValue!=null||value!=null && newValue==null){
                           attributeSettings=false;
                       }else if (!value.equals(newValue)){
                           attributeSettings=false;
                       }
                    }
                }
                if(attributeMetadataTO.getAttributeSettings().get(key).getClass().equals(Double.class) ) {
                    logger.log(Level.DEBUG, "request key : "+attributeMetadataTO.getAttributeSettings().get(key));
                    logger.log(Level.DEBUG, "db key : "+attributeMetadataTO2.getAttributeSettings().get(key));
                    attributeSettings =  ((Double)attributeMetadataTO.getAttributeSettings().get(key)) == ((Double)attributeMetadataTO2.getAttributeSettings().get(key));
                }
                logger.log(Level.DEBUG,"attributeSettings : key "+key+" - "+attributeSettings);
                if(!attributeSettings) {
                    break;
                }
            }
        }
        boolean applicableAccountTypes = attributeMetadataTO.getApplicableAccountTypes().equals(attributeMetadataTO2.getApplicableAccountTypes());
        boolean enterpriseVerifier = attributeMetadataTO.getAttributeVerifiers().equals(attributeMetadataTO2.getAttributeVerifiers());
        //boolean isVerifiesSettingUpdate = attributeMetadataTO.equals(attributeMetadataTO2);
        logger.log(Level.DEBUG,"attributeName : "+attributeName);
        logger.log(Level.DEBUG,"attributeType : "+attributeType);
        logger.log(Level.DEBUG,"attributeStoreSecurityPolicy : "+attributeStoreSecurityPolicy);
        logger.log(Level.DEBUG,"attributeValueModel : "+attributeValueModel);
        logger.log(Level.DEBUG,"attributeSettings : "+attributeSettings);
        logger.log(Level.DEBUG,"applicableAccountTypes : "+applicableAccountTypes);
        logger.log(Level.DEBUG,"enterpriseVerifier : "+enterpriseVerifier);
        if (attributeName && attributeType && attributeStoreSecurityPolicy && attributeValueModel && attributeSettings && applicableAccountTypes && enterpriseVerifier&&isUnique) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EXISTING_AND_UPDATED_DATA_IS_SAME(), errorConstant.getERROR_MESSAGE_EXISTING_AND_UPDATED_DATA_IS_SAME());
        }

    }

    @Override
    public List<AttributeMetadataTO> getAllAttributeMetaData() throws AuthException {

        /*if (!attributeMetadataTos.isEmpty()){
            return attributeMetadataTos;
        }
        attributeMetadataTos.addAll(iamExtensionService.getAllAttributeMetaData(null, null));
        return attributeMetadataTos;*/
        AttributeMetadataWTO metadataWTO;
        String stringMetadata;
        if((stringMetadata=attributeMetadataStore.get(ATTRIBUTE_METADATA_CACHE))!=null){
            logger.log(Level.DEBUG,"getAllAttributeMetaData : Retrieving from cache key:"+ATTRIBUTE_METADATA_CACHE);
            metadataWTO=StringUtil.fromJson(stringMetadata,AttributeMetadataWTO.class);
            return metadataWTO.getAttributeMetadataTOs();
        }
        else{
            synchronized (attributeMetadataStore){
                if((stringMetadata=attributeMetadataStore.get(ATTRIBUTE_METADATA_CACHE))!=null){
                    logger.log(Level.DEBUG,"getAllAttributeMetaData : Retrieving from cache key:"+ATTRIBUTE_METADATA_CACHE);
                    metadataWTO=StringUtil.fromJson(stringMetadata,AttributeMetadataWTO.class);
                    return metadataWTO.getAttributeMetadataTOs();
                }
                else{
                    metadataWTO = new AttributeMetadataWTO(iamExtensionService.getAllAttributeMetaData(null, null));
                    logger.log(Level.DEBUG,"getAllAttributeMetaData : Retrieving from api and adding to cache" + ATTRIBUTE_METADATA_CACHE);
                    attributeMetadataStore.putIfAbsent(ATTRIBUTE_METADATA_CACHE,StringUtil.toJson(metadataWTO));
                    return metadataWTO.getAttributeMetadataTOs();
                }
            }
        }
        //return iamExtensionService.getAllAttributeMetaData(null, null);
    }

    @Override
    public PaginatedTO<AttributeMetadataTO> getAllAttributeMetaData(int page, int limit, String searchText) throws AuthException {
        PaginatedTO<AttributeMetadataTO> attributeMetadataTos = iamExtensionService.getAllAttributeMetaData(page, limit, searchText);
        return attributeMetadataTos;
    }
}
