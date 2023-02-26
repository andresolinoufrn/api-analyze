package br.ufrn.analyze.domain.dto.apimonitor;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.Set;


/**
 * Represents a CLUSTER of VMs or a DEPLOYMENT ( with PODs )
 */

public class GroupedItem {
    private Long id;


    @JsonBackReference
    private MonitorConfig monitorConfig;

    private String groupName;

    private String groupType;

    @JsonManagedReference
    private Set<IndividualItem> individualItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MonitorConfig getMonitorConfig() {
        return monitorConfig;
    }

    public void setMonitorConfig(MonitorConfig monitorConfig) {
        this.monitorConfig = monitorConfig;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Set<IndividualItem> getIndividualItems() {
        return individualItems;
    }

    public void setIndividualItems(Set<IndividualItem> itens) {
        this.individualItems = itens;
    }
}
