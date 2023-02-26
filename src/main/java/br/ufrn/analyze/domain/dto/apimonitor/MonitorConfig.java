package br.ufrn.analyze.domain.dto.apimonitor;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.Set;


public class MonitorConfig {
    private Long id;

    private String configName;

    @JsonManagedReference
    private Set<GroupedItem> groupedItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Set<GroupedItem> getGroupedItems() {
        return groupedItems;
    }

    public void setGroupedItems(Set<GroupedItem> monitoredItems) {
        this.groupedItems = monitoredItems;
    }
}
