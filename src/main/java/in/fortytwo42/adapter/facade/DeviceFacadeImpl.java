
package in.fortytwo42.adapter.facade;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import in.fortytwo42.adapter.enums.State;
import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.DeviceServiceIntf;
import in.fortytwo42.adapter.service.IamExtensionServiceIntf;
import in.fortytwo42.adapter.service.RequestServiceIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.service.UserServiceIntf;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.KeyValueTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.enterprise.extension.webentities.DeviceWE;
import in.fortytwo42.enterprise.extension.webentities.TokenWE;
import in.fortytwo42.entities.enums.RequestType;
import in.fortytwo42.tos.transferobj.UserTO;

public class DeviceFacadeImpl implements DeviceFacadeIntf {

    private DeviceFacadeImpl() {
        super();
    }

    private static final String DEVICE_FACADE_IMPL_LOG = "<<<<< DeviceFacadeImpl";
    private static Logger logger= LogManager.getLogger(DeviceFacadeImpl.class);
    private IamExtensionServiceIntf iamExtensionService = ServiceFactory.getIamExtensionService();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private RequestServiceIntf requestService = ServiceFactory.getRequestService();
    private DeviceServiceIntf deviceServiceIntf = ServiceFactory.getDeviceService();
    private UserServiceIntf userServiceIntf = ServiceFactory.getUserService();
    
    private static final class InstanceHolder {

        private static final DeviceFacadeImpl INSTANCE = new DeviceFacadeImpl();

        private InstanceHolder() {
            super();
        }
    }

    public static DeviceFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public PaginatedTO<DeviceTO> getDevices(Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDevices : start");
        PaginatedTO<DeviceWE> devices = iamExtensionService.getDevices(page, pageSize);
        PaginatedTO<DeviceTO> deviceTOs = new PaginatedTO<>();
        deviceTOs.setList(getDeviceTOList(devices.getList()));
        deviceTOs.setTotalCount(devices.getTotalCount());
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDevices : end");
        return deviceTOs;
    }

