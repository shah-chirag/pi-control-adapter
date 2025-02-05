package in.fortytwo42.adapter.util;

import java.time.Duration;

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

import in.fortytwo42.daos.dao.ConfigDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;

public class ExternalConfigUtil {
	private static final String EXTERNAL_CONFIG_UTIL = "<<<<< ExternalConfigUtil";
	private static String EXTERNAL_CONFIG_CACHE = "externalConfigCache";
	private static Logger logger= LogManager.getLogger(ExternalConfigUtil.class);
	private final ConfigDaoIntf configDao= DaoFactory.getConfigDao();
	private final Config config = Config.getInstance();
	private static final class InstanceHolder {

		private static final ExternalConfigUtil INSTANCE = new ExternalConfigUtil();

		private InstanceHolder() {
			super();
		}
	}
	public static ExternalConfigUtil getInstance() {
		return ExternalConfigUtil.InstanceHolder.INSTANCE;
	}
	private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
	private final CacheConfiguration<String, String> externalConfigCacheConfiguration = CacheConfigurationBuilder
			.newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100000))
			.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(Integer.parseInt(Config.getInstance().getProperty(Constant.EXTERNAL_CONFIG_CACHE_TIMEOUT_IN_SECONDS)!=null?Config.getInstance().getProperty(Constant.EXTERNAL_CONFIG_CACHE_TIMEOUT_IN_SECONDS):"1800"))))
			.build();
	private final Cache<String, String> externalConfigStore = cacheManager.createCache("externalConfigCache", externalConfigCacheConfiguration);
	public String getProperty(String key, String type){
		String value="";
		String cacheKey=key+type;
		if((value=externalConfigStore.get(cacheKey))!=null){
			logger.log(Level.DEBUG,EXTERNAL_CONFIG_UTIL+" : Retrieving from cache : "+key+" "+value);
			return value;
		}
		else{
			synchronized (externalConfigStore){
				if((value=externalConfigStore.get(cacheKey))!=null){
					logger.log(Level.DEBUG,EXTERNAL_CONFIG_UTIL+" : Retrieving from cache : "+key+" "+value);
					return value;
				}
				else{
					try {
						in.fortytwo42.entities.bean.Config dbConfig = configDao.getConfigValue(key, type);
						value=dbConfig.getValue();
						logger.log(Level.DEBUG,EXTERNAL_CONFIG_UTIL+" : Retrieving from DB and adding to cache : "+key+" "+ value);
					}catch (Exception e){
						logger.log(Level.ERROR,EXTERNAL_CONFIG_UTIL,e);
						value=config.getProperty(key);
						logger.log(Level.DEBUG,EXTERNAL_CONFIG_UTIL+" : Retrieving from config and adding to cache : "+key+" "+ value);
					}
					if(value!=null && !value.isEmpty()) {
						externalConfigStore.putIfAbsent(cacheKey, value);
						return value;
					}
					return null;
				}
			}
		}
	}

}
