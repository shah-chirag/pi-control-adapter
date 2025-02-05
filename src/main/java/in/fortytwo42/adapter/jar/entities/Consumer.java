
package in.fortytwo42.adapter.jar.entities;

import java.util.ArrayList;
import java.util.List;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.ShardKey;
import dev.morphia.annotations.ShardKeys;
import dev.morphia.mapping.ShardKeyType;
import in.fortytwo42.ids.entities.beans.IdentityStoreBaseEntity;

@Entity(value = "consumers", useDiscriminator = false)
@ShardKeys(@ShardKey(value = "account_id", type = ShardKeyType.HASHED))
public class Consumer extends IdentityStoreBaseEntity {

    @Property(value = "subscriptions")
    private List<Subscription> subscriptions;

    @Property(value = "account_id")
    private String accountId;

    @Property(value = "role")
    private Role role;


    public Consumer() {
        super();
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Consumer other = (Consumer) obj;
        if (accountId == null) {
            if (other.accountId != null)
                return false;
        }
        else if (!accountId.equals(other.accountId))
            return false;
        return true;
    }



    public void addSubscription(Subscription subscription) {
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }
        subscriptions.add(subscription);
    }
}
