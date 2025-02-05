
package in.fortytwo42.adapter.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.adapter.util.Constant;
import in.fortytwo42.adapter.util.FileUtil;
import in.fortytwo42.daos.dao.AttributeStoreDaoIntf;
import in.fortytwo42.daos.dao.DaoFactory;
import in.fortytwo42.daos.dao.EvidenceDaoIntf;
import in.fortytwo42.daos.exception.NotFoundException;
import in.fortytwo42.entities.bean.AttributeStore;
import in.fortytwo42.entities.bean.EvidenceStore;
import in.fortytwo42.entities.util.EntityToTOConverter;
import in.fortytwo42.tos.transferobj.EvidenceStoreTO;

public class EvidenceStoreServiceImpl implements EvidenceStoreServiceIntf {

    private ErrorConstantsFromConfigIntf errorConstant=ServiceFactory.getErrorConstant();

    private EvidenceDaoIntf evidenceDao = DaoFactory.getEvidenceDao();
    private AttributeStoreDaoIntf attributeStoreDao = DaoFactory.getAttributeStoreDao();

    private Config config = Config.getInstance();
    /**
     * creation of log 4j object for each class
     */
    private static Logger logger= LogManager.getLogger(EvidenceStoreServiceImpl.class);

    private static final class InstanceHolder {
        private static final EvidenceStoreServiceImpl INSTANCE = new EvidenceStoreServiceImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static EvidenceStoreServiceImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public List<EvidenceStoreTO> getEvidenceByAttributeId(Long attributeId) {
        List<EvidenceStore> evidences;
        try {
            evidences = evidenceDao.getEvidenceByAttributeId(attributeId);
        }
        catch (NotFoundException e) {
            return new ArrayList<>();
        }
        return new EntityToTOConverter<EvidenceStore, EvidenceStoreTO>().convertEntityListToTOList(evidences);
    }

