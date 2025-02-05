package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.DeviceTO;

public interface DeviceServiceIntf {

    /**
     * 
     * @param deviceTO
     * @return
     * @throws AuthException 
     */
    DeviceTO editDevice(DeviceTO deviceTO) throws AuthException;

    /**
     * 
     * @param deviceTO
     * @return
     * @throws AuthException 
     */
    DeviceTO editDeviceBindToken(DeviceTO deviceTO) throws AuthException;

    DeviceTO unbindUsersFromDevice(DeviceTO deviceTO) throws AuthException;

}
