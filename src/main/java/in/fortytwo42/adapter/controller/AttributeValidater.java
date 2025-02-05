package in.fortytwo42.adapter.controller;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.AttributeDataRequestTO;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;

public interface AttributeValidater {

    public boolean validate(AttributeDataRequestTO attributeDataRequestTO, User user,AccountWE accountWE )throws AuthException;
    public void process(AttributeDataRequestTO attributeDataRequestTO, User user, Session session, AccountWE accountWE)throws AuthException;

}
