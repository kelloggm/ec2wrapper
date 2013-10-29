package com.amazonaws.wrapper.model;

public class AdapterSettings {

    private boolean isEmulation;
    private String instancePrefix;

    public boolean isEmulation() {
        return isEmulation;
    }

    public void setEmulation(boolean isEmulation) {
        this.isEmulation = isEmulation;
    }

    public String getInstancePrefix() {
        return instancePrefix;
    }

    public void setInstancePrefix(String instancePrefix) {
        this.instancePrefix = instancePrefix;
    }

}
