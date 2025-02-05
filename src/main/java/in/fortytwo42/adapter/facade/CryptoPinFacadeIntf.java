package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.CryptoPinTO;

public interface CryptoPinFacadeIntf {
    CryptoPinTO resetApplicationPin(CryptoPinTO cryptoPinTO) throws AuthException;
    CryptoPinTO changeApplicationPin(CryptoPinTO cryptoPinTO) throws AuthException;
    CryptoPinTO resetEnterprisePin(CryptoPinTO cryptoPinTO) throws AuthException;

    CryptoPinTO changeEnterprisePin(CryptoPinTO cryptoPinTO) throws AuthException;

}
