package  in.fortytwo42.adapter.service;

import java.util.List;

import org.hibernate.Session;

import in.fortytwo42.entities.bean.CallbackUrl;

public interface CallbackUrlServiceIntf {

    void bulkInsert(Session session, List<CallbackUrl> callbackUrls);

}
