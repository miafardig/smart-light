package com.mia.smartlight.model;

import java.util.List;

public class NexaUnit {

    private String name;
    private int id;
    private String className;
    private String category;
    private List<String> actions;
    private List<Attribute> attributes;

    public NexaUnit(String name, int id, String className, String category, List<String> actions, List<Attribute> attributes) {

        this.name = name;
        this.id = id;
        this.className = className;
        this.category = category;
        this.actions = actions;
        this.attributes = attributes;
    }

    public NexaUnit(String name, int id, String category) {
        this.name = name;
        this.id = id;
        this.category = category;
    }

    public NexaUnit() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return name;
    }
}