    @Override
    public PaginatedTO<TokenTO> getTokens(String deviceId, Integer page, Integer pageSize) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getTokens : start");
        PaginatedTO<TokenWE> tokens = iamExtensionService.getDeviceTokens(deviceId, page, pageSize);
        PaginatedTO<TokenTO> tokenTOs = new PaginatedTO<>();
        tokenTOs.setList(getTokenTOList(tokens.getList()));
        tokenTOs.setTotalCount(tokens.getTotalCount());
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getTokens : end");
        return tokenTOs;
    }

    @Override
    public DeviceTO editDevice(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " editDevice : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            deviceTO = requestService.createEditDeviceRequest(session, deviceTO, actor,id, RequestType.EDIT_DEVICE,
                    saveRequest);
            if (!saveRequest) {
                deviceTO = deviceServiceIntf.editDevice(deviceTO);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " editDevice : end");
        }
        return deviceTO;
    }

    @Override
    public DeviceTO editDeviceBindToken(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " editDeviceBindToken : start");
        Session session = sessionFactoryUtil.getSession();
        try {

            deviceTO = requestService.createEditDeviceRequest(session, deviceTO, actor,id, RequestType.EDIT_DEVICE_BIND_TOKEN, saveRequest);
            if (!saveRequest) {
                deviceTO = deviceServiceIntf.editDeviceBindToken(deviceTO);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " editDeviceBindToken : end");
        }
        return deviceTO;
    }

    private List<DeviceTO> getDeviceTOList(List<DeviceWE> deviceWEs) {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDeviceTOList : start");
        List<DeviceTO> adapterDeviceTOs = new ArrayList<>();
        for (DeviceWE deviceWE : deviceWEs) {
            DeviceTO adapterDeviceTO = convertDeviceWEToTO(deviceWE);
            adapterDeviceTOs.add(adapterDeviceTO);
        }
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDeviceTOList : end");
        return adapterDeviceTOs;
    }
    
    private DeviceTO convertDeviceWEToTO(DeviceWE deviceWE) {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " convertDeviceWEToTO : start");
        DeviceTO adapterDeviceTO = new DeviceTO();
        adapterDeviceTO.setDeviceId(deviceWE.getId());
        adapterDeviceTO.setDeviceUDID(deviceWE.getDeviceUDID());
        adapterDeviceTO.setDeviceState(State.valueOf(deviceWE.getDeviceState()));
        adapterDeviceTO.setDeviceDetails(new Gson().fromJson(deviceWE.getDeviceDetails(), new TypeToken<ArrayList<KeyValueTO>>() {
        }.getType()));
        if (deviceWE.getPolicyId() != null) {
            adapterDeviceTO.setPolicyId(deviceWE.getPolicyId());
        }
        if (deviceWE.getTokens() != null) {
            adapterDeviceTO.setTokens(getTokenTOList(deviceWE.getTokens()));
        }
        adapterDeviceTO.setAccountIds(deviceWE.getAccountIds());
        if(deviceWE.getDeviceName() != null) {
            adapterDeviceTO.setDeviceName(deviceWE.getDeviceName());
        }
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " convertDeviceWEToTO : end");
        return adapterDeviceTO;
    }

    private List<TokenTO> getTokenTOList(List<TokenWE> tokenWEs) {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getTokenTOList : start");
        List<TokenTO> adapterTokenTOs = new ArrayList<>();
        for (TokenWE tokenWE : tokenWEs) {
            TokenTO adapterTokenTO = new TokenTO();
            adapterTokenTO.setTokenId(tokenWE.getId());
            adapterTokenTO.setTokenUDID(tokenWE.getTokenUDID());
            adapterTokenTO.setState(State.valueOf(tokenWE.getState()));
            adapterTokenTO.setType(tokenWE.getType());
            adapterTokenTO.setAccountIds(tokenWE.getAccountIds());
            if(tokenWE.getTokenName() != null) {
                adapterTokenTO.setTokenName(tokenWE.getTokenName());
            }
            adapterTokenTOs.add(adapterTokenTO);
        }
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getTokenTOList : end");
        return adapterTokenTOs;
    }

    @Override
    public DeviceTO getDevice(String deviceId) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDevice : start");
        DeviceWE deviceWE = iamExtensionService.getDevice(deviceId);
        DeviceTO deviceTO = convertDeviceWEToTO(deviceWE);
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getDevice : end");
        return deviceTO;
    }

    @Override
    public List<UserTO> getUserInfo(String deviceId) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getUserInfo : start");
        DeviceTO deviceTO = getDevice(deviceId);
        List<UserTO> users = userServiceIntf.getUsersInfo(deviceTO.getAccountIds(), deviceId);
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " getUserInfo : end");
        return users;
    }

    @Override
    public DeviceTO unbindUsersFromDevice(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " unbindUsersFromDevice : start");
        Session session = sessionFactoryUtil.getSession();
        try {

            deviceTO = requestService.createEditDeviceRequest(session, deviceTO, actor,id,RequestType.EDIT_DEVICE_UNBIND_USER, saveRequest);
            if (!saveRequest) {
                deviceTO = deviceServiceIntf.unbindUsersFromDevice(deviceTO);
            }
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " unbindUsersFromDevice : end");
        }
        return deviceTO;
    }
    
    @Override
    public DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException {
        logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " unbindUsersFromDevice : start");
        Session session = sessionFactoryUtil.getSession();
        try {
            deviceTO = deviceServiceIntf.unbindUsersFromDevice(deviceTO);
            sessionFactoryUtil.closeSession(session);
        }
        catch (AuthException e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
            logger.log(Level.DEBUG, DEVICE_FACADE_IMPL_LOG + " unbindUsersFromDevice : end");
        }
        return deviceTO;
    }
    
    public static void main(String[] args) {
        String d = "[{\"key\":\"PLATFORM\",\"value\":\"ANDROID\"},{\"key\":\"OS_VERSION\",\"value\":\"29\"},{\"key\":\"SIM_IDENTIFIER\"}]";
        List<KeyValueTO> dd = new Gson().fromJson(d,new TypeToken<ArrayList<KeyValueTO>>(){}.getType());
        System.out.println(dd.size());
    }
}
