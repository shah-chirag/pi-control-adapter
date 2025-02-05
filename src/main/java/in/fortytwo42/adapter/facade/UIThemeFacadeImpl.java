package in.fortytwo42.adapter.facade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gson.Gson;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.adapter.service.ErrorConstantsFromConfigIntf;
import in.fortytwo42.adapter.service.ServiceFactory;
import in.fortytwo42.adapter.util.Config;
import in.fortytwo42.daos.dao.UIThemeDaoImpl;
import in.fortytwo42.daos.dao.UIThemeDaoIntf;
import in.fortytwo42.daos.exception.ThemeNotFoundException;
import in.fortytwo42.daos.util.SessionFactoryUtil;
import in.fortytwo42.entities.bean.Theme;
import in.fortytwo42.tos.transferobj.ColorTO;
import in.fortytwo42.tos.transferobj.ImageTO;
import in.fortytwo42.tos.transferobj.ThemeTO;

public class UIThemeFacadeImpl implements UIThemeFacadeIntf {

    private static final String UI_THEME_FACADE_IMPL_LOG = "<<<<< UIThemeFacadeImpl";
    private ErrorConstantsFromConfigIntf errorConstant= ServiceFactory.getErrorConstant();
    private static Logger logger= LogManager.getLogger(UIThemeFacadeImpl.class);
    private Config config = Config.getInstance();
    private SessionFactoryUtil sessionFactoryUtil = SessionFactoryUtil.getInstance();
    private UIThemeDaoIntf uiThemeDao = UIThemeDaoImpl.getInstance();
    private Gson gson = new Gson();

    private UIThemeFacadeImpl() {
        super();
    }

    private static final class InstanceHolder {
        private static final UIThemeFacadeImpl INSTANCE = new UIThemeFacadeImpl();

        private InstanceHolder() {

        }
    }

    public static UIThemeFacadeImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public ThemeTO getDefaultTheme() throws AuthException {
        logger.log(Level.DEBUG, UI_THEME_FACADE_IMPL_LOG + " getDefaultTheme : start");
        Session session = sessionFactoryUtil.openSessionWithoutTransaction();
        try {
            Transaction transaction = session.beginTransaction();
            Theme theme = uiThemeDao.getDefaultTheme(session);
            transaction.commit();
            ThemeTO themeTO = theme.convertToTO();
            themeTO.setColors(gson.fromJson((String) themeTO.getColors(), ColorTO.class));
            themeTO.setImages(gson.fromJson((String) themeTO.getImages(), ImageTO.class));
            return themeTO;
        } catch (ThemeNotFoundException e) {
            session.getTransaction().rollback();
            logger.log(Level.ERROR, e.getMessage(), e);
            throw new AuthException(e, errorConstant.getERROR_CODE_THEME_NOT_FOUND(), errorConstant.getERROR_MESSAGE_THEME_NOT_FOUND());
        } finally {
            sessionFactoryUtil.closeSessionWithoutCommit(session);
            logger.log(Level.DEBUG, UI_THEME_FACADE_IMPL_LOG + " getDefaultTheme : end");
        }
    }
}
