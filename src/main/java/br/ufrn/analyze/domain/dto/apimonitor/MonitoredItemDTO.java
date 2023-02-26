package br.ufrn.analyze.domain.dto.apimonitor;

public class MonitoredItemDTO {
    private String itemType;        // CLUSTER, VM, DEPLOYMENT or POD
    private String itemName;        // name of item
    private String resourceType;    // CPU or MEMORY
    private Double totalResource;   // total available resource of item
    private Double usedResource;    // current resource usage
    private String resourceUnit;    // ex CORES for cpu or GB for memory



    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Double getTotalResource() {
        return totalResource;
    }

    public void setTotalResource(Double totalResource) {
        this.totalResource = totalResource;
    }

    public Double getUsedResource() {
        return usedResource;
    }

    public void setUsedResource(Double usedResource) {
        this.usedResource = usedResource;
    }

    public String getResourceUnit() {
        return resourceUnit;
    }

    public void setResourceUnit(String resourceUnit) {
        this.resourceUnit = resourceUnit;
    }

    @Override
    public String toString() {
        return "MonitoredItemNovo{" +
                "itemType='" + itemType + '\'' +
                ", itemName='" + itemName + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", totalResource=" + totalResource +
                ", usedResource=" + usedResource +
                ", resourceUnit='" + resourceUnit + '\'' +
                '}';
    }

}
