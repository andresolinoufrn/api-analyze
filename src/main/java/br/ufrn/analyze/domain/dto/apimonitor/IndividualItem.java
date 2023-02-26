package br.ufrn.analyze.domain.dto.apimonitor;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

/**
 * Represents a individual item, like a VM or a POD
 */

public class IndividualItem {

    private Long id;

    private String itemName;
    private String itemType;


    @JsonBackReference
    private GroupedItem groupedItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public GroupedItem getGroupedItem() {
        return groupedItem;
    }

    public void setGroupedItem(GroupedItem groupedItem) {
        this.groupedItem = groupedItem;
    }
}
