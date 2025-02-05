
package in.fortytwo42.adapter.jar;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;
import dev.morphia.transactions.MorphiaSession;
import in.fortytwo42.adapter.jar.exception.BadDataException;
import in.fortytwo42.adapter.jar.exception.DatabaseError;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.enterprise.extension.tos.AttributeTO;
import in.fortytwo42.enterprise.extension.utils.SHAImpl;
import in.fortytwo42.ids.entities.beans.Account;
import in.fortytwo42.ids.entities.beans.Attribute;
import in.fortytwo42.ids.entities.beans.AttributeMetadata;
import in.fortytwo42.ids.entities.enums.AccountType;
import in.fortytwo42.ids.entities.enums.AttributeSecurityType;
import in.fortytwo42.ids.entities.enums.AttributeStatus;
public final class MongoConnectionManager implements Closeable {

    private static final String FALLOUT_FACDE_IMPL_LOG = "<<<<< MongoConnectionManager";
    private static Logger logger= LogManager.getLogger(MongoConnectionManager.class);
	private final Datastore db;
    private final MongoClient mongoClient;

    private Config config = Config.getInstance();
    private static final String ATTRIBUTES = "attributes";

    private static final String ONLY_ATTRIBUTE_NAME = "attribute_name";
    private static final String ONLY_ATTRIBUTE_VALUE = "attribute_value";
    private static final String ATTRIBUTE_STATUS = "status";

    private MongoConnectionManager() {
        try {
            String[] serverAddresses = getServerAddresses();
            String databaseName = getDatabaseName();
            String databaseUsername = getDatabaseUsername();
            String databasePassword = getDatabasePassword();
            String replicaSetName = getReplicaSetName();
            StringBuilder connectionString = new StringBuilder("mongodb://");
//            connectionString.append(adminUser);
//            connectionString.append(password);
//            connectionString.append("@");
//            connectionString.append(accountName);
            int count = 0;
            for (String string : serverAddresses) {
                connectionString.append(string);
                count++;
                if (serverAddresses.length != count) {
                    connectionString.append(",");
                }
            }
             if (!Boolean.parseBoolean(config.getProperty(MongoConstants.DATABASE_SHARDED)))
                 connectionString.append(String.format("/?replicaSet=%s", replicaSetName));
            System.out.println("Connection String : " + connectionString);
            String finalConnectionString = connectionString.toString();
            System.out.println("Final connection String : " + finalConnectionString);
            ConnectionString connection = new ConnectionString(finalConnectionString);
            boolean isSSLPinningOn = Boolean.parseBoolean(config.getProperty(MongoConstants.HTTP_SSL_STATE));
            SSLContext sslContext = null;
            if (isSSLPinningOn) {
                String keyStoreType = config.getProperty(MongoConstants.SSL_KEYSTORE_TYPE);
                String keyStoreFilePath = config.getProperty(MongoConstants.CLIENT_CERT_FILE_PATH);
                String keyStorePassword = config.getProperty(MongoConstants.SSL_KEYSTORE_PASSWORD);
                String trustStoreFilePath = config.getProperty(MongoConstants.SSL_TRUSTSTORE_FILE_PATH);
                String trustStorePassword = config.getProperty(MongoConstants.SSL_TRUSTSTORE_PASSWORD);
                KeyStore trustStore = SSLUtil.getKeyStore(KeyStore.getDefaultType(), trustStoreFilePath, trustStorePassword);
                KeyStore clientStore = SSLUtil.getKeyStore(keyStoreType, keyStoreFilePath, keyStorePassword);
                sslContext = SSLContexts.custom().loadKeyMaterial(clientStore, keyStorePassword.toCharArray()).loadTrustMaterial(trustStore, null).build();
            }
            boolean isMongoSSLStateOn = Boolean.parseBoolean(config.getProperty(MongoConstants.MONGO_SSL_STATE));
            String authenticationDbName = config.getProperty(MongoConstants.AUTHENTICATION_DB_NAME);
            MongoCredential mongoCredential = MongoCredential.createCredential(databaseUsername, authenticationDbName, databasePassword.toCharArray());
            SSLContext finalSslContext = sslContext;
            MongoClientSettings mongoClientSetting = MongoClientSettings.builder().credential(mongoCredential)
                    .applyToSslSettings(builder -> {
                        builder.invalidHostNameAllowed(Boolean.parseBoolean(config.getProperty(MongoConstants.MONGO_ALLOW_INVALID_HOSTNAMES)));
                        builder.enabled(isMongoSSLStateOn);
                        builder.context(finalSslContext);
                    })
                    .applyConnectionString(connection)
                    .applyToConnectionPoolSettings( builder -> {
                        builder.maxSize(200);
                    })
                    .build();

            mongoClient = MongoClients.create(mongoClientSetting);
            db =Morphia.createDatastore(mongoClient, databaseName);;
            System.out.println("<<<<<Connected!");


            db.getMapper().mapPackage("in.fortytwo42.ids.entities");
            db.ensureIndexes();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new UndeclaredThrowableException(e);
        }
    }

/*    public void update(String did) {
        try {
            MongoCollection<Document> collection = this.db.getCollection("ci0esc_storeV2");
            List<Object> list = new ArrayList<>();
            collection.updateMany(
                    Filters.eq("_id", did),
                    Filters.eq("$set", Filters.eq("iam-escs", list))
            );
            MongoCollection<Document> maxCollection = this.db.getCollection("ci0esc_maxVersion");
            Long num = 0L;
            maxCollection.updateMany(
                    Filters.eq("_id", did),
                    Filters.eq("$set", Filters.eq("maxVersion", num))
            );
            this.close();
            System.out.println("<<<<<< MONGO RESET Complete >>>>>>>>");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("<<<<<< MONGO RESET FAILED >>>>>>>>");
        }
    }*/

