package in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.tos.transferobj.FalloutConfigTO;

public interface FalloutConfigServiceIntf {
    List<FalloutConfigTO> getConfigs();

    FalloutConfigTO getConfigById(Long id) throws AuthException;

    void editFalloutConfig(Session session, FalloutConfigTO falloutConfigTO, String role, String actor) throws AuthException;
}
