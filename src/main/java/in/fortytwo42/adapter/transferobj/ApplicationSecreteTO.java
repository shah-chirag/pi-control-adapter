
package in.fortytwo42.adapter.transferobj;

public class ApplicationSecreteTO {
    private String applicationId;

    private String applicationSecrete;

    public ApplicationSecreteTO(String applicationId, String applicationSecrete) {
        super();
        this.applicationId = applicationId;
        this.applicationSecrete = applicationSecrete;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecrete() {
        return applicationSecrete;
    }

    public void setApplicationSecrete(String applicationSecrete) {
        this.applicationSecrete = applicationSecrete;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((applicationSecrete == null) ? 0 : applicationSecrete.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationSecreteTO other = (ApplicationSecreteTO) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        }
        else if (!applicationId.equals(other.applicationId))
            return false;
        if (applicationSecrete == null) {
            if (other.applicationSecrete != null)
                return false;
        }
        else if (!applicationSecrete.equals(other.applicationSecrete))
            return false;
        return true;
    }

}
