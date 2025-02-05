
package in.fortytwo42.adapter.jar;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import dev.morphia.transactions.MorphiaSession;
import in.fortytwo42.adapter.jar.entities.FcmNotificationDetails;
import in.fortytwo42.adapter.jar.exception.BadDataException;
import in.fortytwo42.daos.exception.NotFoundException;
import org.apache.http.ssl.SSLContexts;

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
import in.fortytwo42.adapter.jar.entities.Consumer;
import in.fortytwo42.adapter.jar.entities.Role;
import in.fortytwo42.adapter.util.Config;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

public final class MongoConnectionManagerIam implements Closeable {

    private final Datastore db;
    private final MongoClient mongoClient;

    private Config config = Config.getInstance();
    Logger logger= LogManager.getLogger(this.getClass());

    private MongoConnectionManagerIam() {
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
            String authenticationDbName = config.getProperty(MongoConstants.AUTHENTICATION_DB_NAME_IAM);
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
            db = Morphia.createDatastore(mongoClient, databaseName);;
            System.out.println("<<<<<Connected!");


            db.getMapper().mapPackage("in.fortytwo42.adapter.jar.entities");
            db.ensureIndexes();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new UndeclaredThrowableException(e);
        }
    }

    public List<Consumer> getAllConsumer(Integer limit, Integer offset) {
        try {
            Query<Consumer> query = getDatastore().find(Consumer.class);
            FindOptions findOptions = new FindOptions();
            query.filter(dev.morphia.query.filters.Filters.eq("role", Role.CONSUMER));

            if (offset != null) {
                findOptions.skip(offset);
            }
            if (limit != null) {
                findOptions.limit(limit);
            }
            findOptions.projection().include("_id", "subscriptions", "account_id", "role");

            return query.iterator(findOptions).toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
  /*  public void update(String did) {
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

    public void remove(List<String> ids) {
        try {
            MongoCollection<Consumer> collection = this.db.getCollection(Consumer.class);

            DeleteResult result = collection.deleteMany(
                    Filters.in("account_id", ids)
            );
            System.out.println("Deleted IAM document count: " + result.getDeletedCount());
            //            this.close();
            System.out.println("<<<<<< MONGO RESET Complete IAM >>>>>>>>");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("<<<<<< MONGO RESET FAILED IAM>>>>>>>>");
            throw e;
        }
    }

    private static final String ACCOUNT_OBJECT_ID = "accountId";

    public List<Consumer> getConsumerByAccountId(String accountId) throws Exception {
        Query<Consumer> query = getDatastore().find(Consumer.class);
        FindOptions findOptions = new FindOptions();
        query.filter(dev.morphia.query.filters.Filters.eq(ACCOUNT_OBJECT_ID, accountId));
        findOptions.projection().include("_id", "account_id", "role");
        return query.iterator(findOptions).toList();
    }

    private Consumer handleConsumerResponse(List<Consumer> consumers) throws Exception {
        if (consumers.isEmpty()) {
            throw new Exception();
        }
        else if (consumers.size() == 1) {
            return consumers.get(0);
        }
        else {
            throw new Exception();
        }
    }
    /**
     *
     * @return database password
     */
    private String getDatabasePassword() {
        if (config.getProperty(MongoConstants.DATABASE_PASSWORD_IAM) != null) {

            return config.getProperty(MongoConstants.DATABASE_PASSWORD_IAM);
        }
        return MongoConstants.DEFAULT_DATABASE_PASSWORD;
    }

    /**
     * @return db username
     */
    private String getDatabaseUsername() {
        if (config.getProperty(MongoConstants.DATABASE_USERNAME_IAM) != null) {

            return config.getProperty(MongoConstants.DATABASE_USERNAME_IAM);
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
        if (config.getProperty(MongoConstants.DATABASE_NAME_IAM) != null) {
            return config.getProperty(MongoConstants.DATABASE_NAME_IAM);
        }
        return "iamdb";
    }

    private String getReplicaSetName() {
        if (config.getProperty(MongoConstants.REPLICA_SET_NAME) != null) {
            return config.getProperty(MongoConstants.REPLICA_SET_NAME);
        }
        return "rs0";
    }

    public void createFcmNotificationDetails(FcmNotificationDetails fcmNotificationDetails) {
        MorphiaSession morphiaSession = MongoConnectionManagerIam.getInstance().getDatastore().startSession();
        morphiaSession.startTransaction();
        try {
            morphiaSession.save(fcmNotificationDetails);
            morphiaSession.commitTransaction();
        }catch (Exception e){
            logger.log(Level.ERROR, e.getMessage(), e);
            throw e;

        }finally {
            if (morphiaSession != null && morphiaSession.hasActiveTransaction()) {
                morphiaSession.abortTransaction();
            }
        }

    }

    public FcmNotificationDetails getFcmNotificationDetailsByID(String applicationId) throws Exception {
        Query<FcmNotificationDetails> query = getDatastore().find(FcmNotificationDetails.class);
        query.filter(dev.morphia.query.filters.Filters.eq("application_id", applicationId));
        List<FcmNotificationDetails> fcmNotificationDetailsList= new ArrayList<>();
        try {
            fcmNotificationDetailsList= query.iterator().toList();
        }
        catch (Exception e) {
            logger.log(Level.ERROR,e.getMessage(),e);
            throw e;
        }
        return handleAccountResponse(fcmNotificationDetailsList);

    }

    public <I> I update(I fcmNotificationDetails, MorphiaSession session) {
       return session.save(fcmNotificationDetails);

    }

    public void delete(FcmNotificationDetails fcmNotificationDetails,MorphiaSession morphiaSession) {
        morphiaSession.delete(fcmNotificationDetails);
    }

    private static final class InstanceHolder {
        private static final MongoConnectionManagerIam INSTANCE = new MongoConnectionManagerIam();

        private InstanceHolder() {
            super();
        }
    }

    public static MongoConnectionManagerIam getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Datastore getDatastore() {
        return db;
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }
    private <I> I handleAccountResponse(List<I> list) throws Exception {
        if (list == null || list.isEmpty()) {
            throw new NotFoundException();
        }
        else if (list.size() == 1) {
            return list.get(0);
        }
        else {
            throw new Exception();
        }
    }

}
