package in.fortytwo42.adapter.cam.util;

import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import in.fortytwo42.adapter.exception.CamUserExistsException;

public class CamResponseUtil {

    CamResponseUtil() {

    }
    public static String getCreatedId(Response response) throws CamUserExistsException {
        URI location = response.getLocation();
        if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
            Response.StatusType statusInfo = response.getStatusInfo();
            if(409 == statusInfo.getStatusCode()){
                throw new CamUserExistsException();
            }
            throw new WebApplicationException("Create method returned status " +
                    statusInfo.getReasonPhrase() + " (Code: " + statusInfo.getStatusCode() + "); expected status: Created (201)", response);
        }
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
