/**
 * 
 */

package in.fortytwo42.adapter.transferobj;

/**
 * @author ChiragShah
 *
 */
public class FileStoragePolicyTO extends BaseTO {

    private Integer maxExportFileCount;

    private Integer maxImportFileCount;

    private Long maxFileSize;

    public Integer getMaxExportFileCount() {
        return maxExportFileCount;
    }

    public void setMaxExportFileCount(Integer maxExportFileCount) {
        this.maxExportFileCount = maxExportFileCount;
    }

    public Integer getMaxImportFileCount() {
        return maxImportFileCount;
    }

    public void setMaxImportFileCount(Integer maxImportFileCount) {
        this.maxImportFileCount = maxImportFileCount;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

  
}
