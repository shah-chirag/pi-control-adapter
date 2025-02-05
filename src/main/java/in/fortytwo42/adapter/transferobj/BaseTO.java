/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

/**
 * @author ChiragShah
 *
 */
public abstract class BaseTO {

    private Integer version;

    private Long id;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