    public void remove(Set<ObjectId> ids) {
        try {
            MongoCollection<Account> collection = this.db.getCollection(Account.class);

            DeleteResult result = collection.deleteMany(
                    Filters.in("_id", ids)
            );
            System.out.println("Deleted IDS document count: " + result.getDeletedCount());
//            this.close();
            System.out.println("<<<<<< MONGO RESET Complete IDS >>>>>>>>");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("<<<<<< MONGO RESET FAILED IDS>>>>>>>>");
            throw e;
        }
    }

    public Account getAllUserAccountsByAccountId(String accountId) throws Exception {
        try {
            Query<Account> query = getDatastore().find(Account.class);
            FindOptions findOptions = new FindOptions();
            query.filter(dev.morphia.query.filters.Filters.eq("_id", new ObjectId(accountId)));
            findOptions.projection().include("_id","account_state","is_token_enabled","crypto_did","account_type","attributes");

            Account account = handleAccountResponse(query.iterator(findOptions).toList());
            return account;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Account> getAllUserAccountsOnlyId(Integer limit, Integer offset) {
        try {
            Query<Account> query = getDatastore().find(Account.class);
            FindOptions findOptions = new FindOptions();
            query.filter(dev.morphia.query.filters.Filters.eq("account_type", AccountType.USER));

            if (offset != null) {
                findOptions.skip(offset);
            }
            if (limit != null) {
                findOptions.limit(limit);
            }
            findOptions.projection().include("_id");
            return query.iterator(findOptions).toList();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Account> getAccountByMultipleAttributes(List<AttributeTO> attributes) {
        List<Account> accounts = new ArrayList<>();
        Query<Account> query = getDatastore().find(Account.class);
        FindOptions findOptions = new FindOptions();
        ArrayList<Filter> filters = new ArrayList<>();
        attributes.forEach((k) -> {
            //            filters.add(Filters.and(Filters.eq(ONLY_ATTRIBUTE_NAME, attributeName), Filters.eq(ONLY_ATTRIBUTE_VALUE, attributeValue), Filters.eq(ATTRIBUTE_STATUS, AttributeStatus.ACTIVE)));
            filters.add(dev.morphia.query.filters.Filters.elemMatch(ATTRIBUTES,
                    dev.morphia.query.filters.Filters.eq(ONLY_ATTRIBUTE_NAME, k.getAttributeName()), dev.morphia.query.filters.Filters.eq(ONLY_ATTRIBUTE_VALUE, k.getAttributeValue()),
                    dev.morphia.query.filters.Filters.eq(ATTRIBUTE_STATUS, AttributeStatus.ACTIVE)));
        });
        Filter[] arr = new Filter[filters.size()];
        arr = filters.toArray(arr);
        query.filter(dev.morphia.query.filters.Filters.and(arr));
        try {
            findOptions.projection().include("_id");
            accounts = query.iterator(findOptions).toList();
        }
        catch (Exception e) {
            throw e;
        }
        return accounts;
    }

    public Account getAccountByAccountId(String accountId) throws Exception {
        Query<Account> query = getDatastore().find(Account.class);
        List<Account> accountList = new ArrayList<>();
        query.filter(dev.morphia.query.filters.Filters.eq("_id", new ObjectId(accountId)));
        try {
            accountList = query.iterator().toList();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        Account account = handleAccountResponse(accountList);
        return account;
    }

    public AttributeMetadata getAttributeMetadata(String attributeName) {
        logger.log(Level.DEBUG, ">>>>> getAttributeMetadata : start");

        Query<AttributeMetadata> query = getDatastore().find(AttributeMetadata.class);
        List<AttributeMetadata> attributeMetadataList = new ArrayList<>();
        query.filter(dev.morphia.query.filters.Filters.eq("attributeName", attributeName), dev.morphia.query.filters.Filters.eq("status", AttributeStatus.ACTIVE));
        try {
            attributeMetadataList = query.iterator().toList();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new DatabaseError();
        }
        logger.log(Level.DEBUG, ">>>>> getAttributeMetadata : end");
        return handleAttributeMetadataResponse(attributeMetadataList);
    }

    private AttributeMetadata handleAttributeMetadataResponse(List<AttributeMetadata> attributeMetadataList) {
        if (attributeMetadataList.isEmpty()) {
            return null;
        }
        else if (attributeMetadataList.size() == 1) {
            return attributeMetadataList.get(0);
        }
        else {
            throw new DatabaseError();
        }
    }

    public void deleteAttribute(AttributeTO attributeTO, String id, MorphiaSession session) throws Exception {

        logger.log(Level.INFO, "AttributeTO : " + new Gson().toJson(attributeTO));
        Account account = getAccountByAccountId(id);
        if (account == null) {
            throw new BadDataException(1406L, "Account with given id not present");
        }
        attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
        AttributeMetadata attributeMetadata = getAttributeMetadata(attributeTO.getAttributeName());
        if (attributeMetadata == null) {
            attributeMetadata = getAttributeMetadata("others");
        }
        String hashedAttributeValue = applySecurityPolicy(attributeMetadata.getAttributeStoreSecurityPolicy(), attributeTO.getAttributeValue());
        attributeTO.setAttributeValue(hashedAttributeValue);
        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeTO.getAttributeName());
        attribute.setAttributeValue(attributeTO.getAttributeValue());
        if (account.getAttributes().contains(attribute)) {
            for (Attribute existingAttribute : account.getAttributes()) {
                if (existingAttribute.equals(attribute) && !existingAttribute.getStatus().equals(AttributeStatus.DELETE)) {
                    existingAttribute.setStatus(AttributeStatus.DELETE);
                    break;
                }
            }
            session.save(account);
        }
        else {
            throw new BadDataException(1406L, "Account with given id not present");
        }

    }

    private String applySecurityPolicy(AttributeSecurityType attributeSecurityType, String attributeValue) {
        String hashedAttributeValue;
        switch (attributeSecurityType) {
            case OPEN:
                hashedAttributeValue = attributeValue;
                break;
            case SHA256:
                hashedAttributeValue = SHAImpl.get256Hash(attributeValue);
                break;
            case SHA512:
                hashedAttributeValue = SHAImpl.get512Hash(attributeValue);
                break;
            default:
                hashedAttributeValue = attributeValue;
                break;
        }
        return hashedAttributeValue.toUpperCase();
    }

    private Account handleAccountResponse(List<Account> accounts) throws Exception {
        if (accounts == null || accounts.isEmpty()) {
            throw new Exception();
        }
        else if (accounts.size() == 1) {
            return accounts.get(0);
        }
        else {
            throw new Exception();
        }
    }

    public void addAttribute(AttributeTO attributeTO, String id, MorphiaSession session) throws Exception {
        logger.log(Level.DEBUG, "MongoConnectionManager  addAttribute : start ");
        logger.log(Level.DEBUG, "MongoConnectionManager  addAttribute : attributeName : "+attributeTO.getAttributeName());
        logger.log(Level.DEBUG, "MongoConnectionManager  addAttribute : attributeValue : "+attributeTO.getAttributeValue());
        Account account = getAccountByAccountId(id);
        if (account == null) {
            throw new BadDataException(1406L, "Account with given id not present");
        }
        attributeTO.setAttributeName(attributeTO.getAttributeName().toUpperCase());
        boolean isAttributeMetadataOthers = false;
        AttributeMetadata attributeMetadata = getAttributeMetadata(attributeTO.getAttributeName());
        if (attributeMetadata == null) {
            attributeMetadata = getAttributeMetadata("others");
            isAttributeMetadataOthers = true;
        }
        String hashedAttributeValue = applySecurityPolicy(attributeMetadata.getAttributeStoreSecurityPolicy(), attributeTO.getAttributeValue());
        attributeTO.setAttributeValue(hashedAttributeValue);
        Attribute attribute = new Attribute();
        attribute.setAttributeName(attributeTO.getAttributeName());
        attribute.setAttributeValue(attributeTO.getAttributeValue());
        if (isAttributeMetadataOthers) {
            attribute.setAttributeTitle(attribute.getAttributeName());
        }
        else if (attributeMetadata.getAttributeSettings().get(Constant.TITLE) != null) {
            attribute.setAttributeTitle(attributeMetadata.getAttributeSettings().get(Constant.TITLE).toString());
        }
        attribute.setStatus(in.fortytwo42.ids.entities.enums.AttributeStatus.ACTIVE);
        Boolean isDefault = attributeTO.getIsDefault() != null && attributeTO.getIsDefault();
        attribute.setIsDefault(isDefault);
        attribute.setOperationStatus(in.fortytwo42.ids.entities.enums.AttributeOperationStatus.SUCCESSFUL);
        account.getAttributes().add(attribute);
        session.save(account);
        logger.log(Level.INFO, "MongoConnectionManager  addAttribute : end ");
    }

    /**
     *
     * @return database password
     */
    private String getDatabasePassword() {
        if (config.getProperty(MongoConstants.DATABASE_PASSWORD) != null) {

            return config.getProperty(MongoConstants.DATABASE_PASSWORD);
        }
        return MongoConstants.DEFAULT_DATABASE_PASSWORD;
    }

    /**
     * @return db username
     */
    private String getDatabaseUsername() {
        if (config.getProperty(MongoConstants.DATABASE_USERNAME) != null) {

            return config.getProperty(MongoConstants.DATABASE_USERNAME);
        }
        return MongoConstants.DEFAULT_DATABASE_USERNAME;
    }

    private String[] getServerAddresses() {
        String serverAddressCommaSeperated = config.getProperty(MongoConstants.REPLICA_SERVER_ADDRESSES);
        if (serverAddressCommaSeperated != null) {

            return serverAddressCommaSeperated.split(",");
        }
        String[] addr = new String[1];
        addr[0] = MongoConstants.LOCALHOST;
        return addr;
    }

    private String getDatabaseName() {
        if (config.getProperty(MongoConstants.DATABASE_NAME) != null) {
            return config.getProperty(MongoConstants.DATABASE_NAME);
        }
        return "iamdb";
    }

    private String getReplicaSetName() {
        if (config.getProperty(MongoConstants.REPLICA_SET_NAME) != null) {
            return config.getProperty(MongoConstants.REPLICA_SET_NAME);
        }
        return "rs0";
    }

    private static final class InstanceHolder {
        private static final MongoConnectionManager INSTANCE = new MongoConnectionManager();

        private InstanceHolder() {
            super();
        }
    }

    public static MongoConnectionManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Datastore getDatastore() {
        return db;
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }

}
