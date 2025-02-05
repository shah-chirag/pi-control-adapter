
package in.fortytwo42.adapter.jar.entities;

import java.util.Date;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import in.fortytwo42.ids.entities.beans.IdentityStoreBaseEntity;

@Entity(useDiscriminator = false)
public class Subscription extends IdentityStoreBaseEntity {

    @Property(value = "account_id")
    private String accountId;

    @Property(value = "start_date")
    private Date startDate;

    @Property(value = "status")
    private Status status;

    public Subscription() {
        super();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
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
        Subscription other = (Subscription) obj;
        if (accountId == null) {
            if (other.accountId != null)
                return false;
        }
        else if (!accountId.equals(other.accountId))
            return false;
        return true;
    }


}
