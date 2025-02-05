package in.fortytwo42.adapter.controller;

import org.hibernate.Session;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.transferobj.UserIciciTO;
import in.fortytwo42.enterprise.extension.core.IAMExtensionV2;
import in.fortytwo42.enterprise.extension.core.Token;
import in.fortytwo42.enterprise.extension.exceptions.UserFoundOnIDSException;
import in.fortytwo42.enterprise.extension.exceptions.ValidationException;
import in.fortytwo42.enterprise.extension.webentities.AccountWE;
import in.fortytwo42.entities.bean.User;

public interface Onboarder {

    public abstract boolean validate(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user) throws AuthException;

    public abstract void process(Token token, IAMExtensionV2 iamExtension, UserIciciTO userTO, AccountWE account, User user, Session session)
            throws AuthException, ValidationException, UserFoundOnIDSException;
}
