
package in.fortytwo42.adapter.service;

import in.fortytwo42.adapter.transferobj.ClientLicenseTO;

/**
 * License related CRUD and validation done here.
 */
public interface LicenseServiceIntf {
    void uploadLicense(String encryptedLicenseFile);

    boolean isLicenseValid();

    boolean canOnboardUser();

    boolean canOnboardApplication();

    ClientLicenseTO getLicense();

    int getTotalNumberOfOnboardedUsers();

    int getTotalNumberOfApplications();


}
