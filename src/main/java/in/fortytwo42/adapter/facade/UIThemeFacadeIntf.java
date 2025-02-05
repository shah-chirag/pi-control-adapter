package in.fortytwo42.adapter.facade;

import in.fortytwo42.adapter.exception.AuthException;
import in.fortytwo42.tos.transferobj.ThemeTO;

public interface UIThemeFacadeIntf {

    public ThemeTO getDefaultTheme() throws AuthException;
}