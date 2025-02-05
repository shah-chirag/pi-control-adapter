package in.fortytwo42.adapter.jar.entities;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import in.fortytwo42.ids.entities.beans.IdentityStoreBaseEntity;


@Entity(value = "fcm_notification_details", useDiscriminator = false)
public class FcmNotificationDetails extends IdentityStoreBaseEntity {

    @Property(value = "project_id")
    private String projectId;

    @Property(value="service_account_json")
    private String serviceAccountJson;

    @Property(value = "application_id")
    private String applicationId;

    @Property(value="package_name")
    private String packageName;

    @Property(value ="bundle_id" )
    private String bundleId;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getServiceAccountJson() {
        return serviceAccountJson;
    }

    public void setServiceAccountJson(String serviceAccountJson) {
        this.serviceAccountJson = serviceAccountJson;
    }
}