    @Override
    public void storeEvidence(Session session, Long attributeId, List<String> evidences) throws AuthException {
        //Folder location fetched from Config to store the evidences.
        String basePath = config.getProperty(Constant.EVIDENCE_STORE_PATH);
        List<EvidenceStore> evidenceStoreList = new ArrayList<>();
        for (Iterator<String> iterator = evidences.iterator(); iterator.hasNext();) {
            String evidence = (String) iterator.next();
            //TODO: media type hardcoded as image/jpeg. To be updated for supporting different file types.
            String[] imageData = evidence.split(",");
            String mediaType = "image/jpeg";//StringUtils.substringBetween(imageData[0], ":", ";");
            String evidencePath = basePath + attributeId + "_" + System.currentTimeMillis() + FileUtil.getExtension(mediaType);
            FileUtil.saveImageToPath(evidence, evidencePath);

            EvidenceStore evidenceStore = new EvidenceStore();

            AttributeStoreDaoIntf attributeStoreDaoIntf = attributeStoreDao;
            try {
                evidenceStore.setAttributeStore(attributeStoreDaoIntf.getById(attributeId));
            }
            catch (NotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT());
            }
            evidenceStore.setFilePath(evidencePath);
            evidenceStore.setMediaType(mediaType);
            AttributeStore attributeStore;
            try {
                attributeStore = attributeStoreDao.getById(attributeId);
            }
            catch (NotFoundException e) {
                throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT());
            }
            evidenceStore.setAttributeStore(attributeStore);
            evidenceStoreList.add(evidenceStore);
        }
        evidenceDao.bulkInsert(session, evidenceStoreList);
    }

    @Override
    public void storeEvidence(Session session, Long attributeId, Long evidenceId) throws AuthException {
        logger.log(Level.INFO, "storeEvidenceNew.." + evidenceId);
        /*EvidenceStore evidenceStore = EvidenceStoreProcessorImpl.getInstance().getEvidenceByEvidenceId(evidenceId);
        String evidenceData = getHashForEvidence(evidenceId);
        System.out.println("evidenceData....>>"+evidenceData);*/

        EvidenceStore evidenceStore = getEvidenceByEvidenceId(evidenceId);
        File evidenceFile = new File(evidenceStore.getFilePath());
        String evidenceData = encodeFileToBase64Binary(evidenceFile);

        String basePath = config.getProperty(Constant.EVIDENCE_STORE_PATH);

        String[] path = evidenceStore.getFilePath().split("_");

        String evidencePath = basePath + attributeId + "_" + path[1];
        logger.log(Level.INFO, "evidencePath...>>" + evidencePath);
        File file = new File(evidenceStore.getFilePath());
        file.delete();
        FileUtil.saveImageToPath(evidenceData, evidencePath);

        AttributeStoreDaoIntf attributeStoreDaoIntf = attributeStoreDao;
        try {
            evidenceStore.setAttributeStore(attributeStoreDaoIntf.getById(attributeId));
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_ATTRIBUTE_NOT_PRESENT(), errorConstant.getERROR_MESSAGE_ATTRIBUTE_NOT_PRESENT());
        }
        evidenceStore.setFilePath(evidencePath);
        evidenceDao.update(session, evidenceStore);
        logger.log(Level.INFO, "storeEvidenceNew..END>>>");
    }

    @Override
    public void storeEvidenceUsingAttributeStore(Session session, AttributeStore attributeStore, Long evidenceId) throws AuthException {
        EvidenceStore evidenceStore = getEvidenceByEvidenceId(evidenceId);
        File evidenceFile = new File(evidenceStore.getFilePath());
        String evidenceData = encodeFileToBase64Binary(evidenceFile);

        String basePath = config.getProperty(Constant.EVIDENCE_STORE_PATH);

        String[] path = evidenceStore.getFilePath().split("_");

        String evidencePath = basePath + attributeStore.getId() + "_" + path[1];
        logger.log(Level.INFO, "evidencePath...>>" + evidencePath);
        File file = new File(evidenceStore.getFilePath());
        file.delete();
        FileUtil.saveImageToPath(evidenceData, evidencePath);

        evidenceStore.setAttributeStore(attributeStore);

        evidenceStore.setFilePath(evidencePath);
        evidenceDao.update(session, evidenceStore);
        logger.log(Level.INFO, "storeEvidenceNew..END>>>");
    }

    public String getHashForEvidence(Long evidenceId) throws AuthException {
        logger.log(Level.INFO, "getHashForEvidence...Start....");
        EvidenceStore evidenceStore = getEvidenceByEvidenceId(evidenceId);
        if (evidenceStore.getFilePath() != null && !evidenceStore.getFilePath().isEmpty()) {
            String evidence = "";
            try {
                evidence = readFileAsString(evidenceStore.getFilePath());
            }
            catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            return evidence;
        }
        return null;
    }

    private static String readFileAsString(String fileName) throws Exception {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    @Override
    public EvidenceStore getEvidenceByEvidenceId(Long evidenceId) throws AuthException {
        try {
            return evidenceDao.getById(evidenceId);
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_EVIDENCE_NOT_FOUND());
        }
    }

    @Override
    public EvidenceStoreTO getEvidenceById(Long evidenceId) throws AuthException {
        try {
            return evidenceDao.getById(evidenceId).convertToTO();
        }
        catch (NotFoundException e) {
            throw new AuthException(null, errorConstant.getERROR_CODE_EVIDENCE_NOT_FOUND(), errorConstant.getERROR_MESSAGE_EVIDENCE_NOT_FOUND());
        }
    }

    @Override
    public List<EvidenceStoreTO> getEvidenceWithDataByAttributeId(Long attributeId) {
        List<EvidenceStore> evidences;
        try {
            evidences = evidenceDao.getEvidenceByAttributeId(attributeId);
        }
        catch (NotFoundException e) {
            return new ArrayList<>();
        }
        List<EvidenceStoreTO> evidenceStoreTOs = new EntityToTOConverter<EvidenceStore, EvidenceStoreTO>().convertEntityListToTOList(evidences);
        for (EvidenceStoreTO evidenceStoreTO : evidenceStoreTOs) {
            logger.log(Level.INFO, "evidenceStoreTO : " + new Gson().toJson(evidenceStoreTO));
            File evidenceFile = new File(evidenceStoreTO.getFilePath());
            String evidenceData = encodeFileToBase64Binary(evidenceFile);
            evidenceStoreTO.setEvidenceData(evidenceData);
        }
        logger.log(Level.INFO, "evidenceStoreTOs : " + new Gson().toJson(evidenceStoreTOs));
        return evidenceStoreTOs;
    }

    private static String encodeFileToBase64Binary(File file) {
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<EvidenceStoreTO> getEvidences(Long attributeId) {
        List<EvidenceStoreTO> evidenceTOs = new ArrayList<>();
        try {
            List<EvidenceStore> evidence = evidenceDao.getEvidenceByAttributeId(attributeId);
            for(EvidenceStore evidenceStore : evidence) {
                EvidenceStoreTO evidenceStoreTO = new EvidenceStoreTO();
                evidenceStoreTO.setFilePath(evidenceStore.getFilePath());
                evidenceStoreTO.setId(evidenceStore.getId());
                evidenceTOs.add(evidenceStoreTO);
            }
        }
        catch (NotFoundException e) {
            logger.log(Level.ERROR, e);
        }
        /*List<Object[]> evidences = evidenceDao.getEvidences(attributeId);
        
        for (Object[] object : evidences) {
            EvidenceStoreTO evidenceStoreTO = new EvidenceStoreTO();
            evidenceStoreTO.setFilePath((String) object[0]);
            evidenceStoreTO.setId((Long) object[1]);
            evidenceTOs.add(evidenceStoreTO);
        }*/
        return evidenceTOs;
    }

    @Override
    public EvidenceStoreTO storeEvidence(Session session, String fileName, byte[] evidenceData) {
        String basePath = config.getProperty(Constant.EVIDENCE_STORE_PATH);
        String mediaType = "image/" + FileUtil.getFileExtension(fileName);
        EvidenceStore evidenceStore = new EvidenceStore();
        evidenceStore.setFilePath(basePath);
        evidenceStore.setMediaType(mediaType);
        evidenceStore = evidenceDao.create(session, evidenceStore);
        String evidencePath = basePath + evidenceStore.getId() + "_" + System.currentTimeMillis() + FileUtil.getExtension(mediaType);
        FileUtil.saveImageToPath(evidenceData, evidencePath);
        evidenceStore.setFilePath(evidencePath);
        evidenceDao.update(session, evidenceStore);
        EvidenceStoreTO evidenceStoreTO = evidenceStore.convertToTO();
        evidenceStoreTO.setStatus(Constant.SUCCESS_STATUS);
        evidenceStoreTO.setFilePath(null);
        return evidenceStoreTO;
    }
    
    @Override
    public void saveEvidence(Session session, AttributeStore attributeStore, List<String> evidences) throws AuthException {
        //Folder location fetched from Config to store the evidences.
        String basePath = config.getProperty(Constant.EVIDENCE_STORE_PATH);
        List<EvidenceStore> evidenceStoreList = new ArrayList<>();
        for (Iterator<String> iterator = evidences.iterator(); iterator.hasNext();) {
            String evidence = (String) iterator.next();
            //TODO: media type hardcoded as image/jpeg. To be updated for supporting different file types.
            String[] imageData = evidence.split(",");
            String mediaType = "image/jpeg";//StringUtils.substringBetween(imageData[0], ":", ";");
            String evidencePath = basePath + attributeStore.getId() + "_" + System.currentTimeMillis() + FileUtil.getExtension(mediaType);
            FileUtil.saveImageToPath(evidence, evidencePath);
            EvidenceStore evidenceStore = new EvidenceStore();
            evidenceStore.setAttributeStore(attributeStore);
            evidenceStore.setFilePath(evidencePath);
            evidenceStore.setMediaType(mediaType);
            evidenceStoreList.add(evidenceStore);
        }
        evidenceDao.bulkInsert(session, evidenceStoreList);
    }
}
