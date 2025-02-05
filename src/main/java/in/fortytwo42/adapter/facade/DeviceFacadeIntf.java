
package in.fortytwo42.adapter.facade;

import java.util.List;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.DeviceTO;
import in.fortytwo42.adapter.transferobj.PaginatedTO;
import in.fortytwo42.adapter.transferobj.TokenTO;
import in.fortytwo42.tos.transferobj.UserTO;

public interface DeviceFacadeIntf {

    /**
     * 
     * @param page
     * @param pageSize
     * @return
     * @throws AuthException 
     */
    PaginatedTO<DeviceTO> getDevices(Integer page, Integer pageSize) throws AuthException;

    /**
     * 
     * @param deviceUDID
     * @param page
     * @param pageSize
     * @return
     * @throws AuthException 
     */
    PaginatedTO<TokenTO> getTokens(String deviceUDID, Integer page, Integer pageSize) throws AuthException;

    /**
     * 
     * @param deviceTO
     * @param actor
     * @param saveRequest
     * @return
     * @throws AuthException 
     */
    DeviceTO editDevice(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException;

    /**
     * 
     * @param deviceTO
     * @param actor
     * @param saveRequest
     * @return
     * @throws AuthException 
     */
    DeviceTO editDeviceBindToken(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException;

    /**
     * 
     * @param deviceId
     * @return
     * @throws AuthException 
     */
    DeviceTO getDevice(String deviceId) throws AuthException;

    /**
     * 
     * @param deviceId
     * @throws AuthException 
     */
    List<UserTO> getUserInfo(String deviceId) throws AuthException;

    DeviceTO unbindUsersFromDevice(DeviceTO deviceTO, String actor,Long id, boolean saveRequest) throws AuthException;

    DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException;

}
